module net.hamnaberg.json.immutable.jackson {
    exports net.hamnaberg.json.jackson;
    requires com.fasterxml.jackson.core;
    requires net.hamnaberg.json.immutable.ast;
    requires net.hamnaberg.json.immutable.core;
}
