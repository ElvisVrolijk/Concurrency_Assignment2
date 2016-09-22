package com.company;

import java.util.concurrent.Semaphore;

/**
 * Created by e_voe_000 on 9/14/2016.
 */
public class Company {

    private static final int NR_OF_SOFTWARE_DEVELOPER = 6;
    private static final int NR_OF_USER = 10;
    private Jaap jaap;
    private SoftwareDeveloper[] softwareDevelopers;
    private User[] users;
    private Semaphore reportProblem, inviteUser, reportArrival, userConsultationInvitation, beginUserConsultation,
            softwareConsultationInvitation, beginSoftwareConsultation;

    public Company() {

        softwareDevelopers = new SoftwareDeveloper[NR_OF_SOFTWARE_DEVELOPER];
        users = new User[NR_OF_USER];

        for (int i = 0; i < NR_OF_SOFTWARE_DEVELOPER; i++) {
            softwareDevelopers[i] = new SoftwareDeveloper("Developer " + i, i);
            softwareDevelopers[i].start();
        }

        for (int i = 0; i < NR_OF_USER; i++) {
            users[i] = new User("User " + i, i);
            users[i].start();
        }
        jaap = new Jaap();
        jaap.start();
    }

    class Jaap extends Thread {

        @Override
        public void run() {

        }
    }

    class SoftwareDeveloper extends Thread {
        private int softwareDeveloperId;

        public SoftwareDeveloper(String name, int softwareDeveloperId) {
            super(name);
            this.softwareDeveloperId = softwareDeveloperId;
        }

        @Override
        public void run() {

        }
    }

    class User extends Thread {
        private int userId;

        public User(String name, int userId) {
            super(name);
            this.userId = userId;
        }

        @Override
        public void run() {
            try {
                //user reports the problem
                reportProblem.release();
                //user waits for the invitation
                inviteUser.acquire();
                //when he recieved an invitation he travels to the company and reports his arrival
                reportArrival.release();
                //he then waits for the consultation invitation
                userConsultationInvitation.acquire();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
