#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

##################################################################################################################################################
# This flow retrieves the MySQL server status from a docker container.
# Inputs:
#    - container - name or ID of the docker container that runs MySQL
#    - dockerHost - docker machine host
#    - dockerUsername - docker machine username
#    - dockerPassword - docker machine password
#    - mysqlUsername - MySQL instance username
#    - mysqlPassword - MySQL instance password
#
# Outputs:
#    - uptime - The number of seconds the MySQL server has been running.
#    - threads - The number of active threads (clients).
#    - questions - The number of questions (queries) from clients since the server was started.
#    - slowQueries - The number of queries that have taken more than long_query_time(MySQL system variable) seconds
#    - opens - The number of tables the server has opened.
#    - flushTables - The number of flush-*, refresh, and reload commands the server has executed.
#    - openTables - The number of tables that currently are open.
#    - queriesPerSecondAVG - an average value of the number of queries in a second
#    - errorMessage - possible error message, may contain the STDERR of the machine or the cause of an exception
#
# Results:
#    - SUCCESS
#    - FAILURE
##################################################################################################################################################

namespace: docker.maintenance

imports:
 docker: docker.maintenance
 linux_ops: linux.ops

flow:
  name: retrieve_mysql_status

  inputs:
    - container
    - dockerHost
    - dockerUsername
    - dockerPassword
    - mysqlUsername
    - mysqlPassword

  workflow:
    validate_linux_machine_ssh_access:
          do:
            linux_ops.validate_linux_machine_ssh_access:
              - host: dockerHost
              - username: dockerUsername
              - password: dockerPassword
          publish:
              - errorMessage

    check_mysql_is_up:
          do:
            docker.check_mysql_is_up:
              - container
              - host: dockerHost
              - username: dockerUsername
              - password: dockerPassword
              - mysqlUsername
              - mysqlPassword
          publish:
            - errorMessage

    get_mysql_status:
          do:
            docker.get_mysql_status:
              - container
              - host: dockerHost
              - username: dockerUsername
              - password: dockerPassword
              - mysqlUsername
              - mysqlPassword
          publish:
              - uptime
              - threads
              - questions
              - slowQueries
              - opens
              - flushTables
              - openTables
              - queriesPerSecondAVG
              - errorMessage

  outputs:
    - uptime
    - threads
    - questions
    - slowQueries
    - opens
    - flushTables
    - openTables
    - queriesPerSecondAVG
    - errorMessage
