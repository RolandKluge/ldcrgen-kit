java -jar -verbose:gc -Xloggc:gc_comparison2_ldcr.log -XX:+PrintGCDetails -Xmx2G -Xms2G ldcrgen.jar -g k=8 n=1000 p_in=0.4 p_out=0.003 t_max=10000 theta=0.3 p_nu=0.75 p_chi=0.65 p_in_new=GAUSSIAN dir=. output=comparison2_ldcr  $@

