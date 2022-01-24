function btn_submit(cmd) {
    if (cmd == 'exec') {
        if (document.forms[0].KIND[0].checked) {
        } else if (document.forms[0].KIND[1].checked) {
        } else if (document.forms[0].KIND[2].checked) {
            if (document.forms[0].METHOD[1].checked) {
            } else {
                //自動生成・観点
                var school_kind = document.forms[0].SCHOOL_KIND_HIDDEN.value;
                if (school_kind != 'P' && school_kind != 'J') {
                    alert('観点データは、小学・中学のみ実行可能です。');
                    return false;
                }
            }
        }
        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            updateFrameLocks();
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

    if (obj.name == 'METHOD') {     //生成方法(自動 or ファイル)
        var f = document.forms[0].METHOD[1].checked;
        document.forms[0].RANGE[0].disabled = f;
        document.forms[0].RANGE[1].disabled = f;
        document.forms[0].ANNUAL.disabled = f;
        document.forms[0].HR_CLASS.disabled = f;
        document.forms[0].COURSECODE.disabled = f;
        document.forms[0].SCHREGNO.disabled = f;
        clr = (document.forms[0].METHOD[1].checked) ? "#cccccc" : "#000000";
        document.getElementById("label4").style.color = clr;
        document.getElementById("label5").style.color = clr;

        if (document.forms[0].KIND[1].checked) {        //出欠データ
            document.forms[0].CREATEDIV[0].disabled = !ret;
            document.forms[0].CREATEDIV[1].disabled = !ret;
            document.forms[0].CREATEDIV[2].disabled = !ret;
        }

        return;
    }

    if (obj.id == 'KIND1' || obj.id == 'KIND3') {        //出欠データ・観点
        document.forms[0].RANGE[0].disabled = false;
        document.forms[0].RANGE[1].disabled = false;
        document.forms[0].ANNUAL.disabled = false;
        document.forms[0].HR_CLASS.disabled = false;
        document.forms[0].COURSECODE.disabled = false;
        document.forms[0].SCHREGNO.disabled = false;
        document.getElementById("label4").style.color = "#000000";
        document.getElementById("label5").style.color = "#000000";
        document.forms[0].METHOD[0].checked = true;


    }else{
        document.forms[0].CREATEDIV[0].disabled = true;
        document.forms[0].CREATEDIV[1].disabled = true;
        document.forms[0].CREATEDIV[2].disabled = true;
    }

//    document.getElementById("label1").style.color = clr;
//    document.getElementById("label3").style.color = clr;
    document.getElementById("label6").style.color = clr;
    document.getElementById("label7").style.color = clr;
    document.getElementById("label77").style.color = clr;
//    document.getElementById("label8").style.color = clr;
//    document.forms[0].METHOD[0].disabled = ret;
//    document.forms[0].METHOD[1].disabled = ret;
//    document.forms[0].HEADERFLG.disabled = ret;
    document.forms[0].CREATEDIV[0].disabled = ret;
    document.forms[0].CREATEDIV[1].disabled = ret;
    document.forms[0].CREATEDIV[2].disabled = ret;
//    document.forms[0].FILE.disabled = ret;
//    document.forms[0].btn_output.disabled = ret;
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
