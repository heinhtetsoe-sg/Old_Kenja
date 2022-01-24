// Add by PP for londing focus 2020-02-03 start
window.onload = function () { 
    if (sessionStorage.getItem("KNJE390SubSearchCheckCenter_CurrentCursor") != null) {
        document.title = "";
        if (sessionStorage.getItem("KNJE390SubSearchCheckCenter_CurrentCursor") == 'btn_search') {
            document.getElementById('search_result').focus();
        }
        // remove item
        sessionStorage.removeItem('KNJE390SubSearchCheckCenter_CurrentCursor');  
    } else {
        // first loading focus
        document.getElementById('screen_id').focus();
    }  
    setTimeout(function () {
            document.title = TITLE; 
    }, 100);
}
function current_cursor(para) {
    sessionStorage.setItem("KNJE390SubSearchCheckCenter_CurrentCursor", para);
}
// Add by PP for londing focus 2020-02-20 end
function btn_submit(cmd) {
    // Add by PP for CurrentCursor 2020-02-03 start 
    if (sessionStorage.getItem("KNJE390SubSearchCheckCenter_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE390SubSearchCheckCenter_CurrentCursor")).blur();
    }
    // Add by PP for CurrentCursor 2020-02-20 end 
    if (cmd == 'check_center_search' && document.forms[0].AREACD.value == "" && document.forms[0].NAME.value == "" && document.forms[0].ADDR1.value == "" && document.forms[0].ADDR2.value == "") {
        alert('検索条件には最低一つ選択または入力してください。');
        // Add by PP for PC-Talker focus 2020-02-03 start
        document.getElementById('btn_search').focus();
        sessionStorage.removeItem('KNJE390SubSearchCheckCenter_CurrentCursor');
        setTimeout(function () {
            document.title = TITLE; 
         }, 100);
        // Add by PP for PC-Talker focus 2020-02-20 end
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//選択ボタン押し下げ時の処理
function btn_check_submit(datacnt) {
    var comma = ",";
    if (datacnt == 0) return false;
    var getcmd = document.forms[0].GET_CMD.value;
    var chk = document.forms[0]['CHECK\[\]'];
    var sep = "";
    var Ch_txt = "";
    var setText = "";
    var i;
    if (chk.length == undefined) {
        var getText = chk.value;
        setText = Ch_txt + sep + getText;
        sep = comma;
    } else {
        for (i = 0; i < chk.length; i++) {
            if (chk[i].checked && chk[i].value) {
                setText += sep + chk[i].value;
                sep = comma;
            }
        }
    }
    if (top.main_frame.right_frame.document.forms[0].CHECK_CENTER_TEXT.value) {
        top.main_frame.right_frame.document.forms[0].CHECK_CENTER_TEXT.value += comma;
    }
    top.main_frame.right_frame.document.forms[0].CHECK_CENTER_TEXT.value += setText;
    top.main_frame.right_frame.closeit();
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}
