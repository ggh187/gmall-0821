package com.atguigu.gmall.pms.VO;

import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

/**********************************************************
 日期:2021-01-20
 作者:刘刚
 描 述:此类的作用是用来封装squs的信息
 这里面有几个字段是spuInfo扩展对象
 * 包含：spuInfo基本信息、spuImages图片信息、baseAttrs基本属性信息、skus信息
 ***********************************************************/
@Data
public class SpuVo extends SpuEntity {

    // 图片信息
    private List<String> spuImages;

    // 基本属性信息，这里又需要新建一个SpuAttrValueVo类封装这个基本属性，因为没有合适的类用来封装
    private List<SpuAttrValueVo> baseAttrs;

    // sku信息,这里是sku的
    private List<SkuVo> skus;

}
