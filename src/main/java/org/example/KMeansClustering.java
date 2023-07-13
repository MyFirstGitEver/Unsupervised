package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class KMeansClustering {
    private final int minimiseEffort, clusterCount, iterations;
    private Vector[] centers;
    private final Vector[] dataset;
    public KMeansClustering(int minimiseEffort, int clusterCount, int iterations, Vector[] dataset){
        this.minimiseEffort = minimiseEffort;
        this.clusterCount = clusterCount;
        this.dataset = dataset;
        this.iterations = iterations;

        centers = new Vector[clusterCount];
        for(int i=0;i<clusterCount;i++){
            centers[i] = new Vector(dataset[0].size());
        }
    }

    public KMeansClustering(int minimiseEffort, int clusterCount, int iterations, Vector[][] dataset){
        this.minimiseEffort = minimiseEffort;
        this.clusterCount = clusterCount;
        this.iterations = iterations;

        centers = new Vector[clusterCount];

        for(int i=0;i<clusterCount;i++){
            centers[i] = new Vector(dataset[0][0].size());
        }

        // flatten the 2d
        int n = dataset.length;
        int m = dataset[0].length;
        this.dataset = new Vector[n * m];

        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                this.dataset[i * m + j] = dataset[i][j];
            }
        }
    }

    private void randomCenters(){
        Random random = new Random();

        for(int i=0;i<clusterCount;i++) {
            for (int j = 0; j < dataset[0].size(); j++) {
                centers[i].setX(j, random.nextFloat());
            }
        }
    }

    public void train() {
        Vector[] minCenters = new Vector[clusterCount];
        float cost = Float.MAX_VALUE;

        for(int i=0;i<minimiseEffort;i++) {
            randomCenters();
            cluster();

            float currCost = cost();

            if(cost > currCost) {
                for(int j=0;j<minCenters.length;j++) {
                    minCenters[j] = new Vector(centers[j].getPoints());
                }
                cost = currCost;
            }
        }

        centers = minCenters;
    }

    public float cost(){
        float total = 0;

        for(Vector x : dataset){
            float min = Float.MAX_VALUE;

            for(int i=0;i<centers.length;i++){
                if(min > x.distanceFrom(centers[i])){
                    min = x.distanceFrom(centers[i]);
                }
            }

            total += min;
        }

        return total;
    }

    public int clusterNumber(Vector x){
        float min = Float.MAX_VALUE;
        int pos = 0;

        for(int i=0;i<centers.length;i++){
            if(min > x.distanceFrom(centers[i])){
                min = x.distanceFrom(centers[i]);
                pos = i;
            }
        }

        return pos;
    }

    public Vector getCenter(int index){
        return centers[index];
    }
    private void cluster() {
        for(int a=0;a<iterations;a++){
            if(a % 40 == 0) {
                System.out.println(a + " iterations have passed! Cost: " + cost());
            }

            Vector[] newCenters = new Vector[centers.length];
            int[] clusterSize = new int[centers.length];

            for(int i=0;i<newCenters.length;i++){
                newCenters[i] = new Vector(centers[0].size());
            }

            for(Vector x : dataset){
                int index = clusterNumber(x);

                newCenters[index].add(x);
                clusterSize[index]++;
            }

            for(int i=0;i<centers.length;i++){
                if(clusterSize[i] != 0){
                    centers[i] = newCenters[i].scaleBy(1.0f / clusterSize[i]);
                }
            }
        }
    }
}

class ImageProcessing{
    private final Vector[][] pixels;
    public ImageProcessing(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        pixels = new Vector[image.getWidth()][image.getHeight()];

        for(int i=0;i<image.getWidth();i++){
            for(int j=0;j<image.getHeight();j++){
                Color color = new Color(image.getRGB(i, j));

                pixels[i][j] = new Vector(color.getRed(), color.getGreen(), color.getBlue());
            }
        }
    }

    public void compressUsingKMeansAndSave(int compressDegree, String savePath) throws IOException {
        KMeansClustering model = new KMeansClustering(1, compressDegree, 10_000, pixels);
        model.train();

        for(int i=0;i<pixels.length;i++){
            for(int j=0;j<pixels[0].length;j++){
                Vector pixel = pixels[i][j];
                Vector bestFit = model.getCenter(model.clusterNumber(pixel));

                pixels[i][j] = bestFit;
            }
        }

        save(savePath);
    }

    public void save(String savePath) throws IOException {
        BufferedImage image = new BufferedImage(pixels.length, pixels[0].length, BufferedImage.TYPE_INT_RGB);

        for(int i=0;i<pixels.length;i++){
            for(int j=0;j<pixels[0].length;j++){
                image.setRGB(i, j, pixels[i][j].intRGB());
            }
        }

        ImageIO.write(image, "png", new File(savePath));
    }
}