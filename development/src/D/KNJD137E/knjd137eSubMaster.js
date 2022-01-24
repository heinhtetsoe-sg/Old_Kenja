//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var chk = document.forms[0]['CHECK\[\]'];
    var Ch_txt = "";

    for (var i=0; i < chk.length; i++) {
        if (chk[i].checked) {
            Ch_txt = Ch_txt + chk[i].value;
        }
    }
    top.main_frame.right_frame.document.forms[0].REMARK1.value = top.main_frame.right_frame.document.forms[0].REMARK1.value + Ch_txt;
    top.main_frame.right_frame.closeit();
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}
