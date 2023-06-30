# transceiver-go
a luizo piace questo nome
#### Version 0.1
the app is now able to show a funny pun, a list of all sensor available to the current device, and a button that switches Activity and layout.
upon clicking the button, the SensorActivity is launched, which retrieves information trough the pressure sensor on the device, and prints the information retrieved accordingly.
:)
#### Version 0.1.5
now the app displays a small colored circle that updates every onSensorChanged call in the SensorActivity activity.
the colour of the circle changes based on the pressure detected by the sensor, and works as follows
- 720+ millibars -> Green
- 360+ millibars -> Yellow
- otherwise -> Red

#### Version 0.2
the app has a new textview on it's MainActivity Layout. this view will reflect wifi signal information which updates every .5 seconds (on paper lol).
it will also detect if the Wifi is active or not. :>