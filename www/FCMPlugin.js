var exec = require('cordova/exec');

function FCMPlugin() { 
	console.log("FCMPlugin.js: is created");
}

// SUBSCRIBE TO TOPIC //
FCMPlugin.prototype.subscribeToTopic = function( topic, success, error ){
	exec(success, error, "FCMPlugin", 'subscribeToTopic', [topic]);
}
// UNSUBSCRIBE FROM TOPIC //
FCMPlugin.prototype.unsubscribeFromTopic = function( topic, success, error ){
	exec(success, error, "FCMPlugin", 'unsubscribeFromTopic', [topic]);
}
// NOTIFICATION CALLBACK //
FCMPlugin.prototype.onNotification = function( callback, success, error ){
	FCMPlugin.prototype.onNotificationReceived = callback;
	exec(success, error, "FCMPlugin", 'registerNotification',[]);
}
// TOKEN REFRESH CALLBACK //
FCMPlugin.prototype.onTokenRefresh = function( callback ){
	FCMPlugin.prototype.onTokenRefreshReceived = callback;
}
// GET TOKEN //
FCMPlugin.prototype.getToken = function( success, error ){
	exec(success, error, "FCMPlugin", 'getToken', []);
}

// DEFAULT NOTIFICATION CALLBACK //
FCMPlugin.prototype.onNotificationReceived = function(payload){
	console.log("Received push notification")
	console.log(payload)
}
// DEFAULT TOKEN REFRESH CALLBACK //
FCMPlugin.prototype.onTokenRefreshReceived = function(token){
	console.log("Received token refresh")
	console.log(token)
}
FCMPlugin.prototype.revokeToken = function(){
	return new Promise(function(resolve, reject) {
		exec(resolve, reject, "FCMPlugin", 'revokeToken', []);
	})
}
// Show the permission pop-up on iOS,
FCMPlugin.prototype.requestPermissionsOnIOS = function(success, error) {
	return new Promise(function(resolve, reject) {
		exec(resolve, reject, "FCMPlugin", 'requestPermission', []);
	})
};
FCMPlugin.prototype.hasPermissionsOnIOS = function () {
	return new Promise(function(resolve, reject) {
		exec(resolve, reject, "FCMPlugin", "hasPermission", []);
	})
};
// FIRE READY //
exec(function(result){ console.log("FCMPlugin Ready OK") }, function(result){ console.log("FCMPlugin Ready ERROR") }, "FCMPlugin",'ready',[]);





var fcmPlugin = new FCMPlugin();
module.exports = fcmPlugin;
