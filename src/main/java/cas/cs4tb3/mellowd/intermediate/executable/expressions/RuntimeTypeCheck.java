package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.variables.IncorrectTypeException;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class RuntimeTypeCheck<T> implements Expression<T> {
    private final Class<T> type;
    private final Expression<?> expression;
    private final SourceLink sourceLink;

    public RuntimeTypeCheck(Class<T> type, Expression<?> expression, SourceLink sourceLink) {
        this.type = type;
        this.expression = expression;
        this.sourceLink = sourceLink;
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        Object value = expression.evaluate(environment);
        if (value == null || type.isAssignableFrom(value.getClass())) {
            return (T) value;
        } else {
            throw sourceLink.toCompilationException(new IncorrectTypeException(sourceLink.text, value.getClass(), type));
        }
    }
}
