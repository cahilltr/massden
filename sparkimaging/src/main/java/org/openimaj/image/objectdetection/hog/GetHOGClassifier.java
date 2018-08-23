package org.openimaj.image.objectdetection.hog;

import org.openimaj.image.analysis.algorithm.histogram.binning.SimpleBlockStrategy;
import org.openimaj.image.feature.dense.gradient.HOG;

public class GetHOGClassifier {

    public static HOGClassifier getHOGClassifier() {
        HOGClassifier hogClassifier = new HOGClassifier();
        hogClassifier.hogExtractor = new HOG(new SimpleBlockStrategy(2));
        return hogClassifier;
    }
}
