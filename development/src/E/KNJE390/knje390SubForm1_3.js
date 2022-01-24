// Add by PP for londing focus 2020-02-03 start
window.onload = function () {
    if (sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor915") != null) {
        document.getElementsByName(sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor915"))[0].focus();
        var value = document.getElementsByName(sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor915"))[0].value;
        document.getElementsByName(sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor915"))[0].value = "";
        document.getElementsByName(sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor915"))[0].value = value;
        sessionStorage.removeItem("KNJE390SubFrom1_3_CurrentCursor915");
    } else {
        sessionStorage.removeItem("KNJE390SubFrom1_3_CurrentCursor915");
        if (sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor")).focus();

            // remove item
            sessionStorage.removeItem('KNJE390SubFrom1_3_CurrentCursor');
        }
    } 
    setTimeout(function () {
            document.title = TITLE; 
    }, 100);
}
function current_cursor(para) {
    sessionStorage.setItem("KNJE390SubFrom1_3_CurrentCursor", para);
}
function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor")).focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubFrom1_3_CurrentCursor'); 
}

 // choice cursor
function current_cursor_choice() {
    document.getElementById('CENTER_NAME').focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubFrom1_3_CurrentCursor'); 
}
// Add by PP for londing focus 2020-02-20 end
function btn_submit(cmd) {
    // Add by PP for CurrentCursor blur 2020-02-03 start 
    if (sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE390SubFrom1_3_CurrentCursor")).blur();
    }
    // Add by PP for CurrentCursor blur 2020-02-20 end 
    if (cmd == 'medical1_delete' && !confirm('{rval MSG103}')){
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
