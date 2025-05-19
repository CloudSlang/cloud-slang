def get_system_truststore_path():
    try:
        value = get_from_smaller_context("get_system_truststore_path()")
    except NameError:
        value = globals().get("get_system_truststore_path()")
    return "" if value is None else value
