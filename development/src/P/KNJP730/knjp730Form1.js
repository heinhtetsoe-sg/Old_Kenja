function btn_submit(cmd) {
    if (cmd == "update") {
        var checkFlg = false;
        re = new RegExp("CHECK");
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            if (obj_updElement.name.match(re) && obj_updElement.checked == true) {
                checkFlg = true;
            }
        }
        if (!checkFlg) {
            alert("最低ひとつチェックを入れてください。");
            return false;
        }
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    //読込中は、更新・取消ボタンをグレーアウト
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_reset.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//（テキストボックス⇔表示）変換
function changeDispText(obj) {
    var slipNo = obj.name.split("-")[1];
    var lCd = obj.name.split("-")[2];
    var mCd = obj.name.split("-")[3];
    var monthArr = new Array();
    monthArr = document.forms[0].MONTH_ARR.value.split(",");

    var setId = slipNo + "-" + lCd + "-" + mCd + "-";
    //納期限チェック用のID(各伝票の各月合計と各伝票の入金を比べる為)
    var setLimitDateId = slipNo + "-101-101-";

    var planMonthMoney = "";
    var paidMonthMoney = "";

    var isGenmen = false;
    if (lCd.indexOf("GEN") != -1) {
        isGenmen = true;
    }
    for (var i = 0; i < monthArr.length; i++) {
        if (lCd != "102" && !isGenmen) {
            planMonthMoney = document.forms[0]["COLLECT_MONTH_" + setId + monthArr[i]].value;
            paidMonthMoney = document.forms[0]["COLLECT_MONTH_" + setId + monthArr[i] + "-2"].value;
        }

        if (obj.checked == true) {
            //納期限チェックボックスの時
            if (lCd == "102") {
                if (document.forms[0]["COLLECT_MONTH_" + setLimitDateId + monthArr[i]].value != document.forms[0]["PAID-" + setId + monthArr[i]].value) {
                    document.getElementById("changeNonText-" + setId + monthArr[i]).style.display = "none";
                    document.getElementById("changeText-" + setId + monthArr[i]).style.display = "inline";
                    document.getElementById("changeNonCmb-" + setId + monthArr[i]).style.display = "none";
                    document.getElementById("changeCmb-" + setId + monthArr[i]).style.display = "inline";
                }

                //各項目の（計画 != 入金額）時
            } else if (isGenmen || planMonthMoney == "0" || planMonthMoney != paidMonthMoney) {
                //(伝票各月合計 != 各月伝票入金)の時。→計画が"0"の時テキストボックスに切り替わっていたため
                if (document.forms[0]["COLLECT_MONTH_" + setLimitDateId + monthArr[i]].value != document.forms[0]["PAID-" + slipNo + "-102-102-" + monthArr[i]].value) {
                    document.getElementById("changeNonText-" + setId + monthArr[i]).style.display = "none";
                    document.getElementById("changeText-" + setId + monthArr[i]).style.display = "inline";
                }
            }
        } else {
            document.getElementById("changeNonText-" + setId + monthArr[i]).style.display = "inline";
            document.getElementById("changeText-" + setId + monthArr[i]).style.display = "none";
            if (lCd == "102") {
                document.getElementById("changeNonCmb-" + setId + monthArr[i]).style.display = "inline";
                document.getElementById("changeCmb-" + setId + monthArr[i]).style.display = "none";
            }
        }
    }
    return;
}

//印刷
function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
