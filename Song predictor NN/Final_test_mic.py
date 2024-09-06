import numpy as np
import tensorflow as tf
import pathlib

# Set the seed value for experiment reproducibility.
seed = 42
tf.random.set_seed(seed)
np.random.seed(seed)

# Define the path to your dataset
DATASET_PATH = 'micmp3'
data_dir = pathlib.Path(DATASET_PATH)

# Function to squeeze the dimensions of the dataset
def squeeze(audio, labels):
    audio = tf.squeeze(audio, axis=-1)
    return audio, labels

# Function to convert raw data to a spectrogram
def get_spectrogram(waveform):
    spectrogram = tf.signal.stft(waveform, frame_length=255, frame_step=128)
    spectrogram = tf.abs(spectrogram)
    spectrogram = spectrogram[..., tf.newaxis]
    return spectrogram

# Function to create a dataset from spectrograms
def make_spec_ds(ds):
    return ds.map(
        map_func=lambda audio, label: (get_spectrogram(audio), label),
        num_parallel_calls=tf.data.AUTOTUNE
    )

# Load test data and preprocess it
test_ds = tf.keras.utils.audio_dataset_from_directory(
    directory=data_dir,
    batch_size=64,
    validation_split=0.2,
    seed=0,
    output_sequence_length=441000,  # Change in test
    subset='training')  # Use 'training' subset here, assuming you want to use all available data for testing

# Apply preprocessing
test_ds = test_ds.map(squeeze, num_parallel_calls=tf.data.AUTOTUNE)
test_ds = make_spec_ds(test_ds)

# Load your trained model
model = tf.keras.models.load_model('mic.h5')

# Evaluate the model on the preprocessed test data
results = model.evaluate(test_ds, verbose=1)
print(f"Test results - Loss: {results[0]}, Accuracy: {results[1]}")