// Add by PP for loading focus 2020-02-03 start
setTimeout(function () {
window.onload = new function () {
    if (sessionStorage.getItem("KNJE460SienKikanForm1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJE460SienKikanForm1_CurrentCursor")).focus();
            // remove item
            sessionStorage.removeItem('KNJE460SienKikanForm1_CurrentCursor');  
        } else {
            // start loading focus
            document.getElementById('screen_id').focus();
    }
    setTimeout(function () {
            document.title = TITLE; 
    }, 100);
 }
}, 800);
function current_cursor(para) {
    sessionStorage.setItem("KNJE460SienKikanForm1_CurrentCursor", para);
}

// Add by PP loading focus 2020-02-20 end
//サブミット
function btn_submit(cmd) {
    /* Add by PP for CurrentCursor 2020-02-03 start */
    if (sessionStorage.getItem("KNJE460SienKikanForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJE460SienKikanForm1_CurrentCursor")).blur();
    }
    /* Add by PP for CurrentCursor 2020-02-20 end */
    if (cmd == 'subform1_update') { 
        var totalGyousu = 0;
        var total_row_cnt = 0;

        //指定の関係機関の行数
        attribute1 = document.forms[0].SIEN_KIKAN;
        var sienkikan_cd = attribute1.options[attribute1.selectedIndex].value;
        for (var re = 1; re <= document.forms[0].SIEN_KIKAN_COUNT.value; re++) {
            total_row_cnt = 0;
            var sienkikan = document.forms[0]['SIEN_KIKAN'+re].value;
            var sienkikanmei = document.forms[0]['SIEN_KIKAN_MEI'+re].value;

            for (var idx = 1; idx <= document.forms[0].SELECT_COUNT.value; idx++) {
                var val = document.forms[0]['SIEN_'+sienkikan+idx].value;
                if(val != ''){
                    total_row_cnt += 2;
                }
            }

            //総行数の加算
            if(sienkikanmei.length >= total_row_cnt){
                totalGyousu += sienkikanmei.length;
            } else {
                totalGyousu += total_row_cnt;
            }
        }


        //指定関係機関が全て以外の場合
        if(sienkikan_cd != 'ALL'){
            //指定以外の関係機関の行数
            total_row_cnt = 0;
            var wk_sienkikan_cd    = '';
            var wk_sienkikan_name  = '';
            for (var idx = 1; idx <= document.forms[0].HID_SELECT_COUNT.value; idx++) {
                var hid_sienkikan_cd = document.forms[0]['HID_SIEN_KIKAN_CD'+idx].value;
                var hid_sienkikan    = document.forms[0]['HID_SIEN_KIKAN'+idx].value;

                if(hid_sienkikan_cd != document.forms[0].SIEN_KIKAN.value){
                    if(wk_sienkikan_cd != '' && wk_sienkikan_cd != hid_sienkikan_cd){
                        //総行数の加算
                        if(wk_sienkikan_name.length >= total_row_cnt){
                            totalGyousu += wk_sienkikan_name.length;
                        } else {
                            totalGyousu += total_row_cnt;
                        }
                        total_row_cnt = 0;
                    }
                    var hid_sien = document.forms[0]['HID_SIEN'+idx].value;
                    if(hid_sien != ''){
                        total_row_cnt += 2;
                    }

                    wk_sienkikan_cd   = hid_sienkikan_cd;
                    wk_sienkikan_name = hid_sienkikan;
                }
            }
            if(wk_sienkikan_cd != '' && wk_sienkikan_cd != document.forms[0].SIEN_KIKAN.value){
                //総行数の加算
                if(wk_sienkikan_name.length >= total_row_cnt){
                    totalGyousu += wk_sienkikan_name.length;
                } else {
                    totalGyousu += total_row_cnt;
                }
                total_row_cnt = 0;
            }

        }

        if(totalGyousu > 50){
            alert('印刷可能な行数を超えています。');
            return true;
        }
    }

    if (cmd == "subform1_loadpastyear") {
        if (document.forms[0].PASTYEAR.value == "") {
            alert('{rval MSG301}' + "年度を選択してください。");
            return false;
        } else {
            document.forms[0].HID_PASTYEARLOADFLG.value = "1";
            cmd = "edit";  //イベントをeditにする
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//渡された文字列の行数を返却
function getRowCnt(val){
    var itigyou_no_mojiLen = 50;
    var row_cnt = 0;
    if(val != ""){
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
        var gyousu = 1;
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
    } else {
        //支援内容が空白の場合
        row_cnt = 0;
    }
    return row_cnt;
}