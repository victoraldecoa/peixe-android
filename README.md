peixe-android
=============

App for University of São Paulo's students to see the restaurant's menus (code and issues in Portuguese-BR)

Aplicativo para os estudantes da USP verem os cardápios dos restaurantes.


To start developing this app:
-------------

1- Install the Android SDK and Eclipse:
 - http://developer.android.com/sdk/installing/index.html

2- Install Git:
 - http://git-scm.com/

3- Download dependencies on another folder:

 - peixe-services-android

 git clone https://github.com/victoraldecoa/peixe-services-android.git
 cd peixe-services-android
 git checkout 2.0

4- Configure peixe-android project

 - On Eclipse, click File, New, Other
 - On the windows that appears, expand "Android" and select "Android Project from Existing Code"
 - Browse for peixe-services-android/peixe-services-android
 - On Package Explorer, right-click on the recently created project, click "Properties"
 - Click Android
 - Select the latest Target you have

5- Configure the other projects
 - peixe-android

6- Configure Peixe's project
 - Go to Peixe's project Properties, click Android, on Library box click "Add..." and add peixe-android-services
