/**
 * 
 */
package chargecar.policies;


/**
 * @author astyler
 * DO NOT EDIT
 */
public class PolicyFactory {
	private static NaiveBufferPolicy nbPolicy = new NaiveBufferPolicy();
	private static NoCapPolicy ncPolicy = new NoCapPolicy();
	private static UserPolicy uPolicy = new UserPolicy();

	public static Policy getNaiveBufferPolicy(){
		return nbPolicy;
	}
	public static Policy getNoCapPolicy(){
		return ncPolicy;
	}
	public static Policy getUserPolicy(){
		return uPolicy;
	}
}
