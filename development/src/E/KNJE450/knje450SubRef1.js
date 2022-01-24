//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var chk = document.forms[0]['CHECK\[\]'];
    var sep1 = sep2 = "";
    var Ch_txt1 = "";

    for (var i=0; i < chk.length; i++) {
        if (chk[i].checked) {
            Ch_txt1 = Ch_txt1 + sep1 + chk[i].value;
            sep1    = ",";
        }
    }

    if (top.main_frame.right_frame.document.forms[0].HANDICAP.value != "") {
        sep2 = ",";
    }
    top.main_frame.right_frame.document.forms[0].HANDICAP.value = top.main_frame.right_frame.document.forms[0].HANDICAP.value + sep2 + Ch_txt1;
    top.main_frame.right_frame.closeit();
}

//対象項目行選択（チェックボックスon/off）
var selectedRow = 0;
function selectRowList() {
    var list = document.getElementById('list');
    var chk = document.forms[0]["CHECK\[\]"];

    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }

    selectedRow = event.srcElement.parentElement.rowIndex;

    //チェックon/off
    if (chk.length) {
        chk[selectedRow].checked = !chk[selectedRow].checked;
    }
}
