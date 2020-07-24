def get(key, default_value=None):
  value = get_from_smaller_context(key)
  return default_value if value is None else value
