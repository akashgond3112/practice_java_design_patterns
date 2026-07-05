/**
 * @author akash
 * @date Jul 04, 2026
 * @time 10:44:30 AM
 */
package com.pattern;

import java.util.stream.Stream;


public interface HasParts extends Document {

    default Stream<Part> getParts() {
        return children(Property.PARTS.toString(), Part::new);
    }
}