package com.atguigu.yygh.hosp.testmongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User,String> {
    //根据姓名和年龄查询
    public List<User> findByNameAndAge(String name,Integer age);

    //模糊查询
    public List<User> findByNameLike(String name);
}
