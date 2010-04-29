package chargecar.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;
import java.io.*;
import org.xml.sax.*;


public class GPXTripParser extends org.xml.sax.helpers.DefaultHandler {
	List<Calendar> rawTimes;
	List<Double> rawLats;
	List<Double> rawLons;
	List<Double> rawEles;
	Stack<String> elementNames;
    private StringBuilder contentBuffer;
	private int points;
	   
	public GPXTripParser() {
		clear();
	}
	   
	public void clear() {
		elementNames = new Stack<String>();
	    contentBuffer = new StringBuilder();
	    rawTimes = new ArrayList<Calendar>();
	    rawLats = new ArrayList<Double>();
	  	rawLons = new ArrayList<Double>();
	  	rawEles = new ArrayList<Double>();
	  	points = 0;
	}
	   
	public List<List<PointFeatures>> read(String filename, double carMass) throws IOException {
		clear();
		FileInputStream in = new FileInputStream(new File(filename));
	    InputSource source = new InputSource(in);
	    XMLReader parser;
		try {
			parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			parser.setContentHandler(this);
		    parser.parse(source);
			
		} catch (SAXException e) {
			e.printStackTrace();
			throw new IOException();
		}
	    in.close();	    
	    
	    return calculateTrips(carMass);
	}
	
	
	
	private List<List<PointFeatures>> calculateTrips(double carMassKg) {
		List<List<PointFeatures>> trips = new ArrayList<List<PointFeatures>>();
		if(rawTimes.isEmpty()){
			return trips;
		}
		//clean of duplicate readings
		removeDuplicates();
		
		List<Calendar> times = new ArrayList<Calendar>();
		List<Double> lats = new ArrayList<Double>();
		List<Double> lons = new ArrayList<Double>();
		List<Double> eles = new ArrayList<Double>();
		
		times.add(rawTimes.get(0));
		lats.add(rawLats.get(0));
		lons.add(rawLons.get(0));
		eles.add(rawEles.get(0));
		
		for(int i=1;i<rawTimes.size();i++){
			long msDiff = rawTimes.get(i).getTimeInMillis() - rawTimes.get(i-1).getTimeInMillis();
			if(msDiff > 360000)
			{
				//if enough time has passed between points (360 seconds)
				//consider them disjoint trips
				trips.add(calculateTrip(times,lats,lons,eles,carMassKg));
				times.clear();
				lats.clear();
				lons.clear();
				eles.clear();
			}		
			
			times.add(rawTimes.get(i));
			lats.add(rawLats.get(i));
			lons.add(rawLons.get(i));
			eles.add(rawEles.get(i));			
		}
		
		if(times.size() > 10){
			//get last trip
			trips.add(calculateTrip(times,lats,lons,eles,carMassKg));			
		}	
		
		return trips;
	}

	private List<PointFeatures> calculateTrip(List<Calendar> times, List<Double> lats, List<Double> lons, List<Double> eles, double carMass){		
		removeTunnels(times, lats, lons, eles);
		interpolatePoints(times, lats, lons, eles);
		List<PointFeatures> tripPoints = new ArrayList<PointFeatures>(times.size());
		runPowerModel(tripPoints, times, lats, lons, eles, carMass);		
		return tripPoints;
	}

	private void interpolatePoints(List<Calendar> times, List<Double> lats,
			List<Double> lons, List<Double> eles) {
		//make sure there is a point every 2 seconds, 
		//as this is all gps based without car scantool
		for(int i=1;i<times.size();i++){
			long newTime = times.get(i).getTimeInMillis();
			long oldTime = times.get(i-1).getTimeInMillis();
			if(newTime - oldTime > 2000){
				double latps = (lats.get(i) - lats.get(i-1))/(newTime - oldTime);
				double lonps = (lons.get(i) - lons.get(i-1))/(newTime - oldTime);
				double eleps = (eles.get(i) - eles.get(i-1))/(newTime - oldTime);
				Calendar interpTime = Calendar.getInstance();
				interpTime.setTimeInMillis(oldTime+2000);
				times.add(i, interpTime);
				lats.add(i, lats.get(i-1)+2*latps);
				lons.add(i, lons.get(i-1)+2*lonps);
				eles.add(i, eles.get(i-1)+2*eleps);				
			}			
		}		
	}

	private void runPowerModel(List<PointFeatures> tripPoints,
			List<Calendar> times, List<Double> lats,
			List<Double> lons, List<Double> eles, double carMassKg) {
		
		List<Double> planarDistances = new ArrayList<Double>();
		List<Double> adjustedDistances = new ArrayList<Double>();
		List<Double> speeds = new ArrayList<Double>();
		List<Double> accelerations = new ArrayList<Double>();
		List<Double> powerDemands = new ArrayList<Double>();
				
		planarDistances.add(0.0);
		adjustedDistances.add(0.0);
		speeds.add(0.0);
		accelerations.add(0.0);

		for(int i=1; i<times.size();i++){
			long sDiff = (times.get(i).getTimeInMillis() - times.get(i-1).getTimeInMillis())/1000;
			double eleDiff = eles.get(i) - eles.get(i-1);
			double tempDist = Haversine(lats.get(i-1), lons.get(i-1), lats.get(i), lons.get(i));
			planarDistances.add(tempDist);			
			tempDist = Math.sqrt((tempDist*tempDist)+(eleDiff*eleDiff));
			adjustedDistances.add(tempDist);			
			double tempSpeed = tempDist/(sDiff);
			
			if(tempDist < 1E-6){
				speeds.add(0.0);
			}else{
				speeds.add(tempSpeed);
			}		
			accelerations.add((speeds.get(i) - speeds.get(i-1))/sDiff);	
		}		



		final double carArea =1.988;//honda civic 2001 si fronta area in metres sq
		final double carDragCoeff=0.31;//honda civic 2006 sedan		
		final double mu = 0.015; //#rolling resistance coef
		final double aGravity = 9.81;
        final double offset = -0.35;
        final double ineff = 1/0.85;
        final double rollingRes = mu*carMassKg*aGravity; 
        final double outsideTemp = ((60 + 459.67) * 5/9);//60F to kelvin
		
		for(int i=0;i<accelerations.size();i++)
		{
			double pressure = 101325 * Math.pow((1-((0.0065 * eles.get(i))/288.15)), ((aGravity*0.0289)/(8.314*0.0065)));			
			double rho = (pressure * 0.0289) / (8.314 * outsideTemp);			
			double airResCoeff = 0.5*rho*carArea*carDragCoeff;
			double mgsintheta = 0;

			if (i > 0){
				final double eleDiff = eles.get(i) - eles.get(i-1);

				if(planarDistances.get(i) < 1E-6){
					mgsintheta = 0;
				}
				else if (Math.abs(speeds.get(i)) < 0.50){
					mgsintheta = 0;			
				}
				else if(eles.get(i) > eles.get(i-1))
				{
					mgsintheta = (carMassKg * aGravity * Math.sin(Math.atan(eleDiff/planarDistances.get(i)))) * -1;
				}
				else if (eles.get(i) < eles.get(i-1))
				{
					mgsintheta = (carMassKg * aGravity * Math.sin(Math.atan(eleDiff/planarDistances.get(i)))) * -1;
				}
			}

			double airRes = airResCoeff * speeds.get(i)*speeds.get(i);
			double force = carMassKg * accelerations.get(i);
			double pwr = 0.0;
			double speed = speeds.get(i);
			if (Math.abs(mgsintheta) < 1E-6)
			{
				if (Math.abs(force) < 1E-6 || force > (rollingRes + airRes))
					pwr = (((force + rollingRes + airRes) * speed) * ineff);
				else if (force <= (rollingRes + airRes))
					pwr = 0.35 * (force - rollingRes - airRes) * speed;
			}		
			//#uphill
			else if (eles.get(i) > eles.get(i-1)){
				if (force <= (mgsintheta + rollingRes + airRes))
					pwr = 0.35 * (force - mgsintheta - rollingRes - airRes) * speed;
				else if (force > (mgsintheta + rollingRes + airRes))
					pwr = (((force - rollingRes - airRes - mgsintheta) * speed) * ineff);
				else if (Math.abs(force) <1E-6)
					pwr = (((mgsintheta + rollingRes + airRes)) * ineff);

			}
			//#downhill	
			else if (eles.get(i) < eles.get(i-1)){
				if (force <= (mgsintheta + rollingRes + airRes))
					pwr = 0.35 * (force - mgsintheta - rollingRes - airRes) * speed;
				else if (Math.abs(force) < 1E-6 || force > (mgsintheta + rollingRes + airRes))
					pwr = (((force + rollingRes + airRes - mgsintheta) * speed) * ineff);
			}

			pwr = ((pwr/-1000.0) + offset);

			if (speed > 12.0)
				pwr = ((pwr - (0.056*(speed*speed))) + (0.68*speed));

			powerDemands.add(pwr);		
		}
		
		for(int i=1;i<times.size();i++){
			int periodMS = (int)(times.get(i).getTimeInMillis() - times.get(i-1).getTimeInMillis());
			tripPoints.add(new PointFeatures(lats.get(i-1), lons.get(i-1), eles.get(i-1), accelerations.get(i), speeds.get(i), powerDemands.get(i), periodMS, times.get(i-1)));
		}
		PointFeatures endPoint = new PointFeatures(lats.get(lats.size()-1),lons.get(lons.size()-1), eles.get(eles.size()-1), 0.0, 0.0, 0.0, 1000, times.get(times.size()-1));
		tripPoints.add(endPoint);
		
	}

	private void removeTunnels(List<Calendar> times, List<Double> lats,
			List<Double> lons, List<Double> eles) {
		//removes tunnel points, tunnels will be fixed later by interpolation
		int consecutiveCounter = 0;
		for(int i=1;i<times.size();i++){
			if(lats.get(i).compareTo(lats.get(i-1)) == 0 &&
					lons.get(i).compareTo(lons.get(i-1)) == 0){
				//consecutive readings at the same position
				consecutiveCounter++;				
			}
			else if(consecutiveCounter > 0){
				//position has changed, after consectuive readings at same position
				//can be tunnel, red light, etc...		
				if(Haversine(lats.get(i-1),lons.get(i-1),lats.get(i), lons.get(i)).compareTo(50.0) > 0){
					//if traveled at least 50 metres, assume tunnel
					times.subList(i-consecutiveCounter, i).clear();
					lats.subList(i-consecutiveCounter, i).clear();
					lons.subList(i-consecutiveCounter, i).clear();
					eles.subList(i-consecutiveCounter, i).clear();
					i = i - consecutiveCounter;
				}
				consecutiveCounter = 0;
			}
		}
		
	}

	private Double Haversine(double lat1, double lon1, double lat2, double lon2) {
	 	double R = 6371000; //earth radius, metres
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1); 
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
		        Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c;
		return d;
	}

	private void removeDuplicates() {
		int length = rawTimes.size();
		for(int i=1;i<length;i++){
			if(rawTimes.get(i).compareTo(rawTimes.get(i-1))<=0){
				rawTimes.remove(i);
				rawLats.remove(i);
				rawLons.remove(i);
				rawEles.remove(i);
				i--;
				length--;
			}
		}
	}
	   
	   /*
	    * DefaultHandler::startElement() fires whenever an XML start tag is encountered
	    * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	    */
	   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	         // the <trkpht> element has attributes which specify latitude and longitude (it has child elements that specify the time and elevation)
	         if (localName.compareToIgnoreCase("trkpt") == 0) {
	        	 rawLats.add(Double.parseDouble(attributes.getValue("lat")));
	        	 rawLons.add(Double.parseDouble(attributes.getValue("lon")));
	        	 points++;
	         }
	      // Clear content buffer
	      contentBuffer.delete(0, contentBuffer.length());
	      
	      // Store name of current element in stack
	      elementNames.push(qName);
	   }
	   
	   /*
	    * the DefaultHandler::characters() function fires 1 or more times for each text node encountered
	    *
	    */
	   public void characters(char[] ch, int start, int length) throws SAXException {
	      contentBuffer.append(String.copyValueOf(ch, start, length));
	   }
	   
	   /*
	    * the DefaultHandler::endElement() function fires for each end tag
	    *
	    */
	   public void endElement(String uri, String localName, String qName) throws SAXException {
	      String currentElement = elementNames.pop();
	      
	      if (points > 0 && currentElement != null) {
	         if (currentElement.compareToIgnoreCase("ele") == 0) {
	            rawEles.add(Double.parseDouble(contentBuffer.toString()));
	         }
	         else if (currentElement.compareToIgnoreCase("time") == 0) {
	        	 rawTimes.add(gmtStringToCalendar(contentBuffer.toString()));	      
	         }
	      }	      
	   }

	private static Calendar gmtStringToCalendar(String dateTimeString) {
		//incoming format: 2010-02-25T22:44:57Z
		String dateString = dateTimeString.substring(0,dateTimeString.indexOf('T'));
		String[] dates = dateString.split("-");
		int year = Integer.parseInt(dates[0]);
		int month = Integer.parseInt(dates[0]);
		int day = Integer.parseInt(dates[0]);
		//format: 22:44:57
		String timeString = dateTimeString.substring(dateTimeString.indexOf('T')+1, dateTimeString.indexOf('Z'));
		String[] times = timeString.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		int second = Integer.parseInt(times[2]);
		
		Calendar calTime = Calendar.getInstance();
		calTime.set(year, month, day, hour, minute, second);
		
		return calTime;
	}
}
		
		
/*

	#after parsing the data file, do the main calculation on it - power, speeds, etc
	def self.do_calculations(carWeight,cargoWeight,passengerWeight,carArea,carDragCoeff,temperature)
		@times = []

		i = 1
		time = 0

		#calculate relative times
		@times << 0	#for the first data point we are not moving yet
		num_times = @gmt_times.size
		while i < num_times

			#grab the hour(s) from the gmt time strings
			time2_hour = @gmt_times[i][0,2].to_i
			time1_hour = @gmt_times[i-1][0,2].to_i

			#grab the minute(s) from the gmt time strings
			time2_minute = @gmt_times[i][2,2].to_i
			time1_minute = @gmt_times[i-1][2,2].to_i

			#grab the second(s) from the gmt time strings
			time2_sec = @gmt_times[i][4,6].to_f		#taken as a float to handle fractional gpx time stamps
			time1_sec = @gmt_times[i-1][4,6].to_f	#taken as a float to handle fractional gpx time stamps

			#convert the full times into seconds
			time1_seconds = time1_hour * 3600 + time1_minute * 60 + time1_sec
			time2_seconds = time2_hour * 3600 + time2_minute * 60 + time2_sec

			if (time1_seconds >= time2_seconds) #time wrap around - ie midnight
				time = time + (((24 * 60 * 60) - time1_seconds) + time2_seconds)
				@times << time
			else
				time = time + (time2_seconds - time1_seconds)
				@times << time
			end

			i += 1
		end

		@speeds = []
		@accels = []

		i = 1

		@planar_dists = []			#assumes world is flat; used only to calculate angle of the ground
		@adjusted_dists = []		#assumes world is not flat; used for distance/speed calculations

		@planar_dists << 0.0
		@adjusted_dists << 0.0

		#calculate speed
		@speeds << 0	#for the first data point we are not moving yet
		num_lats = @lats.size
		while i < num_lats
			temp_dist = Haversine(@lats[i-1], @lngs[i-1], @lats[i], @lngs[i]).to_f #returns dist in km
			temp_planar_dist = temp_dist*1000.0 #now in meters

			@planar_dists << temp_planar_dist.round(6)

			temp_adjusted_dist = Math.sqrt((temp_planar_dist**2)+(@elvs[i]-@elvs[i-1])**2)  #account for angle

			@adjusted_dists << temp_adjusted_dist.round(6)	#store dist into an array of distances


			temp_speed = (temp_adjusted_dist/(@times[i]-@times[i-1])) #speed is now m/s

			if (temp_planar_dist == 0.0)
				@speeds << 0.0
			else		
				@speeds << temp_speed.round(6)
			end

			i += 1
		end

		i = 1

		#calculate acceleration
		@accels << 0	#for the first data point we are not moving yet
		num_speeds = @speeds.size
		while i < num_speeds
			@accels << ((@speeds[i] - @speeds[i-1])/(@times[i]-@times[i-1])).round(6)
			i += 1
		 end

		car_weight = carWeight * 0.45359237 					#lbs to kg
		passenger_weight = passengerWeight * 0.45359237 		#lbs to kg
		cargo_weight = cargoWeight * 0.45359237 				#lbs to kg
		area = carArea											#in m^2
		cp = carDragCoeff

		if carWeight.nil?
			car_weight = @average_car_curb_weight
		end

		if carArea.nil?
			area = @average_car_area
		end

		if carDragCoeff.nil?
			cp = @average_drag_coefficient
		end

		mu = 0.015				#rolling resistance coef
		gravity = 9.81			#in m/s^2

		total_weight = car_weight + passenger_weight + cargo_weight

		offset = -0.35
		ineff = 1/0.85

		rolling_resistance = mu*total_weight*gravity 	# Rolling Resistance 
		outsideTemp = ((temperature + 459.67) * 5/9)	# in Kelvin now

		@calc_power = []
		mgsinthetas = []
		air_resistances = []

		i = 0
		#calculate power based on model
		num_accels = @accels.size
		while i < num_accels

			pressure = 101325 * (1-((0.0065 * @elvs[i])/288.15)) ** ((9.81*0.0289)/(8.314*0.0065))
			rho = (pressure * 0.0289) / (8.314 * outsideTemp)
			air_resistance_coef = 0.5*rho*area*cp

			mgsintheta = 0

			if (i > 0)
				elv_diff = (@elvs[i] - @elvs[i-1])

				if (@planar_dists[i] == 0.0)
					mgsintheta = 0
				elsif (@speeds[i] < 0.50)	#speeds really low here, so mgsintheta doesn't account for very much at all
					mgsintheta = 0			
				#going uphill
				elsif (@elvs[i] > @elvs[i-1])
					mgsintheta = (total_weight * gravity * Math.sin(Math.atan(elv_diff/@planar_dists[i]))) * -1
				#going downhill
				elsif (@elvs[i] < @elvs[i-1])
					mgsintheta = (total_weight * gravity * Math.sin(Math.atan(elv_diff/@planar_dists[i]))) * -1
				end
			end

			mgsinthetas << mgsintheta

			air_resistance = air_resistance_coef * (@speeds[i] ** 2)
			air_resistances << air_resistance

			force = total_weight * @accels[i]

			#level ground
			if (mgsintheta == 0)
				if (force == (rolling_resistance + air_resistance))
					pwr = 0
				elsif (force == 0)
					pwr = (((rolling_resistance + air_resistance) * @speeds[i]) * ineff)
				elsif (force < (rolling_resistance + air_resistance))
					pwr = 0.35 * (force - rolling_resistance - air_resistance) * @speeds[i] 
				elsif (force > (rolling_resistance + air_resistance))
					pwr = (((force + rolling_resistance + air_resistance) * @speeds[i]) * ineff)
				end
			#uphill
			elsif (@elvs[i] > @elvs[i-1])
				if (force == (mgsintheta + rolling_resistance + air_resistance))
					pwr = 0
				elsif (force > (mgsintheta + rolling_resistance + air_resistance))
					pwr = (((force - rolling_resistance - air_resistance - mgsintheta) * @speeds[i]) * ineff)
				elsif (force == 0)
					pwr = (((mgsintheta + rolling_resistance + air_resistance)) * ineff)
				elsif (force < (mgsintheta + rolling_resistance + air_resistance))
					pwr = 0.35 * (force - mgsintheta - rolling_resistance - air_resistance) * @speeds[i]
				end
			#downhill	
			elsif (@elvs[i] < @elvs[i-1])
				if (force == (mgsintheta + rolling_resistance + air_resistance))
					pwr = 0
				elsif (force > (mgsintheta + rolling_resistance + air_resistance))
					pwr = (((force + rolling_resistance + air_resistance - mgsintheta) * @speeds[i]) * ineff)
				elsif (force == 0)
					pwr = (((rolling_resistance + air_resistance - mgsintheta) * @speeds[i]) * ineff)
				elsif (force < (rolling_resistance + air_resistance + mgsintheta))
					pwr = 0.35 * (force - mgsintheta - rolling_resistance - air_resistance) * @speeds[i]
				end
			end

			tmp = ((pwr/-1000.0) + offset)

			#quadratic adjustment if the speed is above 12 m/s
			#temporary fix for high speed drives
			if (@speeds[i] > 12.0)
				tmp = ((tmp - (0.056*(@speeds[i]**2))) + (0.68*@speeds[i]))
			end

			@calc_power << tmp.round(5)   #in Kw, round to 5 decimal places

			i += 1

		end   
	end


	#second pass on the scan tool data to remove any calculation outliers as a result of gps satellite oddities
	def self.clean_gps_data2
		i = 0

		num_times = @gmt_times.size
		while i < num_times do
			if (@speeds[i] > 80 || @accels[i].abs > 80 || @calc_power[i].abs > 100)  #80 m/s, ~180 mph; 100 kW
				#puts "speeds: " + @speeds[i].to_s + " accels: " + @accels[i].abs.to_s + " calc_power: " + @calc_power[i].abs.to_s
				@gmt_times.delete_at(i)
				@times.delete_at(i)
				@lats.delete_at(i)
				@lngs.delete_at(i)
				@elvs.delete_at(i)
				@planar_dists.delete_at(i)
				@adjusted_dists.delete_at(i)
				@speeds.delete_at(i)
				@accels.delete_at(i)
				@calc_power.delete_at(i)
				#puts "More Bad GPS Data Was Removed."
				i = i - 2  #arrays have been resized, so we need to reset our index 1 back for this and 1 more since we do +1 below
				num_times = @gmt_times.size
			end
			i += 1
		end
	end


	#write the newly created calculations based on gps data to a file
	def self.write_gps_calculations(upload_id,type)	
		i = 0
		file = File.new("#{$data_directory}/#{type}/#{upload_id}/calculations.txt", 'w')		
		file2 = File.new("#{$data_directory}/#{type}/#{upload_id}/calculations-strip.txt", 'w')		

		#file.puts "format: GMT time, relative time, lat, long, elev, planar distance, adjusted distance, speed, accel, calc power"
		#file2.puts "format: GMT time, relative time, elev, planar distance, adjusted distance, speed, accel, calc power, power, current, voltage"
		num_times = @gmt_times.size
		while i < num_times do
			file.puts "#{@gmt_times[i]},#{@times[i]},#{@lats[i]},#{@lngs[i]},#{@elvs[i]},#{@planar_dists[i]},#{@adjusted_dists[i]},#{@speeds[i]},#{@accels[i]},#{@calc_power[i]}"
			file2.puts "#{@gmt_times[i]},#{@times[i]},#{@elvs[i]},#{@planar_dists[i]},#{@adjusted_dists[i]},#{@speeds[i]},#{@accels[i]},#{@calc_power[i]}"
			i += 1
		end

		file.close
		file2.close
	end


	#segment gps calculation files for use with graphing - done becuase large time gaps may occur in trip and graphing "breaks" as a result
	def self.segment_gps_data(upload_id,type)
		segment = 1		
		i = 1

		file = File.new("#{$data_directory}/#{type}/#{upload_id}/calculations-#{segment}.txt", 'w')
		file.puts "#{@gmt_times[0]},#{@times[0]},#{@lats[0]},#{@lngs[0]},#{@elvs[0]},#{@planar_dists[0]},#{@adjusted_dists[0]},#{@speeds[0]},#{@accels[0]},#{@calc_power[0]}"

		num_times = @gmt_times.size
		while i < num_times do
			if ((@times[i] - @times[i-1]) > 60 * 10)	#10 minutes have passed, create a new drive segment
				file.close
				segment += 1
				file = File.new("#{$data_directory}/#{type}/#{upload_id}/calculations-#{segment}.txt", 'w')
			end
			file.puts "#{@gmt_times[i]},#{@times[i]},#{@lats[i]},#{@lngs[i]},#{@elvs[i]},#{@planar_dists[i]},#{@adjusted_dists[i]},#{@speeds[i]},#{@accels[i]},#{@calc_power[i]}"
			i += 1
		end

		file.close
	end

*/
