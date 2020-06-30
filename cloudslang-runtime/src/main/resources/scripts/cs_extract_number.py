def cs_extract_number(string, count = 1):
  if type(string).__name__ != 'str' and type(string).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'string', got " + str(string))

  nums = string.split()
  n = count
  result = None
  for x in nums:
    if x.isdigit():
      if n == 1:
        result = x
        break
      else:
        n = n - 1
  return result