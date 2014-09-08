table <- read.table("fy_shuffle_build_up_sizes.data",header=TRUE)
k <- coef(lm(ms ~ size-1, data=table))
k_rounded <- round(k,digits=5)

pdf("fy_shuffle_build_up_sizes.pdf")

plot(table);
#curve(k[1] + k[2] * x, add=TRUE);
abline(a=0,b=k[1])
grid();
legend("bottomright", legend = c(paste(k_rounded[1], " * size"),"ms"), col = 1:1, pch=c(-1,1), lty = 1:0) 

dev.off()
