/**
 */
package org.eclipse.passage.lic.floating.model.api;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>User Grant</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.passage.lic.floating.model.api.UserGrant#getUser <em>User</em>}</li>
 *   <li>{@link org.eclipse.passage.lic.floating.model.api.UserGrant#getAuthentication <em>Authentication</em>}</li>
 * </ul>
 *
 * @see org.eclipse.passage.lic.floating.model.meta.FloatingPackage#getUserGrant()
 * @model
 * @generated
 */
public interface UserGrant extends EObject {
	/**
	 * Returns the value of the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>User</em>' attribute.
	 * @see #setUser(String)
	 * @see org.eclipse.passage.lic.floating.model.meta.FloatingPackage#getUserGrant_User()
	 * @model required="true"
	 * @generated
	 */
	String getUser();

	/**
	 * Sets the value of the '{@link org.eclipse.passage.lic.floating.model.api.UserGrant#getUser <em>User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>User</em>' attribute.
	 * @see #getUser()
	 * @generated
	 */
	void setUser(String value);

	/**
	 * Returns the value of the '<em><b>Authentication</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Authentication</em>' reference.
	 * @see #setAuthentication(EvaluationInstructions)
	 * @see org.eclipse.passage.lic.floating.model.meta.FloatingPackage#getUserGrant_Authentication()
	 * @model
	 * @generated
	 */
	EvaluationInstructions getAuthentication();

	/**
	 * Sets the value of the '{@link org.eclipse.passage.lic.floating.model.api.UserGrant#getAuthentication <em>Authentication</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Authentication</em>' reference.
	 * @see #getAuthentication()
	 * @generated
	 */
	void setAuthentication(EvaluationInstructions value);

} // UserGrant
