function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
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
    if (document.forms[0].SCHCNT.value != '' && document.forms[0].CHECKCNT.value != ''){
        document.forms[0].btn_update.disabled = false;
        document.forms[0].btn_reset.disabled  = false;
    }else if(document.forms[0].SCHCNT.value == document.forms[0].CHECKCNT.value){
        document.forms[0].btn_update.disabled = false;
        document.forms[0].btn_reset.disabled  = false;
    }else {
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled  = true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function check(obj){
    
    if (eval(document.forms[0].SCHCNT.value) < eval(document.forms[0].CHECKCNT.value)){
        alert('回数よりチェック用の\n\n値が大きいです。');
        document.forms[0].CHECKCNT.value = '';
        document.forms[0].CHECKCNT.focus();
    }
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