package com.myproject.demo.dbtest.controller;

import com.myproject.demo.dbtest.service.TreeService;
import com.myproject.demo.dbtest.service.UserService;
import com.myproject.demo.dbtest.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author zxy
 * @Description
 * @Date 2020/10/15
 */
@RequestMapping("/trees")
@RestController
public class TreeController {
    @Autowired
    TreeService treeService;

    @GetMapping("/calculate")
    public void initTreeNumber(){
        treeService.initTreeNumber();
    }
}
