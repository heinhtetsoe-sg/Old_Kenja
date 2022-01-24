// Add by PP for focus 2020-01-20 start
window.onload = function () { 
    if (sessionStorage.getItem("KNJXattendEntermarkForm_CurrentCursor") != null) {
        document.getElementById(sessionStorage.getItem("KNJXattendEntermarkForm_CurrentCursor")).focus();
    } else {
        document.getElementById('screen_id').focus();
    }
        
}
function current_cursor(para) {
     sessionStorage.setItem("KNJXattendEntermarkForm_CurrentCursor", para);
}
// Add by PP for focus 2020-01-31 end
function btn_submit(cmd) {
    /* Add by PP for CurrentCursor 2020-01-20 start */
    if (sessionStorage.getItem("KNJXattendEntermarkForm_CurrentCursor") != null) {
         document.title = "";
         document.getElementById(sessionStorage.getItem("KNJXattendEntermarkForm_CurrentCursor")).blur();
    }
    /* Add by PP for CurrentCursor 2020-01-31 end */
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
