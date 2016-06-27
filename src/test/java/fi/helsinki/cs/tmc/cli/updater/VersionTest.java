package fi.helsinki.cs.tmc.cli.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionTest {

    @Test
    public void constructorWorksWithNumbering() {
        Version ver = new Version(44, 33, 22, "alpha1");
        assertEquals(44, ver.getMajor());
        assertEquals(33, ver.getMinor());
        assertEquals(22, ver.getPatch());
        assertEquals("alpha1", ver.getMetadata());
    }

    @Test
    public void constructorWorksWithStringNoMeta() {
        Version ver = new Version("44.33.22");
        assertEquals(44, ver.getMajor());
        assertEquals(33, ver.getMinor());
        assertEquals(22, ver.getPatch());
        assertEquals(null, ver.getMetadata());
    }

    @Test
    public void constructorWorksWithStringMeta() {
        Version ver = new Version("44.33.22-alpha1.rc1+1234.56");
        assertEquals(44, ver.getMajor());
        assertEquals(33, ver.getMinor());
        assertEquals(22, ver.getPatch());
        assertEquals("alpha1.rc1+1234.56", ver.getMetadata());
    }

    @Test
    public void toStringWorksWithoutMetadata() {
        Version ver = new Version(44, 33, 22);
        assertEquals(44, ver.getMajor());
        assertEquals(33, ver.getMinor());
        assertEquals(22, ver.getPatch());
        assertEquals("44.33.22", ver.toString());
    }

    @Test
    public void toStringWorksWithMetadata() {
        Version ver = new Version(44, 33, 22, "alpha1");
        assertEquals(44, ver.getMajor());
        assertEquals(33, ver.getMinor());
        assertEquals(22, ver.getPatch());
        assertEquals("44.33.22-alpha1", ver.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsException1() {
        assertNotNull(new Version("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsException2() {
        assertNotNull(new Version("0.1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsException3() {
        assertNotNull(new Version("0.0.0.1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsException4() {
        assertNotNull(new Version("0.0.1+meta"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsException5() {
        assertNotNull(new Version("v0.0.1"));
    }

    @Test
    public void compareToWorksByMajor() {
        Version ver1 = new Version(0, 9, 9);
        Version ver2 = new Version(1, 0, 0);
        assertTrue(ver2.compareTo(ver1) > 0);
        assertTrue(ver1.compareTo(ver2) < 0);
    }

    @Test
    public void compareToWorksByMinor() {
        Version ver1 = new Version(0, 0, 9);
        Version ver2 = new Version(0, 1, 0);
        assertTrue(ver2.compareTo(ver1) > 0);
        assertTrue(ver1.compareTo(ver2) < 0);
    }

    @Test
    public void compareToWorksByPatch() {
        Version ver1 = new Version(0, 0, 0);
        Version ver2 = new Version(0, 0, 1);
        assertTrue(ver2.compareTo(ver1) > 0);
        assertTrue(ver1.compareTo(ver2) < 0);
    }

    @Test
    public void compareToWorksIfEqual() {
        Version ver1 = new Version(44, 33, 22);
        Version ver2 = new Version(44, 33, 22);
        assertTrue(ver2.compareTo(ver1) == 0);
    }

    @Test
    public void compareToWorksIfEqualButDifferentMeta() {
        Version ver1 = new Version(44, 33, 22, "alpha1");
        Version ver2 = new Version(44, 33, 22, "foobar");
        assertTrue(ver2.compareTo(ver1) == 0);
    }

    @Test
    public void isNewerWorks() {
        Version ver1 = new Version(0, 9, 9);
        Version ver2 = new Version(1, 0, 0);
        Version ver3 = new Version(1, 0, 0);
        assertTrue(ver2.isNewerThan(ver1));
        assertFalse(ver1.isNewerThan(ver2));
        assertFalse(ver2.isNewerThan(ver3));
    }
}
