function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if (cmd == "update" && document.all("SCORE[]") == undefined)  {
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

function MoveFocus(idx) 
{
   if (window.event.keyCode == 13) 
   {
       idx++; 
       document.all("SCORE"+idx.toString()).focus();
   } 
}

function CheckScore(obj)
{
    obj.value = toInteger(obj.value);    
    if (obj.value > eval(aPerfect[obj.id])) {
        alert('{rval MSG901}' + '満点：'+aPerfect[obj.id]+'以下で入力してください。');
        obj.focus();
        return;
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;
    document.forms[0].TESTSUBCLASSCD.disabled = true;
    document.forms[0].HID_EXAMHALLCD.value = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;
    document.forms[0].EXAMHALLCD.disabled = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}