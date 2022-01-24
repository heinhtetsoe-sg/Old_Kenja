function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'update') {
        var flg = document.forms[0].PASSWORD.value;
        var flg2;
        flg2 = flg.indexOf("*", 0);
        if (flg2 > -1) {
            if (flg == document.forms[0].UME_PASSWORD.value){
            } else {
                alert("パスワードに(*)が含まれています");
                return false;
            }
        }
    }

    if (cmd == 'exec') {
        document.forms[0].encoding = "multipart/form-data";
        if (document.forms[0].OUTPUT[1].checked && document.forms[0].FILE.value == '') {
            alert('ファイルを指定してください');
            return false;
        }

        if (document.forms[0].OUTPUT[0].checked) {
            cmd = 'downloadHead';
        } else if (document.forms[0].OUTPUT[1].checked) {
            cmd = 'uploadCsv';
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = 'downloadCsv';
        } else if (document.forms[0].OUTPUT[3].checked) {
            cmd = 'downloadError';
        } else {
            alert('ラジオボタンを選択してください。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
function changeRadio(obj) {
    var type_file;
    document.forms[0].OUTPUT.value = obj.value;
    if (obj.value == '2') { //2は取り込み
        document.forms[0].FILE.disabled = false;
    } else {
        document.forms[0].FILE.disabled = true;
        type_file = document.getElementById('type_file'); //ファイルアップローダーの値を消す
        var innertString = type_file.innerHTML;
        type_file.innerHTML = innertString;
    }
}
function passHyouji(obj) {
    if (obj.checked == true) {
        document.forms[0].PASSWORD.value = document.forms[0].HID_PASSWORD.value
    } else {
        document.forms[0].PASSWORD.value = document.forms[0].UME_PASSWORD.value
    }

    return;
}
