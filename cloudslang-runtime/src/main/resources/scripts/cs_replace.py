def cs_replace(string, old_val, new_val, count = -1):

  if type(string).__name__ != 'str' and type(string).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'string', got " + str(string))

  if type(old_val).__name__ != 'str' and type(old_val).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'old_val', got " + str(old_val))

  if type(new_val).__name__ != 'str' and type(new_val).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'new_val', got " + str(new_val))

  return string.replace(old_val, new_val) if count < 0 else string.replace(old_val, new_val, count)
