package edu.wpi.first.deployutils;

public interface Resolver<T> {
    T resolve(Object o);

    public static class ResolveFailedException extends RuntimeException {
        private static final long serialVersionUID = 8627035155558972166L;

        public ResolveFailedException(String message) {
            super(message);
        }
    }
}
