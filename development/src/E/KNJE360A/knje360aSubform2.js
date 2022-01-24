//反映ボタン押し下げ時の処理
function btn_submit() {
    var item = document.forms[0].GET_ITEM.value;
    var seq = document.forms[0].GET_SEQ.value;
    var seqArray = seq.split(',');

    var checkStr = eval("/^" + item + "-" + "/");
    var checkTaisyou = eval("/^TAISYOU-" + "/");

    var ceckOk = "";
    for (var i=0; i < parent.document.forms[0].elements.length; i++) {
        if (parent.document.forms[0].elements[i].name.match(checkStr)
            ||
            parent.document.forms[0].elements[i].name.match(checkTaisyou)
            ) {
            for (var j=0; j < seqArray.length; j++) {
                var checkStrCounter = eval("/^" + item + "-" + seqArray[j] + "$/");
                var checkTaisyouObj = eval("/^TAISYOU-" + seqArray[j] + "$/");
                var updObjName = eval("/^" + seqArray[j] + "$/");
                if (parent.document.forms[0].elements[i].name.match(checkTaisyouObj)
                    &&
                    parent.document.forms[0].elements[i].checked
                ) {
                    ceckOk = ceckOk + "," + seqArray[j];
                }
                if (parent.document.forms[0].elements[i].name.match(checkStrCounter)
                    &&
                    ceckOk.indexOf(seqArray[j]) != -1
                ) {
                    parent.document.forms[0].elements[i].value = document.forms[0].REP_VALUE.value;
                }
            }
        }
    }

    //画面を閉じる
    parent.closeit();
}

//対象データ存在チェック
function checkDataExist() {
    alert('変更対象データがありません。');
    parent.closeit();
}
