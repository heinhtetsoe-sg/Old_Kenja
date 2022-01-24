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
        if (document.all("ABSENCE_DAYS[]") != null && change_flg && !confirm('{rval MSG108}')) {
            return false;
        }
    }

    if (cmd == "update" && document.all("ABSENCE_DAYS[]") == null) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function MoveFocus(idx) 
{
   if (window.event.keyCode == 13) 
   {
       idx++; 
       document.all("SCORE"+idx.toString()).focus();
//       document.all("SCORE"+idx.toString()).select();
   } 
}

// function CheckScore(obj)
// {
//    obj.value = toInteger(obj.value);
//    if (obj.value > eval(aPerfect[obj.id])) {
//        alert('{rval MSG901}' + '満点：'+aPerfect[obj.id]+'以下で入力してください。');
//        obj.focus();
//        obj.select();
//        return;
//    }
// }
function Setflg(obj)
{
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].APPLICANTDIV.disabled = true;

    obj.style.background="yellow";
    document.getElementById('ROWID' + obj.id).style.background="yellow";
}
