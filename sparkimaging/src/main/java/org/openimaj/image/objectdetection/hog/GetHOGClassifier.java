package org.openimaj.image.objectdetection.hog;

import de.bwaldvogel.liblinear.SolverType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.image.analysis.algorithm.histogram.binning.QuadtreeStrategy;
import org.openimaj.image.analysis.algorithm.histogram.binning.SimpleBlockStrategy;
import org.openimaj.image.feature.dense.gradient.HOG;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.model.ModelAnnotator;

import static org.openimaj.ml.annotation.linear.GetLinearAnnotator.getLinearAnnotator;

public class GetHOGClassifier {

    public static HOGClassifier getHOGClassifier(int height, int width) {
        HOGClassifier hogClassifier = new HOGClassifier();
        hogClassifier.width = 100;
        hogClassifier.height = 100;
//        hogClassifier.classifier = new ModelAnnotator<DoubleFV, Boolean>();

        hogClassifier.classifier = getLinearAnnotator();

        hogClassifier.hogExtractor = new HOG(new QuadtreeStrategy(2));

        return hogClassifier;
    }
}
