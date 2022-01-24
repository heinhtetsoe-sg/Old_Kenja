function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        alert('{rval MSG303}');
        return false;
    }

    //終了
    if (cmd == 'end') {
        if ((document.forms[0].APPLICANTDIV.disabled) || (document.forms[0].TESTDIV.disabled)) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//入力項目を変更した場合、背景色を黄色表示
function bgcolorYellow(obj, examno) {
    change_flg = true;

    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_SORT.value            = (document.forms[0].SORT2.checked) ? "2" : "1";
    document.forms[0].HID_SHOW.value            = (document.forms[0].SHOW2.checked) ? "2" : "1";
    if (document.forms[0].SHOW2.checked) {
        document.forms[0].HID_FS_CD.value       = document.forms[0].FS_CD.options[document.forms[0].FS_CD.selectedIndex].value;
    } else {
        document.forms[0].HID_FS_CD.value       = "";
    }

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].SORT1.disabled            = true;
    document.forms[0].SORT2.disabled            = true;
    document.forms[0].SHOW1.disabled            = true;
    document.forms[0].SHOW2.disabled            = true;
    if (document.forms[0].SHOW2.checked) {
        document.forms[0].FS_CD.disabled        = true;
    }

    document.getElementById('ROWID' + examno).style.background = "yellow";
//    obj.style.background = "yellow";
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(obj) {
    //対象項目
    var setField = document.forms[0].setField.value.split(",");
    //対象生徒
    var examnoArray = document.forms[0].HID_EXAMNO.value.split(",");
    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }

    //移動可能なオブジェクト
    var targetFieldArray = new Array();
    y = 0;
    for (var e = 0; e < examnoArray.length; e++) {
        for (var i = 0; i < setField.length; i++) {
            targetFieldArray[y++] = setField[i]+'-'+examnoArray[e];
        }
    }

    for (var i = 0; i < targetFieldArray.length; i++) {
        if (targetFieldArray[i] == obj.name) {
            targetObject = eval("document.forms[0][\"" + targetFieldArray[(i + 1)] + "\"]");
            targetObject.focus();
            return;
        }
    }
}
