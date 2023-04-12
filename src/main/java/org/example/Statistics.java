package org.example;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

class Statistics {
    private final List<Double> dataset;
    private final Double populationDeviation;
    private Double cachedMean;
    private Double cachedStd;
    private Integer size;

    public enum TEST_TYPE {
        TWO_SIDED,
        LEFT_TAIL,
        RIGHT_TAIL
    }

    public Statistics(List<Double> dataset, Double populationDeviation) {
        this.dataset = dataset;
        this.populationDeviation = populationDeviation;

        Collections.sort(dataset);
    }

    public Statistics(List<Double> dataset, boolean isPopulation){
        this.dataset = dataset;

        if(isPopulation){
            populationDeviation = mDeviation(0);
        }
        else{
            populationDeviation = null;
        }
    }

    public Statistics(Double cachedStd, boolean isPopulation, Double cachedMean, int size){
        if(isPopulation){
            populationDeviation = cachedStd;
        }
        else{
            populationDeviation = null;
        }

        this.cachedMean = cachedMean;
        this.cachedStd = cachedStd;
        this.size = size;

        dataset = null;
    }

    public int size(){
        if(size != null){
            return size;
        }

        return dataset.size();
    }

    public double mean(){
        if(cachedMean == null){
            double mean = 0;
            for (Double aDouble : dataset) {
                mean += aDouble;
            }

            cachedMean = mean / dataset.size();
        }

        return cachedMean;
    }

    public double std(){
        if(cachedStd == null){
            cachedStd = Objects.requireNonNullElseGet(populationDeviation, () -> mDeviation(1));
        }

        return cachedStd;
    }

    public double skewScore(){
        double mean = mean();
        double median = mMedian();

        return 3 * (mean - median) / mDeviation(0);
    }

    public Pair<Double, Double> confidenceIntervalOfNPercent(double n){
        double percent = n / 100;
        double standardError = std() / Math.sqrt(size());

        double pInTheCorner = (1 - percent) / 2;
        double statistic;

        if(populationDeviation != null){
            statistic = Math.abs(new NormalDistribution().inverseCumulativeProbability(pInTheCorner));
        }
        else{
            statistic = Math.abs(new TDistribution(size()).inverseCumulativeProbability(pInTheCorner));
        }

        // middle n% of data.
        return new Pair<>(mean() - statistic * standardError, mean() + statistic * standardError);
    }

    public double pValue(double hypothesis, TEST_TYPE testType){
        double standardError = std() / Math.sqrt(size());
        double score = (mean() - hypothesis) / standardError;
        return mCalculatePValue(testType, score);
    }

    public double pdf(double x){
        if(populationDeviation != null){
            return new NormalDistribution(mean(), std()).density(x);
        }

        return new TDistribution(mean(), std()).density(x);
    }

    public double pValueWithDiff(Statistics statistics, TEST_TYPE testType, double hypothesis){

        double mean = mean() - statistics.mean();
        double standardError = mStandardErrorOfTwoSamplesDiff(statistics);
        double score = (mean - hypothesis) / standardError;

        return mCalculatePValue(testType, score);
    }

    public boolean test(double hypothesis, TEST_TYPE testType, double n){
        double percent = n / 100;
        double standardError = std() / Math.sqrt(size());

        AbstractRealDistribution distribution = mFetchDistribution();

        double pInTheCorner = (1 - percent) / 2;
        double bound = Math.abs(distribution.inverseCumulativeProbability(pInTheCorner));

        double score = (mean() - hypothesis) / standardError;

        return mTesting(score, bound, testType);
    }

    public boolean testDiff(Statistics statistics, double hypothesis, TEST_TYPE testType, double n){
        double percent = n / 100;
        double standardError = mStandardErrorOfTwoSamplesDiff(statistics);

        AbstractRealDistribution distribution = mFetchDistribution();

        double pInTheCorner = (1 - percent) / 2;
        double bound = Math.abs(distribution.inverseCumulativeProbability(pInTheCorner));
        double score = (mean() - statistics.mean() - hypothesis) / standardError;

        return mTesting(score, bound, testType);
    }

    public Pair<Double, Double> calculateDiffWithConfidence(Statistics statistics, double confidenceInPercent){
        double percent = confidenceInPercent / 100;
        double pInTheCorner = (1 - percent) / 2;

        double standardError = mStandardErrorOfTwoSamplesDiff(statistics);
        double mean = mean() - statistics.mean();

        if(populationDeviation != null && statistics.populationDeviation != null){
            double z = Math.abs(new NormalDistribution().inverseCumulativeProbability(pInTheCorner));

            return new Pair<>(mean - z * standardError, mean + z * standardError);
        }
        else{
            double t = Math.abs(new TDistribution(size() + statistics.size() - 2)
                    .inverseCumulativeProbability(pInTheCorner));

            return new Pair<>(mean - t * standardError, mean + t * standardError);
        }
    }
    public double correlationWith(Statistics statistics) throws Exception{
        if(statistics.size() != size()){
            throw new Exception("Two datasets does not have the same size!");
        }

        double cov = 0;
        double mean1 = mean();
        double mean2 = statistics.mean();

        List<Double> dataset2 = statistics.dataset;

        for(int i=0;i<size();i++){
            cov += (dataset.get(i) - mean1) * (dataset2.get(i) - mean2);
        }

        if(populationDeviation != null && statistics.populationDeviation != null){
            cov /= size();

            return cov / (populationDeviation * statistics.populationDeviation);
        }
        else{
            cov /= (size() - 1);

            return cov / (std() * statistics.std());
        }
    }

    private boolean mTesting(double statistic, double bound, TEST_TYPE testType){
        if(statistic < -bound && testType == TEST_TYPE.RIGHT_TAIL){
            return true; // reject
        }
        else if(statistic > bound && testType == TEST_TYPE.LEFT_TAIL){
            return true; // reject
        }
        else if(Math.abs(statistic) > bound && testType == TEST_TYPE.TWO_SIDED){
            return true; // reject
        }

        return false; // not enough statistical evidence to reject H0
    }

    private double mMedian(){
        if(dataset.size() % 2 == 0){
            return (dataset.get(dataset.size() / 2 - 1) + dataset.get(dataset.size() / 2)) / 2;
        }

        return dataset.get(dataset.size() / 2);
    }

    private double mDeviation(int sampleCoeff){
        double deviation = 0;
        double mean = mean();

        for(int i=0;i<dataset.size();i++){
            deviation += (dataset.get(i) - mean) * (dataset.get(i) - mean);
        }

        return Math.sqrt((deviation / (dataset.size() - sampleCoeff)));
    }

    private double mCalculatePValue(TEST_TYPE testType, double score){
        AbstractRealDistribution distribution = mFetchDistribution();

        if(testType == TEST_TYPE.LEFT_TAIL){
            return distribution.cumulativeProbability(score);
        }
        else if(testType == TEST_TYPE.RIGHT_TAIL){
            return 1 - distribution.cumulativeProbability(score);
        }
        else{
            return 2 * (1 - distribution.cumulativeProbability(Math.abs(score)));
        }
    }

    private double mStandardErrorOfTwoSamplesDiff(Statistics statistics){
        int n1 = size();
        int n2 = statistics.size();

        if(populationDeviation != null && statistics.populationDeviation != null){
            double var1 = populationDeviation * populationDeviation;
            double var2 = statistics.populationDeviation * statistics.populationDeviation;

            return Math.sqrt(var1 / n1 + var2 / n2);
        }
        else{
            double var1 = std();
            var1 *= var1;
            double var2 = statistics.std();
            var2 *= var2;

            // assuming they have the same population variance
            double pooledVar = (var1*(n1 - 1) + var2*(n2 - 1)) / (n1 + n2 - 2);
            return Math.sqrt(pooledVar / n1 + pooledVar / n2);
        }
    }

    private AbstractRealDistribution mFetchDistribution(){
        if(populationDeviation != null){
            return new NormalDistribution();
        }

        return new TDistribution(size());
    }
}

