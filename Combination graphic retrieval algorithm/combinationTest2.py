from asyncio.windows_events import NULL
import imp
import re
from shapely.geometry import Point, Polygon, MultiPoint
import SearchAlgorithm as sa
import osmnx as ox
import matplotlib.pyplot as plt
import math
import copy
from plotLg import plot_graph_routes2
import multiprocessing
from scipy.spatial.distance import cdist

import networkx as nx
import plotLg as lg
import sys

def readFile(srcPath, filename):
    fileRectRead1 = open(srcPath + '/' + filename, 'r', encoding='utf-8')
    shapes1 = []
    line1 = fileRectRead1.readline()
    while line1:
        shapes1.append(eval(line1))
        line1 = fileRectRead1.readline()
    fileRectRead1.close()
    return shapes1

def azimuthAngle(start, end):
    x1 = start[0]
    y1 = start[1]
    x2 = end[0]
    y2 = end[1]
    angle = 0.0
    dx = x2 - x1
    dy = y2 - y1
    if  x2 == x1:
        angle = math.pi / 2.0
        if  y2 == y1 :
            angle = 0.0
        elif y2 < y1 :
            angle = 3.0 * math.pi / 2.0
    elif x2 > x1 and y2 > y1:
        angle = math.atan(dx / dy)
    elif  x2 > x1 and  y2 < y1 :
        angle = math.pi / 2 + math.atan(-dy / dx)
    elif  x2 < x1 and y2 < y1 :
        angle = math.pi + math.atan(dx / dy)
    elif  x2 < x1 and y2 > y1 :
        angle = 3.0 * math.pi / 2.0 + math.atan(dy / -dx)
    return angle * 180 / math.pi

def geodistance(lng1,lat1,lng2,lat2):
    #lng1,lat1,lng2,lat2 = (120.12802999999997,30.28708,115.86572000000001,28.7427)
    lng1, lat1, lng2, lat2 = map(math.radians, [float(lng1), float(lat1), float(lng2), float(lat2)]) # 经纬度转换成弧度
    dlon=lng2-lng1
    dlat=lat2-lat1
    a=math.sin(dlat/2)**2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlon/2)**2
    distance=2*math.asin(math.sqrt(a))*6371*1000 # 地球平均半径，6371km
    distance=round(distance/1000,3)
    return distance * 1000 # 单位：m
    # lonlat1 = [(lat1, lng1)] 
    # lonlat2 = [(lat2, lng2)] 
    # return cdist(lonlat1,lonlat2,metric='euclidean')


def getHeightLength(points):
    length1 = geodistance(points[0][0], points[0][1], points[1][0], points[1][1])
    length2 = geodistance(points[1][0], points[1][1], points[2][0], points[2][1])
    return length1 if length1 > length2 else length2

def getWidthLength(points):
    length1 = geodistance(points[0][0], points[0][1], points[1][0], points[1][1])
    length2 = geodistance(points[1][0], points[1][1], points[2][0], points[2][1])
    return length2 if length1 > length2 else length1

def getAngle2(lastBearing, currBearing):
    angle = currBearing - lastBearing
    absAngle = abs(angle)
    if absAngle > 180: 
        if angle < 0:
            return True
        else:
            return False
    else:
        if angle > 0:
            return True
        else:
            return False

def takeFirst(elem):
    return elem[0]

def takeThird(elem):
    return elem[2]

def greedyCostFuncForSecondShape(currShape, neighborShapes, turnAzimuth):
    newShapes = []
    srcCenter = currShape[-5]
    srcRectH = currShape[-4]
    srcRectW = currShape[-3]
    srcBearing = currShape[-6]

    scores = []
    for neighborShape in neighborShapes:
        neighborCenter = neighborShape[-5]
        neighborBearing = neighborShape[-6]
        neighborRectH = neighborShape[-4]
        neighborRectW = neighborShape[-3]
        centerDistance = geodistance(neighborCenter[0], neighborCenter[1], srcCenter[0], srcCenter[1])

        distD = centerDistance - (srcRectW + neighborRectW) / 2.0
        ratioH = neighborRectH / srcRectH
        # 判断图形之间是否能组合（图形之间不重合，且尺度相差在阈值区间[0.3, 3]之间）
        if distD > 0.0 and ratioH >= 0.3333 and ratioH <= 3.0:
            if turnAzimuth:
                neighborAzimuth = azimuthAngle(neighborCenter, srcCenter)
            else:
                neighborAzimuth = azimuthAngle(srcCenter, neighborCenter)
            angleFW = abs(neighborAzimuth - srcBearing)
            if angleFW > 180.0:
                angleFW = 360.0 - angleFW
            # # 最小外接矩形排列组合的方向在一侧（右上方75°到右下方90°范围内）
            if angleFW < 60:
                angleFX = abs(neighborBearing - srcBearing)
                if angleFX > 180.0:
                    angleFX = 360.0 - angleFX
                if angleFX < 60.0:
                    score = angleFW / 30.0 + angleFX / 30.0                
                    scores.append((score, neighborShape, distD, neighborAzimuth))
            # print(angleFW)
            # print('The num of scores2: ')
            # print(angle2Threshold)
    if len(scores) > 0:
        # 组合评分
        scores.sort(key=takeFirst)
        # print('The num of scores: ')
        # print(len(scores))
        scores20 = scores[0:20]
        resultShape = min(scores20, key=lambda x: x[2])
        # resultShape = scores[0]
        return (resultShape[1], resultShape[3])
    else:
        return ()


def greedyCostFuncTest(lastAzimuth, srcShape, currShape, neighborShapes, turnAzimuth):
    currCenter = currShape[-5]
    currRectH = currShape[-4]
    currRectW = currShape[-3]
    currBearing = currShape[-6]
    srcBearing = srcShape[-6]
    srcCenter = srcShape[-5]
    srcBearing = srcShape[-6]
    scores = []
    for neighborShape in neighborShapes:
        neighborCenter = neighborShape[-5]
        neighborBearing = neighborShape[-6]
        neighborRectH = neighborShape[-4]
        neighborRectW = neighborShape[-3]
        centerDistance = geodistance(neighborCenter[0], neighborCenter[1], currCenter[0], currCenter[1])

        distD = centerDistance - (currRectW + neighborRectW) / 2.0
        ratioH = neighborRectH / currRectH
        # 判断图形之间是否能组合（图形之间不重合，且尺度相差在阈值区间[0.3, 3]之间）
        if distD > 0.0 and ratioH >= 0.3333 and ratioH <= 3.0:
            if turnAzimuth:
                neighborAzimuth = azimuthAngle(neighborCenter, currCenter)
            else:
                neighborAzimuth = azimuthAngle(currCenter, neighborCenter)
            angleFW = abs(neighborAzimuth - srcBearing)
            if angleFW > 180.0:
                angleFW = 360.0 - angleFW
            if angleFW < 60:
                angleFX = abs(neighborBearing - srcBearing)
                if angleFX > 180.0:
                    angleFX = 360.0 - angleFX
                if angleFX < 60.0:
                    score = angleFW / 30.0 + angleFX / 30.0
                    scores.append((score, neighborShape, distD, centerDistance, (currRectW + neighborRectW) / 2.0))

    if len(scores) > 0:
        # 组合评分            
        scores.sort(key=takeFirst)
        # print('The num of scores: ')
        # print(len(scores))
        # id = math.floor(len(scores)/3)
        scores20 = scores[0:20]
        resultShape = min(scores20, key=lambda x: x[2])
        # print('The result shape distD:  ')
        # print(resultShape[2])
        # print(resultShape[3])
        # print(resultShape[4])
            
        # resultShape = scores[0]
        
        return resultShape[1]
    else:
        return ()


def travel(currShape, lastAzimuth, shapeId, index, totalIndex, datas, targets, srcShape):
    if shapeId == 1:
        target = targets[shapeId]
        neighborShapes = datas[target['to']]
        neighborDatas = greedyCostFuncForSecondShape(currShape, neighborShapes, target['turnAzimuth'])
        if len(neighborDatas) == 0:
            totalIndex = []
            # print('--------------3')
            # print(totalIndex)
        else:
            tempShapeId = shapeId + 1
            tempIndex = index
            tempIndex = copy.deepcopy(index)
            tempIndex.append(neighborDatas[0][-1])            
            if targets[tempShapeId]['returnStart']:
                travel(srcShape, neighborDatas[1], tempShapeId, tempIndex, totalIndex, datas, targets, srcShape)
            else:          
                travel(neighborDatas[0], neighborDatas[1], tempShapeId, tempIndex, totalIndex, datas, targets, srcShape)
    else:
        # if shapeId < len(datas): 
            target = targets[shapeId]
            neighborShapes = datas[target['to']]
            neighborDatas = greedyCostFuncTest(lastAzimuth, srcShape, currShape, neighborShapes, target['turnAzimuth'])
            if len(neighborDatas) == 0:
                totalIndex = []
                # print('--------------1')
                # print(totalIndex)
            else:
                tempShapeId = shapeId + 1
                tempIndex = index
                tempIndex = copy.deepcopy(index)
                if tempShapeId >= len(datas):
                    tempIndex.append(neighborDatas[-1])                    
                    totalIndex.append(tempIndex)
                    # print('--------------2')
                    # print(neighborDatas)
                else:
                    tempIndex.append(neighborDatas[-1])
                    if targets[tempShapeId]['returnStart']:
                        travel(srcShape, lastAzimuth, tempShapeId, tempIndex, totalIndex, datas, targets, srcShape)
                    else:
                        travel(neighborDatas, lastAzimuth, tempShapeId, tempIndex, totalIndex, datas, targets, srcShape)


def doSomething(data):
    totalIndex = []
    index = []
    
    # datas = [shapes1, shapes2, shapes3, shapes4]
    datas = data[2]

    shape = data[0]
    shapeId = 0
    index.append(shape[-1])

    tempShapeId = shapeId + 1
    travel(shape, 0.0, tempShapeId, index, totalIndex, datas, data[1], shape)
    return totalIndex


def comLOVE():
    # 注意修改路径
    srcPath = 'Data/OutTxt/Combination/changan/'

    shapes1 = readFile(srcPath, 'rectL.txt')
    shapes2 = readFile(srcPath, 'rectO.txt')
    shapes3 = readFile(srcPath, 'rectU.txt')
    shapes4 = readFile(srcPath, 'rectE.txt')
    datas = [shapes1, shapes2, shapes3, shapes4]
    
    targets = ({'from': 1},
        {'from': 1, 'to': 2, 'turnAzimuth': False, 'returnStart': False},
        {'from': 2, 'to': 3, 'turnAzimuth': False, 'returnStart': False},
        {'from': 1, 'to': 0, 'turnAzimuth': True, 'returnStart': True})
    p = multiprocessing.Pool(8)
    
    shapes = datas[targets[0]['from']]
    newShapes = []
    for shape in shapes:
        newShapes.append((shape, targets, datas))
    totalIndex = p.map(doSomething, newShapes)
    p.close()
    p.join()


    print(len(totalIndex))
    fileIndex = open(srcPath + '/index.txt', 'w')
    for index in totalIndex:
        if len(index) > 0:
            print('-------------------')
            print(len(index))
            fileIndex.write(str(index[0]) + '\n')
    fileIndex.close()

def com1314():
    # 注意修改路径
    srcPath = 'Data/OutTxt/Combination/dongcheng/'

    shapes1 = readFile(srcPath, 'rectI.txt')
    shapes3 = readFile(srcPath, 'rect3.txt')
    shapes4 = readFile(srcPath, 'rect4.txt')
    datas = [shapes1, shapes3, shapes1, shapes4]
    
    targets = ({'from': 1},
        {'from': 1, 'to': 2, 'turnAzimuth': False, 'returnStart': False},
        {'from': 2, 'to': 3, 'turnAzimuth': False, 'returnStart': False},
        {'from': 1, 'to': 0, 'turnAzimuth': True, 'returnStart': True})
    p = multiprocessing.Pool(8)
    
    shapes = datas[targets[0]['from']]
    newShapes = []
    for shape in shapes:
        newShapes.append((shape, targets, datas))
    totalIndex = p.map(doSomething, newShapes)
    p.close()
    p.join()


    print(len(totalIndex))
    fileIndex = open(srcPath + '/index.txt', 'w')
    for index in totalIndex:
        if len(index) > 0:
            print('-------------------')
            print(len(index))
            fileIndex.write(str(index[0]) + '\n')
    fileIndex.close()

def com520():
    # 注意修改路径
    srcPath = 'Data/OutTxt/Combination/minhang/'

    shapes1 = readFile(srcPath, 'rect5.txt')
    shapes3 = readFile(srcPath, 'rect2.txt')
    shapes4 = readFile(srcPath, 'rectO.txt')
    datas = [shapes1, shapes3, shapes4]
    
    targets = ({'from': 2},
        {'from': 2, 'to': 1, 'turnAzimuth': True, 'returnStart': False},
        {'from': 1, 'to': 0, 'turnAzimuth': True, 'returnStart': False})
    p = multiprocessing.Pool(8)
    
    shapes = datas[targets[0]['from']]
    newShapes = []
    for shape in shapes:
        newShapes.append((shape, targets, datas))
    totalIndex = p.map(doSomething, newShapes)
    p.close()
    p.join()


    print(len(totalIndex))
    fileIndex = open(srcPath + '/index.txt', 'w')
    for index in totalIndex:
        if len(index) > 0:
            print('-------------------')
            print(len(index))
            fileIndex.write(str(index[0]) + '\n')
    fileIndex.close()

def comIHy():
    # 注意修改路径
    srcPath = 'Data/OutTxt/Combination/nanshan/'

    shapes1 = readFile(srcPath, 'rectI.txt')
    shapes3 = readFile(srcPath, 'rectHeart.txt')
    shapes4 = readFile(srcPath, 'recty.txt')
    datas = [shapes1, shapes3, shapes4]
    
    targets = ({'from': 1},
        {'from': 1, 'to': 2, 'turnAzimuth': False, 'returnStart': False},
        {'from': 1, 'to': 0, 'turnAzimuth': True, 'returnStart': True})
    p = multiprocessing.Pool(8)
    
    shapes = datas[targets[0]['from']]
    newShapes = []
    for shape in shapes:
        newShapes.append((shape, targets, datas))
    totalIndex = p.map(doSomething, newShapes)
    p.close()
    p.join()


    print(len(totalIndex))
    fileIndex = open(srcPath + '/index.txt', 'w')
    for index in totalIndex:
        if len(index) > 0:
            print('-------------------')
            print(len(index))
            fileIndex.write(str(index[0]) + '\n')
    fileIndex.close()

def main():
    """
        通过sys模块来识别命令行参数
    """
    if len(sys.argv) > 1:
        param = sys.argv[1]
        if param == '1314':
            com1314() # combination '1314'
        elif param == '520':
            com520() # combination '520'
        elif param == 'IHy':
            comIHy() # combination 'I♥y'
        elif param == 'LOVE':
            comLOVE() # combination 'LOVE'
    else:
        print('Please input the param.')

if __name__ == '__main__':
    main()