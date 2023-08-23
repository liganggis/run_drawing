/**
 * Copyright (C), 2018-2022, 武汉大学
 * FileName: Run
 * Author:   天行健
 * Date:     2022/12/1 22:07
 * Description: 主运行类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */

import algorithm.DataUnit;
import algorithm.RouteUnit;
import algorithm.VGMM;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import utils.Graph;
import utils.SaxFunc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 〈一句话功能简述〉<br> 
 * 〈主运行类〉
 *
 * @author 天行健
 * @create 2022/12/1
 * @since 1.0.0
 */
public class Run {

    public static void main(String[] args) {
        /** 1. 解析xml路网数据 **/
        SaxFunc saxFunc = new SaxFunc();

        // saxFunc.parseXML("H:/Users/IDEA/SelfMatch/data/graphml/yangli1-shenzhen_nanshan.graphml");
        // saxFunc.parseXML("H:/Users/IDEA/SelfMatch/data/graphml/yangli3-wuhan_hongshan.graphml");
        // saxFunc.parseXML("H:/Users/IDEA/SelfMatch/data/graphml/yangli5-beijing_dongcheng.graphml");
        // saxFunc.parseXML("H:/Users/IDEA/SelfMatch/data/graphml/yangli7-shanghai_minhang.graphml");
        saxFunc.parseXML("H:/Users/IDEA/SelfMatch/data/graphml/yangli10-xian_changan.graphml");
        // saxFunc.parseXML("H:/Users/IDEA/SelfMatch/data/graphml/verify.graphml");
        // saxFunc.parseXML("H:/Users/IDEA/SelfMatch/data/graphml/Futian.graphml");
        /** 2. 根据解析的路网数据生成无向图 **/
        Graph graph = saxFunc.getGraph();
        System.out.println("Edge num is: " + (graph.getEdgeNum() * 2) + "   ---    " + graph.getVerNum());
        // graph.print();

        // /** Temp:  输入矢量图像输出特征表达模型**/
        // List<String> nodeList = new ArrayList<>();
        // List<String> edgeList = new ArrayList<>();
        // List<String> datas = new ArrayList<>();
        // dfsForTemplate("1", graph, edgeList, nodeList, datas);
        //
        // for (String edgeStr : edgeList) {
        //     System.out.println("-------------------------------边(u,v):   " + edgeStr);
        // }
        // for (String nodeStr : datas) {
        //     System.out.println("-------------------------------结点Id:   " + nodeStr);
        // }

        // Graph.Vertex vertex = graph.getVertex("734045321");
        // Graph.Edge edge = vertex.first;
        // while (edge != null) {
        //     System.out.println("333333333333333333333:  " + edge.getTarget() + ";   " + edge.getSource() +";   " + edge.getBearing());
        //     edge = edge.next;
        // }


        /** 3. 计算无向图各节点的度 **/
        graph.calculateDegree();
        // for (String id : graph.getVertexs()) {
        //     System.out.println("Current node id is:  " + id + ";  the neighbor nodes are: " + graph.getNeighbors(id).size() + "; the degree of currNode is:  " + graph.getVertex(id).getDegree());
        // }

        // List<String> strings = graph.getNeighbors("2481908742");
        // for (String str : strings) {
        //     System.out.println(str);
        // }

        /** 4. 基于输入图形模板化数据进行搜索匹配 **/

        // I 3 4 5 2 O heart y L U E
        String target = "E";
        search(graph, target, "data/dataunit/Final/" + target + ".json", "data/OutTxt/Combination/changan/" + target + "_test.txt");
    }

    /**
     * 绘制图
     */
    public static void plotGraph() {
        /** 1. 解析xml路网数据 **/
        SaxFunc saxFunc = new SaxFunc();
        saxFunc.parseXML("H:/Users/IDEA/SelfMatch/data/graphml/test3v1.graphml");
        /** 2. 根据解析的路网数据生成无向图 **/
        Graph graph = saxFunc.getGraph();

        Set<String> keySets = graph.getVertexs();

        BufferedImage bufferedImage = new BufferedImage(1080, 540, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);	// 消除文字锯齿
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 消除图片锯齿
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); // 使用高质量压缩

        graphics2D.setPaint(Color.cyan);
        Iterator<String> iterator = keySets.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Graph.Vertex vertex = graph.getVertex(key);
            Integer x = (int) Double.parseDouble(vertex.getX());
            Integer y = (int) Double.parseDouble(vertex.getY());
            graphics2D.fillOval(x, y, 3, 3);
        }
        graphics2D.setPaint(Color.black);
        graphics2D.setStroke(new BasicStroke(1f));	// 画笔粗细
        Iterator<String> iterator2 = keySets.iterator();
        while (iterator2.hasNext()) {
            String key = iterator2.next();
            Graph.Vertex vertex = graph.getVertex(key);
            Graph.Edge edge = vertex.first;
            while (edge != null) {
                String source = edge.getSource();
                Graph.Vertex sourceVertex = graph.getVertex(source);
                Integer sourceX = (int) Double.parseDouble(sourceVertex.getX());
                Integer sourceY = (int) Double.parseDouble(sourceVertex.getY());
                String target = edge.getTarget();
                Graph.Vertex targetVertex = graph.getVertex(target);
                Integer targetX = (int) Double.parseDouble(targetVertex.getX());
                Integer targetY = (int) Double.parseDouble(targetVertex.getY());
                graphics2D.drawPolyline(new int[]{sourceX, targetX}, new int[]{sourceY, targetY}, 2);
                edge = edge.next;
            }
        }
        try {
            File srcFile = new File("data/image/test.png");
            ImageIO.write(bufferedImage, "png", srcFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        graphics2D.dispose();
        bufferedImage.flush();
    }

    public static void search(Graph graph, String target, String templateFilePath, String outFilePath) {
        /** 1. 获取输入图形模板化数据 **/
        DataUnit dataUnit = readJson(templateFilePath);
        // System.out.println(dataUnit.center);
        // System.out.println(dataUnit.centerNodes);
        // while (dataUnit3 != null) {
        //     System.out.println(dataUnit3.toString());
        //     dataUnit3 = dataUnit3.next;
        // }

        // System.out.println("++++++++++" + graph.getNeighbors("1144975763"));
        Instant startTime = Instant.now();

        /** 2. 在无向图中进行输入图形的自适应匹配 **/
        VGMM vgmm = new VGMM(graph);
        if (dataUnit.center) vgmm.setCenterNodes(dataUnit.centerNodes);
        vgmm.startSearch(dataUnit);

        // vgmm.startSearchByNode("2402273641", dataUnit);
        /** 3. 搜索结果的处理 **/
        List<RouteUnit> routeUnits = vgmm.getRouteUnitList();
        System.out.println("=============================================================");
        // System.out.println(routeUnits);
        System.out.println("输入图形的匹配搜索结果的个数： " + routeUnits.size());
        // vgmm.parseRouteUnitList(routeUnits,
        //         "H:/Users/IDEA/SelfMatch/data/json/6v1.json"
        // );
        // 过滤重复结果
        // 待补充

        vgmm.parseRouteUnitListInTxtThread(target, routeUnits, outFilePath);
        Instant finishTime = Instant.now();
        long timeElapsed = Duration.between(startTime, finishTime).toMillis();
        System.out.println("搜索匹配耗时(ms):   " + timeElapsed);
    }

    /**
     * 读取输入图形模板
     * @param dataPath
     * @return
     */
    public static DataUnit readJson(String dataPath) {
        File dataFile = new File(dataPath);
        String jsonStr = "";
        if (dataFile.isFile() && dataFile.exists()) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonStr += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // System.out.println(jsonStr);
        JSONArray jsonArray = JSON.parseArray(jsonStr);
        DataUnit dataUnit = null;
        for (int i = jsonArray.size() - 1; i >= 0; i--) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (i == jsonArray.size() - 1) {
                dataUnit = JSON.parseObject(JSON.toJSONString(jsonObject), DataUnit.class);
            } else {
                DataUnit tempDataUnit = JSON.parseObject(JSON.toJSONString(jsonObject), DataUnit.class);
                dataUnit.last = tempDataUnit;
                tempDataUnit.next = dataUnit;
                dataUnit = tempDataUnit;
            }
        }
        return dataUnit;
    }

    /**
     * 深度优先遍历边
     * @param nodeId
     * @param graph
     * @param edgeList
     */
    public static void dfsForTemplate(String nodeId, Graph graph, List<String> edgeList, List<String> nodeList, List<String> datas) {
        Graph.Vertex currNode = graph.getVertex(nodeId);


        Graph.Edge edge = currNode.first;

        // 循环遍历当前结点的相邻结点
        while (edge != null) {
            String neighborNodeId = edge.getTarget();

            if (edge.getVisited() != 1) {
                edge.setVisited(1);
                setEdgeVisited(neighborNodeId, nodeId, graph);
                edgeList.add("(" + edge.getSource() + ", " + edge.getTarget() + ")");
                if (!nodeList.isEmpty()) {
                    // if (nodeList.get(nodeList.size() - 1).equals(nodeId)) {
                    //     // 判断最近的重复结点之间是否为回路
                    //     if (nodeList.contains(neighborNodeId)) System.out.println("(" + edge.getSource() + ", " + edge.getTarget() + ")" + "         " + neighborNodeId);
                    //
                    //     nodeList.add(neighborNodeId);
                    //     // 角度约束
                    //     // String tempStr = datas.get(datas.size() - 1);
                    //     // datas.remove(datas.size() - 1);
                    //     // datas.add(tempStr + "isAngleConstraint: True; ");
                    //     // 边约束
                    //     // datas.add(neighborNodeId + "; isFirst: False; isLengthConstraint: True; ");
                    //     // else datas.add(neighborNodeId + "; isFirst: False; isLengthConstraint: False; ");
                    //     //else nodeList.add(neighborNodeId + "; isFirst: False; ");
                    // } else {
                    //     // 判断是否回路
                    //     // if (nodeList.contains(nodeId)) System.out.println("(" + edge.getSource() + ", " + edge.getTarget() + ")" + "         " + nodeId);
                    //     // if (nodeList.contains(neighborNodeId)) System.out.println("(" + edge.getSource() + ", " + edge.getTarget() + ")" + "         " + neighborNodeId);
                    //     nodeList.add(nodeId);
                    //     nodeList.add(neighborNodeId);
                    //     // // 角度约束
                    //     // datas.add(nodeId + "; isFirst: False; isLengthConstraint: False; isAngleConstraint: True; ");
                    //     // 边约束
                    //     // datas.add(neighborNodeId + "; isFirst: False; isLengthConstraint: True; ");
                    //     // if (edgeList.size() > 1) datas.add(neighborNodeId + "; isFirst: False; isLengthConstraint: True; ");
                    //     // else datas.add(neighborNodeId + "; isFirst: False; isLengthConstraint: False; ");
                    // }
                    nodeList.add(nodeId);
                    nodeList.add(neighborNodeId);
                    // 角度约束
                    datas.add(nodeId + "; isFirst: False; isLengthConstraint: False; isAngleConstraint: True; ");
                    datas.add(neighborNodeId + "; isFirst: False; isLengthConstraint: True; ");
                } else {
                    nodeList.add(nodeId);
                    nodeList.add(neighborNodeId);
                    datas.add(nodeId + "; isFirst: True; isLengthConstraint: False; isAngleConstraint: False; ");
                    datas.add(neighborNodeId + "; isFirst: False; isLengthConstraint: False; ");
                }


                // 结点列表添加
                dfsForTemplate(neighborNodeId, graph, edgeList, nodeList, datas);
            }
            /** 同一结点相邻边的循环 **/
            edge = edge.next;
        }
    }

    /**
     * 无向图设置当前边的相反边的访问状态
     * @param neighborNodeId
     * @param currNodeId
     * @param graph
     */
    public static void setEdgeVisited(String neighborNodeId, String currNodeId, Graph graph) {
        Graph.Vertex neighborNode = graph.getVertex(neighborNodeId);
        Graph.Edge edge = neighborNode.first;
        while (edge != null) {
            if (edge.getTarget().equals(currNodeId)) {
                edge.setVisited(1);
            }
            edge = edge.next;
        }
    }
}