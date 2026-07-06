package com.mrz07.gdxdialogs.core;

/**
 * Factory for creating dialog instances. Used by {@link GDXDialogs#registerDialog} to
 * register platform-specific dialog implementations without any class-name reflection.
 * Defined as a separate interface (rather than java.util.function.Supplier) for GWT
 * compatibility.
 */
public interface DialogFactory<T> {
    T create();
}
