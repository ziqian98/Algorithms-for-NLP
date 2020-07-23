package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.math.SloppyMath;
import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.StringIndexer;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class HMMF2EAligner implements WordAligner {

    private double[][]alpha;
    private double[][]beta;
    private double[][]theta; //emission
    private double [][]dfe; //to estimate emission prob
    private double []de;
    private double []dk; //to estimate transition prob
    private double[]fai; //transition

    private double[][] W; //Viterbi Table
    private int [][] P; //backpointer


    private int maxElen=Integer.MIN_VALUE;
    private int EvocabSize;
    private int FvocabSize;
    private double z; //alpha*beta P(O|M)

    private StringIndexer totalEword;
    private StringIndexer totalFword;


    public HMMF2EAligner(Iterable<SentencePair> trainingData){

        totalEword = buildEwords(trainingData);
        totalFword = buildFwords(trainingData);
        EvocabSize = totalEword.size();
        FvocabSize = totalFword.size();

        for(SentencePair entry:trainingData) {
            if (maxElen < entry.englishWords.size())
                maxElen = entry.englishWords.size();
        }

        initialize_theta();
        initialize_fai();

        for(int Epoch=0; Epoch<6; Epoch++){

            System.out.println("Epoch: " + (Epoch+1));

            initialize_dfe();
            initialize_de();
            initialize_dk();


            for (SentencePair entry: trainingData) {
                fill_alpha(entry);
                fill_beta(entry);
                calculate_z(entry);

                fill_dfe(entry);
                fill_dk(entry);
                fill_de(entry);

            }

            update_theta();
            update_fai();


        }


    }

    private void update_fai(){
        double sum = SloppyMath.logAdd(dk);
        for(int i=0; i<maxElen; i++){
            fai[i] = dk[i]-sum;
        }
    }

    private void update_theta(){
        for(int i=0; i<totalEword.size();i++){
            for(int j=0; j<totalFword.size();j++){
                theta[i][j] = dfe[i][j] - de[i];
            }
        }

    }


    private void fill_de(SentencePair entry){
        for(int row=0; row<entry.englishWords.size();row++){
            for(int col=0; col<entry.frenchWords.size();col++) {
                int eidx = totalEword.indexOf(entry.englishWords.get(row));

                de[eidx] = SloppyMath.logAdd(de[eidx],alpha[row][col] + beta[row][col] - z);


            }
        }
    }

    private void fill_dk(SentencePair entry){
        for(int col=1;col<entry.frenchWords.size();col++){
            for(int row=0; row<entry.englishWords.size();row++){
                int eidx = totalEword.indexOf(entry.englishWords.get(row));
                int fidx = totalFword.indexOf(entry.frenchWords.get(col));

                for(int prevrow=0; prevrow<entry.englishWords.size(); prevrow++){

                        dk[Math.abs(row-prevrow)] = SloppyMath.logAdd(dk[Math.abs(row-prevrow)],
                                alpha[prevrow][col-1] +
                                        beta[row][col]+
                                        fai[Math.abs(row-prevrow)]+
                                        theta[eidx][fidx] - z);
                }
            }
        }

    }

    private void fill_dfe(SentencePair entry){
        for(int row=0; row<entry.englishWords.size();row++){
            for(int col=0; col<entry.frenchWords.size();col++){

                int edix = totalEword.indexOf(entry.englishWords.get(row));
                int fidx = totalFword.indexOf(entry.frenchWords.get(col));

                    dfe[edix][fidx] = SloppyMath.logAdd(dfe[edix][fidx],alpha[row][col] + beta[row][col] - z);

            }
        }

    }




    private void fill_beta(SentencePair entry){
        beta = new double[entry.englishWords.size()][entry.frenchWords.size()];

        for(int row=0; row<entry.englishWords.size();row++){
            for(int col=entry.frenchWords.size()-1; col>-1;col--){
                beta[row][col] = Double.NEGATIVE_INFINITY;
            }
        }

        for(int col=entry.frenchWords.size()-1; col>-1;col--){
            for(int row=0; row<entry.englishWords.size();row++){
                if(col != entry.frenchWords.size()-1){
                    for(int rowAfter=0; rowAfter<entry.englishWords.size();rowAfter++){

                            beta[row][col] = SloppyMath.logAdd(beta[row][col],
                                    beta[rowAfter][col+1]+
                                            fai[Math.abs(row-rowAfter)]+
                                            theta[totalEword.indexOf(entry.englishWords.get(rowAfter))]
                                                    [totalFword.indexOf(entry.frenchWords.get(col+1))]);

                    }

                }else {
                    beta[row][col]=0.0;
                }
            }

        }

    }


    private void calculate_z(SentencePair entry){
        z = Double.NEGATIVE_INFINITY;
        for(int i=0; i<entry.englishWords.size();i++){
                z = SloppyMath.logAdd(z,alpha[i][entry.frenchWords.size()-1]);
        }

    }

    private void fill_alpha(SentencePair entry){
        alpha = new double[entry.englishWords.size()][entry.frenchWords.size()];
        for (int row = 0; row < entry.englishWords.size(); row++){
            for(int col=0; col<entry.frenchWords.size();col++){
                alpha[row][col] = Double.NEGATIVE_INFINITY;
            }
        }

        for(int col=0; col<entry.frenchWords.size();col++) {
            for (int row = 0; row < entry.englishWords.size(); row++) {
                if(col==0) {
                    
                    alpha[row][col] = fai[row] +
                            theta[totalEword.indexOf(entry.englishWords.get(row))]
                                    [totalFword.indexOf(entry.frenchWords.get(col))];
                }

                else {
                    for(int prevrow=0; prevrow<entry.englishWords.size();prevrow++){

                            alpha[row][col] = SloppyMath.logAdd(alpha[row][col],
                                    alpha[prevrow][col-1]+ fai[Math.abs(row-prevrow)]+
                                            theta[totalEword.indexOf(entry.englishWords.get(row))]
                                                    [totalFword.indexOf(entry.frenchWords.get(col))]);


                    }
                }

            }
        }


    }

    private void initialize_de(){
        de = new double[EvocabSize];
        for(int i=0; i<EvocabSize; i++)
            de[i] = Double.NEGATIVE_INFINITY;

    }

    private void initialize_dk(){
        dk = new double[maxElen];
        for(int i=0; i<maxElen;i++)
            dk[i] = Double.NEGATIVE_INFINITY;
    }

    private void initialize_dfe(){
        dfe = new double[EvocabSize][FvocabSize];
        for(int row=0; row<EvocabSize; row++)
            for(int col=0; col<FvocabSize; col++)
                dfe[row][col] = Double.NEGATIVE_INFINITY;
    }

    private void initialize_theta(){
        theta = new double[EvocabSize][FvocabSize];
    }

    private void initialize_fai(){
        fai = new double[maxElen];
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

    private void fill_WP(SentencePair sentencePair){
        int esize = sentencePair.englishWords.size();
        int fsize = sentencePair.frenchWords.size();
        W = new double[esize][fsize];
        P = new int[esize][fsize];

        for(int col=0; col<fsize; col++){
            for(int row=0; row<esize; row++){
                int maxrow = Integer.MIN_VALUE;
                double maxvalue = - Double.MAX_VALUE;
                
                double value;
                if(col!=0){
                    for(int prerow=0; prerow<esize;prerow++){
                        value = W[prerow][col-1] + fai[Math.abs(row-prerow)] +
                                theta[totalEword.indexOf(sentencePair.englishWords.get(row))]
                                        [totalFword.indexOf(sentencePair.frenchWords.get(col))];

                        if(value>maxvalue){

                            maxrow = prerow;
                            maxvalue = value;
                        }
                    }

                    P[row][col] = maxrow;
                    W[row][col] = maxvalue;
                }else {
                    P[row][col] = 0;
                    W[row][col] = fai[row] + theta[totalEword.indexOf(sentencePair.englishWords.get(row))]
                            [totalFword.indexOf(sentencePair.frenchWords.get(col))];

                }


            }
        }


    }



    public Alignment alignSentencePair(SentencePair sentencePair) {

        fill_WP(sentencePair);


        int maxrow = Integer.MIN_VALUE;
        double maxvalue = - Double.MAX_VALUE;

        for(int i=0; i<W.length; i++){
            if(W[i][W[0].length-1] > maxvalue){

                maxrow = i;
                maxvalue = W[i][W[0].length-1];
            }

        }

        Alignment alignment = new Alignment();

        alignment.addAlignment(maxrow,sentencePair.frenchWords.size()-1,true);


        for(int i = sentencePair.frenchWords.size()-1;i>=1; i--){
            maxrow = P[maxrow][i];
            alignment.addAlignment(maxrow,i-1,true);
        }


        return alignment;
    }
}
