#!/usr/bin/env python

# Score with BLEU, produce WMT score files

import os, subprocess, sys

def main(argv):
    
    if len(argv[1:]) < 5:
        print 'usage: {0} <test> <ref> <lang-pair> <test-set> <system>'.format(
          argv[0])
        print 'writes bleu.lang-pair.test-set.system.{seg.scr,sys.scr}'
        sys.exit(1)
    
    BLEU = [os.path.join(os.path.dirname(__file__), 'bleu.py')]
    tst = argv[1]
    ref = argv[2]
    lp = argv[3]
    ts = argv[4]
    s = argv[5]
    
    seg_f = 'bleu.{}.{}.{}.seg.scr'.format(lp, ts, s)
    sys_f = 'bleu.{}.{}.{}.sys.scr'.format(lp, ts, s)
    
    stop = False
    if os.path.exists(seg_f):
        print 'exists: {}'.format(seg_f)
        stop = True
    if os.path.exists(sys_f):
        print 'exists: {}'.format(sys_f)
        stop = True
    if stop:
        sys.exit(1)
    
    seg_o = open(seg_f, 'w')
    sys_o = open(sys_f, 'w')
    
    BLEU.append(tst)
    BLEU.append(ref)
    p = subprocess.Popen(BLEU, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    i = 0
    while True:
        line = p.stderr.readline()
        i += 1
        if not line:
            break
        print >> seg_o, 'bleu\t{}\t{}\t{}\t{}\t{}'.format(lp, ts, s, i, line.strip())
    line = p.stdout.readline()
    print >> sys_o, 'bleu\t{}\t{}\t{}\t{}'.format(lp, ts, s, line.strip())

    seg_o.close()
    sys_o.close()
    
if __name__ == '__main__' : main(sys.argv)
