function waitFor(testFx, onReady, errorCodeOnFailure) {
    var maxtimeOutMillis = 15000;
    var start = new Date().getTime();
    var condition = false;
    var interval = setInterval(function() {
        if ((new Date().getTime() - start < maxtimeOutMillis) && !condition) {
            condition = testFx();
        } else {
            if (!condition) {
                console.error('Timed out.');
                finished(errorCodeOnFailure || 1);
            } else {
                setTimeout(onReady, 100);
                clearInterval(interval);
            }
        }
    }, 300);
}

function login(username, password) {
    // Enable accessibility for easier parsing
    // Actually, I'm not sure about this but nvm
    // Update: There is almost no DOM parsing,
    // but there is no downside to this.
    localStorage.OPTIONS_ESPACE_PRONOTE = JSON.stringify({
        modeA_3: true
    });
    // Fill in the input fields
    var inputs = document.getElementsByTagName('input');
    inputs[0].value = username;
    inputs[1].value = password;
    // Let their script know we have inputed things. Almost black magic.
    var blurEvent = new Event('blur');
    var keyupEvent = new Event('keyup');
    inputs[0].dispatchEvent(keyupEvent);
    inputs[0].dispatchEvent(blurEvent);
    inputs[1].dispatchEvent(keyupEvent);
    inputs[1].dispatchEvent(blurEvent);
    // Finally trigger the login action
    inputs[2].onclick();
}

function checkLoginInputs() {
    var inputs = document.getElementsByTagName('input');
    return inputs.length >= 3;
}

function checkLoggedIn() {
    return (GInterface && GInterface.Instances && GInterface.Instances.length >= 1 && GInterface.Instances[1] && GInterface.Instances[1]._surToutVoir) !== undefined;
}

function goToAccountSettings() {
    var link = Array.prototype.slice.call(document.getElementsByTagName('li')).filter(function(elem) {
        return elem.getAttribute('aria-label') === 'Mon compte';
    })[0];
    link.focus();
    link.click();
}

function clickChange() {
    // Find the link in document.
    var link = Array.prototype.slice.call(document.getElementsByTagName('div')).filter(function(elem) {
        return elem.getAttribute('id') && elem.getAttribute('id').indexOf('mdp') > 0;
    })[0];
    // Click it
    var event = new Event('mouseup');
    link.dispatchEvent(event);
}

function changePassword(old, newPasswd) {
    // Fill the form in, then submit it
    document.getElementById('GInterface.Instances[1].Instances[1]_mdp').value = old;
    document.getElementById('GInterface.Instances[1].Instances[1]_login').value = newPasswd;
    document.getElementById('GInterface.Instances[1].Instances[1]_loginConfirm').value = newPasswd;
    document.getElementById('GInterface.Instances[1].Instances[1]_btns_1').dispatchEvent(new Event('click'));
}

function finishedLoading() {
    return document.getElementsByClassName('Image_Attendre').length === 0;
}


waitFor(function() {
    // Wait for input elements to be loaded
    return checkLoginInputs();
}, function() {
    console.error('Form loaded, logging in "' + username + '"');
    login(username, password);
    waitFor(function() {
        var loggedIn = checkLoggedIn();
        return loggedIn && finishedLoading();
    }, function() {
        console.error('Logged in');
        goToAccountSettings();
        waitFor(function() {
            return finishedLoading();
        }, function() {
            console.error('In account settings');
            clickChange();
            waitFor(function() {
                return finishedLoading();
            }, function() {
                console.error('Form loaded');
                changePassword(password, newPasswd);
                waitFor(function() {
                    return finishedLoading();
                }, function() {
                    console.error('Password changed');
                    finished(0);
                });
            });
        });
    }, 2);
});

function finished(exitCode) {
    exitCode = exitCode || 0;
    if (exitCode === 2) {
        console.log(false);
        //phantom.exit(exitCode);
        return;
    }
    console.log(exitCode === 0 ? true : false);
    //phantom.exit(exitCode);
}
