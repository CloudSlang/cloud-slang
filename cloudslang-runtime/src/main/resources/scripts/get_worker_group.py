def get_worker_group():
    try:
        value = get_from_smaller_context("get_worker_group()")
    except NameError:
        value = globals().get("get_worker_group()")
    return "" if value is None else value
