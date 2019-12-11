#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.Dictionary;

public interface Instance {
    void init(Dictionary configProps);

    void reconfigure(Dictionary configProps);

    void stop();

    String getName();

    void setName(String name);
}
