package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 不适用spring管理
 */
public class DictListenerNew extends AnalysisEventListener<DictEeVo> {

    //每隔2个存储数据库，实际中可以100条，然后清理list，方便内存回收
    private static final int BATCH_COUNT = 2;

    //创建集合用于缓存
    private List<Dict> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    //创建成员变量
    private DictService dictService;

    //创建DictListenerNew有参构造，进行传递
    public DictListenerNew(DictService dictService){
        this.dictService = dictService;
    }

    //从第二行开始读取，一行一行读取，把每行内容封装到对象里面
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {

        //dictEeVo对象，每行封装对象
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        //把封装好的对象添加到缓存list集合中
        cachedDataList.add(dict);
        //判断读取数据是否为2，如果是2，提交添加
        if (cachedDataList.size() >= BATCH_COUNT){
            //调用方法添加
            saveData();
            //存储完成清理list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }

    }
    //

    //在操作完成之后执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
    }

    //添加方法
    private void saveData(){
        dictService.saveBatch(cachedDataList);
    }
}
