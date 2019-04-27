import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class ServerTest {
	static Server actual;
	static Server expected;
	static ServerGUI gg;

	@BeforeEach
	void setUp() throws Exception {
		actual = new Server(100);
		expected = new Server(100);
	}

	@Test
	void testServerInt() {
		actual = new Server(100);
		expected = new Server(100);
		if(!(actual.equals(expected)))
			fail("the defult constractor dosn't working well");	
	}

	@Test
	void testServerIntServerGUI() {
		actual = new Server(100, gg);
		expected = new Server(100, gg);
		if(!(actual.equals(expected)))
			fail("the defult constractor dosn't working well");	
	}

}
