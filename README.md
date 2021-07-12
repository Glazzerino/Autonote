Autonote - FBU project
===
## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
Autonote is a note-taking, note-scanning application with students in mind.

### Description
The app makes use of text recognition, keyword detection and chronological order to compile and automatically organize notebook, both scanned and digital, for easy visualization. It also enables the user to  share their notes with more people and export it to a desktop-friendly format.

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Educational/Assistive.
- **Mobile:** Android app. At first it would be a mobile-only app.
- **Story:** User logs into the app and goes directly to the app activity to scan a notebook of theirs during class hours. Later, when the user wishes to review their notes, they log in again and searches by topic and keywords to find all notes available about a specific topic to prepare for a big exam.
- **Market:** Students, hobbyists.
- **Habit:** People can use the app for both data collection and consulting. Users get used to serach or navigate the UI based on the topic classification features.
- **Scope:** It is an note organizer meant to facilitate and automate document organization and classiffication, with the intention of removing this preocupation from the user's mind while facilitating retrieval.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Main Screen : The user is greeted with their latest notes, a search bar, and a big button meant to carry them to the phyisical page scanner screen.
* Scanning screen: The user points their smartphone towards the pages they wish to digitize, and subsecuent scans are saved to internal storage for subsequent cloud uploading and processing.
* Search screen : The user types in keywords and/or topics. The app responds with a list of query results that point to notes saved on their account.
* Note explorer : notes are organized by topic, presented in chronological order upon topic specification.
* User can login
* User can create a new account
* User can view their profile and a summary of their account

**Optional Nice-to-have Stories**

* A topic-centric discovery page
* User can discover or share other people's notes.
* User can search for posts and other users
### 2. Screen Archetypes

* Login
    * User can login
* Account creation
    * User can create a new account
* Content creation
    * User can scan and write notes.
* Stream
    * User can discover notes of other people.
* Search
    * User searches for notes based on text content, topic and keywords.
### 3. Navigation

**Tab Navigation** (Tab to Screen)
* Home Feed
* Scanner
* Note explorer

**Flow Navigation** (Screen to Screen)

* View post
* Create post
* View user profile

## Wireframes
> Homescreen
![](https://i.imgur.com/tenvHf7.png)

> Scanner
![](https://i.imgur.com/j9qnVpM.png)

> Note explorer
![](https://i.imgur.com/AwPHKZC.png)

> Content-specific explorer
 ![](https://i.imgur.com/IhNdDiR.png)

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema
[This section will be completed in Unit 9]
### Models
* User
    * username: string
    * uid: long int
    * name: string
    * email: string
    * password: string (hashed)
    * collections: long int (list)
* Note
    * topic: string
    * transcription: string
    * source: image
    * keywords: list of strings
    * user_id: long int
    * note_id: long int
    * collection_id: long int
* Collection
    * user_id: long int
    * collection_id: long int
    * notes: long int (list)
    * name: string
### Networking
