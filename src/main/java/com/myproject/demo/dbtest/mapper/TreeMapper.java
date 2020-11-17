package com.myproject.demo.dbtest.mapper;

import com.myproject.demo.dbtest.vo.Tree;
import com.myproject.demo.dbtest.vo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author zxy
 * @Description
 * @Date 2020/10/14
 */
@Mapper
@Component
public interface TreeMapper {
    List<Tree> getAll();

    int updateBatch(@Param("list") List<Tree> trees);
}
