//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;

    var chk = document.forms[0]["CHECK[]"];
    var chk_fuku = document.forms[0]["CHECK_FUKU[]"];
    if (document.forms[0].useChairStaffOrder.value == "1") {
        var stfOrder = document.forms[0]["STAFF_ORDER[]"];
    }
    var sep = (sep1 = sep2 = "");
    var Ch_val = ""; //職員コード
    var Ch_val1 = ""; //担任区分
    var Ch_txt = ""; //副担任名
    var Ch_txt1 = ""; //正担任名
    var Ch_val2 = ""; //職員コード＋'-'＋担任区分
    var setOrder = ""; //ソート
    var setStfOrder = ""; //ソート
    var kubun = "";

    for (var i = 0; i < chk.length; i++) {
        if (chk[i].checked && chk_fuku[i].checked) {
            alert(
                "正副両方にチェックされています。\n正か副どちらか一つにチェックして下さい。"
            );
            return false;
        }
        if (chk[i].checked || chk_fuku[i].checked) {
            if (chk[i].checked) {
                var tmp = chk[i].value.split(",");
                Ch_txt1 = Ch_txt1 + sep1 + tmp[1];
                sep1 = ",";
                kubun = "1";
            }
            if (chk_fuku[i].checked) {
                var tmp = chk_fuku[i].value.split(",");
                Ch_txt = Ch_txt + sep2 + tmp[1];
                sep2 = ",";
                kubun = "0";
            }
            Ch_val = Ch_val + sep + tmp[0];
            Ch_val1 = Ch_val1 + sep + kubun;
            Ch_val2 = Ch_val2 + sep + tmp[0] + "-" + kubun;
            if (document.forms[0].useChairStaffOrder.value == "1") {
                setOrder = setOrder + sep + stfOrder[i].value;
                setStfOrder =
                    setStfOrder + sep + tmp[0] + "-" + stfOrder[i].value;
            } else {
                setOrder = setOrder + sep;
                setStfOrder = setStfOrder + sep + tmp[0] + "-";
            }
            sep = ",";
        }
    }
    if (Ch_val == "") {
        if (chk.checked) {
            var tmp = chk.value.split(",");

            if (chk_fuku.checked) {
                Ch_txt = Ch_txt + sep2 + tmp[1];
                sep2 = ",";
                kubun = "0";
            } else {
                Ch_txt1 = Ch_txt1 + sep1 + tmp[1];
                sep1 = ",";
                kubun = "1";
            }

            Ch_val = Ch_val + sep + tmp[0];
            Ch_val1 = Ch_val1 + sep + kubun;
            Ch_val2 = Ch_val2 + sep + tmp[0] + "-" + kubun;
            if (document.forms[0].useChairStaffOrder.value == "1") {
                setOrder = setOrder + sep + stfOrder.value;
                setStfOrder = setStfOrder + sep + tmp[0] + "-" + stfOrder.value;
            } else {
                setOrder = setOrder + sep;
                setStfOrder = setStfOrder + sep + tmp[0] + "-";
            }

            sep = ",";
        }
    }
    top.main_frame.right_frame.document.forms[0].STAFFCD.value = Ch_val;
    top.main_frame.right_frame.document.forms[0].CHARGEDIV.value = Ch_val1;
    top.main_frame.right_frame.document.forms[0].STAFFNAME_SHOW1.value = Ch_txt1;
    top.main_frame.right_frame.document.forms[0].STAFFNAME_SHOW.value = Ch_txt;
    top.main_frame.right_frame.document.forms[0].STF_CHARGE.value = Ch_val2;
    top.main_frame.right_frame.document.forms[0].ORDER.value = setOrder;
    top.main_frame.right_frame.document.forms[0].STF_ORDER.value = setStfOrder;

    top.main_frame.right_frame.closeit();
}

function VisibleToolMsg(e) {
    var setMsg = "表示順";
    x = event.clientX + document.body.scrollLeft;
    y = event.clientY + document.body.scrollTop;
    document.getElementById("toolMsg").innerHTML = setMsg;
    document.getElementById("toolMsg").style.position = "absolute";
    document.getElementById("toolMsg").style.left = x + 5;
    document.getElementById("toolMsg").style.top = y + 10;
    document.getElementById("toolMsg").style.padding = "4px 3px 3px 8px";
    document.getElementById("toolMsg").style.border = "solid";
    document.getElementById("toolMsg").style.display = "block";
    document.getElementById("toolMsg").style.background = "#ccffff";
}

function InvisibleToolMsg(e) {
    document.getElementById("toolMsg").style.display = "none";
}
