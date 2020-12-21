package com.coolfish.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.coolfish.common.to.es.SkuEsModel;
import com.coolfish.common.utils.R;
import com.coolfish.gmall.search.config.GmallElasticsearchConfig;
import com.coolfish.gmall.search.constant.EsConstant;
import com.coolfish.gmall.search.feign.ProductFeignService;
import com.coolfish.gmall.search.service.MallSearchService;
import com.coolfish.gmall.search.vo.AttrResponseVo;
import com.coolfish.gmall.search.vo.SearchParam;
import com.coolfish.gmall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author 28251
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    /**
     * 检索es
     *
     * @param searchParam 检索参数
     * @return
     */
    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;
        //1、动态构建查询需要的DSL语句
        //1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            //2、执行检索请求
            SearchResponse response = client.search(searchRequest, GmallElasticsearchConfig.COMMON_OPTIONS);
            //3、分析响应数据封装成我们需要的格式
            searchResult = buildSearchResult(searchParam, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * 构建结果数据
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchParam param, SearchResponse response) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        int totalPages = ((int) total % EsConstant.PRODUCT_PAGSIZE) == 0 ? ((int) total % EsConstant.PRODUCT_PAGSIZE) : ((int) total % EsConstant.PRODUCT_PAGSIZE + 1);

        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                esModels.add(skuEsModel);
            }
        }
        result.setProduct(esModels);

        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_add = response.getAggregations().get("brand_add");
        for (Terms.Bucket bucket : brand_add.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            String brand_img_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brand_img_agg);
        }
        result.setBrands(brandVos);

        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatelogId(Long.parseLong(keyAsString));
            ParsedStringTerms catelog_name_agg = bucket.getAggregations().get("catelog_name_agg");
            String catalog_name = catelog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatelogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
//        /////////////////////////////////////////////////////

        result.setPageNum(param.getPageNum());

        result.setTotal(total);

        result.setTotalPages(totalPages);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 0; i < totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
        if ((param.getAttrs() != null && param.getAttrs().size() > 0)) {
            List<SearchResult.NavVo> navs = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.info(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navs);
        }
        return result;
    }

    /**
     * 1、准备检索请求
     * 需要考虑模糊匹配、过滤（按属性、分类、品牌、价格区间、库存），排序，分页，高亮，聚合分析
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //构建了bool query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1must 模糊匹配
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        //1.2构建filter
        if (searchParam.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }

        //按照库存是否有
        boolQuery.filter(QueryBuilders.termsQuery("hasStock", searchParam.getHasStock() == 1));
        //按照价格区间
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = searchParam.getSkuPrice().split("_");
            if (s.length == 2) {
                //区间
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (searchParam.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (searchParam.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        //按照属性查询
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            for (String attrStr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一个必须都生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        sourceBuilder.query(boolQuery);
        /**
         * 排序、分页、高亮
         */
        //排序
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String sort = searchParam.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        //分页
        sourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGSIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGSIZE);
        //高亮
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style ='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }
        /**
         * 聚合分析
         */
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);

        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_add").field("brancImg").size(1));


        sourceBuilder.aggregation(brand_agg);
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("c").field("catelogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catelogName").size(1));

        sourceBuilder.aggregation(catalog_agg);

        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        /**
         * 需要考虑模糊匹配、过滤（按属性、分类、品牌、价格区间、库存）
         */
        return searchRequest;
    }
}
