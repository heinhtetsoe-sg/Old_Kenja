function btn_submit(cmd) {
    if (cmd == 'exec' && document.forms[0].OUTPUT[1].checked == true && (document.forms[0].GUARD_ISSUEDATE.value == '' || document.forms[0].GUARD_EXPIREDATE.value == '')) {
        alert('住所開始/終了日を入力して下さい。');
        return false;
    }

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (document.forms[0].OUTPUT[1].checked == true) {
        if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
            return true;
        }
    } else if (cmd != '') {
        cmd = 'output';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Show_ErrMsg(flg) {
    if (flg == 1) {
        alert('課程コードまたは学科コードが設定されていません');
    }
    if (flg == 3) {
        alert('テンプレートの書き出しに失敗しました');
        return;
    }
    closeWin();
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

function shorimei_show(obj) {
    if (obj.value == 1) {
        document.forms[0].COURSEMAJOR.disabled = false;
    } else {
        document.forms[0].COURSEMAJOR.disabled = true;
    }
}

function OutputFile(filename) {
    parent.top_frame.location.href = filename;
}

function max_semes_cl() {
    alert('{rval MSG311}');
    closeWin();
}
