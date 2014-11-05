#!/bin/bash

for v in $(seq 1 $1);
do
	output="$(java -cp bin cp/articlerep/MainRep $2 $3 $4 $5 $6 $7 $8 $9 ${10})";
	eval res=($output);
	result=$(( $result + ${res[6]} ));
done

echo -e "\n******************************************* | Report | *******************************************";
echo -e "Number of iterations: $1";
echo -e "Running command: java -cp bin cp/articlerep/MainRep $2 $3 $4 $5 $6 $7 $8 $9 ${10}\n";
echo "Final result: $((result / $1))";
echo "";