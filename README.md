# Movie Database (Android App)

An Android application that pulls “Now Playing” movie data from The Movie Database (TMDB) Web API 
and displays it in a modern, responsive UI built with Jetpack Compose. The app uses an offline-first 
Room database to cache API data and persist user interactions such as likes and movie-associated photos.

# Demo
Not hosted. Run locally using the steps below in Android Studio.

# Features
- Displays now-playing movies from the TMDB API
- Offline-first behavior using a local Room database
- List and grid layouts using LazyColumn and LazyVerticalGrid
- Sorting by title, release date, or user rating
- Filtering to show all movies or liked movies only
- Movie detail screen with:
- Movie Poster image
- Title, release date, rating, and vote count
- Overview (if available)
- Persistent Like / Unlike functionality stored locally
- Camera integration to take and view photos associated with a movie
- User preferences (likes and photos) preserved across refreshes and app restarts
- Handles portrait and landscape orientation changes

# Tech Stack
- Platform: Android Studio Code
- Language: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM (Model–View–ViewModel)
- Persistence: Room Database
- Data/API: The Movie Database TMDB API

# Getting Started (Running Locally)
- Download Android Studio Code
- Create an Android device/emulator (API 28+ and require internet connection)
- Clone repository: git clone https://github.com/Hj4704/Movie-Database-Project.git
- Add a TMDB API key
    - Create an account at https://www.themoviedb.org
    - Generate an API key
    - Add the key in the MainActivity.kt class (where it asks for the apiKey)
- Add the following lines to the AndroidManifest.xml inside <manifest ...> (above <application>)
    - <uses-permission android:name="android.permission.INTERNET" />
    - <uses-permission android:name="android.permission.CAMERA" />
- 
