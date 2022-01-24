function btn_submit(cmd)
{
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    } else if (cmd == 'update') {
        //更新中の画面ロック
        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            updateFrameLock();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkScore(obj, perfect) {
    obj.value = toInteger(obj.value);
    if (obj.value && Number(obj.value) > Number(perfect)) {
        obj.value = '';
        alert('得点は、' + perfect + 'までです。');
        obj.focus();
        return false;
    }
}

function closeMethod() {
    closeWin();
}

function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

