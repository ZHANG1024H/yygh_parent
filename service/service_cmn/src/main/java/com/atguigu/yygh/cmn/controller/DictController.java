package com.atguigu.yygh.cmn.controller;


import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListenerNew;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 组织架构表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-10-21
 */
@RestController
@RequestMapping("/cmn/dict/")
/*@CrossOrigin //跨域*/
public class DictController {

    @Autowired
    private DictService dictService;

    //查询所有省（学历、医院等级等）
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public R findByDictCode(
            @ApiParam(name = "dictCode", value = "节点编码", required = true)
            @PathVariable String dictCode) {
        List<Dict> list = dictService.getByDictCode(dictCode);
        return R.ok().data("list",list);
    }

    //导入  上传
    @ApiOperation("导入")
    @PostMapping("importDataNew")
    public R importDictDaraNew(MultipartFile file){
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListenerNew(dictService)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.ok();
    }

    //导入  上传,单例
    @ApiOperation("导入")
    @PostMapping("importData")
    public R importDictDara(MultipartFile file){
        //获取上传文件
        dictService.importDataDict(file);
        return R.ok();
    }

    //导出
    @ApiOperation(value="导出")
    @GetMapping(value = "/exportData")
    public void exportData(HttpServletResponse response){
        dictService.exportDictData(response);
    }

    //列表接口
    //懒加载效果，每次显示一层数据，根据id查询一层数据
    //语句：SELECT * FROM dict WHERE parent_id=?
    @ApiOperation("数据字典列表")
    @GetMapping("findDataById/{id}")
    public R findDataById(@PathVariable Long id){
        //调用service的方法
        List<Dict> list = dictService.getDataById(id);
        return R.ok().data("list",list);
    }

    //value不唯一
    @ApiOperation(value = "value不唯一获取数据字典名称")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(
            @ApiParam(name = "parentDictCode", value = "上级编码", required = true)
            @PathVariable("parentDictCode") String parentDictCode,
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByValue(parentDictCode, value);
    }

    //value唯一
    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByValue("", value);
    }

}

