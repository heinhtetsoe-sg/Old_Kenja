function btn_submit(cmd) {
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset() {
   result = confirm('{rval MSG106}');
   if (result == false) {
       return false;
   }
}

function selcheck(that) {

    //全角から半角
    that.value = toHankakuNum(that.value);
    //数値型へ変換
    that.value = toInteger(that.value);
    //セルが空の時０
    if(that.value == '' ){
        that.value = 0;
        return;
    }
}

//区分別テキストボックス内禁止処理
function check(that) {

checktest(that.value);

}

function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function closing_window(linkpath){

    alert('{rval MSG305}' + '\n(名称マスタ)');
    parent.location.href=linkpath;

    return false;
}

function CallView(cd)
{
    parent.mid_frame.location.href='knjp140kindex.php?cmd=list2&INST_CD='+cd;
    parent.bot_frame.location.href='knjp140kindex.php?cmd=edit1&INST_CD='+cd;
}

