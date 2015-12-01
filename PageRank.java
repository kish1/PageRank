import java.lang.Math;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Map;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;

import java.io.IOException;

class Link_PR
{
    public Integer link_id;
    public Double pagerank;

    public Link_PR(Integer link_id, Double pagerank)
    {
        this.link_id = link_id;
        this.pagerank = pagerank;
    }
}

public class PageRank
{
    private static int link_id_gen;

    public static HashSet<Integer> P;
    public static HashSet<Integer> S;
    public static HashMap<String, Integer> nodeToId;
    public static HashMap<Integer, String> idToNode;
    public static HashMap<Integer, ArrayList<Integer>> M;
    public static HashMap<Integer, Integer> L;
    public static HashMap<Integer, Double> PR;
    public static HashMap<Integer, Double> newPR;
    public static double d;

    static
    {
        link_id_gen = 1;

        P = new HashSet<Integer>();
        S = null;
        nodeToId = new HashMap<String, Integer>();
        idToNode = new HashMap<Integer, String>();
        M = new HashMap<Integer, ArrayList<Integer>>();
        L = new HashMap<Integer, Integer>();
        PR = new HashMap<Integer, Double>();
        newPR = new HashMap<Integer, Double>();
        d = 0.85;
    }

    public static void main(String[] args) throws IOException
    {
        getData(args[0]);
        calculatePR();
    }

    public static Integer getLinkId(String link_name)
    {
        if(nodeToId.containsKey(link_name))
        {
            return nodeToId.get(link_name);
        }

        nodeToId.put(link_name, link_id_gen);
        idToNode.put(link_id_gen, link_name);
        return link_id_gen++;
    }

    public static void getData(String file_name) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file_name));

        String line = "";
        String [] parts;
        ArrayList<Integer> inlink_list = null;
        line = reader.readLine();
        int link_id = -1;
        int inlink_id = -1;

        int count = 0;

        while(line != null)
        {
            parts = line.split(" ");
            //if(parts.length == 1)
                count++;
            link_id = getLinkId(parts[0]);
            //System.out.println("Link: " + parts[0]);

            P.add(link_id);
            if(! M.containsKey(link_id))
            {
                M.put(link_id, new ArrayList<Integer>());
            }
            //inlink_list = new ArrayList<Integer>();
            for(int i=1; i<parts.length; i++)
            {
                //System.out.println(parts[i]);
                inlink_id = getLinkId(parts[i]);
                //inlink_list.add(inlink_id);

                if(L.containsKey(inlink_id))
                {
                    L.put(inlink_id, L.get(inlink_id) + 1);
                }
                else
                {
                    L.put(inlink_id, new Integer(1));
                }

                M.get(link_id).add(inlink_id);
                /*
                if(M.containsKey(link_id))
                {
                    M.get(link_id).add(inlink_id);
                }
                else
                {
                    HashSet<Integer> inlinks = new HashSet<Integer>();
                    inlinks.add(inlink_id);
                    M.put(link_id, inlinks);
                }
                */
            }
            line = reader.readLine();
        }
        reader.close();

        S = new HashSet<Integer>(P);
        S.removeAll(L.keySet());


        //System.out.println("P: " + Integer.toString(P.size()));
        //System.out.println("Sink: " + Integer.toString(S.size()));
        int c = 0;
        for(Map.Entry<Integer, ArrayList<Integer>> me : M.entrySet())
        {
            if(me.getValue().size() == 0)
                c++;
        }
        System.out.println(c);
        System.out.println(count);
        System.out.println(L.get(3050));
    }

    public static void calculatePR() throws IOException
    {
        FileWriter ppw = new FileWriter("wt_perplexity.txt");

        int iteration = 1;

        int N = P.size();
        double INIT_PR = 1.0/N;
        double INIT_NEWPR = (1 - d)/N;
        double H = 0;
        //System.out.println("N: " + Integer.toString(N));
        //System.out.println(INIT_PR);
        for(Iterator<Integer> i = P.iterator(); i.hasNext();)
        {
            PR.put(i.next(), INIT_PR);

            H -= (INIT_PR * (Math.log(INIT_PR)/Math.log(2.0)));
        }
        //System.out.println(H);
        double perplexity = Math.pow(2.0, H);
        ppw.write("After iteration " + Integer.toString(iteration) + ": " + Double.toString(perplexity) + "\n");
        iteration++;
        double previous_perplexity = perplexity;

        int p, q;
        double npr, pr;

        final int CONVERGENCE_THRESHOLD = 4;
        //boolean PRConverged = false;
        double sinkPR;
        int matches = 1;
        while(matches < CONVERGENCE_THRESHOLD)
        {
            sinkPR = 0;
            for(Iterator<Integer> i = S.iterator(); i.hasNext();)
            {
                sinkPR += PR.get(i.next());
            }
            //System.out.println(sinkPR);

            for(Iterator<Integer> i = P.iterator(); i.hasNext();)
            {
                p = i.next();
                //npr = INIT_NEWPR;
                //npr += d*sinkPR/N;
                npr = INIT_NEWPR + d*sinkPR/N;


                for(Iterator<Integer> j = M.get(p).iterator(); j.hasNext();)
                {
                    q = j.next();
                    npr += d*PR.get(q)/L.get(q);
                }

                newPR.put(p, npr);
            }

            H = 0;
            for(Iterator<Integer> i = P.iterator(); i.hasNext();)
            {
                p = i.next();
                pr = newPR.get(p);

                PR.put(p, pr);

                H -= (pr * (Math.log(pr)/Math.log(2.0)));
            }
            //System.out.println(H);
            perplexity = Math.pow(2.0, H);
            //System.out.println("After iteration " + Integer.toString(iteration) + ":- " + Double.toString(perplexity) + "\n");
            ppw.write("After iteration " + Integer.toString(iteration) + ": " + Double.toString(perplexity) + "\n");
            iteration++;

            if( ((int)perplexity) == ((int) previous_perplexity) )
            {
                //System.out.println(Double.toString(perplexity) + " " + Double.toString(previous_perplexity));
                //System.out.println(Integer.toString(((int)perplexity)) + " " + Integer.toString(((int) previous_perplexity)));
                matches++;
            }
            else
            {
                matches = 1;
            }
            previous_perplexity = perplexity;
        }
        ppw.close();

        int topn = (N >= 500)? 500 : N;
        Link_PR[] nodes = new Link_PR[N];
        int j = 0, id;
        for(Iterator<Integer> i = P.iterator(); i.hasNext();)
        {
            id = i.next();
            nodes[j++] = new Link_PR(id, PR.get(id));
        }
        Arrays.sort(nodes, new Comparator<Link_PR>(){
            public int compare(Link_PR a, Link_PR b)
            {
                return (b.pagerank).compareTo(a.pagerank);
            }
        });
        //System.out.println(nodes[0].pagerank);
        //System.out.println(nodes[N-1].pagerank);

        FileWriter prw = new FileWriter("wt_pagerank.txt");
        for(int i = 0; i < topn; i++)
        {
            prw.write(Double.toString(nodes[i].pagerank) + ": " +idToNode.get(nodes[i].link_id) + "\n");
        }
        prw.close();
    }

    public static int gif(double x)
    {
        return 0;
    }

}
