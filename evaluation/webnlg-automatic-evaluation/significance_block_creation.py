import random


# shuffle always the same
random.seed(5)

# create shuffle indices for all, old, new, and sample
index_shuf_all = list(range(1, 1863))
index_shuf_old = list(range(1, 972))
index_shuf_new = list(range(1, 892))

random.shuffle(index_shuf_all)
random.shuffle(index_shuf_old)
random.shuffle(index_shuf_new)



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


teams = ['ADAPT_Centre',
         'GKB_Unimelb',
         'PKUWriter',
         'Tilburg_University-1',
         'Tilburg_University-2',
         'Tilburg_University-3',
         'UIT-DANGNT-CLNLP',
         'UPF-TALN',
         'Baseline']


def randomise_data(filelines, param):
    # randomise; sort list based on numbers from another list
    if param == 'all-cat':
        filelines = [x for _, x in sorted(zip(index_shuf_all, filelines))]
    elif param == 'old-cat':
        filelines = [x for _, x in sorted(zip(index_shuf_old, filelines))]
    elif param == 'new-cat':
        filelines = [x for _, x in sorted(zip(index_shuf_new, filelines))]
    return filelines


def randomise_meteor_ter_data(filelines, param):
    # randomise; keep every three lines frozen;
    # match each shuffle index to three corresponding lines
    filelines_randomised = []
    if param == 'all-cat':
        filelines_per_3 = [filelines[i:i + 3] for i in range(0, len(filelines), 3)]
        # flat three references
        filelines_randomised = [ref for _, x in sorted(zip(index_shuf_all, filelines_per_3)) for ref in x]
        print('')
    elif param == 'old-cat':
        filelines_per_3 = [filelines[i:i + 3] for i in range(0, len(filelines), 3)]
        # flat three references
        filelines_randomised = [ref for _, x in sorted(zip(index_shuf_old, filelines_per_3)) for ref in x]
    elif param == 'new-cat':
        filelines_per_3 = [filelines[i:i + 3] for i in range(0, len(filelines), 3)]
        # flat three references
        filelines_randomised = [ref for _, x in sorted(zip(index_shuf_new, filelines_per_3)) for ref in x]
    return filelines_randomised


def metric_create_blocks():
    params = ['all-cat', 'old-cat', 'new-cat']
    for team in teams:
        for param in params:
            # for bleu and meteor
            write_files(team, param, '')
            # for ter
            write_files(team, param, 'ter')
    print('Blocks for teams were successfully created!')


def write_files(team, param, metric):
    filelines = []
    if metric == 'ter':
        option = '_ter'
    else:
        option = ''
    with open('teams/' + team + '_' + str(param) + option + '.txt', 'r') as f:
        filelines += [line for line in f]
    filelines = randomise_data(filelines, param)
    # each block has 20 elements
    blocks = [filelines[i:i + 20] for i in range(0, len(filelines), 20)]
    for block_id, block in enumerate(blocks[:-1]):      # except the last block of the list
        with open('teams/metric_per_block/' + team + '_' + param + option + '_' + str(block_id + 1) + '.txt', 'w+') as f_block:
            f_block.write(''.join(block))


def reference_create_blocks():
    params = ['all-cat', 'old-cat', 'new-cat']
    for param in params:
        # for bleu, ter, and meteor
        write_reference_files(param, '0')
        write_reference_files(param, '1')
        write_reference_files(param, '2')
        write_reference_files(param, 'meteor')
        write_reference_files(param, 'ter')
    print('Blocks for references were successfully created!')


def write_reference_files(param, metric):
    filelines = []
    if metric == 'meteor':
        option = '-3ref.meteor'
    elif metric == 'ter':
        option = '-3ref-space.ter'
    else:
        option = metric + '.lex'
    with open('references/gold-' + param + '-reference' + option, 'r') as f:
        filelines += [line for line in f]

    # each block has 20 elements in .lex and 60 elements in .meteor
    if metric == 'meteor' or metric == 'ter':
        # need to randomise every three lines, i.e. keep every three lines frozen
        filelines = randomise_meteor_ter_data(filelines, param)
        blocks = [filelines[i:i + 60] for i in range(0, len(filelines), 60)]
    else:
        # randomise
        filelines = randomise_data(filelines, param)
        blocks = [filelines[i:i + 20] for i in range(0, len(filelines), 20)]

    for block_id, block in enumerate(blocks[:-1]):      # except the last block of the list
        if metric == 'meteor':
            with open('references/metric_per_block/gold-' + param + '-reference-3ref-' + str(block_id + 1) + '.meteor', 'w+') as f_block:
                f_block.write(''.join(block))
        if metric == 'ter':
            with open('references/metric_per_block/gold-' + param + '-reference-3ref-' + str(block_id + 1) + '.ter', 'w+') as f_block:
                f_block.write(''.join(block))
        else:
            with open('references/metric_per_block/gold-' + param + '-reference' + metric + '-' + str(block_id + 1) + '.lex', 'w+') as f_block:
                f_block.write(''.join(block))


# for teams
metric_create_blocks()

# for references
reference_create_blocks()


