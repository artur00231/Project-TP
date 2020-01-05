package tp_project.Server;

import java.util.ArrayList;

public class ServerCleanup {
    private static ArrayList<Runnable> to_exec = new ArrayList<>();

    /*
    *This method have to be called by server
    */
    void execClean() {
        for (Runnable exec : to_exec) {
            exec.run();
        }

        to_exec.clear();
    }

    public void addCleaningFuncion(Runnable funcion) {
        to_exec.add(funcion);
    }
}