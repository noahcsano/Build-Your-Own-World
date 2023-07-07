# Project 3 Prep

**For tessellating hexagons, one of the hardest parts is figuring out where to place each hexagon/how to easily place hexagons on screen in an algorithmic way.
After looking at your own implementation, consider the implementation provided near the end of the lab.
How did your implementation differ from the given one? What lessons can be learned from it?**

Answer:

-----

**Can you think of an analogy between the process of tessellating hexagons and randomly generating a world using rooms and hallways?
What is the hexagon and what is the tesselation on the Project 3 side?**

Answer:
In project 3 we will need to generate indoor spaces (rooms, hallways). This is represented by 
the tile design in this lab. Additionally, spacing out the tiles and organizing them will 
correlate to how rooms and such are randomly added/branched out in project 3.
-----
**If you were to start working on world generation, what kind of method would you think of writing first? 
Think back to the lab and the process used to eventually get to tessellating hexagons.**

Answer:
I would start out by writing methods to simply build a room or a hallway given a simple set of 
parameters (define basic space). Following the procedure I used in this lab, I would slowly 
build up from this to add additional "levels" of design and slowly expand the complexity.
-----
**What distinguishes a hallway from a room? How are they similar?**

Answer:
A hallway is a row/space that connects two rooms. It only has an entry and exit. It has a 
straight, rectangular shape. A room can have multiple exits and basically any geometric shape. 
They could be similar in that they have exits and that they both branch out to new locations.