# Road network graphic retrieval for run drawing
The code of single graphic and combination graphic retrieval algorithm.
## Single graphic retrieval algorithm
* Code runs in java 1.8 + IDEA environment
* Code entry is `run.java`
* Road network data is in the path `data/graphml/`
* The graphic template is in the path `data/dataunit/Final/`
* The graphic search results are in the path `data/OutTest/`

## Combination graphic retrieval algorithm
* Code runs in python 3.6 + VSCode environment
* `python computeRectTest2.py` calculates minimum rotation of the outer rectangle and center point
* `python combinationTest2.py IHy | 1314 | 520 | LOVE` for combinatorial graphic retrieval
* `python plotForArticle.py IHy | 1314 | 520 | LOVE` for visualizing search results for combination graphics