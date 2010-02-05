= AIMAX-OSM =

This project provides a framework for building intelligent Open Street Map
(OSM) data applications. It was originally designed to validate and test agent
and search concepts from the AIMA library in an interesting, non-trivial
application area.

Central part of the project is an OSM viewer implementation. It is designed
as a general purpose viewer which is highly configurable and extendible.
The internal map representation is chosen as close as possible to the
original OSM XML file format. Classification and abstraction
lies in the responsibility of the renderer. A rather general renderer
implementation is included, which is based on declarative rendering rules.

Routing functionality is based on the AIMA-CORE library.
All dependencies to the AIMA libraries are encapsulated in the routing
sub-package and the applications using functionality from that package.
So the viewer classes can also be used as stand-alone library
for building general OSM applications.

In the current version, relation entities are ignored and
the size of the map should be limited to about a million nodes
to avoid long loading times. E.g. detailed maps of cities like Berlin 
can be loaded and displayed without any problem if enough heap space
is provided (VM argument -Xmx500M). The tool Osmosis can be used to generate
maps complying to this requirement.

Getting started: Run one of the classes in the applications sub-package.
If no map is displayed, edit the map file name in the main method of the
application's source file and try again.
Then place the mouse inside the map viewer pane. Try mouse-left, mouse-right,
mouse-drag, ctrl-mouse-left, plus button, minus button, ctrl-plus, ctrl-minus,
arrow buttons, and also the mouse-wheel for navigation,
mark setting, and track definition. For routing, at least two marks
must be set.


== Keywords ==

Open Street Map, OSM, Routing, OSM Viewer, Java


== Requirements ==

- Depends on the aima-core and the aima-gui project. 

- To establish a connection to a GPS, the RXTX serial port library
(http://www.rxtx.org/) must be installed. See gps package documentation
for details.


== Current Release: 0.1.1-AIMA3e Published ==

First release based on the 3rd edition of AIMA which contains the
OSM library.


== Applications ==
Under the release/ directory you should find three jar files, aima-core.jar, aima-gui.jar, and aimax-osm.jar. 
Ensure these are on your CLASSPATH, the different GUI programs that can be run using these are:
 * java -classpath aimax-osm.jar aimax.osm.applications.OsmViewerApp
  * just the plain viewer (not dependent on AIMA)
 * java -classpath aimax-osm.jar aimax.osm.applications.OsmViewerPlusApp
  * demonstrates, how to configure and extend the viewer
 * java -classpath aimax-osm.jar aimax.osm.applications.RoutePlannerApp
  * uses aima-core search functionality for routing in OSM maps
 * java -classpath aimax-osm.jar aimax.osm.applications.OsmAgentApp
  * lets map agents from aima-core act in map environments which are defined by OSM data 
 * java -classpath aimax-osm.jar aimax.osm.applications.OsmSearchDemoAgentApp
  * visualizes simulated search space exploration of different search strategies
 * java -classpath aimax-osm.jar aimax.osm.applications.MiniNaviApp
  * provides a base for GPS navigation system development