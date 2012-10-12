int photoPin = 0; // photoresistor pin
int tempPin = 1;  // temperature pin

int photoVal = 0; // photoresistor voltage
float tempVal;    // temperature value

int delayTime = 90; // dalay in ms

void setup()
{
  pinMode(photoPin, INPUT); // set pin as input
  Serial.begin(57600); // set COM port
}

void loop()
{
  photoVal = analogRead(photoPin); //read photoresistor voltage
  Serial.print(";PhotoSensor;" );                       
  Serial.print(photoVal);
  Serial.println(";");
  
  tempVal= analogRead(tempPin)*5/1024.0;
  tempVal= (tempVal - 0.5) / 0.01;
  Serial.print(";Temperature;" );                       
  Serial.print(tempVal); 
  Serial.println(";");
  
  delay(delayTime);
}
