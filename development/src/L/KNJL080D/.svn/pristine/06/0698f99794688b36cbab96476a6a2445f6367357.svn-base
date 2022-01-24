function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].TESTDIV.disabled) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//事前チェックエラー
function errorPreCheck(field) {
    alert('{rval MSG305}'+'\n'+field+'が設定されていません。');
}

//値チェック
function CheckValue(obj, judgeKind) {
    var tmp         = obj.name.split('_');
    var valList     = document.forms[0][tmp[0]+"_LIST"].value;
    var valArray    = valList.split(',');
    var labelArray  = document.forms[0][tmp[0]+"_LABEL_LIST"].value.split(',');

    if (tmp[0] == 'PROCEDUREDIV') {
        //TESTDIV=2 かつ 特待区分が1or2 のときのみ4を入力可
        if (!(document.forms[0].TESTDIV.value == '2' && (judgeKind == '1' || judgeKind == '2'))) {
            valArray.some(function(v, i) {
                if (v == '4') {
                    valArray.splice(i,1);
                    labelArray.splice(i,1);
                }
            });
        }
    }

    //値チェック
    var checkList   = valArray.join('|');
    var msg         = valArray.join(',');
    re = new RegExp(checkList);
    if (obj.value && !obj.value.match(re)) {
        alert('{rval MSG901}' + '\n' + msg + 'を入力してください。');
        obj.focus();
        obj.select();
        return;
    }

    //名称表示
    if (obj.value == '') {
        document.getElementById(tmp[0]+'_NAME'+tmp[1]).innerHTML = '';
    } else {
        for (var i = 0; i < valArray.length; i++) {
            if (obj.value == valArray[i]) {
                document.getElementById(tmp[0]+'_NAME'+tmp[1]).innerHTML = labelArray[i];
                return;
            }
        }
    }
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_SUB_ORDER.value       = document.forms[0].SUB_ORDER.options[document.forms[0].SUB_ORDER.selectedIndex].value;
    document.forms[0].HID_DESIREDIV.value       = document.forms[0].DESIREDIV.options[document.forms[0].DESIREDIV.selectedIndex].value;
    document.forms[0].HID_JUDGEMENT.value       = document.forms[0].JUDGEMENT.options[document.forms[0].JUDGEMENT.selectedIndex].value;
    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].SUB_ORDER.disabled        = true;
    document.forms[0].DESIREDIV.disabled        = true;
    document.forms[0].JUDGEMENT.disabled        = true;

    var tmp = obj.id.split('_');
    document.getElementById('ROWID' + tmp[1]).style.background="yellow";
    obj.style.background="yellow";
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_EXAMNO.value.split(',');
        var tmp = obj.id.split('_');
        var tmpArr = new Array();
        for (var i = 0; i < setArr.length; i++) {
            tmpArr[i] = tmp[0]+'_'+setArr[i];
        }
        var index = tmpArr.indexOf(obj.id);

        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i > 0; i--) {
                    targetId = tmpArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (tmpArr.length - 1)) {
                index++;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i < (tmpArr.length - 1); i++) {
                    targetId = tmpArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        }

        document.getElementById(targetId).focus();
        document.getElementById(targetId).select();
        return false;
    }
}
