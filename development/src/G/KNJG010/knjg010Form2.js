<!--kanji=漢字-->

function clog(o) { if (window.console && window.console.log) { window.console.log(o); } }

function btn_submit(cmd) {
    var i;
    if (cmd == 'add' || cmd == 'cancel') {
        if (bottom_frame_form().SCHREGNO.value == "") {
            alert('学籍番号を入力して下さい。');
            return;
        }
    }

    if (cmd == 'delete') {
        var w = document.forms[0].category_selected.selectedIndex;

        if (w == 1 || w == 0) {
            alert('指定範囲が正しくありません。');
            return;
        }
        //
        //assign new values to arrays
        var j = 0;
        for (i = 2; i < document.forms[0].category_selected.length; i++) {
            if (document.forms[0].category_selected.options[i].selected) {
                j++;
            }
        }

        if (j == 0) {
            return false;
        }

        if (!confirm('{rval MSG103}')) {
            return false;
        }

        for (i = 0; i < top_frame_form().length; i++) {
            var e = top_frame_form().elements[i];
            if (e.name=='category_selected') {
                e.name = 'category_selected[]';
                top_frame_form().cmd.value = cmd;
                top_frame_form().submit();
                e.name = 'category_selected';
                break;
            }
        }
        return false;
    }
    if (cmd == 'init') {
        var w = document.forms[0].DISP.selectedIndex;

        var v = document.forms[0].DISP.options[w].value;
        if (v == 1 || v == 2) {
           cmd = 'list';
        } else if (v == 3 || v == 4) {
           cmd = 'list2';
        }

        bottom_frame_form().DISP2.value = v;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function top_frame_form() {
    return window.parent.top_frame.document.forms[0];
}
function bottom_frame_form() {
    return window.parent.bottom_frame.document.forms[0];
}
function bottom_frame() {
    return window.parent.bottom_frame;
}
function csv(arr) {
    var rtn = "";
    var len = arr.length;
    var i;
    for (i = 0; i < len; i++) {
        if (i > 0) {
            rtn += ",";
        }
        rtn += arr[i];
    }
    return rtn;
}
function btn_issuesubmit(cmd, SERVLET_URL) {
    var i, j;
    var back;
    var backtmp = [];
    var arr = [];
    var dataDesc = {};
    var disp = document.forms[0].DISP.value;
    if (top_frame_form().category_name.length == 2) {
        if (cmd == 'print2') {
            alert('正常に更新されました。');
        }
        return;
    }
    for (i = 2; i < top_frame_form().category_name.length; i++) {

        var tmp = top_frame_form().category_name.options[i].value.split(',');
        top_frame_form().category_name.options[i].selected = 1;
        //処理日付取得
        //tmp[8] = bottom_frame_form().PR_DATE.value;
        //記載責任者番号取得
        //var index = bottom_frame_form().KISAI_SEKI.selectedIndex;
        //tmp[5] = bottom_frame_form().KISAI_SEKI.options[index].value;
        //評定読替情報取得
        //if (bottom_frame_form().HYOUTEI.checked)
        //    tmp[6] = bottom_frame_form().HYOUTEI.value;
        //else
        //    tmp[6] = "";
        //漢字出力情報取得
        //if (bottom_frame_form().KJ_OUT[0].checked)
        //    tmp[7] = bottom_frame_form().KJ_OUT[0].value;
        //else
        //    tmp[7] = bottom_frame_form().KJ_OUT[1].value;
        //取得データ格納
        top_frame_form().category_name.options[i].value = "";
        arr = [];
        dataDesc = {};
        if (cmd == 'print' || cmd == 'print2' || cmd == 'testprint') {
            back = "";
            for (j = 0; j < 9; j++) {
                arr[arr.length] = tmp[j];
                switch (j) {
                  case 0:dataDesc[j] = "SCHREGNO"; break; 
                  case 1:dataDesc[j] = "CERTIF_KINDCD"; break;
                  case 2:dataDesc[j] = "YEAR"; break;
                  case 3:dataDesc[j] = "SEMESTER"; break;
                  case 4:dataDesc[j] = "GRADE"; break;
                  case 5:dataDesc[j] = "TR_CD1"; break;
                  case 6:dataDesc[j] = "HYOUTEI"; break;
                  case 7:dataDesc[j] = "KJ_OUT"; break;
                  case 8:dataDesc[j] = "PR_DATE"; break;
                }
            }
            dataDesc[arr.length] = "CERTIF_NO";  // idx:9
            arr[arr.length] = tmp[j];

            //概評情報取得
            dataDesc[arr.length] = "GAIHYOU_GAKUNEN_COUNT";  // idx:10
            arr[arr.length] = tmp[29] != '' ? tmp[29] : "";
            j++;

            dataDesc[arr.length] = "certifNoSyudou"; // idx:11
            if (document.forms[0].certifNoSyudou.value == '1' || (disp == "3" || disp == "4") && document.forms[0].certif_no_8keta.value == '1') {
                arr[arr.length] = tmp[28] != '' ? tmp[28] : "";
            } else {
                arr[arr.length] = "";
            }

            //その他住所取得
            dataDesc[arr.length] = "sonotaJuusho"; // idx:12
            arr[arr.length] = tmp[30] != '' ? tmp[30] : "";

            //前籍校を含まない取得
            dataDesc[arr.length] = "zenseki"; // idx:13
            arr[arr.length] = tmp[31] != '' ? tmp[31] : "";

            //入学・卒業日付は年月で表示する
            dataDesc[arr.length] = "nentsuki"; // idx:14
            arr[arr.length] = tmp[36] != '' ? tmp[36] : "";

            //印影出力する
            dataDesc[arr.length] = "inei"; // idx:15
            arr[arr.length] = tmp[38] != '' ? tmp[38] : "";

            //半期認定フォーム
            dataDesc[arr.length] = "hankiNinteiForm"; // idx:16
            arr[arr.length] = tmp[39] != '' ? tmp[39] : "";

            //留学単位数を0表示
            dataDesc[arr.length] = "ryugaku"; // idx:17
            arr[arr.length] = tmp[40] != '' ? tmp[40] : "";

            //総合的な学習の時間単位数を0表示
            dataDesc[arr.length] = "sogaku"; // idx:18
            arr[arr.length] = tmp[41] != '' ? tmp[41] : "";

            //
            dataDesc[arr.length] = "certif_index"; // idx:19
            arr[arr.length] = tmp[21] != '' ? tmp[21] : "";

            clog(["category_name", i, arr, dataDesc]);
            top_frame_form().category_name.options[i].value = csv(arr);
            backtmp[i] = csv(tmp);

        } else {
            back = "";
            for (j = 0; j < 43; j++) {
                back += tmp[j] + ",";
            }
            back += tmp[j];
            backtmp[i] = back;
        }
    }

    for (i = 2; i < top_frame_form().category_selected.length; i++) {
        top_frame_form().category_selected.options[i].selected = 0;
    }

    if (cmd == 'print2') {
        alert('正常に更新されました。印刷を開始します。');
    }
    if (cmd == 'print' || cmd == 'print2' || cmd == 'testprint') {
        //６年用フォーム選択
        if (bottom_frame_form().FORM6 && bottom_frame_form().FORM6.checked) {
            document.forms[0].FORM6.value = bottom_frame_form().FORM6.value;
        } else {
            document.forms[0].FORM6.value = "";
        }

        //何年用のフォームを使うのか決める
        if (bottom_frame_form().FORM6 && bottom_frame_form().FORM6.checked) {
            document.forms[0].NENYOFORM.value = bottom_frame_form().NENYOFORM_CHECK.value
        } else {
            document.forms[0].NENYOFORM.value = bottom_frame_form().NENYOFORM_SYOKITI.value
        }

        //未履修科目出力情報取得
        if (document.forms[0].NOT_KINDAI.value == '1') {
            if (bottom_frame_form().MIRISYU[0].checked) {
                document.forms[0].MIRISYU.value = bottom_frame_form().MIRISYU[0].value;
            } else {
                document.forms[0].MIRISYU.value = bottom_frame_form().MIRISYU[1].value;
            }
        }

        //履修のみ科目出力情報取得
        if (document.forms[0].NOT_KINDAI.value == '1') {
            if (bottom_frame_form().RISYU[0].checked) {
                document.forms[0].RISYU.value = bottom_frame_form().RISYU[0].value;
            } else {
                document.forms[0].RISYU.value = bottom_frame_form().RISYU[1].value;
            }
        }
        if (cmd == "testprint") {
            top_frame_form().PARAM_TESTPRINT.value = "1";
        }

        action = top_frame_form().action;
        target = top_frame_form().target;

//    url = location.hostname;
//    top_frame_form().action = "http://" + url +"/cgi-bin/printenv.pl";
        top_frame_form().action = SERVLET_URL +"/KNJG";
        top_frame_form().target = "_blank";

        top_frame_form().submit();

        top_frame_form().action = action;
        top_frame_form().target = target;
        if (cmd == 'print2') {
            btn_submit('list');
        }
        for (i = 2; i < top_frame_form().category_name.length; i++) {
            top_frame_form().category_name.options[i].value = backtmp[i];
        }
        top_frame_form().PARAM_TESTPRINT.value = null;
    } else {
        for (i = 2; i < top_frame_form().category_name.length; i++) {
            top_frame_form().category_name.options[i].value = backtmp[i];
        }
        for (i = 0, j = 0; i < top_frame_form().length; i++) {
            var e = top_frame_form().elements[i];
            if (e.name == 'category_name') {
                e.name = 'category_name[]';
                top_frame_form().cmd.value = cmd;
                top_frame_form().submit();
                e.name = 'category_name';
                break;
            }
        }
    }

    return false;

}

function ClearList(OptionList)
{
    OptionList.length = 2;
}

function lmove(side,cmd)
{
    var i, j;
    var temp1 = [];
    var temp2 = [];
    var tempa = [];
    var tempb = [];
    var current1 = 0;
    var current2 = 0;
    var attribute1;
    var attribute2;
    var disp = document.forms[0].DISP.value;

    var w = document.forms[0].category_selected.selectedIndex;
    var tmp;
    var work;

    if (w == 1 || w == 0) {
        alert('指定範囲が正しくありません。');
        return;
    }

    var hyouteiRadio = document.getElementsByName("hyoteiYomikaeRadio")[0].value;

    //assign what select attribute treat as attribute1 and attribute2
    current1 = 2;
    current2 = 2;
    attribute1 = document.forms[0].category_selected;
    attribute2 = document.forms[0].category_name;

    //fill an array with old values
    for (i = 2; i < attribute2.length; i++) {
        temp1[current2] = attribute2.options[i].value;
        tempa[current2] = attribute2.options[i].text;
        current2++;
    }

    for (i = 2; i < attribute1.length; i++) {
        tmp = attribute1.options[i].value.split(',');
        if (side == "leftall") {
            if (cmd == 'list2') {
                if (tmp[24] == '0' || tmp[37] == '1') {
                    if (attribute1.options[i].selected) {
                        clog(["set selected 0", "cmd", cmd, "24:", tmp[24], "37:", tmp[37], "in", tmp]);
                    }
                    attribute1.options[i].selected = 0;
                } else {
                    attribute1.options[i].selected = 1;
                }
            } else {
                if (tmp[27] == '1' || tmp[37] == '1') {
                    if (attribute1.options[i].selected) {
                        clog(["set selected 0", "cmd", cmd, "27:", tmp[27], "37:", tmp[37], "in", tmp]);
                    }
                    attribute1.options[i].selected = 0;
                } else {
                    attribute1.options[i].selected = 1;
                }
            }
        } else if (cmd == 'list2') {
            if (tmp[24] == '0' || tmp[37] == '1') {
                if (attribute1.options[i].selected) {
                    clog(["set selected 0", "cmd", cmd, "24:", tmp[24], "37:", tmp[37], "in", tmp]);
                }
                attribute1.options[i].selected = 0;
            }
        } else {
            if (tmp[27] == '1' || tmp[37] == '1') {
                if (attribute1.options[i].selected) {
                    clog(["set selected 0", "cmd", cmd, "27:", tmp[27], "37:", tmp[37], "in", tmp]);
                }
                attribute1.options[i].selected = 0;
            }
        }
    }

    //assign new values to arrays
    for (i = 2; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            work = attribute1.options[i].value.split(',');
            //処理日付取得
            work[8] = bottom_frame_form().PR_DATE.value;
            if (disp == "3" || disp == "4") {
                //記載責任者番号取得---2005.06.09
                console.log(disp + ", " + work[5]);
                if (work[5] == '') {
                    var index = bottom_frame_form().KISAI_SEKI.selectedIndex;
                    work[5] = bottom_frame_form().KISAI_SEKI.options[index].value;
                }
                console.log(disp + ", aft " + work[5]);
            }
            //評定読替情報取得
            if (bottom_frame_form().HYOUTEI) {
                if (hyouteiRadio) {
                    for (j = 0; j < bottom_frame_form().HYOUTEI.length; j++) {
                        if (bottom_frame_form().HYOUTEI[j].checked) {
                            work[6] = bottom_frame_form().HYOUTEI[j].value;
                        }
                    }
                    if (work[6] === "0") {
                        work[6] = "";
                    }
                } else {
                    if (bottom_frame_form().HYOUTEI.checked) {
                        work[6] = bottom_frame_form().HYOUTEI.value;
                    } else {
                        work[6] = "";
                    }
                }
            }
            //漢字出力情報取得
            if (bottom_frame_form().KJ_OUT[0].checked) {
                work[7] = bottom_frame_form().KJ_OUT[0].value;
            } else {
                work[7] = bottom_frame_form().KJ_OUT[1].value;
            }
            //概評情報取得
            if (bottom_frame_form().GAIHYOU.checked) {
                work[29] = bottom_frame_form().GAIHYOU.value;
            } else {
                work[29] = "";
            }
            //その他住所取得
            if (bottom_frame_form().SONOTAJUUSYO.checked) {
                work[30] = bottom_frame_form().SONOTAJUUSYO.value;
            } else {
                work[30] = "";
            }

            //前籍校を含まない情報取得
            if (bottom_frame_form().tyousasyoNotPrintAnotherAttendrec.checked) {
                work[31] = bottom_frame_form().tyousasyoNotPrintAnotherAttendrec.value;
            } else {
                work[31] = "";
            }

            //入学・卒業日付は年月で表示する
            if (bottom_frame_form().ENT_GRD_DATE_FORMAT.checked) {
                work[36] = bottom_frame_form().ENT_GRD_DATE_FORMAT.value;
            } else {
                work[36] = "";
            }

            temp1[current2] = csv(work);

            tmp = temp1[current2].split(',');
            //学籍番号　氏名表示
            tempa[current2] = " " + tmp[0] + "  " + tmp[15] + "  " + tmp[14] + "　　" + tmp[13];

            current2++;

        } else {
            temp2[current1] = attribute1.options[i].value;
            tempb[current1] = attribute1.options[i].text;
            current1++;
        }
    }

    ClearList(attribute2);
    //generating new options
    for (i = 2; i < current2; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
    }
    attribute2.length = current2;

    //generating new options
    ClearList(attribute1);
    if (current1 > 2) {
        for (i = 2; i < current1; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
    attribute1.length = current1;

    bottom_frame_form().KJ_OUT[0].checked = true;
}
function rmove(side,cmd)
{
    var i, j;
    var temp1 = [];
    var temp2 = [];
    var tempa = [];
    var tempb = [];
    var current1 = 2;
    var current2 = 2;
    var attribute1;
    var attribute2;

    var w = document.forms[0].category_name.selectedIndex;

    if (w == 1 || w == 0) {
        alert('指定範囲が正しくありません。');
        return;
    }
    //assign what select attribute treat as attribute1 and attribute2
    current1 = 2;
    current2 = 2;
    attribute1 = document.forms[0].category_name;
    attribute2 = document.forms[0].category_selected;

    //fill an array with old values
    for (i = 2; i < attribute2.length; i++) {
        temp1[current2] = attribute2.options[i].value;
        tempa[current2] = attribute2.options[i].text;
        current2++;
    }

    if (side == "rightall") {
        for (i = 2; i < attribute1.length; i++) {
            attribute1.options[i].selected = 1;
        }
    }

    //assign new values to arrays
    for (i = 2; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            temp1[current2] = attribute1.options[i].value;
            var tmp = temp1[current2].split(',');
            if (cmd == 'list2') {
                tempa[current2] = "　" + tmp[10];
                if (tmp[9].length == 1) { //発行番号１桁
                    tempa[current2] += "　　" + tmp[9] + "　 ";
                } else if (tmp[9].length == 2) { //発行番号２桁
                    tempa[current2] += "　　" + tmp[9] + "　";
                } else if (tmp[9].length == 3) { //発行番号３桁
                    tempa[current2] += "　　" + tmp[9] + " ";
                } else if (tmp[9].length == 4) { //発行番号４桁
                    tempa[current2] += "　　" + tmp[9];
                }
            } else {
                tempa[current2] = "　" + tmp[10] + "　　　";
                if (tmp[11] == "") {
                    tempa[current2] += "　";
                } else {
                    tempa[current2] += tmp[11];
                }
            }
            tempa[current2] += "　 " + tmp[15];
            tempa[current2] += "　　" + tmp[0];
            tempa[current2] += "　　　" + tmp[20];
            tempa[current2] += "　　　" + tmp[13];
            for (j = window.parent.bottom_frame.jstrlen(tmp[13])-1; j < 16; j=j+3) {
                tempa[current2] += "　";
            }
            tempa[current2] += tmp[14];
            for (j = window.parent.bottom_frame.jstrlen(tmp[14])-1; j < 16; j=j+3) {
                tempa[current2] += "　";
            }
            tempa[current2] += tmp[26];
            bottm_frm_disable2(cmd);

            current2++;
        } else {
            temp2[current1] = attribute1.options[i].value;
            tempb[current1] = attribute1.options[i].text;
            current1++;
        }
    }

    ClearList(attribute2);
    //generating new options
    for (i = 2; i < current2; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
    }
    attribute2.length = current2;

    //generating new options
    ClearList(attribute1);
    if (current1 > 2) {
        for (i = 2; i < current1; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
    attribute1.length = current1;

}
function issue_select2()
{
    var i, j;
    var w = document.forms[0].category_selected.selectedIndex;

    if (w < 2) {
        return;
    }

    var disp = document.forms[0].DISP.value;
    var hyouteiRadio = document.getElementsByName("hyoteiYomikaeRadio")[0].value;
    var k;
    var v = document.forms[0].category_selected.options[w].value;
    var tmp = v.split(',');

    bottom_frame_form().TK_DATE.value  = tmp[20];
    bottom_frame_form().SCHREGNO.value = tmp[0];
    bottom_frame_form().NAME.value     = tmp[14];
    bottom_frame_form().BN_DATE.value  = tmp[16]
    bottom_frame_form().HR_CLASS.value = tmp[15];
    bottom_frame_form().GAKKA.value  = tmp[18];
    bottom_frame_form().KATEI.value  = tmp[17];
    bottom_frame_form().SOTUGYOU.value  = tmp[19];
    bottom_frame_form().HR_TEARCH.value  = tmp[25];
    bottom_frame_form().GRADUATE_FLG.value  = tmp[23];
    bottom_frame_form().REMARK1.value  = tmp[28];
    if (disp == "1" || disp == "2") {
        window.parent.bottom_frame.document.getElementById("DISP_CERTIF_NO").innerHTML  = tmp[9];
    }
    //パラメータデータがある時、値をセット
    if (tmp[35] != "") {
        //2 記載責任者
        bottom_frame_form().KISAI_SEKI.value = tmp[5];
        //3 漢字出力
        if (tmp[7] == "1") {
            bottom_frame_form().KJ_OUT[0].checked = true;
        } else {
            bottom_frame_form().KJ_OUT[1].checked = true;
        }
        //4 未履修科目出力
        if (tmp[34] == "1") {
            bottom_frame_form().MIRISYU[0].checked = true;
        } else {
            bottom_frame_form().MIRISYU[1].checked = true;
        }
        //5 履修のみ科目出力
        if (tmp[33] == "1") {
            bottom_frame_form().RISYU[0].checked = true;
        } else {
            bottom_frame_form().RISYU[1].checked = true;
        }
        //6 フォーム選択
        if (bottom_frame_form().FORM6) {
            bottom_frame_form().FORM6.checked = tmp[32] == "1";
        }
        //7 発行日
        bottom_frame_form().PR_DATE.value  = tmp[8];
        //8 評定読替
        if (bottom_frame_form().HYOUTEI) {
            if (hyouteiRadio) {
                for (j = 0; j < bottom_frame_form().HYOUTEI.length; j++) {
                    if (tmp[6] == '') {
                        bottom_frame_form().HYOUTEI[j].checked = bottom_frame_form().HYOUTEI[j].value === "0";
                    } else {
                        bottom_frame_form().HYOUTEI[j].checked = bottom_frame_form().HYOUTEI[j].value == tmp[6];
                    }
                }

            } else {
                bottom_frame_form().HYOUTEI.checked = tmp[6] == "1";
            }
        }
        //9 その他住所
        bottom_frame_form().SONOTAJUUSYO.checked = tmp[30] == "1";
        //10 学習成績概評
        bottom_frame_form().GAIHYOU.checked = tmp[29] == "1";
        //11 出欠日数（前籍校を含まない）
        bottom_frame_form().tyousasyoNotPrintAnotherAttendrec.checked = tmp[31] == "1";
        //12 入学・卒業日付は年月で表示する
        bottom_frame_form().ENT_GRD_DATE_FORMAT.checked = tmp[36] == "1";
        if (bottom_frame_form().PRINT_STAMP) {
            //印影出力
            bottom_frame_form().PRINT_STAMP.checked = tmp[38] == "1";
        }
        //半期認定フォーム出力
        if (bottom_frame_form().HANKI_NINTEI_FORM) {
            bottom_frame_form().HANKI_NINTEI_FORM.checked = tmp[39] == "1";
        }

        if (tmp[40] == "1") {
            bottom_frame_form().RYUGAKU_CREDIT1.checked = true;
        } else if (tmp[40] == "2") {
            bottom_frame_form().RYUGAKU_CREDIT2.checked = true;
        } else if (0 <= ["008", "009", "025", "026", "058", "059"].indexOf(tmp[1])) { // 調査書
            if (bottom_frame_form().TANIPRINT_RYUGAKU.value == "1") {
                bottom_frame_form().RYUGAKU_CREDIT1.checked = true;
            } else {
                bottom_frame_form().RYUGAKU_CREDIT2.checked = true;
            }
        } else {
            bottom_frame_form().RYUGAKU_CREDIT2.checked = true;
        }

        if (tmp[41] == "1") {
            bottom_frame_form().SOGAKU_CREDIT1.checked = true;
        } else if (tmp[41] == "2") {
            bottom_frame_form().SOGAKU_CREDIT2.checked = true;
        } else if (0 <= ["008", "009", "025", "026", "058", "059"].indexOf(tmp[1])) { // 調査書
            if (bottom_frame_form().TANIPRINT_SOUGOU.value == "1") {
                bottom_frame_form().SOGAKU_CREDIT1.checked = true;
            } else {
                bottom_frame_form().SOGAKU_CREDIT2.checked = true;
            }
        } else {
            bottom_frame_form().SOGAKU_CREDIT2.checked = true;
        }

        if (bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_PRINCIPAL1) {
            if (tmp[42] == "1") {
                bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_PRINCIPAL1.checked = true;
            } else if (tmp[42] == "2") {
                bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_PRINCIPAL2.checked = true;
            } else if (0 <= ["008", "009", "025", "026", "058", "059"].indexOf(tmp[1])) { // 調査書
                if (bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_PRINCIPAL.value == "1") {
                    bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_PRINCIPAL1.checked = true;
                } else {
                    bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_PRINCIPAL2.checked = true;
                }
            } else {
                bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_PRINCIPAL2.checked = true;
            }
        }

        if (bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_HR_STAFF1) {
            if (tmp[43] == "1") {
                bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_HR_STAFF1.checked = true;
            } else if (tmp[43] == "2") {
                bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_HR_STAFF2.checked = true;
            } else if (0 <= ["008", "009", "025", "026", "058", "059"].indexOf(tmp[1])) { // 調査書
                if (bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_HR_STAFF.value == "1") {
                    bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_HR_STAFF1.checked = true;
                } else {
                    bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_HR_STAFF2.checked = true;
                }
            } else {
                bottom_frame_form().KNJE070_CHECK_PRINT_STAMP_HR_STAFF2.checked = true;
            }
        }

        if (bottom_frame_form().KNJE070D_PRINTHEADERNAME) {
            if (tmp[44] == "1") {
                bottom_frame_form().KNJE070D_PRINTHEADERNAME1.checked = true;
            } else if (tmp[44] == "2") {
                bottom_frame_form().KNJE070D_PRINTHEADERNAME2.checked = true;
            } else if (0 <= ["008", "009", "025", "026", "058", "059"].indexOf(tmp[1])) { // 調査書
                if (bottom_frame_form().KNJE070D_PRINTHEADERNAME.value == "1") {
                    bottom_frame_form().KNJE070D_PRINTHEADERNAME1.checked = true;
                } else {
                    bottom_frame_form().KNJE070D_PRINTHEADERNAME2.checked = true;
                }
            } else {
                bottom_frame_form().KNJE070D_PRINTHEADERNAME2.checked = true;
            }
        }

        if (bottom_frame_form().GVAL_CALC_CHECK) {
            if (tmp[45] == "2") {
                bottom_frame_form().GVAL_CALC_CHECK2.checked = true;
            } else {
                bottom_frame_form().GVAL_CALC_CHECK1.checked = true;
            }
            bottom_frame().gvalCalcChecked();
        }

        if (bottom_frame_form().PRINT_AVG_RANK) {
            if (tmp[46] == "1") {
                bottom_frame_form().PRINT_AVG_RANK1.checked = true;
            } else {
                bottom_frame_form().PRINT_AVG_RANK2.checked = true;
            }
        }

        if (bottom_frame_form().tyousasyo2020shojikouExtendsSelect) {
            window.parent.bottom_frame.setRadioValChecked(tmp[47] || "def", "tyousasyo2020shojikouExtendsSelect");
        }
    } else {
        //7 発行日にログイン日付をセット
        bottom_frame_form().PR_DATE.value  = document.forms[0].SET_PR_DATE.value;
    }
    //UPDATE用の学籍番号、証明書番号をセット
    bottom_frame_form().GET_SCHREGNO.value  = tmp[0];
    bottom_frame_form().GET_CERTIF_INDEX.value = tmp[21];

    for (i = 0; i < bottom_frame_form().CERTIF_KD.length; i++) {
        var x = bottom_frame_form().CERTIF_KD.options[i].value;
        var tmp1 = x.split(',');
        if (tmp1[0] == tmp[1]) {
            bottom_frame_form().CERTIF_KD.selectedIndex = i;
            bottom_frame_form().CERTIF_KD.options[i].selected = 1;
            break;
        }
    }
    window.parent.bottom_frame.checkCertifKind();
    if (disp == "3" || disp == "4") {
        bottom_frame_form().KISAI_SEKI.disabled = true;
    }

    for (i = 0; i < bottom_frame_form().KISAI_SEKI.length; i++) {
        var x = bottom_frame_form().KISAI_SEKI.options[i].value;
        if (x == tmp[5]) {
            bottom_frame_form().KISAI_SEKI.selectedIndex = i;
            bottom_frame_form().KISAI_SEKI.options[i].selected = 1;
            break;
        }
    }

    bottom_frame_form().UPDATED.value = '1';
}

function bottm_frm_disable2(cmd) {
    var i;
    var frm = bottom_frame_form();
    var disp = document.forms[0].DISP.value;
    var e;
    if (cmd == "list2") {
        for (i = 0; i < bottom_frame_form().length; i++) {
            e = frm.elements[i];
            if (e.name == 'KISAI_SEKI' && (disp == "1" || disp == "2") ||
                e.name == 'PR_DATE'    ||
                e.name == 'KJ_OUT'     ||
                e.name == 'HYOUTEI'    ||
                e.name == 'GAIHYOU'    ||
                e.name == 'btn_end'    ||
                e.name == 'FORM6'      ||
                e.name == 'ENT_GRD_DATE_FORMAT' ||
                e.name == 'MIRISYU'    ||
                e.name == 'btn_cancel' ||
                e.name == 'cmd'    ||
                e.name == 'GET_CERTIF_INDEX'    ||
               (e.name == 'btn_calen' && i > 9)
            ) {
                e.disabled = false;
            } else {
                e.disabled = true;
            }
        }

    } else {
        for (i = 0; i < bottom_frame_form().length; i++) {
            e = frm.elements[i];
            if (e.name == 'NAME'      ||
                e.name == 'BN_DATE'   ||
                e.name == 'HR_CLASS'  ||
                e.name == 'GAKKA'     ||
                e.name == 'KATEI'     ||
                e.name == 'HR_TEARCH' ||
                e.name == 'btn_cancel' && (disp == "1" || disp == "2") ||
                e.name == 'SOTUGYOU') {
                e.disabled = true;
            } else {
                e.disabled = false;
            }
        }

        window.parent.bottom_frame.check_remark1();
    }
}


