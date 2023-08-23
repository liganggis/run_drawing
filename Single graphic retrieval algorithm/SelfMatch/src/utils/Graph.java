/**
 * Copyright (C), 2018-2022, 武汉大学
 * FileName: Graph
 * Author:   天行健
 * Date:     2022/12/2 17:38
 * Description: 邻接表定义图结构
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package utils;

import java.util.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈邻接表定义图结构〉
 *
 * @author 天行健
 * @create 2022/12/2
 * @since 1.0.0
 */
public class Graph {
    /** 存储所有顶点 **/
    HashMap<String, Vertex> vertexMap;

    /** 顶点的个数 **/
    private int numV = 0;

    /** 边的个数 **/
    private int numE = 0;

    /** 图的节点 **/
    public static class Vertex {
        private String osmid = "";
        private String x = ""; // d4
        private String y = ""; // d3
        private String highway = ""; // d5
        private String street_count = ""; // d6

        public Edge first = null;
        private Integer visited = 0; // 顶点访问标记数组：0:未访问 1:已访问
        private Integer degree = 0; // 图节点的度

        Vertex() {}

        public void setOsmid(String osmid) {
            this.osmid = osmid;
        }
        public void setX(String xs) {
            this.x = xs;
        }
        public void setY(String ys) {
            this.y = ys;
        }
        public void setHighway(String highway) {
            this.highway = highway;
        }
        public void setStreetCount(String streetCount) {
            this.street_count = streetCount;
        }
        public void setDegree(Integer degree) {
            this.degree = degree;
        }
        public void setVisited(Integer vd) { this.visited = vd; }

        public String getOsmid() {
            return osmid;
        }
        public String getX() {
            return this.x;
        }
        public String getY() {
            return this.y;
        }
        public String getHighway() {
            return this.highway;
        }
        public String getStreetCount() {
            return this.street_count;
        }
        public Integer getDegree() {
            return degree;
        }
        public Integer getVisited() { return visited; }

        /** 获取节点的经纬度 **/
        public List<Double> getXY() {
            List<Double> res = new ArrayList<>();
            res.add(0, Double.parseDouble(this.x));
            res.add(1, Double.parseDouble(this.y));
            return res;
        }

        public String toString() {
            return "Osmid: " + this.osmid + "; " + "Longitude for vertex: " +  this.x + "; " + "Latitude for vertex: " + this.y + "; ";
        }
    }

    /** 图的边 **/
    public static class Edge {
        private String osmid = ""; // d8
        private String oneway = ""; // d9
        private String highway = ""; // d11
        private Double length = 0.0; // d12
        private Double bearing = 0.0; // d13
        private String source = ""; // 边的初始节点
        private String target = ""; // 边的终止节点
        private Integer visited = 0; // 边访问标记数组：0:未访问 1:已访问

        Edge() {}
        Edge(String osmid, String oneway, String highway, Double length, Double bearing, String source, String target) {
            this.osmid = osmid;
            this.oneway = oneway;
            this.highway = highway;
            this.length = length;
            this.bearing = bearing;
            this.source = source;
            this.target = target;
        }

        public Edge next = null; // 初始节点的下一条边

        public void setOsmid(String osmid) {
            this.osmid = osmid;
        }
        public void setOneway(String oneway) {
            this.oneway = oneway;
        }
        public void setHighway(String highway) {
            this.highway = highway;
        }
        public void setLength(Double length) {
            this.length = length;
        }
        public void setBearing(Double bearing) {
            this.bearing = bearing;
        }
        public void setSource(String source) {
            this.source = source;
        }
        public void setTarget(String target) {
            this.target = target;
        }
        public void setVisited(Integer vd) { this.visited = vd; }

        public String getOsmid() {
            return osmid;
        }
        public String getOneway() {
            return oneway;
        }
        public String getHighway() {
            return highway;
        }
        public Double getLength() {
            return length;
        }
        public Double getBearing() {
            return bearing;
        }
        public String getSource() {
            return source;
        }
        public String getTarget() {
            return target;
        }
        public Integer getVisited() { return visited; }

        public Boolean equals(Edge edge) {
            if (this.source.equals(edge.getSource()) && this.target.equals(edge.getTarget())) {
                return true;
            } else {
                return false;
            }
        }

        public String toString() {
            return "Osmid: " + this.osmid + "; "
                    + "Source vertex id: " + this.source + "; "
                    + "Target vertex id: " + this.target + "; "
                    + "Edge length: " + this.length + "; "
                    + "Bearing from source to target: " + this.bearing + "; ";
        }
    }

    Graph() {
        vertexMap = new HashMap<>();
    }

    /** 添加顶点 **/
    public void insertVertex(HashMap<String, Vertex> vertexMap) {
        this.vertexMap.putAll(vertexMap);
        numV = vertexMap.size();
    }

    /** 添加边列 **/
    public void insertEdge(List<Edge> edges) {
        for (Edge edge : edges) {
            _insertEdge(edge);
            _insertEdge(reverseEdge(edge));
        }
        numE += edges.size();
    }

    /** 添加单边 **/
    public Edge reverseEdge(Edge edge) {
        Double bearing = edge.getBearing();
        if (bearing > 180.0) {
            bearing = bearing - 180.0;
        } else if (bearing == 180.0) {
            bearing = 0.0;
        } else {
            bearing = bearing + 180.0;
        }
        return new Edge(edge.getOsmid(),
                edge.getOneway(),
                edge.getHighway(),
                edge.getLength(),
                bearing,
                edge.getTarget(),
                edge.getSource());
    }

    /** 添加单边 **/
    public void _insertEdge(Edge edge) {
        Vertex sourceVertex = vertexMap.get(edge.getSource());
        if (sourceVertex == null) {
            return;
        }
        if (sourceVertex.first == null) {
            sourceVertex.first = edge;
        } else {
            /** 避免重复添加相邻边 **/
            Edge lastEdge = sourceVertex.first;
            if (lastEdge.equals(edge)) {
                return;
            }
            while (lastEdge.next != null) {
                lastEdge = lastEdge.next;
                if (lastEdge.equals(edge)) {
                    return;
                }
            }
            lastEdge.next = edge;
        }
    }

    /** 获取顶点个数 **/
    public int getVerNum() {
        return this.numV;
    }

    /** 获取边的个数 **/
    public int getEdgeNum() {
        return this.numE;
    }

    /** 获取顶点的相邻节点 **/
    public List<String> getNeighbors(String vertexId) {
        Vertex currVertex = this.vertexMap.get(vertexId);
        List<String> neighborIds = new ArrayList<>();
        Edge edge = currVertex.first;
        while (edge != null) {
            neighborIds.add(edge.getTarget());
            edge = edge.next;
        }
        return neighborIds;
    }

    /** 获取所有顶点的id **/
    public Set<String> getVertexs() {
        return vertexMap.keySet();
    }

    /** 根据id获取顶点对象 **/
    public Vertex getVertex(String vexId) {
        return vertexMap.get(vexId);
    }

    /** 根据起始顶点获取边 **/
    public Edge getEdge(String source, String target) {
        Vertex vertex = vertexMap.get(source);
        Edge edge = vertex.first;
        while (edge != null) {
            if (target.equals(edge.getTarget())) {
                return edge;
            }
            edge = edge.next;
        }
        return null;
    }

    /** 计算每个顶点的度并赋予顶点属性 **/
    public void calculateDegree() {
        if (numV == 0 || numE == 0) return;
        for (String vexId: vertexMap.keySet()) {
            Vertex vertex = vertexMap.get(vexId);
            Integer numD = 0;
            Edge edge = vertex.first;
            while (edge != null) {
                numD++;
                edge = edge.next;
            }
            vertex.setDegree(numD);
        }
    }

    /** 打印图 **/
    public void print() {
        Set<HashMap.Entry<String, Vertex>> set = vertexMap.entrySet();
        Iterator<HashMap.Entry<String, Vertex>> iterator = set.iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, Vertex> entry = iterator.next();
            Vertex vertex = entry.getValue();
            Edge edge = vertex.first;
            while (edge != null) {
                System.out.println(vertex.getOsmid() + " 指向 " + edge.getTarget() + " 权值为：" + edge.getLength());
                edge = edge.next;
            }
        }
    }
}