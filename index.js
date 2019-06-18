/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();
const firestore = admin.firestore();

// [START trigger_document_any_change]
exports.transactionManagement = functions.firestore
  .document('companies/{companyID}/transactions/{transactionID}')
  .onWrite((change, context) => {

    // Get an object with the current document value.
    // If the document does not exist, it has been deleted.

    var senderDoc;
    var receiverDoc;
    var projectDoc;
    var debitAccountDoc;
    var creditAccountDoc;
    var transactionDoc;

    var globalCompanyID = context.params.companyID;
    // const document = change.after.exists ? change.after.data() : null;
    if (change.after.exists) {
      const masterData = change.after.data();
      var isSkip = masterData.isSkip;
      if (isSkip === true) {
        console.log('transactionDoc isSkip is  true, returning ....');
        return null;
      }

      const verified = masterData.verified;
      if (verified !== undefined && verified === true) {
        const transactionAmount = masterData.amount;


        // for skip the recursive call while updating any transaction
        const transactionID = masterData.transactionID;
        var transactionData = firestore.doc('companies/' + globalCompanyID + '/transactions/' + transactionID);
        transactionDoc = transactionData.get()
          .then(doc => {
            if (doc.exists) {
              console.log('transactionDoc isSkip set to true');
              return transactionData.update({
                isSkip: true
              });
            } else {
              return null;
            }
          })
          .catch(err => {
            console.log('Error getting document(transactionDoc)', err);
          });

        // for sender( mandatory all transaction)
        const senderID = masterData.senderID;
        var senderData = firestore.doc('companies/' + globalCompanyID + '/users/' + senderID);
        senderDoc = senderData.get()
          .then(doc => {
            if (doc.exists) {

              var senderAmount = doc.get("amount");
              console.log('senderDoc senderAmount(before) : ', senderAmount);
              if (senderAmount === undefined) {
                senderAmount = -transactionAmount;
              } else {
                senderAmount = senderAmount - transactionAmount;
              }
              console.log('senderDoc senderAmount(after) : ', senderAmount);
              return senderData.update({
                amount: senderAmount
              });

            } else {
              return null;
            }

          })
          .catch(err => {
            console.log('Error getting document', err);
          });

        // for receiver ( mandatory all transaction)
        const receiverID = masterData.receiverID;
        var receiverData = firestore.doc('companies/' + globalCompanyID + '/users/' + receiverID);
        receiverDoc = receiverData.get()
          .then(doc => {
            if (doc.exists) {

              var receiverAmount = doc.get("amount");
              console.log('receiverDoc receiverAmount(before) : ', receiverAmount);
              if (receiverAmount === undefined) {
                receiverAmount = transactionAmount;
              } else {
                receiverAmount = receiverAmount + transactionAmount;
              }
              console.log('receiverDoc receiverAmount(after) : ', receiverAmount);
              return receiverData.update({
                amount: receiverAmount
              });

            }else{
              return null;
            }
          })
          .catch(err => {
            console.log('Error getting document receiver = ', err);
          });
        // for project( transactionType should be normal)
        const transactionType = masterData.transactionType;
        const TRANSACTION_TYPE_NORMAL = 3;
        const BASE_NORMAL_ID = 6010000;
        if (transactionType === TRANSACTION_TYPE_NORMAL) {
          const projectID = masterData.projectID;

          var projectIDData = firestore.doc('companies/' + globalCompanyID + '/projects/' + projectID);
          projectDoc = projectIDData.get()
            .then(doc => {
              if (doc.exists) {

                var projectAmount = doc.get("amount");
                console.log('projectDoc projectAmount(before) : ', projectAmount);
                if (projectAmount === undefined) {
                  if (senderID >= BASE_NORMAL_ID) {
                    projectAmount = transactionAmount;
                  } else {
                    projectAmount = - transactionAmount;
                  }
                } else {
                  if (senderID >= BASE_NORMAL_ID) {
                    projectAmount = projectAmount + transactionAmount;
                  } else {
                    projectAmount = projectAmount - transactionAmount;
                  }
                }
                console.log('projectDoc projectAmount(after) : ', projectAmount);
                return projectIDData.update({
                  amount: projectAmount
                });

              }else{
                return null;
              }
            })
            .catch(err => {
              console.log('Error getting document projectDoc = ', err);
            });
        }

        // for bank accounts debit(if account is not undefined)
        const debitAccount = masterData.DebitAccountID;
        if (debitAccount !== undefined) {
          var debitAccountData = firestore.doc('companies/' + globalCompanyID + '/bankAccounts/' + debitAccount);
          debitAccountDoc = debitAccountData.get()
            .then(doc => {
              if (doc.exists) {

                var debitAccountAmount = doc.get("amount");
                console.log('debit account amount(before) : ', debitAccountAmount);
                if (debitAccountAmount === undefined) {
                  debitAccountAmount = - transactionAmount;
                } else {
                  debitAccountAmount = debitAccountAmount - transactionAmount;
                }
                console.log('debit account amount(after) : ', debitAccountAmount);
                return debitAccountData.update({
                  amount: debitAccountAmount
                });
              }else{
                return null;
              }
            })
            .catch(err => {
              console.log('Error getting document debitAccountDoc = ', err);
            });
        }
        // for bank accounts credit (if account is not undefined)
        const creditAccount = masterData.CreditAccountID;
        if (creditAccount !== undefined) {
          var creditAccountData = firestore.doc('companies/' + globalCompanyID + '/bankAccounts/' + creditAccount);
          creditAccountDoc = creditAccountData.get()
            .then(doc => {
              if (doc.exists) {

                var creditAccountAmount = doc.get("amount");
                console.log('credit account amount(before) : ', creditAccountAmount);
                if (creditAccountAmount === undefined) {
                  creditAccountAmount = transactionAmount;
                } else {
                  creditAccountAmount = creditAccountAmount + transactionAmount;
                }
                console.log('credit account amount(after) : ', creditAccountAmount);
                return creditAccountData.update({
                  amount: creditAccountAmount
                });
              }else{
                return null;
              }
            })
            .catch(err => {
              console.log('Error getting document creditAccountAmount = ', err);
            });
        }

      }

    }
    return Promise.all([senderDoc, receiverDoc, projectDoc, debitAccountDoc, creditAccountDoc,transactionDoc]);
  });

exports.modifyUser = functions.firestore
  .document('needtodelete/delete')
  .onWrite((change, context) => {
  });
