package com.atguigu.gmall.pms.VO;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**********************************************************
 日期:2021-01-20
 作者:刘刚
 描 述: 这里面不仅有sku的信息和有10个营销字段的的信息
 ***********************************************************/
@Data
public class SkuVo extends SkuEntity {

    //sku的图片列表
    private List<String> images;

    //接收sku的销售属性
    private List<SkuAttrValueEntity> saleAttrs;

    //成长积分积分优惠信息
    private BigDecimal growBounds;
    //购物积分
    private BigDecimal buyBounds;
    /**这里还有一个坑，数据库里面的int的类型，而前端传给我们的是一个集合类型的
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送  */
    private List<Integer> work;



    //满多少满减优惠信息
    private BigDecimal fullPrice;
   //减多少
    private BigDecimal reducePrice;
    /**这里面又有一个坑，前端的信息用的不是addother，而是fullAddOther
     * 是否参与其他优惠
     */
    private Integer fullAddOther;


    //满几件 打折
    private Integer fullCount;
    //打几折
    private BigDecimal discount;
    //是否叠加其他优惠[0-不可叠加，1-可叠加],注意这里前端传的是ladderAddOther
    private Integer ladderAddOther;


}
