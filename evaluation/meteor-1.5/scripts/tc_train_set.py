#!/usr/bin/env python

import os
import sys

ID = 'ID'
MT = 'MT'
SCORES = ('HTER', 'Rating')
NORM_SCORES = ('Keypress', 'Edits', 'Time')
PAUSE_SCORES = ('APR', 'PWR')

class DataSet:

    def __init__(self, dir):
        os.mkdir(dir)
        self.tst = open(os.path.join(dir, 'corpus.tst'), 'w')
        self.ref = open(os.path.join(dir, 'corpus.ref'), 'w')
        self.ter = open(os.path.join(dir, 'corpus.ter'), 'w')
        self.tst.write('<tstset trglang="any" setid="any" srclang="any">\n<doc docid="any" sysid="sys">\n')
        self.ref.write('<refset trglang="any" setid="any" srclang="any">\n<doc docid="any" sysid="sys">\n')
        self.i = 0

    def add(self, hyp, ref, score):
        self.i += 1
        self.tst.write('<seg id="{}"> {} </seg>\n'.format(self.i, hyp))
        self.ref.write('<seg id="{}"> {} </seg>\n'.format(self.i, ref))
        self.ter.write('any {} {}\n'.format(self.i, score))

    def close(self):
        self.tst.write('</doc>\n</tstset>\n')
        self.ref.write('</doc>\n</tstset>\n')
        self.tst.close()
        self.ref.close()
        self.ter.close()


def main():

    if len(sys.argv[1:]) < 3:
        sys.stderr.write('Create Meteor training sets from TransCenter reports and pre-generated (standard) references\n')
        sys.stderr.write('usage: {} [out-dir] report-dir1 ref1 [report-dir2 ref2 ...]\n'.format(sys.argv[0]))
        sys.exit(2)

    out_dir = os.path.abspath(sys.argv[1])
    if os.path.exists(out_dir):
        sys.stderr.write('{} exists, exiting.\n'.format(out_dir))
        sys.exit(1)
    os.mkdir(out_dir)

    # Open streams
    hyps = open(os.path.join(out_dir, 'corpus.hyps'), 'w')
    refs = open(os.path.join(out_dir, 'corpus.refs'), 'w')
    data = {}
    for label in SCORES + NORM_SCORES + PAUSE_SCORES:
        data[label] = DataSet(os.path.join(out_dir, label))

    # Scan input directories
    dirs = [sys.argv[i] for i in range(2, len(sys.argv), 2)]
    ref_files = [sys.argv[i] for i in range(3, len(sys.argv), 2)]
    for (dir, rf) in zip((os.path.abspath(dir) for dir in dirs), (os.path.abspath(rf) for rf in ref_files)):
        sys.stderr.write('{} ({})\n'.format(dir, rf))
        ref_lines = [line.strip() for line in open(rf)]
        ref_lens = [len(line.split()) for line in ref_lines]
        mt_lines = []
        for f in os.listdir(dir):
            # Find everything else from summary files
            if f.startswith('summary.') and f.endswith('.csv'):
                user = f[len('summary.'):len(f)-len('.csv')]
                csv = open(os.path.join(dir, f))
                sys.stderr.write('+ {}\n'.format(user))
                headers = dict((y, x) for (x, y) in enumerate(csv.readline().strip().split('\t')))
                for line in csv:
                    fields = line.strip().split('\t')
                    id = int(fields[headers[ID]])
                    mt = fields[headers[MT]]
                    # Keep for other files
                    mt_lines.append(mt)
                    # Add to master list
                    hyps.write('{}\n'.format(mt))
                    refs.write('{}\n'.format(ref_lines[id-1]))
                    # Add raw scores to data
                    for label in SCORES:
                        data[label].add(mt, ref_lines[id-1], fields[headers[label]])
                    # Length-norm scores
                    for label in NORM_SCORES:
                        data[label].add(mt, ref_lines[id-1], float(fields[headers[label]]) / ref_lens[id-1])
                # Corresponding pause file
                csv = open(os.path.join(dir, 'pause.{}.csv'.format(user)))
                headers = dict((y, x) for (x, y) in enumerate(csv.readline().strip().split('\t')))
                for line in csv:
                    fields = line.strip().split('\t')
                    id = int(fields[headers[ID]])
                    for label in PAUSE_SCORES:
                        data[label].add(mt_lines[id-1], ref_lines[id-1], fields[headers[label]])
    # Close streams
    hyps.close()
    refs.close()
    for label in SCORES + NORM_SCORES + PAUSE_SCORES:
        data[label].close()

if __name__ == '__main__':
    main()
