function btn_submit(cmd) {
    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].FINSCHOOLCD.value == ''){
            alert('学校コードを入力してください');
            return false;
        }
        if (document.forms[0].FINSCHOOL_PREF_CD.value == ''){
            alert('都道府県を入力してください');
            return false;
        }
    }
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    if (cmd == 'exec') {
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
    if (obj.value == '1') { //1は取り込み
        document.forms[0].FILE.disabled = false;
    } else {
        document.forms[0].FILE.disabled = true;
        type_file = document.getElementById('type_file'); //ファイルアップローダーの値を消す
        var innertString = type_file.innerHTML;
        type_file.innerHTML = innertString;
    }
}

//あり・なしチェックボックスクリック
function chgLBL(chk){
    //alert(chk.checked);
    if (chk.checked == false){
    //alert(1);
        chk.checked = false;
        chk.value   = '0';
    } else {
    //alert(2);
        chk.checked = true;
        chk.value   = '1';
    }
}
