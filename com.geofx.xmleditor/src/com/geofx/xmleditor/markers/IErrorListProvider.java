/**
 * 
 */

package com.geofx.xmleditor.markers;

import java.util.List;

import com.geofx.xmleditor.xml.XMLValidationError;

/**
 * @author rkwright
 *
 */
public interface IErrorListProvider
{
	List<XMLValidationError> 	getErrorList();
}
