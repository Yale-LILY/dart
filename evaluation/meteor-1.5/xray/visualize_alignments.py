#!/usr/bin/env python

import sys

from MeteorAlignment import *
from Generation import *

def main(argv):

    if not check_xelatex():
        sys.exit()

    if len(argv[1:]) < 2:
        print 'usage: {0} <align.out> <prefix> [max]'.format(argv[0])
        print 'writes: <prefix>.pdf, <prefix>.tex'
        print 'max determines max number of alignments to visualize'
        sys.exit()

    align_file = argv[1]
    prefix = argv[2]
    max_align = int(argv[3]) if len(argv[1:]) > 2 else -1
    
    pdf_file = prefix + '.pdf'
    tex_file = prefix + '.tex'

    alignments = read_align_file(align_file, max_align=max_align, a_type=ALIGN_DEFAULT)

    tex_out = open(tex_file, 'w')
    print >> tex_out, DEC_HEADER1
    print >> tex_out, get_font(True)
    print >> tex_out, DEC_HEADER2
    print >> tex_out, DOC_HEADER_ALIGN
    for i in range(len(alignments)):
        a = alignments[i]
        if not check_printable(a):
            continue
        print_align_table(tex_out, a, a_type=ALIGN_DEFAULT)
    # Print footer
    print >> tex_out, DOC_FOOTER
    # Close file
    tex_out.close()
    print >> sys.stderr, \
      'Compiling {0} - this may take a few minutes...'.format(pdf_file)
    xelatex(tex_file, pdf_file)

if __name__ == '__main__' : main(sys.argv)
