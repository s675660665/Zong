package musicxmltestsuite.tests.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for tests which are not complete yet.
 *
 * @author Andreas Wenger
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ToDo {

	String value() default "";
	
}
