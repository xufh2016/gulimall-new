package com.coolfish.gmall.search;

import com.alibaba.fastjson.JSON;
import com.coolfish.gmall.search.config.GmallElasticsearchConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallSearchApplicationTests {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Test
    public void contextLoads() {
        System.out.println(restHighLevelClient);
    }
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("product");
        indexRequest.id("1");
        User user = new User("lisi","mail","18");
        String userStr = JSON.toJSONString(user);
        indexRequest.source(userStr, XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(indexRequest, GmallElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(index.getResult().toString());

//        indexRequest.source("username","zhangsan","age","18");
    }

    @Test
    public void searchData() throws IOException{
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //指定DSL，检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1.1构造检索条件

        sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
//        sourceBuilder.from();
        searchRequest.source(sourceBuilder);
        //执行检索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GmallElasticsearchConfig.COMMON_OPTIONS);
        //分析结果,结果都在searchresponse中
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {

        }
        System.out.println(searchRequest.toString());
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class User{
        private String username;
        private String gender;
        private String age;
    }

}
