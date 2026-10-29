package info.preva1l.trashcan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionTest {

    @Test
    void acceptsPlusSuffix() {
        Version version = Version.fromString("3.3.2+26.2.762effc");
        assertEquals(3, version.getMajor());
        assertEquals(3, version.getMinor());
        assertEquals(2, version.getPatch());
        assertEquals("26.2.762effc", version.getMetadata());
        assertEquals("3.3.2+26.2.762effc", version.toString());
        assertEquals("3.3.2", version.toStringWithoutMetadata());
    }

    @Test
    void acceptsDashSuffix() {
        Version version = Version.fromString("3.2.2-SNAPSHOT");
        assertEquals("3.2.2", version.toStringWithoutMetadata());
        assertEquals("SNAPSHOT", version.getMetadata());
        assertEquals("3.2.2-SNAPSHOT", version.toString());
    }

    @Test
    void acceptsBuildFormat() {
        Version purpur = Version.fromString("26.2.build.2633");
        assertEquals(26, purpur.getMajor());
        assertEquals(2, purpur.getMinor());
        assertEquals(0, purpur.getPatch());
        assertEquals("26.2.build.2633", purpur.toString());

        Version paper = Version.fromString("26.2.build.134-stable");
        assertEquals(26, paper.getMajor());
        assertEquals("stable", paper.getMetadata());
        assertEquals("26.2.build.134-stable", paper.toString());
    }

    @Test
    void buildNumberBreaksTies() {
        assertTrue(Version.fromString("26.2.build.134").compareTo(Version.fromString("26.2.build.2633")) < 0);
        assertTrue(Version.fromString("26.2.build.2633").compareTo(Version.fromString("26.3")) < 0);
    }

    @Test
    void plainVersionsStillWork() {
        assertEquals("1.21.4", Version.fromString("1.21.4").toString());
        assertEquals("1.21.4", Version.fromString("v1.21.4").toString());
        assertEquals(0, Version.fromString("1.2").compareTo(Version.fromString("1.2.0")));
        assertTrue(Version.fromString("3.3.3").compareTo(Version.fromString("3.3.2+26.2.762effc")) > 0);
        assertEquals(0, Version.fromString("1.2").getPatch());
        assertEquals("hotfix", Version.fromString("1.2.3-hotfix").getMetadata());
        assertEquals("", Version.fromString("1.2.3").getMetadata());
    }

    @Test
    void garbageFallsBackToZero() {
        assertEquals("0.0.0", Version.fromString("not a version").toString());
        assertEquals("0.0.0", Version.fromString("").toString());
    }
}
