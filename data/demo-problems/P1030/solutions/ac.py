def preorder(inorder, postorder):
    if not inorder:
        return ""
    root = postorder[-1]
    pos = inorder.index(root)
    left_in = inorder[:pos]
    right_in = inorder[pos+1:]
    left_post = postorder[:len(left_in)]
    right_post = postorder[len(left_in):-1]
    return root + preorder(left_in, left_post) + preorder(right_in, right_post)

inorder = input().strip()
postorder = input().strip()
print(preorder(inorder, postorder))