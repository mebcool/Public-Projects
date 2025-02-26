from flask import Flask, jsonify, render_template, Response
import serial

app = Flask(__name__)


@app.route('/')
def index():
    return render_template("index.html")


@app.route('/get_data')
def get_data():
    ser = serial.Serial('COM4', 9600)
    data_string = ser.readline().decode().strip()
    data_list = data_string.split(',')

    sos = data_list[0]
    bpm = data_list[1]
    xMax = float(data_list[2])
    yMax = float(data_list[3])
    zMax = float(data_list[4])

    data = {
        'sos': sos,
        'bpm': bpm,
        'xMax': xMax,
        'yMax': yMax,
        'zMax': zMax
    }
    ser.close()
    return jsonify(data)


if __name__ == '__main__':
    app.run()
