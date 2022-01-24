//選択ボタン押し下げ時の処理
function btn_submit(datacnt, counter) {
    if (datacnt == 0) return false; // 04//11/12Add

    var chk = document.forms[0]["CHECK[]"];
    var sep = "";
    var Ch_val = "";
    var Ch_txt = "";

    for (var i = 0; i < chk.length; i++) {
        if (chk[i].checked) {
            var tmp = chk[i].value.split(",");

            Ch_val = Ch_val + sep + tmp[0];
            Ch_txt = Ch_txt + sep + tmp[1];
            sep = ",";
        }
    }
    if (Ch_val == "") {
        if (chk.checked) {
            var tmp = chk.value.split(",");

            Ch_val = Ch_val + sep + tmp[0];
            Ch_txt = Ch_txt + sep + tmp[1];
            sep = ",";
        }
    }
    parent.document.forms[0]["GRADE_CLASS" + "-" + counter].value = Ch_val;
    parent.document.forms[0]["HR_NAMEABBV" + "-" + counter].value = Ch_txt;
    //講座名称の自動表示機能・・・追加行のみ
    var updated = parent.document.forms[0]["UPDATED" + "-" + counter].value; //講座データの更新日付
    if (updated == "") {
        var subclassname = parent.document.forms[0]["SUBCLASSNAME"].value; //科目名称
        var subclassabbv = parent.document.forms[0]["SUBCLASSABBV"].value; //科目略称
        Ch_txt = Ch_txt.replace(/,/g, ""); //組略称・・・カンマを取り除く
        parent.document.forms[0]["CHAIRNAME" + "-" + counter].value =
            subclassname + Ch_txt;
    }

    parent.closeit();
}
