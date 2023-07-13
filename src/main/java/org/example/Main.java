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
    public static void main(String[] args) throws IOException {
//        ExcelReader reader =new ExcelReader("D:\\live.xlsx");
//
//        List<String> features = new ArrayList<>();
//        features.add("status_id");
//        features.add("status_type");
//        features.add("status_published");
//        features.add("num_reactions");
//        features.add("num_comments");
//        features.add("num_shares");
//        features.add("num_likes");
//        features.add("num_loves");
//        features.add("num_wows");
//        features.add("num_hahas");
//        features.add("num_sads");
//        features.add("num_angrys");
//
//        Pair<Vector, Float>[] dataset = reader.createLabeledDataset(Integer.MAX_VALUE, 0, features);
//
//        featureScaling(dataset, new Pair[0]);
//
//        Vector[] data = new Vector[dataset.length];
//
//        for(int i=0;i<data.length;i++) {
//            data[i] = dataset[i].first;
//        }
//
//        KMeansClustering clustering = new KMeansClustering(5, 3, 1000, data);
//        clustering.train();
//
//        int[] indexes = new int[dataset.length];
//
//        int notGroup0 = 0;
//        for(int i=0;i<dataset.length;i++) {
//            indexes[i] = clustering.clusterNumber(dataset[i].first);
//
//            if(indexes[i] != 0) {
//                notGroup0++;
//            }
//        }
//
//        System.out.println(notGroup0);
//        System.out.println("cost: " + clustering.cost());


    }

    private static void featureScaling(Pair<Vector, Float>[] trainSet, Pair<Vector, Float>[] testSet) {
        float[] mean = new float[trainSet[0].first.size()];
        float[] std = new float[trainSet[0].first.size()];

        for(int i=0;i<mean.length;i++) {
            for (Pair<Vector, Float> train : trainSet) {
                mean[i] += train.first.x(i);
            }

            mean[i] /= trainSet.length;
        }

        for(int i=0;i<mean.length;i++) {
            for (Pair<Vector, Float> train : trainSet) {
                double term = (train.first.x(i) - mean[i]);

                std[i] += term * term;
            }

            std[i] = (float) Math.sqrt(std[i] / trainSet.length);
        }

        for (Pair<Vector, Float> train : trainSet) {
            for (int i = 0; i < trainSet[0].first.size(); i++) {
                train.first.setX(i, (train.first.x(i) - mean[i]));

                if(std[i] != 0) {
                    train.first.setX(i, train.first.x(i) / std[i]);
                }
            }
        }

        for (Pair<Vector, Float> test : testSet) {
            for (int i = 0; i < testSet[0].first.size(); i++) {
                test.first.setX(i, (test.first.x(i) - mean[i]));

                if(std[i] != 0) {
                    test.first.setX(i, test.first.x(i) / std[i]);
                }
            }
        }
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