import sys

def main():
    encrypted = sys.stdin.readline().strip()
    original = sys.stdin.readline().strip()
    to_decrypt = sys.stdin.readline().strip()

    encrypt_map = {}  # 原文字母 -> 密字
    decrypt_map = {}  # 密字 -> 原文字母
    used_cipher = set()

    failed = False

    for e, o in zip(encrypted, original):
        if o in encrypt_map:
            if encrypt_map[o] != e:
                failed = True
                break
        else:
            if e in used_cipher:
                failed = True
                break
            encrypt_map[o] = e
            decrypt_map[e] = o
            used_cipher.add(e)

    if not failed:
        if len(encrypt_map) != 26:
            failed = True

    if failed:
        print("Failed")
    else:
        result = ''.join(decrypt_map[c] for c in to_decrypt)
        print(result)

if __name__ == "__main__":
    main()