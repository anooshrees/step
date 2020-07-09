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
  /**
   * Handles the "find a meeting" feature by providing the user with a set of times
   * where all given meeting attendees have space in their schedule for a meeting of
   * the given duration
   *
   * @param events all of the events happening on the given day, each of which have
   *               a list of attendees and a TimeRange
   * @param request the details of the meeting being requested; contains a list of
   *                mandatory and optional attendees and the meeting length.
   *
   * @return an ArrayList containing all of the TimeRanges where the meeting represented
   *         by request can be scheduled
   *
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      // meeting request has: name, duration, and attendees (List of Strings)
      // event has: time range, attendees, and name

      long duration = request.getDuration();

       // meeting request exceeds one day
      if (duration > TimeRange.WHOLE_DAY.duration()) {
          return Arrays.asList();
      }

      // all attendees are free or there are no attendees
      if (events.isEmpty() || request.getAttendees().isEmpty()) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }

      // retrieve all the times when at least one of the mandatory attendees 
      // is busy
      List<TimeRange> busyTimeRanges = findBusyTimes(events, request.getAttendees());

      // no busy events for mandatory attendees means 
      // the entire day is free 
      if (busyTimeRanges.isEmpty()) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }

      // sort by both end and start time
      Collections.sort(busyTimeRanges, TimeRange.ORDER_BY_START);
      ArrayList<TimeRange> busyByEndTime = new ArrayList<TimeRange>(busyTimeRanges);
      Collections.sort(busyByEndTime, TimeRange.ORDER_BY_END);

      // meeting times that we'll eventually return as suggestion
      List<TimeRange> meetingTimes = new ArrayList<TimeRange>();

      int startTimeIndex = 0;
      int endTimeIndex = 0;
      
      int numBusyIntervals = busyTimeRanges.size();
      TimeRange latestMeeting = busyByEndTime.get(numBusyIntervals-1);
      int lastBusyTime = latestMeeting.end();

      // handle beginning of the day before busy intervals begin
      TimeRange possibleTime = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, 
                                                      busyTimeRanges.get(endTimeIndex).start(), false);
      if(possibleTime.duration() >= duration) {
            meetingTimes.add(possibleTime);
      }
      endTimeIndex++;

      // find all free times between scheduled events
      while (startTimeIndex < numBusyIntervals && endTimeIndex < numBusyIntervals) {
          int startTime = busyTimeRanges.get(startTimeIndex).end();
          int endTime = busyTimeRanges.get(endTimeIndex).start();

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


  /**
    * Finds the times in a day when mandatory attendees for a meeting are unavailable.
    *
    * @param events the events happening that day
    * @param mandatory the attendees whose attendance is required at another meeting
    *
    * @return an ArrayList of TimeRange signifying when mandatory attendees are at another
    *         event.
    */
  private ArrayList<TimeRange> findBusyTimes(Collection<Event> events, Collection<String> mandatory) {
      ArrayList<TimeRange> busy = new ArrayList<TimeRange> ();

      for (Event event : events) {          
          boolean relevantEvent = false;
          for (String attendee : event.getAttendees()) {
              if (mandatory.contains(attendee)) {
                  relevantEvent = true;
                  break; // only need confirmation that one mandatory attendee is busy
              }
          }

          if (relevantEvent) {
              busy.add(event.getWhen());
          }
      }

      return busy;
  }
}
