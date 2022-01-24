<!--kanji=漢字-->
<!-- <?php # $RCSfile: knje311Form1.js,v $ ?> -->
<!-- <?php # $Revision: 56587 $ ?> -->
<!-- <?php # $Date: 2017-10-22 21:54:51 +0900 (日, 22 10 2017) $ ?> -->


function myBtnSubmit(cmd) {
    if (cmd == 'update') {
        var seq_name = document.forms[0]['SW_SEQ\[\]'];
        var fin_name = document.forms[0]['SW_SENKOU_FIN\[\]'];
        var seq_value = "";
        var fin_value = "";
        var sep = "";
        for (var i = 0; i < seq_name.length; i++) {
            seq_value = seq_value + sep + seq_name[i].value;
            fin_value = fin_value + sep + fin_name[i].value;
            sep = ",";
        }
        if (seq_value == "") {
            seq_value = seq_name.value;
            fin_value = fin_name.value;
        }
        document.forms[0].DATA_SEQ.value        = seq_value;
        document.forms[0].DATA_SENKOU_FIN.value = fin_value;
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


function myCheckText(obj) {
    if (obj.value != "1" && obj.value != "2" && obj.value != "9" && obj.value != "") {
        alert("選考結果は、１（通過）、２（不可）、９（その他）のいずれかを入力して下さい。");
        obj.focus();
        return false;
    }
}


function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
