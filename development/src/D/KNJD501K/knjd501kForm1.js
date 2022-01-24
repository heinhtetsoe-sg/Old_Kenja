/*
 * kanji=漢字
 * <?php

require_once('for_php7.php');
 # $Id: knjd501kForm1.js 56580 2017-10-22 12:35:29Z maeshiro $ ?>
 */

function closing_window(no)
{
    var msg;

    if(no == 'year'){
      msg = '{rval MSG305}';
    }else{
      msg = '{rval MSG300}';
    }

    alert(msg);
    closeWin();
    return true;
}

function btn_submit(cmd)
{
    if (cmd == 'update') {
        var flag;
        flag = "";
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "checkbox" && e.checked && e.name == "CHK_BOX[]"){
                var val = e.value;
                if (val != ''){
                    flag = "on";
                }
            }
        }
        if (flag == ''){
            alert("チェックボックスが選択されておりません。");
            return;
        }
        if (confirm('{rval MSG102}')) {
        } else {
            return;
        }
    }
    if(cmd == 'cancel' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
