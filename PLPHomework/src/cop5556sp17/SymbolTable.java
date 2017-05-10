package cop5556sp17;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import cop5556sp17.AST.Dec;


public class SymbolTable {

	class ChainEntry
	{
		int scope;
		Dec declaration;

		ChainEntry(int scope, Dec d)
		{
			this.scope=scope;
			this.declaration=d;
		}

		@Override
		public String toString()
		{
			return declaration+" scope"+scope;
		}

		@Override
		public int hashCode() {

			return (""+scope).hashCode();

		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ChainEntry)) {
				return false;
			}
			ChainEntry other = (ChainEntry) obj;

			if (declaration == null) {
				if (other.declaration != null) {
					return false;
				}
			} else if (!declaration.equals(other.declaration)) {
				return false;
			}
			if (scope != other.scope) {
				return false;
			}
			return true;
		}


	}

	//TODO  add fields
	HashMap <String,LinkedList<ChainEntry>> h;
	ArrayList <Integer> scope_stack;
	int currentscope=0;
	int nextscope=0;


	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		currentscope= nextscope++;
		scope_stack.add(0,currentscope);
	}



	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		scope_stack.remove(0);

		if (scope_stack.size()>0)
		{
			currentscope=scope_stack.get(0);
		}

	}

	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS

		ChainEntry new_entry=new ChainEntry(currentscope, dec);

		if (h.containsKey(ident))
		{
			LinkedList<ChainEntry> temp=h.get(ident);

			for (ChainEntry c:temp)
			{
				if (c.scope==currentscope)
				{
					return false;
				}
				//else if ()

				//if (c.equals(new_entry) && c.scope==currentscope)
				//{
					//return false;
				//}
			}

			temp.addLast(new_entry);
		}
		else
		{
			LinkedList<ChainEntry> temp=new LinkedList<>();
			temp.addLast(new_entry);
			h.put(ident, temp);
		}

		return true;
	}

	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS

		if (h.containsKey(ident))
		{
			LinkedList<ChainEntry> chainlist=h.get(ident);

			for (Integer temp:scope_stack)
			{
				for (ChainEntry c:chainlist)
				{
					if (c.scope==temp)
					{
						return c.declaration;
					}
				}
			}

			return null;

		}
		else
		{
			return null;
		}

	}

	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		h=new HashMap<>();
		scope_stack=new ArrayList<>();
		enterScope();
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		String s="";

		Iterator <Map.Entry<String, LinkedList<ChainEntry>>> it=h.entrySet().iterator();

		while (it.hasNext())
		{
			int count=0;
			LinkedList<ChainEntry> temp=it.next().getValue();

			for (ChainEntry temp2:temp)
			{
				s+=(count++ +")"+temp2.toString());
			}
			s+='\n';
		}

		return s;
	}
}



