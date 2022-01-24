// Add by PP for londing focus 2020-02-03 start
window.onload = function () {
    if (sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor915") != null) {
        document.getElementsByName(sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor915"))[0].focus();
        var value = document.getElementsByName(sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor915"))[0].value;
        document.getElementsByName(sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor915"))[0].value = "";
        document.getElementsByName(sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor915"))[0].value = value;
        sessionStorage.removeItem("KNJE390SubFrom1_5_CurrentCursor915");
    } else {
        sessionStorage.removeItem("KNJE390SubFrom1_5_CurrentCursor915");
        if (sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor")).focus();

            // remove item
            sessionStorage.removeItem('KNJE390SubFrom1_5_CurrentCursor');
        }
    } 
    setTimeout(function () {
            document.title = TITLE; 
    }, 100);
}
function current_cursor(para) {
    sessionStorage.setItem("KNJE390SubFrom1_5_CurrentCursor", para);
}
function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor")).focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubFrom1_5_CurrentCursor'); 
}

 // choice cursor
function current_cursor_choice() {
    document.getElementById('CENTER_NAME').focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubFrom1_5_CurrentCursor'); 
}

// choice cursor Inspection
function current_cursor_Inspection() {
    document.getElementById('SERVICE_CENTER_TEXT').focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubForm2_2_CurrentCursor'); 
}

// Add by PP for londing focus 2020-02-20 end
function btn_submit(cmd) {
    // Add by PP for CurrentCursor blur 2020-02-03 start 
    if (sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE390SubFrom1_5_CurrentCursor")).blur();
    }
    // Add by PP for CurrentCursor blur 2020-02-20 end 
    if (cmd == 'welfare1_delete' && !confirm('{rval MSG103}')){
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

//事前チェック
function preCheck(msg) {
    alert('{rval MSG305}\n('+msg+')');
}
