/**
 * @author akash
 * @date Jul 04, 2026
 * @time 10:44:11 AM
 */
package com.pattern;

import java.util.Optional;

public interface HasModel extends Document {

    default Optional<String> getModel() {
        return Optional.ofNullable((String) get(Property.MODEL.toString()));
    }
}