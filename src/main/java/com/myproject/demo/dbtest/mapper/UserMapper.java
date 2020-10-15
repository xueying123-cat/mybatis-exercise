package com.myproject.demo.dbtest.mapper;

import com.myproject.demo.dbtest.vo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author zxy
 * @Description
 * @Date 2020/10/14
 */
@Mapper
@Component
public interface UserMapper {
    List<User> getAll();
}
