import random

f= open("input.csv","w+")

print('Team 1 personality 1 percentage')
input_T1_P1 = input()
print('Team 1 personality 2 percentage')
input_T1_P2 = input()
print('Team 1 personality 3 percentage')
input_T1_P3 = input()

for i in range(500):
    number_of_players = random.randrange(20, 101, 5)
    print(number_of_players)
    print(str(input_T1_P1) + ',' + str(input_T1_P2) + ',' + str(input_T1_P3))
    f.write(str(number_of_players) + ',' + str(input_T1_P1) + ',' + str(input_T1_P2) + ',' + str(input_T1_P3))
    for x in range(0,4):
        P1 = 0; P2 = 0; P3 = 0
        P1 = random.randrange(0, 101)
        if P1 < 100:
            P2 = random.randrange(0, 101 - P1)
        if P1 + P2 < 100:
            P3 = 100 - P1 - P2
        f.write(',' + str(P1) + ',' + str(P2) + ',' + str(P3))
        print(str(P1) + ',' + str(P2) + ',' + str(P3))
    f.write('\n')
    
f.close()