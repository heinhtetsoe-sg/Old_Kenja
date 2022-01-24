function btn_submit(cmd) {
    document.forms[0].btn_exec.disabled = true;//再読込中、実行ボタンをグレーアウト
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'exec') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function btn_disabled(obj){

    if (obj.value == 1 || obj.value == 3){
        clr = "#cccccc";
        ret = true;
    }else{
        clr = "#000000";
        ret = false;
    }

    document.getElementById("label6").style.color = clr;
    document.getElementById("label7").style.color = clr;
    document.getElementById("label77").style.color = clr;
    if (document.forms[0].KIND[2].checked) {
        document.forms[0].btn_csv.disabled = true;
        document.forms[0].CHUGAKU_CHOSASHO.disabled = false;
        document.getElementById("LABEL_CHUGAKU_CHOSASHO").style.color = "#000000";
    } else {
        document.forms[0].btn_csv.disabled = false;
        document.forms[0].CHUGAKU_CHOSASHO.disabled = true;
        document.getElementById("LABEL_CHUGAKU_CHOSASHO").style.color = "#cccccc";
    }
    document.forms[0].CREATEDIV[0].disabled = ret;
    document.forms[0].CREATEDIV[1].disabled = ret;
    document.forms[0].CREATEDIV[2].disabled = ret;
}
//CSV入出力画面へ
function openCsvgamen() {
    var URL;
    if (document.forms[0].KIND[0].checked) {
        var URL = document.forms[0].URL_ATTENDREC.value;
    } else if (document.forms[0].KIND[1].checked) {
        var URL = document.forms[0].URL_STUDYREC.value;
    } else {
        return;
    }

    wopen(URL, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
}

function cmb_chg(n) {
    var obj = document.all(n);
    var index = obj.selectedIndex;
    var cnt = hr_class[index].length / 2;
    var no = 0;
    document.forms[0].HR_CLASS.length=0;
    for (var i=0; i<cnt ; i++) {
        document.forms[0].HR_CLASS.options[i] = new Option(hr_class[index][no++],hr_class[index][no++]);
    }

}
