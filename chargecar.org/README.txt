------------------------------------------------------------------------------------------------------------------------

ABOUT
-----
The code provided here is for use with USGS NED data in GridFloat format.  All code is copyright Carnegie Mellon
University, but provided for non-commercial use under the terms of the CNU General Public License v2.  Corporations or
anyone wishing to use this code under other licensing terms must contact us at chargecar@cs.cmu.edu for a commercial
license.

------------------------------------------------------------------------------------------------------------------------

DATABASE
---------
Meta data used by the applications is stored in a MySQL database containing a single table created with the following
commands:

   create table grid_float_index (
      header_file_path varchar(1000) UNIQUE NOT NULL,
      data_file_path varchar(1000) UNIQUE NOT NULL,
      num_columns int(11) NOT NULL,
      num_rows int(11) NOT NULL,
      ll_longitude double NOT NULL,
      ll_latitude double NOT NULL,
      ur_longitude double NOT NULL,
      ur_latitude double NOT NULL,
      cell_size double NOT NULL,
      no_data_value double NOT NULL,
      is_big_endian boolean NOT NULL,
      PRIMARY KEY(header_file_path)
   );
   create index data_file_path_index on grid_float_index(data_file_path);
   create index ll_longitude_index on grid_float_index(ll_longitude);
   create index ll_latitude_index on grid_float_index(ll_latitude);
   create index ur_longitude_index on grid_float_index(ur_longitude);
   create index ur_latitude_index on grid_float_index(ur_latitude);
   create index cell_size_index on grid_float_index(cell_size);

------------------------------------------------------------------------------------------------------------------------

BUILDING
--------
Build must be done with ant.  Simply executing "ant" from the command line at the root directory will build everything.
During the build, you'll be prompted for the username and password of the database used to store the meta data mentioned
above.

------------------------------------------------------------------------------------------------------------------------

APPLICATIONS
------------

ElevationMapGenerator
---------------------
Used for creating greyscale renderings of NED data.  Originally written for GridFloat samples downloaded from the USGS
Seamless server.  Needs to be updated for use with our 1/3 and 1/9 arcsecond NED data of the entire USA.  Example usage:

   java -jar chargecar-elevation-map-generator.jar /path/to/gridfloat.hdr /path/to/output.png

GridFloatIndexer
----------------
Indexes the local GridFloat NED data, storing meta data in the database.  Example usage:

   java -jar chargecar-grid-float-indexer.jar /path/to/NED/root/directory

GPXElevationConverter
---------------------
Converts the elevations in a given GPX file to one containing elevations obtained either from the local NED or the USGS
NED Web Service.  Prints the resulting GPX on stdout.  Example usage:

   java -jar chargecar-gpx-elevation-converter.jar /path/to/file.gpx

or

   java -jar chargecar-gpx-elevation-converter.jar --usgs /path/to/file.gpx

Note that if the --usgs switch is supplied, fetches from the USGS web service are throttled to occur at 50 ms intervals.

GPXTool
----------------
Can validate and privatize a GPX file and also correct the elevations by doing a lookup from the local NED or the USGS
NED Web Service.  Pretty-prints the resulting GPX on stdout.  Example usage:

   Validate and pretty-print:
   java -jar chargecar-gpx-tool.jar /path/to/file.gpx

   Pretty-print only:
   java -jar chargecar-gpx-tool.jar --no-validate /path/to/file.gpx

   Privatize and pretty-print:
   java -jar chargecar-gpx-tool.jar --privatize --no-validate /path/to/file.gpx

   Privatize a 1/10 of a mile radius around a particular location and pretty-print (location must be specified as LAT,LONG):
   java -jar chargecar-gpx-tool.jar --privatize-location=40.443887,-79.946357 --no-validate /path/to/file.gpx

   Privatize a 2/10 of a mile radius around a particular location and pretty-print (location must be specified as LAT,LONG,RADIUS):
   java -jar chargecar-gpx-tool.jar --privatize-location=40.443887,-79.946357,321.8688 --no-validate /path/to/file.gpx

   Validate, lookup elevations from local NED, and pretty-print:
   java -jar chargecar-gpx-tool.jar --lookup-elevations-locally /path/to/file.gpx

   Validate, privatize, lookup elevations from USGS web service, and pretty-print:
   java -jar chargecar-gpx-tool.jar --privatize --lookup-elevations-online /path/to/file.gpx

If both the "--lookup-elevations-locally" and "--lookup-elevations-online" switches are given, the request to do lookups
online is ignored.

GPXValidator
----------------
Validates GPX files.  If valid, it prints the word "Valid" to standard out and returns with a status code of zero.
Otherwise, it prints "Invalid: " followed by the error message explaining why the validation failed and then exists with
a non-zero status code. Example usage:

   java -jar chargecar-gpx-validator.jar /path/to/file.gpx

MotionXGPSRawFileConverter
----------------
Converts a MotionX-GPS .kmz or raw.xml file to a GPX file with fractional-second timestamps:

   java -jar chargecar-motionx-gps-raw-file-converter.jar /path/to/motionx-gpx.kmz

or

   java -jar chargecar-motionx-gps-raw-file-converter.jar /path/to/raw.xml

If executed without an argument, it launches a GUI which allows you to choose a KMZ to convert.

SpeedCalculator
----------------
Calculates speed over time for a given GPX file using the Spherical Law of Cosines.

   java -jar chargecar-speed-calculator.jar /path/to/file.gpx

------------------------------------------------------------------------------------------------------------------------

MISC
----

Gridfloat Format
----------------
The 32-bit (4 byte) is a simple binary raster format (floating point data). There is an accompanying ASCII header file
that provides file size information (number of rows and columns). The data are stored in row major order (all the data
for row 1, followed by all the data for row 2, etc.). More information about binary floating point is at:

http://grouper.ieee.org/groups/754/
http://www.math.grin.edu/~stone/courses/fundamentals/IEEE-reals.html
http://www.psc.edu/general/software/packages/ieee/ieee.html

Q: Does the elevation start in the center of the pixel or one of the corners?
A. The of the elevation "posting" depends on the format of the data.
   ArcGrid format -- the header coordintates are the upper left corner of the upper left pixel.
   BIL format -- the header coordinates are the center of the upper left pixel.
   GridFloat format -- the header coordinates are lower left-hand corner of the lowest left-hand pixel.

USGS NED Web Service
--------------------

Use this to fetch best elevation data for a given lat/long:

   curl "http://gisdata.usgs.net/xmlwebservices2/elevation_service.asmx/getElevation?X_Value=LONGITUDE&Y_Value=LATITUDE&Elevation_Units=meters&Elevation_Only=true&Source_Layer=-1"

Docs:

   http://gisdata.usgs.net/XMLWebServices2/Elevation_Service_Methods.php

------------------------------------------------------------------------------------------------------------------------

