import serial
import socket

def com2socket(com_name, baudrate, host, port,name):
    com = serial.Serial(com_name,baudrate)

    sock = socket.socket()
    sock.connect((host,port))
    while 1:
        line = com.readline()
        line = b'SEND '+name+line
        print(line)
        sock.send(line)

    sock.close
    com.close()

if __name__ == '__main__':
    com2socket("COM3",57600, socket.gethostname(),26123,b'Arduino1')
