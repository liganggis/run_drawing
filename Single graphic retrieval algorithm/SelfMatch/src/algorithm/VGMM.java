/**
 * Copyright (C), 2018-2022, 武汉大学
 * FileName: VectorMatching
 * Author:   天行健
 * Date:     2022/12/3 22:32
 * Description: 矢量图形地图匹配
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package algorithm;

/**
 * 〈一句话功能简述〉<br> 
 * 〈矢量图形的地图匹配〉
 *
 * @author 天行健
 * @create 2022/12/3
 * @since 1.0.0
 */

import com.alibaba.fastjson2.JSON;
import javafx.scene.Parent;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.*;
import utils.Graph;
import utils.ResLg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/** Map matching of vector graphics(矢量图形的地图匹配) **/
public class VGMM {
    /** 数据源：图 **/
    private Graph graph;

    /** 输入图形的匹配结果 **/
    private List<RouteUnit> routeUnitList;

    /** 禁忌表 **/
    private List<String> tabus;
    private List<String> centerNodes;

    /** 初始化 **/
    public VGMM(Graph graph) {
        this.graph = graph;
        this.routeUnitList = new ArrayList<>();
        this.tabus = new ArrayList<>();
        this.centerNodes = new ArrayList<>();
    }

    public void setCenterNodes(List<String> centerNodes) {
        this.centerNodes = centerNodes;
    }

    public Double getBearing(Double lastAzimuth, Double leftAngle) {
        Double bearing = lastAzimuth + leftAngle - 180.0;
        if (bearing < 0.0) bearing += 360.0;
        else if (bearing > 360) bearing -= 360.0;
        return bearing;
    }

    /**
     * 一般性节点的评价函数
     * @param lastNodeId
     * @param lastAzimuth
     * @param currNodeId
     * @param neighborNodeId
     * @param currRouteNode
     * @param targetNode
     * @param isLineExtend
     * @return
     */
    public Integer meritFunc(String lastNodeId, Double lastAzimuth, String currNodeId, String neighborNodeId, Integer vertexNum, RouteUnit currRouteNode, DataUnit targetNode, Boolean isLineExtend) {
        // System.out.println("--------:   " + targetNode.id + ";  " + targetNode.frontId);

        // 全局控制在算法计算过程中是否考虑距离约束
        boolean isLengthConstraint = targetNode.isLengthConstraint;
        if (!Constant.ISLENGTHCONSTRAINT) isLengthConstraint = Constant.ISLENGTHCONSTRAINT;

        // 全局控制在算法计算过程中是否考虑直线延伸约束
        if (!Constant.ISLINEEXTEND) isLineExtend = false;

        // 1. 获取相邻边的方位角
        Double neighborAzimuth = graph.getEdge(currNodeId, neighborNodeId).getBearing();
        // 2. 计算前一条边和相邻边之间的夹角(左角)
        Double angle = neighborAzimuth - lastAzimuth + 180.0;
        if (angle < 0.0) angle += 360.0;
        else if (angle > 360) angle -= 360.0;

        if (vertexNum < targetNode.degree) {
            if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
                // 考虑形状内部线段间长度约束
                if (isLengthConstraint) {
                    Double molecular = currRouteNode.length + graph.getEdge(lastNodeId, currNodeId).getLength();
                    Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
                    molecular += neighborLength;
                    Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                    if (molecular <= denominator * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else {
                    return -1;
                }
            } else {
                return 0;
            }
        }

        // 3. 方向约束的评价
        if (Math.abs(angle - targetNode.angle) <= Constant.ANGLE_THRESHOLD) {
            // 3.1 判断相同节点的搜索结果是否相同
            if (!targetNode.isRepeat) {
                int res = 0;
                // 既满足转折角约束，也满足直线延伸约束
                // if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
                //     res += 10;
                // }
                // 考虑形状内部线段间长度约束
                if (isLengthConstraint) {
                    Double molecular = currRouteNode.length + graph.getEdge(lastNodeId, currNodeId).getLength();
                    Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                    if (molecular < denominator * targetNode.minRatio * (1.0 - Constant.RATIO)) {
                        if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
                            // 考虑形状内部线段间长度约束
                            Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
                            Double molecular2 = molecular + neighborLength;
                            Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                            if (molecular2 <= denominator2 * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                                return -1;
                            } else {
                                return 0;
                            }
                        } else {
                            return 0;
                        }
                    } else if (molecular > denominator * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                        return 0;
                    } else {
                        if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
                            Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
                            Double molecular2 = molecular + neighborLength;
                            Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                            if (molecular2 <= denominator2 * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                                return 11;
                            } else {
                                return 1;
                            }
                         } else {
                            return 1;
                        }
                    }
                } else {
                     if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
                         return 11;
                     } else {
                         return 1;
                     }
                }
            }
            // 3.2 闭合环不必考虑形状内部线段间长度约束
            else {
                String repeatedNodeId = getCurrNodeIdFromRouteById(targetNode.id, currRouteNode);
                // System.out.println("repeatedNodeId:  " + repeatedNodeId + " ;   " + targetNode.id + "  ;   " + currNodeId);
                if (currNodeId.equals(repeatedNodeId)) {
                    return 1;
                } else {
                    Double molecular = currRouteNode.length + graph.getEdge(lastNodeId, currNodeId).getLength();
                    Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                    if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
                        // 考虑形状内部线段间长度约束
                        Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
                        Double molecular2 = molecular + neighborLength;
                        Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                        if (molecular2 <= denominator2 * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                            return -1;
                        } else {
                            return 0;
                        }
                    } else {
                        return 0;
                    }
                }
            }
        }
        // 4. 直线延伸约束的评价
        else if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
            // 考虑形状内部线段间长度约束
            if (isLengthConstraint) {
                Double molecular = currRouteNode.length + graph.getEdge(lastNodeId, currNodeId).getLength();
                Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
                molecular += neighborLength;
                Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                if (molecular <= denominator * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                // System.out.println("12345:  " + lastNodeId + "; " + currNodeId + ";  " + neighborNodeId);
                return -1;
            }
        }
        // 5. 不满足任一约束(默认返回)
        else {
            return 0;
        }
    }

    /**
     * 叶子节点的评价函数
     * @param lastNodeId
     * @param lastAzimuth
     * @param currNodeId
     * @param neighborNodeId
     * @param currRouteNode
     * @param targetNode
     * @return
     */
    // public Integer meritFuncForLeafNode(String lastNodeId, Double lastAzimuth, String currNodeId, String neighborNodeId, Integer vertexNum, RouteUnit currRouteNode, DataUnit targetNode, Boolean isLineExtend) {
    //     // 全局控制在算法计算过程中是否考虑距离约束
    //     boolean isLengthConstraint = targetNode.isLengthConstraint;
    //     if (!Constant.ISLENGTHCONSTRAINT) isLengthConstraint = Constant.ISLENGTHCONSTRAINT;
    //
    //     // 全局控制在算法计算过程中是否考虑直线延伸约束
    //     if (!Constant.ISLINEEXTEND) isLineExtend = false;
    //
    //     // 1. 获取相邻边的方位角
    //     Double neighborAzimuth = graph.getEdge(currNodeId, neighborNodeId).getBearing();
    //     // 2. 计算前一条边和相邻边之间的夹角(左角)
    //     Double angle = neighborAzimuth - lastAzimuth + 180.0;
    //     if (angle < 0.0) angle += 360.0;
    //     else if (angle > 360) angle -= 360.0;
    //
    //     if (vertexNum < targetNode.degree) {
    //         if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
    //             // 考虑形状内部线段间长度约束
    //             if (isLengthConstraint) {
    //                 Double molecular = currRouteNode.length + graph.getEdge(lastNodeId, currNodeId).getLength();
    //                 Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
    //                 molecular += neighborLength;
    //                 Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
    //                 if (molecular <= denominator * targetNode.maxRatio) {
    //                     return -1;
    //                 } else {
    //                     return 0;
    //                 }
    //             } else {
    //                 return -1;
    //             }
    //         } else {
    //             return 0;
    //         }
    //     }
    //
    //     // 3 判断相同节点的搜索结果是否相同
    //     if (isLengthConstraint) {
    //         Double molecular = currRouteNode.length + graph.getEdge(lastNodeId, currNodeId).getLength();
    //         Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
    //         if (targetNode.isRepeat) {
    //             String repeatedNodeId = getCurrNodeIdFromRouteById(targetNode.id, currRouteNode);
    //             if (currNodeId.equals(repeatedNodeId)) {
    //                 return 2;
    //             } else {
    //                 // molecular <= denominator * targetNode.maxRatio没必要，需验证一下对输出结果有没有影响
    //                 if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
    //                     // 考虑形状内部线段间长度约束
    //                     Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
    //                     Double molecular2 = molecular + neighborLength;
    //                     Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
    //                     if (molecular2 <= denominator2 * targetNode.maxRatio) {
    //                         return -1;
    //                     } else {
    //                         return 0;
    //                     }
    //                 } else {
    //                     return 0;
    //                 }
    //             }
    //         } else {
    //             // (1) 以当前节点作匹配节点时终止边过短，继续搜索待匹配节点
    //             if (molecular < denominator * targetNode.minRatio) {
    //                 // 3. 方向约束的评价
    //                 if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) { //  && vertexNum >= targetNode.degree) {
    //                     Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
    //                     Double molecular2 = molecular + neighborLength;
    //                     Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
    //                     // System.out.println("Result:  " + lastNodeId + " , " + currNodeId + " , " + molecular2 + "  ,  " + denominator2 + "        ====       " + targetNode.id);
    //                     if (molecular2 <= denominator2 * targetNode.maxRatio) {
    //                         return -1;
    //                     } else {
    //                         return 0;
    //                     }
    //                 } else {
    //                     return 0;
    //                 }
    //             }
    //             // (2) 以当前节点作匹配节点时终止边过长，终止搜索
    //             else if (molecular > denominator * targetNode.maxRatio) {
    //                 return 0;
    //             }
    //             // (3) 以当前节点作匹配节点时终止边满足最小最大距离约束，进行下一分支的搜索
    //             else {
    //                 if (isLineExtend && (Math.abs(angle - 180) <= Constant.LINE_THRESHOLD)) { //  && vertexNum >= targetNode.degree) {
    //                     Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
    //                     Double molecular2 = molecular + neighborLength;
    //                     Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
    //                     if (molecular2 <= denominator2 * targetNode.maxRatio) {
    //                         return 12;
    //                     } else {
    //                         return 2;
    //                     }
    //                 } else {
    //                     return 2;
    //                 }
    //             }
    //         }
    //     } else {
    //         if (targetNode.isRepeat) {
    //             String repeatedNodeId = getCurrNodeIdFromRouteById(targetNode.id, currRouteNode);
    //             if (currNodeId.equals(repeatedNodeId)) {
    //                 return 2;
    //             } else {
    //                 if (isLineExtend && (Math.abs(angle - 180) <= Constant.LINE_THRESHOLD)) {
    //                     return -1;
    //                 } else {
    //                     return 0;
    //                 }
    //             }
    //         } else {
    //             if (isLineExtend && (Math.abs(angle - 180) <= Constant.LINE_THRESHOLD)) {
    //                 return 12;
    //             } else {
    //                 // if (graph.getNeighbors(currNodeId).size() <= 2) {
    //                 //     return 2;
    //                 // } else {
    //                 //     return 0;
    //                 // }
    //                 return 2;
    //
    //                 // if (targetNode.last.isFirst) {
    //                 //     System.out.println(" --------  :    " + lastNodeId + "  -  " + targetNode.id + "  -  " + currNodeId + "   -  " + neighborNodeId + "   -    " + angle);
    //                 //     return 2;
    //                 // } else {
    //                 //     System.out.println(" -------567567-  :    " + lastNodeId + "  -  " + targetNode.id + "  -  " + currNodeId + "   -  " + neighborNodeId + "   -    " + angle);
    //                 //     return 0;
    //                 // }
    //             }
    //         }
    //     }
    //
    //     // (1) 以当前节点作匹配节点时终止边过短，继续搜索待匹配节点
    //     // if (molecular < denominator * targetNode.minRatio) {
    //     //     // 3. 方向约束的评价
    //     //     if (Math.abs(angle - targetAngle) < Constant.LINE_THRESHOLD) { //  && vertexNum >= targetNode.degree) {
    //     //         Double neighborLength = graph.getEdge(currNodeId, neighborNodeId).getLength();
    //     //         Double molecular2 = molecular + neighborLength;
    //     //         Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
    //     //         if (molecular2 <= denominator2 * targetNode.maxRatio) {
    //     //             return -1;
    //     //         } else {
    //     //             return 0;
    //     //         }
    //     //     } else {
    //     //         return 0;
    //     //     }
    //     // }
    //     // // (2) 以当前节点作匹配节点时终止边过长，终止搜索
    //     // else if (molecular > denominator * targetNode.maxRatio) {
    //     //     return 0;
    //     // }
    //     // // (3) 以当前节点作匹配节点时终止边满足最小最大距离约束，进行下一分支的搜索
    //     // else {
    //     //     // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
    //     //     if (targetNode.isRepeat) {
    //     //         String repeatedNodeId = getCurrNodeIdFromRouteById(targetNode.id, currRouteNode);
    //     //         if (currNodeId.equals(repeatedNodeId)) {
    //     //             return 2;
    //     //         } else {
    //     //             return 0;
    //     //         }
    //     //     } else {
    //     //         return 2;
    //     //     }
    //     // }
    // }

    /**
     * 叶子结点广度搜索
     * @param lastNodeId
     * @param lastAzimuth
     * @param currNodeId
     * @param vertexNum
     * @param currRouteNode
     * @param targetNode
     * @param isLineExtend
     * @return
     */
    public List<ResLg> meritFuncForLeafNode2(String lastNodeId, Double lastAzimuth, String currNodeId, Integer vertexNum, RouteUnit currRouteNode, DataUnit targetNode, Boolean isLineExtend) {
        // System.out.println("--------:   " + targetNode.id + ";  " + targetNode.frontId);

        // 全局控制在算法计算过程中是否考虑距离约束
        boolean isLengthConstraint = targetNode.isLengthConstraint;
        if (!Constant.ISLENGTHCONSTRAINT) isLengthConstraint = Constant.ISLENGTHCONSTRAINT;

        // 全局控制在算法计算过程中是否考虑直线延伸约束
        if (!Constant.ISLINEEXTEND) isLineExtend = false;

        List<ResLg> res = new ArrayList<>();

        // 广度优先遍历
        Integer numLineExtend = 0;
        for (String nodeId : graph.getNeighbors(currNodeId)) {
            if (nodeId.equals(lastNodeId)) continue;
            // 1. 获取相邻边的方位角
            Double neighborAzimuth = graph.getEdge(currNodeId, nodeId).getBearing();
            // 2. 计算前一条边和相邻边之间的夹角(左角)
            Double angle = neighborAzimuth - lastAzimuth + 180.0;
            if (angle < 0.0) angle += 360.0;
            else if (angle > 360) angle -= 360.0;

            if (isLineExtend && (Math.abs(angle - 180.0) <= Constant.LINE_THRESHOLD)) {
                numLineExtend++;
                // 3 判断相同节点的搜索结果是否相同
                if (isLengthConstraint) {
                    Double molecular = currRouteNode.length + graph.getEdge(lastNodeId, currNodeId).getLength();
                    Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                    if (targetNode.isRepeat) {
                        String repeatedNodeId = getCurrNodeIdFromRouteById(targetNode.id, currRouteNode);
                        if (currNodeId.equals(repeatedNodeId)) {
                            res.add(new ResLg(currNodeId, 2));
                        } else {
                            // 考虑形状内部线段间长度约束
                            Double neighborLength = graph.getEdge(currNodeId, nodeId).getLength();
                            Double molecular2 = molecular + neighborLength;
                            Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                            // System.out.println("Result:  " + lastNodeId + " , " + currNodeId + " , " + molecular2 + "  ,  " + denominator2 + "        ====       " + targetNode.id);
                            if (molecular2 <= denominator2 * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                                res.add(new ResLg(currNodeId, nodeId, -1));
                            } else {
                                res.add(new ResLg(0));
                            }
                        }
                    } else {
                        // System.out.println("===============dsf:   " + currNodeId);
                        // (1) 以当前节点作匹配节点时终止边过短，继续搜索待匹配节点
                        if (molecular < denominator * targetNode.minRatio * (1.0 - Constant.RATIO)) {
                            // 3. 方向约束的评价
                            Double neighborLength = graph.getEdge(currNodeId, nodeId).getLength();
                            Double molecular2 = molecular + neighborLength;
                            Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                            // System.out.println("Result:  " + lastNodeId + " , " + currNodeId + " , " + molecular2 + "  ,  " + denominator2 + "        ====       " + targetNode.id);
                            if (molecular2 <= denominator2 * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                                res.add(new ResLg(currNodeId, nodeId, -1));
                            } else {
                                res.add(new ResLg(0));
                            }
                        }
                        // (2) 以当前节点作匹配节点时终止边过长，终止搜索
                        else if (molecular > denominator * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                            res.add(new ResLg(0));
                        }
                        // (3) 以当前节点作匹配节点时终止边满足最小最大距离约束，进行下一分支的搜索
                        else {
                            Double neighborLength = graph.getEdge(currNodeId, nodeId).getLength();
                            Double molecular2 = molecular + neighborLength;
                            Double denominator2 = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                            if (molecular2 <= denominator2 * targetNode.maxRatio * (1.0 + Constant.RATIO)) {
                                res.add(new ResLg(currNodeId, nodeId, 12));
                            } else {
                               res.add(new ResLg(currNodeId, 2));
                            }
                        }
                    }
                } else {
                    if (targetNode.isRepeat) {
                        String repeatedNodeId = getCurrNodeIdFromRouteById(targetNode.id, currRouteNode);
                        if (currNodeId.equals(repeatedNodeId)) {
                            res.add(new ResLg(currNodeId, 2));
                        } else {
                            res.add(new ResLg(currNodeId, nodeId, -1));
                        }
                    } else {
                        res.add(new ResLg(currNodeId, nodeId, 12));
                    }
                }
            }
        }

        if (numLineExtend == 0) {
            // System.out.println("ppppppppppppppppppppppppppp:  " + currNodeId + ";  ");
            if (isLengthConstraint) {
                Double molecular = currRouteNode.length + graph.getEdge(lastNodeId, currNodeId).getLength();
                Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, currRouteNode);
                if (targetNode.isRepeat) {
                    String repeatedNodeId = getCurrNodeIdFromRouteById(targetNode.id, currRouteNode);
                    if (currNodeId.equals(repeatedNodeId)) {
                        res.add(new ResLg(currNodeId, 2));
                    } else {
                        res.add(new ResLg(0));
                    }
                } else {
                    // if ((molecular >= denominator * targetNode.minRatio * (1.0 - Constant.RATIO)) && (molecular <= denominator * targetNode.maxRatio * (1.0 + Constant.RATIO))) {
                    if ((molecular >= denominator * targetNode.minRatio * (1.0 - Constant.RATIO))) {
                        res.add(new ResLg(currNodeId, 2));
                    } else {
                        res.add(new ResLg(0));
                    }
                }
            } else {
                if (targetNode.isRepeat) {
                    String repeatedNodeId = getCurrNodeIdFromRouteById(targetNode.id, currRouteNode);
                    if (currNodeId.equals(repeatedNodeId)) {
                       res.add(new ResLg(currNodeId, 2));
                    } else {
                        res.add(new ResLg(0));
                    }
                } else {
                    res.add(new ResLg(currNodeId, 2));
                }
            }
        }
        return res;
    }

    /**
     * 根据起终节点在RouteUnit中获取路径长度
     * @param lastTargetNodeId
     * @param currTargetNodeId
     * @param currRouteNode
     * @return
     */
    public Double getLengthFromRouteByNodes(String lastTargetNodeId, String currTargetNodeId, RouteUnit currRouteNode) {
        RouteUnit routeUnit = currRouteNode;
        while (routeUnit != null) {
            if (lastTargetNodeId.equals(routeUnit.targetNode.id) && currTargetNodeId.equals(routeUnit.last.targetNode.id)) {
                if (routeUnit.last.length != 0) return routeUnit.last.length;
            } else if (currTargetNodeId.equals(routeUnit.targetNode.id) && lastTargetNodeId.equals(routeUnit.last.targetNode.id)) {
                if (routeUnit.last.length != 0) return routeUnit.last.length;
            }
            routeUnit = routeUnit.last;
        }
        return Double.NaN;
    }

    /**
     * RouteUnit中获取目标节点对应的图节点
     * @param targetNodeId
     * @param currRouteNode
     * @return
     */
    public String getCurrNodeIdFromRouteById(String targetNodeId, RouteUnit currRouteNode) {
        RouteUnit routeUnit = currRouteNode;
        while (routeUnit != null) {
            if (targetNodeId.equals(routeUnit.targetNode.id)) {
                return routeUnit.sourceId;
            }
            routeUnit = routeUnit.last;
        }
        return "";
    }

    /**
     * 获取当前目标节点对应图节点在lastTargetNodeId和currTargetNodeId构成的边点集中的相邻点
     * @param lastTargetNodeId
     * @param currTargetNodeId
     * @param currRouteNode
     * @return
     */
    public String getLastNodeIdFromRouteById(String lastTargetNodeId, String currTargetNodeId, RouteUnit currRouteNode) {
        RouteUnit routeUnit = currRouteNode;
        while (routeUnit != null) {
            if (lastTargetNodeId.equals(routeUnit.targetNode.id) && currTargetNodeId.equals(routeUnit.last.targetNode.id)) {
                return routeUnit.last.edgeNodes.get(1);
            } else if (currTargetNodeId.equals(routeUnit.targetNode.id) && lastTargetNodeId.equals(routeUnit.last.targetNode.id)) {
                if (routeUnit.last.edgeNodes.size() < 2) {
                    routeUnit = routeUnit.last;
                    continue;
                } else {
                    return routeUnit.last.edgeNodes.get(routeUnit.last.edgeNodes.size() - 2);
                }
            }
            routeUnit = routeUnit.last;
        }
        return "";
    }

    /**
     * lastTargetNodeId和currTargetNodeId对应匹配的边的方位角
     * @param lastTargetNodeId
     * @param currTargetNodeId
     * @param currRouteNode
     * @return
     */
    public Double getLastAzimuthFromRouteById(String lastTargetNodeId, String currTargetNodeId, RouteUnit currRouteNode) {
        RouteUnit routeUnit = currRouteNode;
        while (routeUnit != null) {
            if (lastTargetNodeId.equals(routeUnit.targetNode.id) && currTargetNodeId.equals(routeUnit.last.targetNode.id)) {
                return reverseAzimuth(routeUnit.last.bearing);
            } else if (currTargetNodeId.equals(routeUnit.targetNode.id) && lastTargetNodeId.equals(routeUnit.last.targetNode.id)) {
                return routeUnit.last.bearing;
            }
            routeUnit = routeUnit.last;
        }
        return Double.NaN;
    }

    /**
     *
     * @param lastTargetNodeId
     * @param currTargetNodeId
     * @param currRouteNode
     * @return
     */
    public Double getLastAzimuthFromRouteById2(String lastTargetNodeId, String currTargetNodeId, RouteUnit currRouteNode) {
        RouteUnit routeUnit = currRouteNode;
        while (routeUnit != null) {
            if (lastTargetNodeId.equals(routeUnit.targetNode.id) && currTargetNodeId.equals(routeUnit.last.targetNode.id)) {
                return reverseAzimuth(routeUnit.last.bearing);
            } else if (currTargetNodeId.equals(routeUnit.targetNode.id) && lastTargetNodeId.equals(routeUnit.last.targetNode.id)) {
                return routeUnit.last.bearing;
            }
            routeUnit = routeUnit.last;
        }
        return Double.NaN;
    }

    /**
     * 方位角取反
     * @param azimuth
     * @return
     */
    public Double reverseAzimuth(Double azimuth) {
        Double reverseAzimuth = 0.0;
        if (azimuth > 180) {
            reverseAzimuth = azimuth - 180;
        } else {
            reverseAzimuth = azimuth + 180;
        }
        return reverseAzimuth;
    }

    /**
     * 线段方位角偏移角度angle
     * @param azimuth
     * @param angle
     * @return
     */
    public Double offsetAzimuth(Double azimuth, Double angle) {
        Double result = azimuth + angle;
        if (result < 0.0) result += 360.0;
        else if (result > 360.0) result -= 360.0;
        else result = result;
        return result;
    }

    /**
     * 深度回溯的递归主体
     * @param lastNodeId
     * @param lastAzimuth
     * @param currNodeId
     * @param currRouteNode
     * @param targetNode
     * @param isLineExtend
     */
    public void travel(String lastNodeId, Double lastAzimuth, String currNodeId, RouteUnit currRouteNode, DataUnit targetNode, Boolean isLineExtend) {
        // System.out.println("Travel current node id: " + currNodeId + "  ||  " + targetNode.id);
        // System.out.println("--------:   " + targetNode.id + ";  " + targetNode.frontId);
        Graph.Vertex currNode = graph.getVertex(currNodeId);
        Integer vertexNum = currNode.getDegree();
        Graph.Edge edge = currNode.first;

        // 全局控制在算法计算过程中是否考虑距离约束
        boolean isLengthConstraint = targetNode.isLengthConstraint;
        if (!Constant.ISLENGTHCONSTRAINT) isLengthConstraint = Constant.ISLENGTHCONSTRAINT;
        // if (targetNode.isFirst && targetNode.degree > vertexNum) {
        //     return;
        // }

        // 非起点的叶子结点
        if (!targetNode.isAngleConstraint) {
            if (!targetNode.isFirst) {
                Graph.Edge lastEdge = graph.getEdge(lastNodeId, currNodeId);
                // System.out.println("Result:  " + lastNodeId + " , " + currNodeId + " ," + neighborNodeId);
                // 1.2.2 输入图形的目标节点全部匹配完成
                if (targetNode.next == null) {
                    // 1.2.2.1 输入图形最后一个节点的距离约束(避免终止边过短或过长)
                    List<ResLg> meritResult = meritFuncForLeafNode2(lastNodeId, lastAzimuth, currNodeId, vertexNum, currRouteNode, targetNode, isLineExtend);
                    // System.out.println("Result:  " + lastNodeId + " , " + currNodeId + " , " + neighborNodeId + ";   " + meritResult + "        ====       " + targetNode.isRepeat);
                    for (ResLg resLg : meritResult) {
                        if (resLg.res.equals(0)) {
                            continue;
                        } else if (resLg.res.equals(-1)) {
                            RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
                            // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
                            tempRouteNode.length += lastEdge.getLength();
                            tempRouteNode.edgeNodes.add(currNodeId);
                            // b. 沿直线延伸搜索下一节点
                            travel(currNodeId, lastAzimuth, resLg.neighborNodeId, tempRouteNode, targetNode, true);
                        } else if (resLg.res.equals(2)) {
                            RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
                            // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
                            tempRouteNode.length += lastEdge.getLength();
                            tempRouteNode.edgeNodes.add(currNodeId);
                            if (targetNode.last.isFirst) {
                                if (tempRouteNode.length > 600.0 && tempRouteNode.length < 1000.0) {
                                        // if (tempRouteNode.length > 500.0) break;
                                } else {
                                    break;
                                }
                            }

                            // b. 当前节点的匹配结果
                            RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode);
                            tempRouteNode.next = nextRouteNode;
                            nextRouteNode.last = tempRouteNode;
                            // c. 填充当前节点
                            nextRouteNode.edgeNodes.add(currNodeId);
                            // d. 全局记录输入图形的匹配成功结果
                            routeUnitList.add(nextRouteNode);

                            // 禁忌表
                            if (this.centerNodes.size() != 0) {
                                if (this.centerNodes.contains(targetNode.id)) {
                                    this.tabus.add(currNodeId);
                                }
                            }
                        } else if (resLg.res.equals(12)) {
                            RouteUnit tempRouteNode2 = new RouteUnit(currRouteNode);
                            tempRouteNode2.length += lastEdge.getLength();
                            tempRouteNode2.edgeNodes.add(currNodeId);
                            // b. 沿直线延伸搜索下一节点
                            travel(currNodeId, lastAzimuth, resLg.neighborNodeId, tempRouteNode2, targetNode, true);

                            RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
                            // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
                            tempRouteNode.length += lastEdge.getLength();
                            tempRouteNode.edgeNodes.add(currNodeId);
                            if (targetNode.last.isFirst) {
                                if (tempRouteNode.length > 600.0 && tempRouteNode.length < 1000.0) {
                                        // if (tempRouteNode.length > 500.0) break;
                                } else {
                                    break;
                                }
                            }
                            // b. 当前节点的匹配结果
                            RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode);
                            tempRouteNode.next = nextRouteNode;
                            nextRouteNode.last = tempRouteNode;
                            // c. 填充当前节点
                            nextRouteNode.edgeNodes.add(currNodeId);
                            // d. 全局记录输入图形的匹配成功结果
                            routeUnitList.add(nextRouteNode);

                            // 禁忌表
                            if (this.centerNodes.size() != 0) {
                                if (this.centerNodes.contains(targetNode.id)) {
                                    this.tabus.add(currNodeId);
                                }
                            }
                        }
                    }
                }
                // 1.2.3 叶子节点分支匹配完成，进行下一分支的匹配
                else {
                    // 1.2.3.1 分支叶子节点之间的距离约束(避免终止边过短或过长)
                    List<ResLg> meritResult = meritFuncForLeafNode2(lastNodeId, lastAzimuth, currNodeId, vertexNum, currRouteNode, targetNode, isLineExtend);
                    // System.out.println("Result:  " + lastNodeId + " , " + currNodeId + " , " + lastAzimuth + "  ,  " + neighborNodeId + ";   " + meritResult + "        ====       " + targetNode.id);
                    for (ResLg resLg : meritResult) {
                        if (resLg.res.equals(0)) {
                            continue;
                        } else if (resLg.res.equals(-1)) {
                            RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
                            // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
                            tempRouteNode.length += lastEdge.getLength();
                            tempRouteNode.edgeNodes.add(currNodeId);
                            // b. 沿直线延伸搜索下一节点
                            travel(currNodeId, lastAzimuth, resLg.neighborNodeId, tempRouteNode, targetNode, true);
                        } else if (resLg.res.equals(2)) {
                            // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
                            RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
                            tempRouteNode.length += lastEdge.getLength();
                            tempRouteNode.edgeNodes.add(currNodeId);
                            // b. 获取下一分支的当前节点、上一节点和上一方位角
                            RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode);
                            tempRouteNode.next = nextRouteNode;
                            nextRouteNode.last = tempRouteNode;
                            // d. 填充当前节点
                            nextRouteNode.edgeNodes.add(currNodeId);
                            String anotherCurrNodeId = getCurrNodeIdFromRouteById(targetNode.next.id, nextRouteNode);
                            String anotherLastNodeId = getLastNodeIdFromRouteById(targetNode.next.frontId, targetNode.next.id, nextRouteNode);
                            Double anotherLastAzimuth = getLastAzimuthFromRouteById(targetNode.next.frontId, targetNode.next.id, nextRouteNode);
                            nextRouteNode.bearing = anotherLastAzimuth;
                            // e. 进行下一分支的搜索
                            // System.out.println("==========101==========;   " + anotherLastNodeId + "   " + anotherLastAzimuth +  "    " + anotherCurrNodeId + ";  " + targetNode.next.frontId);
                            travel(anotherLastNodeId, anotherLastAzimuth, anotherCurrNodeId, nextRouteNode, targetNode.next,false);

                            // 禁忌表
                            if (this.centerNodes.size() != 0) {
                                if (this.centerNodes.contains(targetNode.id)) {
                                    this.tabus.add(currNodeId);
                                }
                            }
                        } else if (resLg.res.equals(12)) {
                            RouteUnit tempRouteNode2 = new RouteUnit(currRouteNode);
                            tempRouteNode2.length += lastEdge.getLength();
                            tempRouteNode2.edgeNodes.add(currNodeId);
                            // b. 沿直线延伸搜索下一节点
                            if (targetNode.last.isFirst) {
                                if (tempRouteNode2.length > Constant.LENGTH_MIN_THRESHOLD && tempRouteNode2.length < Constant.LENGTH_MAX_THRESHOLD) {
                                    travel(currNodeId, lastAzimuth, resLg.neighborNodeId, tempRouteNode2, targetNode, true);
                                } else {
                                    break;
                                }
                            } else {
                                travel(currNodeId, lastAzimuth, resLg.neighborNodeId, tempRouteNode2, targetNode, true);
                            }

                            RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
                            tempRouteNode.length += lastEdge.getLength();
                            tempRouteNode.edgeNodes.add(currNodeId);
                            RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode);
                            tempRouteNode.next = nextRouteNode;
                            nextRouteNode.last = tempRouteNode;
                            // d. 填充当前节点
                            nextRouteNode.edgeNodes.add(currNodeId);
                            // b. 获取下一分支的当前节点、上一节点和上一方位角
                            String anotherCurrNodeId = getCurrNodeIdFromRouteById(targetNode.next.id, nextRouteNode);
                            String anotherLastNodeId = getLastNodeIdFromRouteById(targetNode.next.frontId, targetNode.next.id, nextRouteNode);
                            Double anotherLastAzimuth = getLastAzimuthFromRouteById(targetNode.next.frontId, targetNode.next.id, nextRouteNode);
                            nextRouteNode.bearing = anotherLastAzimuth;

                            // e. 进行下一分支的搜索
                            // System.out.println("==========101==========" + anotherLastNodeId + "   " + anotherLastAzimuth +  "    " + anotherCurrNodeId + ";  " + targetNode.next);
                            travel(anotherLastNodeId, anotherLastAzimuth, anotherCurrNodeId, nextRouteNode, targetNode.next,false);

                            // 禁忌表
                            if (this.centerNodes.size() != 0) {
                                if (this.centerNodes.contains(targetNode.id)) {
                                    this.tabus.add(currNodeId);
                                }
                            }
                        }
                    }
                }
            }
        }

        // // 针对只有一条相邻边的节点(edge: 表示已访问过的连接此节点的边，相当于下边的lastEdge)
        // if (false && edge != null && edge.next == null) { // 没有下一条边了，不存在直线约束的情况
        //     // 没有角度约束条件的叶子节点
        //     if (!targetNode.isAngleConstraint && !targetNode.isFirst) {
        //         // 1 匹配结果链表的深拷贝
        //         RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
        //         // 2 叶子节点 | 输入图形的目标节点全部匹配完成
        //         if (targetNode.next == null) {
        //             // 2.1 输入图形最后一个节点的距离约束(避免终止边过短或过长)
        //             if (isLengthConstraint) {
        //                 Double molecular = tempRouteNode.length + edge.getLength();
        //                 Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, tempRouteNode);
        //                 // 2.1.1 以当前节点作匹配节点时终止边过短，继续搜索待匹配节点
        //                 if (molecular >= denominator * targetNode.minRatio && molecular <= denominator * targetNode.maxRatio) {
        //                     // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
        //                     tempRouteNode.length += edge.getLength();
        //                     tempRouteNode.edgeNodes.add(currNodeId);
        //                     // b. 当前节点的匹配结果
        //                     RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode);
        //                     tempRouteNode.next = nextRouteNode;
        //                     nextRouteNode.last = tempRouteNode;
        //                     // c. 填充当前节点
        //                     nextRouteNode.edgeNodes.add(currNodeId);
        //                     // d. 全局记录输入图形的匹配成功结果
        //                     routeUnitList.add(nextRouteNode);
        //
        //                     // 禁忌表
        //                     if (this.centerNodes.size() != 0) {
        //                         if (this.centerNodes.contains(targetNode.id)) {
        //                             this.tabus.add(currNodeId);
        //                         }
        //                     }
        //                 }
        //             }
        //             // 2.2 目标节点全部匹配完成时的操作
        //             else {
        //                 // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
        //                 tempRouteNode.length += edge.getLength();
        //                 tempRouteNode.edgeNodes.add(currNodeId);
        //                 // b. 当前节点的匹配结果
        //                 RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode);
        //                 tempRouteNode.next = nextRouteNode;
        //                 nextRouteNode.last = tempRouteNode;
        //                 // c. 填充当前节点
        //                 nextRouteNode.edgeNodes.add(currNodeId);
        //                 // d. 全局记录输入图形的匹配成功结果
        //                 routeUnitList.add(nextRouteNode);
        //
        //                 // 禁忌表
        //                 if (this.centerNodes.size() != 0) {
        //                     if (this.centerNodes.contains(targetNode.id)) {
        //                         this.tabus.add(currNodeId);
        //                     }
        //                 }
        //             }
        //         }
        //         // 3 叶子节点分支匹配完成，进行下一分支的匹配
        //         else {
        //             // 3.1 分支叶子节点之间的距离约束(避免终止边过短或过长)
        //             if (isLengthConstraint) {
        //                 // if (targetNode.last.isAngleConstraint)
        //                 // System.out.println(targetNode);
        //                 Double molecular = tempRouteNode.length + edge.getLength();
        //                 Double denominator = getLengthFromRouteByNodes(targetNode.contrastLEId, targetNode.contrastLSId, tempRouteNode);
        //                 // 3.1.1 以当前节点作匹配节点时终止边过短，继续搜索待匹配节点
        //                 if (molecular >= denominator * targetNode.minRatio && molecular <= denominator * targetNode.maxRatio) {
        //                     // System.out.println("Result:  " + lastNodeId + " , " + currNodeId + " , " + molecular + ";   " + denominator + "         ====       " + targetNode.id);
        //                     // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
        //                     tempRouteNode.length += edge.getLength();
        //                     tempRouteNode.edgeNodes.add(currNodeId);
        //                     // b. 获取下一分支的当前节点、上一节点和上一方位角
        //                     String anotherCurrNodeId = getCurrNodeIdFromRouteById(targetNode.next.id, tempRouteNode);
        //                     String anotherLastNodeId = getLastNodeIdFromRouteById(targetNode.next.frontId, targetNode.next.id, tempRouteNode);
        //                     Double anotherLastAzimuth = getLastAzimuthFromRouteById(targetNode.next.frontId, targetNode.next.id, tempRouteNode);
        //                     // c. 当前节点的匹配结果
        //                     RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode, anotherLastAzimuth);
        //                     tempRouteNode.next = nextRouteNode;
        //                     nextRouteNode.last = tempRouteNode;
        //                     // d. 填充当前节点
        //                     nextRouteNode.edgeNodes.add(currNodeId);
        //                     // e. 进行下一分支的搜索
        //                     // System.out.println("201:   " + anotherLastNodeId + ",  " + anotherCurrNodeId + ", " + anotherLastAzimuth + ",  " + tempRouteNode.bearing);
        //                     travel(anotherLastNodeId, anotherLastAzimuth, anotherCurrNodeId, nextRouteNode, targetNode.next,false);
        //
        //                     // 禁忌表
        //                     if (this.centerNodes.size() != 0) {
        //                         if (this.centerNodes.contains(targetNode.id)) {
        //                             this.tabus.add(currNodeId);
        //                         }
        //                     }
        //                     return;
        //                 }
        //             }
        //             // 3.2 叶子节点结束分支并跳转到另一分支起始节点
        //             else {
        //                 // a. 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
        //                 tempRouteNode.length += edge.getLength();
        //                 tempRouteNode.edgeNodes.add(currNodeId);
        //
        //                 // c. 当前节点的匹配结果
        //                 RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode);
        //                 tempRouteNode.next = nextRouteNode;
        //                 nextRouteNode.last = tempRouteNode;
        //                 // d. 填充当前节点
        //                 nextRouteNode.edgeNodes.add(currNodeId);
        //                 String anotherCurrNodeId = getCurrNodeIdFromRouteById(targetNode.next.id, nextRouteNode);
        //                 String anotherLastNodeId = getLastNodeIdFromRouteById(targetNode.next.frontId, targetNode.next.id, nextRouteNode);
        //                 Double anotherLastAzimuth = getLastAzimuthFromRouteById(targetNode.next.frontId, targetNode.next.id, nextRouteNode);
        //                 nextRouteNode.bearing = anotherLastAzimuth;
        //                 // System.out.println("202:   " + anotherLastNodeId + ",  " + anotherCurrNodeId + ", " + anotherLastAzimuth + ",  " + tempRouteNode.bearing);
        //                 travel(anotherLastNodeId, anotherLastAzimuth, anotherCurrNodeId, nextRouteNode, targetNode.next, false);
        //                 // 禁忌表
        //                 if (this.centerNodes.size() != 0) {
        //                     if (this.centerNodes.contains(targetNode.id)) {
        //                         this.tabus.add(currNodeId);
        //                     }
        //                 }
        //                 return;
        //             }
        //         }
        //     }
        // }

        // 循环遍历当前节点的相邻节点
        while (edge != null) {
            String neighborNodeId = edge.getTarget();
            // System.out.println("000000000000000000000     " + lastNodeId + "  " + currNodeId + "   " + neighborNodeId);
            /** 出边和入边相同时，跳出 **/
            if (neighborNodeId.equals(lastNodeId)) {
                /** 同一节点相邻边的循环 **/
                edge = edge.next;
                continue;
            }

            // 1. 没有角度约束条件的起点和叶子节点
            if (!targetNode.isAngleConstraint) {
                if (targetNode.isFirst) {
                    Double currAzimuth = edge.getBearing();
                    currRouteNode = new RouteUnit(currNodeId, targetNode, currAzimuth);
                    currRouteNode.edgeNodes.add(currNodeId);
                    travel(currNodeId, currAzimuth, neighborNodeId, currRouteNode, targetNode.next, true);
                }
            }
            // 2. 有角度约束条件的非起点和叶子节点的操作
            else {
                Graph.Edge lastEdge = graph.getEdge(lastNodeId, currNodeId);
                // // 2.1 当前节点是否匹配目标节点的评价结果
                // System.out.println("666666666666666666666666:  " + lastNodeId + " , " + currNodeId + " , " + lastAzimuth + "  ,  " + neighborNodeId + ";     " + targetNode.id);
                Integer meritResult = meritFunc(lastNodeId, lastAzimuth, currNodeId, neighborNodeId, vertexNum, currRouteNode, targetNode, isLineExtend);
                // System.out.println("Result:  " + lastNodeId + " , " + currNodeId + " , " + lastAzimuth + "  ,  " + neighborNodeId + ";   " + meritResult + "        ====       " + targetNode.id);
                // 2.2 当前节点不满足约束条件，退出
                if (meritResult.equals(0)) {
                    // System.out.println("====================" + "  Exit!" + "   " + targetNode.id + "  " + targetNode.frontId + "    " + targetNode.backId);
                    /** 同一节点相邻边的循环 **/
                    edge = edge.next;
                    continue;
                }
                // 2.3 当前节点满足直线延伸的约束条件，继续搜索
                else if (meritResult.equals(-1)) {
                    // System.out.println("====================" + "  Line extend!" + "  " + lastAzimuth + "   " +  lastNodeId + "   " + currNodeId + "  " + neighborNodeId + "  " + targetNode);
                    // 2.3.1 匹配结果链表的深拷贝
                    RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
                    // 2.3.2 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
                    tempRouteNode.length += lastEdge.getLength();
                    tempRouteNode.edgeNodes.add(currNodeId);
                    // System.out.println("The length is:   " + tempRouteNode.length + "   " + lastEdge.getLength() + "   " + lastEdge.getBearing());
                    // 2.3.3 沿直线延伸搜索
                    if (targetNode.last.isFirst) {
                        // if (tempRouteNode.length > Constant.LENGTH_MIN_THRESHOLD && tempRouteNode.length < Constant.LENGTH_MAX_THRESHOLD) {
                        if (tempRouteNode.length < Constant.LENGTH_MAX_THRESHOLD) {
                            // System.out.println("targetNode.last.isFirst:      " + lastNodeId + "; " + currNodeId + ";  " + neighborNodeId + "; " + lastAzimuth + ";           " + targetNode.id);
                            travel(currNodeId, lastAzimuth, neighborNodeId, tempRouteNode, targetNode, true);
                        } else {
                            break;
                        }
                    } else {
                        travel(currNodeId, lastAzimuth, neighborNodeId, tempRouteNode, targetNode, true);
                    }
                }
                // 2.3 当前节点满足角度延伸的约束条件，继续搜索下一待匹配节点
                else if (meritResult.equals(1)) {
                    // 2.3.1 匹配结果链表的深拷贝
                    RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
                    // a. 上一目标节点不是叶子节点(除起始节点外)和重复节点时，将当前节点添加至edgeNodes尾部，构成边的序列点集
                    if ((targetNode.last.isAngleConstraint) || targetNode.last.isFirst) {
                        tempRouteNode.length += lastEdge.getLength();
                        tempRouteNode.edgeNodes.add(currNodeId);
                    }
                    // b. 当前节点和下一节点的方位角
                    Double currAzimuth = getBearing(lastAzimuth, targetNode.angle);
                    // c. 当前节点的匹配结果
                    RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode, currAzimuth);
                    tempRouteNode.next = nextRouteNode;
                    nextRouteNode.last = tempRouteNode;
                    // d. 填充当前节点
                    nextRouteNode.edgeNodes.add(currNodeId);
                    //e. 起始边距离约束
                    if (targetNode.last.isFirst) {
                        if (tempRouteNode.length > Constant.LENGTH_MIN_THRESHOLD && tempRouteNode.length < Constant.LENGTH_MAX_THRESHOLD) {
                            // 进行下一待匹配节点的搜索
                            travel(currNodeId, currAzimuth, neighborNodeId, nextRouteNode, targetNode.next, true);
                            // 禁忌表
                            if (this.centerNodes.size() != 0) {
                                if (this.centerNodes.contains(targetNode.id)) {
                                    this.tabus.add(currNodeId);
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    // f. 非起始边的操作
                    else {
                        // System.out.println("==========1==========" + currNodeId + "    " + neighborNodeId);
                        // 进行下一待匹配节点的搜索
                        travel(currNodeId, currAzimuth, neighborNodeId, nextRouteNode, targetNode.next, true);
                        // 禁忌表
                        if (this.centerNodes.size() != 0) {
                            // System.out.println("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff: " + this.centerNodes.contains(targetNode.id) + ";   " + targetNode.id);
                            if (this.centerNodes.contains(targetNode.id)) {
                                this.tabus.add(currNodeId);
                            }
                        }
                    }
                }
                else if (meritResult.equals(11)) {
                    // 第一种情况
                    RouteUnit tempRouteNode = new RouteUnit(currRouteNode);
                    if ((targetNode.last.isAngleConstraint) || targetNode.last.isFirst) {
                        tempRouteNode.length += lastEdge.getLength();
                        tempRouteNode.edgeNodes.add(currNodeId);
                    }
                    // b. 当前节点和下一节点的方位角
                    Double currAzimuth = getBearing(lastAzimuth, targetNode.angle);
                    // c. 当前节点的匹配结果
                    RouteUnit nextRouteNode = new RouteUnit(currNodeId, targetNode, currAzimuth);
                    tempRouteNode.next = nextRouteNode;
                    nextRouteNode.last = tempRouteNode;
                    // d. 填充当前节点
                    nextRouteNode.edgeNodes.add(currNodeId);
                    //e. 起始边距离约束
                    if (targetNode.last.isFirst) {
                        if (tempRouteNode.length > Constant.LENGTH_MIN_THRESHOLD && tempRouteNode.length < Constant.LENGTH_MAX_THRESHOLD) {
                            // 进行下一待匹配节点的搜索
                            travel(currNodeId, currAzimuth, neighborNodeId, nextRouteNode, targetNode.next, true);
                            // 禁忌表
                            if (this.centerNodes.size() != 0) {
                                if (this.centerNodes.contains(targetNode.id)) {
                                    this.tabus.add(currNodeId);
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    // f. 非起始边的操作
                    else {
                        // System.out.println("==========1==========" + currNodeId + "    " + neighborNodeId);
                        // 进行下一待匹配节点的搜索
                        travel(currNodeId, currAzimuth, neighborNodeId, nextRouteNode, targetNode.next, true);
                        // 禁忌表
                        if (this.centerNodes.size() != 0) {
                            if (this.centerNodes.contains(targetNode.id)) {
                                this.tabus.add(currNodeId);
                            }
                        }
                    }


                    // 第二种情况
                    // 2.3.1 匹配结果链表的深拷贝
                    RouteUnit tempRouteNode2 = new RouteUnit(currRouteNode);
                    // 2.3.2 完善上一节点的匹配结果(上一节点和当前节点之间的距离和节点集)
                    tempRouteNode2.length += lastEdge.getLength();
                    tempRouteNode2.edgeNodes.add(currNodeId);
                    // System.out.println("The length is:   " + tempRouteNode.length + "   " + lastEdge.getLength() + "   " + lastEdge.getBearing());
                    // 2.3.3 沿直线延伸搜索
                    if (targetNode.last.isFirst) {
                        // if (tempRouteNode2.length > Constant.LENGTH_MIN_THRESHOLD && tempRouteNode.length < Constant.LENGTH_MAX_THRESHOLD) {
                        if (tempRouteNode.length < Constant.LENGTH_MAX_THRESHOLD) {
                            travel(currNodeId, lastAzimuth, neighborNodeId, tempRouteNode2, targetNode, true);
                        } else {
                            break;
                        }
                    } else {
                        travel(currNodeId, lastAzimuth, neighborNodeId, tempRouteNode2, targetNode, true);
                    }
                }
            }

            /** 同一节点相邻边的循环 **/
            edge = edge.next;
        }
    }

    /**
     * 针对输入图形模板化数据在无向图中进行自适应匹配
     * @param startTargetNode
     */
    public void startSearch(DataUnit startTargetNode) {
        Set<String> nodes = graph.getVertexs();
        // System.out.println("Nodes size is: " + nodes.size());
        for (String node : nodes) {
            // System.out.println("========================   " + this.tabus.size());
            if (this.tabus.contains(node)) {
                this.tabus.remove(node);
                continue;
            }
            travel("", 0.0, node, null, startTargetNode, true);
        }
    }

    /**
     * 针对单个起始结点进行路径搜索
     * @param startNode
     * @param startTargetNode
     */
    public void startSearchByNode(String startNode, DataUnit startTargetNode) {
        travel("", 0.0, startNode, null, startTargetNode, true);
    }

    /**
     * 获取输入图形的匹配结果
     * @return
     */
    public List<RouteUnit> getRouteUnitList() {
        return this.routeUnitList;
    }

    /**
     * 打印单个搜索结果
     * @param routeUnit
     */
    public static void printRouteUnit(RouteUnit routeUnit) {
        while (routeUnit != null) {
            System.out.println(routeUnit);
            routeUnit = routeUnit.last;
        }
    }

    /**
     * 搜索匹配结果的可视化编码
     * @return
     */
    public HashMap parseRouteUnit(RouteUnit routeUnit) {
        Double totalLength = 0.0;
        List<List<List<Double>>> multiLines = new ArrayList<>();
        while (routeUnit != null) {
            if (routeUnit.edgeNodes.size() > 1) {
                List<List<Double>> edgeLine = new ArrayList<>();
                for (String edgeNode : routeUnit.edgeNodes) {
                    edgeLine.add(graph.getVertex(edgeNode).getXY());
                }
                multiLines.add(edgeLine);
            }
            totalLength += routeUnit.length;
            routeUnit = routeUnit.last;
        }
        HashMap hashMap = new HashMap();
        hashMap.put("TYPE", "MultiLineString");
        hashMap.put("LENGTH", totalLength);
        hashMap.put("COORDINATE", multiLines);
        return hashMap;
    }

    public void parseRouteUnitList(List<RouteUnit> routeUnits, String ouputFilePath) {
        List<HashMap> hashMaps = new ArrayList<>();
        for (RouteUnit routeUnit : routeUnits) {
            hashMaps.add(parseRouteUnit(routeUnit));
        }

        // 写入json文件
        BufferedWriter outFileWriter = null;
        try {
            File outfileLine = new File(ouputFilePath);
            outFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfileLine), "UTF-8"));
            outFileWriter.write(JSON.toJSONString(hashMaps));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outFileWriter != null) {
                try {
                    outFileWriter.flush();
                    outFileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public HashMap parseRouteUnitInTxt(String target, RouteUnit routeUnit) {
        Double totalLength = 0.0;
        String str = "[";
        Double bearing = Double.NaN;
        while (routeUnit != null) {
            if (routeUnit.edgeNodes.size() > 1) {
                String edgeStr = "[";
                for (String edgeNode : routeUnit.edgeNodes) {
                    edgeStr += edgeNode + ",";
                }
                edgeStr = edgeStr.substring(0, edgeStr.length() - 1) + "]";
                str += edgeStr + ",";
            }
            // System.out.println(routeUnit.length);
            totalLength += routeUnit.length;
            if (target.equals("y")) {
                if (routeUnit.targetNode.id.equals("2")) bearing = reverseAzimuth(routeUnit.bearing);
            } else if (target.equals("heart")) {
                if (routeUnit.targetNode.id.equals("1")) bearing = routeUnit.bearing;
            } else if (target.equals("I")) {
                if (routeUnit.targetNode.id.equals("1")) bearing = offsetAzimuth(routeUnit.bearing, -90.0);
            } else if (target.equals("3")) {
                if (routeUnit.targetNode.id.equals("2")) bearing = reverseAzimuth(routeUnit.bearing);
            } else if (target.equals("4")) {
                if (routeUnit.targetNode.id.equals("2")) bearing = offsetAzimuth(routeUnit.bearing, 90.0);
            } else if (target.equals("5")) {
                if (routeUnit.targetNode.id.equals("2")) bearing = routeUnit.bearing;
            } else if (target.equals("2")) {
                if (routeUnit.targetNode.id.equals("4")) bearing = routeUnit.bearing;
            } else if (target.equals("L")) {
                if (routeUnit.targetNode.id.equals("1") && routeUnit.targetNode.frontId.equals("")) bearing = routeUnit.bearing;
            } else if (target.equals("O")) {
                if (routeUnit.targetNode.id.equals("1") && routeUnit.targetNode.frontId.equals("")) bearing = routeUnit.bearing;
            } else if (target.equals("U")) {
                if (routeUnit.targetNode.id.equals("1") && routeUnit.targetNode.frontId.equals("")) bearing = routeUnit.bearing;
            } else if (target.equals("E")) {
                if (routeUnit.targetNode.id.equals("2")) bearing = routeUnit.bearing;
            } else {
                if (routeUnit.targetNode.id.equals("1")) bearing = routeUnit.bearing;
            }
            // if (routeUnit.targetNode.id.equals("2")) bearing = reverseAzimuth(routeUnit.bearing); // y
            // if (routeUnit.targetNode.id.equals("1")) bearing = routeUnit.bearing; // heart
            // if (routeUnit.targetNode.id.equals("1")) bearing = offsetAzimuth(routeUnit.bearing, 45.0); // heart
            // if (routeUnit.targetNode.id.equals("1")) bearing = offsetAzimuth(routeUnit.bearing, -90.0); // I
            // if (routeUnit.targetNode.id.equals("2")) bearing = reverseAzimuth(routeUnit.bearing); // 3
            // if (routeUnit.targetNode.id.equals("2")) bearing = offsetAzimuth(routeUnit.bearing, 90.0); // 4
            // if (routeUnit.targetNode.id.equals("2")) bearing = routeUnit.bearing; // 5
            // if (routeUnit.targetNode.id.equals("4")) bearing = routeUnit.bearing; // 2
            // if (routeUnit.targetNode.id.equals("1") && routeUnit.targetNode.frontId.equals("")) bearing = routeUnit.bearing; // L
            // if (routeUnit.targetNode.id.equals("1") && routeUnit.targetNode.frontId.equals("")) bearing = routeUnit.bearing; // O
            // if (routeUnit.targetNode.id.equals("1") && routeUnit.targetNode.frontId.equals("")) bearing = routeUnit.bearing; // U
            // if (routeUnit.targetNode.id.equals("2")) bearing = routeUnit.bearing; // E

            routeUnit = routeUnit.last;
        }

        str = str.substring(0, str.length() - 1) + "," + bearing + "," + totalLength + "]";
        HashMap hashMap = new HashMap();
        hashMap.put("LENGTH", totalLength);
        hashMap.put("COORDINATE", str);
        return hashMap;
    }

    // public void parseRouteUnitListInTxt(List<RouteUnit> routeUnits, String ouputFilePath) {
    //     List<Double> lengthList = new ArrayList<>();
    //     List<String> stringList = new ArrayList<>();
    //     String output = "";
    //     Integer i = 0;
    //     for (RouteUnit routeUnit : routeUnits) {
    //         // if (i == 1) break;
    //         // i++;
    //         HashMap hashMap = parseRouteUnitInTxt(routeUnit);
    //         lengthList.add((Double) hashMap.get("LENGTH"));
    //         String temp = (String) hashMap.get("COORDINATE");
    //         stringList.add(temp);
    //         output += temp + "\n\r";
    //     }
    //
    //     // 写入json文件
    //     BufferedWriter outFileWriter = null;
    //     try {
    //         File outfileLine = new File(ouputFilePath);
    //         outFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfileLine), "UTF-8"));
    //         outFileWriter.write(output);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     } finally {
    //         if (outFileWriter != null) {
    //             try {
    //                 outFileWriter.flush();
    //                 outFileWriter.close();
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    // }

    public void parseRouteUnitListInTxtThread(String target, List<RouteUnit> routeUnits, String ouputFilePath) {
        int stepSize = 80;
        int stepNum = (int) Math.ceil(routeUnits.size() * 1.0 / stepSize);
        CountDownLatch latch = new CountDownLatch(stepNum);
        List<String> fileStrs = new ArrayList<>();
        for (Integer k = 0; k < stepNum; k++) {
            Integer startIndex = k * stepSize, endIndex = (k + 1) * stepSize;
            if (endIndex > routeUnits.size()) endIndex = (int) routeUnits.size();
            ParseThread thread = new ParseThread(target, startIndex, endIndex, fileStrs, routeUnits.subList(startIndex, endIndex), latch);

            thread.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String str = "";
        for (String s : fileStrs) {
            if (s != null && !s.isEmpty())
                str += s;
        }

        // 写入json文件
        BufferedWriter outFileWriter = null;
        try {
            File outfileLine = new File(ouputFilePath);
            outFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfileLine), "UTF-8"));
            outFileWriter.write(str);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outFileWriter != null) {
                try {
                    outFileWriter.flush();
                    outFileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ParseThread extends Thread {
        private int startIndex, endIndex;
        private List<RouteUnit> routeUnits;
        private List<String> result;
        private CountDownLatch latch;
        private String target;

        public ParseThread(String target, Integer startIndex, Integer endIndex, List<String> result, List<RouteUnit> routeUnits, CountDownLatch latch) {
            this.result = result;
            this.routeUnits = routeUnits;
            this.latch = latch;
            this.target = target;
        }

        @Override
        public void run() {
            for (RouteUnit routeUnit : routeUnits) {
                HashMap hashMap = parseRouteUnitInTxt(this.target, routeUnit);
                String temp = (String) hashMap.get("COORDINATE");
                result.add(temp + "\r\n");
            }
            this.latch.countDown();
        }
    }

     /**
     * 旋转点
     *
     * @param point 被旋转的点
     * @param center 旋转中心
     * @param angle 角度
     * @return 旋转后坐标
     */
    public static Coordinate get(Coordinate point, Coordinate center, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = point.x;
        double y = point.y;
        double centerX = center.x;
        double centerY = center.y;
        return new Coordinate(centerX + cos * (x - centerX) - sin * (y - centerY),
                centerY + sin * (x - centerX) + cos * (y - centerY));
    }

    public static Coordinate[] get(Coordinate[] coord, Coordinate center, double angle) {
        Coordinate[] newCoord = new Coordinate[coord.length];
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double xc = center.x, yc = center.y;
        Coordinate ci;
        double x, y;
        for (int i = 0; i < coord.length; i++) {
            ci = coord[i];
            x = ci.x;
            y = ci.y;
            newCoord[i] = new Coordinate(xc + cos * (x - xc) - sin * (y - yc),
                    yc + sin * (x - xc) + cos * (y - yc));
        }
        return newCoord;
    }

    public static LinearRing get(LinearRing linearRing, Coordinate center, double angle,
                                 GeometryFactory gf) {
        return gf.createLinearRing(get(linearRing.getCoordinates(), center, angle));
    }

    public static Polygon rotationPolygon(Polygon geom, Coordinate center, double angle, GeometryFactory gf) {
        LinearRing linearRing = get((LinearRing) geom.getExteriorRing(), center, angle, gf);
        LinearRing[] linearRings = new LinearRing[geom.getNumInteriorRing()];
        for (int j = 0; j < geom.getNumInteriorRing(); j++) {
            linearRings[j] = get((LinearRing) geom.getInteriorRingN(j), center, angle, gf);
        }
        return gf.createPolygon(linearRing, linearRings);
    }

    public static Polygon get(Geometry geom, GeometryFactory gf) {
        Geometry hull = (new ConvexHull(geom)).getConvexHull();
        if (!(hull instanceof Polygon)) {
            return null;
        }
        Polygon convexHull = (Polygon) hull;
        System.out.println(convexHull);

        // 直接使用中心值
        Coordinate c = geom.getCentroid().getCoordinate();
        System.out.println("==============旋转基点==============");
        System.out.println(new GeometryFactory().createPoint(c));
        System.out.println("==============旋转基点==============");
        Coordinate[] coords = convexHull.getExteriorRing().getCoordinates();

        double minArea = Double.MAX_VALUE;
        double minAngle = 0;
        Polygon ssr = null;
        Coordinate ci = coords[0];
        Coordinate cii;
        for (int i = 0; i < coords.length - 1; i++) {
            cii = coords[i + 1];
            double angle = Math.atan2(cii.y - ci.y, cii.x - ci.x);
            Polygon rect = (Polygon) rotationPolygon(convexHull, c, -1 * angle, gf).getEnvelope();
            double area = rect.getArea();
//            此处可以将 rotationPolygon 放到list中求最小值
//            Polygon rotationPolygon = Rotation.get(rect, c, angle, gf);
//            System.out.println(rotationPolygon);
            if (area < minArea) {
                minArea = area;
                ssr = rect;
                minAngle = angle;
            }
            ci = cii;
        }

        return rotationPolygon(ssr, c, minAngle, gf);
    }

    // public void filterRepeatResult(List<RouteUnit> routeUnits) {
    //     for (RouteUnit routeUnit0 : routeUnits) {
    //         for (RouteUnit routeUnit1 : routeUnits) {
    //
    //         }
    //     }
    // }
}