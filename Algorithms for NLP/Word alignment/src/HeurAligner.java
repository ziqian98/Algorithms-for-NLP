package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.StringIndexer;
import java.util.*;

public class HeurAligner implements WordAligner {

    private double [] ce;
    private double [] cf;
    private double [][] cfe;

    private StringIndexer totalEword;
    private StringIndexer totalFword;


    public HeurAligner(Iterable<SentencePair> trainingData) {
        totalEword = buildEwords(trainingData);
        totalFword = buildFwords(trainingData);

        cfe = new double[totalFword.size()][totalEword.size()];
        ce = new double[totalEword.size()];
        cf = new double[totalFword.size()];

        fillCFE(trainingData);
        fillCE(trainingData);
        fillCF(trainingData);

    }

    private void fillCFE(Iterable<SentencePair> trainingData){
        for(SentencePair entry: trainingData){
            List<String> fSentence = entry.frenchWords;
            List<String> eSentence = entry.englishWords;

            for(String fword : fSentence)
                for(String eword : eSentence)
                    cfe[totalFword.indexOf(fword)][totalEword.indexOf(eword)]+=1.0;

        }
    }

    private void fillCF(Iterable<SentencePair> trainingData){
        for(SentencePair entry: trainingData){
            List<String> fSentence = entry.frenchWords;
            for(String fword : fSentence)
                cf[totalFword.indexOf(fword)]+=1.0;
        }
    }

    private  void fillCE(Iterable<SentencePair> trainingData){
        for(SentencePair entry: trainingData){
            List<String> eSentence = entry.englishWords;
            
            for(String eword : eSentence)
                ce[totalEword.indexOf(eword)]+=1.0;
        }
    }


    private StringIndexer buildEwords(Iterable<SentencePair> trainingData){
        StringIndexer e = new StringIndexer();
        for(SentencePair entry: trainingData){
            List<String> eSentence = entry.englishWords;
            e.addAll(eSentence);
        }

        return e;
    }

    private StringIndexer buildFwords(Iterable<SentencePair> trainingData){
        StringIndexer f = new StringIndexer();
        for(SentencePair entry: trainingData){
            List<String> fSentence = entry.frenchWords;
            f.addAll(fSentence);
        }

        return f;
    }



    public Alignment alignSentencePair(SentencePair sentencePair) {
        List<String> eSentence = sentencePair.englishWords;
        List<String> fSentence = sentencePair.frenchWords;

        Alignment alignment = new Alignment();

        for(int row=0; row<fSentence.size();row++){
            double maxvalue = -Double.MAX_VALUE;
            int maxcol = Integer.MIN_VALUE;
            int fi = totalFword.indexOf(fSentence.get(row));

            for(int col=0; col<eSentence.size();col++){
                int ei = totalEword.indexOf(eSentence.get(col));
                double cellValue = cfe[fi][ei]/(ce[ei]*cf[fi]);
                if(cellValue>maxvalue){
                    maxvalue = cellValue;
                    maxcol = col;
                }
            }

            alignment.addAlignment(maxcol,row,true);
        }

        return alignment;
    }




}
