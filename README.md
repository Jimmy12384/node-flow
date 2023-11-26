
# Node Flow

*Node Flow* is a visual design project that simulates particles in a flowing force 
field. As particles approach other particles, lines are formed when within a close distance.
When three particles are within proximity, the area is shaded in to show a deeper 
connection. 

Quad Trees are used to efficiently calculate nearby particles every frame using a 
local search. Meanwhile, Movement is controlled by a vector field generated using 
perlin noise. 


## Run Locally

Clone the project

```bash
  git clone https://github.com/Jimmy12384/node-flow.git
```

Go to the project directory

```bash
  cd node-flow
```

Install dependencies

```bash
  mvn clean install
```

Start the application from the JAR

```bash
  java -jar target/node-flow-1.0.0.jar
```

