package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.api.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**********************************************************
 日期:2021-01-21
 作者:刘刚
 描 述: 
 ***********************************************************/
public interface GmallSmsApi {
    //定义对外暴露的方法
    @PostMapping("sms/skubounds/save")
    public ResponseVo saveSales(@RequestBody SkuSaleVo saleVo);

}
