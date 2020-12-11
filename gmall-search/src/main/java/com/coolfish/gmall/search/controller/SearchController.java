package com.coolfish.gmall.search.controller;


import com.coolfish.gmall.search.service.MallSearchService;
import com.coolfish.gmall.search.vo.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam){
        Object result = mallSearchService.search(searchParam);



        return "list";
    }
}
