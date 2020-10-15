package com.myproject.demo.dbtest.service;

import com.myproject.demo.dbtest.vo.User;
import com.myproject.demo.dbtest.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author zxy
 * @Description
 * @Date 2020/10/14
 */
@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    public int updateStepBoundedGrade(String gradeId, BigDecimal hourlyPay) {
        return userMapper.updateStepBoundedGrade(gradeId, hourlyPay);
    }

    public List<User> getAll(){
        return userMapper.getAll();
    }
}
