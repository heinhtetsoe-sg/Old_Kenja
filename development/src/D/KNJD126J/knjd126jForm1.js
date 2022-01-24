window.onload = init;
function init() {
    //ウィンドウを開いたら呼ばれる関数
    switchDisabled(); //ラジオボタンを表示したり隠したり
}

function btn_submit(cmd, electdiv) {
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) return false;
    } else if (cmd == "update") {
        var kantenHyouji_5 = document.forms[0].kantenHyouji_5.value;
        var kantenHyouji_6 = document.forms[0].kantenHyouji_6.value;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "text" && e.value != "") {
                var str = e.value;
                var nam = e.name;

                if (document.forms[0].HENKAN_TYPE.value == "1") {
                    //英小文字から大文字へ自動変換
                    e.value = str.toUpperCase();
                    str = str.toUpperCase();
                } else if (document.forms[0].HENKAN_TYPE.value == "2") {
                    //英大文字から小文字へ自動変換
                    e.value = str.toLowerCase();
                    str = str.toLowerCase();
                }

                //評定
                if (nam.match(/STATUS9./)) {
                    var word = "評定";
                    if (document.forms[0].useHyoukaHyouteiFlg.value == "1") {
                        word = "評価";
                    } else if (document.forms[0].useHyoukaHyouteiFlg.value == "2") {
                        word = "仮評定";
                    }

                    counter = nam.replace("STATUS9-", "");
                    maxval = document.forms[0]["CHECK_MAXVAL_" + counter].value;

                    if (electdiv == "0") {
                        //数字チェック
                        var newString = "";
                        var count = 0;
                        for (j = 0; j < str.length; j++) {
                            ch = str.substring(j, j + 1);
                            if (ch >= "0" && ch <= "9") {
                                newString += ch;
                            }
                        }
                        if (str != newString) {
                            alert("{rval MSG907}");
                            return;
                        }
                    }
                    if (document.forms[0].useRecordDat.value == "KIN_RECORD_DAT") {
                        if (parseInt(str) < 1 || parseInt(str) > parseInt(maxval)) {
                            alert("{rval MSG901}" + document.forms[0].errMsg.value + "\n（" + word + "）");
                            return;
                        }
                    } else {
                        if (electdiv == "0") {
                            if (document.forms[0].useHyoukaHyouteiFlg.value == "1" && str && (parseInt(str) < 0 || parseInt(str) > 100)) {
                                alert("{rval MSG901}" + "\n（" + word + "）");
                                return;
                            } else if (document.forms[0].useHyoukaHyouteiFlg.value != "1" && (parseInt(str) < 1 || parseInt(str) > parseInt(maxval))) {
                                alert("{rval MSG901}" + document.forms[0].errMsg.value + "\n（" + word + "）");
                                return;
                            }
                        } else if (electdiv != "0" && !str.match(/A|B|C/)) {
                            alert("{rval MSG901}" + "「AまたはBまたはC」を入力して下さい。\n（" + word + "）");
                            return;
                        }
                    }
                    //観点1～5または6
                } else {
                    var checkStr = "/" + document.forms[0].SETSHOW.value.replace(/,/g, "|") + "/";
                    var errStr = document.forms[0].SETSHOW.value.replace(/,/g, "、");
                    if (!str.match(eval(checkStr))) {
                        if (kantenHyouji_5 == 1) {
                            alert("{rval MSG901}" + "「" + errStr + "」を入力して下さい。\n（観点①～⑤）");
                        } else {
                            alert("{rval MSG901}" + "「" + errStr + "」を入力して下さい。\n（観点①～⑥）");
                        }
                        return;
                    }
                }
            }
        }
        clickedBtnUdpate(true);
    }
    //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == "update") {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新時、サブミットする項目使用不可
function clickedBtnUdpate(disFlg) {
    var sk_flg = false;
    if (document.forms[0].use_prg_schoolkind.value == "1" && (document.forms[0].SCHOOL_KIND.type != "hidden" || document.forms[0].SCHOOL_KIND_CNT.value >= 1)) {
        sk_flg = true;
    }

    if (disFlg) {
        if (sk_flg) document.forms[0].H_SCHOOL_KIND.value = document.forms[0].SCHOOL_KIND.value;
        document.forms[0].H_SEMESTER.value = document.forms[0].SEMESTER.value;
        document.forms[0].H_CLASSCD.value = document.forms[0].CLASSCD.value;
        document.forms[0].H_CHAIRCD.value = document.forms[0].CHAIRCD.value;
    } else {
        if (sk_flg) document.forms[0].SCHOOL_KIND.value = document.forms[0].H_SCHOOL_KIND.value;
        document.forms[0].SEMESTER.value = document.forms[0].H_SEMESTER.value;
        document.forms[0].CLASSCD.value = document.forms[0].H_CLASSCD.value;
        document.forms[0].CHAIRCD.value = document.forms[0].H_CHAIRCD.value;
    }
    if (sk_flg) document.forms[0].SCHOOL_KIND.disabled = disFlg;
    document.forms[0].SEMESTER.disabled = disFlg;
    document.forms[0].CLASSCD.disabled = disFlg;
    document.forms[0].CHAIRCD.disabled = disFlg;
    document.forms[0].SELECT[0].disabled = disFlg;
    document.forms[0].SELECT[1].disabled = disFlg;
    document.forms[0].btn_reset.disabled = disFlg;
    document.forms[0].btn_back.disabled = disFlg;
    document.forms[0].btn_print.disabled = disFlg;
}

function background_color(obj) {
    //    obj.style.background='#ffff99';
    obj.style.background = "#ffffff";
}
//入力チェック
function calc(obj, electdiv, grade) {
    var str = obj.value;
    var nam = obj.name;
    var maxValue = document.forms[0]["MAXVALUE_" + grade].value;
    //空欄
    if (str == "") {
        return;
    }

    if (document.forms[0].HENKAN_TYPE.value == "1") {
        //英小文字から大文字へ自動変換
        obj.value = str.toUpperCase();
        str = str.toUpperCase();
    } else if (document.forms[0].HENKAN_TYPE.value == "2") {
        //英大文字から小文字へ自動変換
        obj.value = str.toLowerCase();
        str = str.toLowerCase();
    }
    //評定
    if (nam.match(/STATUS9./)) {
        //選択科目
        var myregex = new RegExp(document.forms[0].SETSHOW.value.replace(/,/g, "|"));
        if (electdiv != "0" && !str.match(myregex)) {
            var errStr = document.forms[0].SETSHOW.value.replace(/,/g, "、");
            alert("{rval MSG901}" + "「" + errStr + "」を入力して下さい。");
            setTimeout(function () {
                obj.focus();
            }, 10);
            return;
        } else if (electdiv == "0") {
            var word = "評定";
            if (document.forms[0].useHyoukaHyouteiFlg.value == "1") {
                word = "評価";
            } else if (document.forms[0].useHyoukaHyouteiFlg.value == "2") {
                word = "仮評定";
            }

            //数字チェック
            var newString = "";
            var count = 0;
            for (j = 0; j < str.length; j++) {
                ch = str.substring(j, j + 1);
                if (ch >= "0" && ch <= "9") {
                    newString += ch;
                }
            }
            if (str != newString) {
                alert("{rval MSG907}");
                setTimeout(function () {
                    obj.focus();
                }, 10);
                return;
            }

            if (document.forms[0].useRecordDat.value == "KIN_RECORD_DAT") {
                if (parseInt(str) < 1 || parseInt(str) > parseInt(maxValue)) {
                    alert("{rval MSG901}" + "「1～" + maxValue + "」を入力して下さい。\n（" + word + "）");
                    setTimeout(function () {
                        obj.focus();
                    }, 10);
                    return;
                }
            } else {
                if (document.forms[0].useHyoukaHyouteiFlg.value == "1" && str != "" && (parseInt(str) < 0 || parseInt(str) > 100)) {
                    alert("{rval MSG901}" + "\n（" + word + "）");
                    setTimeout(function () {
                        obj.focus();
                    }, 10);
                    return;
                } else if (document.forms[0].useHyoukaHyouteiFlg.value != "1" && str != "" && (parseInt(str) < 1 || parseInt(str) > maxValue)) {
                    alert("{rval MSG901}" + "「1～" + maxValue + "」を入力して下さい。\n（" + word + "）");
                    setTimeout(function () {
                        obj.focus();
                    }, 10);
                    return;
                }
            }
        }
        //観点1～5または6
    } else {
        var checkStr = "/" + document.forms[0].SETSHOW.value.replace(/,/g, "|") + "/";
        var errStr = document.forms[0].SETSHOW.value.replace(/,/g, "、");
        if (!str.match(eval(checkStr))) {
            alert("{rval MSG901}" + "「" + errStr + "」を入力して下さい。");
            setTimeout(function () {
                obj.focus();
            }, 10);
            return;
        }
    }
}
//印刷
function newwin(SERVLET_URL) {
    if (document.forms[0].CHAIRCD.value == "") {
        alert("クラス・講座を指定してください。");
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//観点①～⑤または⑥へマウスを乗せた場合、観点名称をチップヘルプで表示
function ViewcdMousein(e, msg_no) {
    var msg = "";
    if (msg_no == 1) msg = document.forms[0].VIEWCD1.value;
    if (msg_no == 2) msg = document.forms[0].VIEWCD2.value;
    if (msg_no == 3) msg = document.forms[0].VIEWCD3.value;
    if (msg_no == 4) msg = document.forms[0].VIEWCD4.value;
    if (msg_no == 5) msg = document.forms[0].VIEWCD5.value;
    if (msg_no == 6) msg = document.forms[0].VIEWCD6.value;

    x = event.clientX + document.body.scrollLeft;
    y = event.clientY + document.body.scrollTop;
    document.getElementById("lay").innerHTML = msg;
    document.getElementById("lay").style.position = "absolute";
    document.getElementById("lay").style.left = x + 5;
    document.getElementById("lay").style.top = y + 10;
    document.getElementById("lay").style.padding = "4px 3px 3px 8px";
    document.getElementById("lay").style.border = "1px solid";
    document.getElementById("lay").style.visibility = "visible";
    document.getElementById("lay").style.background = "#ccffff";
    //document.getElementById("lay").style.cursor = "hand";
}

function ViewcdMouseout() {
    document.getElementById("lay").style.visibility = "hidden";
}

function kirikae(obj, showName, counter) {
    var prevData = document.forms[0].elements[showName].value;
    setValue(obj, showName, document.forms[0].NYURYOKU[1].checked);
    if (prevData != document.forms[0].elements[showName].value) {
        document.forms[0].elements["STATUS9-" + counter].value = "";
    }
}

function kirikae2(obj, showName) {
    event.cancelBubble = true;
    event.returnValue = false;
    clickList(obj, showName);
}

//値をセット
function setValue(obj, showName, clearCheck) {
    if (clearCheck) {
        obj.value = "";
    } else {
        innerName = showName;
        typeValArray = document.forms[0].SETVAL.value.split(",");
        typeShowArray = document.forms[0].SETSHOW.value.split(",");

        var type_div_cnt = 0;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name.match(/TYPE_DIV/)) {
                type_div_cnt++;
            }
        }
        if (type_div_cnt > 1) {
            for (var i = 0; i < type_div_cnt; i++) {
                typeDiv = document.forms[0].TYPE_DIV[i];
                if (typeDiv.checked) {
                    obj.value = typeShowArray[typeDiv.value - 1];
                }
            }
        } else {
            typeDiv = document.forms[0].TYPE_DIV;
            if (typeDiv.checked) {
                obj.value = typeShowArray[typeDiv.value - 1];
            }
        }
    }
}

function clickList(obj, showName) {
    innerName = showName;

    setObj = obj;
    myObj = document.getElementById("myID_Menu").style;
    myObj.left = window.event.clientX + document.body.scrollLeft + "px";
    myObj.top = window.event.clientY + document.body.scrollTop + "px";
    myObj.visibility = "visible";
}

function myHidden() {
    document.getElementById("myID_Menu").style.visibility = "hidden";
    switchDisabled();
}

function setClickValue(val) {
    if (val != "999") {
        typeShowArray = document.forms[0].SETSHOW.value.split(",");
        if (setObj.value != typeShowArray[val - 1]) {
            document.forms[0].elements["STATUS9-" + setObj.name.split("-")[1]].value = "";
        }
        setObj.value = typeShowArray[val - 1];
        typeValArray = document.forms[0].SETVAL.value.split(",");
    }
    myHidden();
    setObj.focus();
}

//disabled（入力方法の値）
function switchDisabled() {
    obj = document.getElementById("NYURYOKU1");
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/TYPE_DIV/)) {
            document.forms[0].elements[i].disabled = !obj.checked;
        }
    }
}

function close_window1() {
    alert("名称マスタD028の登録をして下さい。");
    closeWin();
}

function close_window2() {
    alert("名称マスタD029の登録をして下さい。");
    closeWin();
}
