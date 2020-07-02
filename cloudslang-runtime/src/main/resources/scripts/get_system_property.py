def get_sp(key, default_value=None):
  accessed(key)
  property_value = sys_prop.get(key)
  return default_value if property_value is None else property_value
