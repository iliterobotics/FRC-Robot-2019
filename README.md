# FRC-Robot

This is the incubating software framework for ILITE's 2019 season.

## Project Structure

The project consists of three subprojects: `robot`, `display`, and `common`.
By separating code into subprojects, we can ensure that `robot` doesn't accidentally
call JavaFX libraries from the `display` project, and vice-versa. Instead, we can allow
the `robot` and `display` packages to both use code in the `common` package

### Robot
The `robot` subproject contains code running on the actual robot.
```$xslt
us.ilite.lib - Contains hardware drivers and utilities that are dependent on WPILib components.
us.ilite.robot - Parent package for all robot code
    commands - Contains autonomous commands
    hardware - Contains hardware classes that allow modules to interface with hardware
    loops - Contains high-frequency loops running on the robot
    modules - Contains the modules that control subsystems on the robot
```

### Common
The `common` subproject contains code shared by the `robot` and `display` subprojects, such
as common data structures + more.
```$xslt
config - contains constants, etc.
lib - contains generic classes used from year-to-year
    geometry - Contains geometry classes used to represent robot movement
    util - Utility classes that are not dependent of WPILib
types - Contains enumerations defining common data structure used throughout other subprojects
```

### Display
The `display` subproject contains the code for running the driver's display, autonomous selection,
and logging software.
```$xslt
logging - Contains logging code
display - Contains display code
```

## Building and Deploying
- To build, run `gradlew build`
- To deploy to the robot, run `gradlew deploy`
    - Remember to **build** before you **deploy**


## Contributing

Here's how to get your code into the main robot repository:

### If you've just joined the team:
1. Make an account on [GitHub](https://github.com/).
2. Ask one of the robot programming leads to add your account to the iliterobotics robot programming team.

### If it's the first time you've contributed to this repo:
1. Clone the repo to your computer - `git clone https://github.com/iliterobotics/FRC-Robot-2019`

### Any time you want to make a change:
1. Create and checkout a new branch.
  * `git checkout -b <your_branch_name>`, where <your_branch_name> is a descriptive name for your branch. For example `fix-shooter-wheel`, `two-ball-auto`, or `climbing`. Use dashes in the branch name, not underscores.
1. Make whatever code changes you want/need/ to make. Be sure to write tests for your changes!
1. Commit your work locally.
  * Try to make your commits as atomic (small) as possible. For example, moving functions around should be different from adding features, and changes to one subsystem should be in a different commit than changes to another subsystem.
  * Follow [these](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html) conventions for commit messages. Or else.
  * If your change is anything more than a few lines or small fixes, don't skip the extended description. If you are always using `git commit` with the `-m` option, stop doing that.
1. Push to your branch.
  * `git push origin <your_branch_name>`.
1. Submit a pull request.
  1. Log into Github.
  1. Go to the page for your forked repo.
  1. Select the branch that you just pushed from the "Branch" dropdown menu.
  1. Click "New Pull Request".
  1. Review the changes that you made.
  1. If you are happy with your changes, click "Create Pull Request".
1. Wait
  * People must review (and approve of) your changes before they are merged - master is locked to any pull requests that don't have at least 2 reviews.
    * Specifically, the rules are that one of the following two conditions must be true for it to get merged:
      1. 1 mentor and 1 other person have approved
      1. 2 experienced students and one other person have approved
  * If there are any concerns about your pull request, fix them. Depending on how severe the concerns are, the pull request may be merged without it, but everyone will be happier if you fix your code. 
To update your PR, just push to the branch you made before.
  * Don't dismiss someone's review when you make changes - instead, ask them to re-review it.
1. Merge your changes into master
  * If there are no conflicts, push the "Squash and merge" button, write a good commit message, and merge the changes.
  * If there are conflicts, fix them locally on your branch, push them, wait for Jenkins to pass, and then squash and merge.
1. ???
1. Profit

## Helpful Tips

### VSCode Trouble?

If you're having trouble with IntelliJ, run `gradlew clean build`. This
deletes any compiled Java files and rebuilds the project.

### Tools

You can run any of these with `./gradlew <insert-tool-name-here>`

#### Shuffleboard
- Lets you view values posted to NetworkTables and put them into widgets
- Make sure the server address is set the correct address (File -> Preferences -> NetworkTables)

#### RIOLog
- View console output from the RoboRIO in your terminal
- You can also see this from the driver station

#### OutlineViewer
- Like ShuffleBoard, but you can only view raw values and can't set values. Useful for fast debugging.

### Other remotes

You can add "remotes" to github that refer to other people's robot code repos. This allows you to, for example, take a look at someone else's code to look over it, you would be able to `git checkout wesley/branch-that-breaks-everything` to see it. To add a remote, just do `git remote add <name_of_person> https://github.com/<username>/robot-code`. Once you've done this, you can use `git fetch <name_of_person>` to get updated code from other people's repos!

