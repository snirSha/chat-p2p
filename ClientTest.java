import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientTest {
	static Client actual;
	static Client expected;
	static ClientGUI cg; 

	@BeforeEach
	void setUp() throws Exception {
		actual = new Client("local", 100, "yarden");
		expected = new Client("local", 100, "yarden");
	}

	@Test
	void testClientStringIntString() {
		actual = new Client("local", 100, "yarden");
		expected = new Client("local", 100, "yarden");
		if(!(actual.equals(expected)))
			fail("the defult constractor dosn't working well");	
	}

	@Test
	void testClientStringIntStringClientGUI() {
		actual = new Client("local", 100, "yarden", cg);
		expected = new Client("local", 100, "yarden", cg);
		if(!(actual.equals(expected)))
			fail("the defult constractor dosn't working well");	
	}


}
