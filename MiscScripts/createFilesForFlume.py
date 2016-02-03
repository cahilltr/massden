import time, sys, string, random

# http://stackoverflow.com/questions/5998245/get-current-time-in-milliseconds-in-python
current_milli_time = lambda: int(round(time.time() * 1000))

file_paths = "./"

def f():
    file_name = file_paths + str(current_milli_time()) + "_flume.txt"
    try:
        file = open(file_name,'w+')   # Trying to create a new file or open one
        # From my massden/MiscScripts/createCSVWithCommasInAField.py file
        for i in range(1000):
            s = ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for _ in range(20)) + "," + ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits + string.punctuation) for _ in range(100)) + "," + ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for _ in range(10)) + "\n"
            file.write(s)
        file.close()
    except:
        print('Something went wrong! Can\'t tell what?')
        sys.exit(0)

while True:
    f()
    time.sleep(120)
