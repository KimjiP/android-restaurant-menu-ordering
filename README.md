# android-restaurant-menu-ordering

Note: this app is for a coursework assignment. It queries a server that is not personally maintained by me. It may not work anymore if the webmaster decides to deactivate this service.

This code includes:

1. Database Setup
2. Button initialization
3. Querying and modifying database entries
4. OnClick and OnLongClick events
5. Using Adapter and RecyclerView

See android-restaurant-menu-ordering/app/src/main/java/com/example/madclassproj/ for the java codes

PHOTOS:

## 1. Login Activity

User logs in

<img width="184" alt="login activity" src="https://user-images.githubusercontent.com/22336263/144447060-92b77db8-77a5-4f59-ab0c-d18973fa0d57.PNG">

## 2. Order Activity

User places order. Items will be populated from data sent by server.

<img width="181" alt="order activity" src="https://user-images.githubusercontent.com/22336263/144447063-32edf426-3b26-41f7-ac09-5ce2e4eff920.PNG">

## 3. Payment Activity

User inputs payment given by customer. Items ordered by customer will be populated from the data saved on local database. Customer can pay partially or in full, thus balance is displayed. When customer pays in full, he is considered to have checked out, and the table will be flagged as free (not occupied).

<img width="183" alt="payment activity" src="https://user-images.githubusercontent.com/22336263/144447065-77a0825e-2da2-4974-bff2-9c829bc124e2.PNG">

## 4. Tables Activity

The squares will display the table number. When a square is red, it means it is occupied, and is green when unoccupied.

<img width="184" alt="tables activity" src="https://user-images.githubusercontent.com/22336263/144447066-41f48477-ada5-4248-abf8-be8c95d71aa6.PNG">
