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
        if (document.all("SCORE[]") != undefined && change_flg && !confirm('{rval MSG108}')) {
            return false;
        }
    }

    if(cmd == 'read') {
        if(document.forms[0].TESTDIV.value == ''){
            alert('{rval MSG301}' + '\n ( 入試区分 )');
            return false;
        }
    }

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

function setName(obj, rowid)
{
    outputLAYER('NAMEID' + rowid, '');
    outputLAYER('NAME_KANAID' + rowid, '');
    outputLAYER('SEXID' + rowid, '');

    if (isNaN(obj.value) || eval(obj.value) == undefined) {
       return;
    }
       
    if (obj.value == '')
        return;

    var num = obj.value;
    var setval = '0000' + num;
    obj.value = setval.substr((num.length - 1), 5);
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

//エンターキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_RECEPTNO.value.split(',');
        var index = setArr.indexOf(obj.id);
        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
        } else {
            if (index < (setArr.length - 1)) {
                index++;
            }
        }
        var targetId = setArr[index];
        document.getElementById(targetId).focus();
        return false;
    }
}

function CheckScore(obj)
{
    obj.value = toInteger(obj.value);    
    if (obj.value > eval(aPerfect[obj.id])) {
        alert('{rval MSG901}' + '満点：'+aPerfect[obj.id]+'以下で入力してください。');
        obj.focus();
//        obj.select();
        return;
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].HID_EXAM_TYPE.value = document.forms[0].EXAM_TYPE.options[document.forms[0].EXAM_TYPE.selectedIndex].value;
    document.forms[0].EXAM_TYPE.disabled = true;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;
    document.forms[0].TESTSUBCLASSCD.disabled = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}