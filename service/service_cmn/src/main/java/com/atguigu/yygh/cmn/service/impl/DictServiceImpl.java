package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.catalina.connector.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-10-21
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Autowired
    private DictListener dictListener;

    //列表接口
    //懒加载效果，每次显示一层数据，根据id查询一层数据
    @Cacheable(value = "dict")
    @Override
    public List<Dict> getDataById(Long id) {
       //封装条件
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        //调用mapper方法查询
        List<Dict> dictList = baseMapper.selectList(wrapper);
        /*
        查询数据库返回数据往往不能满足当前前端需求，一般把查询出来数据在做进一步封装
         */
        //Dict有属性hasChildren
        //如果当前数据有下一层数据 hasChildren = true, 否则 hasChildren=false
        //遍历dictlist，的到每一个dict对象，判断每个dict对象是否有下一层数据有hasChildren=true；

        for (Dict dict : dictList) {
            //判断dict是否有下一层数据
            //查询parentid=当前dict的id
            //获取当前dict的id值
            Long dictId = dict.getId();
            boolean flag = isChildData(dictId);
            //返回值，设置hasChildren
            dict.setHasChildren(flag);
        }
        return dictList;
    }

    @Override
    public List<Dict> selectList(Object o) {
        return null;
    }

    @Override
    public void exportDictData(HttpServletResponse response) {
        try {
            //1、设置下载信息 头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
            //2、查询数据查询所有数据字典数据
            List<Dict> dictList = baseMapper.selectList(null);
            // List<Dict> --> List<DictEeVo>
            List<DictEeVo> dictEeVoList = new ArrayList<>();
            for (Dict dict : dictList) {
                /* Long id = dict.getId();
                String name = dict.getName();
                Long parentId = dict.getParentId();

                DictEeVo dictEeVo = new DictEeVo();
                dictEeVo.setId(id);
                dictEeVo.setName(name);
                dictEeVo.setParentId(parentId);*/

                //上面代码的优化版
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictEeVo);
                dictEeVoList.add(dictEeVo);
            }

            //3、使用EasyExcel写操作
            //注意数据集合类型不要传错
            EasyExcel.write(response.getOutputStream(), DictEeVo.class)
                    .sheet("数据字典").doWrite(dictEeVoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @CacheEvict(value = "dict",allEntries = true)
    @Override
    public void importDataDict(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,dictListener).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //根据value值返回对应名称
    @Override
    public String getNameByValue(String dictCode, String value) {
        //判断 value是否唯一
        if (StringUtils.isEmpty(dictCode)){//value唯一
         //value唯一直接查询
            //另一种写法
            /*LambdaQueryWrapper<Dict> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(Dict::getValue,value);*/
         QueryWrapper<Dict> wrapper = new QueryWrapper<>();
         wrapper.eq("value",value);
            Dict dict = baseMapper.selectOne(wrapper);
            if (dict != null){
                return dict.getName();
            }
        }else {//value不唯一
            //根据dictcode查询上层id
            Dict dictParent = this.getDictByDictCode(dictCode);
            if (dictParent != null){
                //获取上层id值
                Long pid = dictParent.getId();
                QueryWrapper<Dict> wrapper = new QueryWrapper<>();
                wrapper.eq("parent_id",pid);
                wrapper.eq("value",value);
                Dict dict = baseMapper.selectOne(wrapper);
                if (dict != null){
                    return dict.getName();
                }
            }

        }
        return null;
    }

    //查询所有学历，医院等
    @Override
    public List<Dict> getByDictCode(String dictCode) {
        Dict dict = this.getDictByDictCode(dictCode);
        if (dict != null){
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("parent_id",dict.getId());
            List<Dict> dictList = baseMapper.selectList(wrapper);
            return dictList;
        }
        return null;
    }

    //根据dictcode查询id
    private Dict getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(wrapper);
        return dict;
    }

    //判断是否有下一层数据
    private boolean isChildData(Long dictId) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",dictId);
        Integer count = baseMapper.selectCount(wrapper);
        return count>0;
    }


}















