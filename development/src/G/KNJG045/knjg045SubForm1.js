function btn_submit(cmd) {
    var staffdiv = document.forms[0].setStaffDiv.value;
    var cnt = document.forms[0].setcounter.value;
    var checkcnt = cnt - 1;
    var dataCount = 0;

    if (staffdiv == '5' || staffdiv == '6') {
        for (var i = 0; i <= checkcnt; i++) {
            var CHECK = "CHECK_" + i;
            var COUNT_CHECK1 = "COUNT_CHECK1_" + i;
            var COUNT_CHECK2 = "COUNT_CHECK2_" + i;
            var COUNT_CHECK3 = "COUNT_CHECK3_" + i;
            if (staffdiv == '6') {
                var COUNT_CHECK4 = "COUNT_CHECK4_" + i;
                var COUNT_CHECK5 = "COUNT_CHECK5_" + i;
            }
            var STAFFCD = "STAFFCD_" + i;
            
            if (staffdiv == '6') {
                if (document.forms[0][CHECK].checked) {
                    if (document.forms[0][COUNT_CHECK1].checked == false && document.forms[0][COUNT_CHECK2].checked == false && document.forms[0][COUNT_CHECK3].checked == false && document.forms[0][COUNT_CHECK4].checked == false && document.forms[0][COUNT_CHECK5].checked == false) {
                        alert('{rval MSG203}' + '回数にチェックが入っていません。' + '\n( ' + document.forms[0][STAFFCD].value + ' )');
                        return false;
                    }
                    dataCount++;
                }
            } else {
                if (document.forms[0][CHECK].checked) {
                    if (document.forms[0][COUNT_CHECK1].checked == false && document.forms[0][COUNT_CHECK2].checked == false && document.forms[0][COUNT_CHECK3].checked == false) {
                        alert('{rval MSG203}' + '回数にチェックが入っていません。' + '\n( ' + document.forms[0][STAFFCD].value + ' )');
                        return false;
                    }
                    dataCount++;
                }
            }
        }
        if (staffdiv == '5') {
            if (dataCount > 4) {
                alert("補欠授業は4名まで登録可能です。");
                return false;
            }
        }
    //staffdiv=1 ～ 4まで
    } else {
        for (var i = 0; i <= checkcnt; i++) {
            var CHECK = "CHECK_" + i;
            if (document.forms[0][CHECK].checked) {
                dataCount++;
            }
        }
        if (staffdiv == '1') {
            if (dataCount > 20) {
                alert("欠席者は20名まで登録可能です。");
                return false;
            }
        } else if (staffdiv == '2') {
            if (dataCount > 20) {
                alert("遅参者は20名まで登録可能です。");
                return false;
            }
        } else if (staffdiv == '3') {
            if (dataCount > 20) {
                alert("早退者は20名まで登録可能です。");
                return false;
            }
        } else if (staffdiv == '4') {
            if (dataCount > 20) {
                alert("出張者は20名まで登録可能です。");
                return false;
            }
        }
    }
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//結果反映
function refStaffName(stf) {
    for (var i=0; i < parent.document.forms[0].elements.length; i++) {
        if (parent.document.forms[0].elements[i].name == stf) {
            parent.document.forms[0].elements[i].value = document.forms[0].setStaffName.value;
        }
    }
}

//回数チェックボックス自動付加
function OptionUse(obj) {
    var inputs = document.getElementsByTagName("input");
    var cnt = obj.name.split('_');
    var staffdiv = document.forms[0].setStaffDiv.value;

    //回数チェックボックス有無チェック
    if (staffdiv == '6') {
        reg1 = new RegExp("^COUNT_CHECK[1-5]_" + cnt[1] + "$");
    } else {
        reg1 = new RegExp("^COUNT_CHECK[1-3]_" + cnt[1] + "$");
    }
    flg = false;
    for (i = 0; i < inputs.length; i++) {
        if (inputs[i].name.match(reg1)) {
            if (inputs[i].checked == true) {
                flg = true;
            }
        }
    }

    if (flg == false) {
        //回数チェックボックスを自動で付ける
        reg2 = new RegExp("^COUNT_CHECK1_" + cnt[1] + "$");
        for (i = 0; i < inputs.length; i++) {
            if (inputs[i].name.match(reg2)) {
                inputs[i].checked = obj.checked;
            }
        }
    }
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}
