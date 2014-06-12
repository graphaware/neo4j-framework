/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.graphaware.neo4j.configurationmodel;

/**
 *
 * @author Vojtech Havlicek (Graphaware)
 */
public class InvalidDistributionException extends Exception{
    
    public InvalidDistributionException(String message)
    {
        super(message);
    }
}
