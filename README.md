# Pixy2-As-I2C-Sensor-for-FTC
The source code for using Pixy2 smart camera as an I2C sensor over the REV control hub


Hello! This is the source code our team has developed for using Pixy2 as an I2C Sensor over the REV control hub. Please reach out! you can contact our team at FTC-5962@saintpeterrobotics.org

# Electrical
To wire the camera into the I2C Bus, you will need to build a custom 4 pin cable to access Pixys SDA, SCL, GRND, and 5V power pins *These run the I2C bus and are the corresponding red, black, blue,and white cables found on most sensors*. For pinout reference use https://docs.pixycam.com/wiki/doku.php?id=wiki:v2:porting_guide#picking-the-right-interface. Our team opted to use single strand female ended cables to attatch to these four pins of interest. Note that the 10 pin ribbon cable that comes with the sensor is not of use for us. Once you isolate the correct pins from the camera, you can connect to the REV hub I2C bus by splicing them into a standard encoder cable, and following this wiring guide https://revrobotics.ca/content/docs/REV-31-1595-UM.pdf. The SDA will go to white, and the SCL will go to blue. 

# programming
For a basic understanding of how I2C devices work see https://github.com/FIRST-Tech-Challenge/FtcRobotController/wiki/Writing-an-I2C-Driver. We did things a little different, but at a high level, essentialy we write a request for information to pixy, then we read its response and parse it into interpretable data. This sheet documents the register and return values to be expected from Pixy https://docs.pixycam.com/wiki/doku.php?id=wiki:v2:porting_guide#picking-the-right-interface.



# Other
the latest firmware and version of pixyMon can be found at https://pixycam.com/downloads-pixy2/
make sure that you configure your camera to use the I2C interface on pixymon, or else none of this will work.
