rm -f comparison1_dcr.graphj
rm -f comparison1_dcr.graphlog

java -jar -verbose:gc -Xloggc:gc_comparison1_dcr.log  -XX:+PrintGCDetails -Xmx2G -Xms2G DCRGenerator.jar -g n=100 p_in=0.3 p_out=0.01 k=5 t_max=1000 outDir=. binary=true fileName=comparison1_dcr  $@

