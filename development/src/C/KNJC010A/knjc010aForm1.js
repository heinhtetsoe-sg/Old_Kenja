function btn_submit(cmd) {
    if (cmd == "reset" && !confirm("{rval MSG106}")) {
        return;
    }
    updateFrameLock();
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changePeriodChair(obj) {
    var periodCd = obj.value.split(":")[0];
    var chairCd = obj.value.split(":")[1];

    // document.forms[0].SCH_CHR_EXECUTEDATE.value = document.forms[0].SCH_CHR_EXECUTEDATE.value;
    document.forms[0].SCH_CHR_PERIODCD.value = periodCd;
    document.forms[0].SCH_CHR_CHAIRCD.value = chairCd;

    updateFrameLock();
    // var lockScreen = document.getElementById('lockScreen');
    // if (lockScreen) {
    //     lockScreen.style.display = "table";
    // }
    btn_submit("schChrSelect");
}

function closeMethod() {
    if (window.opener) {
        window.opener.btn_submit("main");
    }
    closeWin();
}

function scrollRC() {
    document.getElementById("trow").scrollLeft = document.getElementById(
        "tbody"
    ).scrollLeft;
    document.getElementById("tcol").scrollTop = document.getElementById(
        "tbody"
    ).scrollTop;
}

function chengeData(clickData) {
    if (clickData == "HR") {
        document.forms[0].HR_CLASS.disabled = false;
        document.forms[0].STAFF.disabled = true;
    } else {
        document.forms[0].HR_CLASS.disabled = true;
        document.forms[0].STAFF.disabled = false;
    }
}

function dropUnitDate(obj, taisyou) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        re = new RegExp("^CHAIR_" + taisyou);
        var obj_updElement = document.forms[0].elements[i];
        if (obj_updElement.name.match(re)) {
            var setTaisyou = taisyou + obj_updElement.name.substr(re.lastIndex);
            var setColor = obj.checked ? "#cccccc" : "#ccffcc";
            setChangeColor(setTaisyou, "dateAll", setColor);
        }
    }
}

function dropUnit(taisyou) {
    var unitName = "UNIT_" + taisyou;
    var bunkatuName = "BUNKATU_" + taisyou;
    var remarkName = "REMARK_" + taisyou;
    document.getElementById(unitName).innerHTML = "&nbsp;";
    document.getElementById(bunkatuName).innerHTML = "&nbsp;";
    document.forms[0][remarkName].value = "";
    setChangeColor(taisyou, "drop", "#ccffcc");
}

function setChangeColor(taisyou, div, setColor) {
    var idName = "ID_" + taisyou;
    document.getElementById(idName).bgColor = setColor;

    if (div == "text") {
        setChangeText(taisyou, div);
    }
}
function setChangeText(taisyou, div) {
    var sep = "";
    if (document.forms[0].changeVal.value != "") {
        sep = ":";
    }
    document.forms[0].changeVal.value += sep + taisyou;
}
var xmlhttp = null;

/* XMLHttpRequest生成 */
function createXmlHttp() {
    if (document.all) {
        return new ActiveXObject("Microsoft.XMLHTTP");
    } else if (document.implementation) {
        return new XMLHttpRequest();
    } else {
        return null;
    }
}

/* POSTによるデータ送信 */
function chgDataDisp(cmd) {
    /* XMLHttpRequestオブジェクト作成 */
    if (xmlhttp == null) {
        xmlhttp = createXmlHttp();
    } else {
        /* 既に作成されている場合、通信をキャンセル */
        xmlhttp.abort();
    }
    /* 入力フォームデータの処理 */
    var postdata = new String();
    postdata = "cmd=" + cmd;
    if (cmd == "sendKintai") {
        postdata += "&HIDDEN_SCHREG=" + document.forms[0].HIDDEN_SCHREG.value;
        postdata +=
            "&TITLE_DISP_SEIGYO=" + document.forms[0].TITLE_DISP_SEIGYO.value;
    } else if (cmd == "sendChair") {
        postdata += "&HIDDEN_SCHREG=" + document.forms[0].HIDDEN_SCHREG.value;
        postdata +=
            "&SELECT_SCHREGNO=" + document.forms[0].SELECT_SCHREGNO.value;
    }
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function () {
        handleHttpEvent(cmd);
    }; //こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjc010aindex.php", true);
    xmlhttp.setRequestHeader(
        "Content-Type",
        "application/x-www-form-urlencoded"
    );
    xmlhttp.send(postdata);
}

/* レスポンスデータ処理 */
function handleHttpEvent(cmd) {
    if (xmlhttp.readyState == 4) {
        if (xmlhttp.status == 200) {
            var json = xmlhttp.responseText;
            var response;
            eval("response = " + json); //JSON形式のデータ(オブジェクトとして扱える)
            //戻り値
            if (response.result) {
                if (cmd == "sendKintai" || cmd == "sendChair") {
                    setKintai(response);
                }
            }
        } else {
        }
    }
}

/* 勤怠 */
function setKintai(response) {
    for (var key in response) {
        if (key != "result") {
            var data = response[key];
            document.getElementById(key).innerHTML = data;
        }
    }
}

var xmlhttp2 = null;
/* POSTによるデータ送信 */
function chgDataDisp2(selectPeri) {
    var flg = document.getElementsByName("showAttendInfo");
    if (!flg || !flg[0] || flg[0].value != "1") {
        return;
    }

    /* XMLHttpRequestオブジェクト作成 */
    if (xmlhttp2 == null) {
        xmlhttp2 = createXmlHttp();
    } else {
        /* 既に作成されている場合、通信をキャンセル */
        xmlhttp2.abort();
    }
    /* 入力フォームデータの処理 */
    var cmd = "sendKintaiInput";
    var schregno = document.forms[0].SELECT_SCHREGNO.value;
    var postdata;
    postdata = "cmd=" + cmd;
    postdata += "&SELECT_SCHREGNO=" + schregno;
    postdata += "&SELECT_DATE=" + document.forms[0].hid_syoribi.value;
    postdata += "&SELECT_PERIODCD=" + selectPeri;
    var setVal = function (divid, val) {
        var d = document.getElementById(divid);
        d.innerHTML = val || "";
    };
    var setResponse = function (json) {
        setVal("INPUT_CHAIR_NAME", json["INPUT_CHAIR_NAME"]);
        setVal(
            "INPUT_CHAIR_STAFF",
            "【" + (json["INPUT_CHAIR_STAFF"] || "") + "】"
        );
        setVal(
            "INPUT_CHAIR_CREDIT",
            "【" + (json["INPUT_CHAIR_CREDIT"] || "") + "】"
        );
        setVal("INPUT_UPDATE_STAFF", json["INPUT_UPDATE_STAFF"]);
        setVal(
            "INPUT_UPDATE_TIME",
            json["INPUT_UPDATE_TIME"]
                ? "（" + json["INPUT_UPDATE_TIME"] + "）"
                : ""
        );
    };
    /* レスポンスデータ処理方法の設定 */
    xmlhttp2.onreadystatechange = function () {
        if (xmlhttp2.readyState == 4) {
            if (xmlhttp2.status == 200) {
                var txt, response;
                try {
                    txt = xmlhttp2.responseText;
                    response = JSON.parse(txt); //JSON形式のデータ(オブジェクトとして扱える)
                    setResponse(response);
                } catch (e) {
                    console.error(e, txt);
                }
            }
        }
    };
    /* HTTPリクエスト実行 */
    xmlhttp2.open("POST", "knjc010aindex.php", true);
    xmlhttp2.setRequestHeader(
        "Content-Type",
        "application/x-www-form-urlencoded"
    );
    xmlhttp2.send(postdata);
}

var selectedRow = 0;
function selectRow(selectPeri, selectObj, selectId) {
    if (
        document.forms[0].BEF_ID.value != "" &&
        document.forms[0].BEF_COLOR.value != ""
    ) {
        var setId = document.forms[0].BEF_ID.value;
        document.getElementById(setId).bgColor =
            document.forms[0].BEF_COLOR.value == "white"
                ? ""
                : document.forms[0].BEF_COLOR.value;
    }
    document.forms[0].BEF_ID.value = selectId;
    document.forms[0].BEF_COLOR.value =
        selectObj.bgColor == "" ? "white" : selectObj.bgColor;

    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }
    list.rows[selectedRow].bgColor = "white";
    schList.rows[selectedRow].bgColor = "white";
    selectedRow = event.srcElement.parentElement.rowIndex;
    list.rows[selectedRow].bgColor = "#ff88aa";
    schList.rows[selectedRow].bgColor = "#ff88aa";

    var chk = document.forms[0]["SCHREGROWS[]"];
    var chkData =
        chk[selectedRow] === undefined ? chk.value : chk[selectedRow].value;
    document.forms[0].SELECT_SCHREGNO.value = chkData;
    chgDataDisp("sendChair");
    chgDataDisp2(selectPeri);
    selectObj.bgColor = "red";
}

function inPutExe(obj, showName) {
    if (document.forms[0].INPUT_TYPE[0].checked) {
        myIdlist(obj, showName);
    } else {
        clickDi(obj, showName);
    }
}

function myIdlist(obj, showName) {
    if (event.preventDefault) {
        event.preventDefault();
    }
    event.cancelBubble = true;
    event.returnValue = false;
    clickList(obj, showName);
}

function clickList(obj, showName) {
    innerName = showName;
    if (event.preventDefault) {
        myObj = document.getElementById("myID_Menu").style;
    } else {
        myObj = document.forms[0].all["myID_Menu"].style;
    }
    myObj.left = window.event.clientX + document.body.scrollLeft + "px";
    myObj.top = window.event.clientY + document.body.scrollTop + "px";
    myObj.visibility = "visible";
}

function myHidden() {
    document.all["myID_Menu"].style.visibility = "hidden";
}

function clickDi(obj, showName) {
    event.cancelBubble = true;
    event.returnValue = false;
    innerName = showName;
    document.forms[0]["CLICK" + showName].value =
        parseInt(document.forms[0]["CLICK" + showName].value) + 1;
    typeValArray = document.forms[0].SETVAL.value.split(",");
    var isListVal = false;
    for (var key in typeValArray) {
        if (key == document.forms[0]["CLICK" + showName].value) {
            isListVal = true;
            break;
        }
    }
    document.forms[0]["CLICK" + showName].value = isListVal
        ? document.forms[0]["CLICK" + showName].value
        : 0;
    setClickValue(document.forms[0]["CLICK" + showName].value);
}

function setClickValue(val) {
    if (val != "999") {
        typeItinitiArray = document.forms[0].ITINITI.value.split(",");
        if (val == "888") {
            schKey = innerName.split("_");

            re = new RegExp(schKey[0]);
            schregPeri = document.forms[0].HIDDEN_SCHREG.value.split(",");
            var setHiddenSch = "";
            var setSep = "";
            for (var key in schregPeri) {
                schregPeriVal = String(schregPeri[key]);
                if (schregPeriVal.match(re)) {
                    setHiddenSch = setHiddenSch + setSep + schregPeriVal;
                    setSep = ",";
                }
            }

            var hideenSchChange = document.forms[0].HIDDEN_SCHREG.value
                .split(",")
                .join("|");
            var url = "knjc010aindex.php?cmd=syousai";
            url += "&SYOUSAI_SCHREGNO=" + schKey[0];
            url += "&HIDDEN_SCHREG=" + setHiddenSch;
            url +=
                "&SEND_RENZOKU=" +
                document.forms[0]["sendRenzoku_" + schKey[0]].value;
            loadwindow(url, 0, 0, 560, 560);
        } else if (typeItinitiArray[val] == "999") {
            typeShowArray = document.forms[0].SETSHOW.value.split(",");
            document.getElementById("SCH" + innerName).innerHTML =
                typeShowArray[val];
            document.getElementById("SCH" + innerName).style.color = "#000000";
            typeValArray = document.forms[0].SETVAL.value.split(",");
            document.forms[0]["UPD" + innerName].value = typeValArray[val];
        } else {
            schKey = innerName.split("_");
            re = new RegExp(schKey[0]);
            schregPeri = document.forms[0].HIDDEN_SCHREG.value.split(",");
            for (var key in schregPeri) {
                schregPeriVal = String(schregPeri[key]);
                if (schregPeriVal.match(re)) {
                    hiddenSchKey = schregPeriVal.split("_");
                    typeShowArray = document.forms[0].SETSHOW.value.split(",");
                    var checkColor = document.getElementById(
                        "SCH" + schKey[0] + "_" + hiddenSchKey[1]
                    ).bgColor;
                    if (checkColor != "#aaaaaa" && checkColor != "#AAAAAA") {
                        document.getElementById(
                            "SCH" + schKey[0] + "_" + hiddenSchKey[1]
                        ).innerHTML = typeShowArray[val];
                        document.getElementById(
                            "SCH" + schKey[0] + "_" + hiddenSchKey[1]
                        ).style.color = "#000000";
                        typeValArray = document.forms[0].SETVAL.value.split(
                            ","
                        );
                        document.forms[0][
                            "UPD" + schKey[0] + "_" + hiddenSchKey[1]
                        ].value = typeValArray[val];
                    }
                }
            }
        }
    }
    myHidden();
}

//データ入力項目の説明をチップヘルプで表示
function ViewcdMousein(msg) {
    x = event.clientX + document.body.scrollLeft;
    y = event.clientY + document.body.scrollTop;
    document.all("lay").innerHTML = msg;
    document.all["lay"].style.position = "absolute";
    document.all["lay"].style.left = x + 5;
    document.all["lay"].style.top = y + 10;
    document.all["lay"].style.padding = "4px 3px 3px 8px";
    document.all["lay"].style.border = "1px solid";
    document.all["lay"].style.visibility = "visible";
    document.all["lay"].style.background = "#ccffff";
}

function ViewcdMouseout() {
    document.all["lay"].style.visibility = "hidden";
}

function setAllOk(periodCd) {
    re = new RegExp("_" + periodCd);
    schregPeri = document.forms[0].HIDDEN_SCHREG.value.split(",");
    for (var key in schregPeri) {
        schregPeriVal = String(schregPeri[key]);
        if (schregPeriVal.match(re)) {
            hiddenSchKey = schregPeriVal.split("_");
            schKey = hiddenSchKey[0].replace("SCH", "");
            document.getElementById("SCH" + schKey + "_" + periodCd).innerHTML =
                "";
            document.getElementById(
                "SCH" + schKey + "_" + periodCd
            ).style.color = "#000000";
            typeValArray = document.forms[0].SETVAL.value.split(",");
            document.forms[0]["UPD" + schKey + "_" + periodCd].value = "";
        }
    }
}

function setPetition() {
    var ps;
    var e;
    var knjc030aAllInputFlg = document.getElementsByName(
        "knjc030aAllInputFlg"
    )[0].value;
    var renzoku;
    for (schKey in petitionJson) {
        renzoku = document
            .getElementsByName("sendRenzoku_" + schKey)[0]
            .value.split(",");
        ps = petitionJson[schKey] || {};
        for (p in ps) {
            e = document.getElementById("SCH" + schKey + "_" + p);
            if (e) {
                e.style.color = "#ff0000";
            }
            if (
                renzoku.some(function (rp) {
                    return rp == p;
                }) ||
                knjc030aAllInputFlg
            ) {
                e = document.getElementsByName("UPD" + schKey + "_" + p)[0];
                if (e) {
                    e.value = ps[p];
                }
                console.log(schKey, renzoku, e);
            }
        }
    }
}

function wopenChair(url, param) {
    wopen(
        url + param,
        "SUBWIN_KNJB3042",
        0,
        0,
        screen.availWidth,
        screen.availHeight
    );
}

function loadwindowJugyouNaiyouAdd(syoribi) {
    var befId = document.forms[0].BEF_ID.value;
    var befIdData = befId.split("_");
    if (befIdData == "") {
        alert("{rval MSG304}");
        return false;
    } else if (document.forms[0]["DISABLED" + befId.slice(3)]) {
        alert("{rval MSG303}");
        return false;
    }

    var url = "knjc010aindex.php?cmd=jugyouNaiyouAdd";
    url += "&JUGYOU_NAIYOU_SYORIBI=" + syoribi;
    url += "&JUGYOU_NAIYOU_SCHREGNO=" + befIdData[0].slice(3);
    url += "&JUGYOU_NAIYOU_PERIODCD=" + befIdData[1];
    loadwindow(url, 200, 200, 800, 200);
}

function loadwindowSchChrList(syoribi) {
    var url = "knjc010aindex.php?cmd=schChrList";
    url += "&SCH_CHR_EXECUTEDATE=" + syoribi;

    loadwindow(url, 200, 100, 800, 800);
}

var petitionJson;
window.addEventListener("load", function () {
    petitionJson = JSON.parse(
        document.getElementsByName("petitionJson")[0].value
    );
    setPetition();
});

function periodHeadClick(period) {
    event = window.event;
    var scrollX =
        document.documentElement.scrollLeft || document.body.scrollLeft;
    var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
    var X = scrollX + event.clientX + 5;
    var Y = scrollY + event.clientY + 5;

    var url = "knjc010aindex.php?cmd=popupInfo";
    url += "&POPUPINFO_PERIOD=" + period;
    url +=
        "&POPUPINFO_EXECUTEDATE=" + document.forms[0].SCH_CHR_EXECUTEDATE.value;
    url += "&POPUPINFO_CHAIRCD=" + document.forms[0].SCH_CHR_CHAIRCD.value;

    loadwindow(url, X, Y, 200, 400);
}
