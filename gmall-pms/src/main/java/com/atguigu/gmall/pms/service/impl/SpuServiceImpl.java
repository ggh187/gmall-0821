package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.VO.SkuVo;
import com.atguigu.gmall.pms.VO.SpuAttrValueVo;
import com.atguigu.gmall.pms.VO.SpuVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;

import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.sms.api.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService baseAttrMapperservice;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SkuAttrValueService saleAttrValueService;

    @Autowired
    private GmallSmsClient gmallSmsClient;


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySquByCidAndPage(PageParamVo pageParamVo, Long cid) {
//sql语句为  select  * from pms_spu where category_id=225 and (id =7 or name like '%7')
        //使用querywapper实现category_id=225 and (id =7 or name like '%7')
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper();

        //分类查询id的条件
        if (cid != 0) {
            wrapper.eq("category_id", cid);
        }
        //关键字查询
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
        }
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(), wrapper

        );

        return new PageResultVo(page);
    }

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        //拿到前端传过来的数据这时候需要考虑把里面的数据保存到那几张表里面。
        // pms_spu
        //pms_spu_attr_value   spu的基本属性
        //pms_spu_desc  spu的描述信息
        //pms_sku
        //pms_sku_images  sku图片
        //pms_sku_attr_value  sku的销售属性
        //sms_sku_bounds    积分表
        //sms_sku_full_reduction  满减表
        //sms_sku_ladder  满几件打几折
        //先保存spu的相关信息  pms_spu    pu_attr_value   spu_desc

        //1.1 保存pms_spu
        Long id = saveSpu(spuVo);

        //1.2 spu_desc
        saveSpuDesc(spuVo, id);

        //1.3 spu_attr_value
        saveBaseattr(spuVo, id);

        //再保存sku息
        //2.1  sku
        savezSkuInfo(spuVo, id);
        //int i=1/0;


        //3.1 sms_sku_bounds    积分表

        //3.2 sms_sku_full_reduction  满减表

        //3.3 sms_sku_ladder  满几件打几折


    }

    @Transactional
    public  void savezSkuInfo(SpuVo spuVo, Long id) {
        List<SkuVo> skuVos = spuVo.getSkus();
        if (CollectionUtils.isEmpty(skuVos)){

        }
        skuVos.forEach((sku)->{
            //保存sku
            sku.setSpuId(id);
            sku.setCategoryId(spuVo.getCategoryId());
            sku.setBrandId(spuVo.getBrandId());
             //设置默认的图片
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                sku.setDefaultImage(StringUtils.isNotBlank(sku.getDefaultImage()) ? sku.getDefaultImage() : images.get(0));
            }
            this.skuMapper.insert(sku);
            //2.2 sku_images  sku图片
            if (!CollectionUtils.isEmpty(images)) {
                this.imagesService.saveBatch(images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setSkuId(id);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(sku.getDefaultImage(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList()));
            }
            //2.3 ku_attr_value  sku的销售属性
            List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(SkuAttrValueEntity->{
                    SkuAttrValueEntity.setSkuId(id);
                });
                this.saleAttrValueService.saveBatch(saleAttrs);
            }
            //保存营销信息积分满减打折  使用fegin
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(sku, skuSaleVo);//这里还是会少一个字段
            skuSaleVo.setSkuId(id);
            this.gmallSmsClient.saveSales(skuSaleVo);
        });
    }

    private void saveBaseattr(SpuVo spuVo, Long id) {
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {

            //集体插入批量保存
            this.baseAttrMapperservice.saveBatch(
                    //不需要全部遍历 使用Stream流遍历--->vo集合变为entity集合 下面的stream的结果为List<SpuAttrValueVo> spuAttrValueEntitys =
                    baseAttrs.stream().map(spuAttrValueVo -> {//这个spuattrvaluevo里面就是一个spuatrvalue的集合
                        SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                        //使用copy工具类把spuvo里面的对应的属性拷贝到sputtrvalue里面
                        BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                        spuAttrValueEntity.setSpuId(id);
                        return spuAttrValueEntity;
                    }).collect(Collectors.toList()));
        }
    }

    private void saveSpuDesc(SpuVo spuVo, Long id) {
        List<String> spuImages = spuVo.getSpuImages();//这是以dzh分隔的集合
        if (!CollectionUtils.isEmpty(spuImages)) {
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(id);//这里运行的时候报错原因是没有默认的spu_id
            //因为这个表计较特殊,spu_id没有之间自增策略,而且我们的application文件里面设置的主键自增,所以我们即使手动设置了,也是没有用的
            //即:全局yml文件设置主键自增长,而数据库里面没有设置自增
            spuDescEntity.setDecript(StringUtils.join(spuImages, ","));
            this.spuDescMapper.insert(spuDescEntity);
        }
    }

    private Long saveSpu(SpuVo spuVo) {
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        this.save(spuVo);
        return spuVo.getId();
    }

//    public static void main(String[] args) {
//        List<User> users = Arrays.asList(
//        new User(1l, "11", 20),
//        new User(2l, "122", 21),
//        new User(3l, "1333", 22),
//        new User(4l, "14444", 23),
//        new User(5l, "155555", 24)
//        );
//        //过滤出年龄大于22
//        users.stream().filter(user -> user.getAge() > 22).collect(Collectors.toList()).forEach(System.out::println);
//        //这里是一个stream在转化为集合操作,操作的结果收集collect 为tolist/tomap/toset
//        System.out.println("===========================================");
//
//
//        //把user转换为age的流,再转换为集合
//        users.stream().map(User::getAge).collect(Collectors.toList()).forEach(System.out::print);
//
//        //把uer集合转换为person集合  本质就是把集合里面的元素变为新集合里面的元素
//        users.stream().map(user -> {
//            Person person = new Person();
//            person.setAge(user.getAge());
//            person.setId(user.getId());
//            person.setPersonname(user.getName());
//            return  person;
//        }).collect(Collectors.toList()).forEach(System.out::println);
//
//        //求所有人的年龄之和
//        Integer integer = users.stream().map(User::getAge).reduce((a, b) -> (a + b)).get();
//        System.out.println(integer);
//
//    }










}

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//class  User{
//
//    private Long id;
//    private String name;
//    private Integer age;
//}
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//class  Person{
//private Long id;
//private String personname;
//private Integer age;
//
//
//}
