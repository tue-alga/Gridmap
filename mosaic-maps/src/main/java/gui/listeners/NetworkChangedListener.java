package gui.listeners;

/**
 * Listener that can be used to update objects/parts of the GUI that interact with
 * the dual of a network.
 */
public abstract class NetworkChangedListener {

	/**
	 * Fired in case the dual graph changes. The dual may be {@code null}.
	 * @param dual New dual.
	 */
	public abstract void networkChanged();
}
