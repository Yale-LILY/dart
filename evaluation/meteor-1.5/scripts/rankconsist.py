#!/usr/bin/env python

import collections
import sys

def main(argv):
    
    if len(argv[1:]) != 2:
        sys.stderr.write('usage: {} scr rank\n'.format(argv[0]))
        sys.exit(2)

    scr = collections.defaultdict(lambda: collections.defaultdict(dict))

    for line in open(argv[1]):
        # Meteor en-ru newstest2013 balagur.2693 5 0.373206146468917
        (metric, lp, testset, system, id, score) = line.strip().split()
        system = system.split('.')[0]
        score = float(score)
        scr[lp][system][id] = score

    conc = 0
    disc = 0

    for line in open(argv[2]):
        # 1018 cs-en cu-bojar cs-en jhu-heiro
        (id, lp1, sys1, lp2, sys2) = line.strip().split()
        if scr[lp1][sys1][id] > scr[lp2][sys2][id]:
            conc += 1
        else:
            disc += 1

    tau = float(conc - disc) / (conc + disc)
    sys.stderr.write('Tau: {} ({}/{})\n'.format(tau, conc, conc + disc))

if __name__ == '__main__':
    main(sys.argv)
