package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;

import java.util.List;

public class IBMIntersect implements WordAligner {

    private WordAligner IBMF2E;
    private WordAligner IBME2F;


    public IBMIntersect(Iterable<SentencePair> trainingData){
        this.IBMF2E = new IBMF2EAligner(trainingData);
        this.IBME2F = new IBME2FAligner(trainingData);
    }


    public Alignment alignSentencePair(SentencePair pair) {

        MergeAligner MA = new MergeAligner(pair);
        return MA.merge(this.IBMF2E,this.IBME2F);

    }
}
