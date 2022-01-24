function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == 'csvExe' && document.forms[0].D056.value == '') {
        alert("主な事由を指定して下さい。");
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL, cmd) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";
    /******************/
    /* 日付のチェック */
    /******************/
    if (document.forms[0].EDATE.value == '') {
        alert("日付が不正です。");
        document.forms[0].EDATE.focus();
        return false;
    }
    if (cmd == 'csv' && document.forms[0].D056.value == '') {
        alert("主な事由を指定して下さい。");
        return false;
    }

    if (document.forms[0].NISSUU_BUNBO.value < document.forms[0].NISSUU_BUNSHI.value || document.forms[0].JISUU_BUNBO.value < document.forms[0].JISUU_BUNSHI.value) {
        alert("日数の分数指定が不正です。");
        return false;
    }

    var day   = document.forms[0].EDATE.value;      //印刷範囲日付
    var sdate = document.forms[0].SEME_SDATE.value; //学期開始日付
    var edate = document.forms[0].SEME_EDATE.value; //学期終了日付

    if (sdate > day || edate < day) {
        alert("日付が学期の範囲外です");
        return;
    }

    if (document.forms[0].OUTPUT_FUSHIN.checked) {
        if (document.forms[0].FUSHIN_DIV[1].checked) {
            if (document.forms[0].KETTEN_COUNT.value == '' || parseInt(document.forms[0].KETTEN_COUNT.value) <= 0) {
                alert("成績下位者 欠点科目数を指定して下さい。");
                return;
            }
        }
    }

    document.forms[0].DATE.value = document.forms[0].EDATE.value;
    document.forms[0].cmd.value = cmd;

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

window.addEventListener("load", function() {
    var i;
    setevent("OUTPUT_SHIDOU", "click", kubun("div_shidou", ["SHIDOU_KAMOKUSU_INF2", "SHIDOU_HR_RANK", "SHIDOU_COURSE_RANK", "SHIDOU_MAJOR_RANK", "SHIDOU_GRADE_RANK"]));
    setevent("OUTPUT_SHUKKETSU", "click", kubun("div_shukketsu", ["KESSEKI", "CHIKOKU", "SOUTAI"]));
    setevent("OUTPUT_KYOKAKAMOKU", "click", kubun("div_kyokakamoku", ["NISSUU_BUNBO", "NISSUU_BUNSHI", "JISUU_BUNBO", "JISUU_BUNSHI"]));
    setevent("OUTPUT_DOSUBUPU", "click", kubun("div_dosubupu", ['DOSUBUPU_COURSE']));
    setevent("OUTPUT_YURYO", "click", kubun("div_yuryo", ['JOUI_COURSE', 'YURYO', "YURYO_HR_RANK", "YURYO_COURSE_RANK", "YURYO_MAJOR_RANK", "YURYO_GRADE_RANK"]));
    setevent("OUTPUT_FUSHIN", "click", kubun("div_fushin", ['KAI_COURSE', 'FUSHIN', 'KESSHI_NOZOKU', 'KETTEN_COUNT', 'FUSHIN_ORDER_KETTEN_COUNT', "FUSHIN_HR_RANK", "FUSHIN_COURSE_RANK", "FUSHIN_MAJOR_RANK", "FUSHIN_GRADE_RANK"]));

    // 前定期考査順位出力を使用する帳票
    var zenteikiUse = ["OUTPUT_SHIDOU", "OUTPUT_YURYO", "OUTPUT_FUSHIN"];
    for (i in zenteikiUse) {
        setevent(zenteikiUse[i], "click", function (ev) {
            var checked = anyChecked(zenteikiUse);
            enableDiv("div_zenteikijuni", ["TEST_CD_BEFORE", "OUTPUT_RANK_BEFORE1", "OUTPUT_RANK_BEFORE2", "OUTPUT_RANK_BEFORE3", "OUTPUT_RANK_BEFORE4"])(checked);
        });
    }
}, false);

function setevent(id, evname, cb) {
    var ev = {};
    var el = document.getElementById(id);
    if (el) {
        el.addEventListener(evname, cb);
        ev.srcElement = el;
        cb(ev);
    }
}

function kubun(divid, childinputs) {
    return function (ev) { enableDiv(divid, childinputs)(ev.srcElement.checked); }
}

function anyChecked(inputs) {
    var checked = false;
    var i;
    for (i = 0; i <  inputs.length; i++) {
        checked = checked || document.getElementById(inputs[i]).checked;
    }
    return checked;
}

function enableDiv(divid, childinputs) {
    return function (enabled) {
        var i;
        document.getElementById(divid).style.color = enabled ? "#000000" : "#888888";
        for (i = 0; i < childinputs.length; i++) {
            document.getElementById(childinputs[i]).disabled = !enabled;
        }
    }
}

