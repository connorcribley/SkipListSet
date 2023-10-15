# SkipListSet

A skip list class that implements SortedSet in Java. Takes in any parameterized type T that
extends Comparable and stores it in the skip list. Contains iterator and item-wrapper classes.
Implements most SortedSet methods.

For instance:

// This declares a new skip list set
SkipListSet<T> skipListSet = new SkipListSet<T>();

// This defines an array list of values to add to the list
ArrayList<? extends T> addList;

// This adds those values to the list
skipListSet.addAll(addList);

// This defines an array list of values to remove from the list
ArrayList<T> removeList;

// This removes those values from the list (if they exist)
skipListSet.removeAll(removeList);

// This defines a parameterized type to search for in the list
T searchItem;

// This searches the list for the item, returning true if it exists and false if it doesn't
skipListSet.contains(searchItem);

// This rebalances the height of all list items
skipListSet.reBalance();


Many more methods are also supported. Please consult SkipListSet.java for a complete list
of implemented methods.
