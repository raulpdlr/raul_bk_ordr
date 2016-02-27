package processing;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import processing.diff_match_patch.*;
import stocks.solver.raul.BookOrderSolver;

public class ProcessorTest {

	DecimalFormat formatterCommas = new DecimalFormat("#,###.00");
	Charset charset;
	diff_match_patch diffMatchPatch;
	FileInputStream fileInputStream;
	String outputFileProcessed;
	Path outputFilePath;
	PrintStream outputPrintStream;
	BookOrderSolver bookOrderSolver;
	
	@Before
	public void setUp() throws Exception {
		charset = Charset.forName("utf-8");
		// USE SYSTEM.FILESEPERATOR
		
		fileInputStream = new FileInputStream(new File("src" + File.separator + "test" + File.separator + 
				"resources" + File.separator + "testInput" +  File.separator +"pricer.in"));
		
		outputFileProcessed ="src" + File.separator + "test" + File.separator + 
				"resources" + File.separator + "testOutputProcessed" +  File.separator + "output.txt";

		outputFilePath = Paths.get(outputFileProcessed);
		outputPrintStream = new PrintStream(outputFilePath.toFile());
		
		diffMatchPatch = new diff_match_patch();
	
		bookOrderSolver = new BookOrderSolver();
		
		System.setIn(fileInputStream);
	}

	@After
	public void tearDown() throws Exception {
		if(fileInputStream != null){
			fileInputStream.close();			
		}
	}

	static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
			}
	
	@Test
	public final void testPricer1() throws IOException {
		long startTime = System.nanoTime();
		String target = "1";
		System.out.println("Starting test with target: "+ target);
		
		System.setOut(outputPrintStream);	

		String pricerOutFile = "src" + File.separator + "test" + File.separator + 
				"resources" + File.separator + "testOutputVerified" +  File.separator + "pricer.out.1";
		
		String[] args = new String[1];
		args[0] = target;
		bookOrderSolver.main(args);
		
		String fileA = readFile(outputFileProcessed, Charset.forName("utf-8"));
		String fileB = readFile(pricerOutFile, Charset.forName("utf-8"));
		
		LinkedList<Diff> differences = diffMatchPatch.diff_main(fileA, fileB);
		
		int differences_int = diffMatchPatch.diff_levenshtein(differences);
		if(differences_int != 0){
			fail("Differences found between processed output and known good output.");
		}
		
		Long endTime = System.nanoTime();
		Long difference = endTime - startTime;
		double endTimeDouble = difference.doubleValue();
		
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		System.out.println("No errors in processing, took "+ formatterCommas.format(endTimeDouble) + " ns");		 
	}
	
	@Test
	public final void testPricer200() throws IOException {
		long startTime = System.nanoTime();
		String target = "200";
		System.out.println("Starting test with target: "+ target);
		
		System.setOut(outputPrintStream);	

		String pricerOutFile = "src" + File.separator + "test" + File.separator + 
				"resources" + File.separator + "testOutputVerified" +  File.separator + "pricer.out.200";		
			
		String[] args = new String[1];
		args[0] = target;
		bookOrderSolver.main(args);
		
		String fileA = readFile(outputFileProcessed, Charset.forName("utf-8"));
		String fileB = readFile(pricerOutFile, Charset.forName("utf-8"));
		
		LinkedList<Diff> differences = diffMatchPatch.diff_main(fileA, fileB);
		
		int differences_int = diffMatchPatch.diff_levenshtein(differences);
		if(differences_int != 0){
			fail("Differences found between processed output and known good output.");
		}
		
		Long endTime = System.nanoTime();
		Long difference = endTime - startTime;
		double endTimeDouble = difference.doubleValue();
		
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		System.out.println("No errors in processing, took "+ formatterCommas.format(endTimeDouble) + " ns"); 
	}	
	
	@Test
	public final void testPricer10000() throws IOException {
		long startTime = System.nanoTime();
		String target = "10000";
		System.out.println("Starting test with target: "+ target);
		
		System.setOut(outputPrintStream);	

		BookOrderSolver bookOrderSolver = new BookOrderSolver();
		
		String pricerOutFile = "src" + File.separator + "test" + File.separator + 
				"resources" + File.separator + "testOutputVerified" +  File.separator + "pricer.out.10000";
				
		String[] args = new String[1];
		args[0] = target;
		bookOrderSolver.main(args);
		
		String fileA = readFile(outputFileProcessed, Charset.forName("utf-8"));
		String fileB = readFile(pricerOutFile, Charset.forName("utf-8"));
		
		LinkedList<Diff> differences = diffMatchPatch.diff_main(fileA, fileB);
		
		int differences_int = diffMatchPatch.diff_levenshtein(differences);
		if(differences_int != 0){
			fail("Differences found between processed output and known good output.");
		}
		
		Long endTime = System.nanoTime();
		Long difference = endTime - startTime;
		double endTimeDouble = difference.doubleValue();
		
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		System.out.println("No errors in processing, took "+ formatterCommas.format(endTimeDouble) + " ns");
	}		
	
}
