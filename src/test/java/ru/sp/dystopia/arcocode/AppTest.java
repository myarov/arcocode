package ru.sp.dystopia.arcocode;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import ru.sp.dystopia.arcocode.repoman.GitRepoMan;
/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Тест получения последней ревизии.
     * Тест первый: отсутствие репозитория.
     */
    public void testGitRepoManGetLastRevisionNoRepo()
    {
        String testResult;
        GitRepoMan man=new GitRepoMan();
        testResult = man.getLastRevision();
        assertNull(testResult);
    }
}
