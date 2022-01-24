//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var getcmd = document.forms[0].GET_CMD.value;
    var getcounter = document.forms[0].GET_COUNTER.value;
    var staffCd = '';
    var staffName = '';

    if (getcmd === 'substaff') {
        staffCd = 'RETURN_STAFFCD-' + String(getcounter);
        staffName = 'RETURN_STAFF_NAME-' + String(getcounter);
    } else if (getcmd == 'substaff2') {
        staffCd = 'RETURN_STAFFCD';
        staffName = 'RETURN_STAFF_NAME';
    } else if (getcmd == 'substaffProctor') {
        staffCd = 'PROCTOR_STAFFCD-' + String(getcounter);
        staffName = 'PROCTOR_STAFF_NAME-' + String(getcounter);
    }

    var chk = document.forms[0]['CHECK[]'];
    var sep = (sep1 = sep2 = '');
    var Ch_txt1 = '';
    var checkcount = 0;
    for (var i = 0; i < chk.length; i++) {
        if (chk[i].checked) {
            //職員名を取得
            var set_name = document.forms[0]['STAFFNAME' + i].value;
            Ch_txt1 = Ch_txt1 + sep1 + chk[i].value;
            sep1 = '';
            checkcount++;
        }
    }

    if (checkcount > 1) {
        alert('職員は1名のみ指定してください。');
        return false;
    }

    if (checkcount <= 0) {
        Ch_txt1 = '';
        set_name = '';
    }

    top.main_frame.document.forms[0].elements[staffCd].value = Ch_txt1;
    top.main_frame.document.forms[0].elements[staffName].value = set_name;
    top.main_frame.closeit();
}

function checkboxSel(obj) {
    if (!obj.checked) {
        obj.checked = false;
    } else {
        var chk = document.forms[0]['CHECK[]'];
        for (var i = 0; i < chk.length; i++) {
            chk[i].checked = false;
        }
        obj.checked = true;
    }
}
