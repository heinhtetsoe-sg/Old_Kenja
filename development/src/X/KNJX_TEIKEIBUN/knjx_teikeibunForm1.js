//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var getcmd = document.forms[0].GET_CMD.value;
    var chk = document.forms[0]['CHECK\[\]'];
    var sep1 = sep2 = "";
    var Ch_txt1 = "";
    var parentFrame = this.parent.document;
    var existChecked = false;

    if (datacnt === "1") {
        if (chk.checked) {
            Ch_txt1 = chk.value;
            existChecked = true;
        }
    } else {
        for (var i=0; i < datacnt; i++)
        {
            if (chk[i].checked) {
                Ch_txt1 = Ch_txt1 + sep1 + chk[i].value;
                sep1    = "";
                existChecked = true;
            }
        }
    }

    if (!existChecked) {
        parent.closeit();
        return false;
    }

    if (getcmd === 'teikei') {
        var parentText = parentFrame.getElementsByName(document.forms[0].TEXTBOX.value)[0];
        if (parentText) {
            if (parentText.value != "") {
                sep2 = "";
            }
            parentText.value = parentText.value + sep2 + Ch_txt1;
            parentText.style.backgroundColor = "#FFCCFF";
        }

    }
    parent.closeit();
}
