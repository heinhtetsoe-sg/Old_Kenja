// Add by PP for loading focus 2020-02-03 start

window.onload = function () {
    if (sessionStorage.getItem("KNJS343Form1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJS343Form1_CurrentCursor")).focus();
            // remove item
            sessionStorage.removeItem('KNJS343Form1_CurrentCursor');  
    }
    setTimeout(function () {
            document.title = TITLE;
    }, 100);
 }

function current_cursor(para) {
    sessionStorage.setItem("KNJS343Form1_CurrentCursor", para);
}

function tmpSet(obj, id) {
    var value = obj.value;
    obj.value = toInteger(obj.value);
    if (obj.value != value) {
        document.getElementById(id).focus();
    }
}

// Add by PP loading focus 2020-02-20 end
function btn_submit(cmd) {
    // Add by PP for CurrentCursor 2020-02-03 start 
    if (sessionStorage.getItem("KNJS343Form1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJS343Form1_CurrentCursor")).blur();
    }
   // Add by PP for CurrentCursor 2020-02-20 end 

    for (var i=1; i <= document.forms[0].rowCnt.value; i++) {
        document.forms[0]["GNAME-" + i].value = document.getElementById('GNAME-' + i).innerHTML;
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    if (cmd == "update") {
        var checkFlg = false;
        for (var i=1; i <= document.forms[0].rowCnt.value; i++) {
            if (document.forms[0]["NAME-" + i].value != '') {
                checkFlg = true;
            }
        }
        if (!checkFlg) {
            alert('生徒が選択されていません。');
            // Add by PP for CurrentCursor 2020-02-03 start
            document.getElementById(sessionStorage.getItem("KNJS343Form1_CurrentCursor")).focus();
            // remove item
            sessionStorage.removeItem('KNJS343Form1_CurrentCursor'); 
            setTimeout(function () {
            document.title = TITLE;
            }, 100);
            // Add by PP for CurrentCursor 2020-02-20 end
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//全チェック
function allCheck(obj, rowCnt) {
    if (rowCnt) {
        for (var i=1; i <= rowCnt; i++) {
            document.forms[0]['CHECK-' + i].checked = obj.checked;
        }
    }
    return true;
}
//保護者氏名セット
// Add by PP for CurrentCursor 2020-02-03 start
function setGname(obj, idx) {
    var label = obj.options[obj.selectedIndex].text;
    var gName = obj.value.split('-')[1];
    if (gName) {
        document.getElementById('REMARK-' + idx).setAttribute("aria-label", label+"の事由");
        document.getElementById('TREATMENT-' + idx).setAttribute("aria-label", label+"の担任のとった処置");
        document.getElementById('GNAME-' + idx).innerHTML = gName;
    } else {
        document.getElementById('REMARK-' + idx).setAttribute("aria-label", "事由");
        document.getElementById('TREATMENT-' + idx).setAttribute("aria-label", "担任のとった処置");
        document.getElementById('GNAME-' + idx).innerHTML = '';
    }
}
// Add by PP for CurrentCursor 2020-02-20 end
