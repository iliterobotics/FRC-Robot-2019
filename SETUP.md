## VSCode
Install VSCode from [here](https://code.visualstudio.com/Download)

### Extensions
You'll need the following extensions:
#### Mandatory
- Java Extension Pack - Provides Java support for VSCode.
- VS Live Share - Edit code together in real-time, or review code with a bunch of people.
- Gradle Language Support - Provides language support for Gradle. Lets us better edit gradle files.
#### Optional
- Git Project Manager - Once configured, lets you open projects by selecting from a list of Git projects on your computer.
- Gnuplot - GNUPlot language support. Useful for viewing logs.
- Keybindings from your favorite Java editor (Eclipse, IntelliJ, etc.)
- Path Intellisense - Autocomplete file paths
- A nice-looking theme

### Setting up the JDK
You'll have to provide VSCode with the location of your JDK installation. If you don't have
the JDK installed, click [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
1. Navigate to `File -> Preferences -> Settings`
1. Search for "jdk" in the search bar
1. Click `Java Configuration` on the left-hand sidebar. The only setting visible should be `Java: Home`
1. Click on `Edit in settings.json`
1. The right-hand side stores any settings made by the user. Add a line like this: `"java.home": "/Path/To/JDK/Installation",`
    - If you don't know where your JDK installation is, it's probably in `C:/Program Files/Java/jdk1.x.x_xxx`, where the `x`'s are version numbers specific to your installation.
1. You're done! Wait a bit for the Java Language Server to start up and recognize your project (you should see a little spinning icon at the bottom of your screen),
then test it out by clicking on a variable type (like `Module` or `Drive` or `Double`) and pressing <kbd>F12</kbd>. If all goes well, you should be taken to the definition of that class.

### Opening Projects
It's pretty easy. `File -> Open Folder...`, then navigate to the repository you have cloned (The folder named `FRC-Robot-YYYY`). 

### Want to learn more?
[Setting up Java and VSCode](https://code.visualstudio.com/docs/java/java-tutorial)

[Code Navigation](https://code.visualstudio.com/docs/editor/editingevolved)

[Basic Editing](https://code.visualstudio.com/docs/editor/codebasics)