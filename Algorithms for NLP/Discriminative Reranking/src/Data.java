package edu.berkeley.nlp.assignments.rerank.student;

public class Data {
    private int[][] bestKFea;
    private int bestF1Index;

    public Data(int[][] bestKFea, int bestF1Index) {
        this.bestKFea = bestKFea;
        this.bestF1Index = bestF1Index;
    }

    public int [][] getBestkFeature(){
        return this.bestKFea;
    }

    public int getBestF1Index(){
        return this. bestF1Index;
    }

}
