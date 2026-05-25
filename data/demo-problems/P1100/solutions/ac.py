n = int(input())
high = n >> 16
low = n & 0xFFFF
result = (low << 16) | high
print(result)