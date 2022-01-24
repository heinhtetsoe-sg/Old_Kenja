function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if(cmd == 'read' || cmd == 'next' || cmd == 'back' || cmd == 'update' || cmd == 'reset') {
        if (document.forms[0].EXAMNO.value == '' || eval(document.forms[0].EXAMNO.value) == 0) {
            alert('{rval MSG901}' + '\n 受験番号には 1 以上を入力してください');
            return false;
        }
    }


    if (cmd == 'read' || cmd == 'next' || cmd == 'back') {
        if (document.all("RECEPTNO[]") != null && change_flg && !confirm('{rval MSG108}')) {
            return false;
        }
    }

    if (cmd == "update" && document.all("RECEPTNO[]") == null) {
        return false;
    }

    if (cmd == 'update' && !confirm('{rval MSG102}' + '\n\n注意！！\n\n座席番号が未入力の場合は削除され登録されません。\n\n削除される座席番号の得点データも削除されます。')) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Setflg(obj)
{
    change_flg = true;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].HID_EXAM_TYPE.value = document.forms[0].EXAM_TYPE.options[document.forms[0].EXAM_TYPE.selectedIndex].value;
    document.forms[0].EXAM_TYPE.disabled = true;

    obj.style.background="yellow";
    document.getElementById('ROWID' + obj.id).style.background="yellow";

    if (isNaN(obj.value) || eval(obj.value) == undefined || obj.value == '') {
       return;
    }

    var num = obj.value;
    var setval = '000' + num;
    obj.value = setval.substr((num.length - 1), 4);
}
