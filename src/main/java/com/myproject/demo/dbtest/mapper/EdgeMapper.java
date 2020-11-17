package com.myproject.demo.dbtest.mapper;

import com.myproject.demo.dbtest.vo.Edge;
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
public interface EdgeMapper {
    List<Edge> getEdgesByNodeIds(@Param("ids") List<String> ids);
}
