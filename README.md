## Efficient path planning
### Uniform-cost search (UCS)
UCS is similar to breadth-first search, except it uses a priority queue. Breadth-first search only finds the optimal (lowest-cost) path between any two states if all actions have uniform cost. Uniform cost search, by contrast, can find the optimal (lowest-cost) path between any two states, even if the actions do not have uniform cost. 

### A* search
A* search is a generalization of UCS. Instead of using a priority queue that sorts by cost, it uses one that sorts by cost+heuristic. The heuristic is an underestimate of the remaining cost to reach the goal. This heuristic is fast to compute, but also provides enough information to speed up the search. There are faster ways to underestimate the remaining cost. 

## How to run
```bash
$ bash build.bash
$ java Agent
```
When the user left-clicks, use UCS. When the user right-clicks, use A*. 
