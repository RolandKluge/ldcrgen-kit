#!/bin/bash

numRuns=15
pIns="0.1 0.4 0.8 1.0"
#pIns="0.1 0.5 0.9"
pOut="0.01"
ns="1000 5000 10000" 
#ns="1000 2000"
k=15
t_max=1000000

time_tmp_file="time_tmp_file.txt"

result=0
maps="hashmap treemap"

for map in $maps;
do
echo "
--------------------------------------------------------------------------------

RUNNING ALL "n = const" TIMINGS FOR ${map}

--------------------------------------------------------------------------------
"
  if [ "$result" -eq "0" ];
  then
    for n in $ns;
    do  
        if [ "$result" -eq "0" ];
        then
          echo "Timings for $n nodes"
          data_file="test_series_n_const_n${n}_${map}.data"

          rm -f $data_file

          # Assuming that all CPUs are identical
          echo "#
# cluster count k $k
# t_max $t_max
# pOut $pOut
# Hostname: $(hostname)
# CPU: $(cat /proc/cpuinfo | grep "model name" | head -n 1)
# Test runs: $numRuns
# Map type for FYS: $map
# Legend
# <n> <p_in> <time_for_init> <time_for_iterations> <total_time>
#" >> $data_file

          for pIn in $pIns;
          do
              if [ "$result" -eq "0" ];
              then
                echo -e "\t n=${n} pIn=${pIn}"
    #             echo -n "$pIn " >> $data_file
                /usr/bin/time -o $time_tmp_file -f "%e" $(dirname $0)/../run.sh -g p_chi=1 p_omega=0 binary=false n=$n k=$k t_max=$t_max p_in=$pIn p_out=$pOut r=$numRuns:$data_file:${map} output=tmp
                result=$?
                time=$(cat $time_tmp_file) 
                avgTime=$(echo "scale=3;$time/$numRuns" | bc -l)
    #             echo $avgTime >> $data_file
                echo -e "\t Num Runs $numRuns - Time $time - Average Time per Run: $avgTime"
                printf "\n"
              fi
          done
        fi
    done
  fi
done

if [ "$result" != "0" ];
then
  echo "ABORTED"
fi

rm -f tmp.graphj
rm -f $time_tmp_file
