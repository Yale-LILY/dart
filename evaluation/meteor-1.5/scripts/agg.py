#!/usr/bin/env python

# Aggregate: sum input lines by column.  Useful for aggregating
# MeteorStats lines as a MERT implementation would.

from sys import argv, exit, stdin

parse = int

if len(argv) > 1:
    if argv[1].startswith('-h'):
        print 'usage: agg [-f] FILE'
        exit()
    if argv[1] == '-f':
        parse = float
    else:
        stdin = open(argv[1], 'r')
    if len(argv) > 2:
        stdin = open(argv[2], 'r')

agg = None

while True:
    line = stdin.readline()
    if not line:
        break
    f = line.split()
    if agg == None:
        agg = [0] * len(f)
    if len(f) != len(agg):
        print 'error: number of columns not constant'
        exit(1)
    for i in range(len(agg)):
        agg[i] += parse(f[i])

if agg:
    print ' '.join([str(x) for x in agg])

stdin.close()
