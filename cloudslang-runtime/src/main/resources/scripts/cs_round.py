def cs_round(val):
  x = float(val) if  type(val).__name__ == 'str' or  type(val).__name__ == 'unicode' else val

  return str(round(x))
