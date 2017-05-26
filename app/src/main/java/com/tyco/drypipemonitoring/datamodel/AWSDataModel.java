package com.tyco.drypipemonitoring.datamodel;

/**
 * Created by abhijitk on 9/20/2016.
 */
public class AWSDataModel {

    private State state;
    private String clientToken;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    @Override
    public String toString() {
        return "ClassPojo [state = " + state + ", clientToken = " + clientToken + "]";
    }


    public class State {
        private Reported reported;

        public Reported getReported() {
            return reported;
        }

        public void setReported(Reported reported) {
            this.reported = reported;
        }

        @Override
        public String toString() {
            return "ClassPojo [reported = " + reported + "]";
        }
    }


    public class Reported {
        private String id;
        private String timestamp;
        private String A;
        private String C;
        private String I;
        private String T;
        private String W;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getA() {
            return A;
        }

        public void setA(String A) {
            this.A = A;
        }

        public String getC() {
            return C;
        }

        public void setC(String c) {
            this.C = c;
        }

        public String getI() {
            return I;
        }

        public void setI(String i) {
            this.I = i;
        }

        public String getT() {
            return T;
        }

        public void setT(String t) {
            this.T = t;
        }

        public String getW() {
            return W;
        }

        public void setW(String w) {
            this.W = w;
        }

        @Override
        public String toString() {
            return "[id = " + id + ", timestamp = " + timestamp + ", A = " + A + ", C = " + C + ", I = " + I + ", T = " + T + ", W = " + W + "]";
        }
    }


}
