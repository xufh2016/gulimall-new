package com.coolfish.gmall.product;

import com.coolfish.gmall.product.entity.BrandEntity;
import com.coolfish.gmall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("HUAWEI");
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功");

    }

}
