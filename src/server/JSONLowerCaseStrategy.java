package server;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;

/**
* Created by IntelliJ IDEA.
* User: chris
* Date: 26.12.11
* Time: 19:34
* To change this template use File | Settings | File Templates.
*/
class JSONLowerCaseStrategy extends PropertyNamingStrategy {
    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return defaultName.toLowerCase();
    }

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return defaultName.toLowerCase();
    }

}
