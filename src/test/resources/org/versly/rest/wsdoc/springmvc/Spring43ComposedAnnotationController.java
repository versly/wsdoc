package org.versly.rest.wsdoc.springmvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Tests the new method-level composed variants of the @{@link org.springframework.web.bind.annotation.RequestMapping} annotations introduced in
 * Spring Framework 4.3.
 * <p>
 * This tests the following annotations:
 * <ul>
 *     <li>{@link GetMapping}</li>
 *     <li>{@link PostMapping}</li>
 *     <li>{@link PutMapping}</li>
 *     <li>{@link DeleteMapping}</li>
 *     <li>{@link PatchMapping}</li>
 * </ul>
 *
 * @author Sidharth Mishra
 * @see https://docs.spring.io/spring-framework/docs/4.3.1.RELEASE/spring-framework-reference/htmlsingle/#mvc-ann-requestmapping-composed
 */
public class Spring43ComposedAnnotationController {

    /**
     * Some description for this GET method.
     *
     * @param id      The ID.
     * @param pageNbr The page number.
     * @return some map from string to string.
     */
    @GetMapping("/theGetMethod")
    public Map<String, String> theGetMethod(@RequestParam("id") UUID id, @RequestParam("pageNbr") int pageNbr) {

        return new HashMap<>();
    }

    /**
     * Some description for this POST method.
     *
     * @param theReqBody the request-body.
     * @return the ID of item created.
     */
    @PostMapping("/thePostMethod")
    public UUID thePostMethod(@RequestBody Map<String, String> theReqBody) {

        return null;
    }

    /**
     * Some description for this PUT method.
     *
     * @param id                 The ID of the item to update.
     * @param theReqBody         the request-body containing data to update.
     * @param httpServletRequest the raw {@link HttpServletRequest} instance for more contextual data.
     * @return the updated item's ID.
     */
    @PutMapping("/thePutMethod/{id}")
    public UUID thePutMethod(@PathVariable("id") UUID id, @RequestBody Map<String, String> theReqBody, HttpServletRequest httpServletRequest) {

        return null;
    }

    /**
     * Some description for this DELETE method.
     *
     * @param id The ID of the item to delete.
     */
    @DeleteMapping("/theDeleteMethod/{id}")
    public void theDeleteMethod(@PathVariable("id") UUID id) {

    }

    /**
     * Some description for this PATCH method.
     *
     * @param id         The ID of the item to patch.
     * @param theReqBody The request-body containing data to path.
     * @return the ID of the item that was successfully patched.
     */
    @PatchMapping("/thePatchMethod/{id}")
    public UUID thePatchMethod(@PathVariable("id") UUID id, @RequestBody Map<String, String> theReqBody) {

        return null;
    }
}