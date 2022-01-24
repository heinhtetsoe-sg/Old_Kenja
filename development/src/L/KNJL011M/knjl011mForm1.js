function btn_submit(cmd) 
{
    if (cmd == 'delete'){
        if (document.forms[0].DELFLG.value == '') {
            alert('{rval MSG300}\n権限がありません。');
            return true;
        }
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        if (vflg == true) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }
    if (cmd == 'disp_clear') {
        outputLAYER('DATEID', blank_name);
        outputLAYER('FS_NAME_ID', blank_name);
        for (i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'select-one' || document.forms[0].elements[i].type == 'text') {
                if (document.forms[0].elements[i].type == 'select-one') {
                    document.forms[0].elements[i].value = document.forms[0].elements[i].options[0].value;
                } else {
                    document.forms[0].elements[i].value = "";
                }
            }
        }
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//日付チェック
function toDatecheck(index, obj, calno, spare, spare2, spare3)
{
    var txtvalue = obj.value;
    switch(index) {
        case 0:
            if (txtvalue != "") {
                var num = calno.split(",");
                for (i = 0; i < num.length; i++) {
                    if (txtvalue == num[i]) {
                        return false;
                    }
                }
                alert('{rval MSG901}\n正しい値を入力して下さい。');
                obj.value = "";
                return false;
            }
            break;
        case 1:
            if (txtvalue != "" && txtvalue < 1 || txtvalue > 99) {
                alert('{rval MSG901}\n年には1～99を入力して下さい。');
                obj.value = "";
                return false;
            }
            break;
        case 2:
            if (txtvalue != "" && txtvalue < 1 || txtvalue > 12) {
                alert('{rval MSG901}\n月には1～12を入力して下さい。');
                obj.value = "";
                return false;
            }
            break;
        case 3:
            if (txtvalue != "" && txtvalue < 1 || txtvalue > 31) {
                alert('{rval MSG901}\n日には1～31を入力して下さい。');
                obj.value = "";
                return false;
            }
            break;
    }
    return;
}

//和暦名を置き換える
function setName(obj)
{
    var idx = obj.value;
    if (obj.value == '') {
        if (obj.name == 'ERACD') {
            outputLAYER('DATEID', blank_name);
            document.forms[0].WNAME.value = "";
        }
        return;
    }
    if (year_name[idx] != null) {
        if (obj.name == 'ERACD') {
            outputLAYER('DATEID', year_name[idx]);
            document.forms[0].WNAME.value = year_name[idx];
        }
        return;
    }
    return;
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
