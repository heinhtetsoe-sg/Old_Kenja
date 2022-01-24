//選択ボタン押し下げ時の処理
function btn_submit(datacnt) {
    if (datacnt == 0) return false;
    var getcmd = document.forms[0].GET_CMD.value;
    var chk = document.forms[0]["CHECK[]"];
    var sep = (sep1 = sep2 = "");
    var Ch_txt1 = "";

    if (chk.length == undefined) {
        Ch_txt1 = chk.value;
    } else {
        for (var i = 0; i < chk.length; i++) {
            if (chk[i].checked) {
                Ch_txt1 = Ch_txt1 + sep1 + chk[i].value;
                sep1 = ",";
            }
        }
    }

    if (getcmd === "challenged_master") {
        if (top.main_frame.right_frame.document.forms[0].CHALLENGED_NAMES.value != "") {
            sep2 = ",";
        }
        top.main_frame.right_frame.document.forms[0].CHALLENGED_NAMES.value = top.main_frame.right_frame.document.forms[0].CHALLENGED_NAMES.value + sep2 + Ch_txt1;
    } else if (getcmd === "challenged_training_master") {
        if (top.main_frame.right_frame.document.forms[0].REMARK.value != "") {
            sep2 = ",";
        }
        top.main_frame.right_frame.document.forms[0].REMARK.value = top.main_frame.right_frame.document.forms[0].REMARK.value + sep2 + Ch_txt1;
    } else if (getcmd === "team_member_master") {
        if (top.main_frame.right_frame.document.forms[0].TEAM_MEMBERS.value != "") {
            sep2 = ",";
        }
        top.main_frame.right_frame.document.forms[0].TEAM_MEMBERS.value = top.main_frame.right_frame.document.forms[0].TEAM_MEMBERS.value + sep2 + Ch_txt1;
    } else if (getcmd === "aftertime_need_service_master") {
        if (top.main_frame.right_frame.document.forms[0].SERVICE_NEED_FUTURE.value != "") {
            sep2 = ",";
        }
        top.main_frame.right_frame.document.forms[0].SERVICE_NEED_FUTURE.value = top.main_frame.right_frame.document.forms[0].SERVICE_NEED_FUTURE.value + sep2 + Ch_txt1;
        //福祉の将来必要と考えられるサービスの時、3箇所選択チェックボックス
    } else if (getcmd === "medical_care_master") {
        //家庭・病院
        var chk2 = document.forms[0]["CHECK2[]"];
        var sep3 = (sep4 = "");
        var Ch_txt2 = "";
        for (var i = 0; i < chk2.length; i++) {
            if (chk2[i].checked) {
                Ch_txt2 = Ch_txt2 + sep3 + chk2[i].value;
                sep3 = ",";
            }
        }
        //事業所
        var chk3 = document.forms[0]["CHECK3[]"];
        var sep5 = (sep6 = "");
        var Ch_txt3 = "";
        for (var i = 0; i < chk3.length; i++) {
            if (chk3[i].checked) {
                Ch_txt3 = Ch_txt3 + sep5 + chk3[i].value;
                sep5 = ",";
            }
        }
        //学校
        if (top.main_frame.right_frame.document.forms[0].SCHOOL_CARE.value != "") {
            sep2 = ",";
        }
        top.main_frame.right_frame.document.forms[0].SCHOOL_CARE.value = top.main_frame.right_frame.document.forms[0].SCHOOL_CARE.value + sep2 + Ch_txt1;
        //家庭・病院
        if (top.main_frame.right_frame.document.forms[0].HOUSE_CARE.value != "") {
            sep4 = ",";
        }
        top.main_frame.right_frame.document.forms[0].HOUSE_CARE.value = top.main_frame.right_frame.document.forms[0].HOUSE_CARE.value + sep4 + Ch_txt2;
        //事業所
        if (top.main_frame.right_frame.document.forms[0].CENTER_CARE.value != "") {
            sep6 = ",";
        }
        top.main_frame.right_frame.document.forms[0].CENTER_CARE.value = top.main_frame.right_frame.document.forms[0].CENTER_CARE.value + sep6 + Ch_txt3;
    } else if (getcmd === "medical_center") {
        if (top.main_frame.right_frame.document.forms[0].CENTER_NAME.value != "") {
            sep2 = ",";
        }
        top.main_frame.right_frame.document.forms[0].CENTER_NAME.value = top.main_frame.right_frame.document.forms[0].CENTER_NAME.value + sep2 + Ch_txt1;
    } else if (getcmd === "checkname_master") {
        if (top.main_frame.right_frame.document.forms[0].CHECK_NAME.value != "") {
            sep2 = ",";
        }
        top.main_frame.right_frame.document.forms[0].CHECK_NAME.value = top.main_frame.right_frame.document.forms[0].CHECK_NAME.value + sep2 + Ch_txt1;
    }
    top.main_frame.right_frame.closeit();
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}
