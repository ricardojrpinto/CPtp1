#!/bin/bash

SCAL_OUT="output_scalability.txt"
echo `rm $SCAL_OUT`


declare -a RESULTS
ITER=5
N_THREADS=1
count=0
for v in $(seq 1 $ITER) ; do
	echo -e "Command: java -cp bin cp/articlerep/MainRep 10 $N_THREADS 20 25 25 50 10 10 4" ;
	echo "params: nthreads=$N_THREADS r/w=50/50" >> $SCAL_OUT ;
	output="$(java -cp bin cp/articlerep/MainRep 10 $N_THREADS 20 25 25 50 10 10 4)";
	eval res=($output);
	RESULTS[$count]=${res[0]};
	N_THREADS=$(( $N_THREADS * 2 ));
	echo "${RESULTS[$count]}" >> $SCAL_OUT ;
	count=$(( $count + 1 ));
done

N_THREADS=1
count=0
for v in $(seq 1 $ITER) ; do
	echo -e "Command: java -cp bin cp/articlerep/MainRep 10 $N_THREADS 20 10 10 80 10 10 4" ;
	echo "params: nthreads=$N_THREADS r/w=80/20" >> $SCAL_OUT ;
	output="$(java -cp bin cp/articlerep/MainRep 10 $N_THREADS 20 10 10 80 10 10 4)";
	eval res=($output);
	RESULTS[$count]=${res[0]};
	N_THREADS=$(( $N_THREADS * 2 ));
	echo "${RESULTS[$count]}" >> $SCAL_OUT ;
	count=$(( $count + 1 ));
done
