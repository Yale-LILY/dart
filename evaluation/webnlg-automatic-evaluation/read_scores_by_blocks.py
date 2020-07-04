teams = ['ADAPT_Centre',
         'GKB_Unimelb',
         'PKUWriter',
         'Tilburg_University-1',
         'Tilburg_University-2',
         'Tilburg_University-3',
         'UIT-DANGNT-CLNLP',
         'UPF-TALN',
         'Baseline']


def read_meteor_output():
    params = [('all-cat', 93), ('old-cat', 48), ('new-cat', 44)]
    for team in teams:
        for param in params:
            filelines = []
            out = ''
            for block_id in range(1, param[1] + 1):
                with open('eval/metric_per_block/meteor3ref-' + team + '-' + param[0] + '-' + str(block_id) + '.txt', 'r') as f:
                    filelines += [line for line in f]
                    # read last line
                    # Final score:            0.37903195028843584
                    lastline = filelines[-1]
                    meteor = lastline.split()[-1]
                    out += 'Block-' + str(block_id) + '\t' + meteor[:4] + '\n'
            # create data for significance testing
            with open('significance-2005-DARPA-NIST/meteor3ref-' + team + '-' + param[0] + '.blocks', 'w+') as f_blocks:
                f_blocks.write(out)
    print('Scores were written to the significance-2005-DARPA-NIST directory.')


def read_bleu_output():
    params = [('all-cat', 93), ('old-cat', 48), ('new-cat', 44)]
    for team in teams:
        for param in params:
            filelines = []
            out = ''
            for block_id in range(1, param[1] + 1):
                with open('eval/metric_per_block/bleu3ref-' + team + '-' + param[0] + '-' + str(block_id) + '.txt', 'r') as f:
                    # BLEU = 0.00, blah-blah-blah
                    firstline = f.readline()
                    beginning = firstline.split(',')[0]
                    bleu = beginning.split('= ')[1]
                    out += 'Block-' + str(block_id) + '\t' + bleu[:4] + '\n'
            # create data for significance testing
            with open('significance-2005-DARPA-NIST/bleu3ref-' + team + '-' + param[0] + '.blocks', 'w+') as f_blocks:
                f_blocks.write(out)
    print('Scores were written to the significance-2005-DARPA-NIST directory.')


def read_ter_output():
    params = [('all-cat', 93), ('old-cat', 48), ('new-cat', 44)]
    for team in teams:
        for param in params:
            filelines = []
            out = ''
            for block_id in range(1, param[1] + 1):
                with open('eval/metric_per_block/ter3ref-' + team + '-' + param[0] + '-' + str(block_id) + '.txt', 'r') as f:
                    filelines += [line for line in f]
                    # read the fourth line from the bottom
                    # Total TER: 0.8489038270189595(36765.0 / 43308.792857142915)
                    lastline = filelines[-4]
                    ter = lastline.split()[2]
                    out += 'Block-' + str(block_id) + '\t' + ter[:4] + '\n'
            # create data for significance testing
            with open('significance-2005-DARPA-NIST/ter3ref-' + team + '-' + param[0] + '.blocks', 'w+') as f_blocks:
                f_blocks.write(out)
    print('Scores were written to the significance-2005-DARPA-NIST directory.')


read_meteor_output()
read_bleu_output()
read_ter_output()
