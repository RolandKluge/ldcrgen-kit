# read data

maps <- c("hashmap", "treemap")
for (map in maps) {
t1 = read.table(paste("../test_series_n_const_n1000_",map,".data",sep=""))
t1_grouped = aggregate(V5 ~ V2, data = t1, FUN = mean)

t2 = read.table(paste("../test_series_n_const_n5000_",map,".data",sep=""))
t2_grouped = aggregate(V5 ~ V2, data = t2, FUN = mean)

t3 = read.table(paste("../test_series_n_const_n10000_",map,".data",sep=""))
t3_grouped = aggregate(V5 ~ V2, data = t3, FUN = mean)

pdf(paste("test_series_n_const_",map,".pdf",sep=""),width=2.8,height=2.5)
par(mar=c(3.2, 3, 0.5, 0.5))
par(mgp=c(2, 0.5, 0))
par(cex=0.7)
plot(t1_grouped,xlab="p_in",ylab="time[s]",ylim=c(0,50),type="b",col="red")
#fm <- lm(formula = t1_grouped$V2 ~ t1_grouped$V1)
#abline(fm, col="red")
lines(t2_grouped,pch=8,type="b",col="blue")
#fm <- lm(formula = t2_grouped$V2 ~ t2_grouped$V1)
#abline(fm, col="red")
lines(t3_grouped,pch=2,type="b",col="green")
#fm <- lm(formula = t3_grouped$V2 ~ t3_grouped$V1)
#abline(fm, col="red")
legend("topleft",c("n=10k", "n=5k", "n=1k"), pch=c(2,8,1), col=c("green", "blue", "red"))
dev.off()
}
