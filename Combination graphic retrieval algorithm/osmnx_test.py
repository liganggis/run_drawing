import osmnx as ox
import networkx as nx

ox.settings.bidirectional_network_types = ['all']

# simplify: 如果为真，使用simple_graph 函数简化图拓扑（纠正和简化网络拓扑）
# G1 = ox.graph_from_point((33.299896, -111.831638), dist=500, network_type='all', simplify=False)
# fig, ax = ox.plot_graph(G1, node_color='r')

# G1 = ox.graph_from_point((33.299896, -111.831638), dist=500, network_type='all_private', simplify=True)
# fig, ax = ox.plot_graph(G1, node_color='r')

# G2 = ox.graph_from_place('Guangzhou, Guangdong Province, China', network_type='walk', simplify=False)
G2 = ox.graph_from_bbox(30.5155000,30.4498000,114.3453000,114.2281000, network_type='walk', simplify=False) # maxlat, minlat, maxlon, minlon
# G2 = ox.graph_from_bbox(31.2827000,31.2221000,121.6700000, 121.5352000, network_type='all', simplify=False)
# G2 = ox.graph_from_xml('F:/MyData/华为未来终端实验室/第一阶段验收/yangli10-xian_changan.osm', bidirectional=True, simplify=False)
G2 = ox.distance.add_edge_lengths(G2, precision=3)
G2 = ox.add_edge_bearings(G2)
# ox.save_graph_xml(G2, 'D:/Users/OsmProject/Save/Xml/Wuhan_University.xml')

ox.plot_graph(G2)
# bearing1 = nx.get_edge_attributes(G2, 'bearing')
# print(bearing1[979935879, 1417702671, 0])
# G3.add_edge(9681727748, 9681727745, 0, color='red')

# 路网存储
# ox.save_graph_shapefile(G2, 'F:/MyData/华为未来终端实验室/GuangZhou')

# ox.save_graphml(G2, 'D:/Users/OsmProject/Save/GraphML/WuhanTest.graphml')



# 将同一个交叉口的所有nodes合并
# tolerance: 将这个范围内的node进行合并
# reconnect_edges：true表示根据新节点重建拓扑
# G = ox.graph_from_bbox(30.255904285611525,30.141649815003344,120.2972282472983, 120.2239798312058, network_type='drive')
# G = ox.projection.project_graph(G)
# G = ox.simplification.consolidate_intersections(G,tolerance=25, rebuild_graph=True, dead_ends=False, reconnect_edges=True)
# ox.plot_graph(G, node_color='r')

# ox.save_graph_shapefile(G, 'D:/Users/OsmProject/Save/Shp/after_consolidate_intersections.shp')
# ox.save_graph_geopackage(G, 'D:/Users/OsmProject/Save/GeoPackage/before_consolidate_intersections.gpkg')
# ox.save_graph_xml(G, 'D:/Users/OsmProject/Save/Xml/after_consolidate_intersections.xml')


