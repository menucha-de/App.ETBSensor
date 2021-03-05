#!/bin/sh

# example
#
# http://localhost/cgi-bin/execute.sh?argument=42

COMMAND_PREFIX="java -Djava.util.logging.config.file=/opt/havis.custom.vag.rls/logging.properties -Dhavis.custom.vag.rls.send=239.0.2.131 -Dhavis.custom.vag.rls.recv=239.0.2.231 -jar /opt/havis.custom.vag.rls/havis.custom.vag.rls.jar -c"
COMMAND=""
ERROR_MESSAGE=""

if [ -z ${QUERY_STRING} ] ; then
    ERROR_MESSAGE="No query string found!"

else
    ARGUMENT=`echo ${QUERY_STRING} | sed -n 's/^.*argument=\([^&]*\).*$/\1/p'`

    # URL decode
    ARGUMENT=`echo ${ARGUMENT} | sed -e's/%\([0-9A-F][0-9A-F]\)/\\\\\x\1/g'`
    ARGUMENT=`/bin/echo -e ${ARGUMENT}`

    if [ -z ${ARGUMENT} ] ; then
        ERROR_MESSAGE="Invalid query string found! Please specify the \"argument\" parameter."

    else
        # build command
        COMMAND="${COMMAND_PREFIX} ${ARGUMENT}"
    fi
fi

if [ -n "${COMMAND}" ] ; then
    echo "Status: 200 OK"
    echo "Content-Type: text/plain; charset=utf-8"
    echo
    ( ${COMMAND} ) 2>&1

elif [ -n "${ERROR_MESSAGE}" ] ; then
    echo "Status: 400 Bad Request"
    echo "Content-Type: text/plain; charset=utf-8"
    echo
    echo ${ERROR_MESSAGE}
fi

