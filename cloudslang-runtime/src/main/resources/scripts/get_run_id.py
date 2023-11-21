def get_run_id():
    try:
        value = get_from_smaller_context("get_run_id()")
    except NameError:
        value = globals().get("get_run_id()")
    return "" if value is None else value