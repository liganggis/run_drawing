/**
 * Copyright (C), 2018-2022, 武汉大学
 * FileName: Constant
 * Author:   天行健
 * Date:     2022/12/4 14:37
 * Description: 全局静态变量
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package algorithm;

/**
 * 〈一句话功能简述〉<br> 
 * 〈全局静态变量〉
 *
 * @author 天行健
 * @create 2022/12/4
 * @since 1.0.0
 */
public class Constant {
    /** 起始边长度最小阈值 **/
    public final static Double LENGTH_MIN_THRESHOLD = 300.0;
    /** 起始边长度最大阈值 **/
    public final static Double LENGTH_MAX_THRESHOLD = 600.0;
    /** 相邻边夹角(左角)的比较阈值 **/
    public final static Double ANGLE_THRESHOLD = 15.0;
    /** 搜索方向沿直线延伸的角度阈值 **/
    public final static Double LINE_THRESHOLD = 15.0;
    /** 是否在搜索算法过程中包含距离约束 **/
    public final static boolean ISLENGTHCONSTRAINT = true;
    /** 是否在搜索算法过程中包含直线延伸约束 **/
    public final static boolean ISLINEEXTEND = true;
    /** 长度比约束阈值 **/
    public final static double RATIO = 0.3;
}