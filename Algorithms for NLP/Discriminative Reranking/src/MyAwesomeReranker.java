package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.LossAugmentedLinearModel;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.assignments.rerank.PrimalSubgradientSVMLearner;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.math.DoubleArrays;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;


class ZeroOneLoss implements LossAugmentedLinearModel<Data> {

    public UpdateBundle getLossAugmentedUpdateBundle(Data datum, IntCounter weights) {

        int[][] bestKFeature = datum.getBestkFeature();
        int k = bestKFeature.length;
        double[] scores = new double[k];

        for (int i = 0; i < k; i ++) {
            IntCounter feature = MyUtil.getIntCounter(bestKFeature[i]);
            scores[i] = feature.dotProduct(weights) + MyUtil.myLoss(i, datum.getBestF1Index());
        }

        int bestScoreIndex = DoubleArrays.argMax(scores);

        IntCounter lossAugGuessFeatures = MyUtil.getIntCounter(bestKFeature[bestScoreIndex]);
        IntCounter goldFeatures = MyUtil.getIntCounter(bestKFeature[datum.getBestF1Index()]);

        double lossOfGuess = MyUtil.myLoss(bestScoreIndex, datum.getBestF1Index());

        return new UpdateBundle(goldFeatures, lossAugGuessFeatures, lossOfGuess);

    }

}

public class MyAwesomeReranker implements ParsingReranker{


    IntCounter SVMweights;

    Indexer<String> TrainFeaIndex = new Indexer<String>();

    MyFeatureExtractor myFeaExtractor = new MyFeatureExtractor();


    public MyAwesomeReranker(Iterable<Pair<KbestList, Tree<String>>> kbestListsAndGoldTrees){

        List<Data> datum = new ArrayList<Data>();

        for (Pair<KbestList, Tree<String>> eachPair: kbestListsAndGoldTrees) {

            int maxFindex =MyUtil.getBestF1Index(eachPair);

            KbestList kbList = eachPair.getFirst();

            int [][]bestKFeature = new int[kbList.getKbestTrees().size()][];

            for(int i=0; i<kbList.getKbestTrees().size();i++){
                bestKFeature[i] = myFeaExtractor.extractFeatures(kbList,i,TrainFeaIndex,true);
            }

            datum.add(new Data(bestKFeature,maxFindex));
        }

        PrimalSubgradientSVMLearner<Data> SVMmodel = new PrimalSubgradientSVMLearner<Data>(0.01, 0.1, TrainFeaIndex.size(), 30);

        IntCounter initialWeights = new IntCounter();

        SVMweights = SVMmodel.train(initialWeights, new ZeroOneLoss(), datum, 30);

    }

    public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
        double[] scores = new double[kbestList.getScores().length];
        for(int i = 0; i < scores.length; i ++) {
            int[] myFeature = myFeaExtractor.extractFeatures(kbestList, i, TrainFeaIndex, false);
            IntCounter counter = new IntCounter();
            for(int eachFeatureEntry : myFeature){
                counter.incrementCount(eachFeatureEntry,1);
            }
            scores[i] = counter.dotProduct(SVMweights);
        }
        return kbestList.getKbestTrees().get(DoubleArrays.argMax(scores));
    }
}
