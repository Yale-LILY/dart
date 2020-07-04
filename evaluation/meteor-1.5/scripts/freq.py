#!/usr/bin/env python

# Simple word relative frequency counter.  Used to create
# function word lists.

from sys import stdin, argv

freq = {}
total = 0

if argv[1:]:
    stdin = open(argv[1], 'r')

while True:
    line = stdin.readline()
    if not line:
        break
    f = line.split()
    for w in f:
        freq[w] = 1 if w not in freq else freq[w] + 1
        total += 1

for w in sorted(freq, cmp=lambda x,y: freq[y] - freq[x]):
    print w, float(freq[w]) / total
