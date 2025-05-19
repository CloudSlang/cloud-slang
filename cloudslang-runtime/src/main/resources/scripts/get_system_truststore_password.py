def get_system_truststore_password():
    try:
        value = get_from_smaller_context("get_system_truststore_password()")
    except NameError:
        value = globals().get("get_system_truststore_password()")
    return "" if value is None else value
