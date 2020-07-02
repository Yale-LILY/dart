#!/usr/bin/env python

# Convert to and from SGML easily.  There exist many SGML/XML standards
# for MT evaulation.  This script produces files in a format compatible
# with meteor-*.jar, mteval-v*.pl, and tercom.*.jar

import codecs, re, sys

sys.stdin = codecs.getreader('utf-8')(sys.stdin)
sys.stdout = codecs.getwriter('utf-8')(sys.stdout)

def main(argv):

    if len(argv[1:]) < 1:
        print 'SGMLize'
        print 'Do you want a [s]rc, [t]est, [r]eference, or [p]laintext?'
        print 'usage: {0} (s|t|r|p) < <textfile>'.format(argv[0])
        print 'ex:    {0} t < sys-output.txt > sys-output.sgm'.format(argv[0])
        sys.exit(1)
    
    t_type = argv[1]
    
    if t_type not in ['s', 't', 'r', 'p']:
        print 'usage: {0} (s|t|r|p) < <textfile>'.format(argv[0])
        sys.exit(1)
    
    if t_type == 'p':
        while True:
            line = sys.stdin.readline()
            if not line:
                break
            r = re.search(u'<seg[^>]+>\s*(.*\S)\s*<.seg>', line, re.I)
            if r:
                print unescape(r.group(1))
        return

    tag = 'srcset' if t_type == 's' else 'tstset' if t_type == 't' else 'refset'
    seg = 0

    print u'<{0} trglang="any" setid="any" srclang="any">'.format(tag)
    print u'<doc docid="any" sysid="sys">'
    while True:
        line = sys.stdin.readline()
        if not line:
            break
        seg += 1
        print u'<seg id="{0}"> {1} </seg>'.format(seg, escape(line.strip()))
    print u'</doc>'
    print u'</{0}>'.format(tag)

def escape(s):
    return s.replace('&', '&amp;').replace('"', '&quot;').replace('\'', '&apos;'). \
      replace('<', '&lt;').replace('>', '&gt;')

def unescape(s):
    return s.replace('&quot;', '"').replace('&apos;', '\'').replace('&lt;', '<'). \
      replace('&gt;', '>').replace('&amp;', '&')

if __name__ == '__main__': main(sys.argv)
