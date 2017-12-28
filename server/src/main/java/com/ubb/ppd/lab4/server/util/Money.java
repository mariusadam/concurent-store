package com.ubb.ppd.lab4.server.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Currency;

import static java.math.BigDecimal.ZERO;

/**
 * @author Marius Adam
 */
public final class Money implements Comparable<Money>, Serializable {

    private static final int  HASH_SEED        = 23;
    private static final int  HASH_FACTOR      = 37;
    /**
     * Determines if a deserialized file is compatible with this class.
     * <format>
     * Maintainers must change this value if and only if the new version
     * of this class is not compatible with old versions. See Sun docs
     * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html> details. </a>
     * <format>
     * Not necessary to include in first version of the class, but
     * included here as a reminder of its importance.
     */
    private static final long serialVersionUID = 7526471155622776147L;
    /**
     * The default currency to be used if no currency is passed to the constructor.
     */
    private static Currency     DEFAULT_CURRENCY;
    /**
     * The default rounding style to be used if no currency is passed to the constructor.
     * See {@link BigDecimal}.
     */
    private static RoundingMode DEFAULT_ROUNDING;

    static {
        init(Currency.getInstance("EUR"), RoundingMode.HALF_EVEN);
    }

    /**
     * The currency of the money, such as US Dollars or Euros.
     * Never null.
     *
     * @serial
     */
    private final Currency     fCurrency;
    /**
     * The rounding style to be used.
     * See {@link BigDecimal}.
     *
     * @serial
     */
    private final RoundingMode fRounding;
    /**
     * The money amount.
     * Never null.
     *
     * @serial
     */
    private       BigDecimal   fAmount;
    /**
     * @serial
     */
    private       int          fHashCode;

    /**
     * Full constructor.
     *
     * @param aAmount        is required, can be positive or negative. The number of
     *                       decimals in the amount cannot <em>exceed</em> the maximum number of
     *                       decimals for the given {@link Currency}. It's possible to create a
     *                       <tt>Money</tt> object in terms of 'thousands of dollars', for instance.
     *                       Such an amount would have a scale of -3.
     * @param aCurrency      is required.
     * @param aRoundingStyle is required, must match a rounding style used by
     *                       {@link BigDecimal}.
     */
    public Money(BigDecimal aAmount, Currency aCurrency, RoundingMode aRoundingStyle) {
        fAmount = aAmount;
        fCurrency = aCurrency;
        fRounding = aRoundingStyle;
        validateState();
    }

    /**
     * Constructor taking only the money amount.
     * <format>
     * <P>The currency and rounding style both take default values.
     *
     * @param aAmount is required, can be positive or negative.
     */
    public Money(BigDecimal aAmount) {
        this(aAmount, DEFAULT_CURRENCY, DEFAULT_ROUNDING);
    }

    public Money(String amount) {
        this(new BigDecimal(amount));
    }

    /**
     * Constructor taking the money amount and currency.
     * <format>
     * <P>The rounding style takes a default value.
     *
     * @param aAmount   is required, can be positive or negative.
     * @param aCurrency is required.
     */
    public Money(BigDecimal aAmount, Currency aCurrency) {
        this(aAmount, aCurrency, DEFAULT_ROUNDING);
    }

    public Money(String amount, Currency currency) {
        this(new BigDecimal(amount), currency);
    }

    /**
     * Set default values for currency and rounding style.
     * <format>
     * <em>Your application must call this method upon startup</em>.
     * This method should usually be called only once (upon startup).
     * <format>
     * <P>The recommended rounding style is {@link RoundingMode#HALF_EVEN}, also called
     * <em>banker's rounding</em>; this rounding style introduces the least bias.
     * <format>
     * <P>Setting these defaults allow you to use the more terse constructors of this class,
     * which are much more convenient.
     * <format>
     * <P>(In a servlet environment, each app has its own classloader. Calling this
     * method in one app will never affect the operation of a second app running in the same
     * servlet container. They are independent.)
     */
    public static void init(Currency aDefaultCurrency, RoundingMode aDefaultRounding) {
        DEFAULT_CURRENCY = aDefaultCurrency;
        DEFAULT_ROUNDING = aDefaultRounding;
    }

    /**
     * Sum a collection of <tt>Money</tt> objects.
     * Currencies must match. You are encouraged to use database summary functions
     * whenever possible, instead of this method.
     *
     * @param aMoneys          collection of <tt>Money</tt> objects, all of the same currency.
     *                         If the collection is empty, then a zero value is returned.
     * @param aCurrencyIfEmpty is used only when <tt>aMoneys</tt> is empty; that way, this
     *                         method can return a zero amount in the desired currency.
     */
    public static Money sum(Collection<Money> aMoneys, Currency aCurrencyIfEmpty) {
        Money sum = new Money(ZERO, aCurrencyIfEmpty);
        for (Money money : aMoneys) {
            sum = sum.plus(money);
        }
        return sum;
    }

    /**
     * Return the amount passed to the constructor.
     */
    public BigDecimal getAmount() {
        return fAmount;
    }

    /**
     * Return the currency passed to the constructor, or the default currency.
     */
    public Currency getCurrency() {
        return fCurrency;
    }

    /**
     * Return the rounding style passed to the constructor, or the default rounding style.
     */
    public RoundingMode getRoundingStyle() {
        return fRounding;
    }

    /**
     * Return <tt>true</tt> only if <tt>aThat</tt> <tt>Money</tt> has the same currency
     * as this <tt>Money</tt>.
     */
    public boolean isSameCurrencyAs(Money aThat) {
        boolean result = false;
        if (aThat != null) {
            result = this.fCurrency.equals(aThat.fCurrency);
        }
        return result;
    }

    /**
     * Return <tt>true</tt> only if the amount is positive.
     */
    public boolean isPlus() {
        return fAmount.compareTo(ZERO) > 0;
    }

    /**
     * Return <tt>true</tt> only if the amount is negative.
     */
    public boolean isMinus() {
        return fAmount.compareTo(ZERO) < 0;
    }

    /**
     * Return <tt>true</tt> only if the amount is zero.
     */
    public boolean isZero() {
        return fAmount.compareTo(ZERO) == 0;
    }

    /**
     * Add <tt>aThat</tt> <tt>Money</tt> to this <tt>Money</tt>.
     * Currencies must match.
     */
    public Money plus(Money aThat) {
        checkCurrenciesMatch(aThat);
        return new Money(fAmount.add(aThat.fAmount), fCurrency, fRounding);
    }

    /**
     * Subtract <tt>aThat</tt> <tt>Money</tt> from this <tt>Money</tt>.
     * Currencies must match.
     */
    public Money minus(Money aThat) {
        checkCurrenciesMatch(aThat);
        return new Money(fAmount.subtract(aThat.fAmount), fCurrency, fRounding);
    }

    /**
     * Equals (insensitive to scale).
     * <format>
     * <P>Return <tt>true</tt> only if the amounts are equal.
     * Currencies must match.
     * This method is <em>not</em> synonymous with the <tt>equals</tt> method.
     */
    public boolean eq(Money aThat) {
        checkCurrenciesMatch(aThat);
        return compareAmount(aThat) == 0;
    }

    /**
     * Greater than.
     * <format>
     * <P>Return <tt>true</tt> only if  'this' amount is greater than
     * 'that' amount. Currencies must match.
     */
    public boolean gt(Money aThat) {
        checkCurrenciesMatch(aThat);
        return compareAmount(aThat) > 0;
    }

    /**
     * Greater than or equal to.
     * <format>
     * <P>Return <tt>true</tt> only if 'this' amount is
     * greater than or equal to 'that' amount. Currencies must match.
     */
    public boolean gteq(Money aThat) {
        checkCurrenciesMatch(aThat);
        return compareAmount(aThat) >= 0;
    }

    /**
     * Less than.
     * <format>
     * <P>Return <tt>true</tt> only if 'this' amount is less than
     * 'that' amount. Currencies must match.
     */
    public boolean lt(Money aThat) {
        checkCurrenciesMatch(aThat);
        return compareAmount(aThat) < 0;
    }

    /**
     * Less than or equal to.
     * <format>
     * <P>Return <tt>true</tt> only if 'this' amount is less than or equal to
     * 'that' amount. Currencies must match.
     */
    public boolean lteq(Money aThat) {
        checkCurrenciesMatch(aThat);
        return compareAmount(aThat) <= 0;
    }

    /**
     * Multiply this <tt>Money</tt> by an integral factor.
     * <format>
     * The scale of the returned <tt>Money</tt> is equal to the scale of 'this'
     * <tt>Money</tt>.
     */
    public Money times(int aFactor) {
        BigDecimal factor    = new BigDecimal(aFactor);
        BigDecimal newAmount = fAmount.multiply(factor);
        return new Money(newAmount, fCurrency, fRounding);
    }

    /**
     * Multiply this <tt>Money</tt> by an non-integral factor (having a decimal point).
     * <format>
     * <P>The scale of the returned <tt>Money</tt> is equal to the scale of
     * 'this' <tt>Money</tt>.
     */
    public Money times(double aFactor) {
        BigDecimal newAmount = fAmount.multiply(asBigDecimal(aFactor));
        newAmount = newAmount.setScale(getNumDecimalsForCurrency(), fRounding);
        return new Money(newAmount, fCurrency, fRounding);
    }

    // PRIVATE //

    /**
     * Divide this <tt>Money</tt> by an integral divisor.
     * <format>
     * <P>The scale of the returned <tt>Money</tt> is equal to the scale of
     * 'this' <tt>Money</tt>.
     */
    public Money div(int aDivisor) {
        BigDecimal divisor   = new BigDecimal(aDivisor);
        BigDecimal newAmount = fAmount.divide(divisor, fRounding);
        return new Money(newAmount, fCurrency, fRounding);
    }

    /**
     * Divide this <tt>Money</tt> by an non-integral divisor.
     * <format>
     * <P>The scale of the returned <tt>Money</tt> is equal to the scale of
     * 'this' <tt>Money</tt>.
     */
    public Money div(double aDivisor) {
        BigDecimal newAmount = fAmount.divide(asBigDecimal(aDivisor), fRounding);
        return new Money(newAmount, fCurrency, fRounding);
    }

    /**
     * Return the absolute value of the amount.
     */
    public Money abs() {
        return isPlus() ? this : times(-1);
    }

    /**
     * Return the amount x (-1).
     */
    public Money negate() {
        return times(-1);
    }

    /**
     * Returns
     * {@link #getAmount()}.getPlainString() + space + {@link #getCurrency()}.getSymbol().
     * <format>
     * <P>The return value uses the runtime's <em>default locale</em>, and will not
     * always be suitable for display to an end user.
     */
    public String toString() {
        return fAmount.toPlainString() + " " + fCurrency.getSymbol();
    }

    /**
     * Like {@link BigDecimal#equals(java.lang.Object)}, this <tt>equals</tt> method
     * is also sensitive to scale.
     * <format>
     * For example, <tt>10</tt> is <em>not</em> equal to <tt>10.00</tt>
     * The {@link #eq(Money)} method, on the other hand, is <em>not</em>
     * sensitive to scale.
     */
    public boolean equals(Object aThat) {
        if (this == aThat) return true;
        if (!(aThat instanceof Money)) return false;
        Money that = (Money) aThat;
        //the object fields are never null :
        boolean result = (this.fAmount.equals(that.fAmount));
        result = result && (this.fCurrency.equals(that.fCurrency));
        result = result && (this.fRounding == that.fRounding);
        return result;
    }

    public int hashCode() {
        if (fHashCode == 0) {
            fHashCode = HASH_SEED;
            fHashCode = HASH_FACTOR * fHashCode + fAmount.hashCode();
            fHashCode = HASH_FACTOR * fHashCode + fCurrency.hashCode();
            fHashCode = HASH_FACTOR * fHashCode + fRounding.hashCode();
        }
        return fHashCode;
    }

    public int compareTo(Money aThat) {
        final int EQUAL = 0;

        if (this == aThat) return EQUAL;

        //the object fields are never null
        int comparison = this.fAmount.compareTo(aThat.fAmount);
        if (comparison != EQUAL) return comparison;

        comparison = this.fCurrency.getCurrencyCode().compareTo(
                aThat.fCurrency.getCurrencyCode()
        );
        if (comparison != EQUAL) return comparison;


        comparison = this.fRounding.compareTo(aThat.fRounding);
        if (comparison != EQUAL) return comparison;

        return EQUAL;
    }

    /**
     * Always treat de-serialization as a full-blown constructor, by
     * validating the final state of the de-serialized object.
     */
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        //always perform the default de-serialization first
        aInputStream.defaultReadObject();
        //defensive copy for mutable date field
        //BigDecimal is not technically immutable, since its non-final
        fAmount = new BigDecimal(fAmount.toPlainString());
        //ensure that object state has not been corrupted or tampered with maliciously
        validateState();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        //perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }

    private void validateState() {
        if (fAmount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (fCurrency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        if (fAmount.scale() > getNumDecimalsForCurrency()) {
            throw new IllegalArgumentException(
                    "Number of decimals is " + fAmount.scale() + ", but currency only takes " +
                            getNumDecimalsForCurrency() + " decimals."
            );
        }
    }

    private int getNumDecimalsForCurrency() {
        return fCurrency.getDefaultFractionDigits();
    }

    private void checkCurrenciesMatch(Money aThat) {
        if (!this.fCurrency.equals(aThat.getCurrency())) {
            throw new MismatchedCurrencyException(
                    aThat.getCurrency() + " doesn't match the expected currency : " + fCurrency
            );
        }
    }

    /**
     * Ignores scale: 0 same as 0.00
     */
    private int compareAmount(Money aThat) {
        return this.fAmount.compareTo(aThat.fAmount);
    }

    private BigDecimal asBigDecimal(double aDouble) {
        String asString = Double.toString(aDouble);
        return new BigDecimal(asString);
    }

    /**
     * Thrown when a set of  <tt>Money</tt> objects do not have matching currencies.
     * <format>
     * <P>For example, adding together Euros and Dollars does not make any sense.
     */
    public static final class MismatchedCurrencyException extends RuntimeException {
        MismatchedCurrencyException(String aMessage) {
            super(aMessage);
        }
    }
}

