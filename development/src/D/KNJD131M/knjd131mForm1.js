function btn_submit(cmd) {
    if (cmd == 'update') {
        if (document.forms[0].SCHREGNO.value == '') {
            alert('{rval MSG304}');
            return true;
        }
        if (document.forms[0].SEMESTER.value == '') {
            alert('{rval MSG304}\n( 学期 )');
            return true;
        }

        if (document.forms[0].SEMESTER.value == '3') {
            for (var i = 1; i <= 6; i++) {
                if (document.forms[0]['SCHOOLEVENT_NAME' + i].value == '' && document.forms[0]['SCHOOLEVENT_ATTEND' + i].value != '') {
                    alert('{rval MSG301}\n( 学校行事の記録 ' + i + ': 「参加/不参加」が選択されている場合、名称は必須です )');
                    document.forms[0]['SCHOOLEVENT_NAME' + i].focus();
                    return true;
                }
            }
        }
    }

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == '1') {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btnEnd() {
    if (!confirm('保存されていないデータがあれば破棄されます。処理を続行しますか？')) {
        return;
    }
    closeWin();
}
