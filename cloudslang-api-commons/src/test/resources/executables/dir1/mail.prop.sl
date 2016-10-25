# (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
# The Apache License is available at
# http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
# Sample system property file for email protocol
#
# io.cloudslang.base.hostname: host name - Example: smtp.example.com
# io.cloudslang.base.port: port
# io.cloudslang.base.from: sender email
# io.cloudslang.base.to: receiver email
# io.cloudslang.base.username: sender username
# io.cloudslang.base.password: sender password
#
####################################################

namespace: io.cloudslang.base

properties:
  - hostname: localhost
  - port: "49154"
  - from: user@example.com
  - to: otheruser@example.com
  - username: user
  - password: pwd
