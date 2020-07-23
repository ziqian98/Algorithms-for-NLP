package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.math.DoubleArrays;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;


public class MyUtil {

    public static int getBestF1Index(Pair<KbestList, Tree<String>> eachPair){

        EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String> eval = new EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String>(
                Collections.singleton("ROOT"), new HashSet<String>(Arrays.asList(new String[] { "''", "``", ".", ":", "," })));

        KbestList kbList = eachPair.getFirst();
        Tree<String> gold = eachPair.getSecond();

        int k = kbList.getKbestTrees().size();

        double [] FOneScore = new double[k];

        for(int i=0; i<k;i++){
            FOneScore[i] = eval.evaluateF1(kbList.getKbestTrees().get(i),gold);
        }

        return DoubleArrays.argMax(FOneScore);

    }

    public static int myLoss(int index, int bestF1Index){
        if(index!=bestF1Index)
            return 1;
        else
            return 0;
    }


    public static IntCounter getIntCounter(int[] myFeature) {
        IntCounter Counter = new IntCounter();
        for (int eachFeatureEntry : myFeature) {
            Counter.incrementCount(eachFeatureEntry,1);
        }
        return Counter;
    }


}
