function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
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

function changeFlg(obj){
    document.forms[0].CHANGE_FLG.value = '1';

    document.forms[0].HID_APPLICANTDIV.value    = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled     = true;
    document.forms[0].TESTDIV.disabled          = true;

//    document.getElementById('ROWID' + obj.id).style.background="yellow";
//    obj.style.background="yellow";
}

function checkVal(obj, remarkNo){
    var str = obj.value;
    var nam = obj.name;

    //空欄
    if (str == '') { 
        return;
    }

    //英小文字から大文字へ自動変換
    obj.value = str.toUpperCase();
    str = str.toUpperCase();

    //資料
    var errFlg = true;
    var errMsg = '';
    if (remarkNo == "1") {
        errFlg = !str.match(/A|B|C|D|E/);
        errMsg = '「A, B, C, D, E」を入力して下さい。';
    } else {
        errFlg = !str.match(/A|B|C/);
        errMsg = '「A, B, C」を入力して下さい。';
    }
    if (errFlg) {
        alert('{rval MSG901}' + errMsg);
        obj.value = '';
        obj.focus();
        return;
    }
}

//Enterキーで移動
function keyChangeEntToTab2(obj) {
    if (window.event.keyCode != 13) {
        return;
    }

    //移動可能なオブジェクト
    var textArray = document.forms[0].HID_TEXT.value.split(',');
    var examnoArray = document.forms[0].HID_EXAMNO.value.split(',');
    var col = textArray.indexOf(obj.name.split('-')[0]);
    var line = examnoArray.indexOf(obj.name.split('-')[1]);
    //1列目の項目
    var isFirstItem = col == 0 ? true : false;
    //最終列の項目
    var isLastItem = col == textArray.length - 1 ? true : false;
    //1行目の生徒
    var isFirstStudent = line == 0 ? true : false;
    //最終行の生徒
    var isLastStudent = line == examnoArray.length - 1 ? true : false;

    //方向キー Ent13 Tab9 ←37 ↑38 →39 ↓40
    var moveEnt = document.forms[0].MOVE_ENTER[0].checked ? 40 : 39;
    if (window.event.shiftKey) {
        moveEnt = document.forms[0].MOVE_ENTER[0].checked ? 38 : 37;
    }
    if (moveEnt == 37) {
        if (isFirstItem && isFirstStudent) {
            obj.focus();
            return;
        }
        if (isFirstItem) {
            targetname = textArray[(textArray.length - 1)] + '-' + examnoArray[(line - 1)];
            targetObject = document.getElementById(targetname);
            targetObject.focus();
            return;
        }
        targetname = textArray[(col - 1)] + '-' + examnoArray[line];
        targetObject = document.getElementById(targetname);
        targetObject.focus();
        return;
    }
    if (moveEnt == 38) {
        if (isFirstItem && isFirstStudent) {
            obj.focus();
            return;
        }
        if (isFirstStudent) {
            targetname = textArray[(col - 1)] + '-' + examnoArray[(examnoArray.length - 1)];
            targetObject = document.getElementById(targetname);
            targetObject.focus();
            return;
        }
        targetname = textArray[col] + '-' + examnoArray[(line - 1)];
        targetObject = document.getElementById(targetname);
        targetObject.focus();
        return;
    }
    if (moveEnt == 39 || moveEnt == 13) {
        if (isLastItem && isLastStudent) {
            obj.focus();
            return;
        }
        if (isLastItem) {
            targetname = textArray[0] + '-' + examnoArray[(line + 1)];
            targetObject = document.getElementById(targetname);
            targetObject.focus();
            return;
        }
        targetname = textArray[(col + 1)] + '-' + examnoArray[line];
        targetObject = document.getElementById(targetname);
        targetObject.focus();
        return;
    }
    if (moveEnt == 40) {
        if (isLastItem && isLastStudent) {
            obj.focus();
            return;
        }
        if (isLastStudent) {
            targetname = textArray[(col + 1)] + '-' + examnoArray[0];
            targetObject = document.getElementById(targetname);
            targetObject.focus();
            return;
        }
        targetname = textArray[col] + '-' + examnoArray[(line + 1)];
        targetObject = document.getElementById(targetname);
        targetObject.focus();
        return;
    }
}
