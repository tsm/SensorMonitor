import serial

def readCOM(port, baudrate):
    ser = serial.Serial(port,baudrate)
    while 1:
        line = ser.readline()
        print(line)

if __name__ == '__main__':
    readCOM("COM12",57600)
