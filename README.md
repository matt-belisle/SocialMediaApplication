# SocialMediaApplication
This is a simple social media application based off of AusPolData2019 data set from Kaggle.

To run first make sure you have docker and docker-compose installed

Then run `./gradlew shadowJar`

Then unzip the database backup in the Database folder (keep the dump inside this folder)

Then run `docker-compose up`, this should launch the application

go to `localhost:80` in your browser to see the website

All of the api endpoints have examples inside of the postman export.
