# read data

t1 = read.table("test_series_p_in_const_pin0.3.data")
t1_grouped = aggregate(V2 ~ V1, data = t1, FUN = mean)
t2 = read.table("test_series_p_in_const_pin0.6.data")
t2_grouped = aggregate(V2 ~ V1, data = t2, FUN = mean)
t3 = read.table("test_series_p_in_const_pin0.9.data")
t3_grouped = aggregate(V2 ~ V1, data = t3, FUN = mean)


pdf("test_series_p_in_const_pin03.pdf",width=10,height=5)
plot(t1_grouped,xlab="number of nodes",ylab="time[s]",ylim=c(0,max(t1$V2)))
c <- coefficients(lm(formula = sqrt(t1_grouped$V2) ~ t1_grouped$V1))
curve(c[1]*c[1] + x*x*c[2]*c[2] + 2*c[2]*c[1]*x,add=TRUE,col="red")
dev.off()

pdf("test_series_p_in_const_pin06.pdf",width=10,height=5)
plot(t2_grouped,xlab="number of nodes",ylab="time[s]",ylim=c(0,max(t2$V2)))
c <- coefficients(lm(formula = sqrt(t2_grouped$V2) ~ t2_grouped$V1))
curve(c[1]*c[1] + x*x*c[2]*c[2] + 2*c[2]*c[1]*x,add=TRUE,col="red")
dev.off()

pdf("test_series_p_in_const_pin09.pdf",width=10,height=5)
plot(t3_grouped,xlab="number of nodes",ylab="time[s]",ylim=c(0,max(t3$V2)))
c <- coefficients(lm(formula = sqrt(t3_grouped$V2) ~ t3_grouped$V1))
curve(c[1]*c[1] + x*x*c[2]*c[2] + 2*c[2]*c[1]*x,add=TRUE,col="red")
dev.off()