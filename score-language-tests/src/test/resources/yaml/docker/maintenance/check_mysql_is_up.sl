#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

##################################################################################################################################################
# This operation checks if the MySQL server is up, meaning its state is alive.
# Inputs:
#    - container - name or ID of the docker container that runs MySQL
#    - host - docker machine host
#    - username - docker machine username
#    - password - docker machine password
#    - mysqlUsername - MySQL instance username
#    - mysqlPassword - MySQL instance password
#
# Outputs:
#    - errorMessage - contains the STDERR of the machine if the shh action was executed successfully, the cause of the exception otherwise
#
# Results:
#    - SUCCESS - the action was executed successfully and the MySQL server state is alive
#    - FAILURE - some problem occurred, more information in the errorMessage output
##################################################################################################################################################

namespace: docker.maintenance

operations:
  - check_mysql_is_up:
        inputs:
          - container
          - host
          - port:
                default: "'22'"
                override: true
          - username
          - password
          - privateKeyFile:
                default: "''"
                override: true
          - arguments:
                default: "''"
                override: true
          - mysqlUsername
          - mysqlPassword
          - execCmd:
                default: "'mysqladmin -u ' + mysqlUsername + ' -p' + mysqlPassword + ' ping'"
                override: true
          - command:
                default: "'docker exec ' + container + ' ' + execCmd"
                override: true
          - characterSet:
                default: "'UTF-8'"
                override: true
          - pty:
                default: "'false'"
                override: true
          - timeout:
                default: "'90000'"
                override: true
          - closeSession:
                default: "'false'"
                override: true
        action:
          java_action:
            className: org.openscore.content.ssh.actions.SSHShellCommandAction
            methodName: runSshShellCommand
        outputs:
          - errorMessage:  STDERR if returnCode == '0' else returnResult
        results:
          - SUCCESS : returnCode == '0' and returnResult == 'mysqld is alive\n'
          - FAILURE
