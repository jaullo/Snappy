Snappy
Snappy Instant Messenger

I started this new project based on spika messenger because they are not supporting the opensource community.

If u want to join this project and give some help, please contact me

This code is not compatible with the original code of Spika, i u want to use spika u have to compare all files, i will not provide any instructions


Changes for this commint
 * Push notifications are partially working (ballon)
 * Google maps not loading error, was fixed
 * Project package was renamed, now it is "com.snappy"
 * All application string was modified to make this app multilanguage
 * Spanish language was added
 * Video and audio max recording time was increased to 10 minutes
 * Max contacts and favorites limit was increased, 200 and 60 respectively
 * New variables was adden in constants to give more personalization to the app on Message received
 * The triggerNotification method from GCMINTENTSERVICE was replaced with generateNotification method. Now we have personalized status bar messages, user info and message body when showing a message in status bar. Also we have new device wakeup functions.
 * Android kikat error when selecting image or video from gallery was fixed. New FileUtils class was added. New validation for kitkat is made on CameraCropActivity
 * Error that crashes the app when u try to play a sound or video was fixed. The method that causes this fatal error was removed while we work on it to solve the problem. 
 

To get this application code to work, u have to make some changes in server files

1. Go to Spika\Controller\AsyncTaskController.php
2. Search for $controllers->post('/notifyNewDirectMessage' method and add this new variables
          //get the message body tag
            $msgBody = $message['body'];
            
            //set the flag
            $msgFlag = "1";
3. Replace the fields array with this
 $fields = array(
                                'registration_ids' => $registrationIDs,
                                'data' => array( 
                                        "message" => $pushnotificationMessage, 
                                        "fromUser" => $fromUserId,
                                        "messageBody" => $msgBody,
                                        "messageFlag" => $msgFlag,
                                        "fromUserName"=>$fromUser['name'],
                                        "type" => "user", 
                                        "groupId" => ""
                                        ),
                               );
4. Search for $controllers->post('/notifyNewGroupMessage' method dd this new variables
          //get the message body tag
            $msgBody = $message['body'];
            
            //set the flag
            $msgFlag = "2";
5. Replace the fields array method with this
 $fields = array(
                            'registration_ids' => $androidTokens,
                            'data' => array( 
                                    "message" => $pushMessage, 
                                    "fromUser" => $message['from_user_id'],
                                    "messageBody" => $msgBody,
                                    "messageFlag" => $msgFlag,
                                    "fromUserName"=>$fromUserData['name'],
                                    "type" => "group", 
                                    "groupId" => $message['to_group_id']
                                    ),
                           );

6. Restart apache

That's all ;)
Enjoy


