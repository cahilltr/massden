
from pandas import DataFrame
import pandas as pd
from sklearn.preprocessing import Imputer
from sklearn.tree import DecisionTreeClassifier

def check_mapping_or_add(my_col, x):
    if x in cat_var_mapping[my_col]:
        return cat_var_mapping[my_col][x]
    else:
        count = len(cat_var_mapping[col])
        cat_var_mapping[my_col][x] = count
        return count

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

train_target = trainDF['target'].values

trainDF.drop('target', axis=1, inplace=True)

# Fill With Zeros and repeat
#zerosDF = trainDF.fillna(value=0)

imp = Imputer(missing_values='NaN', strategy='mean', axis=0)
train_values = imp.fit_transform(trainDF.values)

clf = DecisionTreeClassifier(random_state=0).fit(train_values, train_target)
predicted_labels = clf.predict(train_values)

wrong_count = 0
for predicted_label, actual in zip(predicted_labels, train_target):
    if (predicted_label != actual):
        wrong_count += 1
    print('Model: {}; truth: {}'.format(predicted_label, actual))

print wrong_count, len(train_target)


imp = Imputer(missing_values='NaN', strategy='mean', axis=0)
test_values = imp.fit_transform(testDF.values)

test_predicted = clf.predict(test_values)
test_predicted_prob = clf.predict_proba(test_values)
print clf.n_classes_, clf.classes_
print len(test_predicted)
print test_predicted_prob[0]

