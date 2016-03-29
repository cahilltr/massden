from pandas import DataFrame
import pandas as pd
from sklearn.preprocessing import Imputer
from sklearn.naive_bayes import BernoulliNB, GaussianNB


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
        train_size, (train_target != y_pred_train).sum()))


def train_bernoulli(data, target, run_type, alpha=1):
    clf = BernoulliNB(alpha=alpha)
    clf.fit(data, target)
    y_pred_train = clf.predict(data)
    print(run_type + ": Number of mislabeled points out of a total %d points : %d" % (
        train_size, (train_target != y_pred_train).sum()))


trainDF = DataFrame.from_csv('/Users/cahillt/Downloads/bnp/train.csv')
testDF = DataFrame.from_csv('/Users/cahillt/Downloads/bnp/test.csv')

train_size = len(trainDF)

for col in trainDF:
    print col, trainDF[col].dtypes

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
train_values_mf = imp.fit_transform(trainDF.values)
train_gaussian(train_values_mf, train_target, "Gaussian, Most Frequent")
for a in [0.5, 1, 2]:
    train_bernoulli(train_values_mf, train_target, "Bernoulli, Most Frequent, alpha=" + str(a), a)

# Mean
imp = Imputer(missing_values='NaN', strategy='mean', axis=0)
train_values_mean = imp.fit_transform(trainDF.values)
train_gaussian(train_values_mean, train_target, "Gaussian, Mean")
for a in [0.5, 1, 2]:
    train_bernoulli(train_values_mean, train_target, "Bernoulli, Mean, alpha=" + str(a), a)

# Median
imp = Imputer(missing_values='NaN', strategy='median', axis=0)
train_values_median = imp.fit_transform(trainDF.values)
train_gaussian(train_values_median, train_target, "Gaussian, Median")
for a in [0.5, 1, 2]:
    train_bernoulli(train_values_median, train_target, "Bernoulli, Median, alpha=" + str(a), a)

# Drop bad columns
train_values_dropped_columns = trainDF.drop(drop_columns, axis=1, inplace=False).values

# Most Frequent
imp = Imputer(missing_values='NaN', strategy='most_frequent', axis=0)
train_values_dropped_mf = imp.fit_transform(train_values_dropped_columns)
train_gaussian(train_values_dropped_mf, train_target, "Gaussian, Most Frequent, Dropped Columns")
for a in [0.5, 1, 2]:
    train_bernoulli(train_values_dropped_mf, train_target, "Bernoulli, Most Frequent, Dropped Columns, alpha=" + str(a), a)

# Mean
imp = Imputer(missing_values='NaN', strategy='mean', axis=0)
train_values_dropped_mean = imp.fit_transform(train_values_dropped_columns)
train_gaussian(train_values_dropped_mean, train_target, "Gaussian, Mean, Dropped Columns")
for a in [0.5, 1, 2]:
    train_bernoulli(train_values_dropped_mean, train_target, "Bernoulli, Mean, Dropped Columns, alpha=" + str(a), a)

# Median
imp = Imputer(missing_values='NaN', strategy='median', axis=0)
train_values_dropped_median = imp.fit_transform(train_values_dropped_columns)
train_gaussian(train_values_dropped_median, train_target, "Gaussian, Median, Dropped Columns")
for a in [0.5, 1, 2]:
    train_bernoulli(train_values_dropped_median, train_target, "Bernoulli, Median, Dropped Columns, alpha=" + str(a), a)

# Zeros
# All Columns
train_values_zeros = zerosDF.values
train_gaussian(train_values_zeros, train_target, "Gaussian, Zeros")
for a in [0.5, 1, 2]:
    train_bernoulli(train_values_zeros, train_target, "Bernoulli, Zeros, alpha=" + str(a), a)

# Drop bad columns
train_zeros_values_dropped_columns = zerosDF.drop(drop_columns, axis=1, inplace=False).values
train_gaussian(train_zeros_values_dropped_columns, train_target, "Gaussian, Zeros, Dropped Columns")
for a in [0.5, 1, 2]:
    train_bernoulli(train_zeros_values_dropped_columns, train_target, "Bernoulli, Zeros, Dropped Columns, alpha=" + str(a), a)
