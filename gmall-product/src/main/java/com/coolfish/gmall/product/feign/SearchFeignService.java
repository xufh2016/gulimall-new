package com.coolfish.gmall.product.feign;

import com.coolfish.common.to.es.SkuEsModel;
import com.coolfish.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gmall-search")
public interface SearchFeignService {
    /**
     * 商品上架远程服务接口
     * @param skuEsModels
     * @return
     */
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
