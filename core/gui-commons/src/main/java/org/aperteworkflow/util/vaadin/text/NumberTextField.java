package org.aperteworkflow.util.vaadin.text;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import pl.net.bluesoft.util.lang.Strings;

import com.vaadin.data.Property;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.TextField;

public class NumberTextField extends TextField {
	private DecimalFormat decimalFormat;
	private char decimentalSeparator;
	private boolean allowsNegative;
	
	public boolean isAllowsNegative() {
		return allowsNegative;
	}

	public void setAllowsNegative(boolean allowsNegative) {
		this.allowsNegative = allowsNegative;
	}

	public NumberTextField() {
		alignRight();
		attachListeners();
	}

	public NumberTextField(String caption) {
		super(caption);
		alignRight();
		attachListeners();
	}

	private void attachListeners() {
		addListener((TextChangeListener) new ValueHandler());
		addListener((FocusListener) new ValueHandler());
		addListener((ValueChangeListener) new ValueHandler());
		addListener((BlurListener) new ValueHandler());
	}

	public NumberTextField(Property dataSource) {
		super(dataSource);
		alignRight();
	}

	public NumberTextField(String caption, Property dataSource) {
		super(caption, dataSource);
		alignRight();
	}

	public NumberTextField(String caption, String value) {
		super(caption, value);
		alignRight();
	}

	private void alignRight() {
		addStyleName("align-right");
	}

	public void addDoubleValidator(String errorMessage) {
		addValidator(new LocalizedDoubleValidator(errorMessage));
		//getDecimalFormat().setParseBigDecimal(true);
	}

	public void addIntegerValidator(String errorMessage) {
		addValidator(new LocalizedIntegerValidator(errorMessage));
	}

	@Override
	public void setPropertyDataSource(final Property newDataSource) {
		super.setPropertyDataSource(getPropertyFormatter(newDataSource));
	}

    protected PropertyFormatter getPropertyFormatter(Property newDataSource) {
        return new PropertyFormatter(newDataSource) {
            @Override
            public String format(Object value) 
            {
            	return getDecimalFormat().format(value);
            }

            @Override
            public Object parse(String formattedValue) throws Exception 
            {
            	if(formattedValue == null)
            		return 0;
            	
                return getDecimalFormat().parseObject(formattedValue);
            }
        };
    }
    
    @Override
    public Object getValue() 
    {
    	// TODO Auto-generated method stub
    	try 
    	{
    		Object value = super.getValue();
    		if(value == null)
    			return null;
    		
			return getDecimalFormat().parseObject((String)value);
		} 
    	catch (ParseException e) 
    	{
			return null;
		}
    }

	protected String removeAlphaCharacters(String input) 
	{
		String value = cleanSeparators(input);
		
		StringBuilder sb = new StringBuilder();
		boolean containsDigits = false;
		for (int i = 0; i < value.length(); ++i) {
			char c = value.charAt(i);
			if (allowsNegative && sb.length() == 0 && c == '-'){
				sb.append('-');
				containsDigits = true; //no, moze nie do końca zawiera cyfry, ale traktujemy to jako poprawne
			}
			if (Character.isDigit(c) || c == getDecimentalSeparator()) {
				sb.append(c);
				if (Character.isDigit(c)) {
					containsDigits = true;
				}
			}
		}
		return containsDigits ? sb.toString() : getNullRepresentation();
	}

	private class ValueHandler implements ValueChangeListener, FocusListener, BlurListener, TextChangeListener {
		@Override
		public void textChange(TextChangeEvent event) {
			if (getStringValue() != null)
				checkValue(event.getText());
		}

		protected Object getStringValue() {
			return getValue();
		}

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			if (getStringValue() != null)
				checkValue(getStringValue().toString());
		}

		@Override
		public void focus(FocusEvent event) {
			if (getStringValue() != null)
				checkValue(getStringValue().toString());
		}

		@Override
		public void blur(BlurEvent event) {
			if (getStringValue() != null)
				checkValue(getStringValue().toString());
		}

		private void checkValue(String propValue) {
			if (Strings.hasText(propValue)) {
				String value = removeAlphaCharacters(propValue);
				if (!value.equals(propValue)) {
					setValue(value);
				}
			}
		}
	}

	protected String getDecimalFormatString() {
		return "0.##";
	}

	protected DecimalFormat getDecimalFormat() {
		if(decimalFormat == null)
		{
			decimalFormat = getLocale() != null ? (DecimalFormat)NumberFormat.getInstance(getLocale()) : (DecimalFormat)NumberFormat.getInstance();
			decimentalSeparator = decimalFormat.getDecimalFormatSymbols().getDecimalSeparator(); 
		}
		return decimalFormat;
	}

	public void setDecimalFormat(DecimalFormat decimalFormat) {
		this.decimalFormat = decimalFormat;
	}
	
	public void addLocalizedDoubleValidator(String message)
	{
		this.addValidator(new LocalizedDoubleValidator(message));
	}
	
	public void addLocalizedIntegerValidator(String message)
	{
		this.addValidator(new LocalizedIntegerValidator(message));
	}

	private class LocalizedDoubleValidator extends DoubleValidator {
		public LocalizedDoubleValidator(String errorMessage) {
			super(errorMessage);
		}

		@Override
		protected boolean isValidString(String value) {
			try 
			{
				String localizedValue = getDecimalFormat().parseObject(value).toString();
				return super.isValidString(localizedValue);
			} 
			catch (ParseException e) {
				return false;
			}
		}
	}

	private class LocalizedIntegerValidator extends IntegerValidator {
		public LocalizedIntegerValidator(String errorMessage) {
			super(errorMessage);
		}

		@Override
		protected boolean isValidString(String value) {
			try 
			{
				String localizedValue = getDecimalFormat().parseObject(value).toString();
				return super.isValidString(localizedValue);
			} 
			catch (ParseException e) {
				return false;
			}
		}
	}
	
	public char getDecimentalSeparator()
	{
		return decimentalSeparator;
	}
	
	public String cleanSeparators(String input)
	{
		if(input == null)
			return null;
		
		if(getDecimentalSeparator() == ',')
			return input.replace('.', getDecimentalSeparator());
		else
			return input.replace(',', getDecimentalSeparator());	
	}
}
