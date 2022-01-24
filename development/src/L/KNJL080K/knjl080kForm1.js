function btn_submit(cmd)
{
    if(cmd == 'read') {
        if (document.forms[0].TESTSUBCLASSCD.value == '' || eval(document.forms[0].TESTSUBCLASSCD.value) == 0) {
            alert('{rval MSG901}' + '\n 受験科目がありません。');
            return false;
        }
    }
    if(cmd == 'read' || cmd == 'next' || cmd == 'back' || cmd == 'update' || cmd == 'reset') {
        if (document.forms[0].EXAMNO.value == '' || eval(document.forms[0].EXAMNO.value) == 0) {
            alert('{rval MSG901}' + '\n 受験番号には 1 以上を入力してください');
            return false;
        }
    }

    if(cmd == 'next' || cmd == 'back' || cmd == 'update' || cmd == 'reset') {
        if (document.forms[0].HID_EXAMNO == undefined) {
            alert('{rval MSG303}');
            return false;
        }
    }

    if (cmd == 'read' || cmd == 'next2' || cmd == 'back2') {
        if (change_flg && !confirm('{rval MSG108}')) {
            return false;
        }
    }

    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    if (cmd == 'next' || cmd == 'back' || cmd == 'update') {
        var eno = document.forms[0].HID_EXAMNO.value;
        var a_eno = eno.split(',');
        for (var i = 0; i < a_eno.length; i++) {
           if (document.all('B_SCORE' + a_eno[i]).type == 'text')
                continue;
           if (!isNaN(document.all('B_'+a_eno[i]).innerHTML)) {
                document.all('B_SCORE'+a_eno[i]).value = document.all('B_'+a_eno[i]).innerHTML;
           }
        }
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

function CheckScore(obj)
{
    obj.value = toInteger(obj.value);
    var val = aPerfect[obj.id].split(',');

    if (obj.name == 'A_SCORE[]') {

        // 配点A入力チェック
        if (obj.value > eval(val[0]) && flg) {
            alert('{rval MSG901}' + '\n\nコース別満点設定で設定した満点：'+val[0]+'以下で入力して下さい。');
            obj.focus();
            obj.select();
            flg = false;
            return;
        }else{
            flg = true;
        }

       // 配点B自動設定
       if (val[1] != '' && obj.value != '' && document.all('B_SCORE' + obj.id).type == 'hidden') {
            outputLAYER('B_'+obj.id, Math.round(eval(obj.value) * val[1]));
       }
       // if (obj.value == '') {    2006/02/09
       if (val[1] != '' && obj.value == '' && document.all('B_SCORE' + obj.id).type == 'hidden') {
            outputLAYER('B_'+obj.id, '');
       }

       // 配点B手動設定
       if (val[1] != '' && obj.value == '' && document.all('B_SCORE' + obj.id).type == 'text') {
            document.getElementById('B_SCORE' + obj.id).value = "";
       }

    } else {

       // 配点B入力チェック
       if (obj.value > eval(val[2]) && flg) {
           alert('{rval MSG901}' + '\n\nコース別満点設定で設定した満点：'+val[2]+'以下で入力して下さい。');
           obj.focus();
           obj.select();
           flg = false;
           return;
       }else{
           flg = true;
       }

       // 配点B手動設定
       if (val[1] != '' && obj.value == '' && document.all('B_SCORE' + obj.id).type == 'text') {
            document.getElementById(obj.id).value = "";
       }
    }


// 2005/09/06
//    if (obj.value > eval(val[0]) && flg) {
//        alert('{rval MSG901}' + '\n\nコース別満点設定で設定した満点：'+val[0]+'以下で入力して下さい。');
//        obj.focus();
//        obj.select();
//        flg = false;
//        return;
//    }else{
//        flg = true;
//    }
// 
//   if (document.all('B_SCORE' + obj.id).type == 'text')
//        return;
// 
//   if (val[1] != '' && obj.value != '') {
//        outputLAYER('B_'+obj.id, Math.round(eval(obj.value) * val[1]));
//   }
//   if (obj.value == '') {
//        outputLAYER('B_'+obj.id, '');
//   }
}
function Setflg(){
    change_flg = true;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].HID_TESTSUBCLASSCD.value = document.forms[0].TESTSUBCLASSCD.options[document.forms[0].TESTSUBCLASSCD.selectedIndex].value;
    document.forms[0].TESTSUBCLASSCD.disabled = true;
}
