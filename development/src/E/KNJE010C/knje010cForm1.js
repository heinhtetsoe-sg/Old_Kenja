function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    if (cmd == 'reload'){ //成績参照
        reloadIframe("knje010cindex.php?cmd=reload");
        for (var i = 0; i < document.forms[0]["CHECK\[\]"].length; i++){
            document.forms[0]["CHECK\[\]"][i].checked = false;
        }
        return true;
    }else if (cmd == 'form2_first'){ //特別活動の記録～
        if (document.forms[0].useSyojikou3.value == "1") {
            loadwindow('knje010cindex.php?cmd=form2_first',0,document.documentElement.scrollTop || document.body.scrollTop,780,660);
        } else if (document.forms[0].tyousasyoTokuBetuFieldSize.value == "1") {
            loadwindow('knje010cindex.php?cmd=form2_first',0,document.documentElement.scrollTop || document.body.scrollTop,780,650);
        } else {
            loadwindow('knje010cindex.php?cmd=form2_first',0,document.documentElement.scrollTop || document.body.scrollTop,670,500);
        }
        return true;
    }else if (cmd == 'form3_first'){ //成績参照
        loadwindow('knje010cindex.php?cmd=form3_first',0,document.documentElement.scrollTop || document.body.scrollTop,600,540);
        return true;
    }else if (cmd == 'form4_first'){ //指導要録参照
        loadwindow('knje010cindex.php?cmd=form4_first',0,document.documentElement.scrollTop || document.body.scrollTop,710,290);
        return true;
    }else if (cmd == 'form6_first'){ //指導要録参照
        loadwindow('knje010cindex.php?cmd=form6_first',0,document.documentElement.scrollTop || document.body.scrollTop,730,430);
        return true;
    }else if (cmd == 'reset'){ //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function CheckHealth(obj)
{
    var el = obj.value;
    if (obj.checked == true) {
        var val = "異常なし";
        if (el == "TR_REMARK") val = "特記事項なし";
        document.forms[0][el].value = val;
        document.forms[0][el].onfocus = new Function("this.blur()");
    } else {
        document.forms[0][el].value = document.forms[0][el].defaultValue;
        document.forms[0][el].onfocus = new Function("");
    }

}
function reloadIframe(url){
    document.getElementById("cframe").src=url
}

function dummycheck(){
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order){
   if (document.forms[0].SCHREGNO.value == ""){
       alert('{rval MSG304}');
       return true;
   }
    nextURL = "";
    for (var i = 0; i < parent.left_frame.document.links.length; i++){
          var search = parent.left_frame.document.links[i].search;
          //searchの中身を&で分割し配列にする。
          arr = search.split("&");

          //学籍番号が一致
          if (arr[1] == "SCHREGNO="+schregno){
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length-1){
                idx = 0;                                         //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            }else if (order == 0){
                idx = i+1;                                       //更新後次の生徒へ
            }else if (order == 1 && i == 0){
                idx = parent.left_frame.document.links.length-1; //更新後前の生徒へ(データが最初の生徒の時)
            }else if (order == 1){
                idx = i-1;                                       //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href;//上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = 'update';
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd){
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL){
        if(cd == '0'){
                //クッキー削除
                deleteCookie("nextURL");

                document.location.replace(nextURL);
            alert('{rval MSG201}');
        }else if(cd == '1'){
                //クッキー削除
                deleteCookie("nextURL");

        }
    }
}

//備考チェックボックス
function CheckRemark() {
    if (document.forms[0].NO_COMMENTS.checked == true) {
        document.forms[0].REMARK.value = document.forms[0].NO_COMMENTS_LABEL.value;
        document.forms[0].REMARK.disabled =true;
    } else {
        document.forms[0].REMARK.disabled = false;
    }
}

function zentaicheck(checkgyousu) {
    valA = document.forms[0].TOTALSTUDYACT.value;
    //改行コードで区切って配列に入れていく
    stringArray = new Array();
    stringArray = valA.split("\r\n");
    row_cntA = 0;
    //改行コードが現れるまでに何行消費するか数える
    for (var i = 0; i < stringArray.length; i++) {
        mojisu = stringArray[i].length;
        mojiLen = 0;
        for (var j = 0; j < mojisu; j++) {
            hitoMoji = stringArray[i].charAt(j);
            moji_hantei = escape(hitoMoji).substr(0,2);
            mojiLen += moji_hantei == "%u" ? 2 : 1;
        }
        amari = mojiLen % (41 * 2);
        gyousu = (mojiLen - amari) / (41 * 2);
        if (amari > 0) {
            gyousu++;
        }
        if (gyousu) {
            row_cntA += gyousu;
        } else {
            row_cntA++;
        }
    }
    if (valA == "") {
        row_cntA = 0;
    }
    valB = document.forms[0].TOTALSTUDYVAL.value;
    //改行コードで区切って配列に入れていく
    stringArrayB = new Array();
    stringArrayB = valB.split("\r\n");
    row_cntB = 0;
    //改行コードが現れるまでに何行消費するか数える
    for (var i = 0; i < stringArrayB.length; i++) {
        mojisu = stringArrayB[i].length;
        mojiLen = 0;
        for (var j = 0; j < mojisu; j++) {
            hitoMoji = stringArrayB[i].charAt(j);
            moji_hantei = escape(hitoMoji).substr(0,2);
            mojiLen += moji_hantei == "%u" ? 2 : 1;
        }
        amari = mojiLen % (41 * 2);
        gyousu = (mojiLen - amari) / (41 * 2);
        if (amari > 0) {
            gyousu++;
        }
        if (gyousu) {
            row_cntB += gyousu;
        } else {
            row_cntB++;
        }
    }
    if (valB == "") {
        row_cntB = 0;
    }
    valC = document.forms[0].REMARK.value;
    //改行コードで区切って配列に入れていく
    stringArrayC = new Array();
    stringArrayC = valC.split("\r\n");
    row_cntC = 0;
    //改行コードが現れるまでに何行消費するか数える
    for (var i = 0; i < stringArrayC.length; i++) {
        mojisu = stringArrayC[i].length;
        mojiLen = 0;
        for (var j = 0; j < mojisu; j++) {
            hitoMoji = stringArrayC[i].charAt(j);
            moji_hantei = escape(hitoMoji).substr(0,2);
            mojiLen += moji_hantei == "%u" ? 2 : 1;
        }
        amari = mojiLen % (41 * 2);
        gyousu = (mojiLen - amari) / (41 * 2);
        if (amari > 0) {
            gyousu++;
        }
        if (gyousu) {
            row_cntC += gyousu;
        } else {
            row_cntC++;
        }
    }
    if (valC == "") {
        row_cntC = 0;
    }
    var zentaigyousu = row_cntA + row_cntB + row_cntC;
    if (zentaigyousu > checkgyousu) {
        alert('画面全体の行数制限を超えています。合計で' + checkgyousu + '行以内にして下さい。\n入力内容\n　活動内容：' + row_cntA + '行　評価：' + row_cntB + '行　備考：' + row_cntC + '行');
    }
}