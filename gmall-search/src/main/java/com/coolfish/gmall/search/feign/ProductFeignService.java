package com.coolfish.gmall.search.feign;

import com.coolfish.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("gmall-product")
public interface ProductFeignService {
    @RequestMapping(value = "product/attr/info/{attrId}", method = RequestMethod.GET)
    R info(@PathVariable("attrId") Long attrId);
}
