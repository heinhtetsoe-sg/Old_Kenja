//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;

    var Ch_txt = "";

    for (var i = 0; i < datacnt; i++) {
        if (document.getElementById("CHECK" + i).checked) {
            Ch_txt = Ch_txt + document.getElementById("CHECK" + i).value;
        }
    }
    parent.document.forms[0].OUTGO_NAME.value = parent.document.forms[0].OUTGO_NAME.value + Ch_txt;
    parent.closeit();
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}
