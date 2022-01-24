function btn_submit(cmd)
{
    var str = new Object();
    var seq = '';
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    //終了
    if (cmd == 'end' || cmd == 'copy') {
        if (document.forms[0].change_flg.value == 'true') {
            if (!confirm('{rval MSG108}')) return true;
        }
        if (cmd == 'end') {
            closeWin();
        }
    }
    //更新時チェック処理
    if(cmd == 'update'){
        //エレメント分ループ
        for (i = 0; i < document.forms[0].elements.length; i++){
            //状態と校時CDの入力状態をチェック
            var el  = document.forms[0].elements[i];
            //エレメントがテキストでなければ、抜ける
            if (el.type == "text") {
                document.forms[0].update_data.value +=  seq + el.id + '-' + el.value;
                seq = ',';
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkCmb(cmd)
{
    var str = new Object();
    var seq = '';
    //変更データあり
    if (document.forms[0].change_flg.value == 'true') {
        alert('変更されたデータがあります。\nコンボボックスを変更する為には\n更新または、取消を実行して下さい。');
        return false;
    }
    return true;
}

function Setflg(obj, eve)
{
    valCheck(obj, eve);
    if (eve == 'change' && obj.id){
        document.forms[0].change_flg.value = true;
        obj.style.background="yellow";
        document.getElementById('ROWID' + obj.id).style.background="yellow";
    }
    return;
}

function valCheck(obj, eve)
{
    obj.value = toInteger(obj.value);
    if (obj.value < 0 || obj.value > 100) {
        alert("入力範囲は、0～100です。");
        obj.value = obj.defaultValue;
        document.forms[0].elements[0].focus();
        obj.focus();
    }
    return;
}
