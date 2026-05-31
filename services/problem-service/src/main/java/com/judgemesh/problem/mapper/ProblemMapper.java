package com.judgemesh.problem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.judgemesh.problem.entity.Problem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {
}
