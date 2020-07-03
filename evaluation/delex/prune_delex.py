import json

if __name__ == '__main__':
    with open("./delex_dict_marked.json") as f:
        delex_dict = json.load(f)

    blacklist = {}
    blacklist_categories = [] # cannot delete during iteration so trac
    ismarked = lambda e: e[-1] == '`'
    for category, values in delex_dict.items():
        # check if category itself is blacklisted
        if ismarked(category):
            pruned_category = category[:-1]
            blacklist[pruned_category] = values
            blacklist_categories.append(category)
            print(f"Removing Category: {pruned_category}")
            continue

        blacklist[category] = []
        for i in range(len(values) - 1, -1, -1):
            value = values[i]
            
            if ismarked(value):
                # bad entry found
                # add to blacklist and delete
                pruned_value = value[:-1]
                blacklist[category].append(pruned_value) 
                print(f"Removing: {pruned_value}")
                del values[i]
            
        if len(blacklist[category]) == 0:
            del blacklist[category]

    for category in blacklist_categories:
        del delex_dict[category]

    # write blacklist to file
    with open("./blacklist.json", "w") as f:
        json.dump(blacklist, f, indent=4)

    # output pruned json
    with open("./delex_dict.json", "w") as f:
        json.dump(delex_dict, f, indent=4)

