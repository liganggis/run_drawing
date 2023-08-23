/**
 * Copyright (C), 2018-2022, 武汉大学
 * FileName: RouteUnit
 * Author:   天行健
 * Date:     2022/12/4 14:42
 * Description: 输入图形地图匹配的结果单元
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈输入图形地图匹配的结果单元〉
 *
 * @author 天行健
 * @create 2022/12/4
 * @since 1.0.0
 */
public class RouteUnit {
    /** Graph中节点ID **/
    public String sourceId = "";
    /** Graph中节点对应的输入图形目标节点 **/
    public DataUnit targetNode = null;
    /** 目标方位角 **/
    public Double bearing = Double.NaN;
    /** Graph中节点和输入图形目标节点之间的长度 **/
    public Double length = 0.0;
    /** Graph中节点和输入图形目标节点之间的节点列表 **/
    public List<String> edgeNodes;

    public RouteUnit() {}

    public RouteUnit(String sourceId, DataUnit targetNode) {
        this.sourceId  =sourceId;
        this.targetNode = targetNode;
        edgeNodes = new ArrayList<>();
    }

    public RouteUnit(String sourceId, DataUnit targetNode, Double bearing) {
        this.sourceId  =sourceId;
        this.targetNode = targetNode;
        this.bearing = bearing;
        edgeNodes = new ArrayList<>();
    }

    public RouteUnit(RouteUnit routeUnit) {
        this.sourceId = routeUnit.sourceId;
        this.targetNode = routeUnit.targetNode;
        this.bearing = routeUnit.bearing;
        this.length = routeUnit.length;
        this.edgeNodes = new ArrayList<>(routeUnit.edgeNodes);
        this.next = routeUnit.next;
        this.last = routeUnit.last;
    }

    public String toString() {
        return "Source id: " + this.sourceId + "; " +
                "TargetNode: " + this.targetNode + "; " +
                "The bearing is: " + this.bearing + "; " +
                "The length is: " + this.length + "; " +
                "The edgeNodes are: " + this.edgeNodes + "; ";
    }

    public RouteUnit next = null;
    public RouteUnit last = null;
}