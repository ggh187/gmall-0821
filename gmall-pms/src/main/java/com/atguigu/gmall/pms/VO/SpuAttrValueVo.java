package com.atguigu.gmall.pms.VO;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**********************************************************
 日期:2021-01-20
 作者:刘刚
 描 述: 
 ***********************************************************/

public class SpuAttrValueVo extends SpuAttrValueEntity {

    //接受的时候使用attrvalue接收，避免后面的转化
    private List<String> valueSelected;
//重写set方法可以了解决,父类的attrvalue，  集合变为以逗号分隔的字符串，放到attrvalue里面
    public void setValueSelected(List<String> valueSelected) {
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }
}
