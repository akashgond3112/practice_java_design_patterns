/**
 * @author akash
 * @date Jul 04, 2026
 * @time 10:45:26 AM
 */
package com.pattern;

import java.util.Map;

public class Car extends AbstractDocument implements HasModel, HasPrice, HasParts {

    public Car(Map<String, Object> properties) {
        super(properties);
    }
}
