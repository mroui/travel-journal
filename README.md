<p align="center">
    <img src="./app/src/main/res/drawable/logo.png" alt="Travel Journal logo icon" width="120"/>
</p>
<h1 align="center">
	Travel Journal
</h1>

<p align="center">
    <b>
    🧭📝🌎
    <br/>
    #Travel #Journal #Itinerary #Android #MobileApp #SocialApp
    </b>
</p>

> Mobile application dedicated to people, who want to travel in an organized way, to be able to entirely enjoy the previously planned journeys, possessing all necessary data in one place. 
It's an advanced equivalent of a travel journal with social functions.

*Application was made as a part of my engineer's thesis.*

## Table of contents
* [Introduction](#introduction)
* [Presentation](#presentation)
* [Assumptions](#assumptions)
* [Requirements](#requirements)
* [Tools&Technologies](#tools&technologies)
* [Database](#database)
* [Features](#features)
* [Screenshots](#screenshots)
* [Setup](#setup)
* [License](#license)

## Introduction

The collected data will be used to create final document, which could be an album with memories, travel journal or to other users an itinerary for their own trips. This will contribute to the creation of a community that shares itineraries with others and uses these already shared.

The main assumption of the application is to enable the consumer to conveniently and easily coordinate, document and plan travel with quick access to the necessary functions and data, which would help to enjoy the trip in a pleasant and clear way.

## Presentation

## Assumptions
#####Objectives:
 * :star2: **Development of an mobile application** for a user, who wants to travel in an organized way, to have all the content and needed informations in one place, f.e.: about planned trip, attractions, interesting places, photos and notes, weather forecast, etc.
  * :star2: **Creation of a document** based on travel's data that can be used by others as an itinerary for own trips
  * :star2: **Creation of a community** that shares itineraries with others

#####Range:
 * :star: **Analysis of functionalities** needed by the user for organized traveling
 * :star: **Analysis of technologies and tools** which are necessary in the project
 * :star: **Analysis of data** that will be shared
 * :star: **Design a database** for stored data
 * :star: **Design a template** for sharing travel documents with other users

## Requirements
* Android 4.4 or newer
* Android Emulator or Android device (highly recommended)
* Modules and permissions:
    * Internet connection 
    * For some functionalities GPS module with granted permissions to detect location
    * Granted permissions with read and write from/to memory to upload and download files
    * Granted permission to receive notifications for the alarm function. 

## Tools&Technologies
* Java 1.8
* Android Studio 3.6.3
* Gradle 3.6.3
* Firebase Platform
    * Firebase Authentication
    * Cloud Firestore
    * Cloud Storage
* Google Maps Platform
    * Maps SDK for Android
    * Places API
* Android Architecture Components
    * View Binding
    * LiveData
    * Data Binding Library
* Material Design
* Model-view-ViewModel (MVVM)
* [OpenWeatherMap](https://openweathermap.org/current) | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) & [ODbL](https://opendatacommons.org/licenses/odbl/)
* [Exchange rates API](https://github.com/exchangeratesapi/exchangeratesapi) | MIT
* [Yandex.Translate](https://tech.yandex.com/translate/doc/dg/concepts/about-docpage) | Yandex Search Engine [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)
* [Bubble Navigation](https://github.com/gauravk95/bubble-navigation) | ver. 1.0.7 | Apache 2.0
* [CircularImageView](https://github.com/lopspower/CircularImageView) | ver. 4.1.1 | Apache 2.0
* [HashtagView](https://github.com/greenfrvr/hashtag-view) | ver. 1.3.1 | MIT
* [Nachos](https://github.com/hootsuite/nachos) | ver. 1.1.1 | Apache 2.0
* [Material Spinner](https://github.com/jaredrummler/MaterialSpinner) | ver. 1.3.1 | Apache 2.0
* [Compressor](https://github.com/zetbaitsu/Compressor) | ver. 2.1.0 | Apache 2.0
* [Android Image Cropper](https://github.com/ArthurHub/Android-Image-Cropper) | ver. 2.8.0 | Apache 2.0
* [Glide](https://github.com/bumptech/glide) | ver. 4.11.0 | [License](https://github.com/bumptech/glide/blob/master/LICENSE)
* [LoopingViewPager](https://github.com/siralam/LoopingViewPager) | ver. 1.2.0 | MIT
* [ColorPicker](https://github.com/kristiyanP/colorpicker) | ver. 1.1.10 | Apache 2.0
* [BoomMenu](https://github.com/Nightonke/BoomMenu) | ver. 2.1.1 | Apache 2.0
* [Retrofit](https://square.github.io/retrofit) | ver. 2.5.0 | Apache 2.0
* [Gson](https://github.com/google/gson) | ver. 2.8.6 | Apache 2.0

## Features
##### :last_quarter_moon_with_face: Creating an account, logging in, registration
* Creating an account, hereinafter referred to as an Traveler's account or profile:
    * By e-mail address and password, additionally specifying the username
    * By Google account
* After registering with an e-mail address and password, account verification is required. The message with a link for verification will be sent to the provided e-mail
* If you forget your password, you can send a message to an e-mail address with a link to set a new one
* Logging into a previously created account
* Save login details by SmartLock service. 

##### :last_quarter_moon_with_face: Traveler profile
* Basic profile information preview: profile icon, username, short description, location, preferences - considering privacy settings of data visibility
* Possibility to send e-mail messages - only in case of allowed access to the someone's e-mail address
* Access to the list of incoming notifications such as: friendship invitation (the ability to accept and add someone to the friends list or decline the invitation) and preview of friend's travel status
* Possibility to send an invitation to the friends list - only in the case of a profile other than your own and no connection to a given profile (users are not friends)
* Access to the list of friends with the option of visiting selected profile or removing a person from the list (only in case of own profile)
* Access to the list of user's own trips and those saved with the possibility of previewing the trip or deleting a given entity (only in case of own profile)
* Access to the settings (only in case of own profile)
* Logging out (only in case of own profile) 

##### :last_quarter_moon_with_face: Settings
* Account, Traveler profile settings with the possibility of changing: profile icons, short description, location, preferences list, username, e-mail address, password, privacy settings (e-mail address, location, preferences - public, only for friends or private)
* Access to information about the author and version of the application
* List of libraries and solutions used with appropriate licenses and links
* Access to contact with technical support. 

##### :last_quarter_moon_with_face: Trip preview
* Overview of basic information about the trip: name, reference photo, destination, date of departure and return, owner (with a link to the profile), tags
* Possibility to download the travel plan document (pdf)
* Possibility to save a trip - only if it's not a logged in user trip. 

##### :last_quarter_moon_with_face: Board, current trip, handy tools
* Quick access to functions:
    * Map and nearby places
    * Weather forecasts
    * Translator
    * Currency converter
    * Alarm with notifications
* In the absence of an active trip - the possibility of creating a new one
* For an existing active trip:
    * Access to the packing list with the possibility of: adding a new item to a selected category, adding a new category, marking already packed items, deleting items with categories and ending packing
    * Ability to preview, add, edit and delete: notes, photos with description, visited places with description and rating
    * The ability to manage the budget and add new expenses by category
    * Possibility to discover nearby places
    * Access to basic travel informations: name, reference photo, destination, date of departure and return, transport and accommodation (type, possible contact details and the option to download files, e.g. booking) and tags
    * Possibility to edit information: trip name, photo and tags
    * Ability to evaluate the day of the trip
    * Ability to end the trip - complete the description, set the sharing options and create a travel plan document. 

##### :last_quarter_moon_with_face: Creation of a new journey
* Adding basic information: name, pictorial photo, date of departure and return with hours, destination, transport and accommodation (type, possible contact details and files, e.g. booking), budget and tags
* Ability to set an alarm with a reminder for the date and time of departure.

##### :last_quarter_moon_with_face: Home
* Ability to discover travel plans shared by others
* Possibility to search for travel plans: by keywords with the possibility of filtering (by duration, destination and selected tags) and sorting the results (by popularity, date added and duration of trips)
* Access to the map with the possibility of: discovering, searching and adding dream places to visit along with short notes in order to plan future trips - pins are placed on the map, which can be read (place name, address and a possible note) and deleted
* Ability to search for friends by username. 

## Database
## Screenshots
[Move on quickly to setup](#setup)
<p align="center">
	<img src="./resources/screenshots/screenshot_travel_journal_01.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_02.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_03.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_04.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_05.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_06.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_07.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_08.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_09.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_10.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_11.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_12.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_13.png" alt="Travel Journal app screenshot"/>
	<img src="./resources/screenshots/screenshot_travel_journal_14.png" alt="Travel Journal app screenshot"/>
</p>


## Setup