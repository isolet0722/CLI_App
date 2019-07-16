#!/bin/bash

top -d 0.5 -b -n2 | grep "Cpu(s)" | tail -n 1 | awk '{printf "{\"cpu\": {\"load\": \"%s\",", $2+$4}'

IFS=$'\n'
RESULT=($(lscpu | grep -E 'CPU|Core|Model name' | awk -F ':' '{print $2}' | sed -e 's/^[[:space:]]*//'))
IFS=$IFS_backup

printf "\"thread\": \"${RESULT[1]}\", \"core\": \"${RESULT[3]}\", \"model\": \"${RESULT[5]}\", \"current_clock\": \"${RESULT[6]}\", \"max_clock\": \"${RESULT[7]}\", \"min_clock\": \"${RESULT[8]}\"},"

free -m | awk 'NR==2{printf "\"memory\": {\"used\": \"%s\", \"max\": \"%s\", \"percentage\": \"%.2f\",", $3, $2, $3*100/$2}'
free -m | awk 'NR==3{printf "\"swap\": \"%s\"},", $4}'

ps -Ao user,pid,comm,pcpu,pmem --sort=-pcpu | head -n 21 | awk '
BEGIN { ORS = ""; print "\"process\": [ "}
{ printf "%s{\"user\": \"%s\", \"pid\": \"%s\", \"cmd\": \"%s\", \"cpu\": \"%s\",  \"mem\": \"%s\"}",
      separator, $1, $2, $3, $4, $5
  separator = ", "
}
END { print "],\n" }';
