package com.myproject.demo.dbtest.service;

import com.myproject.demo.dbtest.mapper.EdgeMapper;
import com.myproject.demo.dbtest.mapper.NodeMapper;
import com.myproject.demo.dbtest.util.SystemException;
import com.myproject.demo.dbtest.vo.Edge;
import com.myproject.demo.dbtest.vo.Node;
import com.myproject.demo.dbtest.vo.NumberVO;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NodeService {
    @Autowired
    NodeMapper nodeMapper;

    @Autowired
    EdgeMapper edgeMapper;

    public int calculateNumber() {
        List<Node> nodes = nodeMapper.getAll();
        List<String> nodeIds = nodes.stream().map(Node::getId).collect(Collectors.toList());
        List<Edge> edges = edgeMapper.getEdgesByNodeIds(nodeIds);

        //定义接收最大值的实体类
        NumberVO numberVO = new NumberVO(0);
        //查找首节点
        List<String> firstNodeIds = getFirstNodeIds(nodes, edges);
        parallel(firstNodeIds, nodes, edges, numberVO);
        return numberVO.getMaxNum();
    }

    private void parallel(List<String> nodeIds,
                          List<Node> nodes,
                          List<Edge> edges,
                          NumberVO numberVO) {
        List<NumberVO> maxNums = new ArrayList<>();
        List<NumberVO> parallelEnds = new ArrayList<>();
        for (String nodeId : nodeIds) {
            NumberVO maxNum = new NumberVO(0);
            //每个分支都当做串联分支处理，调用串联方法
            circle(nodeId, nodes, edges, maxNum);
            if (Strings.isNotEmpty(maxNum.getParallelEndId())) {
                //当前并联分支到了某个结束节点时,找到当前并联分支的最终节点，处理并联结果得到最大值
                parallelEnds.add(maxNum);
            } else {
                //串联分支完成遍历
                maxNums.add(maxNum);
            }
        }

        //找到当前并联分支的最终节点，处理并联结果得到最大值
        if (!parallelEnds.isEmpty()) {
            dealParallelChilds(nodes, edges, numberVO, maxNums, parallelEnds);
        } else {
            numberVO.setMaxNum(maxNums.stream().mapToInt(NumberVO::getMaxNum).sum());
        }
    }

    private void dealParallelChilds(List<Node> nodes, List<Edge> edges, NumberVO numberVO, List<NumberVO> maxNums, List<NumberVO> parallelEnds) {
        //分组
        Map<String, List<NumberVO>> parallelEndGroupByEndId = parallelEnds.stream()
                .collect(Collectors.groupingBy(NumberVO::getParallelEndId));
        Set<String> endIds = parallelEndGroupByEndId.keySet();
        //获取多个并联终点的最后一个节点
        String parallelEndIds = parallelEnds.get(0).getParallelEndIds();
        List<NumberVO> indexEndNumberVOs = getSortEndNumberVOs(endIds, parallelEndIds);
        String endIdFinal = indexEndNumberVOs.get(indexEndNumberVOs.size() - 1).getParallelEndId();

        NumberVO sumParam = new NumberVO(0);
        for (NumberVO indexEndNumberVO : indexEndNumberVOs) {
            String endId = indexEndNumberVO.getParallelEndId();

            //获取当前结束节点对应的并联节点的总和
            List<NumberVO> sourceNodeParams = parallelEndGroupByEndId.get(endId);
            int sum = sourceNodeParams.stream().mapToInt(NumberVO::getMaxNum).sum();

            if (!Objects.equals(endId, endIdFinal)) {
                //要和上个节点做比较，得出最大值
                NumberVO maxParam = new NumberVO(sum);
                //比较当前求和结果和最终节点大小
                Node endNode = getNode(nodes, endId);
                maxParam.setMaxNum(Math.max(sum, endNode.getNumber()));

                //处理下个节点
                List<String> nextIds = getTargetIdsByEdges(edges, endId);
                if (nextIds.size() == 1) {
                    //串联求取最大值，如果这里结束了，则代表已经走完，得到最大值
                    circle(nextIds.get(0), nodes, edges, maxParam);
                    if (Objects.equals(maxParam.getParallelEndId(), endIdFinal)) {
                        sumParam.setMaxNum(maxParam.getMaxNum() + sumParam.getMaxNum());
                        continue;
                    }
                    if (null != maxParam.getParallelTargetIds()
                            && !maxParam.getParallelTargetIds().isEmpty()) {
                        //新的并联关系
                        NumberVO pparam = new NumberVO(0);
                        parallel(maxParam.getParallelTargetIds(), nodes, edges, pparam);
                    }
                } else if (nextIds.size() > 1) {
                    //获取下次并联结果集
                    NumberVO sumParam1 = new NumberVO(0);
                    parallel(nextIds, nodes, edges, sumParam1);
                }
            } else {
                sumParam.setMaxNum(sum + sumParam.getMaxNum());
            }
        }
        maxNums.add(sumParam);

        Node endFinalNode = getNode(nodes, endIdFinal);
        int sumAll = maxNums.stream().mapToInt(NumberVO::getMaxNum).sum();
        numberVO.setMaxNum(Math.max(sumAll, endFinalNode.getNumber()));
        List<String> nextIds = getTargetIdsByEdges(edges, endIdFinal);
        if (nextIds.size() == 1) {
            numberVO.setParallelEndIdFinal(nextIds.get(0));
        } else if (nextIds.size() > 1) {
            //获取下次并联结果集
            NumberVO sumParam1 = new NumberVO(0);
            parallel(nextIds, nodes, edges, sumParam1);
        }
    }

    private List<NumberVO> getSortEndNumberVOs(Set<String> endIds,
                                     String parallelEndIds) {
        List<NumberVO> indexEndNumberVOs = new ArrayList<>();
        for (String endId : endIds) {
            NumberVO indexParam = new NumberVO();
            //size=1时代表当前的endId集合中包含最终节点
            int index = parallelEndIds.indexOf(endId);
            indexParam.setEndIndex(index);
            indexParam.setParallelEndId(endId);
            indexEndNumberVOs.add(indexParam);
        }
        //排序得到最大值
        indexEndNumberVOs.sort(Comparator.comparingInt(NumberVO::getEndIndex));
        return indexEndNumberVOs;
    }

    private Node getNode(List<Node> nodes, String currentNodeId) {
        Optional<Node> optional = nodes.stream()
                .filter(node -> Objects.equals(node.getId(), currentNodeId))
                .findFirst();
        if (!optional.isPresent()) {
            throw new SystemException("系统不存在当前节点信息");
        }
        return optional.get();
    }

    /**
     * 比较当前最大值
     *
     * @param currentNodeId 当前节点
     * @param nodes         所有节点
     * @param edges         所有连线
     * @param maxNum        最大值
     */
    private void circle(String currentNodeId,
                        List<Node> nodes,
                        List<Edge> edges,
                        NumberVO maxNum) {
        //判断当前节点是否为并联终止节点
        List<String> sourceIds = getSourceIdsByEdges(edges, currentNodeId);
        if (sourceIds.size() > 1) {
            maxNum.setParallelEndId(currentNodeId);
            maxNum.setParallelEndIds(currentNodeId);
            getEndId(currentNodeId, edges, maxNum);
            return;
        }

        //获取当前分支信息
        Optional<Node> currentNodes = nodes.stream().filter(node -> Objects.equals(node.getId(), currentNodeId)).findFirst();
        if (!currentNodes.isPresent()) {
            throw new SystemException("当前节点不存在");
        }
        Node currentNode = currentNodes.get();
        maxNum.setMaxNum(Math.max(currentNode.getNumber(), maxNum.getMaxNum()));
        //判断下个节点是否为并联节点
        List<String> nextIds = getTargetIdsByEdges(edges, currentNodeId);
        if (nextIds.size() == 1) {
            //串联
            circle(nextIds.get(0), nodes, edges, maxNum);
        } else if (nextIds.size() > 1) {
            //并联
            maxNum.setParallelTargetIds(nextIds);
            NumberVO parallelNumber = new NumberVO(0);
            parallel(nextIds, nodes, edges, parallelNumber);
            maxNum.setMaxNum(parallelNumber.getMaxNum());
            if (Strings.isNotEmpty(parallelNumber.getParallelEndIdFinal())) {
                circle(parallelNumber.getParallelEndIdFinal(), nodes, edges, maxNum);
            }
        }
    }

    /**
     * 循环遍历得到结果集
     *
     * @param nodeId
     * @param edges
     * @param numberVO
     */
    private void getEndId(String nodeId, List<Edge> edges, NumberVO numberVO) {
        List<String> sourceIds = getSourceIdsByEdges(edges, nodeId);
        if (!sourceIds.isEmpty()) {
            numberVO.setParallelEndIds(numberVO.getParallelEndIds() + "," + nodeId);
            List<String> nextIds = getTargetIdsByEdges(edges, nodeId);
            if (!nextIds.isEmpty()) {
                getEndId(nextIds.get(0), edges, numberVO);
            }
        }
    }

    private List<String> getSourceIdsByEdges(List<Edge> allEdgePOs, String targetId) {
        return allEdgePOs.stream()
                .filter(allEdgePO -> Objects.equals(targetId, allEdgePO.getTarget()))
                .map(Edge::getSource)
                .collect(Collectors.toList());
    }

    private List<String> getTargetIdsByEdges(List<Edge> allEdgePOs, String sourceId) {
        return allEdgePOs.stream()
                .filter(allEdgePO -> Objects.equals(sourceId, allEdgePO.getSource()))
                .map(Edge::getTarget)
                .collect(Collectors.toList());
    }

    private List<String> getFirstNodeIds(List<Node> nodes, List<Edge> edges) {
        List<String> firstNodeIds = new ArrayList<>();
        for (Edge edge : edges) {
            boolean isExists = edges.stream()
                    .anyMatch(edge1 -> Objects.equals(edge.getSource(), edge1.getTarget()));
            if (!isExists) {
                firstNodeIds.add(edge.getSource());
            }
        }

        for (Node node : nodes) {
            String nodeId = node.getId();
            boolean isExists = edges.stream()
                    .anyMatch(edgePO -> (Objects.equals(edgePO.getSource(), nodeId) || Objects.equals(edgePO.getTarget(), nodeId)));
            if (!isExists) {
                firstNodeIds.add(nodeId);
            }
        }
        return firstNodeIds.stream().distinct().collect(Collectors.toList());
    }
}
