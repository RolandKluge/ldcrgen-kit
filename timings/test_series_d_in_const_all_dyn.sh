#/bin/bash

numRuns=15
DInOuts="3,1 10,5 20,10"
ns="1000 5000 10000 20000 30000 40000 50000 60000 70000 80000 90000 100000"
#ns="1000 5000 10000 15000 20000 25000 30000 35000 40000"
t_max=100000
p_chi="0.9"
p_nu="0.5"
p_omega="0.01"
p_mu="0.5"
beta="2"
eta="1"
p_in_new="GAUSSIAN"

time_tmp_file="time_tmp_file.txt"

result=0
maps="hashmap treemap"

for map in $maps;
do
echo "
--------------------------------------------------------------------------------

RUNNING ALL TIMINGS FOR ${map}

--------------------------------------------------------------------------------
"
  if [ "$result" -eq "0" ];
  then
    for DInOut in $DInOuts
    do 
	dIn=$(echo $DInOut | cut -f 1 -d ",")
	dOut=$(echo $DInOut | cut -f 2 -d ",")
	echo $dIn
	echo $dOut
        if [ "$result" -eq "0" ];
        then
          echo "Timings for degrees: ${DInOut}"
          data_file="test_series_d_in_const_all_dyn_DInOut${dIn}_${dOut}_${map}.data"

          rm -f $data_file
          
          # Assuming that all CPUs are identical
          echo "#
# t_max $t_max
# pOut $pOut
# Hostname: $(hostname)
# CPU: $(cat /proc/cpuinfo | grep "model name" | head -n 1)
# Test runs: $numRuns
# Map type for FYS: $map
# Legend
# <n> <d_in> <d_out> <time_for_init> <time_for_iterations> <total_time>
#" >> $data_file

          for n in ${ns}
          do
	      k=$(echo "sqrt($n)" | bc)
	      echo "n is ${n}"
	      echo "k is ${k}"
              if [ "$result" -eq "0" ];
              then
                echo -e "\t n=${n} dIn=${dIn} dOut=${dOut}"
    #             echo -n "$n " >> $data_file
                /usr/bin/time -o $time_tmp_file -f "%e" $(dirname $0)/../run.sh -g binary=false p_chi=${p_chi} p_nu=${p_nu} p_omega=${p_omega} p_mu=${p_mu} beta=${beta} eta=${eta} p_in_new=${p_in_new} n=${n} k=${k} t_max=${t_max} deg_in=${dIn} deg_out=${dOut} r="${numRuns}:${data_file}:${map}" output=tmp
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

[ "$result" != "0" ] &&  echo "ABORTED"

# rm -f tmp.graphj
rm -f $time_tmp_file
