import string
import random


output = raw_input("Where should the file be written: ")

f = open(output, 'a')
for i in range(1000):
    s = ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for _ in range(20)) + "," + ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits + string.punctuation) for _ in range(100)) + "," + ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for _ in range(10)) + "\n"
    f.write(s)

f.close()
