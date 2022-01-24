#!/bin/sh

f=$(find . -type f -name version.txt | grep src/version.txt)
v1=$1
v2=$2

for n in $f
do
  if (grep "version=$v1" $n > /dev/null) then
    mv $n /tmp/version.$$.txt
    sed -e "s/version=$v1/version=$v2/" < /tmp/version.$$.txt > $n
  else
    echo "$n :Bad version"
  fi
done
