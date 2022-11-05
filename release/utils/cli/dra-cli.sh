#!/bin/bash

echo "Welcome to the DRA Command Line ...."
jarfile=$(find -iname "management-*")
while true; do
         prompt="(dra-cli) > "
         read -p "$prompt" input

         if [ -z "$input" ]
         then
            prompt="(dra-cli) > "
            read -p "$prompt" input

         else
               java -jar $jarfile $input
        fi



done

