function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_DESIREDIV.value = document.forms[0].DESIREDIV.options[document.forms[0].DESIREDIV.selectedIndex].value;
    document.forms[0].HID_RECOM_KIND.value = document.forms[0].RECOM_KIND.options[document.forms[0].RECOM_KIND.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].DESIREDIV.disabled = true;
    document.forms[0].RECOM_KIND.disabled = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}

function setName(obj, rowid, flg, slide_flg, shift_desire_flg)
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
            if (obj.value == '3' && slide_flg == '') {
                alert('{rval MSG901}'+'スライド希望者のみ入力可能です。');
                outputLAYER('JUDGEDIV_NAME' + rowid, '');
                obj.value = '';
            } else if (obj.value == '5' && shift_desire_flg == '') {
                alert('{rval MSG901}'+'特別判定希望者のみ入力可能です。');
                outputLAYER('JUDGEDIV_NAME' + rowid, '');
                obj.value = '';
            } else {
                outputLAYER('JUDGEDIV_NAME' + rowid, judgediv_name[idx]);
            }
        } else {
            alert('{rval MSG901}');
            outputLAYER('JUDGEDIV_NAME' + rowid, '');
            obj.value = '';
        }
    }
}
