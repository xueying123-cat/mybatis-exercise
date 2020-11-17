package com.myproject.demo.dbtest.vo;

import lombok.Data;

@Data
public class Tree {
    private String id;
    private String name;
    private String parentId;
    private int number;
    private int type;
}
