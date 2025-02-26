import os
from datetime import datetime
import matplotlib.pyplot as plt
import numpy as np
import os
os.environ['KMP_DUPLICATE_LIB_OK'] = 'TRUE'
import tensorflow as tf
from tensorflow.keras import layers
import pandas as pd
from sklearn.metrics import mean_absolute_error

#Kai Vong + Matt Eddy

goog_dataset = pd.read_csv("GOOG.csv", header=None)

values = goog_dataset[1].values.astype(float)

num_points = 500
x = np.zeros((num_points, 1))
y = np.zeros(num_points)

for i in range(num_points):
    x[i, 0] = i + 1
    y[i] = values[i]

#build model via the add function
model = tf.keras.Sequential()

#rnn is 100 neurons and input is 0 batch size, 1 input at a time
model.add(layers.SimpleRNN(100, input_shape=(None, 1), return_sequences=True))
model.add(layers.SimpleRNN(100))
model.add(layers.Dense(1))

model.summary()

model.compile(optimizer = 'adam', loss='mse')

#just train off the first 300, 250 epochs was max for change in loss
model.fit(x[:300], y[:300], epochs = 250, validation_split= 0.2)

seq_len = 300
num_predict = 200 #total of 200 samples

#generate test input and output data using the last 200 datapoints
test_input = np.zeros((1, seq_len + num_predict, 1))
test_output = y[-num_predict:].reshape(1, -1)

#assign values from the dataset to the test input
test_input[0, :seq_len, 0] = y[:seq_len]

#reshape test input to match the model input shape
test_input = test_input.reshape(1, -1, 1)

#same as live coding
print(test_input.shape)

#create a smaller dataset
# this may be our problem
cur_input = test_input[:, 0:seq_len, :]

#create 2 new lists/arrays to track results
indices = []
predictions = []

#do a loop, feed data to nn, get results sliding window loop
for i in range(num_predict):
    #append my index of the output
    indices.append(i+seq_len)
    
    #execute model prediction
    y = model.predict(cur_input)[0][0]
    
    #update current input
    cur_input = test_input[:,i:(seq_len+i),:]
    
    #append output prediction
    #predictions are showing up the same or all familiar?? cant find issue with cur_input or test_input
    predictions.append(y)
    
#dont ask about pyplot
plt.plot(range(1, num_points + 1), values, '-')
plt.plot(indices, predictions, '--')
mae = mean_absolute_error(test_output.ravel(), predictions)
print("MAE: ", mae)
plt.savefig('stonks.jpg')
plt.show()
