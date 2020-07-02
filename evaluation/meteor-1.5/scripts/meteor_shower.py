#!/usr/bin/env python

# Learn Meteor parameters quickly with up to 42 Meteors
# Run as many as requeted in parallel
# Meteors use 1 cpu / 2gb each

import collections, os, subprocess, sys, threading

def main(argv):
    
    if len(argv[1:]) < 7:
        print >> sys.stderr, 'Learn Meteor parameters efficiently with parallel Trainers'
        print >> sys.stderr, 'Usage: {0} <meteor.jar> <lang> <n-mods> <task> <data_dir> <work_dir> <n-jobs> [other args like -a par.gz, -ch, ...]'.format(argv[0])
        sys.exit(1)
    
    # Args
    meteor_jar = os.path.abspath(argv[1])
    lang = argv[2]
    n_mods = int(argv[3])
    task = argv[4]
    data_dir = os.path.abspath(argv[5])
    work_dir = os.path.abspath(argv[6])
    n_jobs = int(argv[7])
    sb_dir = os.path.join(work_dir, 'sandbox')
    other_args = argv[8:]

    # Working dir
    if os.path.exists(work_dir):
        print 'Work dir {0} exists, exiting'.format(work_dir)
        sys.exit(1)
    os.mkdir(work_dir)
    os.mkdir(sb_dir)

    # Weight ranges for jobs based on mod count
    w_start_list = [1, 0, 0, 0]
    w_end_list = [1, 0, 0, 0]
    for i in range(n_mods):
        w_end_list[i] = 1
    w_start = ''
    w_end = ''
    for i in range(4):
        w_start += str(w_start_list[i]) + ' '
        w_end += str(w_end_list[i]) + ' '
    w_start = w_start.strip()
    w_end = w_end.strip()
    
    # Step is always the same
    step = '0.05 0.10 0.05 0.05 1.0 0.2 0.2 0.2'
    
    # Queue Trainer commands
    queue = collections.deque([])
    for i in range(42):
        sb_sub_dir = os.path.join(sb_dir, '{0}'.format(i + 1))
        os.mkdir(sb_sub_dir)
        out_file = os.path.join(work_dir, 'output.{0}'.format(i + 1))
        a = 0.05 * (i / 2)
        (g_min, g_max) = (0, 0.5) if (i % 2 == 0) else (0.55, 1.0)
        start = '{0} 0 {1} 0 {2}'.format(a, g_min, w_start)
        end = '{0} 2.5 {1} 1.0 {2}'.format(a, g_max, w_end)
        # Retry in case of filesystem failure
        trainer_cmd = 'cd {sd} && while true ; do sleep 1 ; java -Xmx1G -cp {0} Trainer {1} {2} -l {3} -i \'{4}\' -f \'{5}\' -s \'{6}\' {args} > {7} ; if [ "$?" = "0" ] ; then break ; fi ; done'.format(meteor_jar, task, data_dir, lang, start, end, step, out_file, sd=sb_sub_dir, args=' '.join(other_args))
        queue.append(trainer_cmd)
    
    # Run Trainers
    for i in range(n_jobs):
        queue.append(-1)
    threads = []
    for i in range(n_jobs):
        t = threading.Thread(target=run, args=(queue,))
        threads.append(t)
        t.start()
    for t in threads:
        t.join()

    # Sort output
    sort_cmd = 'cat {0}/output.* |sort -g -S4G --parallel={1} >{0}/output.sort'.format(work_dir, n_jobs)
    subprocess.call(sort_cmd, shell=True)

# Run commands until end of queue
def run(queue):
    while True:
        cmd = queue.popleft()
        if cmd == -1:
            return
        subprocess.call(cmd, shell=True)

if __name__ == '__main__' : main(sys.argv)
