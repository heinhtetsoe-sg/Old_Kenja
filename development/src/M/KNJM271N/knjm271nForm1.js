//2013/01/10キーイベントタイムアウト処理をスルー
document.onLoad=keyThroughSet()

function btn_submit(cmd)
{
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'add') {
        if (document.forms[0].DATE.value == '') {
            alert('評価送信日を指定して下さい。');
            return false;
        }
        if (document.forms[0].SUBCLASSCD.value == '') {
            alert('科目を指定して下さい。');
            return false;
        }
        if (document.forms[0].STAFF.value == '') {
            alert('添削者を指定して下さい。');
            return false;
        }
        if (document.forms[0].STANDARD_SEQ.value == '') {
            alert('回数を指定して下さい。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function datecheck(dval)
{
    var chflg = 0;

    //Nullチェック
    if (dval == '') {
        return '';
    }
    //日付正規チェック
    if (!isDate2(dval)) {
       return '';
    }
    //日付の一致チェック
    if (dval == document.forms[0].DEFOULTDATE.value){
        return '';
    }

    if (sem == 0){
        sem = 1;
        document.forms[0].cmd.value = 'dsub';
        document.forms[0].submit();
        return false;
    }
}
function check(obj){
    if (getByte(obj.value) > 40){
        alert("全角２０、半角６０文字以内で入力してください。");
        obj.focus();
    }
}
