package shivshank.pipeline;

class PipelineException extends RuntimeException {
    public PipelineException() {
        super();
    }
    
	public PipelineException(String message) {
		super(message);
	}
    
    public PipelineException(Throwable t) {
        super(t);
    }
    
    public PipelineException(String message, Throwable t) {
        super(message, t);
    }
}