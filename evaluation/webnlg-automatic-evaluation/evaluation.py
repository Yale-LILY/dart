from benchmark_reader import Benchmark
from collections import defaultdict
from unidecode import unidecode
import re
import os
from natsort import natsorted
import sys

categories = ['Astronaut',
              'Airport',
              'Monument',
              'University',
              'Food',
              'SportsTeam',
              'City',
              'Building',
              'WrittenWork',
              'ComicsCharacter',
              'Politician',
              'Athlete',
              'MeanOfTransportation',
              'Artist',
              'CelestialBody']

new_categories = ['MeanOfTransportation', 'CelestialBody', 'Politician', 'Athlete', 'Artist']

old_categories = [item for item in categories if item not in new_categories]


def generate_files():
    # generate files per category
    for cat in categories:
        b = Benchmark()
        b.fill_benchmark([(path, goldfile)])
        # print(cat + ': ' + str(b.entry_count(cat=cat)))
        b_reduced = b.filter([], [cat])
        # print('reduced', b_reduced.entry_count(cat=cat))

        # metric files generation; we use three references
        bleu_ref_files_gen(b_reduced, cat)
        # meteor_ref_files_gen(b_reduced, cat)
        meteor_3ref_files_gen(b_reduced, cat)
        # ter_ref_files_gen(b_reduced, cat)
        ter_ref_files_gen(b_reduced, cat, True)
        # ter_3ref_space_files_gen(b_reduced, cat)

    # generate files per size
    for size in range(1, 8):
        b = Benchmark()
        b.fill_benchmark([(path, goldfile)])
        # print(str(size) + ': ' + str(b.entry_count(size=str(size))))
        b_reduced = b.filter([size], [])
        # print('reduced', b_reduced.entry_count(size=str(size)))
        bleu_ref_files_gen(b_reduced, str(size) + 'size')
        # meteor_ref_files_gen(b_reduced, str(size) + 'size')
        meteor_3ref_files_gen(b_reduced, str(size) + 'size')
        # ter_ref_files_gen(b_reduced, str(size) + 'size')
        ter_ref_files_gen(b_reduced, str(size) + 'size', True)
        # ter_3ref_space_files_gen(b_reduced, str(size) + 'size')

    # generate files per type: old, new, all categories
    b = Benchmark()
    b.fill_benchmark([(path, goldfile)])
    print('Gold count', b.entry_count())
    # metric files generation for all cats
    bleu_ref_files_gen(b, 'all-cat')
    ter_3ref_space_files_gen(b, 'all-cat')      # need this format for significance testing
    # meteor_ref_files_gen(b, 'all-cat')
    meteor_3ref_files_gen(b, 'all-cat')
    # ter_ref_files_gen(b, 'all-cat')
    ter_ref_files_gen(b, 'all-cat', True)

    b_reduced = b.filter([], new_categories)
    print('reduced (new)', b_reduced.entry_count())
    # metric files generation for new cats
    bleu_ref_files_gen(b_reduced, 'new-cat')
    ter_3ref_space_files_gen(b_reduced, 'new-cat')      # need this format for significance testing
    # meteor_ref_files_gen(b_reduced, 'new-cat')
    meteor_3ref_files_gen(b_reduced, 'new-cat')
    # ter_ref_files_gen(b_reduced, 'new-cat')
    ter_ref_files_gen(b_reduced, 'new-cat', True)

    bk = Benchmark()
    bk.fill_benchmark([(path, goldfile)])
    bk_reduced = bk.filter([], old_categories)
    print('reduced (old)', bk_reduced.entry_count())
    # metric files generation for old cats
    bleu_ref_files_gen(bk_reduced, 'old-cat')
    ter_3ref_space_files_gen(bk_reduced, 'old-cat')        # need this format for significance testing
    # meteor_ref_files_gen(bk_reduced, 'old-cat')
    meteor_3ref_files_gen(bk_reduced, 'old-cat')
    # ter_ref_files_gen(bk_reduced, 'old-cat')
    ter_ref_files_gen(bk_reduced, 'old-cat', True)


def bleu_ref_files_gen(b_reduced, param):
    ids_refs = defaultdict(list)
    for entry in b_reduced.entries:
        ids_refs[entry.id] = entry.lexs
    # length of the value with max elements
    max_refs = sorted(ids_refs.values(), key=len)[-1]
    # write references files for BLEU
    for j in range(0, len(max_refs)):
        with open('references/gold-' + param + '-reference' + str(j) + '.lex', 'w+') as f:
            out = ''
            # extract values sorted by key (natural sorting)
            values = [ids_refs[key] for key in natsorted(ids_refs.keys(), reverse=False)]
            for iter, ref in enumerate(values):
                try:
                    # detokenise
                    lex_detokenised = ' '.join(re.split('(\W)', ref[j].lex))
                    # delete redundant white spaces
                    lex_detokenised = ' '.join(lex_detokenised.split())
                    # convert to ascii and lowercase
                    out += unidecode(lex_detokenised.lower()) + '\n'
                except IndexError:
                    out += '\n'
                    lex_detokenised = ''
                id_str = str(iter + 1)
                '''with open('references/bleu_per_block/gold-' + param + '-reference' + str(j) + '-' + id_str + '.lex', 'w+') as f_item:
                    f_item.write(unidecode(lex_detokenised.lower()))'''
            f.write(out)


def meteor_ref_files_gen(b_reduced, param):
    # data for meteor
    # For N references, it is assumed that the reference file will be N times the length of the test file,
    # containing sets of N references in order.
    # For example, if N=4, reference lines 1-4 will correspond to test line 1, 5-8 to line 2, etc.

    ids_refs = {}
    for entry in b_reduced.entries:
        ids_refs[entry.id] = entry.lexs
    # length of the value with max elements
    max_refs = len(sorted(ids_refs.values(), key=len)[-1])
    with open('references/gold-' + param + '-reference.meteor', 'w+') as f:
        # extract values sorted by key (natural sorting)
        values = [ids_refs[key] for key in natsorted(ids_refs.keys(), reverse=False)]
        for ref in values:
            empty_lines = max_refs - len(ref)  # calculate how many empty lines to add (e.g. 8 max references)
            out = [lexicalis.lex for lexicalis in ref]
            out_clean = []
            for sentence in out:
                # detokenise
                sent_clean = ' '.join(re.split('(\W)', sentence))
                # delete redundant white spaces
                sent_clean = ' '.join(sent_clean.split())
                out_clean += [unidecode(sent_clean.lower())]
            f.write('\n'.join(out_clean) + '\n')
            if empty_lines > 0:
                f.write('\n' * empty_lines)


def meteor_3ref_files_gen(b_reduced, param):
    # data for meteor
    # For N references, it is assumed that the reference file will be N times the length of the test file,
    # containing sets of N references in order.
    # For example, if N=4, reference lines 1-4 will correspond to test line 1, 5-8 to line 2, etc.
    ids_refs = {}
    for entry in b_reduced.entries:
        ids_refs[entry.id] = entry.lexs
    # maximum number of references
    max_refs = 3
    with open('references/gold-' + param + '-reference-3ref.meteor', 'w+') as f:
        # extract values sorted by key (natural sorting)
        values = [ids_refs[key] for key in natsorted(ids_refs.keys(), reverse=False)]
        for ref in values:
            empty_lines = max_refs - len(ref)  # calculate how many empty lines to add (e.g. 3 max references)
            out = [lexicalis.lex for lexicalis in ref]
            out_clean = []
            for iter, sentence in enumerate(out):
                if iter < 3:
                    # detokenise
                    sent_clean = ' '.join(re.split('(\W)', sentence))
                    # delete redundant white spaces
                    sent_clean = ' '.join(sent_clean.split())
                    out_clean += [unidecode(sent_clean.lower())]
            f.write('\n'.join(out_clean) + '\n')
            if empty_lines > 0:
                f.write('\n' * empty_lines)


def ter_ref_files_gen(b_reduced, param, three_ref_only=False):
    # data for meteor
    # append (id1) to references
    out = ''
    for iter, entry in enumerate(b_reduced.entries):
        id_str = 'id' + str(iter + 1)
        for i, lex in enumerate(entry.lexs):
            # detokenise
            sent_clean = ' '.join(re.split('(\W)', lex.lex))
            # delete redundant white spaces
            sent_clean = ' '.join(sent_clean.split())
            if three_ref_only and i > 2:        # three references maximum
                break
            out += unidecode(sent_clean.lower()) + ' (' + id_str + ')\n'
    if not three_ref_only:
        with open('references/gold-' + param + '-reference.ter', 'w+') as f:
            f.write(out)
    else:
        with open('references/gold-' + param + '-reference-3ref.ter', 'w+') as f:
            f.write(out)


def ter_3ref_space_files_gen(b_reduced, param):
    # need this function for significance.py to treat ter files similar to meteor ones.
    # data for ter
    ids_refs = {}
    for entry in b_reduced.entries:
        ids_refs[entry.id] = entry.lexs
    # maximum number of references
    max_refs = 3
    with open('references/gold-' + param + '-reference-3ref-space.ter', 'w+') as f:
        # extract values sorted by key (natural sorting)
        values = [ids_refs[key] for key in natsorted(ids_refs.keys(), reverse=False)]
        for id_lex, ref in enumerate(values):
            empty_lines = max_refs - len(ref)  # calculate how many empty lines to add (e.g. 3 max references)
            out = [lexicalis.lex for lexicalis in ref]
            out_clean = []
            for iter, sentence in enumerate(out):
                if iter < 3:
                    # detokenise
                    sent_clean = ' '.join(re.split('(\W)', sentence))
                    # delete redundant white spaces
                    sent_clean = ' '.join(sent_clean.split())
                    out_clean += [unidecode(sent_clean.lower()) + ' (id' + str(id_lex + 1) + ')']
            f.write('\n'.join(out_clean) + '\n')
            if empty_lines > 0:
                f.write('\n' * empty_lines)


def read_participant(output_file, teamname):
    # read participant's outputs
    output = []
    with open(output_file, 'r') as f:
        output += [unidecode(line.strip()) for line in f]

    b = Benchmark()
    b.fill_benchmark([(path, goldfile)])

    # per size
    for size in range(1, 8):
        # print(size)
        # print('# of instances', b.entry_count(size=str(size)))
        entry_ids = []
        # look up id of a line in the gold benchmark, extract its size
        for entry in b.entries:
            if int(entry.size) == size:
                entry_ids += [int(entry.id[2:])]      # entry id -- 'Id1'
        output_reduced = [output[i-1] for i in sorted(entry_ids)]
        write_to_file(output_reduced, str(size)+'size', teamname)

    # per category
    for category in categories:
        # print(category)
        # print('# of instances', b.entry_count(cat=category))
        entry_ids = []
        # look up id of a line in the gold benchmark, extract its category
        for entry in b.entries:
            if entry.category == category:
                entry_ids += [int(entry.id[2:])]      # entry id -- 'Id1'
        output_reduced = [output[i-1] for i in sorted(entry_ids)]
        write_to_file(output_reduced, category, teamname)

    # old categories
    entry_ids = []
    for category in old_categories:
        # print('# of instances', b.entry_count(cat=category))
        # look up id of a line in the gold benchmark, extract its category
        for entry in b.entries:
            if entry.category == category:
                entry_ids += [int(entry.id[2:])]      # entry id -- 'Id1'
    output_reduced = [output[i-1] for i in sorted(entry_ids)]
    write_to_file(output_reduced, 'old-cat', teamname)

    # new categories
    entry_ids = []
    for category in new_categories:
        # print('# of instances', b.entry_count(cat=category))
        # look up id of a line in the gold benchmark, extract its category
        for entry in b.entries:
            if entry.category == category:
                entry_ids += [int(entry.id[2:])]      # entry id -- 'Id1'
    output_reduced = [output[i-1] for i in sorted(entry_ids)]
    write_to_file(output_reduced, 'new-cat', teamname)
    
    # create all-category files
    write_to_file(output, 'all-cat', teamname)
    print('Files creating finished for: ', teamname)


def write_to_file(output_reduced, param, teamname):
    out = ''
    out_ter = ''
    for iter, item in enumerate(output_reduced):
        # detokenise, lowercase, and convert to ascii
        # we do this to ensure consistency between participants
        lex_detokenised = ' '.join(re.split('(\W)', unidecode(item.lower())))
        # delete redundant white spaces
        lex_detokenised = ' '.join(lex_detokenised.split())
        out += lex_detokenised + '\n'
        out_ter += lex_detokenised + ' (id' + str(iter + 1) + ')\n'
        # for bleu significance write each output per file
        '''with open('teams/bleu_per_item/' + teamname + '_' + str(param) + '-' + str(iter + 1) + '.txt', 'w+') as f_item:
            f_item.write(lex_detokenised)'''

    with open('teams/' + teamname + '_' + str(param) + '.txt', 'w+') as f:
        f.write(out)
    with open('teams/' + teamname + '_' + str(param) + '_ter.txt', 'w+') as f:
        f.write(out_ter)

if __name__ == '__main__':
    # file with answers
    curdir = os.path.realpath(os.path.abspath(os.path.dirname(__file__)))
    path = os.path.join(curdir,'data-challenge')
    goldfile = 'testdata_with_lex.xml'
    # generate references
    generate_files()
    
    drctr = os.path.join(curdir,'submissions/')
    team = drctr + sys.argv[1]+'.txt'
    '''team1 = drctr + 'adaptcentre.txt'
    team2 = drctr + 'melbourne.txt'
    team3 = drctr + 'pkuwriter.txt'
    team4a = drctr + 'tilburg-nmt.txt'
    team4b = drctr + 'tilburg-smt.txt'
    team4c = drctr + 'tilburg-template.txt'
    team5 = drctr + 'uit-vietnam.txt'
    team6 = drctr + 'upf-forge.txt'
    team7 = drctr + 'BIU_Chimera_v1.txt'
    team8 = drctr + "BIU_Chimera_NaivePlan_r1.txt"
    team9 = drctr + "BIU_Chimera_NeuralPlan_r1.txt"
    baseline = drctr + "Baseline.txt"
    xg = drctr + 'xgbart.txt' '''
    read_participant(team, sys.argv[1])
    # generate teams
    '''read_participant(team1, 'ADAPT_Centre')
    read_participant(team2, 'GKB_Unimelb')
    read_participant(team3, 'PKUWriter')
    read_participant(team4a, 'Tilburg_University-1')        # nmt
    read_participant(team4b, 'Tilburg_University-2')        # smt
    read_participant(team4c, 'Tilburg_University-3')        # template
    read_participant(team5, 'UIT-DANGNT-CLNLP')
    read_participant(team6, 'UPF-TALN')
    read_participant(baseline, 'Baseline')
    #read_participant(team7, 'BIU_Chimera_v1')
    #read_participant(team8,'BIU_Chimera_NaivePlan_r1')
    #read_participant(team9,'BIU_Chimera_NeuralPlan_r1')
    read_participant(xg,'xgbart')'''

