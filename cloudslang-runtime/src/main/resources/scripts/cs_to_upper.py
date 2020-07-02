def cs_to_upper(string):
  if type(string).__name__ != 'str' and type(string).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'string', got " + str(string))

  return string.upper()
