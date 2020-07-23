package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.StringIndexer;

import java.util.Arrays;
import java.util.List;

public class IBMF2EAligner implements WordAligner {

    private double []s_total;
    private double []total;
    private double [][]t;
    private double [][]count;

    private StringIndexer totalEword;
    private StringIndexer totalFword;

    public IBMF2EAligner(Iterable<SentencePair> trainingData){

        totalEword = buildEwords(trainingData);
        totalFword = buildFwords(trainingData);
        initializeT();

        for(int Epoch=0; Epoch<15;Epoch++){
            s_total = new double[totalEword.size()];
            total = new double[totalFword.size()];
            count = new double[totalFword.size()][totalEword.size()];

            Arrays.fill(s_total,0.0);
            Arrays.fill(total,0.0);
            for(int i =0; i<count.length; i++)
                for(int j=0; j<count[i].length; j++)
                    count[i][j] = 0.0;

            for(SentencePair entry: trainingData) {
                fills_total(entry);
                fillCountTotal(entry);

            }

            updateT();
        }


    }


    private void updateT(){
        for(int row =0; row<totalFword.size();row++)
            for(int col=0; col<totalEword.size();col++)
                t[row][col] = count[row][col] / total[row];
    }

    private void fills_total(SentencePair entry){

            List<String> fSentence = entry.frenchWords;
            
            List<String> eSentence = entry.englishWords;
            for(int ei =0; ei < eSentence.size();ei++){
                int eidx = totalEword.indexOf(eSentence.get(ei));
                s_total[eidx] = 0.0;
                for(int fi=0; fi<fSentence.size();fi++) {
                    s_total[eidx] = s_total[eidx] + t[totalFword.indexOf(fSentence.get(fi))][eidx];
                }

            }
    }

    private void fillCountTotal(SentencePair entry){

            List<String> fSentence = entry.frenchWords;
            List<String> eSentence = entry.englishWords;
            for(int ei=0; ei < eSentence.size();ei++){
                for(int fi=0; fi<fSentence.size();fi++){
                    int eidx = totalEword.indexOf(eSentence.get(ei));
                    int fidx = totalFword.indexOf(fSentence.get(fi));
                    count[fidx][eidx]= count[fidx][eidx] +t[fidx][eidx]/s_total[eidx];
                    total[fidx] = total[fidx] + t[fidx][eidx]/s_total[eidx];
                }
            }
    }



    private void initializeT(){
        t = new double [totalFword.size()][totalEword.size()];
        for(int row = 0; row<totalFword.size();row++)
            for(int col = 0; col<totalEword.size();col++)
                t[row][col] = 1.0/((double)(totalEword.size()));

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

                double cellValue = t[fi][ei];
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
