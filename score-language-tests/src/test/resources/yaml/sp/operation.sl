#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

imports:
  props: user.sys.props

operations:
  name: check_weather:
  inputs:
    - city:
        system_property: props.city
        default: "'Bangkok'"
        overridable: false
    - weather:
        system_property: props.weather
        default: "'hot'"
  action:
    python_script: 'print "The weather is " + weather + " in " + city'
