package de.joernott.c0de.core.value;

/**
 * A data type of the language (see docs/concept/04-data.md). ERROR is a predefined struct type
 * ({@link StructDefinition#ERROR}).
 */
public sealed interface Type permits Type.Simple, Type.Str, Type.Array, Type.Hash, Type.Struct {
    /** The default value of the type. */
    Value defaultValue();

    /** The simple data types. */
    enum Simple implements Type {
        BOOLEAN,
        INTEGER,
        FLOAT,
        CHAR,
        TIME;

        @Override
        public Value defaultValue() {
            return switch (this) {
                case BOOLEAN -> BooleanValue.FALSE;
                case INTEGER -> new IntegerValue(0);
                case FLOAT -> new FloatValue(0.0);
                case CHAR -> new CharValue(' ');
                case TIME -> new TimeValue(0);
            };
        }
    }

    /** STRING with an optional maximum length ({@code null} for no limit). */
    record Str(Long maxLength) implements Type {
        /** STRING without a length limit. */
        public static final Str UNLIMITED = new Str(null);

        @Override
        public Value defaultValue() {
            return new StringValue("");
        }

        @Override
        public String toString() {
            return maxLength == null ? "STRING" : "STRING " + maxLength;
        }
    }

    /** ARRAY with the type of its elements. */
    record Array(Type element) implements Type {
        @Override
        public Value defaultValue() {
            return new ArrayValue(element);
        }

        @Override
        public String toString() {
            return "ARRAY " + element;
        }
    }

    /** HASH with the type of its values. */
    record Hash(Type element) implements Type {
        @Override
        public Value defaultValue() {
            return new HashValue(element);
        }

        @Override
        public String toString() {
            return "HASH " + element;
        }
    }

    /** A struct type, including ERROR. */
    record Struct(StructDefinition definition) implements Type {
        /** The predefined type ERROR. */
        public static final Struct ERROR = new Struct(StructDefinition.ERROR);

        @Override
        public Value defaultValue() {
            return new StructValue(definition);
        }

        @Override
        public String toString() {
            return definition == StructDefinition.ERROR ? "ERROR" : "STRUCT " + definition.name();
        }
    }
}
