#!/usr/bin/env python

import math, os, re, shutil, subprocess, sys, tempfile

# MT-Diff: measure changes in segment-level quality between two systems
# according to BLEU and Meteor

bleu_script = os.path.abspath(os.path.join(os.path.dirname(__file__), \
  'files', 'mteval-v13m.pl'))
meteor_jar = os.path.abspath(os.path.join(os.path.dirname( \
  os.path.dirname(__file__)), 'meteor-1.4.jar'))

langs = 'en cz de es fr ar other'

labels = [(-1.0 + 0.1 * i, -0.9 + 0.1 * i) for i in range(20)]
labels.insert(10, (0, 0))

def main(argv):

    # Meteor jar check
    if not os.path.exists(meteor_jar):
        print 'Please edit the meteor_jar line of {0} to reflect the location of meteor-*.jar'.format(__file__)
        sys.exit(1)

    # Usage
    if len(argv[1:]) < 4:
        print 'usage: {0} <lang> <sys1.hyp> <sys2.hyp> <ref1> [ref2 ...]'. \
          format(argv[0])
        print 'langs: {0}'.format(langs)
        sys.exit(1)

    # Language
    lang = argv[1]
    if lang not in langs.split():
        print 'langs: {0}'.format(langs)
        sys.exit(1)

    # Files
    hyp1_file = argv[2]
    hyp2_file = argv[3]
    ref_files = argv[4:]

    # Work directory
    work_dir = tempfile.mkdtemp(prefix='mt-diff-')

    # SGML Files
    hyp1_sgm = os.path.join(work_dir, 'hyp1')
    hyp2_sgm = os.path.join(work_dir, 'hyp2')
    src_sgm = os.path.join(work_dir, 'src')
    ref_sgm = os.path.join(work_dir, 'ref')

    # Hyp1
    write_sgm(hyp1_file, hyp1_sgm, \
      '<tstset trglang="any" setid="any" srclang="any">', '</tstset>')

    # Hyp2
    write_sgm(hyp2_file, hyp2_sgm, \
      '<tstset trglang="any" setid="any" srclang="any">', '</tstset>')

    # Src (ref1)
    ref_len = write_sgm(ref_files[0], src_sgm, \
      '<srcset trglang="any" setid="any" srclang="any">', '</srcset>')

    # Ref (all refs)
    write_ref_sgm(ref_files, ref_sgm, \
      '<refset trglang="any" setid="any" srclang="any">', '</refset>')

    # BLEU    
    print 'BLEU scoring hyp1...'
    bleu1, bs1 = bleu(hyp1_sgm, ref_sgm, src_sgm, work_dir)
    print 'BLEU scoring hyp2...'
    bleu2, bs2 = bleu(hyp2_sgm, ref_sgm, src_sgm, work_dir)
    bleu_diff = diff_scr(bleu1, bleu2)
    bleu_dd = diff_dist(bleu_diff)

    # Meteor
    print 'Meteor scoring hyp1...'
    meteor1, ms1 = meteor(hyp1_sgm, ref_sgm, lang, work_dir)
    print 'Meteor scoring hyp2...'
    meteor2, ms2 = meteor(hyp2_sgm, ref_sgm, lang, work_dir)
    meteor_diff = diff_scr(meteor1, meteor2)
    meteor_dd = diff_dist(meteor_diff)

    # Header
    print ''
    print '+---------------------------------+'
    print '|    Segment Level Difference     |'
    print '+-------------+--------+----------+'
    print '|   Change    |  BLEU  |  Meteor  |'
    print '+-------------+--------+----------+'
    # Scores
    for (l, b, m) in zip(labels, bleu_dd, meteor_dd):
        if l == (0, 0):
            print '|     0.0     | {2:6} |   {3:6} |'.format(l[0], l[1], b, m)
        else:
            print '| {0:4} - {1:4} | {2:6} |   {3:6} |'.format(l[0], l[1], b, m)
    # Footer
    print '+-------------+--------+----------+'
    print '|  System2 +  | {0:6} |   {1:6} |'. \
      format(sum(bleu_dd[11:]), sum(meteor_dd[11:]))
    print '|  System2 -  | {0:6} |   {1:6} |'. \
      format(sum(bleu_dd[0:10]), sum(meteor_dd[0:10]))
    print '+-------------+--------+----------+'
    print '| # Segments  |      {0:6}       |'.format(ref_len)
    print '+-------------+-------------------+'
    print '|       System Level Score        |'
    print '+-------------+-------------------+'
    print '|   System1   | {0:0.4f} |   {1:0.4f} |'.format(bs1, ms1)
    print '|   System2   | {0:0.4f} |   {1:0.4f} |'.format(bs2, ms2)
    print '+-------------+--------+----------+'
    # Cleanup
    shutil.rmtree(work_dir)
    
def bleu(hyp, ref, src, work_dir=os.curdir):
    # Run BLEU
    bleu_cmd = ['perl', bleu_script, '-t', hyp, '-r', ref, '-s', src, '-b', \
      '--metricsMATR', '--no-norm']
    subprocess.Popen(bleu_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, \
      cwd=work_dir).wait()
    # Get scores from file
    seg = {}
    scr = open(os.path.join(work_dir, 'BLEU-seg.scr'))
    for line in scr:
        part = line.strip().split()
        seg['{0}:{1}'.format(part[2], part[3])] = float(part[4])
    scr.close()
    scr = open(os.path.join(work_dir, 'BLEU-sys.scr'))
    sys_s = float(scr.readline().split()[-1])
    scr.close()
    return (seg, sys_s)
 
def meteor(hyp, ref, lang='en', work_dir=os.curdir):
    # Run Meteor
    meteor_cmd = ['java', '-Xmx2G', '-jar', meteor_jar, hyp, ref, '-sgml', \
      '-l', lang]
    subprocess.Popen(meteor_cmd, stdout=subprocess.PIPE, \
      stderr=subprocess.PIPE, cwd=work_dir).wait()
    # Get scores from file
    seg = {}
    scr = open(os.path.join(work_dir, 'meteor-seg.scr'))
    for line in scr:
        part = line.strip().split()
        seg['{0}:{1}'.format(part[2], part[3])] = float(part[4])
    scr.close()
    scr = open(os.path.join(work_dir, 'meteor-sys.scr'))
    sys_s = float(scr.readline().split()[-1])
    scr.close()
    return (seg, sys_s)

def diff_scr(scr1, scr2):
    diff = []
    for key in scr1.keys():
        diff.append(scr2[key] - scr1[key])
    return diff

def diff_dist(diff):
    step = 0.1
    dist = [0] * 20
    zero = 0
    for d in diff:
        if d == 0:
            zero +=1
        else:
            dist[min(19, int(10 + d * 10))] += 1
    dist.insert(10, zero)
    return dist

def write_sgm(in_file, out_sgm, header, footer):
    file_in = open(in_file)
    file_out = open(out_sgm, 'w')
    print >> file_out, header
    print >> file_out, '<doc sysid="any" docid="any">'
    i = 0
    for line in file_in:
        i += 1
        print >> file_out, '<seg id="{0}"> {1} </seg>'.format(i, line.strip())
    print >> file_out, '</doc>'
    print >> file_out, footer
    file_in.close()
    file_out.close()
    return i

def write_ref_sgm(in_files, out_sgm, header, footer):
    file_out = open(out_sgm, 'w')
    print >> file_out, header
    sys_id = 0
    for in_file in in_files:
        sys_id += 1
        file_in = open(in_file)
        print >> file_out, '<doc sysid="{0}" docid="any">'.format(sys_id)
        i = 0
        for line in file_in:
            i += 1
            print >> file_out, '<seg id="{0}"> {1} </seg>'. \
              format(i, line.strip())
        print >> file_out, '</doc>'
        file_in.close()
    print >> file_out, footer
    file_out.close()

if __name__ == '__main__' : main(sys.argv)
