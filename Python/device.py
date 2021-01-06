import wiotp.sdk.device
import RPi.GPIO as GPIO
import time
from time import sleep

myConfig = {
    "identity":{
        "orgId": "ymmd2u",
        "typeId": "Raspberry_Pi",
        "deviceId": "kyong_pi"
    },
    "auth": {
        "token": "qwer1234"
    }
}

# def myCommandCallback(msg):

trig = 4
echo = 17
danger_led = 24
switch = 23
flag = 0
safe_time = time.time()
danger_time =time.time()
GPIO.setmode(GPIO.BCM)
GPIO.setup(trig,GPIO.OUT)
GPIO.setup(echo,GPIO.IN)
GPIO.setup(danger_led,GPIO.OUT)
GPIO.setup(switch,GPIO.IN,GPIO.PUD_UP)
client = wiotp.sdk.device.DeviceClient(config=myConfig,logHandlers=None)
client.connect()

# myData={'name' : 'foo', 'cpu' : 60, 'mem' : 50}
# while True:
#     client.publishEvent(eventId="status",msgFormat="json",data=myData, qos=0, onPublish=None)

while True:
    GPIO.output(trig,False)
    sleep(0.5)
    GPIO.output(trig,True)
    sleep(0.00001)
    GPIO.output(trig,False)

    while GPIO.input(echo) == 0:
        start = time.time()
    while GPIO.input(echo) == 1:
        stop = time.time()

    time_interval = stop - start
    distance = time_interval * 17000
    distance = round(distance,2)

    print(distance,"cm")
    myData = {"Name":"ChoonSam","Age":68,"IO":"Indoor","distancce":distance}
    if distance < 20 :
        flag = 0
        safe_time = time.time()
        # myData = {"Name":"ChoonSam","Age":68,"I/O":"Indoor","distancce":distance,"safety":"safe"}
        myData["Safety"] = "Safe"
        myData["Time"]=0
        client.publishEvent(eventId="us_dist",msgFormat="json",data=myData,qos=0,onPublish=None)
        GPIO.output(danger_led,GPIO.LOW)
    elif time.time()-safe_time > 10:
        # myData = {"Name":"ChoonSam","Age":68,"I/O":"Indoor","distance":distance,"safety":"danger"}
        myData["Safety"] = "Dangerous"
        if flag == 0:
            danger_time = time.time()
            flag = 1
        myData["Time"] = time.time() - danger_time
        client.publishEvent(eventId="us_dist",msgFormat="json",data=myData,qos=0,onPublish=None)
        GPIO.output(danger_led,GPIO.HIGH)
    else:
        myData["Time"] = time.time()-safe_time
    if GPIO.input(switch) == 0:
        # myData = {"Name":"ChoonSam","Age":68,"I/O":"Indoor","distance":distance,"safety":"safe"}

        safe_time = time.time()
        client.publishEvent(eventId="us_dist",msgFormat="json",data=myData,qos=0,onPublish=None)
        myData["Safety"] = "Safe"
        myData["Time"] = 0
        GPIO.output(danger_led,GPIO.LOW)
    sleep(0.5)
client.disconnect()
