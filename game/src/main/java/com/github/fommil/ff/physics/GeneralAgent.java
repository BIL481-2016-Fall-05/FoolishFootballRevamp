package com.github.fommil.ff.physics;

/**
 * Created by dyani on 12/17/2016.
 */
public class GeneralAgent extends Thread{

    GamePhysics game;

    public GeneralAgent(GamePhysics game) {
        this.game = game;
        this.start();
    }

    public void run() {
        while(true) {

        }
    }
}
