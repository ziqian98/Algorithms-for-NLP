package edu.berkeley.nlp.assignments.assign1.student;


import java.util.Arrays;

public class MyHashmap  {

    private long [] keys;

    private int [] values;

    private int size = 0;

    private double loadFactor = 0.9;


    public MyHashmap(String str){
        if (str.equals("bigram")){
            keys = new long[12000000];
            values = new int[12000000];
        }else if (str.equals("trigram")){
            keys = new long[62000000];
            values = new int[62000000];
        } else if(str.equals("sanbigram")){
            keys = new long[10];
            values = new int[10];
        }else{
            keys = new long[10];
            values = new int[10];
        }

        Arrays.fill(values,-1);
        Arrays.fill(keys, (long)-1);
    }

    public int getPos(long key , long [] keycopy){

        int pos =  (int)  (key % keycopy.length);


        while(keycopy[pos]!=-1 && keycopy[pos]!=key){
            pos++;
            if(pos == keycopy.length)
                pos = 0;
        }
        return pos;
    }

    public void put(long key){

        if((double)size/(double)keys.length >= loadFactor){
            rehash();
        }

        int pos = getPos(key, keys);



        if (keys[pos] == key){
            values[pos]++;
        }

        if (keys[pos] == -1){
            values[pos]++;
            keys[pos] = key;
            size++;
        }

    }

    public void copyHash(long oldkey, int oldvalue, long [] newkeys, int [] newvalues){
        int newpos = getPos(oldkey,newkeys);


        newvalues[newpos] = oldvalue;
        newkeys[newpos] = oldkey;

    }

    public void rehash(){
        long [] newkeys = new long[keys.length * 12/10 ];
        int [] newvalues = new int[values.length * 12/10];

        Arrays.fill(newkeys,-1);
        Arrays.fill(newvalues,-1);


        for(int i=0; i<keys.length; i++){
            long oldkey = keys[i];
            if(oldkey != -1) {
                int oldvalue = values[i];
                copyHash(oldkey,oldvalue, newkeys, newvalues);
            }
        }

        keys = newkeys;
        values = newvalues;
    }

    public int getSize(){
        return size;
    }

    public int keysLenth(){
        return keys.length;
    }

    public long []  getKeys(){
        return keys;
    }

    public int [] getValues(){
        return values;
    }







}
