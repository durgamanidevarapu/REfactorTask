package com.engx.cleancode.finaltask.task1.thirdpartyjar;

public interface Command {
    boolean canProcess(String command);
    void process(String command);


}