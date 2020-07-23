package edu.berkeley.nlp.assignments.assign1.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;

import edu.berkeley.nlp.assignments.assign1.student.MyHashmap;

import java.util.Arrays;
import java.util.regex.Matcher;

public class LmFactory implements LanguageModelFactory {

	/**
	 * Returns a new NgramLanguageModel; this should be an instance of a class that you implement.
	 * Please see edu.berkeley.nlp.langmodel.NgramLanguageModel for the interface specification.
	 *
	 * @param trainingData
	 */
	public NgramLanguageModel newLanguageModel(Iterable<List<String>> trainingData) {

		return new TrigramLanguageModel(trainingData);

	}

	private MyHashmap bigramHashMap ;
	private MyHashmap trigramHashMap ;

	private long[] wordCounter = new long[10];


	private int index1 = 0;
	private int index2 = 0;
	private int index3 = 0;


	private int [] uiFertilityFirst;
	private int [] uiFertilityMiddle;
	private int [] uiFertilityLast;

	private int [] biFertilityFisrt;
	private int [] biFertilityLast;

	private long [] cacheKey = new long[1];
	private double [] cacheValue ;

	private long [] bicacheKey = new long[1];
	private double [] bicacheValeue;

	long maskbit20 = 0xfffff;

	boolean nosanflag = false;


	private double discount = 0.9;

	private double unseenProb = 0;


	public class TrigramLanguageModel implements NgramLanguageModel {


		public TrigramLanguageModel(Iterable<List<String>> sentenceCollection) {

			System.out.println("Start building");

			if (Runtime.getRuntime().maxMemory()> 1000000000)
				nosanflag = true;

			if(nosanflag){
				bigramHashMap = new MyHashmap("bigram");
				trigramHashMap = new MyHashmap("trigram");
			}else{
				bigramHashMap = new MyHashmap("sanbigram");
				trigramHashMap = new MyHashmap("santrigram");

			}



			int sent = 0;
			for (List<String> sentence : sentenceCollection) {
				if (sent % 1000000 == 0) {
					System.out.println("On sentence " + sent);
				}
				sent++;
				List<String> stoppedSentence = new ArrayList<String>(sentence);
				stoppedSentence.add(0, NgramLanguageModel.START);
				stoppedSentence.add(NgramLanguageModel.STOP);


				for (int i = 0; i < stoppedSentence.size(); i++) {

					if (i == 0) {
						index1 = EnglishWordIndexer.getIndexer().addAndGetIndex(stoppedSentence.get(i));
					}

					if (index1 >= wordCounter.length)
						wordCounter = CollectionUtils.copyOf(wordCounter, wordCounter.length * 2);
					wordCounter[index1]++;


					if (i == stoppedSentence.size() - 1)
						break;

					if (i == 0) {
						index2 = EnglishWordIndexer.getIndexer().addAndGetIndex(stoppedSentence.get(i + 1));
					}

					long index2key = (long) index1 << 20L | (long) index2;

					bigramHashMap.put(index2key);

					if (i == stoppedSentence.size() - 2)
						break;

					index3 = EnglishWordIndexer.getIndexer().addAndGetIndex(stoppedSentence.get(i + 2));


					long index3key = ((long) index1 << 40L) | ((long) index2 << 20L) | index3;

					trigramHashMap.put(index3key);


					index1 = index2;
					index2 = index3;
				}
			}

			unseenProb = (double)1 / (double)wordCounter.length;

			System.out.println("Building complete");
			buildBiFertility();
			buildTriFertility();
		}


		public int getOrder() {
			return 3;
		}


		public long getCount(int[] ngram) {

			System.out.println("Binum :"+bigramHashMap.getSize());
			System.out.println("Trinum :"+trigramHashMap.getSize());

			if (ngram.length > 1) {
				if (ngram.length > 2) {
					long w0 = ngram[0];
					long w1 = ngram[1];
					long w2 = ngram[2];
 					long trikey = w0 << 40 | w1 << 20 | w2;

					int tripos = (int) (trikey % trigramHashMap.keysLenth());

					int tricount = 0;

					while(trigramHashMap.getKeys()[tripos] != trikey){

						tripos++;
						if(tripos == trigramHashMap.keysLenth())
							tripos = 0;

						tricount ++;
						if(tricount == trigramHashMap.keysLenth())
							return 0;
					}
					return trigramHashMap.getValues()[tripos];

				} else {
					long biw0 = ngram[0];
					long biw1 = ngram[1];


					long bikey = biw0 << 20 |biw1;

					int bipos = (int) (bikey % bigramHashMap.keysLenth());

					int bicount =0;

					while(bigramHashMap.getKeys()[bipos] !=  bikey){
							bipos++;
							if(bipos == bigramHashMap.keysLenth())
								bipos = 0;

							bicount ++;
							if(bicount == bigramHashMap.keysLenth())
								return 0;
					}
					return bigramHashMap.getValues()[bipos];
				}
			} else if (ngram.length == 1) {
				int word = ngram[0];
				if (word < 0 || word > wordCounter.length)
					return 0;
				return wordCounter[word];

			} else {
				return 0;
			}

		}


		public void buildBiFertility(){
			uiFertilityFirst = new int[wordCounter.length];
			Arrays.fill(uiFertilityFirst,0);
			uiFertilityLast = new int[wordCounter.length];
			Arrays.fill(uiFertilityLast,0);

			for(int i=0; i<bigramHashMap.keysLenth(); i++){
				if(i%1000000 ==0)
					System.out.println("In Bi: "+i+" / "+bigramHashMap.keysLenth());
				if(bigramHashMap.getKeys()[i]!=-1){
					uiFertilityLast[(int)(bigramHashMap.getKeys()[i] & maskbit20)]++;
					uiFertilityFirst[(int)(bigramHashMap.getKeys()[i] & (maskbit20<<20)>>20)]++;
				}
			}
		}

		public void buildTriFertility(){

			uiFertilityMiddle = new int[wordCounter.length];
			Arrays.fill(uiFertilityMiddle,0);
			biFertilityFisrt = new int[bigramHashMap.keysLenth()];
			Arrays.fill(biFertilityFisrt,0);
			biFertilityLast = new int[bigramHashMap.keysLenth()];
			Arrays.fill(biFertilityLast,0);

			for(int i=0; i<trigramHashMap.keysLenth();i++){
				if(i%10000000 ==0)
					System.out.println("In Tri: "+i+" / "+trigramHashMap.keysLenth());
				if(trigramHashMap.getKeys()[i]!=-1){

					long middleLow = trigramHashMap.getKeys()[i] & ((maskbit20<<20)|maskbit20);

					int pos1 = bigramHashMap.getPos(middleLow,bigramHashMap.getKeys());
					biFertilityLast[pos1]++;


					long middleHigh = (trigramHashMap.getKeys()[i] & ((maskbit20<<40)|maskbit20<<20))>>20;

					int pos2 = bigramHashMap.getPos(middleHigh,bigramHashMap.getKeys());
					biFertilityFisrt[pos2]++;

					long middle = (trigramHashMap.getKeys()[i] & (maskbit20<<20))>>20;
					uiFertilityMiddle[(int)middle]++;

				}
			}
		}

		public double triProb(long w1, long w2, long w3){
			double w1w2w3num = 0;
			double w1w2num = 0;
			double w1w2xnum = 0;


			long w1w2w3key = (w1<<40) |(w2<<20)| (w3);

			long w1w2key = (w1<<20) | (w2);

			int pos2 = bigramHashMap.getPos(w1w2key,bigramHashMap.getKeys());
			if(bigramHashMap.getKeys()[pos2] == w1w2key){
				w1w2num = bigramHashMap.getValues()[pos2];
				w1w2xnum = biFertilityFisrt[pos2];
			}

			if(w1w2num==0)
				return biProb(w2, w3);


			int pos1 = trigramHashMap.getPos(w1w2w3key, trigramHashMap.getKeys());

			if(trigramHashMap.getKeys()[pos1] == w1w2w3key)
				w1w2w3num = (double) (trigramHashMap.getValues()[pos1]);



			double alpha = discount*w1w2xnum/w1w2num;

			return Math.max(w1w2w3num-discount ,0) / w1w2num + alpha*biProb(w2, w3);


		}

		public double biProb(long w2 , long w3){
			if (w3 < 0 || w3 >= wordCounter.length) {
				return unseenProb;
			}

			double xw2w3num = 0;
			double xw2xnum = 0;
			double w2xnum = 0;


			long w2w3key = (w2<<20) | w3;

			if(w2>0 && w2<wordCounter.length){
				int w2loc = (int)w2;
				xw2xnum = uiFertilityMiddle[w2loc];
				w2xnum = uiFertilityFirst[w2loc];
			}

			if(xw2xnum == 0)
				return uniProb(w3);

			int pos1 = bigramHashMap.getPos(w2w3key,bigramHashMap.getKeys());
			if(bigramHashMap.getKeys()[pos1] == w2w3key)
				xw2w3num = biFertilityLast[pos1];



			double alpha = discount * w2xnum/xw2xnum;

			return Math.max(xw2w3num - discount,0)/xw2xnum + alpha* uniProb(w3);

		}

		public double uniProb(long w3){
			if (w3 < 0 || w3 >= wordCounter.length) {
				return unseenProb;
			}
			double xw3num = uiFertilityLast[(int) w3];
			if (xw3num == 0)
				return unseenProb;
			return xw3num/bigramHashMap.getSize();
		}



		public double getNgramLogProbability(int[] ngram, int from, int to) {
			if(to - from == 3){

				double res = 0;

				long w1 = (long)ngram[0];
				long w2 = (long)ngram[1];
				long w3 = (long)ngram[2];


				if(nosanflag) {
					if (cacheKey.length == 1) {
						cacheKey = new long[30000000];
						Arrays.fill(cacheKey, -1);
						cacheValue = new double[30000000];
						Arrays.fill(cacheValue, -1);
					}
				}else {
					if (cacheKey.length == 1) {
						cacheKey = new long[300];
						Arrays.fill(cacheKey, -1);
						cacheValue = new double[300];
						Arrays.fill(cacheValue, -1);
					}
				}


				int  triPos = (int) ((w1<<40 | w2<<20 | w3) % cacheKey.length);
				if(cacheKey[triPos]!=-1 && cacheKey[triPos] == (w1<<40 | w2<<20 | w3))
					return cacheValue[triPos];
				else{
					res = Math.log(triProb(w1, w2, w3));
					cacheValue[triPos] = res;
					cacheKey[triPos] =(w1<<40 | w2<<20 | w3);
					return res;

				}

			}else if(to - from == 2){
				long w2 = ngram[from];
				long w3 = ngram[from+1];

				double res = 0;

				if(nosanflag) {
					if (bicacheKey.length == 1) {
						bicacheKey = new long[25000000];
						Arrays.fill(bicacheKey, -1);
						bicacheValeue = new double[25000000];
						Arrays.fill(bicacheValeue, -1);
					}
				}else{
					if (bicacheKey.length == 1) {
						bicacheKey = new long[300];
						Arrays.fill(bicacheKey, -1);
						bicacheValeue = new double[300];
						Arrays.fill(bicacheValeue, -1);
					}
				}

				int bipos = (int)((w2<<20|w3) % bicacheKey.length);
				if(bicacheKey[bipos] != -1 && bicacheKey[bipos] == (w2<<20|w3))
					return bicacheValeue[bipos];
				else{
					res = Math.log(biProb(w2, w3));
					bicacheValeue[bipos] = res;
					bicacheKey[bipos] = (w2<<20|w3);
					return res;
				}


			}else if(to-from ==1){
				long w3 = ngram[from];
				return Math.log(uniProb(w3));

			}else
				return Math.log(unseenProb);
		}
	}
}