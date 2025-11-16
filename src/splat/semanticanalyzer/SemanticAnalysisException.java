package splat.semanticanalyzer;

import splat.SplatException;

public class SemanticAnalysisException extends SplatException {

    public SemanticAnalysisException(String message, int line, int column) {
        super(message, line, column);
    }
}
