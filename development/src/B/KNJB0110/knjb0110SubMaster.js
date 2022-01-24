//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var getcmd = document.forms[0].GET_CMD.value;
    var getcounter = document.forms[0].GET_COUNTER.value;
    var SET_UP_STAFFCD = 'UP_STAFFCD' + String(getcounter);
    var SET_UP_STAFFNAME = 'UP_STAFFNAME' + String(getcounter);
    var chk = document.forms[0]['CHECK\[\]'];
    var sep = sep1 = sep2 = "";
    var Ch_txt1 = "";
    var checkcount = 0;
    for (var i=0; i < chk.length; i++)
    {
        if (chk[i].checked) {
            //職員名を取得
            var set_name = document.forms[0]["STAFFNAME" + i].value;
            Ch_txt1 = Ch_txt1 + sep1 + chk[i].value;
            sep1    = "";
            checkcount++;
        }
    }
    if (getcmd === 'select_staff') {
        if (checkcount > 1) {
            alert("職員は1名のみ指定してください。");
        } else {
            top.main_frame.document.forms[0].elements[SET_UP_STAFFCD].value = Ch_txt1;
            top.main_frame.document.getElementById(SET_UP_STAFFNAME).innerHTML = set_name;
            top.main_frame.closeit();
        }
    }
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}
