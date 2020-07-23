package edu.berkeley.nlp.assignments.rerank.student;

import java.util.List;
import edu.berkeley.nlp.assignments.rerank.*;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.math.DoubleArrays;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.Pair;


public class MyBasicReranker implements ParsingReranker {

    private  double [] PercepWeights;

    private Indexer<String> TrainFeaIndex = new Indexer<String>();

    private MyFeatureExtractor myFeaExtractor = new MyFeatureExtractor();

    private int [][]bestKFeature;

    public MyBasicReranker(Iterable<Pair<KbestList,Tree<String>>> kbestListsAndGoldTrees) {

      for(int epoch = -1; epoch<17; epoch++ ) {

          if(epoch==-1){

              for (Pair<KbestList, Tree<String>> eachPair: kbestListsAndGoldTrees) {

                  KbestList kbList = eachPair.getFirst();

                  bestKFeature = new int[kbList.getKbestTrees().size()][];

                  for(int i=0; i<kbList.getKbestTrees().size();i++){
                      bestKFeature[i] = myFeaExtractor.extractFeatures(kbList,i,TrainFeaIndex,true);
                  }
              }

              PercepWeights = new double[TrainFeaIndex.size()];

          }else {

              System.out.print("On the epoch: ");
              System.out.println(epoch+1);

              for (Pair<KbestList, Tree<String>> eachPair : kbestListsAndGoldTrees) {
                  int maxFindex = MyUtil.getBestF1Index(eachPair);

                  KbestList kbList = eachPair.getFirst();
                  bestKFeature = new int[kbList.getKbestTrees().size()][];
                  double[] score = new double[kbList.getKbestTrees().size()];

                  for (int i = 0; i < kbList.getKbestTrees().size(); i++) {
                      bestKFeature[i] = myFeaExtractor.extractFeatures(kbList, i, TrainFeaIndex, true);
                      score[i] = WeightsUtils.dot(PercepWeights, bestKFeature[i]);
                  }

                  int maxScoreIndex = DoubleArrays.argMax(score);
                  if (maxFindex != maxScoreIndex) {
                      WeightsUtils.add(PercepWeights, bestKFeature[maxScoreIndex], -1);
                      WeightsUtils.add(PercepWeights, bestKFeature[maxFindex], 1);
                  }

              }
          }
      }
    }

    public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
        double[] score = new double[kbestList.getKbestTrees().size()];
        for(int i=0; i<kbestList.getKbestTrees().size(); i++){
            int [] fea = myFeaExtractor.extractFeatures(kbestList,i,TrainFeaIndex,false);
            score[i] = WeightsUtils.dot(PercepWeights,fea);
        }
        return kbestList.getKbestTrees().get(DoubleArrays.argMax(score));
    }

}
