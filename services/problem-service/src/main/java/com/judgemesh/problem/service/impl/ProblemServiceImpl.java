package com.judgemesh.problem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.problem.config.MinioConfig;
import com.judgemesh.problem.converter.ProblemConverter;
import com.judgemesh.problem.entity.Problem;
import com.judgemesh.problem.entity.ProblemTag;
import com.judgemesh.problem.entity.TestcaseManifest;
import com.judgemesh.problem.mapper.ProblemMapper;
import com.judgemesh.problem.mapper.ProblemTagMapper;
import com.judgemesh.problem.mapper.TestcaseManifestMapper;
import com.judgemesh.problem.service.MinioService;
import com.judgemesh.problem.service.ProblemService;
import com.judgemesh.problem.vo.ProblemCreateReq;
import com.judgemesh.problem.vo.ProblemUpdateReq;
import com.judgemesh.problem.vo.TestcaseManifestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemMapper problemMapper;
    private final ProblemTagMapper problemTagMapper;
    private final ProblemConverter problemConverter;
    private final MinioService minioService;
    private final MinioConfig minioConfig;
    private final TestcaseManifestMapper testcaseManifestMapper;

    // Redis 模板 和 JSON 转换工具
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // 注入 Redisson 分布式锁客户端
    private final RedissonClient redissonClient;

    // 文档规定的缓存 Key 前缀
    private static final String CACHE_KEY_PREFIX = "cache:problem:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProblem(ProblemCreateReq req, Long setterId) {
        Problem problem = new Problem();
        problem.setTitle(req.getTitle());
        problem.setDescription(req.getDescription());
        problem.setTimeLimitMs(req.getTimeLimitMs());
        problem.setMemoryLimitMb(req.getMemoryLimitMb());
        problem.setDifficulty(req.getDifficulty());
        problem.setSetterId(setterId);
        problem.setPublished(false);
        problem.setTotalSubmit(0);
        problem.setTotalAc(0);
        problem.setCreatedAt(LocalDateTime.now());
        problem.setUpdatedAt(LocalDateTime.now());

        problemMapper.insert(problem);

        if (req.getTags() != null && !req.getTags().isEmpty()) {
            for (String tag : req.getTags()) {
                ProblemTag pt = new ProblemTag();
                pt.setProblemId(problem.getId());
                pt.setTag(tag);
                problemTagMapper.insert(pt);
            }
        }
        // 创建时不缓存，等第一次查询时再触发缓存加载
        return problem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProblem(Long id, ProblemUpdateReq req) {
        Problem problem = problemMapper.selectById(id);
        if (problem == null) return;

        if (req.getTitle() != null) problem.setTitle(req.getTitle());
        if (req.getDescription() != null) problem.setDescription(req.getDescription());
        if (req.getTimeLimitMs() != null) problem.setTimeLimitMs(req.getTimeLimitMs());
        if (req.getMemoryLimitMb() != null) problem.setMemoryLimitMb(req.getMemoryLimitMb());
        if (req.getDifficulty() != null) problem.setDifficulty(req.getDifficulty());
        if (req.getPublished() != null) problem.setPublished(req.getPublished());
        problem.setUpdatedAt(LocalDateTime.now());

        problemMapper.updateById(problem);

        if (req.getTags() != null) {
            problemTagMapper.delete(new LambdaQueryWrapper<ProblemTag>().eq(ProblemTag::getProblemId, id));
            for (String t : req.getTags()) {
                ProblemTag pt = new ProblemTag();
                pt.setProblemId(id);
                pt.setTag(t);
                problemTagMapper.insert(pt);
            }
        }

        // Cache-Aside 写策略：更新完数据库后，果断删除 Redis 缓存
        String cacheKey = CACHE_KEY_PREFIX + id;
        stringRedisTemplate.delete(cacheKey);
        log.info("题目 {} 更新完毕，已清除 Redis 缓存", id);
    }

    @Override
    public ProblemDTO getProblemDetail(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);

        // 1. 查缓存
        if (StringUtils.hasText(cachedJson)) {
            // 防穿透第一道防线：发现是之前存的空标记，直接返回 null，不打扰 MySQL
            if ("{}".equals(cachedJson)) {
                log.info("题目 {} 命中防穿透空缓存", id);
                return null;
            }
            try {
                log.info("题目 {} 命中 Redis 缓存", id);
                return objectMapper.readValue(cachedJson, ProblemDTO.class);
            } catch (Exception e) {
                log.warn("Redis 缓存解析失败，退化查库", e);
            }
        }

        // 2. 缓存未命中，准备查库（防击穿：使用 Redisson 分布式锁）
        // 锁的粒度细化到具体的题目ID，避免全局阻塞
        String lockKey = "lock:problem:" + id;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁：最多等待3秒，上锁后10秒自动解锁（防止节点宕机死锁）
            boolean isLock = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (isLock) {
                // Double Check ：拿到锁后，再查一次 Redis。
                // 因为可能在你等锁的期间，前面的线程已经查完 MySQL 并把缓存写好了
                cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
                if (StringUtils.hasText(cachedJson)) {
                    if ("{}".equals(cachedJson)) return null;
                    return objectMapper.readValue(cachedJson, ProblemDTO.class);
                }

                log.info("题目 {} 获取分布式锁成功，穿透到 MySQL 查库", id);
                Problem problem = problemMapper.selectById(id);

                // 防穿透第二道防线：MySQL 里也没有？写入一个特殊的空对象 "{}"，TTL 设为 1 分钟
                if (problem == null) {
                    stringRedisTemplate.opsForValue().set(cacheKey, "{}", 1, TimeUnit.MINUTES);
                    return null;
                }

                // 组装 DTO
                ProblemDTO dto = problemConverter.toDto(problem);
                dto.setStatus(problem.getPublished() ? "PUBLISHED" : "DRAFT");
                List<ProblemTag> tags = problemTagMapper.selectList(
                    new LambdaQueryWrapper<ProblemTag>().eq(ProblemTag::getProblemId, id)
                );
                dto.setTags(tags.stream().map(ProblemTag::getTag).collect(Collectors.toList()));

                // 防雪崩：基础 TTL 1 小时 (3600秒) + 随机抖动 ±10% (-360 到 +360秒)
                long baseTtl = 3600L;
                long jitter = (long) (Math.random() * 720) - 360;
                long finalTtl = baseTtl + jitter;

                // 回写 Redis
                String jsonToCache = objectMapper.writeValueAsString(dto);
                stringRedisTemplate.opsForValue().set(cacheKey, jsonToCache, finalTtl, TimeUnit.SECONDS);

                return dto;
            } else {
                // 没拿到锁？说明有其他线程正在疯狂查库。休眠 100ms 后重新调用自己（递归重试）。
                Thread.sleep(100);
                return getProblemDetail(id);
            }
        } catch (Exception e) {
            log.error("获取题目详情异常", e);
            throw new RuntimeException("获取题目详情失败");
        } finally {
            // 释放锁（必须确保是当前线程自己加的锁才释放）
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Page<ProblemDTO> listProblems(int current, int size, String keyword, String tag, String difficulty) {
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Problem::getTitle, keyword);
        }
        if (StringUtils.hasText(difficulty)) {
            wrapper.eq(Problem::getDifficulty, difficulty);
        }
        if (StringUtils.hasText(tag)) {
            String safeTag = tag.replace("'", "''");
            wrapper.inSql(Problem::getId, "SELECT problem_id FROM problem_tag WHERE tag = '" + safeTag + "'");
        }
        wrapper.orderByDesc(Problem::getId);

        Page<Problem> page = problemMapper.selectPage(new Page<>(current, size), wrapper);

        List<ProblemDTO> dtoList = page.getRecords().stream().map(p -> {
            ProblemDTO dto = problemConverter.toDto(p);
            dto.setStatus(p.getPublished() ? "PUBLISHED" : "DRAFT");
            return dto;
        }).collect(Collectors.toList());

        if (!dtoList.isEmpty()) {
            List<Long> problemIds = dtoList.stream().map(ProblemDTO::getId).collect(Collectors.toList());
            List<ProblemTag> allTags = problemTagMapper.selectList(
                new LambdaQueryWrapper<ProblemTag>().in(ProblemTag::getProblemId, problemIds)
            );
            Map<Long, List<String>> tagMap = allTags.stream()
                .collect(Collectors.groupingBy(ProblemTag::getProblemId,
                    Collectors.mapping(ProblemTag::getTag, Collectors.toList())));

            dtoList.forEach(dto -> dto.setTags(tagMap.getOrDefault(dto.getId(), Collections.emptyList())));
        }

        Page<ProblemDTO> dtoPage = new Page<>(current, size, page.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadTestcase(Long problemId, int caseIndex, MultipartFile inputFile, MultipartFile outputFile, Integer score) {
        if (problemMapper.selectById(problemId) == null) {
            throw new RuntimeException("题目不存在");
        }

        String inputKey = String.format("problem-%d/%d.in", problemId, caseIndex);
        String outputKey = String.format("problem-%d/%d.ans", problemId, caseIndex);
        String bucket = minioConfig.getBuckets().getTestcases();

        minioService.uploadFile(bucket, inputKey, inputFile);
        minioService.uploadFile(bucket, outputKey, outputFile);

        TestcaseManifest manifest = testcaseManifestMapper.selectOne(
            new LambdaQueryWrapper<TestcaseManifest>()
                .eq(TestcaseManifest::getProblemId, problemId)
                .eq(TestcaseManifest::getCaseIndex, caseIndex)
        );

        if (manifest == null) {
            manifest = new TestcaseManifest();
            manifest.setProblemId(problemId);
            manifest.setCaseIndex(caseIndex);
            manifest.setInputObject(inputKey);
            manifest.setOutputObject(outputKey);
            manifest.setScore(score != null ? score : 10);
            testcaseManifestMapper.insert(manifest);
        } else {
            manifest.setInputObject(inputKey);
            manifest.setOutputObject(outputKey);
            if (score != null) manifest.setScore(score);
            testcaseManifestMapper.updateById(manifest);
        }
    }

    @Override
    public List<TestcaseManifestVO> getTestcaseManifest(Long problemId) {
        List<TestcaseManifest> list = testcaseManifestMapper.selectList(
            new LambdaQueryWrapper<TestcaseManifest>()
                .eq(TestcaseManifest::getProblemId, problemId)
                .orderByAsc(TestcaseManifest::getCaseIndex)
        );

        String bucket = minioConfig.getBuckets().getTestcases();
        return list.stream().map(m -> {
            TestcaseManifestVO vo = new TestcaseManifestVO();
            vo.setName(String.valueOf(m.getCaseIndex()));
            vo.setScore(m.getScore());
            vo.setInputUrl(minioService.getPresignedUrl(bucket, m.getInputObject(), 5));
            vo.setExpectedOutputUrl(minioService.getPresignedUrl(bucket, m.getOutputObject(), 5));
            return vo;
        }).collect(Collectors.toList());
    }
}
