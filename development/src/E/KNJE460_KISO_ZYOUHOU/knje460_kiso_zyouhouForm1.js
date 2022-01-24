// Add by PP for loading focus 2020-02-03 start
setTimeout(function () {
window.onload = new function () {
    if (sessionStorage.getItem("KNJE460KisoZyouhouForm1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJE460KisoZyouhouForm1_CurrentCursor")).focus();
            // remove item
            sessionStorage.removeItem('KNJE460KisoZyouhouForm1_CurrentCursor');  
        } else {
            // start loading focus
            document.getElementById('screen_id').focus();
    }
    setTimeout(function () {
            document.title = TITLE; 
    }, 100);
 }
}, 800);
function current_cursor(para) {
    sessionStorage.setItem("KNJE460KisoZyouhouForm1_CurrentCursor", para);
}
function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJE460KisoZyouhouForm1_CurrentCursor")).focus();
    // remove item
    sessionStorage.removeItem('KNJE460KisoZyouhouForm1_CurrentCursor'); 
}
// Add by PP loading focus 2020-02-20 end
//サブミット
function btn_submit(cmd) {
    /* Add by PP for CurrentCursor 2020-02-03 start */
    if (sessionStorage.getItem("KNJE460KisoZyouhouForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE460KisoZyouhouForm1_CurrentCursor")).blur();
    }
    /* Add by PP for CurrentCursor 2020-02-20 end */
    if (cmd == "subform1_loadpastyear") {
        if (document.forms[0].PASTYEAR.value == "") {
            alert('{rval MSG301}' + "年度を選択してください。");
            return false;
        } else {
            document.forms[0].HID_PASTYEARLOADFLG.value = "1";
            cmd = "edit";  //イベントをeditにする
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}