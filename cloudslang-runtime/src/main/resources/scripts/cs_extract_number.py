def cs_extract_number(str, count = 1):
  nums = str.split()
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