/**
 * Copyright (C), 2018-2022, 武汉大学
 * FileName: SaxFunc
 * Author:   天行健
 * Date:     2022/12/1 22:20
 * Description: Sax解析xml文件
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Sax解析xml文件〉
 *
 * @author 天行健
 * @create 2022/12/1
 * @since 1.0.0
 */
public class SaxFunc {

    private Graph graph = new Graph();

    public void parseXML(String filePath) {
        try {
            // SAX解析
            // 1.获取解析工厂
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // 2.从解析工厂获取解析器
            SAXParser saxParser = factory.newSAXParser();
            // 3.得到解读器
            XMLReader xmlReader = saxParser.getXMLReader();
            // 4.设置内容处理器
            PHandler pHandler = new PHandler();
            xmlReader.setContentHandler(pHandler);
            // 5.读取xml的文档内容
            xmlReader.parse(filePath);

            // 6.输出提取的XML内容
            HashMap<String, Graph.Vertex> vertexMap = pHandler.getVertexMap();
            List<Graph.Edge> edgeList = pHandler.getEdgeList();
            System.out.println("-----------:   " + edgeList.size());
            // Set<HashMap.Entry<String, Graph.Vertex>> set = vertexMap.entrySet();
            // Iterator<HashMap.Entry<String, Graph.Vertex>> iterator = set.iterator();
            // while (iterator.hasNext()) {
            //     HashMap.Entry<String, Graph.Vertex> entry = iterator.next();
            //     Graph.Vertex vertex = entry.getValue();
            //     System.out.println("------Vertex:      " + vertex.toString());
            // }
            // for(Graph.Edge edge : edgeList) {
            //     System.out.println("------Edge:       " + edge.toString());
            // }
            graph.insertVertex(vertexMap);
            graph.insertEdge(edgeList);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Graph getGraph() {
        return this.graph;
    }
}

class PHandler extends DefaultHandler {

    // XML key值对应表
    private Map<String, HashMap> keyAttr;
    private HashMap<String, Graph.Vertex> vertexMap;
    private List<Graph.Edge> edgeList;
    private Graph.Vertex vertex;
    private Graph.Edge edge;
    // 标签名称
    private String tag = null;
    private String dataKey = null;

    /**
     * 文档解析开始时调用，该方法只会调用一次
     * @throws SAXException
     */
    @Override
    public void startDocument() throws SAXException {
        System.out.println("----解析文档开始----");
        keyAttr = new HashMap<>();
        vertexMap = new HashMap<>();
        edgeList = new ArrayList<>();
    }

    /**
     * 每当遇到起始标签时调用
     * @param uri xml文档的命名空间
     * @param localName 标签的名字
     * @param qName 带命名空间的标签的名字
     * @param attributes 标签的属性集
     * @throws SAXException
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // System.out.println("标签<"+qName + ">解析开始");
        if (null != qName) {
            tag = qName;
            if ("key".equals(tag)) {
                HashMap hashMap = new HashMap();
                for (int i = 0; i < attributes.getLength(); i++) {
                    hashMap.put(attributes.getQName(i), attributes.getValue(i));
                      // System.out.println("       Attribute: " + attributes.getQName(i) + "=\""
                      //     + attributes.getValue(i) + "\"");
                }
                keyAttr.put((String) hashMap.get("id"), hashMap);
            } else if ("node".equals(tag)) {
                String idAttr = "";
                for (int i = 0; i < attributes.getLength(); i++) {
                    if ("id".equals(attributes.getQName(i))) {
                        idAttr = attributes.getValue(i);
                    }
                }
                vertex = new Graph.Vertex();
                vertex.setOsmid(idAttr);
            } else if ("edge".equals(tag)) {
                String sourceAttr = "", targetAttr = "";
                for (int i = 0; i < attributes.getLength(); i++) {
                    if ("source".equals(attributes.getQName(i))) {
                        sourceAttr = attributes.getValue(i);
                    } else if ("target".equals(attributes.getQName(i))) {
                        targetAttr = attributes.getValue(i);
                    }
                }
                edge = new Graph.Edge();
                edge.setSource(sourceAttr);
                edge.setTarget(targetAttr);
            } else if ("data".equals(tag)) {
                dataKey = attributes.getValue(0);
            }
        }
    }

    /**
     * 解析标签内的内容的时候调用
     * @param ch 当前读取到的TextNode(文本节点)的字节数组
     * @param start 字节开始的位置，为0则读取全部
     * @param length 当前TextNode的长度
     * @throws SAXException
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String contents = new String(ch, start, length).trim();
        if (contents.length() > 0) {
            if ("data".equals(tag) && dataKey != null) {
                HashMap hashMap = keyAttr.get(dataKey);
                String attr = (String) hashMap.get("attr.name") + "_" + (String) hashMap.get("for");
                switch (attr) {
                    case "y_node":
                        vertex.setY(contents);
                        break;
                    case "x_node":
                        vertex.setX(contents);
                        break;
                    case "highway_node":
                        vertex.setHighway(contents);
                        break;
                    case "street_count_node":
                        vertex.setStreetCount(contents);
                        break;
                    case "osmid_edge":
                        edge.setOsmid(contents);
                        break;
                    case "oneway_edge":
                        edge.setOneway(contents);
                        break;
                    case "highway_edge":
                        edge.setHighway(contents);
                        break;
                    case "length_edge":
                        edge.setLength(Double.parseDouble(contents));
                        break;
                    case "bearing_edge":
                        edge.setBearing(Double.parseDouble(contents));
                        break;
                    default:
                        break;
                }
            }
        } else {
            // System.out.println("内容为-->" + "空");
        }
    }

    /**
     * 每当遇到结束标签时调用
     * @param uri xml文档的命名空间
     * @param localName 标签的名字
     * @param qName 带命名空间的标签的名字
     * @throws SAXException
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("data".equals(qName)) {
            if (dataKey != null) dataKey = null;
        } else if ("node".equals(qName)) {
            vertexMap.put(vertex.getOsmid(), vertex);
            vertex = null;
        } else if ("edge".equals(qName)) {
            edgeList.add(edge);
            edge = null;
        } else if ("key".equals(qName)) {

        }
        tag = null;
    }

    /**
     * 文档解析结束后调用，该方法只会调用一次
     * @throws SAXException
     */
    @Override
    public void endDocument() throws SAXException {
        System.out.println("----解析文档结束----");
    }

    public HashMap<String, Graph.Vertex> getVertexMap() {
        return this.vertexMap;
    }

    public List<Graph.Edge> getEdgeList() {
        return this.edgeList;
    }
}