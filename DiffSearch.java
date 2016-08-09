/* 2016.8.1, improve difference based algorithm 
 * 1) read test and training set
 * 2) calculate difference
 * 3) search diff-seq in the database, if hit, predict the next integer; if not, continue 2)
 * 2016.8.8, compute in 2nd round. Hit number increases from 15073 to 15080, only 7. Not good.
 * 2016.8.9, add an option to include cases of matching negative sequence
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
    int diff_len=6; // sequence length threshold
    int diff_level=3; // difference depth threshold
    HashSet<Integer> lm_index=new HashSet<Integer>();
    int round=1; // the round of learning
    public DiffSearch(){
	String s;
	String[] ss;
	ArrayList<BigInteger> seq=new ArrayList<BigInteger>();
	int k=0;
	int level=0;
	int k1=0;
	int k2=0;
	try{
	    //	    BufferedReader in0=new BufferedReader(new FileReader(new File("brutal_level3_seq.txt").getAbsoluteFile()));
	    BufferedReader in0=new BufferedReader(new FileReader(new File("lm_l5_res_seq.txt").getAbsoluteFile()));
	    BufferedReader in=new BufferedReader(new FileReader(new File("test.csv").getAbsoluteFile()));
	    try{
		/*
		// input predicted results from "difference.r" by linear regression
		while((s=in0.readLine())!=null){
		    k1++;
		    ss=(s.split("\t")[1]).split(",");
		    lm_index.add(Integer.valueOf(s.split("\t")[0]));
		    seq=new ArrayList<BigInteger>();
		    for(int i=0;i<ss.length;i++){
			seq.add(new BigInteger(ss[i].trim()));
		    }
		    level=0;
		    while(level<=diff_level&&seq.size()>=diff_len){
			data.add(seq);
			seq=get_diff(seq);
			level++;
		    }
		}
		*/
		// 2016.8.8, input predicted results from "brutal_level3.txt"
		while((s=in0.readLine())!=null){
		    k1++;
		    ss=(s.split("\t")[1]).split(",");
		    lm_index.add(Integer.valueOf(s.split("\t")[0]));
		    seq=new ArrayList<BigInteger>();
		    for(int i=0;i<ss.length;i++){
			seq.add(new BigInteger(ss[i].trim()));
		    }
		    level=0;
		    while(level<=diff_level&&seq.size()>=diff_len){
			data.add(seq);
			seq=get_diff(seq);
			level++;
		    }
		}
		System.out.println("read previous result: "+k1+"; data size:  "+data.size()+"; previous result index: "+lm_index.size());
	
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
		    if(lm_index.contains(Integer.valueOf(s.split(",")[0]))){ // do not add sequences that have already been predicted in lm_res 
			k2++;
			continue;
		    }
		    level=0;
		    while(level<=diff_level&&seq.size()>=diff_len){
			data.add(seq);
			seq=get_diff(seq);
			level++;
		    }
		}
	    }finally{
		in0.close();
		in.close();
	    }
	}catch(IOException e){
	    throw new RuntimeException(e);
	}
	System.out.println("Exclude in test: "+k2);
	System.out.println("k: "+k+"; test size:  "+test.size()+"; test index: "+test_ind.size()+" "+test_ind.get(10));
	/*
	ArrayList<BigInteger> alb=new ArrayList<BigInteger>();
	for(int i=0;i<15;i++) alb.add(test.get(10).get(i+5));
	System.out.println(alb);
	for(int i=0;i<20;i++) System.out.println("seq "+i+": "+match_subseq(alb,test.get(i)));
	*/
	// read training set and merge into test as the whole dataset
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
		    level=0;
		    while(level<=diff_level&&seq.size()>=diff_len){
			data.add(seq);
			seq=get_diff(seq);
			level++;
		    }
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
	HashMap<Integer,BigInteger> res=new HashMap<Integer,BigInteger>();
	int hit=0;
	int index=0;
	int level=0;
	ArrayList<BigInteger> diff=new ArrayList<BigInteger>();
	boolean match_sub=false;
	ArrayList<BigInteger> pre_diff=new ArrayList<BigInteger>();
	int[] match_subseq_res={-1,1};
	int neg_hit=0;
	for(int i=0;i<test.size();i++){
	    diff=test.get(i);
	    level=0;
	    while(diff.size()>=diff_len&&level<=diff_level){
		//		System.out.println(diff);
		if(all_equal(diff)){
		    hit++;
		    res.put(test_ind.get(i),predict(test.get(i),diff.get(0),level));
		    break;
		}
		match_sub=false;
		for(ArrayList<BigInteger> alb:data){
		    match_subseq_res=match_subseq(diff,alb);
		    index=match_subseq_res[0];
		    if(index!=-1){
			match_sub=true;
			//			System.out.println("hit: "+alb.get(index));
			if(match_subseq_res[1]==1) res.put(test_ind.get(i),predict(test.get(i),alb.get(index),level));
			else{
			    res.put(test_ind.get(i),predict(test.get(i),alb.get(index).negate(),level));
			    neg_hit++;
			}
			break;
		    }
		}
		if(match_sub){
		    hit++;
		    break;
		}else{
		    diff=get_diff(diff);
		    level++;
		}
	    }
	}
	System.out.println("hit: "+hit+"; neg_hit: "+neg_hit);
	try{
	    PrintWriter out=new PrintWriter(new FileWriter(new File("brutal_"+"level"+diff_level+"_negate.txt").getAbsoluteFile()));
	    try{
		for(Integer ind:res.keySet()){
		    out.println(ind+"\t"+res.get(ind));
		}
	    }finally{
		out.close();
	    }
	}catch(IOException e){
	    throw new RuntimeException(e);
	}
    }
    BigInteger predict(ArrayList<BigInteger> x, BigInteger y, int l){
	if(l==0) return(y);
	ArrayList<BigInteger> last_diff=new ArrayList<BigInteger>();
	BigInteger res=new BigInteger(y.toString());
	for(int i=0;i<l;i++){
	    last_diff.add(x.get(x.size()-1));
	    x=get_diff(x);
	}
	for(int i=last_diff.size()-1;i>=0;i--){
	    res=res.add(last_diff.get(i));
	}
	return(res);
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
    int[] match_subseq(ArrayList<BigInteger> x, ArrayList<BigInteger> seq){ // return the index of next integer, otherwise return -1
	int[] res={-1,1}; // the first element is the index and the second is the sign
	if(seq.size()<x.size()) return(res);
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
	    // check whether x matches the negative sequence
	    hit=true;
	    index=0;
	    i=0;
	    while((seq.size()-index)>=x.size()){
		hit=true;
		for(i=0;i<x.size();i++){
		    if(!x.get(i).equals(seq.get(index+i).negate())){
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
	    if((index+x.size())>=seq.size()) return(res);
	    else{
		res[0]=index+x.size();
		res[1]=-1;
		return(res);
	    } 
	}else{
	    res[0]=index+x.size();
	    return(res);
	}
    }
}
