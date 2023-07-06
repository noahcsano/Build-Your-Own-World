# Build-Your-Own-World

UC Berkeley CS61B - "Data Structures and Algorithms" Project 

Language: Java
Libraries/Packages: StdDraw, java.io.Serializable, java.io.Array, java.io.Random, and other provided utility libraries.

For this project, we implemented an engine for generating random 2-dimensional explorable worlds. 
The requirements for the outputs of the random worlds were the following:
  The world must be a 2D grid, drawn using our tile engine. 
  The world must be pseudorandomly generated. 
  The generated world must include rooms and hallways, though it may also include outdoor spaces.
  At least some rooms should be rectangular, though you may support other shapes as well.
  Your game must be capable of generating hallways that include turns (or equivalently, straight hallways that intersect).
  The world should contain a random number of rooms and hallways.
  The locations of the rooms and hallways should be random.
  The width and height of rooms should be random.
  The length of hallways should be random.
  Rooms and hallways must have walls that are visually distinct from floors. Walls and floors should be visually distinct from unused     
  spaces.
  Rooms and hallways should be connected, i.e. there should not be gaps in the floor between adjacent rooms or hallways.
  The world should be substantially different each time, i.e. you should not have the same basic layout with easily predictable features

The project relies on the StdDraw API which provides a basic capability for creating drawings with your programs. (Standard drawing also includes facilities for text, color, pictures, and animation, along with user interaction via the keyboard and mouse.)
This API was primarily helpful for generating a random world based on a certain algorithm and displaying an avatar that can be controlled throughout the map using keyboard inputs. 

The game also supports a saving and loading feature which allows users to save/quit the game and pick up from where they ended by loading the previously played game. I implemented this through serializing the game state(including the tiles, avatar, size of world, and etc. objects) and storing it into a separate file. When loading, we need to deserialize the game state, retrieve, and load the objects into a new world.
