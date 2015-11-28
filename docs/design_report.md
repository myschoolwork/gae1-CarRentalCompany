#Design Report

## GAE Exercise 3.2

* When to use indirect: When confirming quotes. At this point the actual reservations are made and no double reservations may exist. So the confirming has to happen sequentially as to not interfere with other people that might also be confirming their reservations at the same time. This will take a long time if many user want to do it simultaneously, thus here indirect communication will be required.
* Passed data: Information about their current quotes and reservations are passed around. These aren't the actual object (Quote/Reservation), just information about them. Since the user-side is just a front-end, and doesn't actually do anything other than showing the information to the user, it doesn't make sense to pass references to the data as this would only make the app less safe.

## GAE Exercise 3.3

* 