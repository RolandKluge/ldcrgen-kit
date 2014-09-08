java -jar -verbose:gc -Xloggc:gc_comparison1_ldcr.log  -XX:+PrintGCDetails -Xmx2G -Xms2G ldcrgen.jar -g n=100 p_in=0.3 p_out=0.01 k=5 t_max=1000 dir=. output=comparison1_ldcr $@

