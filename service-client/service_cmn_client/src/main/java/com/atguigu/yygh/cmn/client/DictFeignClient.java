package com.atguigu.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 数据字典API接口
 */
@FeignClient("service-cmn")
public interface DictFeignClient {
    /**
     * 获取数据字典名称
     * @param parentDictCode
     * @param value
     * @return
     */
    @GetMapping(value = "/cmn/dict/getName/{parentDictCode}/{value}")
    String getName(@PathVariable("parentDictCode") String parentDictCode, @PathVariable("value") String value);
    /**
     * 获取数据字典名称
     * @param value
     * @return
     */
    @GetMapping(value = "/cmn/dict/getName/{value}")
    String getName(@PathVariable("value") String value);
}
