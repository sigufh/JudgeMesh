package com.judgemesh.api.client;

import com.judgemesh.api.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** user-service Feign 客户端。由 B 实现服务端。 */
@FeignClient(name = "user-service", path = "/api/user/internal")
public interface UserClient {

    @GetMapping("/{id}")
    UserDTO getById(@PathVariable("id") Long id);

    /** 余额扣分接口,Seata 分支事务示例。 */
    @PostMapping("/deduct")
    void deductBalance(@RequestParam("userId") Long userId,
                       @RequestParam("amount") Integer amount);
}
