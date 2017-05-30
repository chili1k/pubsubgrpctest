import re
from matplotlib.pyplot import ion, figure, gca, scatter, savefig, xlabel, ylabel, title, legend, Line2D
from glob import glob
from os.path import splitext
from string import split

ontime = re.compile(
    '(\d+):(\d+):(\d+\.\d+) \[pool-\d+-thread-\d+\] INFO  org.acme.App - Published in (\d+) ms! \[\d+\]')
late = re.compile(
    '(\d+):(\d+):(\d+\.\d+) \[pool-\d+-thread-\d+\] WARN  org.acme.App - Publish succeeded. Timeout period breached with duration of (\d+) ms')
expired = re.compile(
    '(\d+):(\d+):(\d+\.\d+) \[pool-\d+-thread-\d+\] WARN  org.acme.App - Publish failed after (\d+) ms. DEADLINE_EXCEEDED')
unavailable = re.compile(
    '(\d+):(\d+):(\d+\.\d+) \[pool-\d+-thread-\d+\] WARN  org.acme.App - Publish failed after (\d+) ms. UNAVAILABLE.*')
unauthed = re.compile(
    '(\d+):(\d+):(\d+\.\d+) \[pool-\d+-thread-\d+\] WARN  org.acme.App - Publish failed after (\d+) ms. UNAUTHENTICATED')

labels = (
    (ontime, 'SUCCESS', 'green'),
    (late, 'SUCCESS (DEADLINE EXCEEDED)', 'yellowgreen'),
    (expired, 'FAILURE (DEADLINE EXCEEDED)', 'darkorange'),
    (unavailable, 'FAILURE (UNAVAILABLE)', 'red'),
    (unauthed, 'FAILURE (UNAUTHENTICATED)', 'gray'),
    )
colors = dict((id(pattern), color) for pattern, _, color in labels)
def marker_color(pattern):
    return colors[id(pattern)]

outfiles = glob('output-*.txt')

def events(output):
    for line in output:
        for pattern, _, _ in labels:
            match = pattern.match(line)
            if match:
                hh, mm, sss, latency = match.groups()
                time = int(hh)*3600 + int(mm)*60 + float(sss)
                yield time, float(latency), pattern
                break

def plot(outfile):
    output = open(outfile, 'r')
    times, latencies, patterns = zip(*events(output))
    times = [t - times[0] for t in times]
    latencies = [l/1000 for l in latencies]
    ion()
    fig = figure()
    axes = gca()
    axes.scatter(times, latencies, 2, map(marker_color, patterns))
    xlabel('Time (s)')
    ylabel('Latency (s)')
    legend(handles=[Line2D([], [], label=label, color=color, marker='.', linestyle='') for _, label, color in labels],
           loc='upper left', bbox_to_anchor=(1.05, 1))
    basename = splitext(outfile)[0]
    loss = split(basename, '-')[1]
    title('Network loss %s%%' % loss)
    savefig(basename + '.png', bbox_inches='tight')
    savefig(basename + '.pdf', bbox_inches='tight')

for outfile in outfiles:
    plot(outfile)
