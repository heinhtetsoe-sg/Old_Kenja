//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var getcmd = document.forms[0].GET_CMD.value;
    var chk = document.forms[0]['CHECK\[\]'];
    var sep1 = sep2 = "";
    var Ch_txt1 = "";
    var parentFrame = top.main_frame.right_frame.document.getElementById('cframe').contentWindow.document;
    var existChecked = false;

    for (var i=0; i < chk.length; i++)
    {
        if (chk[i].checked) {
            Ch_txt1 = Ch_txt1 + sep1 + chk[i].value;
            sep1    = "";
            existChecked = true;
        }
    }

    if (!existChecked) {
        parent.closeit();
        return false;
    }

    if (getcmd === 'teikei_act') {
        if (parentFrame.forms[0].TOTALSTUDYACT.value != "") {
            sep2 = "";
        }
        parentFrame.forms[0].TOTALSTUDYACT.value = parentFrame.forms[0].TOTALSTUDYACT.value + sep2 + Ch_txt1;
        parentFrame.forms[0].TOTALSTUDYACT.style.backgroundColor = "#FFCCFF";
    } else {
        if (parentFrame.forms[0].TOTALSTUDYVAL.value != "") {
            sep2 = "";
        }
        parentFrame.forms[0].TOTALSTUDYVAL.value = parentFrame.forms[0].TOTALSTUDYVAL.value + sep2 + Ch_txt1;
        parentFrame.forms[0].TOTALSTUDYVAL.style.backgroundColor = "#FFCCFF";
    }
    parent.closeit();
}
