//選択されている教職員のチェックを入れる
function onLoadFunc() {
    var textCd   = document.forms[0].TEXT_CD.value;
    var staffCd   = parent.document.getElementsByName(textCd)[0].value;

    var staffCount = document.forms[0].COUNTER.value - 1;
    for (let i = 0; i <= staffCount; i++) {
        var checkObj = document.getElementById("STAFFCHECK-" + i);
        if (checkObj.value == staffCd) {
            //同じ職員コードのものがあれば自動チェック
            checkObj.checked = true;
            break;
        }
    }
}

//チェック時
function changeCheck(obj) {
    //現在チェックされているチェックボックスとその職員コードを保持
    var currentCheck        = null;
    var currentCheckStaffCd = "";
    var staffCount = document.forms[0].COUNTER.value - 1;
    for (let i = 0; i <= staffCount; i++) {
        var tempObj = document.getElementById("STAFFCHECK-" + i);
        if (tempObj.checked == true && tempObj != obj) {
            currentCheck        = tempObj;
            currentCheckStaffCd = tempObj.value;
        }
    }

    //別のチェックボックスがチェックされている場合はチェックを切り替える
    //下記の場合は何もしない
    //・同じチェックボックスの切り替えの場合
    //・チェックオンのチェックボックスがない場合
    if (currentCheckStaffCd != "" && currentCheck != obj) {
        currentCheck.checked = false;
        obj.checked = true;
    }
}

//選択ボタン押下時
function goWin() {
    //KNJH705の表示するテキスト名取得
    var textCd   = document.forms[0].TEXT_CD.value;
    var textName = document.forms[0].TEXT_NAME.value;

    //KNJH705の監督の要素取得
    var staffCd   = parent.document.getElementsByName(textCd);
    var staffName = parent.document.getElementsByName(textName);

    //教員数
    var staffCount = document.forms[0].COUNTER.value - 1;

    //チェックのついている値を取得
    staffCd[0].value   = "";
    staffName[0].value = "";
    for (let i = 0; i <= staffCount; i++) {
        if (document.getElementById("STAFFCHECK-" + i).checked == true) {
            staffCd[0].value   = document.getElementById("STAFFCHECK-" + i).value;
            staffName[0].value = document.getElementsByName("STAFFNAME-" + i)[0].value;
            break;
        }
    }
    parent.closeit();
}

function closePop() {
    parent.closeit();
}
