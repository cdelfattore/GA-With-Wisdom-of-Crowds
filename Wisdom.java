/* Assignment: Project 4 - TSP – Wisdom of the Crowds
** Name: Chris Del Fattore
** Email: crdelf01@cardmail.louisville.edu
** Description: Implementation of a Genetic algorithm with Wisdom of the Crowds logic for TSP Problem
*/
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Wisdom {
	public static Map<Integer,Point> points; //map of points
	public static Map<Integer,Map<Integer,Double>> edgeLengths; //map of edge lengths for all points
	public static Random rnd; //Random number

	public static void main(String[] args) throws IOException {

		//Get the points from the input file
		//The Below list is used to store the point information from the input file
		points = new HashMap<Integer,Point>();

		//Takes the filename as a parameter. File contains points and the x and y cooridnates.
		String filename = args[0];

		//BufferedReader used to read input from a file
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		//pattern is the regular expression used to parse throught the input file and find the point number and the point's x and y value.
		//The pattern will find all of the points in the file
		String pattern = "(?m)^\\d+\\s\\d+\\.\\d+\\s\\d+\\.\\d+";
		Pattern r = Pattern.compile(pattern);

		String value = null;

		//the below while loop with go through the file line by line and see if a match has been made with the regular expression.
		//If a match is made, the line is parsed, retrieving the piont name, x and y coordinate values
		//the points are saved in the points list.
		while((value = reader.readLine()) != null){
			Matcher m = r.matcher(value);
			if(m.find()) {
				//add the point to the List of points
				Point p = new Point(Integer.parseInt(value.split(" ")[0]), Double.parseDouble(value.split(" ")[1]), Double.parseDouble(value.split(" ")[2]));
				points.put(p.name,p);
			}
		}

		/*for(Integer p : points.keySet()){
			System.out.println(points.get(p).name + " " + points.get(p).x + " " + points.get(p).y);	
		}*/

		//Pre-calculate the length of all of the edges
		//Map to store the edgeLengths for all of the edges
		edgeLengths = new HashMap<Integer,Map<Integer,Double>>();

		//simple array of intergers used to create random populations
		//will initally be filled with the values of all of the points
		int[] path = new int[points.size()];

		//Iterate through all of the points
		//create all the permutaions of edges
		//find their distance
		for(Integer a : points.keySet()){
			//System.out.println(a + " " + points.get(a).x + " " + points.get(a).y);
			path[a-1] = a;
			Map<Integer,Double> listDoubles = new HashMap<Integer,Double>();
			for(Integer b : points.keySet()){
				if(a==b) {
					listDoubles.put(b,Double.MAX_VALUE);
				}
				else {
					listDoubles.put(b,computeDistance(points.get(a),points.get(b)));	
				}
			}
			edgeLengths.put(a,listDoubles);
		}

		//Print out the edge lengths
		/*for(Integer i : edgeLengths.keySet()) {
			for(Integer j : edgeLengths.get(i).keySet()){
				System.out.println(i + "-" + j + " " + edgeLengths.get(i).get(j));
			}
		}*/
		//Print the contents of the path array
		/*for(int i = 0; i < path.length; i++){
			System.out.println(path[i]);
		}*/

		//Random Number obj
		rnd = new Random();

		//Create  random array of integers
		List<int[]> populations = new ArrayList<int[]>();
		populations.add(shuffle(path));

		//List of Paths with distances
		List<Path> lPaths = new ArrayList<Path>();
		//create 10 arrays of random intergers
		for(int i = 1; i <= 32; i++){
			populations.add(shuffle(populations.get(i-1)));
			Path tmp = new Path(populations.get(i-1));
			lPaths.add(tmp);
		}

		//Print out paths to check and see that they are random.
		//The Paths are Random
		/*for(Path p : lPaths){
			System.out.println(p.pathList);
		}*/

		//order the lPaths array in order to find the best parents
		//lpaths will now be empty

		// int popSize = Integer.valueOf(args[3]); //popSize will be the number of paths to carry on through each generation
		int popSize = 32; //popSize will be the number of paths to carry on through each generation
		ArrayList<Path> orderPaths = sortPathList(lPaths,popSize);
		
		/*for(Path p : orderPaths){
			System.out.println(p.pathList + " " + p.dist);
		}*/

		//Genetic algorithm
		//do k generations of cross overs and mutations
		//args array gets input from the GUI.java class
		/*int k = Integer.valueOf(args[1]);
		int numCrossNodes = Integer.valueOf(args[2]);
		int mutateRate = Integer.valueOf(args[4]);*/
		int k = 1000;
		int numCrossNodes = (int)points.size() / 5;
		int mutateRate = 10;
		for(int i = 0; i < k; i++){
			int initOrderPathSize = orderPaths.size()-1;
			//j represents the smaller paths in the array, only use the first half of these
			//could change this to a fixed number
			for(int j = 0; j <= initOrderPathSize; j++){ 
				//h represents all of the nodes in the list,
				for(int h = 0; h <= initOrderPathSize; h++){ 
					if(h != j)	{ //if to prevent combining the same path with itself
						if(compareArrayList(orderPaths.get(j),orderPaths.get(h)) == false || orderPaths.get(j).dist != orderPaths.get(h).dist){
							//Create a child path by calling the create child path.
							//System.out.println("Same Path");
							Path tmp = createChildPath(orderPaths.get(j),orderPaths.get(h),numCrossNodes,mutateRate);
							//add the new path to orderPaths, will sort the entire list after the for loop terminates
							orderPaths.add(tmp);	
						}
						//if the paths are the same mutate them
						//this is an improve on the genetic algorithm implementation from project 4
						else {
							orderPaths.get(j).swap();
							orderPaths.get(h).swap();
							Path tmp = createChildPath(orderPaths.get(j),orderPaths.get(h),numCrossNodes,mutateRate);
							//add the new path to orderPaths, will sort the entire list after the for loop terminates
							orderPaths.add(tmp);	
						}
					}
				}
			}
			Double previousDistance = 0.0;
			for(int ka = 0; ka < orderPaths.size(); ka++){
				if(orderPaths.get(ka).dist == previousDistance){
					orderPaths.get(ka).swap();
				}
				previousDistance = orderPaths.get(ka).dist;
			}

			//sort the orderPaths list so the smaller length paths are at the beginning of the list
			orderPaths = sortPathList(orderPaths,popSize);

			/*for(Path p : orderPaths){
				System.out.println(p.pathList + " " + p.dist);
			}
			System.out.println();*/
			//System.out.println(orderPaths.get(0).dist);
		}
		/*for(Path p : orderPaths){
			//System.out.println(p.pathList + " " + p.dist);
			System.out.println(p.dist);
		}
		System.out.println();*/


		/*for(Path p : orderPaths){
			System.out.println(p.pathList + " " + p.dist);
			//System.out.println(p.dist);
		}
		System.out.println();*/

		// for(Path p : orderPaths){
		// 	System.out.println(/*p.pathList + " " +*/ p.dist);
		// }
		/*System.out.println();
		Boolean foo = compareArrayList(orderPaths.get(0),orderPaths.get(1));
		System.out.println(foo);*/

		//Wisdom of the crowd logic

		//first search through the orderPath Arraylist and grab all the different paths
		//store them in a new arraylist
		List<Path> uniquePaths = new ArrayList<Path>();
		List<Double> uniqueDistances = new ArrayList<Double>();
		for(Path p : orderPaths){
			if(!uniqueDistances.contains(p.dist)){
				uniqueDistances.add(p.dist);
				uniquePaths.add(p);
			}
			if(uniquePaths.size() == 6) break;
		}

		for(Path p : uniquePaths){
			//System.out.println(p.pathList + " " + p.dist);
			System.out.println(p.dist);
		}
		System.out.println();

		//find edges that are common among the top 6 different smallest paths
		//create a multi-dimensional array for this
		int[][] edgeCounts = new int[points.size()+1][points.size()+1];
		for(Path p : uniquePaths){
			//System.out.println(p.pathList);
			for(int i = 0; i < p.pathList.size(); i++){
				if(i+1 == p.pathList.size()){
					//System.out.print(p.pathList.get(i)+"-"+p.pathList.get(0));
					if(p.pathList.get(i) < p.pathList.get(0)) {
						edgeCounts[p.pathList.get(i)][p.pathList.get(0)] += 1;
					}
					else if(p.pathList.get(i) > p.pathList.get(0)){
						edgeCounts[p.pathList.get(0)][p.pathList.get(i)] += 1;
					}
				}
				else {
					//System.out.print(p.pathList.get(i)+"-"+p.pathList.get(i+1)+ ",");
					if(p.pathList.get(i) < p.pathList.get(i+1)){
						edgeCounts[p.pathList.get(i)][p.pathList.get(i+1)] += 1;		
					}
					else if(p.pathList.get(i) > p.pathList.get(i+1)){
						edgeCounts[p.pathList.get(i+1)][p.pathList.get(i)] += 1;		
					}
					
				}
			}
			//System.out.println();
		}
		//print the occurances of the path lengths
		/*for (int i = 1; i <= points.size();i++ ) {
			for(int j = 1; j <= points.size(); j++){
				if(edgeCounts[i][j] != 0){
					System.out.println(i+","+j + ": " +edgeCounts[i][j]);	
				}
				
			}
		}*/

		//use a map to easily find what edges occur most often
		Map<String,Integer> mostCommonEdges = new HashMap<String,Integer>();
		for (int i = 1; i <= points.size();i++ ) {
			for(int j = 1; j <= points.size(); j++){
				if(edgeCounts[i][j] != 0){
					//System.out.println(i+","+j + ": " +edgeCounts[i][j]);	
					mostCommonEdges.put(i+"-"+j,edgeCounts[i][j]);
				}
				
			}
		}

		/*for(String s : mostCommonEdges.keySet()){
			System.out.println(s + ", " + mostCommonEdges.get(s));
		}*/

		//construct the new path with the n-1 most common edges
		//because I only uses 6 paths to figure out which are the most common
		//start with the edges that occur 6 times, then 5... until there is n-1 edges.
		List<String> wisdomPath = new ArrayList<String>();
		Boolean stop = false;
		for(int i = 6; i > 0; i--){
			if(stop) break;
			for(String s : mostCommonEdges.keySet()){
				//System.out.println(s + ", " + mostCommonEdges.get(s));
				if(stop) break;
				if(mostCommonEdges.get(s) == i){
					//System.out.println(s + ", " + mostCommonEdges.get(s));
					wisdomPath.add(s);
					if(wisdomPath.size() == points.size()-1){
						stop = true;
					}
				}
			}
		}

		/*String initPathStr = "";
		for(String s : wisdomPath){
			//System.out.print(s + " ");
			initPathStr += s + "-";
		}
		initPathStr = initPathStr.substring(0,initPathStr.length()-1);
		System.out.println(initPathStr);*/

		//build the path into a hamiltonia path
		/*List<String> finPath = new ArrayList<String>(); 
		for(String s : wisdomPath){
			//System.out.println(s.split("-")[0] + " " + s.split("-")[1]);
			String pointA = s.split("-")[0];
			String pointB = s.split("-")[1];
			if(finPath.size() == 0){
				finPath.add(pointA);
				finPath.add(pointB);
				wisdomPath.remove(s);
			}
			else {

			}
			for(String a : wisdomPath){
				System.out.println(a + " ");
			}
			System.out.println();	
		}*/

		List<String> finPath = new ArrayList<String>();
		finPath.add(wisdomPath.get(0).split("-")[0]);
		finPath.add(wisdomPath.get(0).split("-")[1]);
		wisdomPath.remove(wisdomPath.get(0));

		//System.out.println(finPath.get(0) + " " + finPath.get(1));

		while(wisdomPath.size() != 0){
			for(String a : wisdomPath){
					System.out.print(a + " ");
				}			
				System.out.println();

			for(int i = 0; i < wisdomPath.size(); i++){
				Integer pointA = Integer.valueOf(finPath.get(0));
				Integer pointB = Integer.valueOf(finPath.get(finPath.size()-1));
				String[] iter = wisdomPath.get(i).split("-");
				System.out.println(iter[0] + " " + iter[1]);

				if(Integer.valueOf(iter[0]) == pointA || Integer.valueOf(iter[1]) == pointA){
					/*System.out.println(wisdomPath.get(i));
					System.out.println(pointA);
					System.out.println(wisdomPath.get(i).replace("-",""));*/
					/*String pointToAdd = wisdomPath.get(i).replace(pointA,"");
					pointToAdd = pointToAdd.replace("-","");
					
					
					finPath.add(0, pointToAdd);
					wisdomPath.remove(i);*/
					break;
				}

				else if(Integer.valueOf(iter[0]) == pointB || Integer.valueOf(iter[1]) == pointB){
					/*System.out.println(wisdomPath.get(i));
					System.out.println(pointB);*/

					/*int index = wisdomPath.get(i).indexOf(pointB);
					String pointToAdd = "";
					if(index == 2){
						pointToAdd = Character.toString(wisdomPath.get(i).charAt(0));
					}
					else if(index == 0){
						pointToAdd = Character.toString(wisdomPath.get(i).charAt(2));
					}
					//System.out.println(pointToAdd);
					*/
					/*String pointToAdd = wisdomPath.get(i).replace(pointB,"");
					pointToAdd = pointToAdd.replace("-","");
					
					finPath.add(finPath.size(), pointToAdd);
					wisdomPath.remove(i);*/

					break;
				}
				else {
					
				}
				
			}
			for(String a : finPath){
					System.out.print(a + " ");
				}			
				System.out.println();
				System.out.println();
			//break;
		}

		/*ArrayList<Integer> finIntPath = new ArrayList<Integer>();
		for(String a : finPath){
			finIntPath.add(Integer.valueOf(a));
		}

		for(Integer i : finIntPath){
			System.out.println(i);
		}
		Path finalPathObj = new Path(finIntPath);
		System.out.println(finalPathObj.dist);*/





	} //end of main

	//Method to compute distance
	//Takes to points as parameters and computes the distance between them.
	//Uses distance formula
	public static double computeDistance(Point a, Point b){
		return Math.sqrt( ((a.x - b.x) * (a.x - b.x )) + ((a.y - b.y ) * (a.y - b.y ) ) );
	}

	//fisher-yates algorithm to shuffle array
	//this will be used to generate the k random populations needed for the start of the Wisdom algorithm
	public static int[] shuffle(int[] arra) {
		int[] arr = new int[arra.length];
		for(int i = 0;i < arra.length;i++){
			arr[i] = arra[i];
		}
		for(int i = arr.length - 1; i > 0; i--){
			int index = rnd.nextInt(i + 1);
			int a = arr[index];
			arr[index] = arr[i];
			arr[i] = a;
		}
		return arr;
	}

	//takes an array of int and converts it to a array of Integer Objects
	//needed when creating path objects
	public static Integer[] toObj(int[] primArray){
		Integer[] fin = new Integer[primArray.length];
		for(int i = 0; i < primArray.length; i++){
			fin[i] = Integer.valueOf(primArray[i]);
		}
		return fin;
	}

	public static ArrayList<Path> sortPathList(List<Path> arrayList, int pop){
		//order the arrayList array in order to find the best parents
		ArrayList<Path> orderPaths = new ArrayList<Path>();
		while(arrayList.size() > 0){
			int indexShortest = -1;
			double disShortest = Double.MAX_VALUE;
			for(int i = 0; i < arrayList.size(); i++){
				if(arrayList.get(i).dist < disShortest){
					indexShortest = i;
					disShortest = arrayList.get(i).dist;
				}
			}
			orderPaths.add(arrayList.get(indexShortest));
			arrayList.remove(indexShortest);
		}
		//keep only the k smallest paths, pop is determined by the population size entered in the text box in GUI.java
		int popSize = pop;
		if(orderPaths.size() > popSize) {
			ArrayList<Path> finalPaths = new ArrayList<Path>();
			for(int i = 0; i < popSize; i++){
			finalPaths.add(orderPaths.get(i));
			}	
			return finalPaths;
		}
		else {
			return orderPaths;
		}	
	}

	//return true if they are the same list
	public static Boolean compareArrayList(Path a, Path b){
		for(int i = 0; i < a.pathList.size();i++){
			if(a.pathList.get(i) != b.pathList.get(i)) {
				return false;
			}
		}
		return true;
	}

	//method used to create child paths
	public static Path createChildPath(Path two, Path one, int numCrossNodes, int mutateRate){
		//array to hold the child path
		//points from path one and two will be used to populate the child arraylist
		ArrayList<Integer> child = new ArrayList<Integer>();
		//xn is the amount of Crossover Nodes (nodes = points)
		//grab the nodes starting at a random integer startIndex
		int startIndex = rnd.nextInt(one.pathList.size() - numCrossNodes);
		//System.out.println(startIndex);
		for(int i = startIndex; i < one.pathList.size(); i++) {
			child.add(one.pathList.get(i));
		}

		int i = 0;
		//Add the nodes at a random index - crossIndex
		int crossIndex = rnd.nextInt(child.size() - 1);
		//System.out.println(crossIndex);
		while(child.size() < points.size()){ //needs to equal size of input
			if(!child.contains(two.pathList.get(i))) {
				child.add(crossIndex, two.pathList.get(i));
			}
			i++;
		}
		Path tmp = new Path(child);
		
		//Mutate Rate
		//muNum is a random number if this number is between one and the mutate rate
		int muNum = (int)(Math.random() * (100));
		
		//if mutateRate = 100, the it will always mutate
		if(muNum >= 0 && muNum < mutateRate ){
			//System.out.println(muNum + " is less than " + mutateRate);
			tmp.swap();	
		}
		else {
			//System.out.println(muNum + " is not less than " + mutateRate);
		}		
		//System.out.println(tmp.pathList + " " + tmp.dist);
		//childPaths.add(tmp);
		return tmp;
	}
}


//Object used to represent a single point
//Point Stores the Name, X and Y Value
//with methods to retrieve the name, x and y value
//and a method to set the name.
//Turns out there is a java class called point
class Point {
	int name;
	double x, y;
	//constructor
	Point(int name, double x, double y) {
		this.name = name;
		this.x = x;
		this.y = y;
	}
	//needed when converting a number to a letter and vise versa
	void setName(int a) {
		this.name = a;
	}
}

//objected used to represent a path
class Path {
	ArrayList<Integer> pathList;
	double dist;
	
	//construct a path using a array of int
	Path(int[] arrayPath){
		this.pathList = new ArrayList<Integer>(Arrays.asList(Wisdom.toObj(arrayPath)));
		this.dist = calcPathDistance();
	}
	//Construct using and Arraylist of Integer objects
	Path(ArrayList<Integer> arrayPath){
		this.pathList = new ArrayList<Integer>(arrayPath);
		this.dist = calcPathDistance();
	}

	//swap simulates the mutation
	void swap(){
		//will generate any random number between 0 and the size of points
		int swapIndexA = (int)(Math.random() * (pathList.size() - 1));
		int swapIndexB = (int)(Math.random() * (pathList.size() - 1));
		//if the are the same make swapIndexB slightly bigger
		if(swapIndexA == swapIndexB){
			swapIndexB = swapIndexB + 1;
		}
		//Swap the points in the arraylist
		Collections.swap(this.pathList, swapIndexA, swapIndexB);
		this.dist = calcPathDistance();
	}

	//method used to calculate the paths distance
	double calcPathDistance(){
		double finDis = 0.0;
		for(int i = 0; i < pathList.size(); i++){
			if(i+1 < this.pathList.size()){
				//System.out.println(Wisdom.computeDistance(Wisdom.points.get(pathList.get(i)),Wisdom.points.get(pathList.get(i+1))));
				finDis += Wisdom.computeDistance(Wisdom.points.get(pathList.get(i)),Wisdom.points.get(pathList.get(i+1)));	
			}
			else {
				//System.out.println(Wisdom.computeDistance(Wisdom.points.get(pathList.get(0)),Wisdom.points.get(pathList.get(pathList.size() - 1))));
				finDis += Wisdom.computeDistance(Wisdom.points.get(pathList.get(0)),Wisdom.points.get(pathList.get(pathList.size() - 1)));	
			}
		}
		//System.out.println(finDis);
		return finDis;
	}
}