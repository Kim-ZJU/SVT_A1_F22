import java.util.HashSet;
import java.util.Set;



public class AccountDAOFake implements IAccountDAO {
	/*
	 * A full in-memory fake of AccountDAO.
	 * 
	 * IMPORTANT NOTE:
	 * If you take advantage of the Account class for simulating an in-memory database,
	 * make sure that your storage representation relies on fully cloned objects, not just Account references passed.
	 * Tests should not be able to distinguish this object from a real DAO object connected to a real DB. 
	 * In this sense, this class is a full fake that works with all inputs. 
	 */

	private Set<Account> accounts = new HashSet<Account>();

	public boolean isFullFake() {  
		return true;
	}

	public void save(Account member) {
		// implement this method
		accounts.add(member.clone());
	}

	public Account findByUserName(String userName)  {
		// implement this method
		for (Account each : accounts) {
			if (each.getUserName().equals(userName)) {
				return each;
			}
		}
		return null;
	}
	
	public Set<Account> findAll()  {
		// implement this method
		return accounts;
	}

	public void delete(Account member) {
		// implement this method
		for (Account each : accounts) {
			if (each.getUserName().equals(member.getUserName())) {
				accounts.remove(each);
				break;
			}
		}
	}

	public void update(Account member) {
		// implement this method
        Account toRemove = null;
		for (Account each : accounts) {
			if (each.getUserName().equals(member.getUserName())) {
				toRemove = each;
				break;
			}
		}
		accounts.remove(toRemove);
		accounts.add(member.clone());
	}

}
