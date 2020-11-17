package com.myproject.demo.dbtest.controller;

import com.myproject.demo.dbtest.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/nodes")
@RestController
public class NodeController {
    @Autowired
    NodeService nodeService;

    @GetMapping
    public int getMaxNumber() {
        return nodeService.calculateNumber();
    }
}
