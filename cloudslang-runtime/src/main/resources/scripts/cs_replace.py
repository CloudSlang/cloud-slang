def cs_replace(str, old_val, new_val, count = -1):
  return str.replace(old_val, new_val) if count < 0 else str.replace(old_val, new_val, count)