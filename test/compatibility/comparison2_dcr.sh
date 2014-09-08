
rm -f comparison2_dcr.graphj
rm -f comparison2_dcr.graphlog

java -jar -verbose:gc -Xloggc:gc_comparison2_dcr.log  -XX:+PrintGCDetails -Xmx2G -Xms2G DCRGenerator.jar -g k=8 n=1000 p_in=0.4 p_out=0.003 t_max=10000 theta=0.3 p_nu=0.75 p_chi=0.65 enp=true outDir=. fileName=comparison2_dcr  $@

