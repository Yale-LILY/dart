import os
import random
import re
import json
import sys
import getopt
from os.path import join
from collections import defaultdict
from benchmark_reader import Benchmark


def delexicalisation(out_src, out_trg, category, properties_objects, delex):
    """
    Perform delexicalisation.
    :param out_src: source string
    :param out_trg: target string
    :param category: DBPedia category
    :param properties_objects: dictionary mapping properties to objects
    :return: delexicalised strings of the source and target; dictionary containing mappings of the replacements made
    """
    # replace all occurrences of Alan_Bean to ASTRONAUT in input
    delex_subj = delex[category]
    delex_src = out_src
    delex_trg = out_trg
    # for each instance, we save the mappings between nondelex and delex
    replcments = {}
    for subject in delex_subj:
        clean_subj = ' '.join(re.split('(\W)', subject.replace('_', ' ')))
        if clean_subj in out_src:
            delex_src = out_src.replace(clean_subj + ' ', category.upper() + ' ')
            replcments[category.upper()] = ' '.join(clean_subj.split())   # remove redundant spaces
        if clean_subj in out_trg:
            delex_trg = out_trg.replace(clean_subj + ' ', category.upper() + ' ')
            replcments[category.upper()] = ' '.join(clean_subj.split())

    # replace all occurrences of objects by PROPERTY in input
    for pro, obj in sorted(properties_objects.items()):
        obj_clean = ' '.join(re.split('(\W)', obj.replace('_', ' ').replace('"', '')))
        if obj_clean in delex_src:
            delex_src = delex_src.replace(obj_clean + ' ', pro.upper() + ' ')
            replcments[pro.upper()] = ' '.join(obj_clean.split())   # remove redundant spaces
        if obj_clean in delex_trg:
            delex_trg = delex_trg.replace(obj_clean + ' ', pro.upper() + ' ')
            replcments[pro.upper()] = ' '.join(obj_clean.split())

    # possible enhancement for delexicalisation:
    # do delex triple by triple
    # now building | location | New_York_City New_York_City | isPartOf | New_York
    # is converted to
    # BUILDING location ISPARTOF City ISPARTOF City isPartOf ISPARTOF
    return delex_src, delex_trg, replcments

def delexicalisation_helper(out_src, out_trg, all_categories, properties_objects, delex):
    complete_rplc_dict = {}
    for category in all_categories:
        out_src, out_trg, rplc_dict = delexicalisation(out_src, out_trg, category, properties_objects, delex)
        complete_rplc_dict = {**complete_rplc_dict, **rplc_dict}
    return out_src, out_trg, complete_rplc_dict

def create_source_target(b, options, dataset, delex_dict_path, delex=True):
    """
    Write target and source files, and reference files for BLEU.
    :param b: instance of Benchmark class
    :param options: string "delex" or "notdelex" to label files
    :param dataset: dataset part: train, dev, test
    :param delex: boolean; perform delexicalisation or not
    :return: if delex True, return list of replacement dictionaries for each example
    """
    source_out = []
    target_out = []
    rplc_list = []  # store the dict of replacements for each example
    
    with open(delex_dict_path) as data_file:
        delex_dict = json.load(data_file)
        all_categories = delex_dict.keys()

    for entr in b.entries:
        tripleset = entr.modifiedtripleset
        lexics = entr.lexs
        # category = entr.category
        for lex in lexics:
            triples = ''
            properties_objects = {}
            for triple in tripleset.triples:
                triples += triple.s + ' ' + triple.p + ' ' + triple.o + ' '
                properties_objects[triple.p] = triple.o
            triples = triples.replace('_', ' ').replace('"', '')
            # separate punct signs from text
            out_src = ' '.join(re.split('(\W)', triples))
            if lex.lex is None:
                continue
            out_trg = ' '.join(re.split('(\W)', lex.lex))
            if len(out_src) == 0 or len(out_trg) == 0:
                continue
            if delex:
                out_src, out_trg, rplc_dict = delexicalisation_helper(out_src, out_trg, all_categories, properties_objects, delex_dict)
                if len(out_src) == 0 or len(out_trg) == 0:
                    continue
                rplc_list.append(rplc_dict)
            # delete white spaces
            source_out.append(' '.join(out_src.split()))
            target_out.append(' '.join(out_trg.split()))

    # shuffle two lists in the same way
    random.seed(10)
    if delex:
        corpus = list(zip(source_out, target_out, rplc_list))
        random.shuffle(corpus)
        source_out, target_out, rplc_list = zip(*corpus)
    else:
        corpus = list(zip(source_out, target_out))
        random.shuffle(corpus)
        source_out, target_out = zip(*corpus)

    with open(dataset + '-webnlg-' + options + '.triple', 'w+') as f:
        f.write('\n'.join(source_out))
    with open(dataset + '-webnlg-' + options + '.lex', 'w+') as f:
        f.write('\n'.join(target_out))

    # create separate files with references for multi-bleu.pl for dev set
    scr_refs = defaultdict(list)
    if dataset == 'dev' and not delex:
        for src, trg in zip(source_out, target_out):
            scr_refs[src].append(trg)
        # length of the value with max elements
        max_refs = sorted(scr_refs.values(), key=len)[-1]
        keys = [key for (key, value) in sorted(scr_refs.items())]
        values = [value for (key, value) in sorted(scr_refs.items())]
        # write the source file not delex
        with open(options + '-source.triple', 'w+') as f:
            f.write('\n'.join(keys))
        # write references files
        for j in range(0, len(max_refs)):
            with open(options + '-reference' + str(j) + '.lex', 'w+') as f:
                out = ''
                for ref in values:
                    try:
                        out += ref[j] + '\n'
                    except:
                        out += '\n'
                f.write(out)

    return rplc_list


def relexicalise(predfile, rplc_list):
    """
    Take a file from seq2seq output and write a relexicalised version of it.
    :param rplc_list: list of dictionaries of replacements for each example (UPPER:not delex item)
    :return: list of predicted sentences
    """
    relex_predictions = []
    with open(predfile, 'r') as f:
        predictions = [line for line in f]
    for i, pred in enumerate(predictions):
        # replace each item in the corresponding example
        rplc_dict = rplc_list[i]
        relex_pred = pred
        for key in sorted(rplc_dict):
            relex_pred = relex_pred.replace(key + ' ', rplc_dict[key] + ' ')
        relex_predictions.append(relex_pred)
    # with open('relexicalised_predictions_full.txt', 'w+') as f:
        # f.write(''.join(relex_predictions))

    # create a mapping between not delex triples and relexicalised sents
    with open('test-webnlg-all-notdelex.triple', 'r') as f:
        dev_sources = [line.strip() for line in f]
    src_gens = {}

    with open('relexicalised_predictions.txt', 'w+') as f:
        for src, gen in zip(dev_sources, relex_predictions):
            #src_gens[src] = gen  # need only one lex, because they are the same for a given triple
            f.write(gen)

    # write generated sents to a file in the same order as triples are written in the source file
    #with open('all-notdelex-source.triple', 'r') as f:
    #    triples = [line.strip() for line in f]

    #with open('relexicalised_predictions.txt', 'w+') as f:
    #    for triple in triples:
    #        f.write(src_gens[triple])

    return relex_predictions


def input_files(path, delex_path, filepath=None, relex=False):
    """
    Read the corpus, write train and dev files.
    :param path: directory with the WebNLG benchmark
    :param filepath: path to the prediction file with sentences (for relexicalisation)
    :param relex: boolean; do relexicalisation or not
    :return:
    """
    parts = ['train', 'dev', 'test'] if not relex else ['test']
    options = ['all-delex', 'all-notdelex'] if not relex else ['all-delex'] # generate files with/without delexicalisation
    for part in parts:
        for option in options:
            files = [(path, f"{part}.xml")]
            print(files)
            b = Benchmark()
            b.fill_benchmark(files)
            if option == 'all-delex':
                rplc_list = create_source_target(b, option, part, delex_path, delex=True)
                print('Total of {} files processed in {} with {} mode'.format(len(files), part, option))
            elif option == 'all-notdelex':
                rplc_list = create_source_target(b, option, part, delex_path, delex=False)
                print('Total of {} files processed in {} with {} mode'.format(len(files), part, option))
            if relex and part == 'test' and option == 'all-delex':
                relexicalise(filepath, rplc_list)
    print('Files necessary for training/evaluating are written on disc.')


def main(argv):
    usage = 'usage:\npython3 webnlg_baseline_input.py -i <data-directory> -d <delex-dir>' \
           '\ndata-directory is the directory where you unzipped the archive with data'
    try:
        opts, args = getopt.getopt(argv, 'i:d:', ['inputdir='])
    except getopt.GetoptError:
        print(usage)
        sys.exit(2)
    input_data = False
    delex_path = None
    for opt, arg in opts:
        if opt in ('-i', '--inputdir'):
            inputdir = arg
            input_data = True
        elif opt in ('-d'):
            delex_path = arg
        else:
            print(usage)
            sys.exit()
    if not input_data:
        print(usage)
        sys.exit(2)
    print('Input directory is ', inputdir)
    print('Delex path is ', delex_path)
    input_files(inputdir, delex_path)


if __name__ == "__main__":
    main(sys.argv[1:])
