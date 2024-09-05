import os
import pathlib
import matplotlib.pyplot as plt
import numpy as np
import tensorflow as tf
from tensorflow.keras import layers
from tensorflow.keras import models
from IPython import display
import sounddevice as sd
from scipy.io.wavfile import write
import time
import tempfile
import tensorflow_model_optimization as tfmot
from silence_tensorflow import silence_tensorflow
silence_tensorflow()

###################################################
########## Functions needed
###################################################

# Convert model to TFLITE
def convert_model_tflite(model, name):
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]
    converted_model = converter.convert()


    tflite_file = name + ".tflite"


    with open(tflite_file, 'wb') as f:
        f.write(converted_model)

    return tflite_file


# Squeeze removes the dimensions of 1 from dataset.  Just a component of preprocessing
def squeeze(audio, labels):
  audio = tf.squeeze(audio, axis=-1)
  return audio, labels


# Convert raw data to a SPECTROGRAM, a 2D image that represents the frequency info
def get_spectrogram(waveform):
  # Convert the waveform to a spectrogram via a STFT.
  spectrogram = tf.signal.stft(waveform, frame_length=255, frame_step=128)
  # Obtain the magnitude of the STFT.
  spectrogram = tf.abs(spectrogram)
  # Add a `channels` dimension, so that the spectrogram can be used
  # as image-like input data with convolution layers (which expect
  # shape (`batch_size`, `height`, `width`, `channels`).
  spectrogram = spectrogram[..., tf.newaxis]
  return spectrogram


# Create a dataset from the spectrograms
def make_spec_ds(ds):
  return ds.map(
      map_func=lambda audio,label: (get_spectrogram(audio), label),
      num_parallel_calls=tf.data.AUTOTUNE)


###################################################
########## End of Functions needed
###################################################


# Set the seed value for experiment reproducibility.
seed = 42
tf.random.set_seed(seed)
np.random.seed(seed)
DATASET_PATH = 'Mic_Splits/train'
data_dir = pathlib.Path(DATASET_PATH)

commands = np.array(tf.io.gfile.listdir(str(data_dir)))
commands = commands[(commands != 'README.md') & (commands != '.DS_Store')]
print('Commands:', commands)


# Load up full audio dataset
train_ds, val_ds = tf.keras.utils.audio_dataset_from_directory(
    directory=data_dir,
    batch_size=16,
    validation_split=0.2,
    seed=0,
    #shuffle=False,
    output_sequence_length=441000,
    #output during split the sameple rate
    subset='both')

#create labels names and print
label_names = np.array(train_ds.class_names)
print()
print("label names:", label_names)

# Training and val dataset need to be preprocessed with squeeze
train_ds = train_ds.map(squeeze, tf.data.AUTOTUNE)
val_ds = val_ds.map(squeeze, tf.data.AUTOTUNE)

# Call function to convert audio data to spectrogram
train_spectrogram_ds = make_spec_ds(train_ds)
val_spectrogram_ds = make_spec_ds(val_ds)

for example_spectrograms, example_spect_labels in train_spectrogram_ds.take(1):
  break


input_shape = example_spectrograms.shape[1:]
print('Input shape:', input_shape)
num_labels = len(label_names)

# Instantiate the `tf.keras.layers.Normalization` layer.
norm_layer = layers.Normalization()
# Fit the state of the layer to the spectrograms
# with `Normalization.adapt`.
norm_layer.adapt(data=train_spectrogram_ds.map(map_func=lambda spec, label: spec))



model = models.Sequential([
    layers.Input(shape=input_shape),
    layers.Resizing(32, 32),
    norm_layer,
    layers.Conv2D(32, 3, activation='relu', padding='same'),
    layers.MaxPooling2D(),
    layers.Conv2D(64, 3, activation='relu', padding='same'),  
    layers.MaxPooling2D(),
    layers.Dropout(0.5),
    layers.Flatten(),
    layers.Dense(128, activation='relu'),
    layers.Dropout(0.5),
    layers.Dense(num_labels),
])

model.summary()


model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.0001),
    loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
    metrics=['accuracy'],
)

EPOCHS = 200
history = model.fit(
    train_spectrogram_ds,
    validation_data=val_spectrogram_ds,
    epochs=EPOCHS,
    callbacks=tf.keras.callbacks.EarlyStopping(verbose=1, patience=6),
)


model.evaluate(val_spectrogram_ds, return_dict=True)


model.save('mic.h5', include_optimizer = True)
print("h5 saved")
#b_tflite = convert_model_tflite(model,"mic")
print("tflite saved")


