# 2016.7.4, nth difference to predict directly from test set
test_data<-read.table('test.csv',as.is=T,header=T,row.name=1,sep=',')
test<-sapply(test_data[,1],function(x){as.numeric(strsplit(x,split=',')[[1]])})
names(test)<-rownames(test_data)
options(scipen=999) # repress scientific notation

cal_diff<-function(x){
    return(x[-1]-x[-length(x)]>)
}
equal_diff<-function(x,nth=1){
    while(length(x)>1&length(unique(x))>1){
        x<-cal_diff(x)
        if(length(unique(x))==1) break
    }
    return(x)
}

# scan sequences that could be solved by nth difference
equal_diff_res<-sapply(test,equal_diff)
diff_solve_index<-which(sapply(equal_diff_res,length)>1) # [1] 2819

# 2016.7.18, calculate the last integer for those sequences
rec_diff<-function(x){ # here is tricky to return a neat list
    if(length(unique(x))>1) return(c(list(x),rec_diff(x[-1]-x[-length(x)])))
    else return(list(x))
}
cal_last<-function(xx){
    if(length(xx)>1){
        xx[[length(xx)-1]]<-c(xx[[length(xx)-1]],xx[[length(xx)]][length(xx[[length(xx)]])]+xx[[length(xx)-1]][length(xx[[length(xx)-1]])])
        return(cal_last(xx[-length(xx)]))
    }else return(xx[[1]][length(xx[[1]])])
}
diff_solve_predict<-sapply(test[diff_solve_index], function(x){cal_last(rec_diff(x))}) # 2819

# 2016.7.20, write results for this part
diff_res<-cbind(names(test),rep(0,length(test)))
rownames(diff_res)<-diff_res[,1]
diff_res[names(diff_solve_predict),2]<-diff_solve_predict
colnames(diff_res)<-c('Id','Last')
options(scipen=999) # repress scientific notation
write.csv(file='diff_res.csv',diff_res,row.names=F,col.names=T,quote=F)


# 2016.7.20, the second rule, linear regression: x(n)=a*x(n-1)+b*x(n-2)+c(n-3)+d
lm_fit<-function(x,len=4,num=10){
    if(length(x)<10) return(0)
    xx<-x
    x<-x[1:10]
    ind1<-1:(length(x)-len+1)
    ind2<-len:length(x)
    data<-t(mapply(function(y1,y2){
        return(x[y1:y2])
    },y1=ind1,y2=ind2))
    lm_model<-lm(data[,4]~data[,3]+data[,2]+data[,1])
    data<-data.frame(t(xx[(length(xx)-2):length(xx)]))
    pv<-summary(lm_model)$coefficients[,4] # remove NA p value
    pv<-pv[!is.na(pv)]
    if(sum(pv<10^(-20))>1){
        return(round(predict.lm(lm_model,newdata=data)))
    }else return(0)
}

lm_solve<-sapply(test,lm_fit)
names(lm_solve)<-names(test)
# combine those results to "diff_res"
lm_res<-diff_res
lm_res[which(lm_solve>0),2]<-lm_solve[which(lm_solve>0)]
write.csv(file='lm_res.csv',lm_res,quote=F)

save.image('difference.rdata')
