package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;

public class HMMIntersect implements WordAligner {

    private WordAligner HMMF2E;
    private WordAligner HMME2F;

    public HMMIntersect(Iterable<SentencePair> trainingData){
        this.HMMF2E = new HMMF2EAligner(trainingData);
        this.HMME2F = new HMME2FAligner(trainingData);

    }


    public Alignment alignSentencePair(SentencePair pair) {

        MergeAligner MA = new MergeAligner(pair);
        return MA.merge(this.HMMF2E,this.HMME2F);
    }

}
