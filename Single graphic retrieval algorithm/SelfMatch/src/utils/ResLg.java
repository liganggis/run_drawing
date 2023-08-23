/**
 * Copyright (C), 2018-2023, 武汉大学
 * FileName: ResLg
 * Author:   天行健
 * Date:     2023/5/18 15:21
 * Description: 广义搜索结果单元
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package utils;

/**
 * 〈一句话功能简述〉<br> 
 * 〈广义搜索结果单元〉
 *
 * @author 天行健
 * @create 2023/5/18
 * @since 1.0.0
 */
public class ResLg {

    public String currNodeId;
    public String neighborNodeId;
    public Integer res;

    public ResLg(String currNodeId, String neighborNodeId, Integer res) {
        this.currNodeId = currNodeId;
        this.neighborNodeId = neighborNodeId;
        this.res = res;
    }

    public ResLg(String currNodeId, Integer res) {
        this.currNodeId = currNodeId;
        this.neighborNodeId = "";
        this.res = res;
    }

    public ResLg(Integer res) {
        this.currNodeId = "";
        this.neighborNodeId = "";
        this.res = res;
    }
}