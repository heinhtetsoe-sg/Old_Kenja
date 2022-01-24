//2013/01/10キーイベントタイムアウト処理をスルー
document.onLoad=keyThroughSet()

function btn_submit(cmd) {

    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);
    document.forms[0].windowHeight.value = bodyHeight;

    if (cmd == 'alldel' || cmd == 'chdel'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//画面リサイズ
function submit_reSize() {
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.getElementById("tbody").style.height = bodyHeight - 380;
}
function closing_window() {
    alert('{rval MSG300}'+'\n'+'担当講座が無い為、権限がありませ。');
    closeWin();
    return true;
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
function checkkey(){
    if (event.keyCode == 13){
        document.forms[0].cmd.value = 'add';
        document.forms[0].submit();
        return false;
    }
}
function keyfocs1(obj){
    if (event.keyCode == 13){
        if (document.forms[0].SCHREGNO) {
            document.forms[0].SCHREGNO.focus();
        }
    }
}
function keyfocs2(obj){
    if (event.keyCode == 13){
        document.forms[0].HYOUKA.focus();
    }
}
