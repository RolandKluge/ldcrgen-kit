# read data

t1 = read.table("test_series_n_const_n1000.data")
#t1_grouped = tapply(t1$V2, t1$V1, mean)
t1_grouped = aggregate(V2 ~ V1, data = t1, FUN = mean)
t2 = read.table("test_series_n_const_n5000.data")
t2_grouped = aggregate(V2 ~ V1, data = t2, FUN = mean)
t3 = read.table("test_series_n_const_n10000.data")
t3_grouped = aggregate(V2 ~ V1, data = t3, FUN = mean)

pdf("test_series_n_const_n1000.pdf",width=10,height=5)
plot(t1_grouped,xlab="p_in",ylab="time[s]",ylim=c(0,max(t1$V2)))
fm <- lm(formula = t1_grouped$V2 ~ t1_grouped$V1)
abline(fm, col="red")
dev.off()

pdf("test_series_n_const_n5000.pdf",width=10,height=5)
plot(t2_grouped,xlab="p_in",ylab="time[s]",ylim=c(0,max(t2$V2)))
fm <- lm(formula = t2_grouped$V2 ~ t2_grouped$V1)
abline(fm, col="red")
dev.off()

pdf("test_series_n_const_n10000.pdf",width=10,height=5)
plot(t3_grouped,xlab="p_in",ylab="time[s]",ylim=c(0,max(t3$V2)))
fm <- lm(formula = t3_grouped$V2 ~ t3_grouped$V1)
abline(fm, col="red")
dev.off()