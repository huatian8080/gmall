package com.huatian.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huatian.gmall.bean.SpuInfo;
import com.huatian.gmall.manage.util.GmallUploadUtil;
import com.huatian.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SpuController {

    @Reference
    SpuService spuService;

    @RequestMapping("/delSpuInfo")
    @ResponseBody
    public String delSpu(String id){
        spuService.delSpu(id);
        return "SUCCESS";
    }

    @RequestMapping("/saveSpu")
    @ResponseBody
    public String saveSpu(SpuInfo spuInfo){
        spuService.saveSpu(spuInfo);
        return "SUCCESS";
    }


    @RequestMapping("/fileUpload")
    @ResponseBody
    public String uploadFile(@RequestParam("file") MultipartFile multipartFile){
        String imgUrl = GmallUploadUtil.uploadImage(multipartFile);
        return  imgUrl;
    }

    @RequestMapping("/spuListPage")
    public String toSpuListPage(){
        return "spuListPage";
    }

    @RequestMapping("/getSpuList")
    @ResponseBody
    public List<SpuInfo> getSpuList(String catalog3Id){
        List<SpuInfo> spuInfos = spuService.getSpuList(catalog3Id);
        return  spuInfos;
    }
}
