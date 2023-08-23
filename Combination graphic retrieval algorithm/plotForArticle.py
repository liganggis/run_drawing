import osmnx as ox
import networkx as nx
from combinationTest2 import geodistance
import plotLg as lg
import sys

# G = ox.load_graphml('H:/Users/OsmProject/Save/GraphML/Futian.graphml')

# route = [277057475, 564641472, 1722057347, 1722057351, 6043430741, 6043430722, 8324496446, 8099204879, 8099316427, 8099316426, 8099316425, 8099316400, 8099204885, 8099316455, 8099316365, 8099316369, 8099316368, 8099316363, 8099316367, 8099316474, 8099316487, 8099316512, 8099308404, 8099316511, 8105505228, 8105505222, 8099316495, 8099316494, 8099316496, 1722057338, 8099316507, 8099212791]
# ox.plot_graph_route(G, route, 'r')

# fileRouteRead0 = open('H:/Users/IDEA/SelfMatch/data/OutTxt/A.txt', 'r', encoding='utf-8')
# shapes0 = []
# line0 = fileRouteRead0.readline()
# while line0:
#     if line0 != '':
#         print(line0)
#         shapes0 = eval(line0)
#         line0 = fileRouteRead0.readline()
# fileRouteRead0.close()

# print(shapes0)
# G = ox.load_graphml('H:/Users/IDEA/SelfMatch/data/graphml/test2/yangli1-shenzhen_nanshan.graphml')
# G = ox.load_graphml('H:/Users/IDEA/SelfMatch/data/graphml/test2/yangli3-wuhan_hongshan.graphml')
# G = ox.load_graphml('H:/Users/IDEA/SelfMatch/data/graphml/test2/yangli5-beijing_dongcheng.graphml')
# G = ox.load_graphml('H:/Users/IDEA/SelfMatch/data/graphml/test2/yangli7-shanghai_minhang.graphml')
# G = ox.load_graphml('H:/Users/IDEA/SelfMatch/data/graphml/test2/yangli10-xian_changan.graphml')
# G = G.to_undirected()

# G = ox.load_graphml('F:/MyData/华为未来终端实验室/第一阶段验收/graphml/yangli1-shenzhen_nanshan.graphml')
# print(len(G.nodes()))
# print(len(G.edges()))
# fig, ax = ox.plot_graph(G, node_color='r')
# lg.plot_graph(G, node_color='r', node_size=0, bgcolor="#ffffffff")
# lg.plot_graph(G, node_color='r', bgcolor="#ffffffff")
# ox.plot_graph_routes(G, [[14,15],[19,18],[14,19],[13,12],[14,13],
#     [21,62],[21,64,24],[21,63],[21,61,20],
#     [10,56,11],[57,58],[10,57],
#     [33,65],[35,36],[34,66,35],[33,34],[35,33],
#     [8,9],[8,54]], 'r')
# ox.plot_graph_routes(G, [[18,19],[18,17],[19,18],[14,19],[15,14],[18,15],
# [31,29],[31,30],[26,25],[27,59,26],[28,60,27],[31,28]], 'r')
# ox.plot_graph_routes(G, [[33,32],[35,36],[34,35],[33,34],[35,33], [31,29],[31,30],[26,25],[27,26],[28,27],[31,28], [6,1],[5,6],[4,5],[3,4],[2,3],[1,2],
# [21,22],[21,24],[21,23],[21,20], [40,41],[39,40],[38,37],[39,38], [10,11],[12,13],[10,12], [43,44],[42,43],[51,42],[50,51],[49,50],[48,49],[47,48],[46,47],[45,46],[44,45], [15,17],[15,16],[14,15],[19,14],[18,19],[15,18], [8,9],[7,8]], 'r')

# Heart
# ox.plot_graph_routes(G, 
# [[2184499119,9629031484,2704579470,7264926350,2704579517,9629031523,2184499120,9629031526,4526092254,2704611119,9629031619,2184499121,2402273641],[2184499029,8349563304,2184499037,7501209744,7501209743,2184499043,8349563124,2297455707,2402275847,2184499046,8312143633,9629025405,2184499051,2184499060,9629025406,9629025370,9629031482,2184499119],[2184499034,8349563345,2428805618,2184499033,2402275856,2184499032,8349563142,8349563141,8349563306,2184499029],[2184499062,2184499054,9629031507,2184499044,2402273654,8349563687,2184499034],[7782982564,7782982563,9629031598,2184499073,2184499072,6444510065,2184499069,6444510066,2184499065,7782982570,6444510058,2184499064,6444510059,9629031510,2184499062],[2402273641,9629031618,7782982548,7782982549,5178935608,7782982551,7782982552,2402273697,9629031600,7782982564]]
# , 'r')

# ox.plot_graph_routes(G,
# [[2184499128,1236299315,1236299446,5999844246,1236299420,1236299330,278660437],[2184499060,9629025406,9629025370,9629031482,2184499119,9629031483,2184499126,2184499127,9629031547,2184499128],[2184499062,9629031506,6647460205,9629025377,9629025408,2184499060],[2184499120,9629031524,2704586014,4526092210,9629031508,2184499062],[2184499121,9629031619,2704611119,4526092254,9629031526,2184499120],[278660437,9629031615,2184499121]], 'r')


# I_heart_y (shenzhen_nanshan)
# ox.plot_graph_routes(G, 
# [[6647339694,8349563367,6647335574,8349563382,6647339693,6647339859,4810198446],[8349563201,6647335577,6647335576,8349563224,6647335575,8349563234,6647339862,6647339861,6647339694],
# [2184499119,9629031484,2704579470,7264926350,2704579517,9629031523,2184499120,9629031526,4526092254,2704611119,9629031619,2184499121,2402273641],[2184499029,8349563304,2184499037,7501209744,7501209743,2184499043,8349563124,2297455707,2402275847,2184499046,8312143633,9629025405,2184499051,2184499060,9629025406,9629025370,9629031482,2184499119],[2184499034,8349563345,2428805618,2184499033,2402275856,2184499032,8349563142,8349563141,8349563306,2184499029],[2184499062,2184499054,9629031507,2184499044,2402273654,8349563687,2184499034],[7782982564,7782982563,9629031598,2184499073,2184499072,6444510065,2184499069,6444510066,2184499065,7782982570,6444510058,2184499064,6444510059,9629031510,2184499062],[2402273641,9629031618,7782982548,7782982549,5178935608,7782982551,7782982552,2402273697,9629031600,7782982564],
# [2958627173,9909496924,8461536026,8461536052,8461536050],[1236299551,1236299430,5178935409,5179092050],[2958627173,1236299567,1236299328,1236299551],[2958627113,1236299556,1236299517,1236299488],[2958627173,2958627113]]
# , 'r')

# ox.plot_graph_routes(G, 
# [[1236299482,1236299386,8909590508,1236299288],

# [2184499119,9629031484,2704579470,7264926350,2704579517,9629031523,2184499120,9629031526,4526092254,2704611119,9629031619,2184499121,2402273641],[2184499029,8349563304,2184499037,7501209744,7501209743,2184499043,8349563124,2297455707,2402275847,2184499046,8312143633,9629025405,2184499051,2184499060,9629025406,9629025370,9629031482,2184499119],[2184499034,8349563345,2428805618,2184499033,2402275856,2184499032,8349563142,8349563141,8349563306,2184499029],[2184499062,2184499054,9629031507,2184499044,2402273654,8349563687,2184499034],[7782982564,7782982563,9629031598,2184499073,2184499072,6444510065,2184499069,6444510066,2184499065,7782982570,6444510058,2184499064,6444510059,9629031510,2184499062],[2402273641,9629031618,7782982548,7782982549,5178935608,7782982551,7782982552,2402273697,9629031600,7782982564],

# [2184499017,2400351125,2428805606,278376793,5981067881],[2184499026,278660706,8349563695,6444510073,2402273633,2397991660,8349563400],[2184499017,6647335574,2184499018,8349563244,2184499020,8349563381,2184499026],[1937711039,6647335575,8349563238,2184499021,8349563231,2184499023],[2184499017,8349563265,1937711036,1937713783,1937711039]]
# , 'r')

# 1314 (beijing_dongcheng)
# ox.plot_graph_routes(G, 
# [[528868025,528868022,9797859398,528868018,1976297662,528868013,4253292805,528868011,9797859403,9797859402,528868009,528868005],
# [341225742,341225958],[324265679,2093035179],[341225742,340244113,340251195,324265679],[340244224,530638431,341228302],[341225742,342737295,340244224],
# [340416980,340413694,385085256,1555209156,341225626,340244115,341221208,324265678],
# [734045321,385084231],[734045321,385084239],[734045361,1555209158,734045523],[734045321,734045361]]
# , 'r')

#  520 (shanghai_minhang)
# ox.plot_graph_routes(G, 
# [[1813362850,1813362710,1813362803],[1812536700,1812536206,1813362793],[1812536316,1802241219,1812536432,1812536700],[1813362651,1812536316],[1813362850,1813362800,1813362651],

# [1813362672,1813362837,1813362830,5940895704],[1812536399,1812536435,3448623065],[1812536548,1802221777,1812536554,1812536399],[1812536503,1812536315,1812536548],[1813362672,1812536505,1812536503],

# [1813362664,6001764525,1812536567,1812536648,1812536284,1812536569],[1813362802,1813362664],[3543647156,3543647157,3543647158,6001764539,1813362802],[1812536569,6000838367,3543647156]]
# , 'r')

# # 2023 (xian_changan)
# ox.plot_graph_routes(G, 
# [[7140284760,4035591752,4035591753,7140284861,9867987729],[7140284760,7140284293,7140284469,7140284566],

# [8177720347,9867987727,7140284476,7140284477,7140284875,7140284514],[5479793331,2386057121,6245448739,6245448718,8177720347],[7140284422,2386057131,5479793329,5479793330,7140284473,9867987726,5479793331],[7140284514,7140284643,7140284422],

# [2386064281,5479793325,7140284897,9867987724],[8617585116,5479793324,7140284313,9867987723],[2386064281,8617585116],

# [7140284507,9867930239],[2386064298,9966107798],[7140284507,9867930238,2386064298],[5479793016,6540963671],[7140284507,5479793016]]
# , 'r')


# print(shapes3[5])
# print(shapes4[414])

# print(indexs[0])
# print(indexs[1])

# ct3 = shapes3[5][-5]
# ct4 = shapes4[414][-5]
# centerDistance = geodistance(ct3[0], ct3[1], ct4[0], ct4[1])
# print(centerDistance)
# print(shapes3[5][-4])
# print(shapes3[5][-3])
# print(shapes4[414][-4])
# print(shapes4[414][-3])

# G = ox.load_graphml('Data/graphml/test2/yangli1-shenzhen_nanshan.graphml')
# G = ox.load_graphml('Data/graphml/test2/yangli3-wuhan_hongshan.graphml')
# G = ox.load_graphml('Data/graphml/test2/yangli5-beijing_dongcheng.graphml')
# G = ox.load_graphml('Data/graphml/test2/yangli7-shanghai_minhang.graphml')
# G = ox.load_graphml('Data/graphml/test2/yangli10-xian_changan.graphml')
# G = G.to_undirected()
# srcPath = 'Data/OutTxt/Combination/changan/'

def readFile(srcPath, filename):
    fileRectRead1 = open(srcPath + '/' + filename, 'r', encoding='utf-8')
    shapes1 = []
    line1 = fileRectRead1.readline()
    while line1:
        shapes1.append(eval(line1))
        line1 = fileRectRead1.readline()
    fileRectRead1.close()
    return shapes1

def plotLOVE():
    # 注意修改路径
    G = ox.load_graphml('Data/graphml/test2/yangli10-xian_changan.graphml')
    G = G.to_undirected()
    srcPath = 'Data/OutTxt/Combination/changan/'
    s1 = readFile(srcPath, 'L_test.txt')
    s2 = readFile(srcPath, 'O_test.txt')
    s3 = readFile(srcPath, 'U_test.txt')
    s4 = readFile(srcPath, 'E_test.txt')
    ixs = readFile(srcPath, 'index.txt')

    for index in ixs:
        temp = index
        route1 = s1[temp[3]]
        r1 = route1.copy()
        r1.pop(-1)
        r1.pop(-1)
        route2 = s2[temp[0]]
        r2 = route2.copy()
        r2.pop(-1)
        r2.pop(-1)
        route3 = s3[temp[1]]
        r3 = route3.copy()
        r3.pop(-1)
        r3.pop(-1)
        route4 = s4[temp[2]]
        r4 = route4.copy()
        r4.pop(-1)
        r4.pop(-1)
            
        route = r1 + r3 + r4 + r2
        ox.plot_graph_routes(G, 
        route
        , 'r')

def plotIHy():
    # 注意修改路径
    G = ox.load_graphml('Data/graphml/test2/yangli1-shenzhen_nanshan.graphml')
    G = G.to_undirected()
    srcPath = 'Data/OutTxt/Combination/nanshan/'
    s1 = readFile(srcPath, 'I_test.txt')
    s2 = readFile(srcPath, 'heart_test.txt')
    s3 = readFile(srcPath, 'y_test.txt')
    ixs = readFile(srcPath, 'index.txt')

    for index in ixs:
        temp = index
        route1 = s1[temp[2]]
        r1 = route1.copy()
        r1.pop(-1)
        r1.pop(-1)
        route2 = s2[temp[0]]
        r2 = route2.copy()
        r2.pop(-1)
        r2.pop(-1)
        route3 = s3[temp[1]]
        r3 = route3.copy()
        r3.pop(-1)
        r3.pop(-1)
            
        route = r1 + r2 + r3
        ox.plot_graph_routes(G, 
        route
        , 'r')

def plot520():
    # 注意修改路径
    G = ox.load_graphml('Data/graphml/test2/yangli7-shanghai_minhang.graphml')
    G = G.to_undirected()
    srcPath = 'Data/OutTxt/Combination/minhang/'
    s1 = readFile(srcPath, '5_test.txt')
    s2 = readFile(srcPath, '2_test.txt')
    s3 = readFile(srcPath, 'O_test.txt')
    ixs = readFile(srcPath, 'index.txt')

    for index in ixs:
        temp = index
        route1 = s1[temp[2]]
        r1 = route1.copy()
        r1.pop(-1)
        r1.pop(-1)
        route2 = s2[temp[1]]
        r2 = route2.copy()
        r2.pop(-1)
        r2.pop(-1)
        route3 = s3[temp[0]]
        r3 = route3.copy()
        r3.pop(-1)
        r3.pop(-1)
            
        route = r1 + r2 + r3
        ox.plot_graph_routes(G, 
        route
        , 'r')

def plot1314():
    # 注意修改路径
    G = ox.load_graphml('Data/graphml/test2/yangli5-beijing_dongcheng.graphml')
    G = G.to_undirected()
    srcPath = 'Data/OutTxt/Combination/dongcheng/'
    s1 = readFile(srcPath, 'I_test.txt')
    s3 = readFile(srcPath, '3_test.txt')
    s4 = readFile(srcPath, '4_test.txt')
    ixs = readFile(srcPath, 'index.txt')

    for index in ixs:
        temp = index
        route1 = s1[temp[3]]
        r1 = route1.copy()
        r1.pop(-1)
        r1.pop(-1)
        route2 = s1[temp[1]]
        r2 = route2.copy()
        r2.pop(-1)
        r2.pop(-1)
        route3 = s3[temp[0]]
        r3 = route3.copy()
        r3.pop(-1)
        r3.pop(-1)
        route4 = s4[temp[2]]
        r4 = route4.copy()
        r4.pop(-1)
        r4.pop(-1)
            
        route = r1 + r2 + r3 + r4
        ox.plot_graph_routes(G, 
        route
        , 'r')

# for shape in shapes4:
#     route = shape
#     route.pop(-1)
#     route.pop(-1)
#     ox.plot_graph_routes(G, 
#     route
#     , 'r')

def main():
    """
        通过sys模块来识别命令行参数
    """
    if len(sys.argv) > 1:
        param = sys.argv[1]
        if param == '1314':
            plot1314() # show results of '1314'
        elif param == '520':
            plot520() # show results of '520'
        elif param == 'IHy':
            plotIHy() # show results of 'I♥y'
        elif param == 'LOVE':
            plotLOVE() # show results of 'LOVE'
    else:
        print('Please input the param.')

if __name__ == '__main__':
    main()