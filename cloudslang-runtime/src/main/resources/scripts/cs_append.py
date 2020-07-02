def cs_append(value, to_append):

  if type(value).__name__ != 'str' and type(value).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'value', got " + str(value))

  if type(to_append).__name__ != 'str' and type(to_append).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'to_append', got " + str(to_append))

  return value + to_append
