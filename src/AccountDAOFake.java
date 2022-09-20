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
		
	public boolean isFullFake() {  
		return true;
	}

	public void save(Account member) {
		// implement this method
	}

	public Account findByUserName(String userName)  {
		return null;
		// implement this method
	}
	
	public Set<Account> findAll()  {
		return null;
		// implement this method
	}

	public void delete(Account member) {
		// implement this method
	}

	public void update(Account member) {
		// implement this method
	}

}
