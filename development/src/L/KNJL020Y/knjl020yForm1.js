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
        if ((document.all("AVERAGE_ALL[]") != null || document.all("AVERAGE5[]") != null || document.all("ABSENCE_DAYS[]") != null || document.all("ABSENCE_DAYS2[]") != null || document.all("ABSENCE_DAYS3[]") != null) && change_flg && !confirm('{rval MSG108}')) {
            return false;
        }
    }

//    if (cmd == "update" && (document.all("ABSENCE_DAYS[]") == null || document.all("ABSENCE_DAYS2[]") == null || document.all("ABSENCE_DAYS3[]") == null)) {
//        return false;
//    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function MoveFocus(idx) 
{
   if (window.event.keyCode == 11) 
   {
       idx++; 
       document.all("SCORE"+idx.toString()).focus();
   } 
}

function Setflg(obj)
{
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].APPLICANTDIV.disabled = true;

    obj.style.background="yellow";
    document.getElementById('ROWID' + obj.id).style.background="yellow";
}
