// Genericized Skip List Set Collection
// Program created by Connor Cribley

/*
    DISCLAIMER: This program was created in its entirety by Connor Cribley. I do not authorize anybody to
    replicate, alter, submit, or otherwise use this program in a way which violates the academic integrity of any
    educational institution. Any attempt to use this code to commit academic plagiarism was done so without my
    permission.
*/


import java.util.*;

//Skip list class
public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {

    //Iterator class
    class SkipListSetIterator<T extends Comparable<T>> implements Iterator<T> {
        //Iterator item
        SkipListSetItem<T> currentItem;

        //Constructor
        public SkipListSetIterator() {
            //initialize the iterator item to head
            currentItem = (SkipListSetItem<T>) head;
            
            //Navigate to the bottom node of the head
            while (currentItem.getBelow() != null)
                currentItem = currentItem.getBelow();
        }

        //Returns false if the next iteration null
        @Override
        public boolean hasNext() {
            return currentItem != null;
        }
    
        //Returns the payload of the current iteration
        @Override
        public T next() {
            T payload = currentItem.getPayload();
            currentItem = currentItem.getNext();
            return payload;
        }

        //Remove throws exception
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Error: Method remove() not supported!", null);
        }
    }

    //Item wrapper class
    class SkipListSetItem<T extends Comparable<T>> {
        //Payload
        private T payload;

        //Links
        private SkipListSetItem<T> next;
        private SkipListSetItem<T> previous;
        private SkipListSetItem<T> above;
        private SkipListSetItem<T> below;

        //Getters
        public T getPayload() { return payload; }
        public SkipListSetItem<T> getNext() { return next; }
        public SkipListSetItem<T> getPrevious() { return previous; }
        public SkipListSetItem<T> getAbove() { return above; }
        public SkipListSetItem<T> getBelow() { return below; }

        //Setters
        public void setPayload(T payload) { this.payload = payload; }
        public void setBelow(SkipListSetItem<T> below) { this.below = below; }
        public void setAbove(SkipListSetItem<T> above) { this.above = above; }
        public void setPrevious(SkipListSetItem<T> previous) { this.previous = previous; }
        public void setNext(SkipListSetItem<T> next) { this.next = next; }

        //Constructor
        public SkipListSetItem(T payload) {
            this.payload = payload;
            this.next = null;
            this.previous = null;
            this.above = null;
            this.below = null;
        }
    }

    //Head and tail pointers
    private SkipListSetItem<T> head;
    private SkipListSetItem<T> tail;

    //Pseudo-random number generator
    Random rand = new Random();

    //Other data types
    private int currentHeight; //current height of the skip list
    private int maxHeight; //maximum height of the skip list
    private int heightChanges; //this value changes based on how much we grow/shrink the max height
    private int numItems; //number of items in the list


    //Constructors

    //Builds a new, empty skip list
    public SkipListSet() {
        head = new SkipListSetItem<T>(null);
        tail = new SkipListSetItem<T>(null);
        head.setNext(tail);
        tail.setPrevious(head);

        numItems = 0;
        maxHeight = 2;  //Starting height is 2 for an empty list
        currentHeight = 0;
        heightChanges = 1;
    }

    //Builds a new skip list then adds all the items in c to it
    public SkipListSet(Collection<T> c) {
        this();
        addAll(c);
    }

    //Return the number of items in the skip list
    @Override
    public int size() {
        return numItems;
    }

    //Return true if the list is empty, false otherwise
    @Override
    public boolean isEmpty() {
        return (numItems == 0);
    }

    //Can suppress unchecked cast warning
    @Override
    public boolean contains(Object o) {
        //Type cast
        T payload = (T)o;
        //find the item (or the item before it, if it's not there)
        SkipListSetItem<T> foundItem = traverse(payload);
        //If it's the item we're looking for, return true
        if (foundItem != null && foundItem.getPayload() != null && payload.compareTo(foundItem.getPayload()) == 0)
            return true;
        //Otherwise return false
        return false;
    }

    //Returns a new iterator
    @Override
    public Iterator<T> iterator() {
        return new SkipListSetIterator<T>();
    }


    @Override
    public Object[] toArray() {
        //Array and integer to iterate through the array
        Object[] array = new Object[numItems];
        int i = 0;

        //Start at the head
        SkipListSetItem<T> currentItem = head;

        //Go to the bottom of the head 
        while (currentItem.getBelow() != null)
            currentItem = currentItem.getBelow();

        //For each item in the skip list, add the payload to the array
        while (currentItem != null) {
            array[i] = currentItem.getPayload();
            i++;
            currentItem = currentItem.getNext();
        }

        //Return the array
        return array;
    }

    //Type casts the toArray function to T[]
    @Override
    public <T> T[] toArray(T[] a) {
        return (T[])toArray();
    }

    @Override
    public boolean add(T e) {
        //Find the item that goes before the item we want to insert
        SkipListSetItem<T> currentItem = traverse(e);
        //If the item is already in the skip list, return false
        if (currentItem != null && currentItem.getPayload() != null && currentItem.getPayload().compareTo(e) == 0)
            return false;
        //Otherwise, it's not in the skip list and we can insert it. Start by creating a new item
        SkipListSetItem<T> newItem = new SkipListSetItem<T>(e);
        //We insert it as we would in a linked list, but 2-dimensionally
        newItem.setPrevious(currentItem);
        newItem.setNext(currentItem.getNext());
        currentItem.getNext().setPrevious(newItem);
        currentItem.setNext(newItem);
        //Randomly generate the number of additional levels this item has
        int height = calculateHeight();
        //if the number of additional levels is not zero, we need to build upwards
        if (height > 0) {
            //save the current height of the skip list
            int initialHeight = currentHeight;
            int heightDifference = 0;

            /*
            while the current height of the skip list (that is, the height of the head and tail
            items) is less than the number of levels we want to add, we must increase the height
            of our list (that is, the height of the head and tail pointers) to match this new height
            */
            while (currentHeight <= height) {
                //create new head and tail items
                SkipListSetItem<T> newHead = new SkipListSetItem<T>(null);
                SkipListSetItem<T> newTail = new SkipListSetItem<T>(null);
                //Insert them as we would into a linked list
                newHead.setNext(newTail);
                newTail.setPrevious(newHead);
                //Also insert them on top of the previous head/tail
                newHead.setBelow(head);
                newTail.setBelow(tail);
                head.setAbove(newHead);
                tail.setAbove(newTail);
                //Set the head and tail to these new items
                head = newHead;
                tail = newTail;
                //increment the current height of the list
                currentHeight++;
            }

            //if the number of levels to add is less than the height of the list
            if (height < currentHeight)
                // set initial height to zero
                initialHeight = 0;
            //If the number of levels to add is greater than the initial height
            if (height > initialHeight)
                //Set the height difference equal to the difference between the number of levels to add and the initial height
                heightDifference = height - initialHeight;
            //Otherwise, set the height difference to 1;
            else
                heightDifference = 1;

            //For each level we want to add
            for (int i = 0; i < heightDifference; i++) {
                //Go back if the previous item is at the top level
                while (currentItem.getAbove() == null)
                    currentItem = currentItem.getPrevious();
                //Go up on the previous item
                currentItem = currentItem.getAbove();
                //create a new item to add on top of the item we've added
                SkipListSetItem<T> buildItem = new SkipListSetItem<T>(e);
                //Properly add it to the skip list
                buildItem.setPrevious(currentItem);
                buildItem.setNext(currentItem.getNext());
                currentItem.getNext().setPrevious(buildItem);
                currentItem.setNext(buildItem);
                buildItem.setBelow(newItem);
                newItem.setAbove(buildItem);
                //set the new item to point to the newly build item on top of it;
                newItem = buildItem;
            }
        }
        //increment the number of items in the list
        numItems++;

        //If we need to increase the maximum height, do so
        if (numItems/heightChanges >= maxHeight) {
            maxHeight *= 2;
            heightChanges *= 2;
        }

        return true;
    }

    //Can suppress unchecked cast warning
    @Override
    public boolean remove(Object o) {
        //Type cast
        T payload = (T)o;
        //find the item (or the item before it, if it's not there) we wish to delete, set it to the current item
        SkipListSetItem<T> currentItem = traverse(payload);
        //If the item we're looking for is not in the skip list, return false
        if (currentItem == null || currentItem.getPayload() == null || payload.compareTo(currentItem.getPayload()) != 0)
            return false;
        //Get the item before the item we wish to delete
        SkipListSetItem<T> previousItem = currentItem.getPrevious();

        //Set up a loop that only breaks if we have deleted all of the nodes with the given payload
        while (currentItem != null) {
            //Delete the item as we would in a linked list
            previousItem.setNext(currentItem.getNext());
            currentItem.getNext().setPrevious(previousItem);
            //Set all pointers (except below) in the deleted item to null
            currentItem.setPrevious(null);
            currentItem.setNext(null);
            currentItem.setAbove(null);
            //Set the item itself to null
            currentItem = null;
            //If (and while) we are at the top of the previous item, we must move back until we are not at the top
            while(previousItem.getAbove() == null)
                previousItem = previousItem.getPrevious();
            //Go one level higher on the previous position
            previousItem = previousItem.getAbove();
            //If the previous item's next item points to an item with a payload we want to delete, Set the current
            //item equal to the next item after previous item
            if (previousItem.getNext().getPayload() != null && previousItem.getNext().getPayload().compareTo(payload) == 0)
                currentItem = previousItem.getNext();
            //Set the below pointer to null
            if (currentItem != null)
                currentItem.setBelow(null);
        }
        //Decrement the number of items in the list
        numItems--;

        //If we need to decrease the maximum height, do so
        if (numItems/heightChanges <= maxHeight) {
            maxHeight /= 2;
            heightChanges /= 2;
        }

        //If there is an empty level in our skip list (meaning that the item
        //we deleted was the tallest one in the list), we must remove it
        if (head.getBelow().getNext() == tail.getBelow()) {
            //Set pointers to the head and tail of the list
            SkipListSetItem<T> headPtr = head;
            SkipListSetItem<T> tailPtr = tail;
            //The head is now the node below the previous head
            head = head.getBelow();
            tail = tail.getBelow();
            //Nullify all pointers that aren't needed
            head.setAbove(null);
            tail.setAbove(null);
            headPtr.setBelow(null);
            tailPtr.setBelow(null);
            headPtr.setNext(null);
            tailPtr.setPrevious(null);
            //Nullify the old head and tail pointers
            headPtr = null;
            tailPtr = null;
        }

        //Return true
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c)
            if(!contains(o)) return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T e : c)
            add(e);
        return true;
    }

    //Retains all of the items in the skip list
    @Override
    public boolean retainAll(Collection<?> c) {
        //Create a new skip list, add all of Objects in C to i
        clear();
        for (Object o : c) {
            T payload = (T)o;
            add(payload);
        }
        return false;
    }

    //Removes all items in a collection of items from the skip list, if they exist.
    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c)
            remove(o);
        return true;
    }

    @Override
    public void clear() {
        //Since the Java garbage collection system handles deleteion, all we have to do to clear a list is to make a new one
        head = new SkipListSetItem<T>(null);
        tail = new SkipListSetItem<T>(null);
        head.setNext(tail);
        tail.setPrevious(head);

        numItems = 0;
        maxHeight = 2;
        currentHeight = 0;
        heightChanges = 1;
    }

    @Override
    public Comparator<? super T> comparator() {
        //This can just return null
        return null;
    }

    //Method not supported, throws exception
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException("Error: Method subSet(T, T) not supported!", null);
    }

    //Method not supported, throws exception
    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException("Error: Method headSet(T) not supported!", null);
    }

    //Method not supported, throws exception
    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException("Error: Method tailSet(T) not supported!", null);
    }

    //Returns first item in list
    @Override
    public T first() {
        //Pointer to traverse down the head
        SkipListSetItem<T> firstItem = head;

        //Traverse down the head
        while (firstItem.getBelow() != null)
            firstItem = firstItem.getBelow();

        //Return the first item, if the list is not empty
        if (firstItem.getNext() != null)
            return firstItem.getNext().getPayload();

        //Otherwise return null
        return null;
    }

    @Override
    public T last() {
        //Pointer to traverse down the tail
        SkipListSetItem<T> lastItem = tail;

        //Traverse down the tail
        while (lastItem.getBelow() != null)
            lastItem = lastItem.getBelow();

        //Return the last item, if the list is not empty
        if (lastItem.getPrevious() != null)
            return lastItem.getPrevious().getPayload();
            
        //Otherwise return null
        return null;
    }

    //Method to calculate what the additional height of each item should be during insertion
    public int calculateHeight() {

        //Start at zero
        int additionalLevels = 0;

        /*
        We want the probability of the number of levels to be half that of the probability of it being the previous level
        (i.e. 2 = 50%, 3 = 25%, etc.) to do this, we randomly generate a boolean and increment the number of levels until
        that boolean is false (or until we reached the max height of the list) 
         */
        while (rand.nextBoolean() && additionalLevels < maxHeight - 1) {
            additionalLevels++;
        }

        //return the number of additional levels
        return additionalLevels;
    }

    //Rebalance method to rebalance the height of all list items
    public void reBalance() {
        //Set the current height to 0
        currentHeight = 0;
        
        // Start at the bottom of the head (which will be our new head)
        while (head.getBelow() != null)
            head = head.getBelow();
        
        //Remove the nodes on top of head
        head.setAbove(null);
            
        //Pointer for iterating over the list
        SkipListSetItem<T> currentItem = head.getNext();
        //Pointer for keeping track of the previous item in our list
        SkipListSetItem<T> previousItem = head;

        //Also remove the items on top of the tail
        while (tail.getBelow() != null)
            tail = tail.getBelow();
        tail.setAbove(null);
        
        //This skip list is now effectively a sorted list that we can iterate over
        //So, for each item in the list
        while (currentItem != null) {
            //Nullify the pointers to the node above it
            currentItem.setAbove(null);

            //Assign a random height to it
            int height = calculateHeight();
            //If this height is not zero
            if (height > 0) {
                //Create a traversal pointer to iterate up the height of the current item
                SkipListSetItem<T> newCurrentItem = currentItem;
                //Variables for initial height and height difference
                int initialHeight = currentHeight;
                int heightDifference;
                /*
                while the randomly calculated height is larger than the current height of our list,
                we need to increase the height of our list (that is, the height of the head and tail
                pointers)
                */
                while (currentHeight <= height) {
                    //Create new head and tail pointers
                    SkipListSetItem<T> newHead = new SkipListSetItem<T>(null);
                    SkipListSetItem<T> newTail = new SkipListSetItem<T>(null);
                    //Insert them as you would into a linked list
                    newHead.setNext(newTail);
                    newTail.setPrevious(newHead);
                    //Also insert them on top of the previous head/tail
                    newHead.setBelow(head);
                    newTail.setBelow(tail);
                    head.setAbove(newHead);
                    tail.setAbove(newTail);
                    //These are now the new head and tail
                    head = newHead;
                    tail = newTail;
                    //Finally, increase the current height of our list
                    currentHeight++;
                }

                //If the randomly calculated height is less than the height of the list
                if (height < currentHeight)
                    //Set the initial height to zero
                    initialHeight = 0;
                //If the randomly calculated height is greater than the initial height
                if (height > initialHeight)
                    //Set the height difference to be the difference between calculated height and the initial height
                    heightDifference = height - initialHeight;
                //Otherwise, set the Height difference to 1
                else
                    heightDifference = 1;
                
                //For each level we want to add
                for (int i = 0; i < heightDifference; i++) {
                    //Go back if the previous item is at the top level
                    while (previousItem.getAbove() == null)
                        previousItem = previousItem.getPrevious();
                    //Go up on the previous item
                    previousItem = previousItem.getAbove();
                    //Create a new item to add on top of the item we've added
                    SkipListSetItem<T> newItem = new SkipListSetItem<T>(newCurrentItem.getPayload());
                    //Properly add it to the skip list
                    newItem.setPrevious(previousItem);
                    newItem.setNext(previousItem.getNext());
                    previousItem.getNext().setPrevious(newItem);
                    previousItem.setNext(newItem);
                    //Also insert it on top of our current item
                    newItem.setBelow(newCurrentItem);
                    newCurrentItem.setAbove(newItem);
                    //Set the current item to the item we've just built
                    newCurrentItem = newItem;
                }
            }
            //Traverse
            currentItem = currentItem.getNext();
            if (currentItem != null)
                previousItem = currentItem.getPrevious();
        }
    }

    //Traverses the skip list up until the item we want (for contains/delete) or the item before the item we want (insert)
    private SkipListSetItem<T> traverse(T payload) {
        //Start at the head of the linked list
        SkipListSetItem<T> currentItem = head;
        //Flag to fire if we are at the final position (either at payload (contains/delete) or the node before payload (insert))
        boolean finalPosition = false;

        while (!finalPosition) {
            //While our current item is less that the value of the next item in the row, and we are not at the end of the row
            while (currentItem.getNext() != null && currentItem.getNext().getPayload() != null && currentItem.getNext().getPayload().compareTo(payload) <= 0)
                //Get the next item
                currentItem = currentItem.getNext();
            //If we can go down from the current node, do so
            if (currentItem.getBelow() != null)
                currentItem = currentItem.getBelow();
            //Otherwise, we have reached the lowest position of the node we are looking for
            else
                finalPosition = true;
        }
        //Return the item
        return currentItem;
    }
}
