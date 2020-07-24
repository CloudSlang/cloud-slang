def get(key, default_value=None):
  try:
    value = get_from_smaller_context(key)
  except NameError:
    value = globals().get(key)
  return default_value if value is None else value
