package beans.parameter;

public class ParameterEstimation_UpdateInformationBeans {
	private String parameterId;
	private double startValue;
	private double updatedValue;
	private String unit;
	private boolean global;
	private double lower;
	private double upper;
	public String getParameterId() {
		return parameterId;
	}
	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}
	public double getStartValue() {
		return startValue;
	}
	public void setStartValue(double startValue) {
		this.startValue = startValue;
	}
	public double getUpdatedValue() {
		return updatedValue;
	}
	public void setUpdatedValue(double updatedValue) {
		this.updatedValue = updatedValue;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public boolean isGlobal() {
		return global;
	}
	public void setGlobal(boolean global) {
		this.global = global;
	}
	public double getLower() {
		return lower;
	}
	public void setLower(double lower) {
		this.lower = lower;
	}
	public double getUpper() {
		return upper;
	}
	public void setUpper(double upper) {
		this.upper = upper;
	}
	
}
