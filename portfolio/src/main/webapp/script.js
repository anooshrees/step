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

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts =
      ['I have visited every continent except Antarctica!', 'I taught myself how to watercolor!', 'In high school, I was on the varsity lacrosse team!', 'I speak fluent French after 8 years of classes!'];

  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/***
 * Highlights the user-selected element in the top navigation bar
 */
function makeResponsive() {
  var currentElement = document.getElementById("myTopnav");
  if (currentElement.className === "topnav") {
    currentElement.className += " responsive";
  } else {
    currentElement.className = "topnav";
  }
}

/**
 * Part of the tutorial for week 3; retrive comments stored in a JSON
 * object and include them in HTML for the site.
 */
function getComments() {
  document.getElementById('comments-container').innerHTML = "";

  fetch('/data').then(response => response.json()).then((comments) => {
        const commentsListElement = document.getElementById('comments-container');
        comments.forEach((comment) => {
            commentsListElement.appendChild(createListElement(comment.content));
        })
    });
}

/**
 * Week 3 task to limit the number of comments displayed by accessing data
 * from a query URL.
 */
function limitComments(){
    document.getElementById('comments-container').innerHTML = "";

    fetch('/data?num-comments='+document.getElementById('num-comments').value)
          .then(response => response.json()).then((comments) => {
            const commentsListElement = document.getElementById('comments-container');
            comments.forEach((comment) => {
                commentsListElement.appendChild(createListElement(comment.content));
            })
        });
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

/**
 * Delete all of the comments using the DeleteDataServlet to empty
 * the datastore
 */
function deleteComments() {
  fetch('/delete-data', {method: 'POST', body: new URLSearchParams()})
  .then(response => response.text()).then(() => {
      getComments();
  });
}

var map;
function initMap() {
  map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: -34.397, lng: 150.644 },
    zoom: 8
  });
}