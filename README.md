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

#### Version 0.2.5
adjusted buttons and added Audio listeners, pretty much works like the pressure one, but without the cute colored circle.
uses different update system, updating every tic.

#### Version 0.3
added telephony signal info in the SensorActivity, although it gets displayed in a shitty display and deleted if the circle needs to update,
it doesn't matter, it works. i'm done finally lol. we just need to process the data and create dependencies for the whole app and map.
that'll be fun.

#### Version 0.4
added class NetworkSignalStrength and superclass Sensor, refactored Main and kept the old one as Legacy, tested the new classes.
Click the new button on the Main screen to show network information :)

#### Version 0.5 Beta
added class WifiSignalStrength, works the same way as NetworkSignalStrength, but with wifi info (yay!)
the class is as now untested, i'll need to test it. i also added corrisponding working test buttons and text, i just need to test it.