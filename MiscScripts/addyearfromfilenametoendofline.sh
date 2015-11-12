#!/bin/bash
regex="yob(.*).txt"
echo "HERE"

for f in *.txt
do
	[[ $f =~ $regex ]]
	year="${BASH_REMATCH[1]}"
	echo $year
	sed "s/.$/,$year/" -i $f
done
