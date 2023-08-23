/**
 * Copyright (C), 2018-2022, 武汉大学
 * FileName: DataUnit
 * Author:   天行健
 * Date:     2022/12/4 12:41
 * Description: 输入图形的数据单元
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈输入图形的数据单元〉
 *
 * @author 天行健
 * @create 2022/12/4
 * @since 1.0.0
 */
public class DataUnit {
    /** 输入图形的节点ID **/
    public String id = "";
    /** 当前节点的前一节点 **/
    public String frontId = "";
    /** 当前节点的后一节点 **/
    public String backId = "";
    /** 前一节点、当前节点和后一节点构成的左角 **/
    public Double angle = Double.NaN;
    /** 是否考虑角度约束 **/
    public Boolean isAngleConstraint = false;
    /** 当前节点是否为闭合的节点（与已有节点会重合） **/
    public Boolean isRepeat = false;
    /** 是否为输入图形的第一个节点 **/
    public Boolean isFirst = false;
    /** 节点的度 **/
    public Integer degree = 0;
    /** 进行长度对比的边的起始节点(contrast length start id) **/
    public String contrastLSId = "";
    /** 进行长度对比的边的终止节点(contrast length start id) **/
    public String contrastLEId = "";
    /** 当前节点和前一节点之间的长度相对进行对比的边的最小允许比例 **/
    public Double minRatio = 0.0;
    /** 当前节点和前一节点之间的长度相对进行对比的边的最大允许比例 **/
    public Double maxRatio = 0.0;
    /** 是否考虑内部形状约束(长度约束) **/
    public Boolean isLengthConstraint = false;
    /** 是否中心对称、旋转对称 **/
    public Boolean center = false;
    /** 起点对应不变点 **/
    public List<String> centerNodes = new ArrayList<>();

    public DataUnit next = null;
    public DataUnit last = null;

    public DataUnit() {}

    public DataUnit(String id,
                    String frontId,
                    String backId,
                    Boolean isFirst,
                    Boolean isRepeat,
                    Boolean isAngleConstraint,
                    Double angle,
                    Boolean isLengthConstraint,
                    String contrastLSId,
                    String contrastLEId,
                    Double minRatio,
                    Double maxRatio,
                    Boolean center,
                    List<String> centerNodes) {
        this.id = id;
        this.frontId = frontId;
        this.backId = backId;
        this.isFirst = isFirst;
        this.isRepeat = isRepeat;
        this.isAngleConstraint = isAngleConstraint;
        this.angle = angle;
        this.isLengthConstraint = isLengthConstraint;
        this.contrastLSId = contrastLSId;
        this.contrastLEId = contrastLEId;
        this.minRatio = minRatio;
        this.maxRatio = maxRatio;
        this.center = center;
        this.centerNodes = centerNodes;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setFrontId(String frontId) {
        this.frontId = frontId;
    }
    public void setBackId(String backId) {
        this.backId = backId;
    }
    public void setIsAngleConstraint(Boolean angleConstraint) {
        isAngleConstraint = angleConstraint;
    }
    public void setAngle(Double angle) {
        this.angle = angle;
    }
    public void setIsFirst(Boolean first) {
        isFirst = first;
    }
    public void setIsRepeat(Boolean repeat) {
        isRepeat = repeat;
    }
    public void setIsLengthConstraint(Boolean lengthConstraint) {
        isLengthConstraint = lengthConstraint;
    }
    public void setContrastLSId(String contrastLSId) {
        this.contrastLSId = contrastLSId;
    }
    public void setContrastLEId(String contrastLEId) {
        this.contrastLEId = contrastLEId;
    }
    public void setMinRatio(Double minRatio) {
        this.minRatio = minRatio;
    }
    public void setMaxRatio(Double maxRatio) {
        this.maxRatio = maxRatio;
    }

    @Override
    public String toString() {
        return "DataUnit [id=" + id + ", frontId=" + frontId + "" +
                ", backId=" + backId + ", isFirst=" + isFirst + ", isRepeat=" + isRepeat + "" +
                ", isAngleConstraint=" + isAngleConstraint + "" +
                ", angle=" + angle +
                ", degree=" + degree +
                ", isLengthConstraint=" + isLengthConstraint + "" +
                ", contrastLSId=" + contrastLSId + "" +
                ", contrastLEId=" + contrastLEId + "" +
                ", center=" + center + "" +
                ", minRatio=" + minRatio + "" +
                ", maxRatio=" + maxRatio + "]";
    }
}