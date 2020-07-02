def get(key, default_value=None):
  value = globals().get(key)
  return default_value if value is None else value
