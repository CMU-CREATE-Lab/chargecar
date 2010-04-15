package chargecar.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.io.*;
import org.xml.sax.*;


public class GPXTripParser extends org.xml.sax.helpers.DefaultHandler {
	List<String> gmt_times;
	List<String> latitudes;
	List<String> longitudes;
	List<String> elevations;
	Stack<String> elementNames;
    private StringBuilder contentBuffer;
	private int points;
	   
	public GPXTripParser() {
		clear();
	}
	   
	public void clear() {
		elementNames = new Stack<String>();
	    contentBuffer = new StringBuilder();
	    gmt_times = new ArrayList<String>();
	    latitudes = new ArrayList<String>();
	  	longitudes = new ArrayList<String>();
	  	elevations = new ArrayList<String>();
	  	points = 0;
	}
	   
	public Trip read(String filename) throws IOException {
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
	    
	    return createTrip();
	}
	
	private Trip createTrip(){
		return null;
	}
	   
	   /*
	    * DefaultHandler::startElement() fires whenever an XML start tag is encountered
	    * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	    */
	   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	         // the <trkpht> element has attributes which specify latitude and longitude (it has child elements that specify the time and elevation)
	         if (localName.compareToIgnoreCase("trkpt") == 0) {
	        	 latitudes.add(attributes.getValue("lat"));
	        	 longitudes.add(attributes.getValue("lon"));
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
	            elevations.add(contentBuffer.toString());
	         }
	         else if (currentElement.compareToIgnoreCase("time") == 0) {
	          	gmt_times.add(contentBuffer.toString());
	         }
	      }	      
	   }
	   

	
}
		
		
/*		#This old code would query the USGS server for elevaiton data. Extremely slow. Instead, we use our own copy of USGS data

		i = 1	
		counter = 0
		start_index = -1

		num_times = @gmt_times.size
		while i < num_times do

			#this check is done if we have duplicate gmt entries (happens with the iPhone due to sub-second tracking)
			#this would cause a problem where the diff in gmt time would be 0 and it would seem that 24 hours had 
			#passed between these two points. Easiest method is to just filt out the 2nd point rather than account for it.
			if @gmt_times[i] == @gmt_times[i-1]
				@gmt_times.delete_at(i)
				@lats.delete_at(i)
				@lngs.delete_at(i)
				@elvs.delete_at(i)
				#puts "Bad GPS Data Was Removed."
				i -= 1
				num_times -= 1
			elsif @lats[i] == @lats[i-1] && @lngs[i] == @lngs[i-1]				


				if start_index == -1
					start_index = i-1
				end

				counter += 1

			else
				if (counter >= 20)		#20 steps (corresponding on average to 20-40 seconds)
					(counter+1).times do
						@gmt_times.delete_at(start_index)
						@lats.delete_at(start_index)
						@lngs.delete_at(start_index)
						@elvs.delete_at(start_index)
						#puts "Bad GPS Data Was Removed."
					end
					i = i - counter - 2
					num_times = @gmt_times.size
				end
				counter = 0
				start_index = -1	
			end

			i += 1
		end

		#we've gone through the file, but the last 20 have been flagged for removal
		if (counter >= 20)				#20 steps (corresponding on average to 20-40 seconds)
			(counter+1).times do
				@gmt_times.delete_at(start_index)
				@lats.delete_at(start_index)
				@lngs.delete_at(start_index)
				@elvs.delete_at(start_index)
				#puts "Bad GPS Data Was Removed."
			end
		end

	end


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
