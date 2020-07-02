#!/usr/bin/env python

# Read Meteor output, write to WMT score format

import os, sys

def main(argv):
    
    if len(argv[1:]) < 3:
        print 'usage: {0} <lang-pair> <test-set> <system> [metric]'.format(
          argv[0])
        print 'writes metric.lang-pair.test-set.system.{seg.scr,sys.scr}'
        print ''
        print 'Pipe Meteor output to this script'
        sys.exit(1)
    
    lp = argv[1]
    ts = argv[2]
    s = argv[3]
    m = argv[4] if len(argv[1:]) > 3 else 'Meteor'
    
    seg_f = '{0}.{1}.{2}.{3}.seg.scr'.format(m, lp, ts, s)
    sys_f = '{0}.{1}.{2}.{3}.sys.scr'.format(m, lp, ts, s)
    
    stop = False
    if os.path.exists(seg_f):
        print 'exists: {0}'.format(seg_f)
        stop = True
    if os.path.exists(sys_f):
        print 'exists: {0}'.format(sys_f)
        stop = True
    if stop:
        sys.exit(1)
    
    seg_o = open(seg_f, 'w')
    sys_o = open(sys_f, 'w')
    
    while True:
        line = sys.stdin.readline()
        if not line:
            break
        if line.startswith('Segment'):
            f = line.split()
            print >> seg_o, '{0}\t{1}\t{2}\t{3}\t{4}\t{5}'.format(m, lp, ts, s,
              f[1], f[3])
        if line.startswith('Final score'):
            scr = line.split()[2]
            print >> sys_o, '{0}\t{1}\t{2}\t{3}\t{4}'.format(m, lp, ts, s,
              scr)

    seg_o.close()
    sys_o.close()
    
if __name__ == '__main__' : main(sys.argv)