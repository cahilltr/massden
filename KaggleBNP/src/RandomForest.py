from pandas import DataFrame
import pandas as pd
from sklearn.preprocessing import Imputer
from sklearn.ensemble import RandomForestClassifier


def check_mapping_or_add(my_col, x):
    if x in cat_var_mapping[my_col]:
        return cat_var_mapping[my_col][x]
    else:
        count = len(cat_var_mapping[col])
        cat_var_mapping[my_col][x] = count
        return count


def run_random_forest(train_values, train_target, run_type):
    forest = RandomForestClassifier()
    forest = forest.fit(train_values, train_target)

    predicted_labels = forest.predict(train_values)

    wrong_count = 0
    for predicted_label, actual in zip(predicted_labels, train_target):
        if predicted_label != actual:
            wrong_count += 1
    print(run_type + " results in incorrect: " + str(wrong_count) + " out of " + str(len(train_target)))


trainDF = DataFrame.from_csv('/Users/cahillt/Downloads/bnp/train.csv')
testDF = DataFrame.from_csv('/Users/cahillt/Downloads/bnp/test.csv')

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


train_target = trainDF['target'].values

trainDF.drop('target', axis=1, inplace=True)

# All data
# Most Frequent
imp = Imputer(missing_values='NaN', strategy='most_frequent', axis=0)
train_values_mf = imp.fit_transform(trainDF.values)
run_random_forest(train_values_mf, train_target, "All data, most_frequent")
# Mean
imp = Imputer(missing_values='NaN', strategy='mean', axis=0)
train_values_mean = imp.fit_transform(trainDF.values)
run_random_forest(train_values_mean, train_target, "All data, mean")
# Median
imp = Imputer(missing_values='NaN', strategy='median', axis=0)
train_values_median = imp.fit_transform(trainDF.values)
run_random_forest(train_values_median, train_target, "All data, median")

# Zeros
zerosDF = trainDF.fillna(value=-1)
run_random_forest(zerosDF.values, train_target, "All data, Zeros")

# Dropped Columns
train_values_dropped_columns = trainDF.drop(drop_columns, axis=1, inplace=False).values
# Most Frequent
imp = Imputer(missing_values='NaN', strategy='most_frequent', axis=0)
train_values_mf_dc = imp.fit_transform(train_values_dropped_columns)
run_random_forest(train_values_mf_dc, train_target, "Dropped Columns, most_frequent")
# Mean
imp = Imputer(missing_values='NaN', strategy='mean', axis=0)
train_values_mean_dc = imp.fit_transform(train_values_dropped_columns)
run_random_forest(train_values_mean_dc, train_target, "Dropped Columns, mean")
# Median
imp = Imputer(missing_values='NaN', strategy='median', axis=0)
train_values_median_dc = imp.fit_transform(train_values_dropped_columns)
run_random_forest(train_values_median_dc, train_target, "Dropped Columns, median")

# Zeros with Dropped Columns
train_zeros_values_dropped_columns = zerosDF.drop(drop_columns, axis=1, inplace=False).values
run_random_forest(train_zeros_values_dropped_columns, train_target, "Dropped Columns, Zeros")