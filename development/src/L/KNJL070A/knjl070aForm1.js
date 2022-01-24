function btn_submit(cmd) {
    //取消
    if (cmd == "reset" && !confirm("{rval MSG106}")) return true;

    //チェックボックスのチェック有無
    var counter = 0;
    if (cmd == "update" || cmd == "end") {
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name.match(/^CHECKED/) && document.forms[0].elements[i].checked == true) {
                counter++;
            }
        }
    }

    //更新
    if (cmd == "update") {
        //更新対象生徒チェック
        if (counter == 0) {
            alert("{rval MSG304}");
            return false;
        }

        //データを格納
        document.forms[0].HIDDEN_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.value;
        document.forms[0].HIDDEN_TESTDIV.value = document.forms[0].TESTDIV.value;
        if (document.forms[0].SORT[0].checked == true) document.forms[0].HIDDEN_SORT.value = document.forms[0].SORT[0].value;
        if (document.forms[0].SORT[1].checked == true) document.forms[0].HIDDEN_SORT.value = document.forms[0].SORT[1].value;
        if (document.forms[0].SHDIV[0].checked == true) document.forms[0].HIDDEN_SHDIV.value = document.forms[0].SHDIV[0].value;
        if (document.forms[0].SHDIV[1].checked == true) document.forms[0].HIDDEN_SHDIV.value = document.forms[0].SHDIV[1].value;
        if (document.forms[0].SHDIV[2].checked == true) document.forms[0].HIDDEN_SHDIV.value = document.forms[0].SHDIV[2].value;
        document.forms[0].HIDDEN_PASS_DIV.value = document.forms[0].PASS_DIV.value;
        document.forms[0].HIDDEN_WISH_COURSE.value = document.forms[0].WISH_COURSE.value;
        document.forms[0].HIDDEN_UPD_SHDIV.value = document.forms[0].UPD_SHDIV.value;
        document.forms[0].HIDDEN_UPD_COURSE.value = document.forms[0].UPD_COURSE.value;
        document.forms[0].HIDDEN_HEADER.value = document.forms[0].HEADER.value;

        //使用不可項目
        document.forms[0].APPLICANTDIV.disabled = true;
        document.forms[0].TESTDIV.disabled = true;
        document.forms[0].SORT[0].disabled = true;
        document.forms[0].SORT[1].disabled = true;
        document.forms[0].SHDIV[0].disabled = true;
        document.forms[0].SHDIV[1].disabled = true;
        document.forms[0].SHDIV[2].disabled = true;
        document.forms[0].PASS_DIV.disabled = true;
        document.forms[0].WISH_COURSE.disabled = true;
        document.forms[0].UPD_SHDIV.disabled = true;
        document.forms[0].UPD_COURSE.disabled = true;
        document.forms[0].HEADER.disabled = true;
        document.forms[0].btn_input.disabled = true;
        document.forms[0].btn_output.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    //終了
    if (cmd == "end") {
        if (counter > 0) {
            if (confirm("{rval MSG108}")) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    //CSV出力
    if (cmd == "csvOutput" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    //CSV取込
    if (cmd == "csvInput") {
        //使用不可項目
        document.forms[0].btn_input.disabled = true;
        document.forms[0].btn_output.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var el = document.forms[0].elements[i];
        if (el.name.match(/^CHECKED/) && el.disabled == false) {
            el.checked = obj.checked;

            var repno = el.id.split("_");
            var col = document.getElementById("bgcolor-" + repno[1]);

            //背景色変更
            if (obj.checked == true) {
                col.style.backgroundColor = "yellow";
            } else {
                col.style.backgroundColor = "#ffffff";
            }
        }
    }
}

//チェックボックスによる背景色変更
function checkboxChgBGColor(obj, receptno) {
    console.log("bgcolor-" + receptno);
    var col = document.getElementById("bgcolor-" + receptno);

    if (obj.checked == true) {
        col.style.backgroundColor = "yellow";
    } else {
        col.style.backgroundColor = "#ffffff";
    }
}

//コンボボックスによる背景色等変更
function cmbChgBGColor(obj) {
    var repno = document.forms[0].HID_RECEPTNO.value.split(",");
    var upd_shdiv = document.forms[0].UPD_SHDIV;
    var upd_course = document.forms[0].UPD_COURSE;

    //ALLチェックボックスのコントロール
    disCheckAll = true;
    if (upd_shdiv.value.length && upd_course.value.length) {
        disCheckAll = false;
    }
    document.forms[0].CHECKALL.disabled = disCheckAll;

    if (repno.length > 0) {
        for (var i = 0; i < repno.length; i++) {
            var shdiv = document.forms[0]["SHDIV-" + repno[i]];
            for (var sh = 1; sh <= 2; sh++) {
                //対象
                target = "COURSE" + sh + "-" + repno[i];
                var selectedCourse = document.getElementById(target);

                //登録済みコース
                var courseData = document.forms[0]["COURSECD" + sh + "-" + repno[i]];

                //背景色
                bgcolor = "";
                if (shdiv.value < sh) {
                    //対象外
                    selectedCourse.className = "no_search";
                } else {
                    if (upd_shdiv.value.length && upd_shdiv.value == sh) {
                        bgcolor = "lime";
                    } else if (upd_course.value.length && upd_course.value == courseData.value) {
                        bgcolor = "lime";
                    }
                    if (upd_shdiv.value.length && upd_course.value.length) {
                        if (upd_shdiv.value == sh && upd_course.value == courseData.value) {
                            bgcolor = "pink";
                        } else if (upd_shdiv.value != sh) {
                            bgcolor = "";
                        }
                    }
                    if (!bgcolor.length) {
                        bgcolor = "white";
                    }
                    //セット
                    selectedCourse.style.backgroundColor = bgcolor;
                }
            }

            var chk = document.getElementById("CHECKED_" + repno[i]);
            var bgclr = document.getElementById("bgcolor-" + repno[i]);

            //チェックボックスのコントロール
            disCheckbox = true;
            if (upd_shdiv.value.length && upd_course.value.length && upd_shdiv.value <= shdiv.value) {
                disCheckbox = false;
            }
            chk.disabled = disCheckbox;

            if (disCheckbox == true) {
                chk.checked = false;
                bgclr.style.backgroundColor = "#ffffff";
            }
        }
    }
    return;
}
