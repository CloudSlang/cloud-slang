def cs_extract_number(string, count = 1):
  if type(string).__name__ != 'str' and type(string).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'string', got " + str(string))

  result = None
  numbers = re.findall("[0-9]*\.?[0-9]+", string)

  result = numbers[count - 1]
  return result