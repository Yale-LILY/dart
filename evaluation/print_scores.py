import statistics
import sys
if __name__ == '__main__':
    scoresf = sys.argv[1]
    with open(scoresf) as f:
        scores = [float(s) for s in f.readlines()]
    print(statistics.mean(scores))
