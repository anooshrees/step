// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      // meeting request has: name, duration, and attendees (List of Strings)
      // event has: time range, attendees, and name

      long duration = request.getDuration();

       // meeting request exceeds one day
      if (duration > TimeRange.WHOLE_DAY.duration()) {
          return Arrays.asList();
      }

      // all attendees are free
      if (events.isEmpty()) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }

      // there are no attendees
      if (request.getAttendees().isEmpty()) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }
      

      Collection<String> mandatory = request.getAttendees();
      // Collection<String> optional = request.getOptionalAttendees();

      
      // retrieve all the times when at least one of the mandatory attendees 
      // is busy
      List<TimeRange> busy = new ArrayList<TimeRange> ();
      for (Event e : events) {
          Set<String> eventAttendees = e.getAttendees();
          
          boolean relevantEvent = false;
          for (String a : eventAttendees) {
              if (mandatory.contains(a)) {
                  relevantEvent = true;
                  break; // only need confirmation that one mandatory attendee is busy
              }
          }

          if (relevantEvent) {
              busy.add(e.getWhen());
          }
      }

      // no busy events for mandatory attendees means 
      // the entire day is free 
      if (busy.isEmpty()) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }

      // sort by both end and start time
      Collections.sort(busy, TimeRange.ORDER_BY_START);
      ArrayList<TimeRange> busyByEndTime = new ArrayList<TimeRange>(busy);
      Collections.sort(busyByEndTime, TimeRange.ORDER_BY_END);

      // meeting times that we'll eventually return as suggestion
      List<TimeRange> meetingTimes = new ArrayList<TimeRange>();

      int startTimeIndex = 0;
      int endTimeIndex = 0;

      int startTime = TimeRange.START_OF_DAY;
      int endTime = busy.get(endTimeIndex).start();
      
      int numBusyIntervals = busy.size();
      TimeRange latestMeeting = busyByEndTime.get(numBusyIntervals-1);
      int lastBusyTime = latestMeeting.end();

      // handle beginning of the day before busy intervals begin
      TimeRange possibleTime = TimeRange.fromStartEnd(startTime, endTime, false);
      if(possibleTime.duration() >= duration) {
            meetingTimes.add(possibleTime);
      }
      endTimeIndex++;

      // find all free times between scheduled events
      while (startTimeIndex < numBusyIntervals && endTimeIndex < numBusyIntervals) {
          startTime = busy.get(startTimeIndex).end();
          endTime = busy.get(endTimeIndex).start();

          if (startTime > endTime) {
              endTimeIndex++;
          } else if (startTime == endTime) {
              startTimeIndex++;
              endTimeIndex++;
          } else {
              possibleTime = TimeRange.fromStartEnd(startTime, endTime, false);
              if(possibleTime.duration() >= duration) {
                meetingTimes.add(possibleTime);
              }

              startTimeIndex++;
              endTimeIndex++;
          }
      }

      // check for time after all events are done
      possibleTime = TimeRange.fromStartEnd(lastBusyTime, TimeRange.END_OF_DAY, true);
      if(possibleTime.duration() >= duration) {
            meetingTimes.add(possibleTime);
      }

    return meetingTimes;
  }
}
