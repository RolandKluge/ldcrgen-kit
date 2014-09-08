# read data

t1 = read.table(paste("../test_series_n_const_n1000_hashmap.data",sep=""))
t1_grouped = aggregate(V4 ~ V2, data = t1, FUN = mean)

#t2 = read.table(paste("../test_series_n_const_n5000_",map,".data",sep=""))
#t2_grouped = aggregate(V4 ~ V2, data = t2, FUN = mean)

t2 = read.table(paste("../test_series_n_const_n10000_hashmap.data",sep=""))
t2_grouped = aggregate(V4 ~ V2, data = t2, FUN = mean)

t3 = read.table(paste("../test_series_n_const_n1000_treemap.data",sep=""))
t3_grouped = aggregate(V4 ~ V2, data = t3, FUN = mean)

t4 = read.table(paste("../test_series_n_const_n10000_treemap.data",sep=""))
print(t4)
t4_grouped = aggregate(V4 ~ V2, data = t4, FUN = mean)

pdf(paste("test_series_n_const_all_maps_cr.pdf",sep=""),width=2.8,height=2.5)
par(cex=0.7)
par(mar=c(3.2, 3, 0.5, 0.5))
par(mgp=c(2, 0.5, 0))

plot(t1_grouped,xlab="p_in",ylab="time[s]",ylim=c(0,35),type="b",col="red")
lines(t2_grouped,pch=8,type="b",col="blue")
lines(t3_grouped,pch=2,type="b",col="green")
lines(t4_grouped,pch=5,type="b",col="black")
legend("topleft",c("n=10k, treemap", "n=1k, treemap", "n=10k, hashmap", "n=1k, hashmap"), pch=c(5,2,8,1), col=c("black", "green", "blue", "red"))
dev.off()
