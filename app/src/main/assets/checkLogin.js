function login(username, password) {
    var inputs = document.getElementsByTagName('input');
    inputs[0].value = username;
    inputs[1].value = password;
    GInterface.traiterEvenementValidation();
}

function checkLoginInputs() {
    var inputs = document.getElementsByTagName('input');
    return inputs.length >= 2;
}

function checkLoggedIn() {
    return document.getElementById('GInterface.Instances[2]') !== null;
}

function waitFor(testFx, onReady, timeOutMillis) {
    var maxtimeOutMillis = timeOutMillis ? timeOutMillis : 10000,
        start = new Date().getTime(),
        condition = false,
        interval = setInterval(function() {
            if ((new Date().getTime() - start < maxtimeOutMillis) && !condition) {
                condition = testFx();
            } else {
                if (!condition) {
                    console.error('"waitFor()" timeout');
                    JavaInterface.finishLogin(false);
                    clearInterval(interval);
                } else {
                    // console.error('"waitFor()" finished in ' + (new Date().getTime() - start) + 'ms.');
                    setTimeout(onReady, 100);
                    clearInterval(interval);
                }
            }
        }, 300);
}

waitFor(function() {
    // Wait for input elements to be loaded
    return checkLoginInputs();
}, function() {
    console.error('Form loaded, logging in ' + username);
    login(username, password);
    console.error("Started logging in process");
    waitFor(function() {
        var correct = checkLoggedIn();
        var incorrect = document.getElementsByTagName('body')[0].textContent.indexOf('incorrect') >= 0;
        return incorrect || correct;
    }, function() {
        var loggedIn = checkLoggedIn();
        console.log(loggedIn);
        JavaInterface.finishLogin(loggedIn);
    });
});
