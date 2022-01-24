//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var chk = document.forms[0]['CHECK\[\]'];
    var sep1 = sep2 = "";
    var Ch_txt1 = "";
    var parentFrame = this.parent.document;
    var existChecked = false;
    var targetName = document.forms[0].TRAINREF_TARGET.value;
    var keta = document.forms[0].TOTALREMARK_KETA.value;
    var gyo = document.forms[0].TOTALREMARK_GYO.value;
    if (document.forms[0].TORIKOMI_MULTI.value != "1") {
        var targetObj = parentFrame.forms[0][targetName];

        if (datacnt === "1") {
            if (chk.checked) {
                Ch_txt1 = chk.value;
                existChecked = true;
            }
        } else {
            sep1    = "";
            for (var i=0; i < datacnt; i++)
            {
                if (chk[i].checked) {
                    Ch_txt1 = Ch_txt1 + sep1 + chk[i].value;
                    sep1    = "\n";
                    existChecked = true;
                }
            }
        }

        if (!existChecked) {
            parent.closeit();
            return false;
        }

        if (targetObj) {
            if (targetObj.value != "") {
                sep2 = "\n";
            }
            var dispRemark = targetObj.value + sep2 + Ch_txt1;
            if (gyo && keta) {
                console.log(gyo);
                charCount(dispRemark, gyo, keta, true);
            }
            targetObj.value = dispRemark;
            targetObj.style.backgroundColor = "#FFCCFF";
        }
    } else {
        var torikomi = [];
        var target = {};
        try {
            target = JSON.parse(targetName);
            for (var src in target) {
                if (!target.hasOwnProperty(src)) {
                    continue;
                }
                torikomi.push({"SRC": src, "TARGET": target[src]});
            }
        } catch (e) {
        }
        for (var i = 0; i < torikomi.length; i++) {
            var srcid = "CHECK_" + torikomi[i]["SRC"];
            var srcelem = document.getElementById(srcid);
            var targetObj = parentFrame.forms[0][torikomi[i]["TARGET"]];
            sep2 = "";
            if (srcelem && srcelem.checked && targetObj) {
                Ch_txt1 = srcelem.value;

                if (targetObj.value != "") {
                    sep2 = "\n";
                }
                var dispRemark = targetObj.value + sep2 + Ch_txt1;
                if (gyo && keta) {
                    console.log(gyo);
                    charCount(dispRemark, gyo, keta, true);
                }
                targetObj.value = dispRemark;
                targetObj.style.backgroundColor = "#FFCCFF";

            }
        }
    }

    parent.closeit();
}
function btn_submitYear(cmd) { //年度コンボ変更時のサブミット

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function charCount(val, gyosu, itigyou_no_mojiLen, dispMsg)
{
    //改行コードで区切って配列に入れていく
    stringArray = new Array();
    stringArray = val.split("\n");

    row_cnt = 0;
    gyousu = 1;
    //改行コードが現れるまでに何行消費するか数える
    for (var i = 0; i < stringArray.length; i++) {
        mojisu = stringArray[i].length;
        mojiLen = 0;
        for (var j = 0; j < mojisu; j++) {
            hitoMoji = stringArray[i].charAt(j);
            moji_hantei = escape(hitoMoji).substr(0,2);
            mojiLen += moji_hantei == "%u" ? 2 : 1;
        }
        amari = mojiLen % itigyou_no_mojiLen;
        gyousu = (mojiLen - amari) / itigyou_no_mojiLen;
        if (amari > 0) {
            gyousu++;
        }
        if (gyousu) {
            row_cnt += gyousu;
        } else {
            row_cnt++;
        }
    }
    var retArray = Array();
    retArray["GYOUSU"] = row_cnt;
    if (row_cnt > gyosu) {
        if (dispMsg) {
            alert('行数を超えています。'+gyosu+'行以内で入力して下さい。');
        }
        retArray["FLG"] = false;
        return retArray;
    }
    retArray["FLG"] = true;
    return retArray;
}