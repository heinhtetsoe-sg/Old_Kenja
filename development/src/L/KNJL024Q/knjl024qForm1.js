function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//戻る
function Page_jumper(link) {
    parent.location.href=link;
}

// CHECK STRING - ENSURE ALL CHARACTERS ARE LETTERS
function charCount(val, gyosu, itigyou_no_mojiLen, dispMsg) {
    //改行コード判定
    kaigyo = "\r\n";
    if (val.indexOf("\r\n") > -1) {
        kaigyo = "\r\n";
    } else if (val.indexOf("\n") > -1) {
        kaigyo = "\n";
    } else if (val.indexOf("\r") > -1) {
        kaigyo = "\r";
    }

    //改行コードで区切って配列に入れていく
    stringArray = new Array();
    stringArray = val.split(kaigyo);

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
