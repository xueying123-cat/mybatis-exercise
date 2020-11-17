package com.myproject.demo.dbtest.service;

import com.myproject.demo.dbtest.mapper.TreeMapper;
import com.myproject.demo.dbtest.vo.Tree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TreeService {
    @Autowired
    TreeMapper treeMapper;

    public List<Tree> getAll() {
        return treeMapper.getAll();
    }
    public void initTreeNumber() {
        List<Tree> trees = getAll();
        List<Tree> employees = trees.stream()
                .filter(tree -> Objects.equals(tree.getType(), 3))
                .collect(Collectors.toList());
        calculate(employees, trees);
        //更新
        treeMapper.updateBatch(trees);
    }

    public void calculate(List<Tree> employees, List<Tree> trees) {
        //分组
        Map<String, List<Tree>> employeesByParentId = employees.stream()
                .collect(Collectors.groupingBy(Tree::getParentId));

        Set<String> parentIds = employeesByParentId.keySet();
        if(parentIds.isEmpty()){
            return;
        }
        List<Tree> parentTrees = new ArrayList<>();
        for (String parentId : parentIds) {
            List<Tree> treesInParentId = employeesByParentId.get(parentId);
            int sum = treesInParentId.stream().mapToInt(Tree::getNumber).sum();

            for (Tree tree : trees) {
                if (Objects.equals(parentId, tree.getId())) {
                    tree.setNumber(sum);
                    parentTrees.add(tree);
                }
            }
        }
        calculate(parentTrees, trees);
    }
}
