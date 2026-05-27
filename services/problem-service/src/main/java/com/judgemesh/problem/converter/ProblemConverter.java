//把数据库实体 Problem 自动转换成我们 judgemesh-api 里定义的共享 ProblemDTO，省去手动写 get/set
package com.judgemesh.problem.converter;

import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.problem.entity.Problem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProblemConverter {
    ProblemDTO toDto(Problem problem);
}
