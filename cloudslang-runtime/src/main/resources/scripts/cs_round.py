def cs_round(val, digits = 0):
  x = float(val) if  type(val).__name__ == 'str' or  type(val).__name__ == 'unicode' else val
  y = int(digits) if  type(val).__name__ == 'str' or  type(val).__name__ == 'unicode' else digits

  if y == 0:
    return str(round(x))
  else:
    return str(round(x, y))
