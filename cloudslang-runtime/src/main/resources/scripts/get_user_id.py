def get_user_id():
    try:
        value = get_from_smaller_context("get_user_id()")
    except NameError:
        value = globals().get("get_user_id()")
    return "" if value is None else value
