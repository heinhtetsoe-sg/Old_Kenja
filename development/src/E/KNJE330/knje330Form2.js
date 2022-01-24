<!--kanji=漢字-->
<!-- <?php # $RCSfile: knje330Form2.js,v $ ?> -->
<!-- <?php # $Revision: 56587 $ ?> -->
<!-- <?php # $Date: 2017-10-22 21:54:51 +0900 (日, 22 10 2017) $ ?> -->


function myBtnSubmit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


function myBtnReset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}


function myCheckTime(obj){
    switch(obj.name) {
    case "HOUR_S":
    case "HOUR_E":
        var h = toInteger(obj.value);
        var ih = parseInt(h);
        if (ih < 0 || 23 < ih) {
            alert("０から２３の数字を入力してください。");
            obj.focus();
        }else {
            obj.value = h;
        }
        break;

    case "MINUTE_S":
    case "MINUTE_E":
        var m = toInteger(obj.value);
        var im = parseInt(m);
        if (im < 0 || 59 < im) {
            alert("０から５９の数字を入力してください。");
            obj.focus();
        }else {
            obj.value = m;
        }
        break;
    }
}


function myDisableText(obj) {
    var check_value = obj.value;
    var sonota_value = document.forms[0].SONOTA.value;
    var flag = false;

    if (parseInt(check_value) == parseInt(sonota_value)) {
        // 「その他」が選択されたときだけ、入力可能（応募方法）
        flag = false;
    } else {
        flag = true;
    }

    document.forms[0].HOWTOEXAM_REMARK.disabled = flag;

    var temp = document.forms[0].HOWTOEXAM_REMARK.className;
    var cn = temp.replace(/\bunedit_ope\b/, '');
    if (flag) {
        document.forms[0].HOWTOEXAM_REMARK.className = cn + " unedit_ope";
    } else {
        document.forms[0].HOWTOEXAM_REMARK.className = cn;
    }
}
