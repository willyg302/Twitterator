function printFooter() {
    document.write("<div id=\"footer\"><span class=\"doNotPrint\">");
    document.write("Copyright &copy; 2013 HICHI. Design by <a href=\"http://haran.freeshell.org/oswd/\">Haran</a>.");
    document.write("</span></div>");
}

var actionTags = ['Add user', 'Create database', 'Export', 'Search', 'Track keyword', 'Track user'];
var promptTags = [];
var dbTags = ['test', 'test2']; // These will be dynamically pulled from server on startup

// For the server...
var serverActs = ['addUser', 'createDB', 'export', 'search', 'trackKeyword', 'trackUser'];

// Special case
var exportTags = ['CSV'];

// User feedback stuff
var feedback = ['Great!', 'Alright.', 'Okay!', 'Awesome!', 'Cool.'];
var trans = ['Now, ', 'Next, ', ''];

// Used just for UI
var promptMessages = ['Enter the handle of the user you want to add.', 'Enter the name of the database you want to create.', 'What kind of file do you want to export?', 'What do you want to search for?', 'Enter the keyword you want to track.', 'Enter the handle of the user you want to track.'];

var action = -1; // What the user wants to do, like "add user" (index)
var prompt = null; // The special tag, for example the username, DB name, or search term
var db = -1; // Only true if they need DB (index)

// Once we allow submit, we also "lock" input (nothing else can be entered)
// The rationale is that if we get here, what they've added is good to go
// They can still remove though (invalidates submit)
var allowSubmit = false;

$(function() {
    restart();
    notifyUser("Type into the field below!", "success");
});

// Called just after a tag has been added
function afterAdded(event, ui) {
    if (ui.duringInitialization) {
        return;
    }
    
    var dirty = false;

    // User just added an action
    if (inArrayCI(actionTags, ui.tagLabel)) {
        // If an action hasn't already been added
        if (action == -1) {
            action = indexOfCaseInsensitive(actionTags, ui.tagLabel);
        } else {
            dirty = true;
            notifyUser("An action has already been added! Assuming this is a prompt.", "warning");
            prompt = ui.tagLabel;
        }
    }

    // User just added a DB (shouldn't get here for Create DB)
    else if (inArrayCI(dbTags, ui.tagLabel)) {
        // If a DB hasn't already been added
        if (db == -1) {
            db = indexOfCaseInsensitive(dbTags, ui.tagLabel);
        } else {
            dirty = true;
            notifyUser("A database has already been added! Assuming this is a prompt.", "warning");
            prompt = ui.tagLabel;
        }
    }

    // Prompt (at this point, we KNOW a prompt hasn't already been added)
    else {
        prompt = ui.tagLabel;
    }
    if (!dirty) {
        notifyUser(feedback[Math.floor(Math.random() * feedback.length)], "success");
    }
    
    findThingToDo();
}


function loadAction() {
    reloadTagit(actionTags);
    notifyUser("What action would you like to take?", "success", true);
}

function loadDB() {
    reloadTagit(dbTags);
    notifyUser("What database would you like to store data in?", "success", true);
}

function loadPrompt(tags) {
    reloadTagit(tags);
    notifyUser(promptMessages[action], "success", true);
}


/*
Determines what the user should do next and updates message field
*/
function findThingToDo() {
    // Let's load stuff from the dblist (bad that we have to do this every time but...)
    dbTags = document.getElementById("dblist").innerHTML.split(",");
    
    
    if (action == -1) {
        loadAction();
    } else if (prompt == null) {
        loadPrompt((action == 2) ? exportTags : promptTags);
    } else if (db == -1 && action != 1) { // Create DB special case handled here
        loadDB();
    } else {
        allowSubmit = true;
        // Load promptTags to zero out the dropdown hints box
        reloadTagit(promptTags);
        notifyUser("Press SUBMIT to send your query to Twitterator!", "success");
    }
}

// Called just after a tag has been removed
function afterRemoved(event, ui) {
    // We assume this means they invalidated stuff
    allowSubmit = false;

    // User just deleted an action
    if (inArrayCI(actionTags, ui.tagLabel)) {
        // User just deleted THEIR action
        if (indexOfCaseInsensitive(actionTags, ui.tagLabel) == action) {
            action = -1;
        } else {
            prompt = null;
        }
    }

    // User just deleted a DB
    else if (inArrayCI(dbTags, ui.tagLabel)) {
        // User just deleted THEIR DB
        if (indexOfCaseInsensitive(dbTags, ui.tagLabel) == db) {
            db = -1;
        } else {
            prompt = null;
        }
    }

    // User just deleted a prompt
    else {
        prompt = null;
    }

    notifyUser("", "success");
    findThingToDo();
}



function indexOfCaseInsensitive(array, str) {
    for (var i = 0; i < array.length; i++) {
        if (array[i].toUpperCase() === str.toUpperCase()) {
            return i;
        }
    }
    return -1;
}

function inArrayCI(array, str) {
    return (indexOfCaseInsensitive(array, str) != -1);
}

// Called just BEFORE a tag will be added (what do we want to PREVENT adding?)
function beforeAdded(event, ui) {
    // Locked in (cannot add because of allowSubmit)
    if (allowSubmit) {
        return false;
    }

    var isAct = inArrayCI(actionTags, ui.tagLabel);
    var isDB = inArrayCI(dbTags, ui.tagLabel);

    // SPECIAL CASES FIRST

    // Create DB special case
    if (action == 1 && inArrayCI(dbTags, ui.tagLabel)) {
        notifyUser("That database already exists!", "error");
        return false;
    }

    // Other DB special case (user already has a DB added, tries to select Create DB)
    if (db != -1 && indexOfCaseInsensitive(actionTags, ui.tagLabel) == 1) {
        notifyUser("That database already exists!", "error");
        return false;
    }



    // If an action/db has already been entered	and the input is an action/db,
    // we simply pass it off UNLESS a prompt has also been entered!

    // If we get here, we assume it's a prompt
    /*
	We block it if...
	It's a duplicate action and prompt already entered
	It's a duplicate DB and prompt already entered
	It's NOT an action or a DB but a prompt has already been entered
	*/
	
    if (prompt != null) {
        if ((isAct && action != -1) || (isDB && db != -1) || (!isAct && !isDB)) {
            notifyUser("You have already entered a prompt!", "error");
            return false;
        }
    }

    // Export special case (prompt must be one of the characters)
    if (action == 2 && !inArrayCI(exportTags, ui.tagLabel)) {
        if (isAct || (isDB && db != -1) || (!isAct && !isDB)) {
            notifyUser("Unrecognized export type", "error");
            return false;
        }
    }

    // Export other case (user already has weird thing added, tries to select Export)
    if (prompt != null) {
        if (!inArrayCI(exportTags, prompt) && indexOfCaseInsensitive(actionTags, ui.tagLabel) == 2) {
            notifyUser("Unrecognized export type", "error");
            return false;
        }
    }
    return true;
}



function reloadTagit(tags) {
    $('#omniField').tagit({
        availableTags: tags,
        singleField: true,
        showAutocompleteOnFocus: true,
        caseSensitive: false,
        removeConfirmation: true,
        singleFieldNode: $('#inputField'),
        afterTagAdded: afterAdded,
        afterTagRemoved: afterRemoved,
        beforeTagAdded: beforeAdded
    });
}

// Where level is one of: success, warning, error
function notifyUser(msg, level, append) {
    append = (typeof append === "undefined") ? false : append;
    var oldMsg = "";
    if (append) {
        var newTrans = trans[Math.floor(Math.random() * trans.length)];
        if (newTrans != "") {
            msg = msg.toLowerCase();
        }
        oldMsg = document.getElementById("prompt").innerHTML + " " + newTrans;
    }
    document.getElementById("prompt").innerHTML = oldMsg + msg;
    if (!append) {
        document.getElementById("prompt").className = "alert-msg rnd8 " + level;
    }
}

// Call this to reset the omnibox back to initial state
function restart() {
    action = -1;
    prompt = null;
    db = -1;
    allowSubmit = false;
    reloadTagit(actionTags);
    findThingToDo();
}

// Called when user hits the submit button
function submitInput() {
    if (!allowSubmit) {
        if (action == -1) {
            notifyUser("You must enter an action to take!", "error");
        } else if (prompt == null) {
            notifyUser("You must enter a query!", "error");
        } else {
            notifyUser("You must enter a valid database!", "error");
        }
        return;
    }
    notifyUser("Submitting input to Twitterator...", "success");
    
    sendPrompt(serverActs[action], prompt, dbTags[db]);

    // We should really lock everything until the server sends a reply...then do the lines below

    
    $("#omniField").tagit("removeAll");
    restart();
}



/**
 * Regular HTTP AJAX stuff follows, with XML response protocol.
 * This forces the server to use our AJAX polling handler, which is the
 * "default" if everything else fails. Very hacky.
 */
function ajaxPoll(value) {
    var req;
    if (typeof XMLHttpRequest != "undefined") {
        req = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }
    req.open("GET", "bogus.html?query=" + value, true);
    req.onreadystatechange = function() {ajaxCallback(req)};
    req.send(null);
}

function ajaxCallback(req) {
    if (req.readyState == 4 && req.status == 200 && req.getResponseHeader("Content-Type").indexOf("xml") != -1) {
        var locations = req.responseXML.getElementsByTagName("location");
        var messages = req.responseXML.getElementsByTagName("message");
        for (var i = 0; i < locations.length; i++) {
            var location = locations[i].childNodes[0].nodeValue;
            var message = messages[i].childNodes[0].nodeValue;
            document.getElementById(location).innerHTML = message;
        }
    }
}

function startup() {
    ajaxPoll("startup");
}

function sendPrompt(action, prompt, db) {
    ajaxPoll("omnibox&action=" + action + "&prompt=" + prompt + "&db=" + db);
}