function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if(cmd == 'read' || cmd == 'next' || cmd == 'back' || cmd == 'update' || cmd == 'reset') {
        if (document.forms[0].EXAMNO.value == '' || eval(document.forms[0].EXAMNO.value) == 0) {
            if (document.forms[0].SORT[0].checked) {
                alert('{rval MSG901}' + '\n 受験番号には 1 以上を入力してください');
                return false;
            }
        }
    }


    if (cmd == 'read' || cmd == 'next' || cmd == 'back') {
        if (document.all("JUDGEDIV[]") != null && change_flg && !confirm('{rval MSG108}')) {
            return false;
        }
    }

    if (cmd == "update" && document.all("JUDGEDIV[]") == null) {
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
   } 
}

function Setflg(obj)
{
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled = true;

    obj.style.background="yellow";
    document.getElementById('ROWID' + obj.id).style.background="yellow";
}
function setName(obj, rowid, flg)
{
    var idx = obj.value;
    if (obj.value == '') {
        if (flg == '0') {
            outputLAYER('JUDGEDIV_NAME' + rowid, '');
        }
        return;
    }
    if (flg == '0') {
        if (typeof judgediv_name[idx] != "undefined") {
            outputLAYER('JUDGEDIV_NAME' + rowid, judgediv_name[idx]);
        } else {
            alert('{rval MSG901}');
            outputLAYER('JUDGEDIV_NAME' + rowid, '');
            obj.value = '';
        }
    }
}
