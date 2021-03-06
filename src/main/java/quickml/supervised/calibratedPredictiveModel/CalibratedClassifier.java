package quickml.supervised.calibratedPredictiveModel;

import quickml.data.AttributesMap;
import quickml.data.PredictionMap;
import quickml.supervised.classifier.Classifier;
import quickml.supervised.regressionModel.IsotonicRegression.PoolAdjacentViolatorsModel;

import java.io.Serializable;
import java.util.Set;


/**
 * Created by alexanderhawk on 3/10/14.
 */

/*
  This class uses the Pool-adjacent violators algorithm to calibrate the probabilities returned by a binary classifier, where postive
  classifications have a label of 1.0, and negative classifications have a label 0.0.
*/
public class CalibratedClassifier implements Classifier {
    private static final long serialVersionUID = 8291739965981425742L;
    public PoolAdjacentViolatorsModel pavFunction;
    public Classifier wrappedPredictiveModel;

    public CalibratedClassifier(Classifier wrappedPredictiveModel, PoolAdjacentViolatorsModel PAVFunction) {
        this.wrappedPredictiveModel = wrappedPredictiveModel;
        this.pavFunction = PAVFunction;
    }
    @Override
    public double getProbability(AttributesMap attributes, Serializable label) {
        double rawProbability = wrappedPredictiveModel.getProbability(attributes, label);
        return pavFunction.predict(rawProbability);
    }

    @Override
    public double getProbabilityWithoutAttributes(AttributesMap attributes, Serializable label, Set<String> attributesToIgnore) {
        double rawProbability = wrappedPredictiveModel.getProbabilityWithoutAttributes(attributes, label, attributesToIgnore);
        return pavFunction.predict(rawProbability);
    }


    @Override
    public PredictionMap predict(final AttributesMap attributes) {
        PredictionMap predictionMap = wrappedPredictiveModel.predict(attributes);
        double positiveClassProb =  pavFunction.predict(wrappedPredictiveModel.getProbability(attributes, 1.0));
        predictionMap.put(Double.valueOf(1.0), positiveClassProb);
        predictionMap.put(Double.valueOf(0.0), 1.0 - positiveClassProb);

        return predictionMap;
    }

    @Override
    public PredictionMap predictWithoutAttributes(final AttributesMap attributes, Set<String> attributesToIgnore) {
        PredictionMap predictionMap = wrappedPredictiveModel.predictWithoutAttributes(attributes, attributesToIgnore);
        double positiveClassProb =  pavFunction.predict(wrappedPredictiveModel.getProbabilityWithoutAttributes(attributes, 1.0, attributesToIgnore));
        predictionMap.put(Double.valueOf(1.0), positiveClassProb);
        predictionMap.put(Double.valueOf(0.0), 1.0 - positiveClassProb);

        return predictionMap;
    }

    @Override
    public Serializable getClassificationByMaxProb(AttributesMap attributes) {
        PredictionMap predictionMap = predict(attributes);
        double maxProb = 0;
        Serializable classification = null;
        for (Serializable prediction : predictionMap.keySet()) {
            double prob = predictionMap.get(prediction);
            if (prob > maxProb) {
                maxProb = prob;
                classification = prediction;
            }
        }
        if (classification == null)
            throw new RuntimeException("unable to make a classification");

        return classification;
    }
}


