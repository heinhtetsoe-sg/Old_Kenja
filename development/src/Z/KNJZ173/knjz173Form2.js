function btn_submit(cmd) {

    if (cmd == 'delete' || cmd == 'update' || cmd == 'add') {
        paddNumCode();
        if (document.forms[0].KAIKIN_CD.value == '') {
            alert('{rval MSG310}' + "皆勤コードが未入力です。");
            return true;
        }
        if (cmd == 'update' || cmd == 'add') {
            if (document.forms[0].KAIKIN_NAME.value == '') {
                alert('{rval MSG310}' + "皆勤名称が未入力です。");
                return true;
            }
            if (document.forms[0].REF_YEAR.value == '') {
                alert('{rval MSG310}' + "参照年数が未入力です。");
                return true;
            }

            if (document.forms[0].KNJZ173_DISPPATTERN.value == '1') {
                if (document.forms[0].REF_YEAR.value == '1' && document.forms[0].KESSEKI_CONDITION.value == '') {
                    alert('{rval MSG310}' + "欠席・遅刻・早退・結果の合計が未入力です。");
                    return true;
                }
            } else {
                if (document.forms[0].KESSEKI_CONDITION.value == '') {
                    alert('{rval MSG310}' + "欠席が未入力です。");
                    return true;
                }
                if (document.forms[0].LE_EXCHGTYPE.value == '2') {
                    if (document.forms[0].TIKOKU_CONDITION.value == '' || document.forms[0].SOUTAI_CONDITION.value == '') {
                        alert('{rval MSG310}' + '遅刻・早退が未入力です。');
                        return true;
                    }
                } else {
                    if (document.forms[0].KESSEKI_KANSAN.value == '') {
                        alert('{rval MSG310}' + '遅刻換算が未入力です。');
                        return true;
                    }
                    if (document.forms[0].KESSEKI_KANSAN.value == 0) {
                        alert('{rval MSG901}' + '遅刻換算は1以上の値を入力して下さい。');
                        return true;
                    }
                }
            }
            var chkcntr = getChkCntr();
            if (document.forms[0].SET_PREFATTEND_GRADE1.checked) {
                if (chkcntr == 0) {
                    alert('{rval MSG310}' + '取得対象学年が未チェックです。');
                    return true;
                }
                /*
                if (chkcntr > 3) {
                    alert('{rval MSG302}' + '選択可能な取得対象学年は3つ以下です。');
                    return true;
                }
                */
            }
        }
    }

    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    }

    if (cmd == 'update' && !confirm('{rval MSG102}')) {
        return true;
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function paddNumCode() {
    if (charCount(document.forms[0].KAIKIN_CD.value) == 1) {
        document.forms[0].KAIKIN_CD.value = '0' + document.forms[0].KAIKIN_CD.value;
    }
}

function charCount(str) {
    var len = 0;
    str = escape(str);
    for (i=0;i<str.length;i++,len++) {
        if (str.charAt(i) == "%") {
            if (str.charAt(++i) == "u") {
                i += 3;
                len++;
            }
            i++;
        }
    }
    return len;
}

function getChkCntr() {
    var chkcntr = 0;
    var strTmp = "G_KIND_";
    console.log("実行");
    for (var i = 1; i <= document.forms[0].targetGradeCntA.value; i++) {
        if (document.getElementById(strTmp+"A_"+ i) && document.getElementById(strTmp+"A_"+ i).checked) {
            chkcntr++;
        }
    }
    for (var i = 1; i <= document.forms[0].targetGradeCntH.value; i++) {
        if (document.getElementById(strTmp+"H_"+ i) && document.getElementById(strTmp+"H_"+ i).checked) {
            chkcntr++;
        }
    }
    for (var i = 1; i <= document.forms[0].targetGradeCntJ.value; i++) {
        if (document.getElementById(strTmp+"J_"+ i) && document.getElementById(strTmp+"J_"+ i).checked) {
            chkcntr++;
        }
    }
    for (var i = 1; i <= document.forms[0].targetGradeCntP.value; i++) {
        if (document.getElementById(strTmp+"P_"+ i) && document.getElementById(strTmp+"P_"+ i).checked) {
            chkcntr++;
        }
    }
    for (var i = 1; i <= document.forms[0].targetGradeCntK.value; i++) {
        if (document.getElementById(strTmp+"K_"+ i) && document.getElementById(strTmp+"K_"+ i).checked) {
            chkcntr++;
        }
    }
console.log(chkcntr);
    return chkcntr;
}

function chkSuji(obj) {
    if (obj.value !=  null && obj.value != "" && !isNumOnly(obj)) {
        alert('{rval MSG907}' + "数字のみ入力可能です。");
        obj.focus();
        return false;
    }
    return true;
}
function isNumOnly(obj) {
    var regex = new RegExp(/^[0-9]+$/);
    return regex.test(obj.value);
}
