from shapely.geometry import Point, Polygon, MultiPoint
import SearchAlgorithm as sa
import osmnx as ox
import matplotlib.pyplot as plt
import math



def geodistance(lng1,lat1,lng2,lat2):
    #lng1,lat1,lng2,lat2 = (120.12802999999997,30.28708,115.86572000000001,28.7427)
    lng1, lat1, lng2, lat2 = map(math.radians, [float(lng1), float(lat1), float(lng2), float(lat2)]) # 经纬度转换成弧度
    dlon=lng2-lng1
    dlat=lat2-lat1
    a=math.sin(dlat/2)**2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlon/2)**2
    distance=2*math.asin(math.sqrt(a))*6371*1000 # 地球平均半径，6371km
    distance=round(distance/1000,3)
    return distance * 1000 # 单位：m

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

def getHeightLength(points, targetAzimuth):
    length1 = geodistance(points[0][0], points[0][1], points[1][0], points[1][1])
    length2 = geodistance(points[1][0], points[1][1], points[2][0], points[2][1])
    return length1 if length1 > length2 else length2

def getWidthLength(points):
    length1 = geodistance(points[0][0], points[0][1], points[1][0], points[1][1])
    length2 = geodistance(points[1][0], points[1][1], points[2][0], points[2][1])
    width = length2 if length1 > length2 else length1
    return width

def getWH(points, targetAzimuth):
    length1 = geodistance(points[0][0], points[0][1], points[1][0], points[1][1])
    azimuth1 = azimuthAngle(points[0], points[1])
    a1 = abs(azimuth1 - targetAzimuth)
    if a1 > 180:
        a1 = 360 - a1
    length2 = geodistance(points[1][0], points[1][1], points[2][0], points[2][1])
    azimuth2 = azimuthAngle(points[1], points[2])
    a2 = abs(azimuth2 - targetAzimuth)
    if a2 > 180:
        a2 = 360 - a2
    if a1 < a2:
        return (length1, length2) # w, h
    else:
        return (length2, length1) # w, h

def crt(srcFilePath, outFilePath, G, isI):
    fileRoutes = open(srcFilePath, 'r', encoding='utf-8')
    fileRect = open(outFilePath, 'w')
    line = fileRoutes.readline()
    i = 0
    while line:
        if len(line) <= 1:
            line = fileRoutes.readline() # 读取下一行 
            continue
        print(i)
        shape = sa.getPointFromRoute(G, eval(line))
        min_rect = MultiPoint(shape[0]).minimum_rotated_rectangle
        rectList = []
        rectH = 0
        rectW = 0
        if min_rect.geom_type == 'Polygon':
            rectList = list(min_rect.exterior.coords)
            rectW, rectH = getWH(rectList, shape[1])
            if isI:
                rectW = rectH / 2.0
        else:
            rectList = list(min_rect.coords)
            rectW, rectH = getWH([rectList[0],rectList[1],rectList[1],rectList[1],rectList[0]], shape[1])
            if isI:
                rectW = rectH / 2.0
        rectList.append(shape[1])
        rectList.append(min_rect.centroid.coords[:][0])
        rectList.append(rectH)
        rectList.append(rectW)
        rectList.append(shape[2])
        rectList.append(i)
        fileRect.write(str(rectList) + '\n')
        line = fileRoutes.readline() # 读取下一行
        i += 1
    fileRect.close()
    fileRect.close()

def test520():
    # 注意修改路径
    G = ox.load_graphml('Data/graphml/test2/yangli7-shanghai_minhang.graphml')
    srcPath = 'Data/OutTxt/Combination/minhang/'
    crt(srcPath + '/2_test.txt', srcPath + '/rect2.txt', G, False)
    crt(srcPath + '/5_test.txt', srcPath + '/rect5.txt', G, False)
    crt(srcPath + '/O_test.txt', srcPath + '/rectO.txt', G, False)

def testIHY():
    # 注意修改路径
    G = ox.load_graphml('Data/graphml/test2/yangli1-shenzhen_nanshan.graphml')
    srcPath = 'Data/OutTxt/Combination/nanshan/'
    crt(srcPath + '/I_test.txt', srcPath + '/rectI.txt', G, True)
    crt(srcPath + '/heart_test.txt', srcPath + '/rectHeart.txt', G, False)
    crt(srcPath + '/y_test.txt', srcPath + '/recty.txt', G, False)

def test1314():
    # 注意修改路径
    G = ox.load_graphml('Data/graphml/test2/yangli5-beijing_dongcheng.graphml')
    srcPath = 'Data/OutTxt/Combination/dongcheng/'
    crt(srcPath + '/I_test.txt', srcPath + '/rectI.txt', G, True)
    crt(srcPath + '/3_test.txt', srcPath + '/rect3.txt', G, False)
    crt(srcPath + '/4_test.txt', srcPath + '/rect4.txt', G, False)

def testLOVE():
    # 注意修改路径
    G = ox.load_graphml('Data/graphml/test2/yangli10-xian_changan.graphml')
    srcPath = 'Data/OutTxt/Combination/changan/'
    crt(srcPath + '/L_test.txt', srcPath + '/rectL.txt', G, False)
    crt(srcPath + '/O_test.txt', srcPath + '/rectO.txt', G, False)
    crt(srcPath + '/U_test.txt', srcPath + '/rectU.txt', G, False)
    crt(srcPath + '/E_test.txt', srcPath + '/rectE.txt', G, False)

if __name__ == '__main__':
    # testLOVE()
    # test1314()
    testIHY()
    # test520()