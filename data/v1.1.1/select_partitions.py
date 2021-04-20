import os
import json
from xml.etree import ElementTree

# default settings
manual = ["WikiTableQuestions_lily", "WikiSQL_lily", "WikiTableQuestions_mturk"]
manual_and_auto = manual + ["WikiSQL_decl_sents"]
full = manual_and_auto + ["webnlg", "e2e"]

##### STEP 1. choose one of default settings or customize (remove comment) #####
selected_partitions = full
# selected_partitions = [
#                        "WikiTableQuestions_lily", "WikiSQL_lily", # internal annotations
#                        "WikiTableQuestions_mturk",                # MTurk annotations + internal check
#                        "WikiSQL_decl_sents",                      # automatic annotations
#                        "webnlg", "e2e"                            # other resources, less open-domain but contain more instances
#                        ]

##### STEP 2. specify setting #####
setting = "full"
assert setting in ["manual", "manual_and_auto", "full", "custom"]

if not os.path.exists(setting):
    os.makedirs(setting)

print("***************************")
print(f"Selected setting: {setting}")
print("***************************")

dataset_version = "v1.1.1"
datasets = ['train', 'dev', 'test']

for dataset in datasets:
    print(f"Preparing {dataset}-split json file...")
    with open(f"dart-v1.1.1-full-{dataset}.json", "r", encoding="utf-8") as f_r:
        data = json.load(f_r)
    selected_data = []
    for instance in data:
        if len(instance['annotations']) == 0: # remove empty annotations
            continue
        if instance['annotations'][0]['source'] in selected_partitions:
            selected_data.append(instance)

    with open(f"{setting}/dart-{dataset_version}-{setting}-{dataset}.json", "w", encoding="utf-8") as f_w:
        json.dump(selected_data, f_w, indent=2)


    print(f"Preparing {dataset}-split xml file...")
    root = ElementTree.parse(f"dart-v1.1.1-full-{dataset}.xml").getroot()
    entries = root.getchildren()[0]
    to_be_deleted = []
    for entry in entries:
        if entry.find("lex") == None: # remove empty annotations
            to_be_deleted.append(entry)
            continue
        if entry.find("lex").attrib['comment'] not in selected_partitions:
            to_be_deleted.append(entry)
    for entry in to_be_deleted:
        entries.remove(entry)
    tree = ElementTree.ElementTree()
    tree._setroot(root)
    tree.write(f"{setting}/dart-{dataset_version}-{setting}-{dataset}.xml", xml_declaration=True, encoding="utf-8")
