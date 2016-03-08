from pandas import DataFrame
import pandas as pd
from sklearn.preprocessing import Imputer
from sklearn.naive_bayes import BernoulliNB, GaussianNB
import numpy as np


# http://scikit-learn.org/stable/modules/generated/sklearn.preprocessing.Imputer.html
# http://stackoverflow.com/questions/30317119/classifiers-in-scikit-learn-that-handle-nan-null
# https://github.com/wrightaprilm/WorkshopTTU/blob/master/data-crunching/03-checking_your_data.md
# http://www.tutorialspoint.com/python/python_dictionary.htm
# http://chrisalbon.com/python/pandas_list_unique_values_in_column.html

def check_mapping_or_add(my_col, x):
    if x in cat_var_mapping[my_col]:
        return cat_var_mapping[my_col][x]
    else:
        count = len(cat_var_mapping[col])
        cat_var_mapping[my_col][x] = count
        return count

def train_gaussian(data, target, run_type):
    gnb = GaussianNB()
    model = gnb.fit(data, target)
    y_pred_train = model.predict(data)
    print(run_type + ": Number of mislabeled points out of a total %d points : %d" % (
        len(train_values), (train_target != y_pred_train).sum()))

def train_bernoulli(data, target, run_type):
    clf = BernoulliNB()
    clf.fit(data, target)
    y_pred_train = clf.predict(data)
    print(run_type + ": Number of mislabeled points out of a total %d points : %d" % (
        len(train_values), (train_target != y_pred_train).sum()))

def binarize(columnName, df, features=None):
    df[columnName] = df[columnName].astype(str)
    if(features is None):
        features = np.unique(df[columnName].values)
    print(features)
    for x in features:
        df[columnName+'_' + x] = df[columnName].map(lambda y:
                                                    1 if y == x else 0)
    df.drop(columnName, inplace=True, axis=1)
    return df, features


trainDF = DataFrame.from_csv('/Users/cahillt/Downloads/bnp/train.csv')
testDF = DataFrame.from_csv('/Users/cahillt/Downloads/bnp/test.csv')

for col in trainDF:
    print col, trainDF[col].dtypes

# Create Binary fields
# for col in trainDF[2:]:
#     if trainDF[col].dtype == 'object':
#         trainDF, bin_features = binarize(col, trainDF)
#         testDF, _ = binarize(col, test, binfeatures)

# Map categorical variables to integer variables.
cat_var_mapping = {}

for col in trainDF:
    if trainDF[col].dtypes == 'object':
        unique = pd.unique(trainDF[col].ravel())
        unique_map = {}
        for i in range(0, len(unique)):
            unique_map[unique[i]] = i
        cat_var_mapping[col] = unique_map

for col in trainDF:
    if trainDF[col].dtypes == 'object':
        trainDF[col] = trainDF[col].map(cat_var_mapping[col])

for col in testDF:
    if testDF[col].dtypes == 'object':
        testDF[col] = testDF[col].map(lambda x: check_mapping_or_add(col, x))

train_target = trainDF['target'].values

trainDF.drop('target', axis=1, inplace=True)

# Fill With Zeros and repeat
zerosDF = trainDF.fillna(value=-1)

total_rows = len(trainDF)
column_percentages = {}
for col in trainDF:
    nan_row_count = trainDF[col].isnull().sum()
    print col, nan_row_count, nan_row_count / float(total_rows)
    column_percentages[col] = nan_row_count / float(total_rows)

drop_columns = []
for key, value in column_percentages.iteritems():
    if value > 0.25:
        drop_columns.append(key)


# All Data
# Most Frequent
imp = Imputer(missing_values='NaN', strategy='most_frequent', axis=0)
train_values = imp.fit_transform(trainDF.values)
train_gaussian(train_values, train_target, "Gaussian, Most Frequent")
train_bernoulli(train_values, train_target, "Bernoulli, Most Frequent")

# Mean
imp = Imputer(missing_values='NaN', strategy='mean', axis=0)
train_values = imp.fit_transform(trainDF.values)
train_gaussian(train_values, train_target, "Gaussian, Mean")
train_bernoulli(train_values, train_target, "Bernoulli, Mean")

# Median
imp = Imputer(missing_values='NaN', strategy='median', axis=0)
train_values = imp.fit_transform(trainDF.values)
train_gaussian(train_values, train_target, "Gaussian, Median")
train_bernoulli(train_values, train_target, "Bernoulli, Median")


# Drop bad columns
train_values_dropped_columns = trainDF.drop(drop_columns, axis=1, inplace=False).values

# Most Frequent
imp = Imputer(missing_values='NaN', strategy='most_frequent', axis=0)
train_values = imp.fit_transform(train_values_dropped_columns)
train_gaussian(train_values, train_target, "Gaussian, Most Frequent, Dropped Columns")
train_bernoulli(train_values, train_target, "Bernoulli, Most Frequent, Dropped Columns")

# Mean
imp = Imputer(missing_values='NaN', strategy='mean', axis=0)
train_values = imp.fit_transform(train_values_dropped_columns)
train_gaussian(train_values, train_target, "Gaussian, Mean, Dropped Columns")
train_bernoulli(train_values, train_target, "Bernoulli, Mean, Dropped Columns")

# Median
imp = Imputer(missing_values='NaN', strategy='median', axis=0)
train_values = imp.fit_transform(train_values_dropped_columns)
train_gaussian(train_values, train_target, "Gaussian, Median, Dropped Columns")
train_bernoulli(train_values, train_target, "Bernoulli, Median, Dropped Columns")

# Zeros
# All Columns
train_values = zerosDF.values
train_gaussian(train_values, train_target, "Gaussian, Zeros")
train_bernoulli(train_values, train_target, "Bernoulli, Zeros")

# Drop bad columns
train_zeros_values_dropped_columns = zerosDF.drop(drop_columns, axis=1, inplace=False).values
train_gaussian(train_values, train_target, "Gaussian, Zeros, Dropped Columns")
train_bernoulli(train_values, train_target, "Bernoulli, Zeros, Dropped Columns")

# Binarization