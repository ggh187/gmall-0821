package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    //两表关联查询 attr表和arrtgroup表
    @Override
    public List<AttrGroupEntity> queryGroupsBycatId(Long catId) {
        // 查询组--->这里正常运行但是有瑕疵，我们需要的基本数据的信息，而不是全部的信息,type=1I
       List<AttrGroupEntity> groupEntities=this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", catId));
        if (CollectionUtils.isEmpty(groupEntities)) {
            return  null;
        }

        //根据组查规格,遍历集合
        groupEntities.forEach(group->{
            List<AttrEntity> attrEntities=this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id",group.getId()).eq("type",1));

            group.setAttrEntities(attrEntities);
        });
        return groupEntities;


    }

}