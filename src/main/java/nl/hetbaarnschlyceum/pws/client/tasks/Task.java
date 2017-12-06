package nl.hetbaarnschlyceum.pws.client.tasks;

public abstract class Task implements Runnable {
    protected String request;

    public abstract void setRequest(String request);

    @Override
    public void run() {

    }
}
