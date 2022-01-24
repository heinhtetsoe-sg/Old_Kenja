// Add by PP for focus 2020-02-03 start

function current_cursor(para) {
    sessionStorage.setItem("KNJE390SubFrom1_2_CurrentCursor", para);
}

function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJE390SubFrom1_2_CurrentCursor")).focus();
    // remove item
    sessionStorage.removeItem('KNJE390SubFrom1_2_CurrentCursor');  
}
function current_cursor_list() {
    document.getElementById('P_J_SCHOOL_CD').focus();
    // remove item
    sessionStorage.removeItem('P_J_SCHOOL_CD');  
}

// Add by PP  focus 2020-02-20 end

function btn_submit(cmd) {
    // Add by PP for CurrentCursor blur 2020-02-03 start 
    if (sessionStorage.getItem("KNJE390SubFrom1_2_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE390SubFrom1_2_CurrentCursor")).blur();
    }
    // Add by PP for CurrentCursor blur 2020-02-20 end 
    if (cmd == 'educate1_delete' && !confirm('{rval MSG103}')){
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

window.onload = function(e) {
    var keta = document.forms[0].useFinschoolcdFieldSize.value;
    if (keta == '12' && document.forms[0].P_J_SCHOOL_CD) {
        document.forms[0].P_J_SCHOOL_CD.maxlength = 12;
        document.forms[0].P_J_SCHOOL_CD.size = 12;
    }
    // Add by PP for loading focus 2020-02-03 start
    if (sessionStorage.getItem("KNJE390SubFrom1_2_CurrentCursor") != null) {
       document.title = "";
       document.getElementById(sessionStorage.getItem("KNJE390SubFrom1_2_CurrentCursor")).focus();

        // remove item
        sessionStorage.removeItem('KNJE390SubFrom1_2_CurrentCursor');  
    } 
     setTimeout(function () {
            document.title = TITLE; 
    }, 100);
    // Add by PP loading focus 2020-02-20 end
};

