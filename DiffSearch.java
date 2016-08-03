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
     public DiffSearch(){
	 String s;
	 String[] ss;
	 ArrayList<BigInteger> seq=new ArrayList<BigInteger>();
	 int k=1;
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
			     //			     if(i==0) seq.add(new BigInteger(ss[i].substring(1,ss[i].length())));
			     //			     else if(i==(ss.length-1)) seq.add(new BigInteger(ss[i].substring(0,ss[i].length()-1)));
			     seq.add(new BigInteger(ss[i].trim()));
			 }catch(NumberFormatException e){
			     System.out.println(k+" "+ss.length+" "+ss[0]+" "+test.get(0));
			     throw new RuntimeException(e);
			 }
		     }
		     test.add(seq);
		 }
	     }finally{
		 in.close();
	     }
	 }catch(IOException e){
	     throw new RuntimeException(e);
	 }
	 System.out.println("k: "+k+"; test size:  "+test.size()+" "+test.get(10));
     }
}
