function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if(cmd == 'read' || cmd == 'next' || cmd == 'back' || cmd == 'update' || cmd == 'reset') {
        if (document.forms[0].RECEPTNO.value == '' || eval(document.forms[0].RECEPTNO.value) == 0) {
            alert('{rval MSG901}' + '\n 座席番号には 1 以上を入力してください');
            return false;
        }
    }

    if (cmd == 'read' || cmd == 'next' || cmd == 'back') {
        if (document.all("EXAMNO[]") != null && change_flg && !confirm('{rval MSG108}')) {
            return false;
        }
    }

    if(cmd == 'read') {
        if(document.forms[0].TESTDIV.value == ''){
            alert('{rval MSG301}' + '\n ( 入試区分 )');
            return false;
        }
    }

    if (cmd == "update" && document.all("EXAMNO[]") == null) {
        return false;
    }
    if (cmd != "update"){
        document.forms[0].target = "_self";
    } else {
        document.forms[0].target = 'top_frame';
    }
    if (cmd == 'update' && !confirm('{rval MSG102}' + '\n\n注意！！\n\n受験番号が未入力の場合は削除され登録されません。\n\n削除される受験番号の得点データも削除されます。')) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm()
{
    if(confirm('{rval MSG106}')) return true;
    return false;
}

function setName(obj, rowid, flg)
{
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled = true;
    
    outputLAYER('NAMEID' + rowid, '');
    outputLAYER('NAME_KANAID' + rowid, '');
    outputLAYER('SEXID' + rowid, '');

    if (flg == 1){
        document.getElementById('ROWID' + rowid).style.background="yellow";
        obj.style.background="yellow";
    }
    if (isNaN(obj.value) || eval(obj.value) == undefined) {
       return;
    }
       
    if (obj.value == '')
        return;

    var num = obj.value;
    var setval = '000' + num;
    obj.value = setval.substr((num.length - 1), 4);
}
