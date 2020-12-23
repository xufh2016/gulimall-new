package com.coolfish.gmall.product.web;


import com.coolfish.gmall.product.service.SkuInfoService;
import com.coolfish.gmall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {

        SkuItemVo skuItemVo = skuInfoService.item(skuId);

        model.addAttribute("item", skuItemVo);
        System.out.println("jumpman------------------------");
        return "item";
    }
}
