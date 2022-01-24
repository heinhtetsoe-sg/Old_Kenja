// Add by PP for loading focus 2020-02-03 start
window.onload = function () { 
    if (sessionStorage.getItem("KNJE390Family_CurrentCursor") != null) {
        if (sessionStorage.getItem("KNJE390Family_CurrentCursor") == 'RELANAME') {
            document.getElementById('RELANAME').focus();
            // for cursor in textbox
            var value = document.getElementById('RELANAME').value;
            document.getElementById('RELANAME').value = "";
            document.getElementById('RELANAME').value = value;
        } else {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJE390Family_CurrentCursor")).focus();
        }
        // remove item
        sessionStorage.removeItem('KNJE390Family_CurrentCursor');  
    } else {
        // first loading focus
        document.getElementById('screen_id').focus();
    }  
    setTimeout(function () {
            document.title = TITLE; 
    }, 1000);
}

function current_cursor(para) {
    sessionStorage.setItem("KNJE390Family_CurrentCursor", para);
}

// Add by PP loading focus 2020-02-20 end

function btn_submit(cmd) {
    // Add by PP for CurrentCursor blur 2020-02-03 start 
    if (sessionStorage.getItem("KNJE390Family_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE390Family_CurrentCursor")).blur();
    }
    // Add by PP for CurrentCursor blur 2020-02-20 end 
    if (cmd == 'family_delete' && !confirm('{rval MSG103}')) {
        // Add by PP for delete focus blur 2020-02-03 start
        document.getElementById('btn_delete').blur();
        // Add by PP delete focus blur 2020-02-20 end
        return true;
    }
    if (cmd == 'family_insert' || cmd == 'family_update') {
        if (document.forms[0].RELANAME.value == "") {
            alert('{rval MSG301}' + '(氏名)');
            // Add by PP for insert data (氏名) focus 2020-02-03 start
            document.getElementById('RELANAME').focus();
            sessionStorage.removeItem('KNJE390Family_CurrentCursor'); 
            setTimeout(function () {
            document.title = TITLE; 
            }, 1000);
            // Add by PP insert data (氏名) focus 2020-02-20 end
            return false;
        } else if (document.forms[0].RELAKANA.value == "") {
            alert('{rval MSG301}' + '(氏名かな)');
            // Add by PP for insert data (氏名かな) focus 2020-02-03 start
            document.getElementById('RELAKANA').focus();
            sessionStorage.removeItem('KNJE390Family_CurrentCursor'); 
            setTimeout(function () {
            document.title = TITLE; 
            }, 1000);
            // Add by PP insert data (氏名かな) focus 2020-02-20 end
            return false;
        }
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
