package edu.berkeley.nlp.assignments.parsing.student;

import java.util.*;

import edu.berkeley.nlp.assignments.parsing.*;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

public class GenerativeParser implements Parser{

    private SimpleLexicon lexicon;

    //Indexer<String> gramLabelInd;

    private Grammar grammar;

    //to store probability
    private double biChart[][][];
    private double uChart[][][];

    //to store position for backtrack
    private double biChartPosMiddle[][][];
    private double biChartPosLeft[][][];
    private double biChartPosRight[][][];
    private double uChartPos[][][];

    private UnaryClosure unaryclosure;

    public GenerativeParser(List<Tree<String>> trainTrees){

        System.out.println("train parser size: " + trainTrees.size());
        System.out.print("Annotating / binarizing training trees ... ");

        List<Tree<String>> annotatedTrainTrees = annotateTrees(trainTrees);
        System.out.println("done.");
        System.out.print("Building grammar ... ");

        this.grammar = Grammar.generativeGrammarFromTrees(annotatedTrainTrees);
        System.out.println("done. (" + this.grammar.getLabelIndexer().size() + " states)");

        this.unaryclosure = new UnaryClosure(grammar.getLabelIndexer(), grammar.getUnaryRules());
        this.lexicon = new SimpleLexicon(annotatedTrainTrees);

        System.out.println("done.");

        createEachChart(40, 41,grammar.getLabelIndexer().size());


    }

    public static String getType(Object o){
        return o.getClass().toString();
    }

    private void createEachChart(int row, int column, int gramSize){

        this.uChartPos = new double[row][column][gramSize];
        this.biChartPosMiddle = new double[row][column][gramSize];
        this.biChartPosLeft = new double[row][column][gramSize];
        this.biChartPosRight = new double[row][column][gramSize];
        this.biChart = new double[row][column][gramSize];
        this.uChart = new double[row][column][gramSize];

    }

    private void initEachChart(int sentenceSize){
        //System.out.println("1"+this.grammar.getLabelIndexer().size());
        for(int min = 0; min < sentenceSize; min++){
            for(int max = 0; max < (sentenceSize+1); max++){
                for(int grammarIndex = 0; grammarIndex<this.grammar.getLabelIndexer().size(); grammarIndex++){
                    this.uChartPos[min][max][grammarIndex] = Double.NEGATIVE_INFINITY;
                    this.biChartPosMiddle [min][max][grammarIndex] = Double.NEGATIVE_INFINITY;
                    this.biChartPosLeft[min][max][grammarIndex] = Double.NEGATIVE_INFINITY;
                    this.biChartPosRight[min][max][grammarIndex] = Double.NEGATIVE_INFINITY;
                    this.biChart[min][max][grammarIndex] = Double.NEGATIVE_INFINITY;
                    this.uChart[min][max][grammarIndex] = Double.NEGATIVE_INFINITY;
                }
            }
        }
    }

    private void fillUnaryChartFirst(List<String> sentence){
        //System.out.println("2"+this.grammar.getLabelIndexer().size());
        for(int row=0; row<sentence.size(); row++){
            for(int IndexInGrammar =0; IndexInGrammar < this.grammar.getLabelIndexer().size();IndexInGrammar++){
                if(biChart[row][row+1][IndexInGrammar]==Double.NEGATIVE_INFINITY)
                    continue;
                else{
                    for(UnaryRule unaries : this.unaryclosure.getClosedUnaryRulesByChild(IndexInGrammar)){
                        double score = biChart[row][row+1][IndexInGrammar]+unaries.getScore();
                        if(score>uChart[row][row+1][unaries.getParent()]){
                            uChartPos[row][row+1][unaries.getParent()] = IndexInGrammar;
                            uChart[row][row+1][unaries.getParent()] = score;
                        }
                    }
                }
            }
        }
    }

    private void fillBinaryChartFirst(List<String> sentence){
        for(int row=0; row<sentence.size(); row++){
            for(String tag: this.lexicon.getAllTags()){
                double score = this.lexicon.scoreTagging(sentence.get(row),tag);
                if(score!=Double.NaN && score!=Double.NEGATIVE_INFINITY){
                    int IndexInGrammar = this.grammar.getLabelIndexer().indexOf(tag);
                    if(score>biChart[row][row+1][IndexInGrammar])
                        biChart[row][row+1][IndexInGrammar] = score;
                }
            }
        }
    }


    private void fillTwo(List<String> sentence){
        //fill binary chart
        //System.out.println("4"+this.grammar.getLabelIndexer().size());
        int grammarSize = this.grammar.getLabelIndexer().size();
        for(int max = 2; max<=sentence.size();max++){
            for(int min = max-2; min >= 0; min--){
                for(int mid = min+1; mid<=max-1;mid++){
                    for(int C1 = 0; C1<grammarSize; C1++){
                        if(uChart[min][mid][C1] != Double.NEGATIVE_INFINITY){
                            for(BinaryRule binaries : this.grammar.getBinaryRulesByLeftChild(C1)){
                                int C2 = binaries.getRightChild();
                                if(uChart[mid][max][C2]!=Double.NEGATIVE_INFINITY){
                                    int C = binaries.getParent();
                                    double score = binaries.getScore()+uChart[min][mid][C1]+uChart[mid][max][C2];
                                    if(score>biChart[min][max][C]){
                                        biChartPosLeft[min][max][C] = C1;
                                        biChartPosMiddle[min][max][C] = mid;  //To locate C1 C2 in which square
                                        biChartPosRight[min][max][C] = C2;
                                        biChart[min][max][C] = score;
                                    }
                                }
                            }
                        }
                    }

                }

                // fill unary chart
                for(int IndexInGrammar=0; IndexInGrammar<grammarSize;IndexInGrammar++) {
                    if (biChart[min][max][IndexInGrammar] == Double.NEGATIVE_INFINITY) {
                        continue;
                    } else {
                        for (UnaryRule unaries : this.unaryclosure.getClosedUnaryRulesByChild(IndexInGrammar)) {
                            double score = biChart[min][max][IndexInGrammar] + unaries.getScore();
                            if (score > uChart[min][max][unaries.getParent()]) {
                                uChartPos[min][max][unaries.getParent()] = IndexInGrammar;
                                uChart[min][max][unaries.getParent()] = score;
                            }
                        }

                    }
                }
            }
        }

    }


    private Tree<String> unaryBacktrack(int row, int col, int pos, List<String> sentence){
        int bottom = (int)uChartPos[row][col][pos];

        List<Integer> allPathLength = this.unaryclosure.getPath(new UnaryRule(pos, bottom)); //[ 1, 2, 3] A->A->B->C
        Tree<String> bottomTree =binaryBacktrack(row,col,bottom,sentence);
        List<Tree<String>> allTreesBelow = new ArrayList<Tree<String>>();

        if(allPathLength.size() == 1){
            return binaryBacktrack(row,col,pos,sentence);
        }

        if(allPathLength.size() == 2){
            allTreesBelow.add(bottomTree);
        }

        if(allPathLength.size() > 2){

            Tree<String> temp = bottomTree;
            for(int pathIndex = allPathLength.size()-2; pathIndex>=1; pathIndex--){  //from the second last one
                List<Tree<String>> tempTree = new ArrayList<Tree<String>>();
                int pathNum = allPathLength.get(pathIndex);
                tempTree.add(temp);
                temp = new Tree<String>(this.grammar.getLabelIndexer().get(pathNum),tempTree);
            }

            allTreesBelow.add(temp);

        }
        Tree<String> totalTree = new Tree<String>(this.grammar.getLabelIndexer().get(pos),allTreesBelow);
        return totalTree;

    }

    private Tree<String> binaryBacktrack(int row, int col, int pos, List<String> sentence){

        List<Tree<String>> allTreesBelow = new ArrayList<Tree<String>>();

        if(col == row+1){
            allTreesBelow.add(new Tree<String> (sentence.get(row)));
            return new Tree<String> (this.grammar.getLabelIndexer().get(pos),allTreesBelow);
        }

        int position = (int)biChartPosMiddle[row][col][pos];
        int C1 = (int)biChartPosLeft[row][col][pos];
        int C2 = (int)biChartPosRight[row][col][pos];

        Tree<String> C1Subtree = unaryBacktrack(row,position,C1,sentence);
        Tree<String> C2Subtree = unaryBacktrack(position,col,C2,sentence);

        allTreesBelow.add(C1Subtree);
        allTreesBelow.add(C2Subtree);

        Tree<String> totalTree = new Tree<String>(this.grammar.getLabelIndexer().get(pos),allTreesBelow);
        return totalTree;

    }


    private List<Tree<String>> annotateTrees(List<Tree<String>> trees) {
        List<Tree<String>> annotatedTrees = new ArrayList<Tree<String>>();
        for (Tree<String> tree : trees) {
            annotatedTrees.add(MyTreeAnnotations.annotateTreeLosslessBinarization(tree));

        }
        return annotatedTrees;
    }

    public Tree<String> getBestParse(List<String> sentence){
        initEachChart(sentence.size());
        Tree<String> annotatedBestParse = null;
        annotatedBestParse = buildMyParse(sentence);
        return MyTreeAnnotations.unAnnotateTree(annotatedBestParse);
    }

    private Tree<String> buildMyParse(List<String> sentence){
        //System.out.println("s1111111");
        fillBinaryChartFirst(sentence);
        fillUnaryChartFirst(sentence);
        fillTwo(sentence);
        //System.out.println("s222222");
        if(uChart[0][sentence.size()][0]!=Double.NEGATIVE_INFINITY){
            //System.out.println("s33333333");
            Tree<String> res = unaryBacktrack(0,sentence.size(),0,sentence);
            //System.out.println("s44444444");
            return res;
        }else{
            return new Tree<String>("ROOT", Collections.singletonList(new Tree<String>("JUNK")));
        }

    }
}
