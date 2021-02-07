package adt;

import java.util.concurrent.atomic.AtomicInteger;

public interface ICloneStringable {
	void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all);
	void setCloneString(String[] cloneString, AtomicInteger fromIndex);
}
