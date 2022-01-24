function btn_submit(cmd) {

    if (cmd == 'delete' || cmd == 'update' || cmd == 'add') {
        if (document.forms[0].ENTEXAM_SCHOOLCD.value == '') {
            alert('{rval MSG308}');
            return true;
        }
        if (cmd == 'update' || cmd == 'add') {
            if (document.forms[0].FINSCHOOLCD.value == '') {
                alert('{rval MSG308}');
                return true;
            }
        }
    }

    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    }

    if (cmd == 'update' && !confirm('{rval MSG102}')) {
        return true;
    }

    if (cmd == 'reset') {
        if (document.forms[0].ENTEXAM_SCHOOLCD.value == '') {
            alert('{rval MSG308}');
            return true;
        }
        if (!confirm('{rval MSG106}')) {
            return false;
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
