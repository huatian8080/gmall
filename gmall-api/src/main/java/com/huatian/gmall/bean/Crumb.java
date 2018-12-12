package com.huatian.gmall.bean;

public class Crumb {

    //当前请求url中所包含的属性值
    private String valueName;
    //当前请求url减去面包屑的属性值的新请求
    private String urlParam;

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getUrlParam() {
        return urlParam;
    }

    public void setUrlParam(String urlParam) {
        this.urlParam = urlParam;
    }
}
