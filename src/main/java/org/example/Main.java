package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

//        KMeansClustering trainer = new KMeansClustering(1, 3, 10,
//                loadData("test.txt", 2));
//        trainer.train();

//        new ImageProcessing("test.png").compressUsingKMeansAndSave(20, "save.png");

//        AnomalyDetection detector = new AnomalyDetection(loadData("training.txt", 2));
//
//        Vector[] validateX = loadData("validate.txt", 2);
//        Pair<Vector, Boolean>[] validateSet = new Pair[validateX.length];
//
//        BufferedReader reader = new BufferedReader(new FileReader("labels.txt"));
//        String line;
//
//        int index = 0;
//        while((line = reader.readLine()) != null){
//            String[] labels = line.split(" ");
//
//            for(int i=0;i<labels.length;i++){
//                validateSet[index] = new Pair<>(validateX[index], labels[i].equals("1"));
//                index++;
//            }
//        }
//
//        detector.validate(validateSet, 1000);

//        AnomalyDetection detector = new AnomalyDetection(loadData("xtrain.txt", 11));
//
//        Vector[] validateX = loadData("xval.txt", 11);
//        Pair<Vector, Boolean>[] validateSet = new Pair[validateX.length];
//
//        BufferedReader reader = new BufferedReader(new FileReader("yval.txt"));
//        String line;
//
//        int index = 0;
//        while((line = reader.readLine()) != null){
//            validateSet[index] = new Pair<>(validateX[index], line.trim().equals("1"));
//            index++;
//        }
//
//        detector.validate(validateSet, 1000);

public class Main {
    public static void main(String[] args) {
    }

    static Vector[] loadData(String path, int vectorSize) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        String x;
        List<Vector> list = new ArrayList<>();

        int lastLength = 0;
        Vector v = null;
        while((x = reader.readLine()) != null){
            String data = x.trim().replaceAll(" +", " ");
            String[] entries = data.split(" ");

            if(lastLength == 0){
                v = new Vector(vectorSize);
            }

            for(int i=0;i<entries.length;i++){
                v.setX(i + lastLength, Float.parseFloat(entries[i]));
            }

            if(lastLength + entries.length == vectorSize){
                lastLength = 0;
                list.add(v);
            }
            else{
                lastLength += entries.length;
            }
        }

        reader.close();

        return list.toArray(new Vector[0]);
    }
}