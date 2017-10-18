### Project 1 - Stage 2 (Popular Movies App)
---
##### Android Developer Nanodegree - Udacity
---


### UWatch

Popular movies App that displays information from TheMovieDB.org on the Top 20 Most Popular or Top Rated movies (according the criteria selected by the user). It also contains a section to display the user's "Favorite" movies.

###### These are the different features you will find on this app:

1 - When you first access the app's main screen, you will see a circular progress bar indicating data is being fetched. When data has been received and it starts to load, a custom placeholder image will be shown on each spot where there will be a movie poster but it will fade out to show the actual poster after it's completely loaded.

2 - Movie posters are displayed on a grid. The number of columns will adjust automatically depending on the device width and orientation.

3 -  You can select the sorting criteria you would like to browse through by clicking or tapping on the spinner that shows the current selection. A dropdown menu will be displayed with the options available. Options are: "Most Popular" movies, "Top rated" movies or "Favorites". 

The user interface will update automatically to respond to this new selection.

4 - Once you've browsed through the posters and found a movie that you would like to learn more about, clicking or tapping on a poster will take you to a movie details screen where you can find additional data like the movie's poster, title, release year, user average rating, plot, cast, trailers and reviews. 

5 - If you click on a trailer thumbnail, an app will be launched to play its corresponding youtube video.

6 - If you click on "Read Reviews", a new screen will be displayed with the movie reviews. If there aren't any, a dialog will warn the user and return automatically to the details screen.

You can always go back to the posters section and select a new movie.

---
#### WARNING: To use this app you need an API key from [TheMovieDB API](https://www.themoviedb.org/).
There is a comment on the NetworkUtils class indicating where you need to include a constant for your API key in String format.

----

#### Special features
---

- The user can select a movie as a "Favorite" in the movie's details activity by clicking on a floating action button with a heart symbol. This selection can be toggled with the same button. 

- On device rotation on the main posters grid, the scroll position will be maintained so the user doesn't have to scroll down again to find the movie he/she was interested in.

- The movie details section has a collapsing toolbar that will show the movie's title in a smaller font for better user experience when scrolling down. 

<br>

---

#### Technical features
---

- Movie posters are loaded using the [Picasso](http://square.github.io/picasso/) library that handles image loading.

- The user's favorite movies data is saved to an SQLite database through a Service, including images (poster, backdrop, trailer thumbnails). Images are first downloaded through Picasso's `.get( )` method and then they are saved to internal storage and their path is updated in the database.

- The user interface is responsive. A custom layout for the main and details activities is displayed for devices with a smallest width of 600dp. 

- Images contain a contentDescription tag for accessibility.

<br>

---
#### Handling errors
---

- **No internet connection:**  If there is no internet connection available, a dialog box will alert the user. If the app has loaded data previously, this data will remain responsive but the user won't be able to request new data until there is an internet connection.  The "Favorites' category will be displayed automatically when there is no internet connection.

    If the user had no internet connection and no data was loaded previously but he/she reconnects and restarts the app, data will be fetched automatically.

- **Poster didn't load correctly:** An error poster will be shown instead. On the main screen, the error poster will prompt the user to click if he/she wants to access the movie data, even if the poster is not available. Once inside the movie details screen, a second error poster will display a message saying that the poster couldn't be loaded. 

- **No favorite movies selected:** If the user tries to access the "Favorites" section without selecting favorite movies first, a dialog will warn hum/her and the "Most Popular" section will be selected automatically so the user can select favorite movies.

- **No reviews available:** A dialog with be displayed and the user will be taken automatically to the corresponding movie details activity.

<br>

---

##### Attributions
---

- This app is powered by [TheMovieDB API](https://www.themoviedb.org/) which provides movies data and posters

- Images used to create the custom error and loading placeholders were taken from [Pixabay.com](https://pixabay.com/) under creative commons license

    - [Popcorn](https://pixabay.com/en/popcorn-buttered-cinema-corn-food-155602/)
    - [Movie roll](https://pixabay.com/en/filmstrip-film-frames-camera-film-33429/)


- StackOverflow forums were extremely helpful during this project.

- The  isNetworkAvailable method in NetworkUtils class was based on an answer from this [StackOverflow discussion](https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android)  

- Found for the Collapsing Toolbar in Android documentation and [this blog](http://blog.grafixartist.com/toolbar-animation-with-android-design-support-library/)

- Spinner information found in StackOverflow forums

- Column number is set programatically using the [method found on this post](https://stackoverflow.com/questions/33575731/gridlayoutmanager-how-to-auto-fit-columns)

-  Gradient drawable based on [this post](https://stackoverflow.com/questions/32989851/how-can-i-use-a-color-as-placeholder-image-with-picasso)

- Images are saved to internal storage with a method based on these posts

   - [How to save and retrieve images to internal storage](https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android)

   - [How to save bitmap on internal storage download from internet](https://stackoverflow.com/questions/19978100/how-to-save-bitmap-on-internal-storage-download-from-internet)

   - [Send Bitmap using intent Android
](https://stackoverflow.com/questions/11010386/send-bitmap-using-intent-android)

- Method to save arrays to an SQLite database was based on [this post](https://stackoverflow.com/questions/9053685/android-sqlite-saving-string-array)

- Picasso's `.get( )` method used from [this comment by laobie](https://github.com/square/picasso/issues/227)

- Libraries used:
    - [Picasso](http://square.github.io/picasso/)
    - [ExpandableTextView](https://github.com/Manabu-GT/ExpandableTextView)
