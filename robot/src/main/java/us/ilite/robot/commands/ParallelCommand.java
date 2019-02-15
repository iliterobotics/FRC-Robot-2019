package us.ilite.robot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ParallelCommand extends ACommand {

  List<ACommand> mCommandList;
  
  public ParallelCommand(List<ACommand> pCommandList) {
    this.mCommandList = new LinkedList<>();
    this.mCommandList.addAll(pCommandList);
  }
  
  public ParallelCommand(ACommand... pCommands ) {
    this(Arrays.asList(pCommands));
  }
  
  @Override
  public void init(double pNow) {
    for(ACommand c : mCommandList) {
      c.init(pNow);
    }
  }

  @Override
  public boolean update(double pNow) {
    List<ACommand> toremove = new ArrayList<>();
    for(ACommand c : mCommandList) {
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
    for(ACommand c : mCommandList) {
      c.shutdown(pNow);
    }
  }

}
