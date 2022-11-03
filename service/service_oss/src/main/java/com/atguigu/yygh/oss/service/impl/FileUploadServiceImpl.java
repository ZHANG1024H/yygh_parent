package com.atguigu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.atguigu.yygh.oss.service.FileUploadService;
import com.atguigu.yygh.oss.utils.ConstantPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    //文件上传
    @Override
    public String upload(MultipartFile file) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantPropertiesUtil.END_POINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantPropertiesUtil.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtil.ACCESS_KEY_SECRET;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantPropertiesUtil.BUCKET_NAME;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            //文件名称
            String objectName = file.getOriginalFilename();
            //使用UUID工具类，将-此符号转换为空
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            //把名称拼接到文件名称后面
            objectName = uuid+objectName;

            //得到当前时间
            String date = new DateTime().toString("yyyy/MM/dd");
            objectName = date+"/"+objectName;

            //输入流
            InputStream inputStream = file.getInputStream();

            //调用putObject实现文件上传
            // 创建PutObject请求。
            /**
             * bucketName:bucket名称
             *objectName：在oss的bucket上传文件路径+名称   //2022/11/01/01.jpg
             * inputStream：上传文件流
             */
            ossClient.putObject(bucketName, objectName, inputStream);
            //返回上传之后，文件在阿里云路径
            // https://yyhgtest-0620.oss-cn-beijing.aliyuncs.com/01.jpg
            String url = "https://"+bucketName+"."+endpoint+"/"+objectName;
            return url;
        } catch (Exception oe) {

        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
