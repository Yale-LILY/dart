#!/usr/bin/env python

import os, subprocess, shutil, sys, tempfile

TERCOM = os.path.join(os.path.dirname(__file__), 'tercom-0.8.0.jar')

def main(argv):

    if len(argv[1:]) < 2:
        print >> sys.stderr, 'Usage: {0} hyps refs [--no-norm] [--char]'.format(argv[0])
        print >> sys.stderr, 'Segment scores to stderr, final to stdout'
        sys.exit(1)

    norm = '-s' if '--no-norm' in argv[3:] else '-N'
    char = '--char' in argv[3:]

    work = tempfile.mkdtemp(prefix='ter.')
    
    hyps = os.path.join(work, 'hyps')
    mktrans(argv[1], hyps, char)
    
    refs = os.path.join(work, 'refs')
    mktrans(argv[2], refs, char)
    
    out = open(os.path.join(work, 'out'), 'w')
    err = open(os.path.join(work, 'err'), 'w')
    tab = os.path.join(work, 'ter')
    p = subprocess.Popen(['java', '-jar', TERCOM, '-h', hyps, '-r', refs, '-o',
      'sum', '-n', tab, norm], stdout=out, stderr=err)
    p.wait()
    out.close()
    err.close()
    
    t = open(tab + '.sum')
    while True:
        line = t.readline()
        if line.startswith('Sent Id'):
            t.readline()
            break
    while True:
        line = t.readline()
        if line.startswith('---'):
            break
        print >> sys.stderr, line.split()[-1]
    print t.readline().split()[-1]
        
    shutil.rmtree(work)
    
def mktrans(f, tmp, char=False):
    o = open(tmp, 'w')
    i = 0
    for line in open(f, 'r'):
        i += 1
        if char:
            line = ' '.join([ch for ch in line if ch != ' '])
        print >> o, '{0}  ({1})'.format(line.strip(), i)
    o.close()
    
if __name__ == '__main__' : main(sys.argv)
