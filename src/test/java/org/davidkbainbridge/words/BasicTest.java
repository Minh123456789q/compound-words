package org.davidkbainbridge.words;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

public class BasicTest {

	@Test
	public void Test1() {
		Words words = new Words();

		try (InputStream source = ClassLoader
				.getSystemResourceAsStream("sample1.txt")) {
			words.load(new InputStreamReader(source));
		} catch (Exception e) {
			Assert.fail(String.format("%s : %s", e.getClass().getName(),
					e.getMessage()));
		}
		FindCompoundWordApp app = new FindCompoundWordApp(words, 1);
		app.run();
		Result result = app.getResult();

		Assert.assertEquals(3, result.getCompoundWordCount());
		Assert.assertEquals(1, result.getLongestCompoundWords().size());
		Assert.assertEquals("ratcatdogcat", result.getLongestCompoundWords()
				.toArray()[0]);
	}

	@Test
	public void Test2() {
		Words words = new Words();

		try (InputStream source = ClassLoader
				.getSystemResourceAsStream("sample2.txt")) {
			words.load(new InputStreamReader(source));
		} catch (Exception e) {
			Assert.fail(String.format("%s : %s", e.getClass().getName(),
					e.getMessage()));
		}
		FindCompoundWordApp app = new FindCompoundWordApp(words, 1);
		app.run();
		Result result = app.getResult();

		Assert.assertEquals(1, result.getCompoundWordCount());
		Assert.assertEquals(1, result.getLongestCompoundWords().size());
		Assert.assertEquals("catdogsrat", result.getLongestCompoundWords()
				.toArray()[0]);
	}

	@Test
	public void Test3() {
		Words words = new Words();

		try (InputStream source = ClassLoader
				.getSystemResourceAsStream("sample3.txt")) {
			words.load(new InputStreamReader(source));
		} catch (Exception e) {
			Assert.fail(String.format("%s : %s", e.getClass().getName(),
					e.getMessage()));
		}
		FindCompoundWordApp app = new FindCompoundWordApp(words, 1);
		app.run();
		Result result = app.getResult();

		Assert.assertEquals(0, result.getCompoundWordCount());
		Assert.assertEquals(0, result.getLongestCompoundWords().size());
	}

	@Test
	public void Test4() {
		Words words = new Words();

		try (InputStream source = ClassLoader
				.getSystemResourceAsStream("sample4.txt")) {
			words.load(new InputStreamReader(source));
		} catch (Exception e) {
			Assert.fail(String.format("%s : %s", e.getClass().getName(),
					e.getMessage()));
		}
		FindCompoundWordApp app = new FindCompoundWordApp(words, 1);
		app.run();
		Result result = app.getResult();

		Assert.assertEquals(3, result.getCompoundWordCount());
		Assert.assertEquals(2, result.getLongestCompoundWords().size());
		
		// The result set should have [ratcatcat, catdograt]
		Assert.assertTrue(result.getLongestCompoundWords().remove("ratcatcat"));
		Assert.assertTrue(result.getLongestCompoundWords().remove("catdograt"));
	}
	
	@Test
	public void Test1_4() {
		Words words = new Words();

		try (InputStream source = ClassLoader
				.getSystemResourceAsStream("sample1.txt")) {
			words.load(new InputStreamReader(source));
		} catch (Exception e) {
			Assert.fail(String.format("%s : %s", e.getClass().getName(),
					e.getMessage()));
		}
		FindCompoundWordApp app = new FindCompoundWordApp(words, 4);
		app.run();
		Result result = app.getResult();

		Assert.assertEquals(3, result.getCompoundWordCount());
		Assert.assertEquals(1, result.getLongestCompoundWords().size());
		Assert.assertEquals("ratcatdogcat", result.getLongestCompoundWords()
				.toArray()[0]);
	}

	@Test
	public void Test2_4() {
		Words words = new Words();

		try (InputStream source = ClassLoader
				.getSystemResourceAsStream("sample2.txt")) {
			words.load(new InputStreamReader(source));
		} catch (Exception e) {
			Assert.fail(String.format("%s : %s", e.getClass().getName(),
					e.getMessage()));
		}
		FindCompoundWordApp app = new FindCompoundWordApp(words, 4);
		app.run();
		Result result = app.getResult();

		Assert.assertEquals(1, result.getCompoundWordCount());
		Assert.assertEquals(1, result.getLongestCompoundWords().size());
		Assert.assertEquals("catdogsrat", result.getLongestCompoundWords()
				.toArray()[0]);
	}

	@Test
	public void Test3_4() {
		Words words = new Words();

		try (InputStream source = ClassLoader
				.getSystemResourceAsStream("sample3.txt")) {
			words.load(new InputStreamReader(source));
		} catch (Exception e) {
			Assert.fail(String.format("%s : %s", e.getClass().getName(),
					e.getMessage()));
		}
		FindCompoundWordApp app = new FindCompoundWordApp(words, 4);
		app.run();
		Result result = app.getResult();

		Assert.assertEquals(0, result.getCompoundWordCount());
		Assert.assertEquals(0, result.getLongestCompoundWords().size());
	}

	@Test
	public void Test4__4() {
		Words words = new Words();

		try (InputStream source = ClassLoader
				.getSystemResourceAsStream("sample4.txt")) {
			words.load(new InputStreamReader(source));
		} catch (Exception e) {
			Assert.fail(String.format("%s : %s", e.getClass().getName(),
					e.getMessage()));
		}
		FindCompoundWordApp app = new FindCompoundWordApp(words, 4);
		app.run();
		Result result = app.getResult();

		Assert.assertEquals(3, result.getCompoundWordCount());
		Assert.assertEquals(2, result.getLongestCompoundWords().size());
		
		// The result set should have [ratcatcat, catdograt]
		Assert.assertTrue(result.getLongestCompoundWords().remove("ratcatcat"));
		Assert.assertTrue(result.getLongestCompoundWords().remove("catdograt"));
	}
}
