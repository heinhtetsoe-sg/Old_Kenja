function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG310}\n( 学校種別 )');
        return;
    }

    //必須チェック
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( 入試区分 )');
        return;
    }

    //入力範囲チェック(出力点範囲)
    if (1000 <= Number(document.forms[0].SCORE_S.value) || Number(document.forms[0].SCORE_S.value) < 0 || 1000 <= Number(document.forms[0].SCORE_E.value) || Number(document.forms[0].SCORE_E.value) < 0) {
        alert('{rval MSG901}\n( 出力点範囲 )');
        return;
    }
    //小数点の有無チェック(出力点範囲 開始)
    if (document.forms[0].SCORE_S.value.match(/^-?[0-9]+\.[0-9]+$/)) {
        //小数点チェック(出力点範囲)
        if (String(document.forms[0].SCORE_S.value).split(".")[1].length > 1) {
            alert('小数点第一位まで\n( 出力点範囲 )');
            return;
        }
    }
    //小数点の有無チェック(出力点範囲 終了)
    if (document.forms[0].SCORE_E.value.match(/^-?[0-9]+\.[0-9]+$/)) {
        //小数点チェック(出力点範囲)
        if (String(document.forms[0].SCORE_E.value).split(".")[1].length > 1) {
            alert('小数点第一位まで\n( 出力点範囲 )');
            return;
        }
    }
    //大小チェック(出力点範囲)
    if (Number(document.forms[0].SCORE_S.value) > Number(document.forms[0].SCORE_E.value)) {
        alert('出力点範囲の開始より終了が小さいです。');
        return;
    }

    //入力範囲チェック(出力点範囲 きざみ点)
    if (1000 <= Number(document.forms[0].SCORE_KIZAMI.value) || Number(document.forms[0].SCORE_KIZAMI.value) < 0) {
        alert('{rval MSG901}\n( 出力点範囲 きざみ点 )');
        return;
    }

    //終了-開始が刻み値以上
    if (Number(document.forms[0].SCORE_E.value) - Number(document.forms[0].SCORE_S.value) < Number(document.forms[0].SCORE_KIZAMI.value)) {
        alert('刻みが開始～終了の範囲よりも大きいです。');
        return;
    }

    //小数点の有無チェック(出力点範囲 きざみ点)
    if (document.forms[0].SCORE_KIZAMI.value.match(/^-?[0-9]+$/) || document.forms[0].SCORE_KIZAMI.value.match(/^-?[0-9]+\.[0-9]+$/)) {
        //小数点チェック(出力点範囲 きざみ点)
        if (String(document.forms[0].SCORE_KIZAMI.value).indexOf(".") >= 0
            && String(document.forms[0].SCORE_KIZAMI.value).split(".")[1].length > 1) {
            alert('小数点第一位まで\n( 出力点範囲 きざみ点 )');
            return;
        }
        if (parseFloat(document.forms[0].SCORE_KIZAMI.value) <= 0.0) {
            alert('{rval MSG901}\n0より大\n( 出力点範囲 きざみ点 )');
            return;
        }
    } else {
        alert('{rval MSG901}\n( 出力点範囲 きざみ点 )');
        return;
    }

    //入力範囲チェック(虫眼鏡)
    if (1000 <= Number(document.forms[0].MUSHIMEGANE_S.value) || Number(document.forms[0].MUSHIMEGANE_S.value) < 0 || 1000 <= Number(document.forms[0].MUSHIMEGANE_E.value) || Number(document.forms[0].MUSHIMEGANE_E.value) < 0) {
        alert('{rval MSG901}\n( 虫眼鏡 )');
        return;
    }
    //小数点の有無チェック(虫眼鏡 開始)
    if (document.forms[0].MUSHIMEGANE_S.value.match(/^-?[0-9]+\.[0-9]+$/)) {
        //小数点チェック(虫眼鏡)
        if (String(document.forms[0].MUSHIMEGANE_S.value).split(".")[1].length > 1) {
            alert('小数点第一位まで\n( 虫眼鏡 )');
            return;
        }
    }
    //小数点の有無チェック(虫眼鏡 終了)
    if (document.forms[0].MUSHIMEGANE_E.value.match(/^-?[0-9]+\.[0-9]+$/)) {
        //小数点チェック(虫眼鏡)
        if (String(document.forms[0].MUSHIMEGANE_E.value).split(".")[1].length > 1) {
            alert('小数点第一位まで\n( 虫眼鏡 )');
            return;
        }
    }
    //大小チェック(虫眼鏡)
    if (Number(document.forms[0].MUSHIMEGANE_S.value) > Number(document.forms[0].MUSHIMEGANE_E.value)) {
        alert('虫眼鏡の開始より終了が小さいです。');
        return;
    }

    //入力範囲チェック(虫眼鏡 きざみ点)
    if (1000 <= Number(document.forms[0].MUSHIMEGANE_KIZAMI.value) || Number(document.forms[0].MUSHIMEGANE_KIZAMI.value) < 0) {
        alert('{rval MSG901}\n( 虫眼鏡 きざみ点 )');
        return;
    }

    //終了-開始が刻み値以上
    if (Number(document.forms[0].MUSHIMEGANE_E.value) - Number(document.forms[0].MUSHIMEGANE_S.value) < Number(document.forms[0].MUSHIMEGANE_KIZAMI.value)) {
        alert('虫眼鏡 刻みが開始～終了の範囲よりも大きいです。');
        return;
    }

    //小数点の有無チェック(虫眼鏡 きざみ点)
    if (document.forms[0].MUSHIMEGANE_KIZAMI.value.match(/^-?[0-9]+$/) || document.forms[0].MUSHIMEGANE_KIZAMI.value.match(/^-?[0-9]+\.[0-9]+$/)) {
        //小数点チェック(虫眼鏡 きざみ点)
        if (String(document.forms[0].MUSHIMEGANE_KIZAMI.value).indexOf(".") >= 0
            && String(document.forms[0].MUSHIMEGANE_KIZAMI.value).split(".")[1].length > 1) {
            alert('小数点第一位まで\n( 虫眼鏡 きざみ点)');
            return;
        }
        if (parseFloat(document.forms[0].MUSHIMEGANE_KIZAMI.value) < 0.0) {
            alert('{rval MSG901}\n0より大\n( 虫眼鏡 きざみ点 )');
            return;
        }
    } else {
        //空文字は虫眼鏡未使用のパターンとなるので、それ以外をエラーにする。
        //また、範囲が入っているのに刻みが入ってないのはエラーにする。
        if (document.forms[0].MUSHIMEGANE_KIZAMI.value != ""
            || (document.forms[0].MUSHIMEGANE_KIZAMI.value == ""
                && (document.forms[0].MUSHIMEGANE_S.value != "" || document.forms[0].MUSHIMEGANE_E.value != ""))) {
            alert('{rval MSG901}\n( 虫眼鏡 きざみ点 )');
            return;
        }

    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    // url = location.hostname;
    // document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
