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
 * Part of the tutorial for week 3; retrieve response from Servlet 
 */
async function getHelloUsingAsyncAwait() {
  const response = await fetch('/data');
  const hello = await response.text();

  // When adding text to the website, remove the HTML tags from servlet output for readability 
  document.getElementById('hello-container').innerText = 
        hello.substring(4 /* removes <h1> from beginning of string */, 
                        hello.length-6 /* removes </h1> from end of string */);
}