## Programming Assignment
This repository contains a postgres-backed Spring Boot app for handling college registration. Specifically this app 
handles Courses, Degrees, and Students. Your task is to implement the Student API, and the most difficult part of this
will be calculating a Student's minimum number of semesters remaining based on their transcript and their Degree's 
requirements.

### The Data Model
The exact details of this app's data model can be found in src/main/resources/db/changelog/changes/schema.xml but 
briefly: 
* A Course has a name, prerequisites (0 to N Courses), meeting days (days of the week the class meets), start time, and 
end time. A Course's name uniquely identifies it.  
* A Degree has a name and a list of requirements (O to N courses). A Degree's name uniquely identifies it 
* A Student has a name, transcript (0 to N completed Courses), and major (a Degree). A Student's name uniquely 
identifies them. 

### The Student API
The Student API consists of two endpoints: 
```
PUT /student/{name}
{
    "transcript": ["CLASS1", "CLASS2",... "CLASSN"], 
    "major": "The Name Of Some Degree"
}
``` 
```
GET /student/{name} //Returns: 
{
    "name": "name"
    "major": "The Name Of Some Degree",
    "transcript": ["CLASS1", "CLASS2",... "CLASSN"], 
    "semestersRemaining": 3
}
```

The most complicated part of this API is the `semestersRemaining` field on the `GET /student/{name}` response. This
field represents the minimum number of semesters remaining for the student to earn their degree. Additionally:
* `semestersRemaining` should be 0 if the Student has already completed all of the courses required by their degree. 
* `semestersRemaining` should be -1 if it is impossible for the Student to complete their degree.

When implementing the logic for `semestersRemaining` you may make the following assumptions:
* A Student can take as many courses each semester as they want. 
* A Student can't take two courses in the same semester if their times overlap e.g. If one course is from 13:00 to 15:00
on Mondays and the other is from 14:30 to 15:30 Monday, Wednesday, Friday, then the two courses can't be taken in the 
same semester because they overlap from 14:30 to 15:00 on Monday. 
* A Course's end time is exclusive. E.g. A course from 14:00 to 15:00 on Monday and another course from 15:00 to 16:00
on Monday **do not overlap** 
* A Course cannot be scheduled over midnight. I.e. A Course's start time is always before its end time. 
* If CourseA is a prerequisite to CourseB then they can't both be taken in the same semester. 
* If CourseA is a prerequisite to CourseB then CourseA must be taken before CourseB may be taken.

Scenarios where it is impossible for a Student to complete their degree (i.e. cases where we return -1): 
* The Student doesn't have a Degree
* A Degree has two requirements which are directly or transitively prerequisites of each other AND the student has not 
completed any of these requirements. E.g. if a Degree requires the courses: CourseA, CourseB, and CourseC; and then
CourseA has prerequisite CourseB has prerequisite CourseC has prerequisite CourseA, then the Student can only complete 
their degree if they already have one of these three courses in their transcript.
 
### Examples 
Take a look at the test cases in 
src/test/java/com/example/registration/controller/StudentControllerIntegrationTest.java. It will be helpful for you to
run these test cases early and often as you implement the Student API. Additionally, the rest of the app is well tested
and it would be a good idea to occasionally run the full test suite to make sure that everything is still working 
properly. 

### Running The App

To run app: 
```
mvn clean install
docker-compose build
docker-compose up
```

To shut down (important even if you use ctrl+c)
```
docker-compose down
```

To run just the database: (Helpful for debugging in the IDE) 
```
docker-compose up registration_db
```

Finally, Swagger UI is configured for this app and can be accessed (when the app is running) at 
http://localhost:8080/swagger-ui.html  