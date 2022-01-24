function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_SCORE.value == '1') {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function interviewAllOk() {
    document.forms[0].CHANGE_SCORE.value = '1';
    $('.judgeDiv1 select').each(function(index, element) {
        var examNo = element.name.split("_")[1];
        if ($(document.forms[0]["JUDGEMENT2_" + examNo]).val() != '3') {
            $(document.forms[0]["JUDGEDIV_" + examNo]).val(1);
            $(document.forms[0]["JUDGEMENT2_" + examNo]).val(1);
        }
    });
    $('#dataTable tr .examTd').each(function(index, element){
        var examNo = element.innerHTML;
        if ($(document.forms[0]["JUDGEMENT2_" + examNo]).val() != '3') {
            changeJudgeDiv(document.forms[0]["JUDGEDIV_" + examNo], examNo);
        }
    });
}

function changeScore(obj) {
    document.forms[0].CHANGE_SCORE.value = '1';
    var i, examNo, desirediv, testdiv1, remark4, remark5, sucDesiredivOpts, remark4Obj, remark5Obj, opt, remark4valOld, remark5valOld;
    if (slideJs) {
        if (/REMARK5_*/.test(obj.name)) {
            // 合格コースを変更した際、合格区分のコンボのオプションを選択可能合格コースに入れ替える
            examNo = obj.name.substring("REMARK5_".length);
            desirediv = document.forms[0]["HID_DESIREDIV_" + examNo].value; // 受験区分
            testdiv1 = document.forms[0]["HID_TESTDIV1_" + examNo].value; // 出願コース
            remark5 = obj.value;
            remark4Obj = document.forms[0]["REMARK4_" + examNo];
            sucDesiredivOpts = slideJs["DESIREDIV"][desirediv + "-" + testdiv1 + "-" + remark5]; // 選択可能合格区分

            remark4valOld = remark4Obj.value;
            cmbClear(remark4Obj, 0);

            if (obj.value != '') {
                document.forms[0]["JUDGEMENT2_" + examNo].value = '1';
                var valFlg = false;
                if (sucDesiredivOpts) {
                    for (i = 0; i < sucDesiredivOpts.length; i++) {
                        opt = new Option();
                        opt.value = sucDesiredivOpts[i].value;
                        opt.text = sucDesiredivOpts[i].name;
                        remark4Obj.options[remark4Obj.options.length] = opt;
                        if (remark4valOld == sucDesiredivOpts[i].value) {
                            valFlg = true;
                        }
                    }
                }
                if (valFlg) {
                    remark4Obj.value = remark4valOld;
                }
            } else {
                document.forms[0]["JUDGEMENT2_" + examNo].value = '2';
            }
        }
    }
}

function changeJudgeDiv(obj, examNo) {
    document.forms[0].CHANGE_SCORE.value = '1';
    var desirediv = document.forms[0]["HID_DESIREDIV_" + examNo].value; // 受験区分
    var testdiv1  = document.forms[0]["HID_TESTDIV1_" + examNo].value;  // 出願コース
    if (document.forms[0]["HOPE_" + examNo]) {
        document.forms[0]["HOPE_" + examNo].disabled = false;
    }
    if (obj.value == '3') {
        document.forms[0]["REMARK4_" + examNo].disabled = true;
        document.forms[0]["REMARK5_" + examNo].disabled = true;
        document.forms[0]["JUDGEMENT2_" + examNo].disabled = true;
        document.forms[0]["REMARK4_" + examNo].value = '';
        document.forms[0]["REMARK5_" + examNo].value = '';
        document.forms[0]["JUDGEMENT2_" + examNo].value = obj.value;
    } else {
        document.forms[0]["REMARK4_" + examNo].disabled = false;
        document.forms[0]["REMARK5_" + examNo].disabled = false;
        document.forms[0]["JUDGEMENT2_" + examNo].disabled = false;
        document.forms[0]["REMARK4_" + examNo].value = document.forms[0]["HID_REMARK4_" + examNo].value;
        document.forms[0]["REMARK5_" + examNo].value = document.forms[0]["HID_REMARK5_" + examNo].value;
        if (document.forms[0].HID_L004NMSP1.value == '1') {
            document.forms[0]["JUDGEMENT2_" + examNo].value = document.forms[0]["HID_JUDGEMENT2_" + examNo].value;
        } else {
            if (obj.value == '') {
                document.forms[0]["REMARK4_" + examNo].value = '';
                document.forms[0]["REMARK5_" + examNo].value = '';
                document.forms[0]["JUDGEMENT2_" + examNo].value = '';
            }
            if (obj.value == '1') {
                var sucDesiredivOpts = slideJs["TESTDIV1"][desirediv + "-" + testdiv1]; // 選択可能合格コース
                var courseObj = document.forms[0]["REMARK5_" + examNo];
                if (sucDesiredivOpts) {
                    cmbSet(sucDesiredivOpts, courseObj, '');
                }
                document.forms[0]["REMARK5_" + examNo].value = document.forms[0]["HID_DESIREDIV_" + examNo].value;
                changeScore(document.forms[0]["REMARK5_" + examNo]);
                document.forms[0]["REMARK5_" + examNo].disabled = true;
                document.forms[0]["JUDGEMENT2_" + examNo].value = obj.value;
            }
            if (obj.value == '2') {
                var sucDesiredivOpts = slideJs["TESTDIV1"][desirediv + "-" + testdiv1]; // 選択可能合格コース
                var courseObj = document.forms[0]["REMARK5_" + examNo];
                if (sucDesiredivOpts) {
                    cmbSet(sucDesiredivOpts, courseObj, desirediv);
                }

                document.forms[0]["REMARK5_" + examNo].value = '';
                document.forms[0]["JUDGEMENT2_" + examNo].value = obj.value;
                cmbClear(document.forms[0]["REMARK4_" + examNo], 0);

                if (document.forms[0]["HOPE_" + examNo]) {
                    document.forms[0]["HOPE_" + examNo].disabled = true;
                }
            }
        }
    }
    if (document.forms[0].HID_L004NMSP1.value == '1') {
        document.forms[0]["REMARK5_" + examNo].disabled = true;
        document.forms[0]["JUDGEMENT2_" + examNo].disabled = true;
        document.forms[0]["JUDGEMENT2_" + examNo].value = obj.value;
        if (obj.value == '1') {
            document.forms[0]["REMARK5_" + examNo].value = document.forms[0]["HID_DESIREDIV_" + examNo].value;
            var sucDesiredivOpts = slideJs["DESIREDIV"][desirediv + "-" + testdiv1 + "-" + document.forms[0]["REMARK5_" + examNo].value]; // 選択可能合格区分
            var courseObj = document.forms[0]["REMARK4_" + examNo];
            if (sucDesiredivOpts) {
                cmbSet(sucDesiredivOpts, courseObj, '');
            }
        } else {
            document.forms[0]["REMARK5_" + examNo].value = '';
            cmbClear(document.forms[0]["REMARK4_" + examNo], 0);
        }
    }
}
function cmbSet(optVal, setObj, removeOpt) {
    cmbClear(setObj, 0);
    for (i = 0; i < optVal.length; i++) {
        var opt = new Option();
        opt.value = optVal[i].value;
        opt.text = optVal[i].name;
        if (removeOpt != '' && opt.value == removeOpt) {
            continue;
        }
        setObj.options[setObj.options.length] = opt;
    }
}
function cmbClear(obj, len) {
    obj.options.length = 0;
    var opt = new Option();
    opt.value = '';
    opt.text = '';
    obj.options[len] = opt;
}
