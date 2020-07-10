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
   * the given duration. If optional attendees AND mandatory attendees can attend a 
   * meeting during a set of given times, those times are returned; otherwise, the 
   * query() call returns a list of times only the mandatory attendees can attend. In
   * the case where there are no mandatory attendees, optional attendees are treated
   * as though they are mandatory.
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

      long duration = request.getDuration();

      if (duration > TimeRange.WHOLE_DAY.duration()) {
          return Arrays.asList();
      } else if (events.isEmpty()) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }
      
      List<String> attendees = new ArrayList<String>(request.getAttendees());
      List<TimeRange> busyTimeRanges = findBusyTimes(events, attendees);

      // check for optional attendees' availability, if there are any
      if (!request.getOptionalAttendees().isEmpty()) {
          List<String> optional = new ArrayList<String>(request.getOptionalAttendees());
          attendees.addAll(optional);

          List<TimeRange> optionalBusyTimeRanges = findBusyTimes(events, attendees);
          List<TimeRange> optionalMeetingTimes = findMeetingTimes(optionalBusyTimeRanges, duration);
          
          if(!optionalMeetingTimes.isEmpty()) { // optional AND mandatory can make it
            return optionalMeetingTimes;
          } else if(request.getAttendees().isEmpty()) { // only optional attendees were requested 
            return Arrays.asList();                     // and none can make the meeting
          }
      }

      return findMeetingTimes(busyTimeRanges, duration);
  }

  /**
    * Finds the meeting times of time duration that don't fall during a busy time of day
    *
    * @param busyTimeRanges the times meeting members are unavilable
    * @param duration the length of the meeting to be scheduled
    *
    * @return a List of meeting times that work around busyTimeRanges.
    */
  private List<TimeRange> findMeetingTimes(List<TimeRange> busyTimeRanges, long duration) {
      if (busyTimeRanges.isEmpty()) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }
      
      Collections.sort(busyTimeRanges, TimeRange.ORDER_BY_START);
      List<TimeRange> busyByEndTime = orderByEndTime(busyTimeRanges);

      return findFreeTimes(busyTimeRanges, busyByEndTime, duration);
  }

  /**
    * Reorders a List of TimeRange objects by end time
    *
    * @param busy the List that is copied and reordered
    *
    * @return a copy of busy ordered by end time
    */
  private List<TimeRange> orderByEndTime(List<TimeRange> busy) {
      List<TimeRange> byEndTime = new ArrayList<TimeRange>(busy);
      Collections.sort(byEndTime, TimeRange.ORDER_BY_END);
      return byEndTime;
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
  private ArrayList<TimeRange> findBusyTimes(Collection<Event> events, List<String> mandatory) {
      ArrayList<TimeRange> busy = new ArrayList<TimeRange> ();

      for (Event event : events) {          
          boolean relevantEvent = false;
          for (String attendee : event.getAttendees()) {
              if (mandatory.contains(attendee)) {
                  relevantEvent = true;
                  busy.add(event.getWhen());
                  break; // only need confirmation that one mandatory attendee is busy
              }
          }
      }

      return busy;
  }

  /**
    * Finds the times in a day when mandatory attendees for a meeting are available for a given
    * duration.
    *
    * @param busyTimeRanges the times where at least one attendees is unavailable, sorted by start time
    * @param busybyEndTime the times where at least one attendees is unavailable, sorted by end time
    * @param duration the length of the new meeting to be added
    *
    * @return an ArrayList of TimeRange signifying when mandatory attendees are available for at least
    *         time duration.
    */ 
  private List<TimeRange> findFreeTimes(List<TimeRange> busyTimeRanges, List<TimeRange> busyByEndTime,
                                        long duration) {
      
      List<TimeRange> meetingTimes = new ArrayList<TimeRange>();

      int startTimeIndex = 0;
      int endTimeIndex = 0;
      
      int numBusyIntervals = busyTimeRanges.size();

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
      int lastBusyTime = busyByEndTime.get(numBusyIntervals-1).end();
      possibleTime = TimeRange.fromStartEnd(lastBusyTime, TimeRange.END_OF_DAY, true);
      if(possibleTime.duration() >= duration) {
            meetingTimes.add(possibleTime);
      }

      return meetingTimes;
  }
}
