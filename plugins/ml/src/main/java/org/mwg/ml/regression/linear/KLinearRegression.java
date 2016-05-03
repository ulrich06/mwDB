package org.mwg.ml.regression.linear;

import org.mwg.ml.classifier.common.KSlidingWindowManagingNode;

/**
 * Created by andre on 4/26/2016.
 */
public interface KLinearRegression extends KSlidingWindowManagingNode {

    /**
     * @return Regression coefficients. Intercept is in place of response index.
     */
    double[] getCoefficients();

    /**
     * @return Intercept of linear regression
     */
    double getIntercept();

    /**
     * Sets L2 regularization coefficient (0 by default)
     * @param l2 new coefficient
     */
    void setL2Regularization(double l2);

    /**
     *
     * @return
     */
    double getL2Regularization();
}
