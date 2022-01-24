function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";

    //削除確認
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消確認
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    //必須入力チェック
    if (cmd == 'add' || cmd == 'delete') {
        if (document.forms[0].GRADE.value == '') {
            alert('{rval MSG301}\n（学年）');
            return false;
        }
        if (document.forms[0].IBPRG_COURSE.value == '') {
            alert('{rval MSG301}\n（IBコース）');
            return false;
        }
        if (document.forms[0].SUBCLASS.value == '') {
            alert('{rval MSG301}\n（科目）');
            return false;
        }
    }

    //ＣＳＶ処理
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

//段階数チェック
function seq(max) {
    var seq;
    seq = document.forms[0].MAX_SEQ.value;

    if (seq > 0) {
    } else {
        alert('{rval MSG901}'+'\n1以上を入力してください。\n(段階数)');
        document.forms[0].MAX_SEQ.focus();
        return false;
    }

    if (seq == max) {
        return false;
    }

    if (seq > 100) {
        alert('{rval MSG913}'+'\n段階数は100を超えてはいけません。');
        return false;
    } 

    document.forms[0].cmd.value = 'seq';
    document.forms[0].submit();
    return false;
}

function checkDecimal(obj) {
    var decimalValue = obj.value
    var check_result = false;

    if (decimalValue != '') {
        //空じゃなければチェック
        if (decimalValue.match(/^[0-9]+(\.[0-9]+)?$/)) {
            check_result = true;
        }
    } else {
        check_result = true;
    }

    if (!check_result) {
        alert('数字を入力して下さい。');
        obj.value = "";
    }

    //正しい値ならtrueを返す
    return check_result;
}

//ファイルアップローダーの値を削除
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
