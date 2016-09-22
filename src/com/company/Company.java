package com.company;

import java.util.concurrent.Semaphore;

/**
 * Created by e_voe_000 on 9/14/2016.
 */
public class Company {

    //countdown for developers consult, there have to be at least three
    private int availableDevelopers = 0;

    private static final int NR_OF_SOFTWARE_DEVELOPER = 6;
    private static final int NR_OF_USER = 10;
    private Jaap jaap;
    private SoftwareDeveloper[] softwareDevelopers;
    private User[] users;
    private Semaphore reportProblem, inviteUser, reportArrival, userConsultationInvitation, beginUserConsultation;
    private Semaphore softwareConsultationInvitation, beginSoftwareConsultation, inviteDeveloperForDeveloperConsult, inviteDeveloperForUserConsult;
    private Semaphore developerReportsIn;

    Company() {
        reportProblem = new Semaphore(0, true);
        inviteUser = new Semaphore(0, true);
        reportArrival = new Semaphore(0, true);
        userConsultationInvitation = new Semaphore(0, true);
        beginUserConsultation = new Semaphore(0, true);
        beginSoftwareConsultation = new Semaphore(0, true);
        softwareConsultationInvitation = new Semaphore(0, true);

        //limited amount of developers seats for a user consultation, and adt least 3 for the developers consultation
        inviteDeveloperForDeveloperConsult = new Semaphore(0, true);
        inviteDeveloperForUserConsult = new Semaphore(0, true);
        developerReportsIn = new Semaphore(0, true);

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

    private class Jaap extends Thread {

        private void ConsultingUser(){
            try {
                System.out.println("Jaap is in a user consultation");
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void ConsultingDeveloper(){
            try {
                System.out.println("Jaap is in a Developer consultation");
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //when jaap has recieved =>three developerReportsIn, he releases the beginDevelopersConsult
        @Override
        public void run() {
            try {
                //when user releases a problem jaap acquires it
                reportProblem.acquire();
                //when the problem is acquired jaap releases the invitation
                inviteUser.release();
                //when a user reports his arrival at the company, Jaap acquires it
                reportArrival.acquire();
                //when everything is ok for the one developer
                if (availableDevelopers >= 1) {
                    //TODO: all user acquire this, and ONE developer acquires this
                    inviteDeveloperForUserConsult.release();
                    //Jaap releases the invitation
                    userConsultationInvitation.release();
                    //Jaap starts the consultation
                    beginUserConsultation.release();
                }
                if (availableDevelopers >= 3) {
                    //TODO: all available developers acquire this
                    inviteDeveloperForDeveloperConsult.release();
                    beginSoftwareConsultation.release();
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class SoftwareDeveloper extends Thread {
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
                try {
                    //if there is one software developer available, invite all the users and th developer to the
                    //the developer says he is available for a consultation (doesn't matter which)
                    developerReportsIn.release();
                    availableDevelopers++;

                    if (inviteDeveloperForUserConsult.tryAcquire()) {
                        //TODO: only the first report in is invited, the rest goes back to work
                        //if he is invited, release begin consultation
                        beginUserConsultation.release();
                    } else if (inviteDeveloperForDeveloperConsult.tryAcquire()) {
                        //if there are three reports in
                        beginSoftwareConsultation.acquire();
                    } else {
                        //if he isn't invited he goes back to work
                        developerReportsIn.acquire();
                        availableDevelopers--;
                        Work();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    private class User extends Thread {
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
                //after acquiring the invitation, he travels to the company
                Travel();
                //and reports his arrival
                reportArrival.release();
                //he then waits for the consultation invitation
                userConsultationInvitation.acquire();
                //when he is invited the consult starts
                beginUserConsultation.acquire();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
