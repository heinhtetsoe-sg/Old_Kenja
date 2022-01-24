/* Add by HPA for current_cursor start 2020/02/03 */
window.onload = function () {
    document.title = "クラス別出欠情報入力（月単位）画面";
    if (sessionStorage.getItem("KNJC034DForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJC034DForm1_CurrentCursor")).focus();
        setTimeout(function () {
            document.title = "クラス別出欠情報入力（月単位）画面";
        }, 1000);
    }
}

function current_cursor(para) {
    sessionStorage.setItem("KNJC034DForm1_CurrentCursor", para);
}

function current_cursor_focus() {
    document.getElementById(sessionStorage.getItem("KNJC034DForm1_CurrentCursor")).focus();
}
/* Add by HPA for current_cursor end 2020/02/20 */

function btn_submit(cmd) {
    //データ指定チェック
    /* Add by HPA for current_cursor start 2020/02/03 */
    if (sessionStorage.getItem("KNJC034DForm1_CurrentCursor") != null) {
        document.title = "";
        parent.top_frame.document.title = "";
        document.getElementById(sessionStorage.getItem("KNJC034DForm1_CurrentCursor")).blur();
    }
    /* Add by HPA for current_cursor end 2020/02/20 */
    if (cmd == 'update') {
        if (document.forms[0].DATA_CNT.value == 0) {
            alert('{rval MSG304}');
            return false;
        }
        if (!document.forms[0].MONTH_SEM.value) {
            alert('{rval MSG304}');
            return false;
        }

        //データを格納
        if (document.forms[0].SCHOOL_KIND) {
            document.forms[0].HIDDEN_SCHOOL_KIND.value = document.forms[0].SCHOOL_KIND.value;
        }
        document.forms[0].HIDDEN_MONTH_SEM.value = document.forms[0].MONTH_SEM.value;
        if (document.forms[0].useFi_Hrclass.value == "1" || document.forms[0].useSpecial_Support_Hrclass.value == "1") {
            if (document.forms[0].HR_CLASS_TYPE[0].checked == true) document.forms[0].HIDDEN_HR_CLASS_TYPE.value = document.forms[0].HR_CLASS_TYPE[0].value;
            if (document.forms[0].HR_CLASS_TYPE[1].checked == true) document.forms[0].HIDDEN_HR_CLASS_TYPE.value = document.forms[0].HR_CLASS_TYPE[1].value;
            if (document.forms[0].useFi_Hrclass.value != "1") {
                if (document.forms[0].GRADE_MIX.checked == true) document.forms[0].HIDDEN_GRADE_MIX.value = document.forms[0].GRADE_MIX.value;
            }
        } else {
            document.forms[0].HIDDEN_HR_CLASS_TYPE.value = document.forms[0].HR_CLASS_TYPE.value;
        }
        document.forms[0].HIDDEN_GRADE_HR_CLASS.value = document.forms[0].GRADE_HR_CLASS.value;

        //使用不可項目
        if (document.forms[0].SCHOOL_KIND) {
            document.forms[0].SCHOOL_KIND.disabled = true;
        }
        document.forms[0].MONTH_SEM.disabled = true;
        if (document.forms[0].useFi_Hrclass.value == "1" || document.forms[0].useSpecial_Support_Hrclass.value == "1") {
            document.forms[0].HR_CLASS_TYPE[0].disabled = true;
            document.forms[0].HR_CLASS_TYPE[1].disabled = true;
            if (document.forms[0].useFi_Hrclass.value != "1") {
                document.forms[0].GRADE_MIX.disabled = true;
            }
        }
        document.forms[0].GRADE_HR_CLASS.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }
    //取消確認
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//スクロール
function scrollRC() {
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

//チェックボックスのラベル表示（出欠済・未）
/* Edit by HPA for PC-talker 読み start 2020/02/03 */
function checkExecutedLabel(obj, id, label_id, day) {
    var zumi = document.getElementById(id);
    var labelId = document.getElementById(label_id);
    if (obj.checked) {
        labelId.setAttribute("aria-label", day + "日 の 済");
        zumi.innerHTML = '<font color="white">' + '済' + '</font>';
    } else {
        labelId.setAttribute("aria-label", day + "日 の 未");
        zumi.innerHTML = '<font color="#ff0099">' + '未' + '</font>';
    }
}
/* Edit by HPA for PC-talker 読み end 2020/02/20 */
