#!/bin/sh
/usr/local/jdk/bin/java -jar -Dcgi.content_type=$CONTENT_TYPE -Dcgi.content_length=$CONTENT_LENGTH -Dcgi.request_method=$REQUEST_METHOD -Dcgi.query_string=$QUERY_STRING -Dcgi.server_name=$SERVER_NAME -Dcgi.server_port=$SERVER_PORT -Dcgi.script_name=$SCRIPT_NAME -Dcgi.path_info=$PATH_INFO build/libs/pkg.jar neilw4.omin.pkg 2>> log.txt
