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

  /*  @Test
    public void testAliYunOSS() throws FileNotFoundException {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-qingdao.aliyuncs.com";
// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = "LTAI4G4a6BaJHCz73MtEFRkV";
        String accessKeySecret = "Za7Yo6T1W6YER4qUtXyFujA7SGdsh7";

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\28251\\Desktop\\timg11.jpg");
        ossClient.putObject("xufh-test-oss-1", "aaa.jpg", inputStream);
        System.out.println("success");
// 关闭OSSClient。
        ossClient.shutdown();
    }*/

}
