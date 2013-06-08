#helloSweet

This is demo program showing how to build minimal swt/jface application.

##Compilation

gradle build

##Runing from command-line

Use platform-specific shell or batch script in folder $buildDir/output

##Running from eclipse

Use menu command "Run As/Java Application", choose hellosweet.Main class as main-class.

##Known issues

* NullPointerException on project compilation or update 
  (under eclipse, not always reproducible).

  **Source**
  
  The error occurs on resource loading in class SweetAppPlugin, mostly under eclipse.

  **Solution**

  1. Find GradleDaemon process on the given machine. On linux:
  
  ```shell
  ps aux | grep GradleDaemon | grep -v grep
  ```  

  2. Kill the process. On linux:
  
  ```
  sudo kill -9 process_num
  ```
  
  3. It's not needed to restart eclipse - killing GradleDaemon fixes the problem
     immediately.