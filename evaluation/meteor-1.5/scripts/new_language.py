#!/usr/bin/env python

import codecs
import glob
import gzip
import os
import shutil
import subprocess
import sys


METEOR_JAR = glob.glob(os.path.join(os.path.dirname(os.path.dirname(__file__)), 'meteor-*.jar'))[0]
JVM_SIZE = '12G'

def lower(in_f, out_f, limit=None):
    inp = gzip.open(in_f) if in_f.endswith('.gz') else open(in_f)
    out = gzip.open(out_f, 'w') if out_f.endswith('.gz') else open(out_f, 'w')
    i = 0
    for line in inp:
        i += 1
        out.write(line.decode('utf-8').lower().encode('utf-8'))
        if limit and i == limit:
            break
    inp.close()
    out.close()

def par_fmt(in_f, out_f):
    with gzip.open(out_f, 'wb') as out:
        for line in gzip.open(in_f):
            (p1, p2, prob) = line.strip().split(' ||| ')
            out.write('{}\n'.format(prob))
            out.write('{}\n'.format(p1))
            out.write('{}\n'.format(p2))

def main(argv):
    
    if len(argv[1:]) < 4:
        sys.stderr.write('usage: out-dir corpus.f corpus.e phrase-table.gz [target-corpus.e]\n'.format(argv[0]))
        sys.exit(2)

    (out_dir, corpus_f, corpus_e, pt) = argv[1:5]
    tgt_corpus_e = argv[5] if len(argv[1:]) == 5 else corpus_e

    if os.path.exists(out_dir):
        sys.stderr.write('{} exists, exiting.'.format(out_dir))
        sys.exit(1)
    os.mkdir(out_dir)

    # Lowercase inputs
    corpus_f_lc = os.path.join(out_dir, 'corpus.f')
    corpus_e_lc = os.path.join(out_dir, 'corpus.e')
    pt_lc = os.path.join(out_dir, 'pt.gz')
    tgt_corpus_e_lc = os.path.join(out_dir, 'tgt-corpus.e')

    sys.stderr.write('Preparing inputs:\n')
    sys.stderr.write('+ lc f\n')
    lower(corpus_f, corpus_f_lc)
    sys.stderr.write('+ lc e\n')
    lower(corpus_e, corpus_e_lc)
    sys.stderr.write('+ lc phrase table\n')
    lower(pt, pt_lc)
    sys.stderr.write('+ lc target e\n')
    lower(tgt_corpus_e, tgt_corpus_e_lc, 10000)

    sys.stderr.write('Running Parex:\n')
    par_dir = os.path.join(out_dir, 'parex')
    PAREX = ['java', '-XX:+UseCompressedOops', '-Xmx{}'.format(JVM_SIZE), '-cp', METEOR_JAR, 'Parex', corpus_f_lc, corpus_e_lc, pt_lc, tgt_corpus_e_lc, par_dir]
    subprocess.call(PAREX)

    sys.stderr.write('Copying files:\n')
    dir_files = os.path.join(out_dir, 'meteor-files')
    os.mkdir(dir_files)
    sys.stderr.write('+ function.words\n')
    shutil.copy(os.path.join(par_dir, 'parex.e'), os.path.join(dir_files, 'function.words'))
    sys.stderr.write('+ paraphrase.gz\n')
    par_fmt(os.path.join(par_dir, 'paraphrase.gz'), os.path.join(dir_files, 'paraphrase.gz'))
    sys.stderr.write('{} is now ready to passed to Meteor with -new flag\n'.format(dir_files))

if __name__ == '__main__':
    main(sys.argv)
