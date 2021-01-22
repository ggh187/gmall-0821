package com.atguigu.gmall.pms.feign;


import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;


/**********************************************************
 日期:2021-01-21
 作者:刘刚
 描 述: 
 ***********************************************************/

@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {


}
