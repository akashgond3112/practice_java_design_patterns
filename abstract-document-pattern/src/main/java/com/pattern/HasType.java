/**
 * @author akash
 * @date Jul 04, 2026
 * @time 10:43:27 AM
 */
package com.pattern;

import java.util.Optional;

public interface HasType extends Document {

    default Optional<String> getType() {
        return Optional.ofNullable((String) get(Property.TYPE.toString()));
    }
}
