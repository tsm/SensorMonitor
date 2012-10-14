#!/usr/local/bin/python
# -*- coding: utf-8 -*-
import socket
import sys
import random
from time import sleep

def com2socket(com_name, baudrate, host, port,name):

    sock = socket.socket()
    sock.connect((host,int(port)))
    while 1:
        #line = com.readline()
        line = b'SEND '+bytes(name, encoding='ascii')+b';PhotoSensor;'+bytes(str(random.randint(100,700)), encoding='ascii')+b';\r\n'
        print(line)
        sock.send(line)
        sleep(0.01)
        line = b'SEND '+bytes(name, encoding='ascii')+b';Temperature;'+bytes(str(random.randint(180,360)/10.0), encoding='ascii')+b';\r\n'
        print(line)
        sock.send(line)
        sleep(0.8)

        
    sock.close
    com.close()

if __name__ == '__main__':
    if len(sys.argv) == 6:
        com2socket(sys.argv[1],sys.argv[2],sys.argv[3],sys.argv[4],sys.argv[5])
    else:
        print('Run with parameters: COM_NAME COM_BOUD_RATE MONITOR_ADDRESS MONITOR_PORT NAME')
        print('Example: python sensor.py COM3 57600 127.0.0.1 26123 Arduino0')
