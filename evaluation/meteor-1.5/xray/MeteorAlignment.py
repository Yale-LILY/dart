import math

ALIGN_DEFAULT = 1
ALIGN_METEOR = 2

MATCH_TYPES = ['ex', 'ap', 'ap', 'ap', 'rm']
NO_MATCH = 'blank'

class ScoredAlignment(object):

    name = ''
    sen1 = []
    sen2 = []
    p = 0.0
    r = 0.0
    frag = 0.0
    score = 0.0
    matrix = [[]]
    sen1_matched = []

    def __init__(self, align_in=None, a_type=None):
        if align_in and a_type:
            self.read_alignment(align_in, a_type)

    def read_alignment(self, align_in, a_type=ALIGN_DEFAULT):
        '''Read next alignment from an input stream
        '''

        # Read next line
        line = align_in.readline()
        if not line:
            return

        # Line should be 'Alignment...'
        if not line.startswith('Alignment'):
            print 'Error: alignment does not start with Alignment line'
            return

        # Alignment name
        f = line.split()
        if a_type == ALIGN_METEOR:
            # Name tokens
            self.name = '\t'.join(f[1:-4])
            # P R Fr Sc
            self.p, self.r, self.frag, self.score = map(float, f[-4:])
        else:
            self.name = line.strip()

        # Sentence words    
        self.sen1 = align_in.readline().split()
        self.sen2 = align_in.readline().split()

        # Matrix
        self.matrix = []
        self.sen1_matched = []
        for w1 in self.sen1:
            row = []
            for w2 in self.sen2:
                row.append('')
            self.matrix.append(row)
            self.sen1_matched.append(NO_MATCH)

        # discard header 'Line2Start...'
        align_in.readline()

        # Read matches
        while True:
            line = align_in.readline()
            if not line.strip():
                break
            m2, m1, mod_name, s = line.split()
            m2_s, m2_l = map(int, m2.split(':'))
            m1_s, m1_l = map(int, m1.split(':'))
            mod = int(mod_name)
            for i in range(m1_l):
                self.sen1_matched[m1_s + i] = m2_s
                for j in range(m2_l):
                    self.matrix[m1_s + i][m2_s + j] = MATCH_TYPES[mod]

    # Reverse sentence 2 and alignment to render right to left
    def rtl(self):
        self.sen2.reverse()
        for x in self.matrix:
            x.reverse()
        self.sen1_matched.reverse()


class ScoredSegment(object):
    
    sen_len = 0
    p = 0.0
    r = 0.0
    frag = 0.0
    score = 0.0

    def __init__(self, sen_len, p, r, frag, score):
        self.sen_len = sen_len
        self.p = p
        self.r = r
        self.frag = frag
        self.score = score

def extract_scores(alignments):
    scores = []
    for align in alignments:
        scores.append(ScoredSegment(len(align.sen2), align.p, align.r, \
          align.frag, align.score))
    return scores

def read_align_file(align_file, max_align=-1, a_type=ALIGN_METEOR):
    a_in = open(align_file)
    alignments = []
    count = 0
    while True:
        if max_align != -1 and count >= max_align:
            break
        count += 1
        a = ScoredAlignment(a_in, a_type)        
        if not a.name:
            break
        alignments.append(a)
    return alignments

def cmp_score_best(x, y):
    diff = (x[0].score - x[1].score) - (y[0].score - y[1].score)
    return -1 if diff > 0 else 1 if diff < 0 else 0

def cmp_score_diff(x, y):
    diff = abs(x[0].score - x[1].score) - abs(y[0].score - y[1].score)
    return 1 if diff > 0 else -1 if diff < 0 else 0

def cmp_score(x, y):
    diff = x.score - y.score
    return 1 if diff > 0 else -1 if diff < 0 else 0

def get_score_dist(scores, size=10):
    step = 1.0 / size
    dist = [0] * size
    for s in [abs(x) for x in scores]:
        if math.isnan(s):
            dist[0] += 1
            continue
        dist[min(size - 1, int(math.floor(float(s) / step)))] += 1
    return dist
