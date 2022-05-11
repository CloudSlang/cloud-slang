import re
def cs_extract_number(string, count = 1):
  if type(string).__name__ != 'str' and type(string).__name__ != 'unicode':
    raise Exception("Expected a string for parameter 'string', got " + str(string))

  result = None
  numbers = re.findall("[0-9]*\.?[0-9]+", string)

  count = int(count)
  if len(numbers) >= count & count >= 1:
    result = numbers[count - 1]

  return result