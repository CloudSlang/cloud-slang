#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

##################################################################################################################################################
# This flow retrieves the MySQL server status and notifies the user by sending an email that contains the status or the possible errors.
# Inputs:
#    - container - name or ID of the docker container that runs MySQL
#    - dockerHost - docker machine host
#    - dockerUsername - docker machine username
#    - dockerPassword - docker machine password
#    - mysqlUsername - MySQL instance username
#    - mysqlPassword - MySQL instance password
#    - emailHost - email server host
#    - emailPort - email server port
#    - emailSender - email sender
#    - emailRecipient - email recipient
#
# Results:
#    - SUCCESS
#    - FAILURE
##################################################################################################################################################

namespace: docker.maintenance

imports:
 docker: docker.maintenance
 email: email.ops

flow:
  name: report_mysql_status

  inputs:
    - container
    - dockerHost
    - dockerUsername
    - dockerPassword
    - mysqlUsername
    - mysqlPassword
    - emailHost
    - emailPort
    - emailSender
    - emailRecipient

  workflow:
    retrieve_mysql_status:
          do:
            docker.retrieve_mysql_status:
                - dockerHost
                - dockerUsername
                - dockerPassword
                - container
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

    send_status_mail:
          do:
            email.send_mail:
                - hostname: emailHost
                - port: emailPort
                - htmlEmail: "'false'"
                - from: emailSender
                - to: emailRecipient
                - subject: "'MySQL Server Status on ' + dockerHost"
                - body: >
                     'The MySQL server status on host ' + dockerHost + ' is detected as:\nUptime: ' + uptime
                     + '\nThreads: ' + threads + '\nQuestions: ' + questions + '\nSlow queries: ' + slowQueries
                     + '\nOpens: ' + opens + '\nFlush tables: ' + flushTables + '\nOpen tables: ' + openTables
                     + '\nQueries per second avg: ' + queriesPerSecondAVG
          navigate:
            SUCCESS: SUCCESS
            FAILURE: FAILURE

    on_failure:
      send_error_mail:
        do:
          email.send_mail:
                - hostname: emailHost
                - port: emailPort
                - from: emailSender
                - to: emailRecipient
                - subject: "'MySQL Server Status on ' + dockerHost"
                - body: >
                    'The MySQL server status checking on host ' + dockerHost
                    + ' ended with the following error message: ' + errorMessage
        navigate:
          SUCCESS: FAILURE
          FAILURE: FAILURE
