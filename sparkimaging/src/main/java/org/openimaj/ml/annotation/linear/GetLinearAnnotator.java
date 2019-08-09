package org.openimaj.ml.annotation.linear;

import de.bwaldvogel.liblinear.SolverType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.ml.annotation.Annotator;

import java.util.ArrayList;

public class GetLinearAnnotator {

    public static Annotator getLinearAnnotator() {
        LiblinearAnnotator<DoubleFV, Boolean> annotator = new LiblinearAnnotator<DoubleFV, Boolean>(
                new IdentityFeatureExtractor<DoubleFV>(), LiblinearAnnotator.Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 0.01, 0.01, 1, true);

        annotator.internal.annotationsList = new ArrayList<Boolean>();

        return annotator;
    }
}
