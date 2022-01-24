function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if (cmd == 'read') {
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

    //実行
    if (cmd == "exec") {
        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "csvInput";
        } else {
            cmd = "csvOutput";
        }
    }

    //CSV取込
    if (cmd == "csvInput") {
        if (!confirm('処理を開始します。よろしいでしょうか？')) {
            return false;
        }
        if (document.forms[0].APPLICANTDIV.value == '') {
            alert('{rval MSG304}\n( 入試制度 )');
            return;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG304}\n( 入試区分 )');
            return;
        }
        if (document.forms[0].EXAM_TYPE.value == '') {
            alert('{rval MSG304}\n( 受験型 )');
            return;
        }
        if (document.forms[0].TESTSUBCLASSCD.value == '') {
            alert('{rval MSG304}\n( 受験科目 )');
            return;
        }
        if (document.forms[0].EXAMHALLCD.value == '') {
            alert('{rval MSG304}\n( 試験会場 )');
            return;
        }

        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled  = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
    document.forms[0].HID_EXAMHALLCD.value = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;
    document.forms[0].EXAMHALLCD.disabled = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}