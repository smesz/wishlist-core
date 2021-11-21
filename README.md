# wishlist-core app

## Description

Application that acts as an main component for creating users and manages the users' wishlists.

## Tech stack

1. Spring Boot with web-flux
2. Kotlin language
3. Postgresql with R2DBC driver
4. Testing:
    1. junit 5
    2. kotest + mockk

## Pros

1. non-blocking API provides better performance when dealing with large amount of parallel active users
2. Kotlin enables us to write more precise code with less boilerplate code

## Cons

1. by using R2DBC, we are losing relationships between entities (maybe it will be changed in the future). So there are
   no relations between entities - we have to do a lot manually
2. code may not look readable and it's hard to write for beginners
