//<!--kanji=漢字-->

function btn_submit(cmd) {
    var i;
    if (cmd == 'add' || cmd == 'cancel'){
        if (bottom_frame1().SCHREGNO.value == "") {
            alert('学籍番号を入力して下さい。');
            return;
        }
    }
//    if (cmd == 'delete') {
//        var w = document.forms[0].category_selected.selectedIndex;
//
//        if (w == 1 || w == 0) {
//            alert('指定範囲が正しくありません。');
//            return;
//        }
//        //
//        //assign new values to arrays
//        var j = 0;
//        for (i = 2; i < document.forms[0].category_selected.length; i++) {   
//            if ( document.forms[0].category_selected.options[i].selected) {  
//                j++;
//            }
//        }
//
//        if (j == 0) {
//            return false;
//        }
//
//        if (!confirm('{rval MSG103}')) {
//            return false;
//        }
//
//        for (i = 0; i < top_frame1().length; i++) {
//            var e = top_frame1().elements[i];
//            if (e.name == 'category_selected') {
//                e.name = 'category_selected[]';
//                top_frame1().cmd.value = cmd;
//                top_frame1().submit();
//                e.name = 'category_selected';
//                break;
//            }
//        }
//        return false;
//    }
    if (cmd == 'init') {
        var w = document.forms[0].DISP.selectedIndex;

        var v = document.forms[0].DISP.options[w].value;
        if (v == 1 || v == 2) {
           cmd = 'list';
        } else if (v == 3 || v == 4) {
           cmd = 'list2';
        }

        bottom_frame1().DISP2.value = v;

    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function top_frame1() {
    return window.parent.top_frame.document.forms[0];
}
function bottom_frame1() {
    return window.parent.bottom_frame.document.forms[0];
}
function checkRisyu() {
    if (document.forms[0].MIRISYU[0].checked && document.forms[0].RISYU[1].checked) {
        alert('未履修科目が出力される状態になっています。');
        document.forms[0].RISYU[0].checked = true;
        document.forms[0].RISYU[1].checked = false;
    }
}
function checkKisai1(code) {
    var code_array = code.split(",");
    var cd = code_array[0];
    var isTarget = -1 != getKisaiSekiEnabledCertifKindCds().indexOf(cd);
    if (isTarget) {
        document.forms[0].KISAI_SEKI.disabled = false;
    } else {
        document.forms[0].KISAI_SEKI.disabled = true;
    }
}
function checkPrintStamp(code) {
    var code_array;
    var cd;
    var tgt = [];
    if (document.forms[0].PRINT_STAMP) {
        code_array = code.split(",");
        cd = code_array[0];

        //// 調査書 成績証明書 卒業証明書 在学証明書
        //tgt = ['008' , '009' , '025' , '026' , '058' , '006' , '027' , '001' , '022' , '023' , '004' , '012'] 
        tgt = JSON.parse(document.getElementsByName("printStampCertifKindcds")[0].value);

        document.forms[0].PRINT_STAMP.disabled = -1 == tgt.indexOf(cd);
    }
}
function setPrice() {
    var code = bottom_frame1().CERTIF_KD.value;
    var code_array = code.split(",");
    var certifKindcd = code_array[0];
    var graduateFlg = bottom_frame1().GRADUATE_FLG.value;
    var price;
    var formPrice = document.getElementById("PRICE");
    if (formPrice) {
        price = getPrice()[certifKindcd + "-" + graduateFlg];
        if (price != undefined && price != '') {
            formPrice.innerHTML = price + "円";
        } else {
            formPrice.innerHTML = "";
        }
    }
}

function setRadioValChecked(val, name) {
    var radios = document.getElementsByName(name);
    if (radios) {
        for (i = 0; i < radios.length; i++) {
            radios[i].checked = radios[i].value === val;
        }
    }
}

function checkTyousasyo2020shojikouExtendsSelect(code) {
    var code_array = code.split(",");
    var certifKindcd = code_array[0];
    var dis = -1 == ["058", "059"].indexOf(certifKindcd);
    var name = "tyousasyo2020shojikouExtendsSelect";
    var radios = document.getElementsByName(name);
    if (radios) {
        for (i = 0; i < radios.length; i++) {
            radios[i].disabled = dis; 
        }
    }
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')) {
        return false;
    }
}

function OnAuthError()
{
    alert('{rval MZ0026}');
    closeWin();
}

function bottm_frm_disable1(){

    var i;
    var frm = bottom_frame1();
    var e;
    for (i = 0; i < bottom_frame1().length; i++) {  
        e = frm.elements[i];
        if (e.name == 'NAME' || e.name == 'BN_DATE' || e.name == 'HR_CLASS' || e.name == 'GAKKA' || e.name == 'KATEI' || e.name == 'HR_TEARCH' || e.name == 'SOTUGYOU') {
           e.disabled = true;
        } else if (e.name == 'btn_cancel') {
            e.disabled = true;
        } else if (e.name != 'REMARK1') {
            e.disabled = false;
        }
    }

}

function gakusekichg() {
    bottom_frame1().UPDATED.value = '1';
}

function reloadTop(i) {
    window.parent.top_frame.document.location.href = "knjg010index.php?cmd=list&DISP=" + i;
}

function checkCertifKind() {
    var code = bottom_frame1().CERTIF_KD.value;
    checkKisai1(code);
    checkPrintStamp(code);
    setPrice();
    checkTyousasyo2020shojikouExtendsSelect(code);
}

window.onload = form1onload;

function form1onload() {
    check_remark1();
    checkCertifKind();
    gvalCalcChecked();
}

function check_remark1() {
    if (document.forms[0].DISP2.value == 1) {
        document.forms[0].REMARK1.disabled = document.forms[0].certifNoSyudou.value != 1;
    }
}

function jstrlen(str) {
    var len = 0;
    var i;
    str = escape(str);
    for (i = 0; i < str.length; i++, len++) {
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

function gvalCalcChecked() {
    var gvalCalcCheck1 = document.getElementById("GVAL_CALC_CHECK2"); // 多重平均
    var ids = ["PRINT_AVG_RANK1", "PRINT_AVG_RANK2"];
    var obj;
    var dis = !(gvalCalcCheck1 && gvalCalcCheck1.checked);
    for (var i in ids) {
        obj = document.getElementById(ids[i]);
        if (obj) {
            obj.disabled = dis;
        }
    }
}

