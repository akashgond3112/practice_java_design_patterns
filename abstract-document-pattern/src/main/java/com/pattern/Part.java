/**
 * @author akash
 * @date Jul 04, 2026
 * @time 10:44:59 AM
 */
package com.pattern;

import java.util.Map;

public class Part extends AbstractDocument implements HasType, HasModel, HasPrice {

    public Part(Map<String, Object> properties) {
        super(properties);
    }
}