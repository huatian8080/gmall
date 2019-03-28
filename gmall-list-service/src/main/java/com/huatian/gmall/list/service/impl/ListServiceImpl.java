package com.huatian.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huatian.gmall.bean.SkuLsInfo;
import com.huatian.gmall.bean.SkuLsParam;
import com.huatian.gmall.bean.SkuLsResult;
import com.huatian.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;



    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        String myDsl = getMyDsl(skuLsParam);
        System.err.println(myDsl);
        Search build = new Search.Builder(myDsl).addIndex("gmall")
                .addType("SkuLsInfo").build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (execute != null){
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo source = hit.source;
                if (hit.highlight != null){
                    List<String> list = hit.highlight.get("skuName");
                    String s = list.get(0);
                    source.setSkuName(s);
                }
                skuLsInfoList.add(source);
            }
        }
        return skuLsInfoList;
    }

    @Override
    public void saveSkuLsInfoToList(SkuLsInfo skuLsInfo) {
        try {
            Index build = new Index.Builder(skuLsInfo).index("gmall").type("SkuLsInfo").
                    id(skuLsInfo.getId()).build();
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMyDsl(SkuLsParam skuLsParam){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //must
        String keyword = skuLsParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder;'>");
            highlightBuilder.field("skuName");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlight(highlightBuilder);
        }
        //filter
        String[] valueId = skuLsParam.getValueId();
        if(valueId != null && valueId.length > 0){
            for (String s : valueId) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",s);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        String catalog3Id = skuLsParam.getCatalog3Id();
        if (StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        searchSourceBuilder.size(20);
        searchSourceBuilder.from(0);
        return  searchSourceBuilder.toString();
    }
}
