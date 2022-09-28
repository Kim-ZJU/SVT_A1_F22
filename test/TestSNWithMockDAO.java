import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;


public class TestSNWithMockDAO extends TestSNAbstractGeneric {

	protected DAOFactory daoFactory;

	@Override @Before
	public void setUp() throws Exception {
		// whatever you need to do here
		super.setUp();
		daoFactory = DAOFactory.getInstance();
		accountDAO = daoFactory.getAccountDAOMock();
		sn = new SocialNetwork(accountDAO);
		m1 = sn.join("John");
		m2 = sn.join("Hakan");
		m3 = sn.join("Serra");
		m4 = sn.join("Dean");
		m5 = sn.join("Hasan");
		all.add(m1);
		all.add(m2);
		all.add(m3);
		all.add(m4);
		all.add(m5);
		if (DAOFactory.isMock(accountDAO)) {
			when(accountDAO.findByUserName("John")).thenReturn(m1);
			when(accountDAO.findByUserName("Hakan")).thenReturn(m2);
			when(accountDAO.findByUserName("Serra")).thenReturn(m3);
			when(accountDAO.findByUserName("Dean")).thenReturn(m4);
			when(accountDAO.findByUserName("Hasan")).thenReturn(m5);
			when(accountDAO.findAll()).thenReturn(all);
		}
	}
	
	/* 
	 * Generic tests are automatically inherited from abstract superclass - they should continue to work here! 
	 */

    @Test
	public void canLeaveSocialNetwork() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m2);
		sn.leave();
		if (DAOFactory.isMock(accountDAO)) {
			all.remove(m2);
			when(accountDAO.findByUserName("Hakan")).thenReturn(null);
		}
		// might have to do additional checking if using a Mockito mock
		sn.login(m3);
		assertFalse(sn.hasMember(m2.getUserName()));
	}	

	/* 
	 * VERIFICATION TESTS
	 * 
	 * These tests use a mock and verify that persistence operations are called. 
	 * They ONLY ensure that the right persistence operations of the mocked IAccountDAO implementation are called with
	 * the right parameters. They need not and cannot verify that the underlying DB is actually updated. 
	 * They don't verify the state of the SocialNetwork either. 
	 * 
	 */
	
	@Test public void willAttemptToPersistANewAccount() throws UserExistsException {
		// make sure that when a new member account is created, it will be persisted
		verify(accountDAO, times(5)).save(any(Account.class));
	}
	
	@Test public void willAttemptToPersistSendingAFriendRequest() 
        throws UserNotFoundException, UserExistsException, NoUserLoggedInException {
		// make sure that when a logged-in member issues a friend request, any changes to the affected accounts will be persisted
		sn.login(m1);
		sn.sendFriendshipTo("Hakan");
		verify(accountDAO, times(1)).update(eq(m1));
		verify(accountDAO, times(1)).update(eq(m2));
	}
	
	@Test public void willAttemptToPersistAcceptanceOfFriendRequest() 
        throws UserNotFoundException, UserExistsException, NoUserLoggedInException {
		// make sure that when a logged-in member issues a friend request, any changes to the affected accounts will be persisted
		sn.login(m1);
		sn.sendFriendshipTo("Hakan");
		sn.login(m2);
		sn.acceptFriendshipFrom("John");
		verify(accountDAO, times(2)).update(eq(m1));
		verify(accountDAO, times(2)).update(eq(m2));
	}
	
	@Test public void willAttemptToPersistRejectionOfFriendRequest() 
        throws UserNotFoundException, UserExistsException, NoUserLoggedInException {
		// make sure that when a logged-in member rejects a friend request, any changes to the affected accounts will be persisted
		sn.login(m1);
		sn.sendFriendshipTo("Hakan");
		sn.login(m2);
		sn.rejectFriendshipFrom("John");
		verify(accountDAO, times(2)).update(eq(m1));
		verify(accountDAO, times(2)).update(eq(m2));
	}
	
	@Test public void willAttemptToPersistBlockingAMember() 
        throws UserNotFoundException, UserExistsException, NoUserLoggedInException {
		// make sure that when a logged-in member blocks another member, any changes to the affected accounts will be persisted
		sn.login(m1);
		sn.block("Hakan");
		verify(accountDAO, times(1)).update(eq(m1));
		verify(accountDAO, times(1)).update(eq(m2));
	}
		
	@Test public void willAttemptToPersistLeavingSocialNetwork() 
        throws UserExistsException, UserNotFoundException, NoUserLoggedInException {
		// make sure that when a logged-in member leaves the social network, his account will be permanenlty deleted and  
		// any changes to the affected accounts will be persisted
		sn.login(m1);
		sn.sendFriendshipTo("Hakan");
		sn.sendFriendshipTo("Serra");
		sn.login(m2);
		sn.acceptFriendshipFrom("John");
		sn.login(m1);
		sn.leave();
		verify(accountDAO, times(1)).delete(eq(m1));
		verify(accountDAO, times(3)).update(eq(m2)); //m1 request, accept m1, m1 leave
		verify(accountDAO, times(2)).update(eq(m3)); //m1 request, m1 leave
	}
}
