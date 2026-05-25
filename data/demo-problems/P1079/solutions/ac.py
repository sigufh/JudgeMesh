key = input().strip()
cipher = input().strip()
plain = []
key_len = len(key)
for i, c in enumerate(cipher):
    k = key[i % key_len]
    is_upper = c.isupper()
    c_lower = c.lower()
    k_lower = k.lower()
    shift = ord(k_lower) - ord('a')
    p_lower = chr((ord(c_lower) - ord('a') - shift) % 26 + ord('a'))
    p = p_lower.upper() if is_upper else p_lower
    plain.append(p)
print(''.join(plain))