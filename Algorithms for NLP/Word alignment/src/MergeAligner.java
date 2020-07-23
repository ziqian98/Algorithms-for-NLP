package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;

import java.util.List;

public class MergeAligner {
    private SentencePair pair;

    public MergeAligner(SentencePair pair){
        this.pair = pair;
    }

    public Alignment merge(WordAligner a1, WordAligner a2){

        Alignment F2E = a1.alignSentencePair(this.pair);
        Alignment E2F = a2.alignSentencePair(this.pair);

        Alignment alignment = new Alignment();

        for(int eidx=0; eidx<this.pair.englishWords.size();eidx++ ){
            for(int fidx=0; fidx<this.pair.frenchWords.size();fidx++){
                if(F2E.containsSureAlignment(eidx,fidx))
                    if(E2F.containsSureAlignment(eidx,fidx))
                        alignment.addAlignment(eidx,fidx,true);
            }
        }

        return alignment;

    }


}
