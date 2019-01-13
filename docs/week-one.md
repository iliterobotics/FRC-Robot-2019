# Kickoff - Day One

## Summary
We started migrating to WPILib's new development environment and installed the tools released for 2019. Since this game will require a high-level of automation, we started working on
a vision system to detect the reflective tape targets around the field. We also created some initial requirements for a two-hatch autonomous, including starting position, where we will be scoring
the hatches, and other requirements. In addition, we continued revamping our robot telemetry system, which will make integrating robot software and hardware an easier process and allow us to 
effectively diagnose issues during competition. Since autonomous has been replaced by the "sandstorm" period, the driver's heads-up display we've been working on is more important than ever, since
it will allow us to quickly see the status of the robot without seeing it directly.

- Stephen began migrating our project to WPILib's new build system, which was released today.
- We all installed the tooling to drive the robot, deploy code to the robot, and diagnose the Talon SRX.
- Jesse created a document detailing some initial measurements and requirements (including starting position) for a two-hatch autonomous. It is TBD which platform level we will start on.
- Gleb and Mr. Shapiro requested that the Limelight be wired onto the programming bot by Electronics and started reviewing the Limelight documentation.
- Faris and Daniel continued working on bringing our telemetry branch up-to-date.

# Day Two

## Summary
We finished migrating our project to the 2019 WPILib environment. The next step is to conduct a code cleanup and review of the codebase as a whole. One of our software mentors, Chris, taught
our member more about how Git works and about standard Git workflows. Since we've determined that this robot will almost certainly need an elevator, the team started writing a framework
for an elevator and started integrating it with the driver's head-up display. We also successfully tracked cargo balls - the next step is to debug why we are not recieving targeting data. Finally,
we made some fixes to get the programming bot successfully running code.

- Jesse helped Stephen get the 2019 project compiling with WPILib's new vendor dependency system.
- Chris discussed Git and project workflow with the members who were present.
- Daniel created the data structure and classes for an elevator, and continued working on a heads-up display for the driver while Kate shadowed.
- Gleb and Mr. Shapiro successfully tuned the Limelight on the 2018 robot to track cargo balls, but had issues getting targeting data from the Limelight and deploying code to the 2018 robot to debug the issue.
- Electronics imaged the programming bot RIO and updated all the firmware. They also updated the Limelight with the latest available image, 2018.5.
- Stephen explained how robot odometry works to John and described the basics of generating trajectories using splines and arcs.
- Stephen got the 2019 robot project successfully running on the robot and ran an initial test of trajectory-following.

# Day Three-Four

