package org.example;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

class AnomalyDetection{
    private final double[] means;
    private final double[] deviations;
    private double eps;

    AnomalyDetection(Vector[] dataset) {
        int m = dataset[0].size();
        int n = dataset.length;

        means = new double[m];
        deviations = new double[m];

        for(int i=0;i<m;i++){
            List<Double> data = new ArrayList<>(n);

            for(int j=0;j<n;j++){
                data.add((double) dataset[j].x(i));
            }

            Statistics statistics = new Statistics(data, true);
            means[i] = statistics.mean();
            deviations[i] = statistics.std();
        }
    }

    public void validate(Pair<Vector, Boolean>[] validateSet, int range){
        // Choose epsilon

        double minProb = Double.MAX_VALUE, maxProb = Double.MIN_VALUE;

        for(Pair<Vector, Boolean> v : validateSet) {
            double percent = predict(v.getFirst());
            minProb = Math.min(percent, minProb);
            maxProb = Math.max(percent, maxProb);
        }

        double step = (maxProb - minProb) / range;

        double maxMean = Double.MIN_VALUE;
        for(int i=0;i<=range;i++){
            double tempEps = step * i + minProb;

            // (+) : anomaly
            // (-) : normal
            int tp = 0, fp = 0, fn = 0;

            for(Pair<Vector, Boolean> p : validateSet){
                double percent = predict(p.getFirst());

                if(percent < tempEps && p.getSecond()){
                    tp++;
                }
                else if(percent < tempEps && !p.getSecond()){
                    fp++;
                }
                else if((Math.abs(percent - tempEps) < 0.00001 || percent > tempEps) && p.getSecond()){
                    fn++;
                }
            }

            double precision = (float)tp / (tp + fp);
            double recall = (float)tp / (tp + fn);

            double mean = (2 * precision * recall) / (precision + recall);

            if(mean > maxMean){
                maxMean = mean;
                eps = tempEps;
            }
        }
    }

    public double predict(Vector v){
        float percent = 1;

        for(int i=0;i<v.size();i++){
            percent *= new NormalDistribution(means[i], deviations[i]).density(v.x(i));
        }

        return percent;
    }
}
