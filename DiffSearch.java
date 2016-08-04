/* 2016.8.1, improve difference based algorithm 
 * 1) read test and training set
 * 2) calculate difference
 * 3) search diff-seq in the database, if hit, predict the next integer; if not, continue 2)
 */
import java.io.*;
import java.util.*;
import java.math.*;
class DiffSearch{
    static public void main(String args[]){
	new DiffSearch();
    }
    ArrayList<ArrayList<BigInteger>> test=new ArrayList<ArrayList<BigInteger>>();
    ArrayList<Integer> test_ind=new ArrayList<Integer>();
    ArrayList<ArrayList<BigInteger>> data=new ArrayList<ArrayList<BigInteger>>();
    public DiffSearch(){
	String s;
	String[] ss;
	ArrayList<BigInteger> seq=new ArrayList<BigInteger>();
	int k=0;
	try{
	    BufferedReader in=new BufferedReader(new FileReader(new File("test.csv").getAbsoluteFile()));
	    try{
		in.readLine();
		while((s=in.readLine())!=null){
		    k++;
		    ss=(s.split("\"")[1]).split(",");
		    seq=new ArrayList<BigInteger>();
		    for(int i=0;i<ss.length;i++){
			try{
			    /*
			           if(ss.length==1){
				    seq.add(new BigInteger(ss[i].substring(1,ss[i].length()-1)));
				     continue;
				          }
			    */
			    //     if(i==0) seq.add(new BigInteger(ss[i].substring(1,ss[i].length())));
			    //     else if(i==(ss.length-1)) seq.add(new BigInteger(ss[i].substring(0,ss[i].length()-1)));
			    seq.add(new BigInteger(ss[i].trim()));
			}catch(NumberFormatException e){
			    System.out.println(k+" "+ss.length+" "+ss[0]+" "+test.get(0));
			    throw new RuntimeException(e);
			}
		    }
		    test_ind.add(Integer.valueOf(s.split(",")[0]));
		    test.add(seq);
		}
	    }finally{
		in.close();
	    }
	}catch(IOException e){
	    throw new RuntimeException(e);
	}
	System.out.println("k: "+k+"; test size:  "+test.size()+"; test index: "+test_ind.size()+" "+test_ind.get(10));
	/*
	ArrayList<BigInteger> alb=new ArrayList<BigInteger>();
	for(int i=0;i<15;i++) alb.add(test.get(10).get(i+5));
	System.out.println(alb);
	for(int i=0;i<20;i++) System.out.println("seq "+i+": "+match_subseq(alb,test.get(i)));
	*/
	// read training set and merge into test as the whole dataset
	data.addAll(test);
	k=0;
	try{
	    BufferedReader in2=new BufferedReader(new FileReader(new File("train.csv").getAbsoluteFile()));
	    try{
		in2.readLine();
		while((s=in2.readLine())!=null){
		    k++;
		    ss=(s.split("\"")[1]).split(",");
		    seq=new ArrayList<BigInteger>();
		    for(int i=0;i<ss.length;i++){
			try{
			    seq.add(new BigInteger(ss[i].trim()));
			}catch(NumberFormatException e){
			    System.out.println(k+" "+ss.length+" "+ss[0]);
			    throw new RuntimeException(e);
			}
		    }
		    data.add(seq);
		}
	    }finally{
		in2.close();
	    }
	}catch(IOException e){
	    throw new RuntimeException(e);
	}
	System.out.println("k: "+k+"; total data: "+data.size());
	/*
	ArrayList<BigInteger> tmp=test.get(57);
	while(tmp.size()>=2){
	    System.out.println(tmp+" "+all_equal(tmp));
	    tmp=get_diff(tmp);
	}
	*/
	brutal_search();
    }
    void brutal_search(){
	ArrayList<BigInteger> res=new ArrayList<BigInteger>();
	int hit=0;
	int index=0;
	int diff_len=6;
	ArrayList<BigInteger> diff=new ArrayList<BigInteger>();
	boolean match_sub=false;
	for(int i=57;i<58;i++){
	    diff=test.get(i);
	    while(diff.size()>=diff_len){
		System.out.println(diff);
		if(all_equal(diff)){
		    hit++;
		    break;
		}
		match_sub=false;
		for(ArrayList<BigInteger> alb:data){
		    index=match_subseq(diff,alb);
		    if(index!=-1){
			match_sub=true;
			System.out.println("hit: "+alb.get(index));
			break;
		    }
		}
		if(match_sub){
		    hit++;
		    break;
		}else diff=get_diff(diff);
	    }
	}
	System.out.println("hit: "+hit);
    }
    ArrayList<BigInteger> get_diff(ArrayList<BigInteger> x){
	ArrayList<BigInteger> diff=new ArrayList<BigInteger>();
	for(int i=1;i<x.size();i++)
	    diff.add(x.get(i).subtract(x.get(i-1)));
	return(diff);
    }
    boolean all_equal(ArrayList<BigInteger> x){
	boolean res=true;
	for(int i=1;i<x.size();i++){
	    if(!x.get(i-1).equals(x.get(i))){
		res=false;
		break;
	    }
	}
	return(res);
    }
    int match_subseq(ArrayList<BigInteger> x, ArrayList<BigInteger> seq){ // return the index of next integer, otherwise return -1
	if(seq.size()<x.size()) return(-1);
	int index=0;
	int i=0;
	boolean hit=true;
	while((seq.size()-index)>=x.size()){
	    hit=true;
	    for(i=0;i<x.size();i++){
		if(!x.get(i).equals(seq.get(index+i))){
		    hit=false;
		    break;
		}
	    }
	    if(hit){
		break;
	    }else{
		index++;
	    }
	}
	if((index+x.size())>=seq.size()){
	    return(-1);
	}else{
	    return(index+x.size());
	}
    }
}
