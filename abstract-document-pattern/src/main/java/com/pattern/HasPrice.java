/**
 * @author akash
 * @date Jul 04, 2026
 * @time 10:43:51 AM
 */
package com.pattern;

import java.util.Optional;

public interface HasPrice extends Document {

    default Optional<Number> getPrice() {
        return Optional.ofNullable((Number) get(Property.PRICE.toString()));
    }
}
