# <code reference> https://github.com/rainierez/MatrixKeypad_Python

import RPi.GPIO as GPIO


class keypad:
    # CONSTANTS
    KEYPAD = [[1, 2, 3], [4, 5, 6], [7, 8, 9], ["*", 0, "#"]]

    ROW = [18, 23, 24, 25]
    COLUMN = [4, 27, 22]
    # ROW = [18, 23, 25, 24]
    # COLUMN = [4, 27, 22]

    def __init__(self):
        GPIO.setmode(GPIO.BCM)
        GPIO.setwarnings(False)

    def getKey(self):
        # Set all columns as output low
        for j in range(len(self.COLUMN)):
            GPIO.setup(self.COLUMN[j], GPIO.OUT)
            GPIO.output(self.COLUMN[j], GPIO.LOW)
            # print(self.COLUMN[j])

        # Set all rows as input
        for i in range(len(self.ROW)):
            GPIO.setup(self.ROW[i], GPIO.IN, pull_up_down=GPIO.PUD_UP)
            # print(self.ROW[i])

        # Scan rows for pushed key/button
        # A valid key press should set "rowVal"  between 0 and 3.
        rowVal = -1
        for i in range(len(self.ROW)):
            tmpRead = GPIO.input(self.ROW[i])
            if tmpRead == 0:
                rowVal = i

        # if rowVal is not 0 thru 3 then no button was pressed and we can exit
        if rowVal < 0 or rowVal > 3:
            self.exit()
            return

        # Convert columns to input
        for j in range(len(self.COLUMN)):
            # if GPIO.setup(self.COLUMN[j], GPIO.IN, pull_up_down=GPIO.PUD_DOWN):
            #     print(self.COLUMN[i])
            GPIO.setup(self.COLUMN[j], GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

        # Switch the i-th row found from scan to output
        GPIO.setup(self.ROW[rowVal], GPIO.OUT)
        GPIO.output(self.ROW[rowVal], GPIO.HIGH)

        # Scan columns for still-pushed key/button
        # A valid key press should set "colVal"  between 0 and 2.
        colVal = -1
        for j in range(len(self.COLUMN)):
            tmpRead = GPIO.input(self.COLUMN[j])
            if tmpRead == 1:
                colVal = j

        # if colVal is not 0 thru 2 then no button was pressed and we can exit
        if colVal < 0 or colVal > 2:
            self.exit()
            return

        # Return the value of the key pressed
        self.exit()
        # print(self.KEYPAD[rowVal])
        # print(self.KEYPAD[colVal])

        return self.KEYPAD[rowVal][colVal]

    def exit(self):
        # Reinitialize all rows and columns as input at exit
        for i in range(len(self.ROW)):
            GPIO.setup(self.ROW[i], GPIO.IN, pull_up_down=GPIO.PUD_UP)
        for j in range(len(self.COLUMN)):
            GPIO.setup(self.COLUMN[j], GPIO.IN, pull_up_down=GPIO.PUD_UP)

    #########
    def checkConnections(self):
        print("Checking connections...")
        for row_pin in self.ROW:
            GPIO.setup(row_pin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
            print(f"Row {row_pin}: {GPIO.input(row_pin)}")

        for col_pin in self.COLUMN:
            GPIO.setup(col_pin, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
            print(f"Column {col_pin}: {GPIO.input(col_pin)}")


if __name__ == "__main__":
    # Initialize the keypad class
    kp = keypad()

    # Loop while waiting for a keypress

    digit = None
    while digit == None:
        digit = kp.getKey()

    print(digit)
    # kp.checkConnections()
