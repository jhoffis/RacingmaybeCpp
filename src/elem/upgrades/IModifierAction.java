package elem.upgrades;

import java.io.Serializable;

public interface IModifierAction extends Serializable {
	void modify(RegVals regularValues);
}
