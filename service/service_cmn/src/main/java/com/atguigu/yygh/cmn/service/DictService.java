package com.atguigu.yygh.cmn.service;

import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-10-21
 */
public interface DictService extends IService<Dict> {
    List<Dict> getDataById(Long id);

    //列表接口
    //懒加载效果，每次显示一层数据，根据id查询一层数据
    List<Dict> selectList(Object o);

    //导出
    void exportDictData(HttpServletResponse response);

    //导入，上传
    void importDataDict(MultipartFile file);

    //根据value值返回对应名称
    String getNameByValue(String dictCode,String value);

    //查询所有学历和医院等级等
    List<Dict> getByDictCode(String dictCode);
}
