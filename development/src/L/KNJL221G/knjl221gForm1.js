function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Page_jumper(link) {
    if (vflg && !confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}
//計算
function Keisan() {
    var setSum    = 0;
    var setTotal5 = 0;
    var setAvg;
    var setKey;
    var kyouka_count = document.forms[0].kyouka_count.value;
    var total5Flg    = document.forms[0].total5Flg.value.split(':');
    var total5FlgArr = new Array();

    for (var i=0; i < kyouka_count; i++) {
        setKey = total5Flg[i].split('-')[0];
        total5FlgArr[setKey] = total5Flg[i].split('-')[1];
    }

    for (var i=1; i <= kyouka_count; i++) {
        if (document.forms[0]["CONFIDENTIAL_RPT0" + i].value != '') {
            setSum = parseInt(setSum) + parseInt(document.forms[0]["CONFIDENTIAL_RPT0" + i].value);
            if (total5FlgArr['0' + i] == '1') {//namespare1が'1'のもの
                setTotal5 = parseInt(setTotal5) + parseInt(document.forms[0]["CONFIDENTIAL_RPT0" + i].value);
            }
        }
    }

    document.forms[0].TOTAL5.value    = setTotal5;
    document.forms[0].TOTAL_ALL.value = setSum;

    return
}
//エンターキーをTabに変換
function changeEnterToTab (obj) {
    var targetObject;
    var targetObjectform;

    if (window.event.keyCode == '13') {
        //移動可能なオブジェクト
        var textFieldArray = document.forms[0].setTextField.value.split(",");

        for (var i = 0; i < textFieldArray.length; i++) {
            if (textFieldArray[i] == obj.name) {
                //シフト＋Enter押下
                if (window.event.shiftKey) {
                    targetObjectform = textFieldArray[(i - 1)];
                } else {
                    targetObjectform = textFieldArray[(i + 1)];
                }
                targetObject = document.forms[0][targetObjectform];
                targetObject.focus();
                targetObject.select();
                return;
            }
        }
    }
    return;
}
//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
