package us.ilite.robot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Updates a given list of commands in parallel, rather than one at a time.
 * ParallelCommand is a command itself, so it can be used in sequence with other commands if
 * needed. Some potential use cases are: turning and raising an elevator at the same time,
 * Grabbing vision targeting data and turning to a heading based on that data, etc.
 */
public class ParallelCommand implements ICommand {

  List<ICommand> mCommandList;
  
  public ParallelCommand(List<ICommand> pCommandList) {
    this.mCommandList = new LinkedList<>();
    this.mCommandList.addAll(pCommandList);
  }
  
  public ParallelCommand(ICommand ... pCommands ) {
    this(Arrays.asList(pCommands));
  }
  
  @Override
  public void init(double pNow) {
    for(ICommand c : mCommandList) {
      c.init(pNow);
    }
  }

  @Override
  public boolean update(double pNow) {
    List<ICommand> toremove = new ArrayList<>();
    for(ICommand c : mCommandList) {
      if(c.update(pNow)) {
        toremove.add(c);
      }
    }
    mCommandList.removeAll(toremove);
    if(mCommandList.isEmpty()) {
      return true;
    }
    return false;
  }

  @Override
  public void shutdown(double pNow) {
    for(ICommand c : mCommandList) {
      c.shutdown(pNow);
    }
  }

}
