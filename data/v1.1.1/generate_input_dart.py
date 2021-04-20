import json
import sys
import numpy as np
from xml.dom import minidom
import glob
from pathlib import Path
import re
import unidecode
import os
import subprocess

##### STEP 1: specify setting #####
setting = "full"
assert setting in ["manual", "manual_and_auto", "full", "custom"]


datasets = ['train', 'dev', 'test']
version = "v1.1.1"

def camel_case_split(identifier):
    matches = re.finditer('.+?(?:(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|$)', identifier)
    d = [m.group(0) for m in matches]
    new_d = []
    for token in d:
        token = token.replace('(', '')
        token_split = token.split('_')
        for t in token_split:
            #new_d.append(t.lower())
            new_d.append(t)
    return new_d

def get_nodes(n):
    n = n.strip()
    n = n.replace('(', '')
    n = n.replace('\"', '')
    n = n.replace(')', '')
    n = n.replace(',', ' ')
    n = n.replace('_', ' ')

    #n = ' '.join(re.split('(\W)', n))
    n = unidecode.unidecode(n)
    # n = n.lower()

    return n

def get_relation(n):
    n = n.replace('(', '')
    n = n.replace(')', '')
    n = n.strip()
    n = n.split()
    n = "_".join(n)
    n = n.lower()
    return n

def process_triples(mtriples):
    nodes = []

    for m in mtriples:

        ms = m.firstChild.nodeValue
        ms = ms.strip().split(' | ')
        n1 = ms[0]
        n2 = ms[2]
        nodes1 = get_nodes(n1)
        nodes2 = get_nodes(n2)

        edge = get_relation(ms[1])

        edge_split = camel_case_split(edge)
        edges = ' '.join(edge_split)

        nodes.append('<H>')
        nodes.extend(nodes1.split())

        nodes.append('<R>')
        nodes.extend(edges.split())

        nodes.append('<T>')
        nodes.extend(nodes2.split())

    return nodes

def get_data_dev_test(file_, dataset):

    datapoints = []

    xmldoc = minidom.parse(file_)
    entries = xmldoc.getElementsByTagName('entry')
    cont = 0
    for e in entries:
        mtriples = e.getElementsByTagName('mtriple')
        nodes = process_triples(mtriples)

        lexs = e.getElementsByTagName('lex')

        surfaces = []
        if len(lexs) == 0:
            # print(e.getAttribute('eid'))
            continue
        for l in lexs:
            #l = l.firstChild.nodeValue.strip().lower()
            if l.firstChild == None:
                # print(e.getAttribute('eid'), l.getAttribute('lid'))
                continue
            l = l.firstChild.nodeValue.strip().replace('\n', '')
            new_doc = ' '.join(re.split('(\W)', l)).replace('\n', '')
            new_doc = ' '.join(new_doc.split())
            # new_doc = tokenizer.tokenize(new_doc)
            # new_doc = ' '.join(new_doc)
            surfaces.append((l, new_doc.lower()))
        datapoints.append((nodes, surfaces))
        cont += 1
    return datapoints, cont

def get_data(file_):

    datapoints = []

    xmldoc = minidom.parse(file_)
    entries = xmldoc.getElementsByTagName('entry')
    cont = 0
    for e in entries:
        mtriples = e.getElementsByTagName('mtriple')
        nodes = process_triples(mtriples)

        lexs = e.getElementsByTagName('lex')
        # if len(lexs) == 0:
            # print(e.getAttribute('eid'))
        for l in lexs:
            #l = l.firstChild.nodeValue.strip().lower()
            if l.firstChild == None:
                # print(e.getAttribute('eid'), l.getAttribute('lid'))
                continue
            l = l.firstChild.nodeValue.strip().replace('\n', '')
            new_doc = ' '.join(re.split('(\W)', l)).replace('\n', '')
            new_doc = ' '.join(new_doc.split())
            #new_doc = tokenizer.tokenize(new_doc)
            #new_doc = ' '.join(new_doc)
            datapoints.append((nodes, (l, new_doc.lower())))
            cont += 1

    return datapoints, cont


dataset_points = []
for d in datasets:
    cont_all = 0

    filename = f'dart-{version}-{setting}-{d}.xml'
    if d == 'train':
        datapoint, cont = get_data(filename)
    else:
        datapoint, cont = get_data_dev_test(filename, d)

    print(d, len(datapoint))
    print('cont', cont)
    dataset_points.append(datapoint)

path = os.path.dirname(os.path.realpath(__file__)) + f'/dart-{setting}/'
if not os.path.exists(path):
    subprocess.call(['mkdir', '-p', path])
    # os.makedirs(path)

subprocess.call(['rm', path + '*'])
# os.system("rm " + path + '/*')

for idx, datapoints in enumerate(dataset_points):

    part = datasets[idx]

    if part == 'dev':
        part = 'val'

    nodes = []
    surfaces = []
    surfaces_2 = []
    surfaces_3 = []

    surfaces_eval = []
    surfaces_2_eval = []
    surfaces_3_eval = []
    for datapoint in datapoints:
        node = datapoint[0]
        sur = datapoint[1]
        nodes.append(' '.join(node))
        if part != 'train':
            surfaces.append(sur[0][0])
            surfaces_eval.append(sur[0][1])
            if len(sur) > 1:
                surfaces_2.append(sur[1][0])
                surfaces_2_eval.append(sur[1][1])
            else:
                surfaces_2.append('')
                surfaces_2_eval.append('')
            if len(sur) > 2:
                surfaces_3.append(sur[2][0])
                surfaces_3_eval.append(sur[2][1])
            else:
                surfaces_3.append('')
                surfaces_3_eval.append('')
        else:
            if sur[0] == '':
                print('!')
            surfaces.append(sur[0])
            surfaces_eval.append(sur[1])

    with open(path + '/' + part + '.source', 'w', encoding='utf8') as f:
        f.write('\n'.join(nodes))
        f.write('\n')
    with open(path + '/' + part + '.target', 'w', encoding='utf8') as f:
        f.write('\n'.join(surfaces))
        f.write('\n')
    if part != 'train':
        with open(path + '/' + part + '.target2', 'w', encoding='utf8') as f:
            f.write('\n'.join(surfaces_2))
            f.write('\n')
        with open(path + '/' + part + '.target3', 'w', encoding='utf8') as f:
            f.write('\n'.join(surfaces_3))
            f.write('\n')

    with open(path + '/' + part + '.target_eval', 'w', encoding='utf8') as f:
        f.write('\n'.join(surfaces_eval))
        f.write('\n')
    if part != 'train':
        with open(path + '/' + part + '.target2_eval', 'w', encoding='utf8') as f:
            f.write('\n'.join(surfaces_2_eval))
            f.write('\n')
        with open(path + '/' + part + '.target3_eval', 'w', encoding='utf8') as f:
            f.write('\n'.join(surfaces_3_eval))
            f.write('\n')

        path_c = os.path.dirname(os.path.realpath(__file__))
        subprocess.call(['python', path_c+'/convert_files_crf.py', path+'/'+part])
        subprocess.call(['python', path_c+'/convert_files_meteor.py', path+'/'+part])
        # os.system("python " + path_c + '/' + "convert_files_crf.py " + path + '/' + part)
        # os.system("python " + path_c + '/' + "convert_files_meteor.py " + path + '/' + part)
