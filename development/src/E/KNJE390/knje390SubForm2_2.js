// Add by PP for loading focus 2020-02-03 start
window.onload = function () {
    if (sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor915") != null) {
        document.getElementsByName(sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor915"))[0].focus();
        var value = document.getElementsByName(sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor915"))[0].value;
        document.getElementsByName(sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor915"))[0].value = "";
        document.getElementsByName(sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor915"))[0].value = value;
        sessionStorage.removeItem("KNJE390SubForm2_2_CurrentCursor915");
    } else {
        sessionStorage.removeItem("KNJE390SubForm2_2_CurrentCursor915");
        if (sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor") != null) {
            if (sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor") == 'CHECK_DATE') {
                document.getElementById('CHECK_DATE').focus();
            } else {
                document.title = "";
                document.getElementById(sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor")).focus();
            }
             // remove item
            sessionStorage.removeItem('KNJE390SubForm2_2_CurrentCursor');
        }
    }
    setTimeout(function () {
        document.title = TITLE; 
    }, 100);
}

function current_cursor(para) {
    sessionStorage.setItem("KNJE390SubForm2_2_CurrentCursor", para);
}

function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor")).focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubForm2_2_CurrentCursor'); 
}

// choice cursor
function current_cursor_choice() {
    document.getElementById('CHALLENGED_NAMES').focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubForm5_CurrentCursor'); 
}

// choice cursor Inspection
function current_cursor_Inspection() {
    document.getElementById('CHECK_CENTER_TEXT').focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubForm2_2_CurrentCursor'); 
}
// Add by PP loading focus 2020-02-20 end

function btn_submit(cmd) {
    // Add by PP for CurrentCursor 2020-01-10 start 
    if (sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor") != null) {
        document.getElementById(sessionStorage.getItem("KNJE390SubForm2_2_CurrentCursor")).blur();
    }
    // Add by PP for CurrentCursor 2020-01-17 end 
    if (cmd == 'check2_delete' && !confirm('{rval MSG103}')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}
