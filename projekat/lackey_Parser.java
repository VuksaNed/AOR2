package rs.ac.bg.etf.parser.CisPenn2011;

import rs.ac.bg.etf.parser.Parser;
import rs.ac.bg.etf.predictor.Instruction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

//https://www.cis.upenn.edu/~milom/cis501-Fall12/traces/trace-format.html
public class lackey_Parser implements Parser {

	private int numOfInstruction;
	private int numOfBr;
	private int numOfCndBr;
	private int numOfBackwardCndBr;
	private int numOfTaken;

	public int getNumOfTaken() {
		return numOfTaken;
	}

	public int getNumOfBackwardCndBr() {
		return numOfBackwardCndBr;
	}

	public int getNumOfInstruction() {
		return numOfInstruction;
	}

	public int getNumOfBr() {
		return numOfBr;
	}

	public int getNumOfCndBr() {
		return numOfCndBr;
	}

	private BufferedReader r;

	public lackey_Parser(String pathToTrace) {
		try {
			r = new BufferedReader(
						new InputStreamReader(new GZIPInputStream(new FileInputStream(pathToTrace)), "US-ASCII"));
                        numOfInstruction=0;
                        numOfBr=0;
                        numOfCndBr=0;
                        numOfBackwardCndBr=0;
                        numOfTaken=0;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Instruction getNext() {
		try {
			String line = r.readLine();
			if (line == null)
				return null;
			String[] tokens = line.split("\\s+");
                        long pc=0;
                        boolean cond=false,outcome=false,isBranch=false;
                        try{
                            pc = Long.parseLong(tokens[1],16);
                            //String conditionRegister = tokens[5];
                            String TNnotBranch = tokens[2];
                            cond = true; //&& conditionRegister.equals("R");
                            outcome = TNnotBranch.contains("T");
                            isBranch = true;
                        }
                        catch(Exception e){
                            //System.err.println(line);         //Uncomment to see output of bad lines in trace
                        }
			//long targetAddressTakenBranch = Long.parseLong(tokens[11], 16);
			//boolean isBackward = pc > targetAddressTakenBranch;

			return new CISPENN2011_Instruction(pc, outcome, isBranch, cond, false);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public void fillStats() {
                /*
		long pc;
		String conditionRegister;
		String TNnotBranch;
		boolean cond;
		boolean outcome;
		long targetAddressTakenBranch;
		boolean isBackward;
		long microOpCount;
		
		try {
			String line = " ";
			while ((line = r.readLine()) != null) {

				String[] tokens = line.split("\\s+");
				
				microOpCount = Long.parseLong(tokens[0]);
				pc = Long.parseLong(tokens[1], 16);
				conditionRegister = tokens[5];
				TNnotBranch = tokens[3];
				cond = !TNnotBranch.equals("-"); //&& conditionRegister.equals("R");
				outcome = TNnotBranch.contains("T");

				targetAddressTakenBranch = Long.parseLong(tokens[11], 16);
				isBackward = pc > targetAddressTakenBranch;
				
				if(microOpCount == 1)
					numOfInstruction++;
				if(targetAddressTakenBranch != 0)
					numOfBr++;
				if(cond)
					numOfCndBr++;
				if(cond && outcome)
					numOfTaken++;
				if(targetAddressTakenBranch != 0 && isBackward)
					numOfBackwardCndBr++;
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

}
