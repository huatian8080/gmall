package com.huatian.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huatian.gmall.bean.*;
import com.huatian.gmall.manage.mapper.SkuAttrValueMapper;
import com.huatian.gmall.manage.mapper.SkuImageMapper;
import com.huatian.gmall.manage.mapper.SkuInfoMapper;
import com.huatian.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.huatian.gmall.service.ListService;
import com.huatian.gmall.service.SkuService;
import com.huatian.gmall.utils.RedisUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    RedisUtil redisUtil;
    @Reference
    ListService listService;



    @Override
    public List<SkuInfo> getSkuList(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        return skuInfoMapper.select(skuInfo);
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();
        //保存平台属性值
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }
        //保存销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }
        //保存图片信息值
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insertSelective(skuImage);
        }
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuLsInfoToList(skuLsInfo);
    }

    @Override
    public SkuInfo getSkuById(String skuId) {
        SkuInfo skuInfo = null;
        //查询缓存
        Jedis jedis = redisUtil.getJedis();
        String cacheJson = jedis.get("sku:" + skuId + ":info");
        if (StringUtils.isBlank(cacheJson)){
            //分布式缓存锁服务器取锁,实际开发中锁服务和业务数据服务不是同一个redis
            String ok = jedis.set("sku:" + skuId + ":lock", "1","nx","px",10000);
            if (StringUtils.isNotBlank(ok)){
                //缓存中没有则查询数据库
                skuInfo = getSkuByIdFromDB(skuId);
                if(skuInfo != null){
                    //将查询结果同步到缓存中
                    jedis.set("sku:" + skuId + ":info",JSON.toJSONString(skuInfo));
                    jedis.del("sku:" + skuId + ":lock");
                }
            }else {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);
            }
        }else{
            skuInfo = JSON.parseObject(cacheJson, SkuInfo.class);
        }
        return  skuInfo;
    }

    public SkuInfo getSkuByIdFromDB(String skuId){
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        return skuInfo;
    }

    @Override
    public List<SkuInfo> getSkuAttrValueListBySpuId(String spuId) {
        return skuSaleAttrValueMapper.selectSkuAttrValueListBySpuId(spuId);
    }

    @Override
    public List<SkuInfo> getMySkuInfoList(String catalog3Id) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id(catalog3Id);
        List<SkuInfo> skuInfoList = skuInfoMapper.select(skuInfo);
        for (SkuInfo info : skuInfoList) {
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(info.getId());
            List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
            info.setSkuAttrValueList(skuAttrValueList);
        }
        return  skuInfoList;
    }
}
