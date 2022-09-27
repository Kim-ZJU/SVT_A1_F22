import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public abstract class TestSNAbstractGeneric {
	
	protected IAccountDAO accountDAO; 
	protected SocialNetwork sn = new SocialNetwork();
	protected Account m1, m2, m3, m4, m5;
	protected Set<Account> all = new HashSet<Account>();

	@Before
	public void setUp() throws Exception {
		m1 = sn.join("John");
		m2 = sn.join("Hakan");
		m3 = sn.join("Serra");
		m4 = sn.join("Dean");
		m5 = sn.join("Hasan");
		// ... other test accounts/members you need to create for all tests should go here ... 
		
		/* you can set expectation for mock objects here or in the tests
		 * when injected DAO double is a mock, like this...
		 */
		
		/* if (DAOFactory.isMock(... injected IAccountDAO here...)) {
			//
			// accountDAO is a Mockito mock object, so you may set expectations
			// with when-then clauses, etc. 
			//
		*/
		}

	@After
	public void tearDown() throws Exception {
	
	}
	
	/* 
	 * GENERIC TESTS
	 * 
	 * These tests are generic: they should work with a real, mock, or fake DB,
	 * given proper mock and fake DAO implementations. 
	 * They should be independent of the IAccountDAO implementation injected into
	 * the SocialNetwork.
	 * 
	 */
	
	@Test
	public void canJoinSocialNetwork() throws UserExistsException {
		Account newMember = sn.join("Gloria");
		assertEquals("Gloria", newMember.getUserName());
	}

	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeUsingSocialNetwork()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.hasMember("Jane");
	}

	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeListingMembers()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.listMembers();
	}

	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeSendingFriendshipTo()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.sendFriendshipTo("Jane");
	}
	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeAcceptingFriendshipFrom()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.acceptFriendshipFrom("Jane");
	}
	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeRejectingFriendshipFrom()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.rejectFriendshipFrom("Jane");
	}

	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeBlocking()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.block("Jane");
	}

	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeSendingFriendshipCancellationTo()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.sendFriendshipCancellationTo("Jane");
	}

	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeSettingAutoAcceptFriendships()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.autoAcceptFriendships();
	}

	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeCancelingAutoAcceptFriendships()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.cancelAutoAcceptFriendships();
	}

	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeLeaving()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.leave();
	}

	@Test(expected = NoUserLoggedInException.class)
	public void mustLoginBeforeRecommendFriends()
			throws NoUserLoggedInException, UserNotFoundException {
		sn.recommendFriends();
	}

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
		m3 = sn.login(m3);
		assertTrue(m3.getIncomingRequests().isEmpty());
		m2 = sn.login(m2);
		sn.sendFriendshipTo(m3.getUserName());
		assertTrue(m2.getOutgoingRequests().contains(m3.getUserName()));
		m3 = sn.login(m3);
		assertTrue(m3.getIncomingRequests().contains(m2.getUserName()));

	}

	@Test
	public void acceptingFriendRequestCreatesFriendship()
			throws UserNotFoundException, NoUserLoggedInException {
		m2 = sn.login(m2);
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

	// @Test(expected = UserNotFoundException.class)
	// public void canNotRecommendFriendsWhenNoUserFound()
	// 	throws UserNotFoundException, NoUserLoggedInException {
	// 	// TODO
	// }

	// @Test(expected = UserNotFoundException.class)
	// public void canNotAcceptFriendshipFromWhenNoUserFound()
	// 	throws UserNotFoundException, NoUserLoggedInException {
	// 		// TODO

	// }

	// @Test(expected = UserNotFoundException.class)
	// public void canNotRejectFriendshipFromWhenNoUserFound()
	// 	throws UserNotFoundException, NoUserLoggedInException {
	// 		// TODO

	// }

	// @Test(expected = UserNotFoundException.class)
	// public void canNotBlockWhenNoUserFound()
	// 	throws UserNotFoundException, NoUserLoggedInException {
	// 		// TODO

	// }

	@Test(expected = UserExistsException.class)
	public void canNotJoinSocialNetworkAgain() throws UserExistsException {
		sn.join("Hakan");
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
		// might have to do additional checking if using a Mockito mock
		sn.login(m3);
		assertFalse(sn.hasMember(m2.getUserName()));
		assertEquals(0, m2.getIncomingRequests().size());
		assertEquals(0, m2.getOutgoingRequests().size());
	}	
	
    @Test
	public void rejectingAllFriendRequests() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m1);//John
		sn.sendFriendshipTo("Serra");
		sn.login(m2);//Hakan
		sn.sendFriendshipTo("Serra");
		sn.login(m3);//Serra
		sn.rejectAllFriendships();
		assertEquals(0, m3.getIncomingRequests().size());
	}

    @Test
	public void acceptingAllFriendRequests() throws UserNotFoundException,
			NoUserLoggedInException {
		m1 = sn.login(m1);//John
		sn.sendFriendshipTo("Serra");
		m2 = sn.login(m2);//Hakan
		sn.sendFriendshipTo("Serra");
		m3 = sn.login(m3);//Serra
		sn.acceptAllFriendships();
		assertEquals(0, m3.getIncomingRequests().size());
		assertEquals(2, m3.getFriends().size());
		assertTrue(m3.getFriends().contains("John"));
		assertTrue(m3.getFriends().contains("Hakan"));
	}

    @Test
	public void sendFriendshipCancellationTo() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m1);//John
		sn.sendFriendshipTo("Serra");//m3
		sn.sendFriendshipCancellationTo("Serra");
		assertEquals(0, m3.getIncomingRequests().size());
		assertEquals(0, m1.getOutgoingRequests().size());
	}
	
	@Test
	public void unblock() throws UserNotFoundException,
			NoUserLoggedInException {
		m1 = sn.login(m1);
		sn.block("Hakan");//m2
		sn.unblock("Hakan");
		assertTrue(sn.listMembers().contains("Hakan"));
		sn.sendFriendshipTo("Hakan");
		m2 = sn.login(m2);
		assertEquals(1, m1.getOutgoingRequests().size());
		assertEquals(1, m2.getIncomingRequests().size());
	}

	@Test
	public void autoAcceptFriendships() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m3);
		sn.autoAcceptFriendships();
		sn.login(m1);//John
		sn.sendFriendshipTo("Serra");//m3
		sn.login(m2);//Hakan
		sn.sendFriendshipTo("Serra");//m3
		m3 = sn.login(m3);
		assertEquals(2, m3.getFriends().size());
	}

	@Test
	public void cancelAutoAcceptFriendships() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m3);
		sn.autoAcceptFriendships();
		sn.login(m1);//John
		sn.sendFriendshipTo("Serra");//m3
		sn.login(m3);
		sn.cancelAutoAcceptFriendships();
		sn.login(m2);//Hakan
		sn.sendFriendshipTo("Serra");//m3
		m3 = sn.login(m3);
		assertEquals(1, m3.getFriends().size());
		assertTrue(m3.getFriends().contains("John"));
	}

	@Test(expected = NoUserLoggedInException.class)
	public void logout() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m3);
		sn.logout();
		sn.sendFriendshipTo("Hakan");
	}

	@Test(expected = UserNotFoundException.class)
	public void loginCanHandleNullInput() throws UserNotFoundException,
			NoUserLoggedInException {
		assertNull(sn.login(null));	
	}

	@Test(expected = UserNotFoundException.class)
	public void loginCanHandleNonExistUsername() throws UserNotFoundException,
			NoUserLoggedInException {
		Account anonymous = new Account();
		anonymous.setUserName("Anonymous");
		assertNull(sn.login(anonymous));
	}

	@Test
	public void recommendFriendsAlreadyFriends() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m1);
		sn.sendFriendshipTo(m2.getUserName());
		sn.sendFriendshipTo(m3.getUserName());
		sn.sendFriendshipTo(m4.getUserName());
		sn.login(m2);
		sn.acceptFriendshipFrom(m1.getUserName());
		sn.sendFriendshipTo(m4.getUserName());
		sn.login(m3);
		sn.acceptFriendshipFrom(m1.getUserName());
		sn.sendFriendshipTo(m4.getUserName());
		sn.login(m4);
		sn.acceptFriendshipFrom(m1.getUserName());
		sn.acceptFriendshipFrom(m2.getUserName());
		sn.acceptFriendshipFrom(m3.getUserName());
		sn.login(m1);
		Set<String> recommendations = sn.recommendFriends();
		assertFalse(recommendations.contains(m4.getUserName()));
		assertFalse(recommendations.contains(m3.getUserName()));
		assertFalse(recommendations.contains(m1.getUserName()));
		assertEquals(0, recommendations.size());
	}

	@Test
	public void recommendFriendsBlockedAccount() throws UserNotFoundException,
			NoUserLoggedInException {
		sn.login(m1);
		sn.sendFriendshipTo(m2.getUserName());
		sn.sendFriendshipTo(m3.getUserName());
		sn.block(m4.getUserName());
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
		assertFalse(recommendations.contains(m4.getUserName()));
		assertFalse(recommendations.contains(m3.getUserName()));
		assertFalse(recommendations.contains(m1.getUserName()));
		assertEquals(0, recommendations.size());
	}


	
	/*
	 * The rest are auxiliary tests. 
	 * They make sure that Account objects are cloneable and Account equality works as expected.
	 */
	
	@Test
	
	public void setEqualityWorksAsExpected() {
		Set<String> s1 = new HashSet<String>();
		Set<String> s2 = new HashSet<String>();
		s1.add("abc");
		s1.add("def");
		s2.add("def");
		s2.add("abc");
		assertEquals(s1, s2);
	}
	
	public void accountEqualityWorksAsExpected() {
		Account m6 = m2;
		assertEquals(m2, m6);
		assertEquals(m2, m2.clone());
		//assertNotEquals(m1, m2); // assertNotEquals doesn't work in webcat, replace with assertFalse
		assertFalse(m2.equals(m3));
	}
	
	@Test public void canCloneAccount() {
		Account orig = m2;
		Account initCopy = orig.clone();
		m2.requestFriendship(m5);
		m2.autoAcceptFriendships();
		m2.requestFriendship(m3);
		m4.requestFriendship(m2);
		m2.block(m1);	
		Account clone = m2.clone();
		assertTrue(clone.equals(orig));
		// assertNotEquals(initCopy, clone); // assertNotEquals doesn't work in webcat, replace with assertFalse
		assertFalse(initCopy.equals(m3));
		assertTrue(orig.getFriends().contains(m3.getUserName()));
		assertTrue(clone.getFriends().contains(m3.getUserName()));
		assertTrue(clone.getOutgoingRequests().contains(m4.getUserName()));
		assertTrue(clone.blockedMembers().contains(m1.getUserName()));
		assertTrue(clone.getIncomingRequests().contains(m5.getUserName()));
	}

}
