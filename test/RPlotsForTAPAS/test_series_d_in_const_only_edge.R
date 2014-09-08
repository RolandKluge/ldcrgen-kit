# Start using scientific notation at 1E5
options(scipen=5)

maps <- c("hashmap", "treemap")
for (map in maps) {
t1 = read.table(paste("../test_series_p_in_const_pin0.3_",map,".data",sep=""))
t1_grouped = aggregate(V4 ~ V1, data = t1, FUN = mean)
t2 = read.table(paste("../test_series_p_in_const_pin0.6_",map,".data",sep=""))
t2_grouped = aggregate(V4 ~ V1, data = t2, FUN = mean)
t3 = read.table(paste("../test_series_p_in_const_pin0.9_",map,".data",sep=""))
t3_grouped = aggregate(V4 ~ V1, data = t3, FUN = mean)

pdf(paste("test_series_p_in_const_",map,".pdf",sep=""),width=2.8,height=2.5)
par(mar=c(3.2, 3, 0.5, 0.5))
par(mgp=c(2, 0.5, 0))
par(cex=0.7)
plot(t1_grouped,xlab="number of nodes",ylab="time[s]",ylim=c(1,100), log="xy", type="b", col="red")
#c <- coefficients(lm(formula = sqrt(t1_grouped$V2) ~ t1_grouped$V1))
#curve(c[1]*c[1] + x*x*c[2]*c[2] + 2*c[2]*c[1]*x,add=TRUE,col="red")

lines(t2_grouped,pch=8,type="b",col="blue")
#c <- coefficients(lm(formula = sqrt(t2_grouped$V2) ~ t2_grouped$V1))
#curve(c[1]*c[1] + x*x*c[2]*c[2] + 2*c[2]*c[1]*x,add=TRUE,col="red")

points(t3_grouped,pch=2, type="b",col="green")
#c <- coefficients(lm(formula = sqrt(t3_grouped$V2) ~ t3_grouped$V1))
#curve(c[1]*c[1] + x*x*c[2]*c[2] + 2*c[2]*c[1]*x,add=TRUE,col="red")
legend("topleft",c("p_in=0.9", "p_in=0.6", "p_in=0.3"), pch=c(2,8,1), col=c("green", "blue", "red"))
dev.off()
}
