#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
 ops: user.ops

flow:
  name: on_failure_one_step
  inputs:
    - userNumber
    - emailHost
    - emailPort
    - emailSender
    - emailRecipient
  workflow:
    - check_number:
        do:
          ops.check_number:
            - number: ${ userNumber }
        publish:
          - new_number: ${ preprocessed_number } # publish the output in the flow level so it will be visible for other steps
        navigate:
          - EVEN: process_even_number
          - ODD: process_odd_number
          - FAILURE: on_failure

    - process_even_number:
        do:
          ops.process_even_number:
            - even_number: ${ new_number }
        navigate:
          - SUCCESS: SUCCESS # end flow with success result

    - process_odd_number:
        do:
          ops.process_odd_number:
            - odd_number: ${ new_number }
        navigate:
          - SUCCESS: SUCCESS # end flow with success result

    - on_failure: # you can also use this step for default navigation in failure case
        - send_error_mail: # or refer it by the step name
            do:
              ops.send_email_mock:
                - hostname: ${ emailHost }
                - port: ${ emailPort }
                - sender: ${ emailSender }
                - recipient: ${ emailRecipient }
                - subject: 'Flow failure'
                - body: >
                    ${ 'Wrong number: ' + str(userNumber) }

