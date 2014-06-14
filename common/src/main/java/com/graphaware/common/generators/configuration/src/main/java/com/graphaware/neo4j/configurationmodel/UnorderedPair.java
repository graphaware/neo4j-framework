/**
 * TODO : 
 * place this class into util package of the framework.
 * Maybe reimplement, to behave as neo Pair.of ?
 */

package com.graphaware.neo4j.configurationmodel;

import java.util.Objects;

/**
 *
 * @author Vojtech Havlicek (Graphaware)
 * @param <T>
 */
public class UnorderedPair<T> {
    private final T first;
    private final T second;
    
    public UnorderedPair (T a, T b) {
        first = a;
        second = b;
    }
    
    public T first() {
        return first;
    }
    
    public T second() {
        return second;
    }
    
    /**
     *  
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj instanceof UnorderedPair) {
            UnorderedPair that = (UnorderedPair) obj;
            boolean result = (equals(this.first(), that.first() ) && equals(this.second(), that.second())) ||
                             (equals(this.first(), that.second()) && equals(this.second(), that.first()));
            
            return result;
        }   
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.first);
        hash = 23 * hash + Objects.hashCode(this.second);
        return hash;
    }
    
    @Override
    public String toString() {
       String s = "(" + first.toString() + ", " + second.toString() + ")";
       return s;
    }
    
    static boolean equals( Object obj1, Object obj2 )
    {
        return ( obj1 == obj2 ) || ( obj1 != null && obj1.equals( obj2 ) );
    }
}
