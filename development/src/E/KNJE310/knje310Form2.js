<!--kanji=漢字-->
<!-- <?php # $RCSfile: knje310Form2.js,v $ ?> -->
<!-- <?php # $Revision: 56587 $ ?> -->
<!-- <?php # $Date: 2017-10-22 21:54:51 +0900 (日, 22 10 2017) $ ?> -->


function myBtnSubmit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    }
    if (cmd == 'add' || cmd == 'update' || cmd == 'delete') {
        myBtnSum(); // 総合点の計算
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


function myDisableText(obj) {
    var check_value = obj.value;
    var flag = false;

    if (4 < parseInt(check_value)) {
        flag = true;
        document.forms[0].SENKOU_NO.disabled = false;
    } else {
        flag = false;
        document.forms[0].SENKOU_NO.disabled = true;
    }

    document.forms[0].BUNAME.disabled       = flag;
    document.forms[0].JUKEN_HOWTO.disabled  = flag;
    document.forms[0].RECOMMEND.disabled    = flag;
    document.forms[0].TEST.disabled         = flag;
    document.forms[0].btn_sum.disabled      = flag;

    var temp  = document.forms[0].BUNAME.className;
    var temp2 = document.forms[0].RECOMMEND.className;
    var temp3 = document.forms[0].TEST.className;
    var temp4 = document.forms[0].SENKOU_NO.className;
    var cn  = temp.replace(/\bunedit_ope\b/, '');
    var cn2 = temp2.replace(/\bunedit_ope\b/, '');
    var cn3 = temp3.replace(/\bunedit_ope\b/, '');
    var cn4 = temp4.replace(/\bunedit_ope\b/, '');
    if (flag) {
        document.forms[0].BUNAME.className      = cn + " unedit_ope";
        document.forms[0].RECOMMEND.className   = cn2 + " unedit_ope";
        document.forms[0].TEST.className        = cn3 + " unedit_ope";
        document.forms[0].SENKOU_NO.className   = cn4;
    } else {
        document.forms[0].BUNAME.className      = cn;
        document.forms[0].RECOMMEND.className   = cn2;
        document.forms[0].TEST.className        = cn3;
        document.forms[0].SENKOU_NO.className   = cn4 + " unedit_ope";
    }
}


function myBtnWopen(obj) {
    var check_value = document.forms[0].SCHOOL_SORT.value;

    if (4 < parseInt(check_value)) {
        // 会社マスタ検索
        wopen('../../X/KNJXSEARCH9/index.php?PATH=/E/KNJE310/knje310index.php&cmd=&target=KNJE310','search',0,0,790,470);
    } else {
        // 学校マスタ検索
        wopen('../../X/KNJXSEARCH8/index.php?PATH=/E/KNJE310/knje310index.php&cmd=&target=KNJE310','search',0,0,790,470);
    }
}


function myBtnSum() {
    var check_value = document.forms[0].SCHOOL_SORT.value;
    var test    = document.forms[0].TEST;
    var attend  = document.forms[0].ATTEND;
    var avg_sum = document.forms[0].AVG_SUM;

    if (parseInt(check_value) < 5 && 50 < test.value) {
        alert('{rval MSG901}'+'(統一テスト) 「50点以下」を入力して下さい。');
        return;
    }

    var gokei = 0;
    if (parseInt(check_value) < 5 && test.value != "") {
        gokei = eval(attend.value) + eval(avg_sum.value) + eval(test.value);
    } else {
        gokei = eval(attend.value) + eval(avg_sum.value);
    }

    gokei = Math.round(gokei * 10) / 10;
    document.forms[0].SW_SEISEKI.value = gokei;
    document.forms[0].SEISEKI.value = gokei;
}
