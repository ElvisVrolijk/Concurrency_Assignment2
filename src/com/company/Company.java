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
    private Semaphore endDeveloperConsultation, beginSoftwareConsultation, inviteDeveloperForDeveloperConsult, inviteDeveloperForUserConsult;
    private Semaphore developerReportsIn, endUserConsultation, developerReportsOut, newProblem, mutex;

    Company() {
        newProblem = new Semaphore(1, true);
        reportProblem = new Semaphore(0, true);
        inviteUser = new Semaphore(0, true);
        reportArrival = new Semaphore(0, true);
        userConsultationInvitation = new Semaphore(0, true);
        beginUserConsultation = new Semaphore(1, true);
        beginSoftwareConsultation = new Semaphore(0, true);
        endDeveloperConsultation = new Semaphore(0, true);
        endUserConsultation = new Semaphore(0, true);
        developerReportsOut = new Semaphore(0, true);
        //limited amount of developers seats for a user consultation, and adt least 3 for the developers consultation
        inviteDeveloperForDeveloperConsult = new Semaphore(3, true);
        inviteDeveloperForUserConsult = new Semaphore(0, true);
        developerReportsIn = new Semaphore(0, true);

        mutex = new Semaphore(0, true);
        mutex.release();

        softwareDevelopers = new SoftwareDeveloper[NR_OF_SOFTWARE_DEVELOPER];
        users = new User[NR_OF_USER];

        jaap = new Jaap();
        jaap.start();

        for (int i = 0; i < NR_OF_USER; i++) {
            users[i] = new User("User " + i, i);
            users[i].start();
        }

        for (int i = 0; i < NR_OF_SOFTWARE_DEVELOPER; i++) {
            softwareDevelopers[i] = new SoftwareDeveloper("Developer " + i, i);
            softwareDevelopers[i].start();
        }


    }

    private class Jaap extends Thread {

        private void ConsultingUser() {
            try {
                System.out.println("Jaap started USER consultation");
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void ConsultingDeveloper() {
            try {
                System.out.println("Jaap started DEVELOPER consultation");
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //when jaap has received =>three developerReportsIn, he releases the beginDevelopersConsult
        @Override
        public void run() {
            while (true) {
                try {
                    //when user releases a problem Jaap acquires it
                    if (reportProblem.tryAcquire()) {
                        //when the problem is acquired jaap releases the invitation
                        inviteUser.release();
                        //when a user reports his arrival at the company, Jaap acquires it
                        reportArrival.acquire();
                        inviteDeveloperForUserConsult.release();
                        //when everything is ok for the one developer
                        mutex.acquire();
                        availableDevelopers--;
                        mutex.release();
                        beginUserConsultation.acquire();
                        //Jaap releases the invitation
                        userConsultationInvitation.release();
                        //Jaap starts the consultation
                        ConsultingUser();
                        endUserConsultation.release();
                        developerReportsOut.release();
                        newProblem.release();
                        System.out.println("User consult ended");
                    }

                    if (!reportProblem.tryAcquire()) {

                        if (beginSoftwareConsultation.tryAcquire() && inviteDeveloperForDeveloperConsult.availablePermits() == 0) {
                            mutex.acquire();
                            availableDevelopers = 0;
                            mutex.release();
                            ConsultingDeveloper();
                            endDeveloperConsultation.release();
                            System.out.println("Developer consult ended.");
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
                    System.out.println(getName() + " is available");
                    mutex.acquire();
                    availableDevelopers++;
                    mutex.release();

                    if (inviteDeveloperForUserConsult.tryAcquire()) {
                        //if he is invited, release begin consultation
                        System.out.println(getName() + " has been invited for a USER consultation");
                        beginUserConsultation.release();
                        System.out.println(getName() + " went to the USER consultation");
                        mutex.acquire();
                        availableDevelopers--;
                        mutex.release();
                        developerReportsOut.acquire();
                    }
                    if (inviteDeveloperForDeveloperConsult.tryAcquire()) {
                        //if there are three reports in
                        System.out.println(getName() + " was invited for a DEVELOPERS consultation");
                        beginSoftwareConsultation.release();
                        System.out.println(getName() + " went to the DEVELOPERS consultation");
                        mutex.acquire();
                        availableDevelopers = 0;
                        mutex.release();
                        endDeveloperConsultation.acquire();
                        inviteDeveloperForUserConsult.release();
                    }
                    //if he isn't invited he goes back to work
                    developerReportsIn.acquire();
                    mutex.acquire();
                    availableDevelopers--;
                    mutex.release();
                    Work();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    private class User extends Thread {
        private int userId;
        private boolean problemFound = true;

        public User(String name, int userId) {
            super(name);
            this.userId = userId;
        }

        private void JustLive() {
            try {
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            while (true) {
                try {
                    JustLive();

                    if (problemFound ) {

                        reportProblem.release();

                        if (newProblem.tryAcquire()) {
                            System.out.println(getName() + " found a problem.");
                            //user reports the problem
                            //user waits for the invitation
                            inviteUser.acquire();
                            System.out.println("Jaap has invited the user");
                            //after acquiring the invitation, he travels to the company
                            Travel();
                            //and reports his arrival
                            reportArrival.release();
                            System.out.println(getName() + " has reported his arrival");
                            //he then waits for the consultation invitation
                            userConsultationInvitation.acquire();
                            //when he is invited the consult starts
                            beginUserConsultation.release();
                            System.out.println(getName() + " is in a USER consultation");
                            endUserConsultation.acquire();
                            problemFound = false;
                            System.out.println(getName() + "'s problem was solved.");
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
