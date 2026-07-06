/**
 * @author akash
 * @date Jul 04, 2026
 * @time 10:45:47 AM
 */
package com.pattern;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
  public static void main(String[] args) {
    log.info("Constructing parts and car");

    var wheelProperties = Map.of(
            Property.TYPE.toString(), "wheel",
            Property.MODEL.toString(), "15C",
            Property.PRICE.toString(), 100L);

    var doorProperties = Map.of(
            Property.TYPE.toString(), "door",
            Property.MODEL.toString(), "Lambo",
            Property.PRICE.toString(), 300L);

    var carProperties = Map.of(
            Property.MODEL.toString(), "300SL",
            Property.PRICE.toString(), 10000L,
            Property.PARTS.toString(), List.of(wheelProperties, doorProperties));

    var car = new Car(carProperties);

    log.info("Here is our car:");
    log.info("-> model: {}", car.getModel().orElseThrow());
    log.info("-> price: {}", car.getPrice().orElseThrow());
    log.info("-> parts: ");
    car.getParts().forEach(p -> log.info("\t{}/{}/{}",
            p.getType().orElse(null),
            p.getModel().orElse(null),
            p.getPrice().orElse(null))
    );
}
}
