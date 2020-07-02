#!/usr/bin/env python

import re, sys

DEFAULT_DIST = 0.0
DEFAULT_LEN = 0

def main(argv):

    # Directions
    if len(argv[1:]) < 1:
        sys.stderr.write('Using defaults - for help, use {0} -h\n'.format(argv[0]))
    
    min_dist = DEFAULT_DIST
    min_len = DEFAULT_LEN
    words = []

    # help or min distance
    if len(argv[1:]) > 0:
        if argv[1] in '--help':
            print 'Delete single matches to improve monotonicity of alignments'
            print ''
            print 'usage:', argv[0], 'min_rel_dist', 'min_seg_len', \
              'word_list', '<', 'matcher.out', '>', 'matcher.out.mon'
            print ''
            print 'min_rel_dist - minimum relative distance for deletion' + \
              '(default = X)'
            print 'min_seg_len  - minimum segment length (reference) to' + \
              'consider (default = X)'
            print 'word_list    - file of words, one per line, to consider' + \
              'for deletion (default = all words)'
            sys.exit()
        else:
            min_dist = float(argv[1])

    # min length
    if len(argv[1:]) > 1:
        min_len = int(argv[2])

    # word list
    if len(argv[1:]) > 2:
        words_in = open(argv[3])
        for line in words_in:
            words.append(line.strip().split()[0])
        words_in.close()

    # Read alignments
    while True:
        # Next line should be 'Alignment...'
        line = sys.stdin.readline()
        # End of file
        if not line:
            break
        if not line.startswith('Alignment'):
            print 'Error: file does not start with Alignment line'
            print 'Please use exact output of Matcher'
            sys.exit(1)   
        print line,
        sen1 = sys.stdin.readline()
        words1 = sen1.split()
        print sen1,
        sen2 = sys.stdin.readline()
        words2 = sen2.split()
        print sen2,
        print sys.stdin.readline(),
        # Read matches
        match_words2 = []
        match_words1 = []
        match_start2 = []
        match_start1 = []
        match_len2 = []
        match_len1 = []
        mods = []
        scores = []
        while True:
            line = sys.stdin.readline()
            if not line.strip():
                break
            m2, m1, mod, score = line.split()
            m2_s, m2_l = map(int, m2.split(':'))
            match_start2.append(m2_s)
            match_len2.append(m2_l)
            match_words2.append(words2[m2_s : m2_s + m2_l])
            m1_s, m1_l = map(int, m1.split(':'))
            match_start1.append(m1_s)
            match_len1.append(m1_l)
            match_words1.append(words1[m1_s : m1_s + m1_l])
            mods.append(mod)
            scores.append(score)
        # For sentences minimum length or above that have more than one match
        if len(words2) >= min_len and len(mods) > 1:
            # Look for stray matches
            for i in range(len(mods)):
                # Phrase matches safe
                if match_len1[i] > 1 or match_len2[i] > 1:
                    continue
                # Words not on list safe
                if words:
                    if words2[match_start2[i]] not in words \
                      and words1[match_start1[i]] not in words:
                        continue
                # Distance from monotonicity with previous match
                if i == 0:
                    dist_prev = 0
                else:
                    dist_prev = abs((match_start1[i] - match_start1[i - 1]) \
                      - (match_start2[i] - match_start2[i - 1]))
                # Distance from monotonicity with next match
                if i == len(mods) - 1:
                    dist_next = 0
                else:
                    dist_next = abs((match_start1[i + 1] - match_start1[i]) \
                      - (match_start2[i + 1] - match_start2[i]))
                # Anchored matches safe
                if i != 0 and dist_next == 0:
                        continue
                if i != len(mods) - 1 and dist_prev == 0:
                    continue
                # Total jump distance
                dist = min(dist_prev, dist_next)
                # Delete if exceeds threshold
                if float(dist) / len(words2) >= min_dist:
                    mods[i] = -1 # dist / len(words2)

        # Write new match lines
        for i in range(len(mods)):
            print '{0}:{1}\t\t\t{2}:{3}\t\t\t{4}\t\t{5}'.format( \
              match_start2[i], match_len2[i], match_start1[i], match_len1[i], \
              mods[i], scores[i])
        print ''

if __name__ == '__main__' : main(sys.argv)
