def cs_prepend(value, to_prepend):

  if type(value).__name__ != 'str' and type(value).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'value', got " + str(value))

  if type(to_prepend).__name__ != 'str' and type(to_prepend).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'to_prepend', got " + str(to_prepend))

  return to_prepend + value
