/*
 * Copyright 2010 Pablo Arrighi, Alex Concha, Miguel Lezama for version 1.
 * Copyright 2013 Pablo Arrighi, Miguel Lezama, Kevin Mazet for version 2.    
 *
 * This file is part of GOOL.
 *
 * GOOL is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, version 3.
 *
 * GOOL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License along with GOOL,
 * in the file COPYING.txt.  If not, see <http://www.gnu.org/licenses/>.
 */

package gool.test;

import gool.Settings;
import gool.generator.common.Platform;
import gool.generator.cpp.CppPlatform;
import gool.generator.csharp.CSharpPlatform;
import gool.generator.java.JavaPlatform;
import gool.generator.python.PythonPlatform;
import gool.generator.objc.ObjcPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GoolTestMonteCarlo {

	/*
	 * At this day, the GOOL system supports 6 output languages that are
	 * symbolized by Platforms. You may comment/uncomment these platforms to
	 * enable/disable tests for the corresponding output language.
	 * 
	 * You may also add your own tests by creating a new method within this
	 * class preceded by a @Test annotation.
	 */
	private List<Platform> platforms = Arrays.asList(

			(Platform) JavaPlatform.getInstance(),
			(Platform) CSharpPlatform.getInstance(),
			(Platform) CppPlatform.getInstance(),
			(Platform) PythonPlatform.getInstance()// ,
//			 (Platform) ObjcPlatform.getInstance()

			);

	private static class GoolTestExecutor {
		private static final String CLEAN_UP_REGEX = "Note:.*?[\r\n]|(\\w+>\\s)|[\\r\\n]+";
		private String input;
		private String expected;
		private List<Platform> testedPlatforms;
		private List<Platform> excludedPlatforms;

		public GoolTestExecutor(String input, String expected,
				List<Platform> testedPlatforms, List<Platform> excludedPlatforms) {
			this.input = input;
			this.expected = expected;
			this.testedPlatforms = testedPlatforms;
			this.excludedPlatforms = excludedPlatforms;
		}

		public void compare(Platform platform) throws Exception {
//			if (excludedPlatforms.contains(platform)) {
//				String errorMsg = "The following target platform(s) have been excluded for this test: ";
//				for (Platform p : excludedPlatforms)
//					if (testedPlatforms.contains(p))
//						errorMsg += p + " ";
//				Assert.fail(errorMsg
//						+ "\nThis test may contain some patterns that are not supported by GOOL at the moment for these target platforms. You may see the GOOL wiki for further documentation.");
//			}
			if (excludedPlatforms.contains(platform)){
				System.err.println("The following target platform(s) have been "
						+ "excluded for this test:" + platform.getName());
				return;
			}
			String expect = this.expected;
			if (platform instanceof CppPlatform){// C++ does not have booleans
				expect = expect.replaceAll("True", "1");
				expect = expect.replaceAll("False", "0");
			}

			String result = compileAndRun(platform);
			// The following instruction is used to remove some logging data
			// at the beginning of the result string
			if (platform == ObjcPlatform.getInstance()
					&& result.indexOf("] ") != -1)
				result = result.substring(result.indexOf("] ") + 2);

			Assert.assertEquals(String.format("The platform %s", platform),
					expect, result);
		}

		protected String compileAndRun(Platform platform) throws Exception {
			String cleanOutput = cleanOutput(TestHelperJava.generateCompileRun(
					platform, input, MAIN_CLASS_NAME));
			return cleanOutput;
		}

		private static String cleanOutput(String result) {
			return result.replaceAll(CLEAN_UP_REGEX, "").trim();
		}
	}

	private static final String MAIN_CLASS_NAME = "Test";

	private List<Platform> testNotImplementedOnPlatforms = new ArrayList<Platform>();

	private void excludePlatformForThisTest(Platform platform) {
		testNotImplementedOnPlatforms.add(platform);
	}

	
	@BeforeClass
	public static void init() {
	}

	
	@Test
	public void monteCarlo() throws Exception {
		String input = TestHelperJava.surroundWithClass("public double MonteCarlo(){double x = 0.0;"
				+ "double y = 0.0;"
				+ "int tiragesDansLeDisque = 0;"
				+ "for (int i = 1; i<100000; i++){"
				+ "x = Math.random();"
				+ "y = Math.random();"
				+ "if (x*x + y*y <= 1){tiragesDansLeDisque++;}}"
				+ "double estimation = 4.0 * tiragesDansLeDisque / 100000;"
				+ "return estimation;}"
				+ "public static void main(String[] args){"
				+ "Test t = new Test();"
				+ "double diff = Math.ceil(Math.abs(t.MonteCarlo() - Math.PI));"
				+ "System.out.println( diff == 1.0);}",
				MAIN_CLASS_NAME, "");
				
		String expected = "true";
		excludePlatformForThisTest((Platform) CSharpPlatform.getInstance());
		excludePlatformForThisTest((Platform) CppPlatform.getInstance());
		excludePlatformForThisTest((Platform) PythonPlatform.getInstance());
		compareResultsDifferentPlatforms(input, expected);
	}

	
	private void compareResultsDifferentPlatforms(String input, String expected)
			throws Exception {
		compareResultsDifferentPlatforms(new GoolTestExecutor(input, expected,
				platforms, testNotImplementedOnPlatforms));
		this.testNotImplementedOnPlatforms = new ArrayList<Platform>();
	}

	private void compareResultsDifferentPlatforms(GoolTestExecutor executor)
			throws Exception {
		for (Platform platform : platforms) {
			executor.compare(platform);
		}
	}
}
