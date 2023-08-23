import re
from timeit import repeat
import osmnx as ox
import networkx as nx
import matplotlib.pyplot as plt
import copy
import data

# 统一左角|基本测试|0-9|汉字“田”|不一个个计算夹角|距离约束

def getAzimuth(bearing, contiLine):
    if contiLine:
        return bearing
    else:
        if (bearing > 180):
            return bearing - 180
        else:
            return bearing + 180

# 根据线段顺序节点获取方位角
def getAzimuthFromNodes(G, lastNode, currNode):
    azimuth = None
    if G.has_edge(lastNode, currNode):
        temp = G.adj[lastNode][currNode]
        azimuth = getAzimuth(temp[0]['bearing'], True)
    else:
        temp = G.adj[currNode][lastNode]
        azimuth = getAzimuth(temp[0]['bearing'], False)
    return azimuth

# 线段方位角取反
def turnAzimuth(azimuth):
    if (azimuth > 180):
        return azimuth - 180
    else:
        return azimuth + 180

# 线段方位角偏移角度angle
def offsetAzimuth(azimuth, angle):
    result = azimuth + angle
    if result < 0:
        result += 360
    elif result > 360:
        result -= 360
    return result

# 获取线段长度
def getLengthFromNodes(G, lastNode, currNode):
    if G.has_edge(lastNode, currNode):
        temp = G.adj[lastNode][currNode]
        return temp[0]['length']
    else:
        return temp[0]['length']

def getLengthFromRouteByNodes(lastId, currId, route):
    lastNode = getCurrOsmidFromRouteById(route, lastId)
    currNode = getCurrOsmidFromRouteById(route, currId)
    for r in route:
        edgeNodes = r['edgeNodes']
        if (edgeNodes[0] == currNode) and (edgeNodes[-1] == lastNode):
            return r['length']
        elif (edgeNodes[0] == lastNode) and (edgeNodes[-1] == currNode):
            return r['length']
        else:
            continue
    return None

def polylineCostFunc(G, lastAzimuth, currNode, neighborNode, targets, targetsId, route, lineExtension=True):
    neighborAzimuth = None
    # 判断相邻边方向是否相同，并获取边属性
    if G.has_edge(currNode, neighborNode):
        temp = G.adj[currNode][neighborNode]
        neighborAzimuth = getAzimuth(temp[0]['bearing'], True)
    else:
        temp = G.adj[neighborNode][currNode]
        neighborAzimuth = getAzimuth(temp[0]['bearing'], False)
    # 夹角(左角)
    angle = neighborAzimuth - lastAzimuth + 180
    if angle < 0:
        angle += 360
    elif angle > 360:
        angle -= 360

    
    # 满足方向约束
    if abs(angle - targets[targetsId]['angle'][0][3]) < data.AngleThreshold:
        # 判断相同节点的搜索结果是否相同
        if not targets[targetsId]['repeat']:
            # 考虑形状内部线段间距离约束
            if 'targetLength' in targets[targetsId].keys():
                molecular = getLengthFromNodes(G, route[targetsId - 1]['edgeNodes'][-1], currNode)
                molecular += route[targetsId - 1]['length']
                denominator = getLengthFromRouteByNodes(targets[targetsId]['targetLength'][2], targets[targetsId]['targetLength'][3], route)
                minRatio = targets[targetsId]['targetLength'][4]
                maxRatio = targets[targetsId]['targetLength'][5]
                if (molecular > (denominator * minRatio)) and (molecular < (denominator * maxRatio)):
                    return 1
                else:
                    return 0
            else:
                return 1
            # return 1
        else:
            # 闭合环不必考虑形状内部线段间距离
            repeatId = getCurrOsmidFromRouteById(route, targets[targetsId]['targetsId'])
            if currNode == repeatId:
                return 1
            else:
                return 0
    # 满足线段沿直线延伸的约束
    elif lineExtension and abs(angle - 180) < data.LineThreshold:
        if 'targetLength' in targets[targetsId].keys():
            molecular = getLengthFromNodes(G, route[targetsId - 1]['edgeNodes'][-1], currNode)
            neighborLength = getLengthFromNodes(G, currNode, neighborNode)
            molecular = route[targetsId - 1]['length'] + molecular + neighborLength
            denominator = getLengthFromRouteByNodes(targets[targetsId]['targetLength'][2], targets[targetsId]['targetLength'][3], route)
            minRatio = targets[targetsId]['targetLength'][4]
            maxRatio = targets[targetsId]['targetLength'][5]
            # print(denominator)
            # print(maxRatio)
            # print(molecular, denominator, (denominator * maxRatio), maxRatio)
            if molecular < (denominator * maxRatio):
                return -1
            else:
                return 0
        else:
            return -1
        # return -1
    else:
        return 0
    
def polylineCostFuncForEndPoint(G, lastAzimuth, currNode, neighborNode, targets, targetsId, targetAngle, route, lineExtension=True):
    
    neighborAzimuth = None
    # 判断相邻边方向是否相同，并获取边属性
    if G.has_edge(currNode, neighborNode):
        temp = G.adj[currNode][neighborNode]
        neighborAzimuth = getAzimuth(temp[0]['bearing'], True)
    else:
        temp = G.adj[neighborNode][currNode]
        neighborAzimuth = getAzimuth(temp[0]['bearing'], False)
    # 夹角(左角)
    angle = neighborAzimuth - lastAzimuth + 180
    if angle < 0:
        angle += 360
    elif angle > 360:
        angle -= 360
    
    # 满足方向约束
    if abs(angle - targetAngle) < data.LineThreshold:
        molecular = getLengthFromNodes(G, route[targetsId - 1]['edgeNodes'][-1], currNode)
        neighborLength = getLengthFromNodes(G, currNode, neighborNode)
        molecular = route[targetsId - 1]['length'] + molecular + neighborLength
        denominator = getLengthFromRouteByNodes(targets[targetsId]['targetLength'][2], targets[targetsId]['targetLength'][3], route)
        minRatio = targets[targetsId]['targetLength'][4]
        maxRatio = targets[targetsId]['targetLength'][5]
        if (molecular < (denominator * maxRatio)):
            return -1
        else:
            return 0
    else:
        return 0

def searchRoute(G, currNode, targets, routes):
    targetsId = 0
    route = [] # {'id': , 'targetsId': , 'edgeNodes': []}
    travel(G, currNode, 0, currNode, targets, targetsId, route, routes, True)

def getCurrOsmidFromRouteById(route, id):
    for r in route:
        if r['targetsId'] == id:
            return r['id']
    return None

def getLastOsmidFromRouteById(route, currNode, id):
    lastNode = getCurrOsmidFromRouteById(route, id)
    for r in route:
        edgeNodes = r['edgeNodes']
        if (edgeNodes[0] == currNode) and (edgeNodes[-1] == lastNode):
            return edgeNodes[1]
        elif (edgeNodes[0] == lastNode) and (edgeNodes[-1] == currNode):
            return edgeNodes[-2]
        else:
            continue
    return None

def getLastAzimuthFromRoute(route, currNode, id):
    lastNode = getCurrOsmidFromRouteById(route, id)
    for r in route:
        edgeNodes = r['edgeNodes']
        if (edgeNodes[0] == currNode) and (edgeNodes[-1] == lastNode):
            return turnAzimuth(r['bearing'])
        elif (edgeNodes[0] == lastNode) and (edgeNodes[-1] == currNode):
            return r['bearing']
        else:
            continue
    return None

# lineExtension: 是否允许直线延伸
def travel(G, lastNode, lastAzimuth, currNode, targets, targetsId, route, routes, lineExtension=True):
    neighborNodes = list(G.neighbors(currNode))
    if len(neighborNodes) == 1:
        # 针对只有一条连接边的节点
        if len(targets[targetsId]['angle']) == 0:
            tempRoute = copy.deepcopy(route)
            tempTargetsId = targetsId + 1
            if targets[targetsId]['firstNode']:
                azimuth = getAzimuthFromNodes(G, currNode, neighborNodes[0])
                tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'bearing': azimuth, 'length': 0, 'edgeNodes': [currNode]})
                travel(G, currNode, azimuth, neighborNodes[0], targets, tempTargetsId, tempRoute, routes, True)
            else:
                if tempTargetsId >= len(targets):
                    # 考虑形状内部线段间距离约束
                    if 'targetLength' in targets[targetsId].keys():
                        molecular = getLengthFromNodes(G, route[targetsId - 1]['edgeNodes'][-1], currNode)
                        molecular += route[targetsId - 1]['length']
                        denominator = getLengthFromRouteByNodes(targets[targetsId]['targetLength'][2], targets[targetsId]['targetLength'][3], route)
                        minRatio = targets[targetsId]['targetLength'][4]
                        maxRatio = targets[targetsId]['targetLength'][5]
                        if molecular >= (denominator * minRatio) and molecular <= (denominator * maxRatio):
                            length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                            tempRoute[targetsId - 1]['length'] += length
                            tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                            tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                            routes.append(tempRoute[:])
                    else:
                        # 结束端点的操作
                        length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                        tempRoute[targetsId - 1]['length'] += length
                        tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                        tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                        routes.append(tempRoute[:])
                else:
                    # 考虑形状内部线段间距离约束
                    if 'targetLength' in targets[targetsId].keys():
                        molecular = getLengthFromNodes(G, route[targetsId - 1]['edgeNodes'][-1], currNode)
                        molecular += route[targetsId - 1]['length']
                        denominator = getLengthFromRouteByNodes(targets[targetsId]['targetLength'][2], targets[targetsId]['targetLength'][3], route)
                        minRatio = targets[targetsId]['targetLength'][4]
                        maxRatio = targets[targetsId]['targetLength'][5]
                        if molecular >= (denominator * minRatio) and molecular <= (denominator * maxRatio):
                            length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                            tempRoute[targetsId - 1]['length'] += length
                            tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                            tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                            currN = getCurrOsmidFromRouteById(tempRoute, targets[tempTargetsId]['angle'][0][1])
                            lastN = getLastOsmidFromRouteById(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                            lastAzimuth = getLastAzimuthFromRoute(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                            travel(G, lastN, lastAzimuth, currN, targets, tempTargetsId, tempRoute, routes, False)
                            return
                        else:
                            return

                    else:
                        length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                        tempRoute[targetsId - 1]['length'] += length
                        tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                        tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                        currN = getCurrOsmidFromRouteById(tempRoute, targets[tempTargetsId]['angle'][0][1])
                        lastN = getLastOsmidFromRouteById(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                        lastAzimuth = getLastAzimuthFromRoute(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                        travel(G, lastN, lastAzimuth, currN, targets, tempTargetsId, tempRoute, routes, False)
                        return
    
    for neighborNode in neighborNodes:
        if neighborNode == lastNode:
            continue
        # 起点和开放的终点
        if len(targets[targetsId]['angle']) == 0:
            tempRoute = copy.deepcopy(route)
            tempTargetsId = targetsId + 1
            if targets[targetsId]['firstNode']:
                azimuth = getAzimuthFromNodes(G, currNode, neighborNode)
                tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'bearing': azimuth, 'length': 0, 'edgeNodes': [currNode]})
                travel(G, currNode, azimuth, neighborNode, targets, tempTargetsId, tempRoute, routes, True)
            else:
                if tempTargetsId >= len(targets):
                    # 考虑形状内部线段间距离约束
                    if 'targetLength' in targets[targetsId].keys():
                        molecular = getLengthFromNodes(G, route[targetsId - 1]['edgeNodes'][-1], currNode)
                        molecular += route[targetsId - 1]['length']
                        denominator = getLengthFromRouteByNodes(targets[targetsId]['targetLength'][2], targets[targetsId]['targetLength'][3], route)
                        minRatio = targets[targetsId]['targetLength'][4]
                        maxRatio = targets[targetsId]['targetLength'][5]
                        if molecular < (denominator * minRatio):
                            err = polylineCostFuncForEndPoint(G, lastAzimuth, currNode, neighborNode, targets, targetsId, 180, route, lineExtension)
                            if err == 0:
                                continue
                            else:
                                length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                                tempRoute[targetsId - 1]['length'] += length
                                tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                                # 满足直线约束时，不改变待匹配的目标节点
                                travel(G, currNode, lastAzimuth, neighborNode, targets, targetsId, tempRoute, routes, True)
                        elif molecular > (denominator * maxRatio):
                            break
                        else:
                            length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                            tempRoute[targetsId - 1]['length'] += length
                            tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                            tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                            routes.append(tempRoute[:])
                            break
                    else:
                        # 结束端点的操作
                        length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                        tempRoute[targetsId - 1]['length'] += length
                        tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                        tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                        routes.append(tempRoute[:])
                        break
                else:
                    # 考虑形状内部线段间距离约束
                    if 'targetLength' in targets[targetsId].keys():
                        molecular = getLengthFromNodes(G, route[targetsId - 1]['edgeNodes'][-1], currNode)
                        molecular += route[targetsId - 1]['length']
                        denominator = getLengthFromRouteByNodes(targets[targetsId]['targetLength'][2], targets[targetsId]['targetLength'][3], route)
                        minRatio = targets[targetsId]['targetLength'][4]
                        maxRatio = targets[targetsId]['targetLength'][5]
                        if molecular < (denominator * minRatio):
                            err = polylineCostFuncForEndPoint(G, lastAzimuth, currNode, neighborNode, targets, targetsId, 180, route, lineExtension)
                            if err == 0:
                                continue
                            else:
                                length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                                tempRoute[targetsId - 1]['length'] += length
                                tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                                travel(G, currNode, lastAzimuth, neighborNode, targets, targetsId, tempRoute, routes, True)
                        elif molecular > (denominator * maxRatio):
                            break
                        else:
                            length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                            tempRoute[targetsId - 1]['length'] += length
                            tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                            tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                            currN = getCurrOsmidFromRouteById(tempRoute, targets[tempTargetsId]['angle'][0][1])
                            lastN = getLastOsmidFromRouteById(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                            lastAzimuth = getLastAzimuthFromRoute(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                            travel(G, lastN, lastAzimuth, currN, targets, tempTargetsId, tempRoute, routes, False)
                            break
                    else:
                        length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                        tempRoute[targetsId - 1]['length'] += length
                        tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                        tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                        currN = getCurrOsmidFromRouteById(tempRoute, targets[tempTargetsId]['angle'][0][1])
                        lastN = getLastOsmidFromRouteById(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                        lastAzimuth = getLastAzimuthFromRoute(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                        travel(G, lastN, lastAzimuth, currN, targets, tempTargetsId, tempRoute, routes, False)
                        break
        else:
            err = polylineCostFunc(G, lastAzimuth, currNode, neighborNode, targets, targetsId, route, lineExtension)
            if err == 0:
                continue
            elif err == -1:
                # print('Line Node id: ', currNode)
                tempRoute = copy.deepcopy(route)
                length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                tempRoute[targetsId - 1]['length'] += length
                tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                travel(G, currNode, lastAzimuth, neighborNode, targets, targetsId, tempRoute, routes, True)
            elif err == 1:
                tempRoute = copy.deepcopy(route)
                tempTargetsId = targetsId + 1
                if tempTargetsId >= len(targets):
                    length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                    tempRoute[targetsId - 1]['length'] += length
                    tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                    tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                    # 在这里考虑距离约束
                    if not targets[targetsId]['repeat']:
                        tempRoute[targetsId]['edgeNodes'].append(neighborNode)
                    routes.append(tempRoute[:])
                    continue
                else:
                    if not targets[targetsId]['repeat']:
                        if not targets[targetsId - 1]['repeat'] and (len(targets[targetsId - 1]['angle']) or targets[targetsId - 1]['firstNode']):
                            length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                            tempRoute[targetsId - 1]['length'] += length
                            tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                        azimuth = getAzimuthFromNodes(G, currNode, neighborNode)
                        tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'bearing': azimuth, 'length': 0, 'edgeNodes': [currNode]})
                        # travel(G, currNode, azimuth, neighborNode, targets, tempTargetsId, tempRoute, routes, True)
                        if tempRoute[0]['length'] > 150:
                            travel(G, currNode, azimuth, neighborNode, targets, tempTargetsId, tempRoute, routes, True)
                        else:
                            break
                    else: # 环闭合后开启下一段搜索
                        # print('Bihe Node id: ', currNode)
                        length = getLengthFromNodes(G, tempRoute[targetsId - 1]['edgeNodes'][-1], currNode)
                        tempRoute[targetsId - 1]['length'] += length
                        tempRoute[targetsId - 1]['edgeNodes'].append(currNode)
                        tempRoute.append({'id': currNode, 'targetsId': targets[targetsId]['id'], 'edgeNodes': [currNode]})
                        currN = getCurrOsmidFromRouteById(tempRoute, targets[tempTargetsId]['angle'][0][1])
                        lastN = getLastOsmidFromRouteById(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                        lastAzimuth = getLastAzimuthFromRoute(tempRoute, currN, targets[tempTargetsId]['angle'][0][0])
                        # print('-----------', currN, lastN, tempTargetsId, lastAzimuth, tempRoute)
                        travel(G, lastN, lastAzimuth, currN, targets, tempTargetsId, tempRoute, routes, False)
                        break


# 去重
def defuplication(routes):
    set_routes = []
    for route in routes:
        if route not in set_routes:
            set_routes.append(route)
    return set_routes

# colors = ['green', 'red', 'yellow', 'purple', 'blue']

# i = 0
# route_colors = []
# while i < len(routes):
#     route_colors.append(colors[i % len(colors)])
#     i += 1

def plotRoute(G, routes):
    totalR = []
    tempR = []
    # print(routes[0])
    for r in routes[0]:
        if len(r['edgeNodes']) > 1:
            tempR.extend(r['edgeNodes'][:-1])
        else:
            tempR.extend(r['edgeNodes'])
            totalR.append(tempR)
            tempR = []
    # print('--------------------------')
    # print(totalR)
    if len(totalR) > 1:
        ox.plot_graph_routes(G, totalR, 'r')
    else:
        ox.plot_graph_route(G, totalR[0], 'r')

def plotRoutes(G, routes):
    for route in routes:
        totalR = []
        tempR = []
        for r in route:
            if len(r['edgeNodes']) > 1:
                tempR.extend(r['edgeNodes'][:-1])
            else:
                tempR.extend(r['edgeNodes'])
                totalR.append(tempR)
                tempR = []
        if len(totalR) > 1:
            ox.plot_graph_routes(G, totalR, 'r')
        else:
            ox.plot_graph_route(G, totalR[0], 'r')

def plotGraph(G, routes):
    if len(routes) > 1:
        ox.plot_graph_routes(G, routes, 'r')
    else:
        ox.plot_graph_route(G, routes[0], 'r')


def getPointFromRoute(G, routes):
    points = []
    bearing = routes[-2]
    length = routes[-1]
    routes.pop(-1)
    routes.pop(-1)
    for route in routes:
        for r in route:
            nodeData = G.nodes[r]
            x = nodeData['x']
            y = nodeData['y']
            points.append((x,y))
    return [points, bearing, length]