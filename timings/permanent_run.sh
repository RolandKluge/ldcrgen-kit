#!/bin/bash

while [ true ];
do
	../run.sh -l -g cl_sizes=[20,30,40,50,60,70,80,90,200] p_in_list=[1.0,0.9,0.8,0.7,0.6,0.5,0.5,0.5,0.4] eta=1 p_in_new=GAUSSIAN theta=0.5 t_max=10000 p_out=0.0001
done
