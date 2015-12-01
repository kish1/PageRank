import math
import sys
global link_to_id, id_to_link, id, P, M, L, S, d
link_to_id = {}
id_to_link = {}
id = 1

P = []
M = {}
d = 0.85
L = {}
S = None


def get_id(link):
    global link_to_id, id_to_link, id
    try:
        return link_to_id[link]
    except KeyError:
        pass
    link_to_id[link] = id
    id_to_link[id] = link
    id += 1
    return (id - 1)

def main():
    get_data(sys.argv[1])
    calculate_pr(sys.argv[1],int(sys.argv[2]))

def get_data(filename):
    global S
    with open(filename) as source_file:
        for line in source_file:
            line = line.rstrip()
            x = line.split(' ')
            p = get_id(x[0])
            P.append(p)
            M[p] = list()
            for i in range(1, len(x)):
                val = get_id(x[i])
                M[p].append(val)
                try:
                    L[val] += 1
                except KeyError:
                    L[val] = 1

    S = list(set(P) - set(L.keys()))

    #print(len(filter(lambda x: x==[], M.values())))

def calculate_pr(source_file_name, result_count):
    global P, M, L, S, d

    pp = open(source_file_name + "_pp.txt" , 'w+')
    iter = 1

    PR = {}
    newPR = {}
    N = len(P)
    INIT_PR = 1.0/N
    INIT_NEWPR = (1-d)/N

    H = 0
    for p in P:
        PR[p] = INIT_PR
        H -= (INIT_PR * math.log(INIT_PR, 2))
    #print(H)
    perplexity = 2**H
    prev = perplexity

    pp.write("Iteration " + str(iter) + ": " + str(perplexity) + "\n")
    iter += 1

    times = 1

    while times < 4:
        sinkPR = 0
        for s in S:
            sinkPR += PR[s]
        #print(sinkPR)
        for p in P:
            npr = INIT_NEWPR + d*sinkPR/N
            for q in M[p]:
                npr += d*PR[q]/L[q]
            newPR[p] = npr

        H = 0
        for p in P:
            pr = newPR[p]
            H -= (pr * math.log(pr,2))
            PR[p] = pr

        perplexity = 2**H
        pp.write("Iteration " + str(iter) + ": " + str(perplexity) + "\n")
        iter+=1
        if int(perplexity) == int(prev):
            times += 1
        else:
            times = 1
        prev = perplexity
    pp.close()

    count = result_count
    if result_count > N:
        count = N

    values = sorted(PR, key=PR.get, reverse=True)[:count]

    with open(source_file_name + "_pr.txt", "w+") as pr:
        for id in values:
            pr.write(str(PR[id]) + ": " + id_to_link[id] + "\n")



if __name__ == '__main__':
    main()
