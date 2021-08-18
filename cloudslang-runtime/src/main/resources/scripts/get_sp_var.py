def get_sp_var(key):
  try:
    value1 = get_from_smaller_context(key)
  except NameError:
    value1 = globals().get(key)
  accessed(value1)
  property_value = sys_prop.get(value1)
  return property_value
