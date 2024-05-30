package co.luckywolf.benchmark;

class JPong extends JCommand {

    public Service service;
    public PongStatus pongStatus;

    public JPong(Service service, PongStatus pongStatus, String s) {
        this.service = service;
        this.pongStatus = pongStatus;
    }
}
