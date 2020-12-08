package com.coolfish.gmall.product.feign;

import com.coolfish.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gmall-ware")
public interface WareFeignServcie {
    /**
     * 库存服务的远程查询sku是否有库存的接口
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
}
