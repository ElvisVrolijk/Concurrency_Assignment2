package com.company;

import java.util.concurrent.Semaphore;

/**
 * Created by e_voe_000 on 9/14/2016.
 */
public class Company {

    private static final int NR_OF_SOFTWARE_DEVELOPER = 6;
    private static final int NR_OF_USER = 10;
    private static final int NR_OF_DEVELOPER_SEATS = 3;
    private Jaap jaap;
    private SoftwareDeveloper[] softwareDevelopers;
    private User[] users;
    private Semaphore reportProblem, inviteUser, reportArrival, userConsultationInvitation, beginUserConsultation;
    private Semaphore softwareConsultationInvitation, beginSoftwareConsultation, inviteDeveloperForDeveloperConsult, inviteDeveloperForUSerConsult;
    private Semaphore developerReportsIn;

    public Company() {

        reportProblem = new Semaphore(0, true);
        inviteUser = new Semaphore(0, true);
        reportArrival = new Semaphore(0, true);
        userConsultationInvitation = new Semaphore(0, true);
        beginUserConsultation = new Semaphore(0, true);
        beginSoftwareConsultation = new Semaphore(0, true);
        softwareConsultationInvitation = new Semaphore(0, true);
        //limited amount of developers seats for a user consultation, and adt least 3 for the developers consultation
        inviteDeveloperForDeveloperConsult = new Semaphore(NR_OF_DEVELOPER_SEATS, true);
        inviteDeveloperForUSerConsult = new Semaphore(0, true);

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
            try {
                //when user releases a problem jaap acquires it
                reportProblem.acquire();
                //when the problem is acquired jaap releases the invitation
                inviteUser.release();
                //when a user reports his arrival at the company, Jaap acquires it
                reportArrival.acquire();
                //when everything is ok for the TODO: one developer

                //Jaap releases the invitation
                inviteUser.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class SoftwareDeveloper extends Thread {
        private int softwareDeveloperId;

        //the developer is working
        private void Work() {
            try {
                System.out.println(getName() + " is working.");
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public SoftwareDeveloper(String name, int softwareDeveloperId) {
            super(name);
            this.softwareDeveloperId = softwareDeveloperId;
        }

        @Override
        public void run() {
            while (true) {
                //if there is one software developer available, invite all the users and th developer to the
                //the developer says he is available for a consultation (doesn't matter which)
                developerReportsIn.release();
                //he can either get an invitation or he goes back to work
                if (inviteDeveloperForUSerConsult.tryAcquire()) {
                    //TODO: only the first report in is invited, the rest goes back to work
                    //if he is invited, release begin consultation
                    beginUserConsultation.release();
                } else {
                    //if he isn't invited he goes back to work
                    Work();
                }

                if (inviteDeveloperForDeveloperConsult.tryAcquire()) {
                    //if there are three reports in
                    //TODO: start the consult
                } else {
                    //else if there are no three developers
                    //they wait for the rest

                }
            }
        }
    }


    class User extends Thread {
        private int userId;

        public User(String name, int userId) {
            super(name);
            this.userId = userId;
        }


        private void Travel() {
            try {
                System.out.println(getName() + " is traveling.");
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                //user reports the problem
                reportProblem.release();
                //user waits for the invitation
                inviteUser.acquire();
                //after acquireing the invitation, he travels to the company
                Travel();
                //and reports his arrival
                reportArrival.release();
                //he then waits for the consultation invitation
                userConsultationInvitation.acquire();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
