#!/usr/bin/env python

import sys

LANGS = {
        'cs': 'Czech',
        'en': 'English',
        'es': 'Spanish',
        'de': 'German',
        'fr': 'French',
        'hi': 'Hindi',
        'ru': 'Russian',
        }

N_SYSTEMS = 5

def main(argv):
    
    if len(argv[1:]) != 1:
        sys.stderr.write('usage: {} fr-en <wmt-data.csv >fr-en.rank\n'.format(argv[0]))
        sys.exit(2)

    # Language pair
    lp = argv[1]
    (l1, l2) = (LANGS[l] for l in lp.split('-'))

    # Read header
    names = dict((k, v) for (v, k) in enumerate(sys.stdin.readline().strip().split(',')))
    src = names['srclang']
    tgt = names['trglang']
    idx = names['srcIndex']
    systems = [names['system{}Id'.format(i)] for i in range(1, N_SYSTEMS + 1)]
    ranks = [names['system{}rank'.format(i)] for i in range(1, N_SYSTEMS + 1)]
    
    # Find matching lines
    for line in sys.stdin:
        fields = line.strip().split(',')
        if fields[src] != l1 or fields[tgt] != l2:
            continue
        ranked = []
        for (system, rank) in zip(systems, ranks):
            s = fields[system]
            r = int(fields[rank])
            # Skip blank
            if r != -1:
                ranked.append((r, s))
        # Sort by rank, lowest to highest
        ranked.sort()
        # Unroll to binary judgments
        for i in range(len(ranked)):
            for j in range(i + 1, len(ranked)):
                # Skip ties
                if ranked[i][0] < ranked[j][0]:
                    sys.stdout.write('{id}\t{lp}\t{sys1}\t{lp}\t{sys2}\n'.format(id=fields[idx], lp=lp, sys1=ranked[i][1], sys2=ranked[j][1]))

if __name__ == '__main__':
    main(sys.argv)

