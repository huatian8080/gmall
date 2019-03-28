package com.huatian.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huatian.gmall.bean.SkuInfo;
import com.huatian.gmall.bean.SkuLsInfo;
import com.huatian.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	JestClient jestClient;


	@Test
	public void contextLoads() throws IOException {
		String myDsl = getMyDsl();
		System.err.println(myDsl);
		Search build = new Search.Builder(myDsl).addIndex("gmall")
				.addType("SkuLsInfo").build();

		SearchResult execute = jestClient.execute(build);
		List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
		for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
			SkuLsInfo source = hit.source;
			System.err.println(source);
		}

	}

	public static String getMyDsl(){
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		//bool
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		//must
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","硅谷");
		boolQueryBuilder.must(matchQueryBuilder);
		//filter
		TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId","39");
		boolQueryBuilder.filter(termQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.size(10);
		searchSourceBuilder.from(0);
		return  searchSourceBuilder.toString();
	}

	@Reference
	SkuService skuService;

	@Test
	public void getMySkuInfoList() throws IOException {
		List<SkuInfo> skuInfoList = skuService.getMySkuInfoList("61");
		List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
		for (SkuInfo skuInfo : skuInfoList) {
			SkuLsInfo skuLsInfo = new SkuLsInfo();
			BeanUtils.copyProperties(skuInfo,skuLsInfo);
			skuLsInfoList.add(skuLsInfo);
		}
		for (SkuLsInfo skuLsInfo : skuLsInfoList) {
			Index build = new Index.Builder(skuLsInfo).index("gmall").type("SkuLsInfo").
					id(skuLsInfo.getId()).build();
			jestClient.execute(build);
		}
	}

}
