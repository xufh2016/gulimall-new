package com.coolfish.gmall.search.service;

import com.coolfish.gmall.search.vo.SearchParam;
import com.coolfish.gmall.search.vo.SearchResult;

public interface MallSearchService {
    /**
     *
     * @param searchParam 检索参数
     * @return 检索结果
     */
    SearchResult search(SearchParam searchParam);
}
