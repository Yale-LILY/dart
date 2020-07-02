#!/usr/bin/env python

# Filter and merge multiple rank training sets into a single set.  Segments are
# relabeled by original data set and renumbered after filtering.  The resulting
# combined set contains only segments for which rank judgments exist.

# Filtering is highly recommended even for single sets to greatly reduce
# training time.

import os, re, sys

def main(argv):
    
    if len(argv[1:]) < 2:
        print 'usage: {0} <clean-dir> <file1.rank> [file2.rank ...]'.format(argv[0])
        print 'Rank files should have same basename (src-tgt.rank)'
        print 'Original test sets identified with ~n'
        exit(1)
    
    clean_dir = argv[1]
    r_files = argv[2:]
    
    if clean_dir == os.path.dirname(os.path.abspath(r_files[0])):
        print 'This is a bad idea.  Please specify a different clean-dir.'
        sys.exit(1)
    
    # Single rank file
    r_out = open(os.path.join(clean_dir, os.path.basename(r_files[0])),
          'w')
    
    r_n = 0
    id = 0
    
    # For each rank file
    for r_file in r_files:
        
        r_n += 1
        
        # Renumber segments in rank file, keep order
        seg = {}
        r_in = open(r_file, 'r')
        for line in r_in:
            f = line.split()
            if f[0] not in seg:
                id += 1
                seg[f[0]] = id
            # Append rank set numbers to system names
            print >> r_out, '{0}\t{1}\t{2}\t{3}\t{4}'.format(seg[f[0]],
              append_n(f[1], r_n), f[2], append_n(f[3], r_n), f[4])
        r_in.close()
        
        r_base = os.path.basename(os.path.abspath(r_file))
        prefix = r_base[0:r_base.find('.')]
        f_dir = os.path.dirname(os.path.abspath(r_file))
        
        # Filter and renumber segments in system outputs and ref file
        for sgm_file in os.listdir(f_dir):
            if not (sgm_file.startswith(prefix) and sgm_file.endswith('.sgm')):
                continue
            sgm_in = open(os.path.join(f_dir, sgm_file), 'r')
            # Append rank set numbers to system names
            sgm_out = open(os.path.join(clean_dir,
              append_n(sgm_file, r_n)), 'w')
            for line in sgm_in:
                r = re.search(u'^<seg id="([0-9]+)">', line, re.I)
                if not r:
                    print >> sgm_out, line.strip()
                    continue
                if r.group(1) in seg:
                    print >> sgm_out, re.sub(u'^<seg id="[0-9]+">',
                      '<seg id="{0}">'.format(seg[r.group(1)]), line).strip()
            sgm_in.close()
            sgm_out.close()
    
    # Finished writing rank file
    r_out.close()

# Append set number to appropriate location
def append_n(s, n):
    i = s.find('.')
    if i == -1:
        i = len(s)
    return '{0}~{1}{2}'.format(s[0:i], n, s[i:])

if __name__ == '__main__' : main(sys.argv)
