package com.atguigu.yygh.hosp.testmongo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.regex.Pattern;

@SpringBootTest
public class TestMongoTemplate {
    //注入MongoTemplate
    @Autowired
    private MongoTemplate mongoTemplate;

    //2.查询所有
    @Test
    public void findAll(){
        List<User> list = mongoTemplate.findAll(User.class);
        System.out.println(list);
    }

    //3.根据id查询
    @Test
    public void findId(){
        User user = mongoTemplate.findById("6356070ef16edc4b5d7163f8", User.class);
        System.out.println(user);
    }

    //4.条件查询
    //根据名称和年龄查询：name = ? and age = ?
    @Test
    public void findQuery(){
        //封装条件
        Query query = new Query(Criteria.where("name").is("test").and("age").is(20));
        List<User> users = mongoTemplate.find(query, User.class);
        System.out.println("users = " + users);
    }

    //5.模糊查询
    @Test
    public void findQueryLike(){
        //构建条件
        //封装正则表达式
        //String regex ="^.*test.*$";
        String name = "est";
        String regex = String.format("%s%s%s","^.*",name,".*$");
        System.out.println("regex = " + regex);
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        Query query = new Query(Criteria.where("name").regex(pattern));

        List<User> list = mongoTemplate.find(query, User.class);

    }

    //6.分页查询
    @Test
    public void findUsersPage(){
        //初始值
        String name = "est";
        int page = 1;
        int pageSize = 2;

        //封装模糊查询
        String regex = String.format("%s%s%s","^.*",name,".*$");
        System.out.println("regex = " + regex);
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        Query query = new Query();
        query.addCriteria(Criteria.where("name").regex(pattern));

        //查询记录数
        long count = mongoTemplate.count(query, User.class);
        //分页查询
        int begin = (page-1)*pageSize;
        List<User> list = mongoTemplate.find(query.skip(begin).limit(pageSize), User.class);
        System.out.println("count = " + count);
        System.out.println("list = " + list);
    }

    //7.删除

    @Test
    //1.添加
    public void add(){
        User user = new User();
        user.setAge(20);
        user.setName("test");
        user.setEmail("111111111@qq.com");
        User userAdd = mongoTemplate.insert(user);
        //添加完成之后，返回对象里面包含主键id值
        System.out.println(userAdd);
    }
}
