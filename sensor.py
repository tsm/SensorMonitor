#!/usr/local/bin/python
# -*- coding: utf-8 -*-
import serial
import socket
import sys

def com2socket(com_name, baudrate, host, port,name):
    com = serial.Serial(com_name,baudrate)

    sock = socket.socket()
    sock.connect((host,int(port)))
    while 1:
        line = com.readline()
        line = b'SEND '+bytes(name, encoding='ascii')+line
        print(line)
        sock.send(line)

    sock.close
    com.close()

if __name__ == '__main__':
    if len(sys.argv) == 6:
        com2socket(sys.argv[1],sys.argv[2],sys.argv[3],sys.argv[4],sys.argv[5])
    else:
        print('Run with parameters: COM_NAME COM_BOUD_RATE MONITOR_ADDRESS MONITOR_PORT NAME')
        print('Example: python sensor.py COM3 57600 127.0.0.1 26123 Arduino0')
