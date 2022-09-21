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
	protected IAccountDAO accountDAO; 
	protected SocialNetwork sn;
	protected Account m1, m2, m3, m4, m5;
	protected Set<Account> all = new HashSet<Account>();

	@Override @Before
	public void setUp() throws Exception {
		// whatever you need to do here
		super.setUp();
		daoFactory = DAOFactory.getInstance();
		accountDAO = daoFactory.getAccountDAOMock();
		sn = new SocialNetwork(accountDAO);
		m1 = sn.join("m1");
		m2 = sn.join("m2");
		m3 = sn.join("m3");
		m4 = sn.join("m4");
		m5 = sn.join("m5");
		all.add(m1);
		all.add(m2);
		all.add(m3);
		all.add(m4);
		all.add(m5);
		if (DAOFactory.isMock(accountDAO)) {
			when(accountDAO.findByUserName("m1")).thenReturn(m1);
			when(accountDAO.findByUserName("m2")).thenReturn(m2);
			when(accountDAO.findByUserName("m3")).thenReturn(m3);
			when(accountDAO.findByUserName("m4")).thenReturn(m4);
			when(accountDAO.findByUserName("m5")).thenReturn(m5);
			when(accountDAO.findAll()).thenReturn(all);
		}
	}
	
	/* 
	 * Generic tests are automatically inherited from abstract superclass - they should continue to work here! 
	 */
	@Test
	public void canLoginAndFindYourself() throws NoUserLoggedInException,
			UserNotFoundException {
		sn.login(m1);
		assertTrue(sn.hasMember(m1.getUserName()));
	}

	@Test
	public void canLoginAndFindOthers() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m1);
		assertTrue(sn.hasMember(m2.getUserName()));
		assertTrue(sn.hasMember(m3.getUserName()));
	}

	@Test
	public void canListMembers() throws NoUserLoggedInException,
			UserNotFoundException {
		sn.login(m1);
		Set<String> members = sn.listMembers();
		assertTrue(members.contains(m2.getUserName()));
		assertTrue(members.contains(m3.getUserName()));
	}

	@Test
	public void sendingFriendRequestCreatesPendingRequestAndResponse()
			throws UserNotFoundException, NoUserLoggedInException {
		assertTrue(m3.getIncomingRequests().isEmpty());
		sn.login(m2);
		sn.sendFriendshipTo(m3.getUserName());
		m2 = sn.login(m2);
		m3 = sn.login(m3);
		assertTrue(m3.getIncomingRequests().contains(m2.getUserName()));
		assertTrue(m2.getOutgoingRequests().contains(m3.getUserName()));
	}

	@Test
	public void acceptingFriendRequestCreatesFriendship()
			throws UserNotFoundException, NoUserLoggedInException {
		sn.login(m2);
		sn.sendFriendshipTo(m3.getUserName());
		m3 = sn.login(m3);
		sn.acceptFriendshipFrom(m2.getUserName());
		m2 = sn.login(m2);
		assertTrue(m3.getFriends().contains(m2.getUserName()));
		assertTrue(m2.getFriends().contains(m3.getUserName()));
	}

	@Test
	public void rejectingFriendRequestClearsPendingRequestAndResponse()
			throws UserNotFoundException, NoUserLoggedInException {
		sn.login(m2);
		sn.sendFriendshipTo(m3.getUserName());
		sn.login(m3);
		sn.rejectFriendshipFrom(m2.getUserName());
		assertFalse(m3.getIncomingRequests().contains(m2.getUserName()));
		assertFalse(m2.getOutgoingRequests().contains(m3.getUserName()));
	}

	@Test(expected = UserNotFoundException.class)
	public void canNotSendAFriendRequestToNonExistingMember()
			throws UserNotFoundException, NoUserLoggedInException {
		sn.login(m2);
		sn.sendFriendshipTo("Anonymous");
	}

	@Test(expected = UserExistsException.class)
	public void canNotJoinSocialNetworkAgain() throws UserExistsException {
		sn.join("m1");
	}

	@Test
	public void blockingAMemberMakesUserInvisibleToHerInHasMember()
			throws UserExistsException, UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m2);
		sn.block(m3.getUserName());
		sn.login(m3);
		assertFalse(sn.hasMember(m2.getUserName()));
	}

	@Test
	public void blockingAMemberMakesUserInvisibleToHerInListMembers()
			throws UserExistsException, UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m2);
		sn.block(m3.getUserName());
		sn.login(m3);
		Set<String> allMembers = sn.listMembers();
		assertFalse(allMembers.contains(m2.getUserName()));
	}

	@Test
	public void recommendMembersReturnsSharedFriendsOfMyFriends()
			throws UserNotFoundException, NoUserLoggedInException {
		sn.login(m1);
		sn.sendFriendshipTo(m2.getUserName());
		sn.sendFriendshipTo(m3.getUserName());
		sn.login(m2);
		sn.acceptFriendshipFrom(m1.getUserName());
		sn.sendFriendshipTo(m4.getUserName());
		sn.login(m3);
		sn.acceptFriendshipFrom(m1.getUserName());
		sn.sendFriendshipTo(m4.getUserName());
		sn.login(m4);
		sn.acceptFriendshipFrom(m2.getUserName());
		sn.acceptFriendshipFrom(m3.getUserName());
		sn.login(m1);
		Set<String> recommendations = sn.recommendFriends();
		assertTrue(recommendations.contains(m4.getUserName()));
		assertFalse(recommendations.contains(m3.getUserName()));
        assertFalse(recommendations.contains(m1.getUserName()));
		assertEquals(1, recommendations.size());
	}

    @Test
	public void canLeaveSocialNetwork() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m2);
		sn.leave();
		if (DAOFactory.isMock(accountDAO)) {
			all.remove(m2);
			when(accountDAO.findByUserName("m2")).thenReturn(null);
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
		sn.sendFriendshipTo("m2");
		verify(accountDAO, times(1)).update(eq(m1));
		verify(accountDAO, times(1)).update(eq(m2));
	}
	
	@Test public void willAttemptToPersistAcceptanceOfFriendRequest() 
        throws UserNotFoundException, UserExistsException, NoUserLoggedInException {
		// make sure that when a logged-in member issues a friend request, any changes to the affected accounts will be persisted
		sn.login(m1);
		sn.sendFriendshipTo("m2");
		sn.login(m2);
		sn.acceptFriendshipFrom("m1");
		verify(accountDAO, times(2)).update(eq(m1));
		verify(accountDAO, times(2)).update(eq(m2));
	}
	
	@Test public void willAttemptToPersistRejectionOfFriendRequest() 
        throws UserNotFoundException, UserExistsException, NoUserLoggedInException {
		// make sure that when a logged-in member rejects a friend request, any changes to the affected accounts will be persisted
		sn.login(m1);
		sn.sendFriendshipTo("m2");
		sn.login(m2);
		sn.rejectFriendshipFrom("m1");
		verify(accountDAO, times(2)).update(eq(m1));
		verify(accountDAO, times(2)).update(eq(m2));
	}
	
	@Test public void willAttemptToPersistBlockingAMember() 
        throws UserNotFoundException, UserExistsException, NoUserLoggedInException {
		// make sure that when a logged-in member blocks another member, any changes to the affected accounts will be persisted
		sn.login(m1);
		sn.block("m2");
		verify(accountDAO, times(1)).update(eq(m1));
		verify(accountDAO, times(1)).update(eq(m2));
	}
		
	@Test public void willAttemptToPersistLeavingSocialNetwork() 
        throws UserExistsException, UserNotFoundException, NoUserLoggedInException {
		// make sure that when a logged-in member leaves the social network, his account will be permanenlty deleted and  
		// any changes to the affected accounts will be persisted
		sn.login(m1);
		sn.sendFriendshipTo("m2");
		sn.sendFriendshipTo("m3");
		sn.login(m2);
		sn.acceptFriendshipFrom("m1");
		sn.login(m1);
		sn.leave();
		verify(accountDAO, times(1)).delete(eq(m1));
		verify(accountDAO, times(3)).update(eq(m2)); //m1 request, accept m1, m1 leave
		verify(accountDAO, times(2)).update(eq(m3)); //m1 request, m1 leave
	}

}
