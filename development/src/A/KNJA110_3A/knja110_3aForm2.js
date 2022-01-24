function btn_submit(cmd) {
   
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}'))
            return false;
    }        
        
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
function checktest(num) {

    switch (num) {
        case "1":
            document.forms[0].TRANSFER_EDATE.disabled = false;
            document.forms[0].TRANSFERPLACE.disabled = false;
            document.forms[0].TRANSFERADDR.disabled = false;
            document.forms[0].ABROAD_CREDITS.disabled = false;
            document.all('TRANSFER_EDATE').style.backgroundColor = "white";
            document.all('TRANSFERPLACE').style.backgroundColor = "white";
            document.all('TRANSFERADDR').style.backgroundColor = "white";
            document.all('ABROAD_CREDITS').style.backgroundColor = "white";
            break;
        case "2":
        case "3":
            document.forms[0].TRANSFER_EDATE.disabled = false;
            document.forms[0].TRANSFERPLACE.disabled = true;
            document.forms[0].TRANSFERADDR.disabled = true;
            document.forms[0].ABROAD_CREDITS.disabled = true;
            document.all('TRANSFER_EDATE').style.backgroundColor = "white";
            document.all('TRANSFERPLACE').style.backgroundColor = "darkgray";
            document.all('TRANSFERADDR').style.backgroundColor = "darkgray";
            document.all('ABROAD_CREDITS').style.backgroundColor = "darkgray";
            break;
        case "4":
            document.forms[0].TRANSFER_EDATE.disabled = true;
            document.forms[0].TRANSFERPLACE.disabled = true;
            document.forms[0].TRANSFERADDR.disabled = true;
            document.forms[0].ABROAD_CREDITS.disabled = true;
            document.all('TRANSFER_EDATE').style.backgroundColor = "darkgray";
            document.all('TRANSFERPLACE').style.backgroundColor = "darkgray";
            document.all('TRANSFERADDR').style.backgroundColor = "darkgray";
            document.all('ABROAD_CREDITS').style.backgroundColor = "darkgray";
            break;
    }
}
function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].classyear.length==0 && document.forms[0].classmaster.length==0) {
        alert('指定範囲が正しく有りません。');
        return false;
    }
    for (var i = 0; i < document.forms[0].classyear.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].classyear.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'replace_update';
    if (document.forms[0].TRANSFER_SDATE.value == "") {
        alert('{rval MSG901}\n(異動期間開始日が未入力)');
        return true;
    }
    document.forms[0].submit();
    return false;
}
function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}