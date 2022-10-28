package com.atguigu.yygh.hosp.testmongo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.List;

@SpringBootTest
public class TestRepository {

    @Autowired
    private UserRepository userRepository;

    //1.添加和修改
    @Test
    public void createUser(){
        User user = new User();
        user.setAge(20);
        user.setName("李四");
        user.setEmail("222222222222@qq.com");
        userRepository.save(user);
    }

    //2.查询所有
    @Test
    public void findAll(){
        List<User> list = userRepository.findAll();
        System.out.println("list = " + list);
    }

    //3.id查询
    @Test
    public void findById(){
        User user = userRepository.findById("6356070ef16edc4b5d7163f8").get();
        System.out.println("user = " + user);
    }

    //4.条件查询
    //name = ? and age = ?
    @Test
    public void findUserList(){
        //创建实体类的对象，设置查询条件
        User user = new User();
        user.setName("李四");
        user.setAge(20);
        //使用条件对象封装
        Example<User> example = Example.of(user);
        List<User> list = userRepository.findAll(example);
        System.out.println("list = " + list);


    }


    //5.模糊查询
    @Test
    public void findUsersLikeName(){
        //创建模糊查询匹配器
        ExampleMatcher matcher = ExampleMatcher.matching()//构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)//改变默认字符串匹配方式，模糊查询
                .withIgnoreCase(true);//忽略大小写
        //封装条件
        User user = new User();
        user.setName("李");
        Example<User> example = Example.of(user,matcher);

        //调用方法
        List<User> list = userRepository.findAll(example);
        System.out.println("list = " + list);
    }

    //6.分页查询
    @Test
    public void findUserPage(){
        //1.先设置排序规则
        Sort sort = Sort.by(Sort.Direction.DESC, "age");


        //2.设置分页规则
        //of方法中目前有三个参数
        //第一个参数：当前页，默认第一页的值是0，
        //第二个参数：每页显示的记录数
        //第三个参数：排序规则的对象
        Pageable pageable = PageRequest.of(0, 2, sort);

        //创建模糊查询匹配器
        ExampleMatcher matcher = ExampleMatcher.matching()//构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)//改变默认字符串匹配方式，模糊查询
                .withIgnoreCase(true);//忽略大小写
        //封装条件
        User user = new User();
        user.setName("李");
        //3.设置条件
        Example<User> example = Example.of(user,matcher);

        //4.调用方法实现条件分页查询
        Page<User> pageModel = userRepository.findAll(example, pageable);

        List<User> list = pageModel.getContent();
        System.out.println("list = " + list);

    }

    //7.删除
    @Test
    public void delete(){
        userRepository.deleteById("6356341ee5e7d35177437545");
    }

    //测试SpringData中调用MongoDB中自定义的查询方法
    //1.条件查询
    @Test
    public void demo1(){
        List<User> list = userRepository.findByNameAndAge("test", 20);
        System.out.println("list = " + list);
    }
    //2.模糊查询
    @Test
    public void demo2(){
        List<User> list = userRepository.findByNameLike("test");
        System.out.println("list = " + list);
    }
}












