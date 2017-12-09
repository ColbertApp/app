function waitFor(testFx, onReady, errorCodeOnFailure) {
    var maxTimeOutMillis = 15000;
    var start = new Date().getTime();
    var condition = false;
    var interval = setInterval(function() {
        if ((new Date().getTime() - start < maxTimeOutMillis) && !condition) {
            condition = testFx();
        } else {
            if (!condition) {
                console.error('Timed out.');
                finished(errorCodeOnFailure || 1);
                clearInterval(interval);
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
    // Let their script know we have input things. Almost black magic.
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

function goToSchedulePage() {
    var link = Array.prototype.slice.call(document.getElementsByTagName('li')).filter(function(elem) {
        return elem.getAttribute('aria-label') === 'Emploi du temps';
    })[0];
    link.focus();
    link.click();
}

function getAverage(marks) {
    var total = 0;
    var amount = 0;
    total = marks.reduce(function(prev, mark) {
        var coef = parseFloat(mark.coef.replace(',', '.'));
        var value = parseFloat(mark.value.replace(',', '.'));
        return prev + (value * coef);
    }, 0) * 20;
    amount = marks.reduce(function(prev, mark) {
        var coef = parseFloat(mark.coef.replace(',', '.'));
        var bareme = parseInt(mark.bareme);
        return prev + coef * bareme;
    }, 0);
    return (total / amount).toFixed(2);
}

function getMarks(getAverage) {
    var noMarks = GInterface &&
        GInterface.Instances &&
        GInterface.Instances.length >= 1 &&
        GInterface.Instances[1] &&
        !(GInterface.Instances[1] instanceof PageResultatReleve ||
            GInterface.Instances[1] instanceof PageResultatBulletins);
    if (noMarks) return [];
    var marks = GInterface.Instances[1].ListeElements.ListeElements;
    return marks.map(function(elem) {
        var marks = elem.ListeDevoirs.ListeElements.map(function(markElement) {
            return {
                value: markElement.Note.note,
                bareme: markElement.Bareme.chaine,
                title: markElement.Commentaire,
                coef: markElement.Coefficient.note,
            };
        });
        return {
            subject: elem.Libelle,
            marks: marks,
            average: 'Moyenne: ' + (elem.MoyenneEleve.note || getAverage(marks)),
        };
    }).filter(function(elem) {
        return elem.marks.length > 0;
    });
}

function goToMarks() {
    var link = Array.prototype.slice.call(document.getElementsByTagName('li')).filter(function(elem) {
        return elem.getAttribute('aria-label') === 'Relevé de notes';
    })[0];
    link.focus();
    link.click();
}

function getSchedule(dateToString, pad) {
    var courses = GInterface.Instances[1].donneesGrille.listeCours.ListeElements;
    return courses.map(function(elem) {
        return {
            hour: ('0' + elem.DateDuCours.getHours()).substr(-2) + 'h' + ('0' + elem.DateDuCours.getMinutes()).substr(-2),
            isTeacherMissing: elem.Statut === 'Prof. absent',
            notice: elem.Statut,
            teacherName: elem.ListeContenus.ListeElements[1].Libelle,
            sub: elem.ListeContenus.ListeElements[0].Libelle,
            classroom: elem.ListeContenus.ListeElements[elem.ListeContenus.ListeElements.length - 1].Libelle,
            date: dateToString(elem.DateDuCours, pad),
        };
    });
}

function goToHomework() {
    GInterface.Instances[1]._surToutVoir(7);
}

function finishedLoading() {
    return document.getElementsByClassName('Image_Attendre').length === 0 && (scheduleLoaded() || homeworkLoaded() || marksLoaded());
}

function scheduleLoaded() {
    return GInterface &&
        GInterface.Instances &&
        GInterface.Instances.length >= 2 &&
        GInterface.Instances[1] &&
        GInterface.Instances[1].donneesGrille &&
        GInterface.Instances[1].donneesGrille.listeCours &&
        GInterface.Instances[1].donneesGrille.listeCours.ListeElements &&
        GInterface.Instances[1].donneesGrille.listeCours.ListeElements.length;
}

function homeworkLoaded() {
    return GInterface &&
        GInterface.Instances &&
        GInterface.Instances.length >= 2 &&
        GInterface.Instances[1] &&
        GInterface.Instances[1].ListeTravailAFaire !== undefined;
}

function marksLoaded() {
    var nomarks = GInterface &&
        GInterface.Instances &&
        GInterface.Instances.length >= 1 &&
        GInterface.Instances[1] &&
        !(GInterface.Instances[1] instanceof PageResultatReleve ||
            GInterface.Instances[1] instanceof PageResultatBulletins);
    return nomarks || (GInterface &&
        GInterface.Instances &&
        GInterface.Instances.length >= 2 &&
        GInterface.Instances[1] &&
        GInterface.Instances[1].ListeElements !== undefined);
}

function pad(n) {
    return ('0' + n).slice(-2);
}

function dateToString(date, pad) {
    return pad(date.getDate()) + '/' + pad(date.getMonth() + 1) + '/' + date.getFullYear();
}

function goToNextSchedule() {
    let nextWeekNumber = GInterface.Instances[1].Instances[0].Position + 1;
    //Boom, magic
    GInterface.Instances[1].Instances[0].SetSelection(nextWeekNumber);
}


function htmlToText(htmlString) {
    // Use DOM in order to get text with html entities.
    // Speed isn't a priority here, at that scale.
    var div = document.createElement('div');
    div.innerHTML = htmlString;
    return div.textContent;
}

function getHomework(dateToString, pad, htmlToText) {
    var taf = GInterface.Instances[1].ListeTravailAFaire.ListeElements;
    return taf.map(function(elem) {
        return {
            date: dateToString(elem.PourLe, pad),
            sub: elem.Matiere.Libelle,
            content: htmlToText(elem.descriptif),
        };
    });
}

var schedule = [];
var homework = [];
var marks = [];

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
        console.error('Going to homework page');
        goToHomework();
        waitFor(function() {
            return homeworkLoaded && finishedLoading();
        }, function() {
            console.error('Homework page loaded. Parsing');
            homework = getHomework(dateToString, pad, htmlToText);
            console.error('Going to schedule page');
            goToSchedulePage();
            waitFor(function() {
                return scheduleLoaded() && finishedLoading();
            }, function() {
                console.error('Schedule loaded. Parsing');
                schedule = getSchedule(dateToString, pad);
                console.error('Loading next schedule...');
                goToNextSchedule();
                waitFor(function() {
                    return scheduleLoaded() && finishedLoading();
                }, function() {
                    schedule = schedule.concat(getSchedule(dateToString, pad));
                    console.error('Going to marks page');
                    goToMarks();
                    console.error('goToMarks launched');
                    waitFor(function() {
                        return finishedLoading();
                    }, function() {
                        console.error('Marks page loaded. Parsing');
                        marks = getMarks(getAverage);
                        console.error('Got all the data needed. Quitting.');
                        finished();
                    });
                });
            });
        });
    }, 2);
});

function finished(exitCode) {
    exitCode = exitCode || 0;
    if (exitCode === 2) {
        console.log(JSON.stringify({
            error: 'Login failed',
            loginSuccess: false,
        }));
        console.warn(exitCode);
        return;
    }
    var finalResult = {
        loginSuccess: true,
        schedule: schedule,
        taf: homework,
        marks: marks,
    };
    console.error('Printing result. End of script');
    console.log(finalResult);
    console.warn(exitCode);
    JavaInterface.finishFetch(JSON.stringify(finalResult));
}
