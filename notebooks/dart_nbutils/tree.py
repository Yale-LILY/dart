import json
from anytree import Node, RenderTree

def print_tree(root):
    for pre, fill, node in RenderTree(root):
        predicate = node.predicate + ':' if node.predicate is not None else ''
        print(f"{pre}{predicate}{node.name}")


def generate_tree(tripleset):
    edges = []
    for triple in tripleset['tripleset']:
        E, P, V = triple
        edges.append((E, (P, V)))
    nodes = {}
    for x, yz in edges:
        if x not in nodes:
            nodes[x] = Node(x, parent=None, predicate=None)
        y, z = yz
        if z in nodes:
            assert nodes[z].predicate is None
            assert nodes[z].parent is None
            nodes[z].predicate = y
            nodes[z].parent = nodes[x]
        else:
            nodes[z] = Node(z, predicate=y, parent=nodes[x])
    roots = [n for n in nodes.values() if n.is_root]
    assert len(roots) == 1
    return roots[0]
