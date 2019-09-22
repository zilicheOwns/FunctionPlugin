package ziliche.top.function;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testRegex() {
        String pattern = "((http[s]?|ftp)://)?([a-zA-Z0-9]+)(.yangkeduo.com|.pinduoduo.com)(:\\d+)?([a-zA-Z0-9/.\\-]+)?(/|\\?)?([a-zA-Z0-9.\\-~!@#$%&amp;*+?:;_/=&lt;&gt;]*)?";

        String s = "http://t00img.yangkeduo.com/goods/images/2019-08-18/2f3a5a8b-654e-4697-a228-ab2c9a83b46c.jpg?imageMogr2/strip%7CimageView2/2/w/1300/q/80";
        String s2 = "http://images.pinduoduo.com/mrk/2019-09-17/7b32934e-4041-4850-8a9e-368de33b9d62.jpeg";

        System.out.println(Pattern.matches(pattern, s));


    }

}