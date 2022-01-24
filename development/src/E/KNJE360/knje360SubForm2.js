function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == '') {
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'subform2_clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //進路相談ボタン押し下げ時
    if (cmd == 'subform4') {
        loadwindow('knje360index.php?cmd=subform4&TYPE=btn', 0, 0, 760, 680);
        return true;
    }

    if (cmd == 'subform2_update') {
        if (document.forms[0].SEQ.value == '') {
            alert('{rval MSG308}');
            return true;
        }
        //update時で、プロパティが立っている
        if (document.forms[0].USEAUTOSETCOLLEGENAMETOTHINKEXAM.value == '1') {
            //進路状況が"1:決定"で登録済
            if (document.forms[0].HID_FSTREAD_PLANSTAT.value == '1') {
                //登録データと学校コードが一致しない
                if (document.forms[0].HID_FSTREAD_SCHOOL_CD.value != document.forms[0].SCHOOL_CD.value) {
                    //登録確認メッセージ
                    if (!confirm('{rval MSG105}' + '\n学校コードが変わる場合、指導要録に表記する進路先（直接入力）\nには登録されていた学校名称が設定されます。')) {
                        return false;
                    } else {
                        document.forms[0].THINKEXAM.value = document.forms[0].HID_FSTREAD_SCHOOL_NAME.value;
                    }
                }
            }
        }
    }

    if (cmd == 'subform2_insert' || cmd == 'subform2_update') {
        if (document.forms[0].SCHOOL_CD.value == '') {
            alert('{rval MSG304}\n　　（学校コード）');
            return true;
        }
        if (document.forms[0].TOROKU_DATE.value == '') {
            alert('データを入力してください。\n　　（登録日）');
            return true;
        }

        var date = document.forms[0].TOROKU_DATE.value.split('/');
        var sdate = document.forms[0].SDATE.value.split('/');
        var edate = document.forms[0].EDATE.value.split('/');
        sdate_show = document.forms[0].SDATE.value;
        edate_show = document.forms[0].EDATE.value;

        if (new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2])) || new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2]))) {
            alert('登録日が入力範囲外です。\n（' + sdate_show + '～' + edate_show + '）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function changeDispSh(obj) {
    var shArr = new Array();
    shArr = document.forms[0].SH_ARR.value.split(',');

    for (var i = 0; i < shArr.length; i++) {
        if (obj.value == shArr[i]) {
            document.getElementById('shDisp').style.display = 'inline';
            return;
        } else {
            document.getElementById('shDisp').style.display = 'none';
        }
    }
    return;
}
function keyChangeEntToTab(obj, nextObj) {
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    //方向キー
    //var moveEnt = e.keyCode;
    if (e.keyCode != 13) {
        return;
    }
    targetObject = document.forms[0][nextObj];
    targetObject.focus();
}

//調査書発行ラジオON/OFF
function issueControl(obj) {
    var org = document.forms[0].ORIGINAL_ISSUE.value;
    if (org == '1') {
        obj.checked = true;
    }
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order) {
    if (document.forms[0].SCHREGNO.value == '') {
        alert('{rval MSG304}');
        return true;
    }
    nextURL = '';

    for (var i = 0; i < parent.left_frame.document.links.length; i++) {
        var search = parent.left_frame.document.links[i].search;
        //searchの中身を&で分割し配列にする。
        arr = search.split('&');

        //学籍番号が一致
        if (arr[1] == 'SCHREGNO=' + schregno) {
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length - 1) {
                idx = 0; //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            } else if (order == 0) {
                idx = i + 1; //更新後次の生徒へ
            } else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length - 1; //更新後前の生徒へ(データが最初の生徒の時)
            } else if (order == 1) {
                idx = i - 1; //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href.replace('edit', 'subform2'); //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = 'subform2_insert';
    //クッキー書き込み
    saveCookie('nextURL', nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd) {
    var nextURL;
    nextURL = loadCookie('nextURL');
    if (nextURL) {
        if (cd == '0') {
            //クッキー削除
            deleteCookie('nextURL');
            document.location.replace(nextURL);
            alert('{rval MSG201}\n画面をクリアしました。');
        } else if (cd == '1') {
            //クッキー削除
            deleteCookie('nextURL');
        }
    }
}

//学校検索画面
function Page_jumper(link) {
    //学校検索画面を開く
    link = link + '/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=&target_number=';
    loadwindow(
        link,
        event.clientX +
            (function () {
                var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
                return scrollX;
            })(),
        0,
        650,
        600
    );

    //学校検索イベント
    collegeSelectEvent();
}

//学校検索イベント
function collegeSelectEvent() {
    knjAjax('subform2_college');
}
/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax(cmd) {
    //この区間は送信処理、
    var sendData = '';
    var seq = '';
    var form_datas = document.forms[0];

    sendData = 'cmd=' + cmd;
    sendData += '&SCHREGNO=' + form_datas.SCHREGNO.value;
    sendData += '&SEQ=' + form_datas.SEQ.value;
    sendData += '&SCHOOL_CD=' + form_datas.SCHOOL_CD.value;
    sendData += '&FACULTYCD=' + form_datas.FACULTYCD.value;
    sendData += '&DEPARTMENTCD=' + form_datas.DEPARTMENTCD.value;
    sendData += '&ADVERTISE_DIV=';
    sendData += '&PROGRAM_CD=';
    sendData += '&FORM_CD=';
    sendData += '&L_CD=';
    sendData += '&S_CD=';
    sendData += '&LIMIT_DATE_WINDOW=';
    sendData += '&LIMIT_DATE_MAIL=';
    sendData += '&LIMIT_MAIL_DIV=';
    sendData += '&STAT_DATE1=';
    sendData += '&STAT_DATE3=';
    //東京都集計用
    if (form_datas.useTokyotoShinroTyousasyo.value == '1') {
        sendData += '&SCHOOL_CATEGORY_CD=' + form_datas.SCHOOL_CATEGORY_CD.value;
        sendData += '&TOKYO_L_CD=' + form_datas.TOKYO_L_CD.value;
        sendData += '&TOKYO_M_CD=' + form_datas.TOKYO_M_CD.value;
    }

    httpObj = createXmlHttp();
    httpObj.onreadystatechange = statusCheck;
    httpObj.open('GET', 'knje360index.php?' + sendData, true); //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null); //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck() {
    //サーバからの応答をチェック
    /******** httpObj.readyState *******/ /********** httpObj.status *********/
    /*  0:初期化されていない           */ /*  200:OK                         */
    /*  1:読込み中                     */ /*  403:アクセス拒否               */
    /*  2:読込み完了                   */ /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */ /***********************************/
    /*  4:準備完了                     */
    /***********************************/
    if (httpObj.readyState == 4 && httpObj.status == 200) {
        //入試カレンダーの使用
        if (document.forms[0].useCollegeExamCalendar.value == '1') {
            var targetADVERTISE_DIV = document.getElementById('ADVERTISE_DIV');
            var targetPROGRAM_CD = document.getElementById('PROGRAM_CD');
            var targetFORM_CD = document.getElementById('FORM_CD');
            var targetL_CD = document.getElementById('L_CD');
            var targetS_CD = document.getElementById('S_CD');
        }
        var targetLIMIT_DATE_WINDOW = document.getElementById('LIMIT_DATE_WINDOW');
        var targetLIMIT_DATE_MAIL = document.getElementById('LIMIT_DATE_MAIL');
        var targetLIMIT_MAIL_DIV = document.getElementById('LIMIT_MAIL_DIV');
        var targetSTAT_DATE1 = document.getElementById('STAT_DATE1');
        var targetSTAT_DATE3 = document.getElementById('STAT_DATE3');
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            var targetSCHOOL_CATEGORY_CD = document.getElementById('SCHOOL_CATEGORY_CD');
            var targetTOKYO_L_CD = document.getElementById('TOKYO_L_CD');
            var targetTOKYO_M_CD = document.getElementById('TOKYO_M_CD');
        }

        var response = httpObj.responseText;
        var responseArray = response.split('::');
        //入試カレンダーの使用
        if (document.forms[0].useCollegeExamCalendar.value == '1') {
            targetADVERTISE_DIV.innerHTML = responseArray[0];
            targetPROGRAM_CD.innerHTML = responseArray[1];
            targetFORM_CD.innerHTML = responseArray[2];
            targetL_CD.innerHTML = responseArray[3];
            targetS_CD.innerHTML = responseArray[4];
        }
        targetLIMIT_DATE_WINDOW.innerHTML = responseArray[5];
        targetLIMIT_DATE_MAIL.innerHTML = responseArray[6];
        targetLIMIT_MAIL_DIV.innerHTML = responseArray[7];
        targetSTAT_DATE1.innerHTML = responseArray[8];
        targetSTAT_DATE3.innerHTML = responseArray[9];
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            targetSCHOOL_CATEGORY_CD.innerHTML = responseArray[18];
            targetTOKYO_L_CD.innerHTML = responseArray[19];
            targetTOKYO_M_CD.innerHTML = responseArray[20];
        }
    }
}
/************************** Ajax ***********************************/

//学校検索イベント（募集区分等）チェンジコンボ
function collegeSelectEvent2() {
    knjAjax2('subform2_college');
}
/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax2(cmd) {
    //この区間は送信処理、
    var sendData = '';
    var seq = '';
    var form_datas = document.forms[0];

    sendData = 'cmd=' + cmd;
    sendData += '&SCHREGNO=' + form_datas.SCHREGNO.value;
    sendData += '&SEQ=' + form_datas.SEQ.value;
    sendData += '&SCHOOL_CD=' + form_datas.SCHOOL_CD.value;
    sendData += '&FACULTYCD=' + form_datas.FACULTYCD.value;
    sendData += '&DEPARTMENTCD=' + form_datas.DEPARTMENTCD.value;
    //入試カレンダーの使用
    if (form_datas.useCollegeExamCalendar.value == '1') {
        sendData += '&ADVERTISE_DIV=' + form_datas.ADVERTISE_DIV.value;
        sendData += '&PROGRAM_CD=' + form_datas.PROGRAM_CD.value;
        sendData += '&FORM_CD=' + form_datas.FORM_CD.value;
        sendData += '&L_CD=' + form_datas.L_CD.value;
        sendData += '&S_CD=' + form_datas.S_CD.value;
    } else {
        sendData += '&ADVERTISE_DIV=';
        sendData += '&PROGRAM_CD=';
        sendData += '&FORM_CD=';
        sendData += '&L_CD=';
        sendData += '&S_CD=';
    }
    sendData += '&LIMIT_DATE_WINDOW=' + form_datas.LIMIT_DATE_WINDOW.value;
    sendData += '&LIMIT_DATE_MAIL=' + form_datas.LIMIT_DATE_MAIL.value;
    sendData += '&LIMIT_MAIL_DIV=' + form_datas.LIMIT_MAIL_DIV.value;
    sendData += '&STAT_DATE1=' + form_datas.STAT_DATE1.value;
    sendData += '&STAT_DATE3=' + form_datas.STAT_DATE3.value;
    //東京都集計用
    if (form_datas.useTokyotoShinroTyousasyo.value == '1') {
        sendData += '&SCHOOL_CATEGORY_CD=' + form_datas.SCHOOL_CATEGORY_CD.value;
        sendData += '&TOKYO_L_CD=' + form_datas.TOKYO_L_CD.value;
        sendData += '&TOKYO_M_CD=' + form_datas.TOKYO_M_CD.value;
    }

    httpObj = createXmlHttp();
    httpObj.onreadystatechange = statusCheck2;
    httpObj.open('GET', 'knje360index.php?' + sendData, true); //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null); //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck2() {
    //サーバからの応答をチェック
    /******** httpObj.readyState *******/ /********** httpObj.status *********/
    /*  0:初期化されていない           */ /*  200:OK                         */
    /*  1:読込み中                     */ /*  403:アクセス拒否               */
    /*  2:読込み完了                   */ /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */ /***********************************/
    /*  4:準備完了                     */
    /***********************************/

    if (httpObj.readyState == 4 && httpObj.status == 200) {
        //入試カレンダーの使用
        if (document.forms[0].useCollegeExamCalendar.value == '1') {
            var targetADVERTISE_DIV = document.getElementById('ADVERTISE_DIV');
            var targetPROGRAM_CD = document.getElementById('PROGRAM_CD');
            var targetFORM_CD = document.getElementById('FORM_CD');
            var targetL_CD = document.getElementById('L_CD');
            var targetS_CD = document.getElementById('S_CD');
        }
        var targetLIMIT_DATE_WINDOW = document.getElementById('LIMIT_DATE_WINDOW');
        var targetLIMIT_DATE_MAIL = document.getElementById('LIMIT_DATE_MAIL');
        var targetLIMIT_MAIL_DIV = document.getElementById('LIMIT_MAIL_DIV');
        var targetSTAT_DATE1 = document.getElementById('STAT_DATE1');
        var targetSTAT_DATE3 = document.getElementById('STAT_DATE3');
        var targetSCHOOL_NAME = document.getElementById('SCHOOL_NAME');
        var targetFACULTYNAME = document.getElementById('FACULTYNAME');
        var targetDEPARTMENTNAME = document.getElementById('DEPARTMENTNAME');
        var targetZIPCD = document.getElementById('ZIPCD');
        var targetADDR1 = document.getElementById('ADDR1');
        var targetADDR2 = document.getElementById('ADDR2');
        var targetTELNO = document.getElementById('TELNO');
        var targetSCHOOL_GROUP_NAME = document.getElementById('SCHOOL_GROUP_NAME');
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            var targetSCHOOL_CATEGORY_CD = document.getElementById('SCHOOL_CATEGORY_CD');
            var targetTOKYO_L_CD = document.getElementById('TOKYO_L_CD');
            var targetTOKYO_M_CD = document.getElementById('TOKYO_M_CD');
        }

        var response = httpObj.responseText;
        var responseArray = response.split('::');
        //入試カレンダーの使用
        if (document.forms[0].useCollegeExamCalendar.value == '1') {
            targetADVERTISE_DIV.innerHTML = responseArray[0];
            targetPROGRAM_CD.innerHTML = responseArray[1];
            targetFORM_CD.innerHTML = responseArray[2];
            targetL_CD.innerHTML = responseArray[3];
            targetS_CD.innerHTML = responseArray[4];
        }
        targetLIMIT_DATE_WINDOW.innerHTML = responseArray[5];
        targetLIMIT_DATE_MAIL.innerHTML = responseArray[6];
        targetLIMIT_MAIL_DIV.innerHTML = responseArray[7];
        targetSTAT_DATE1.innerHTML = responseArray[8];
        targetSTAT_DATE3.innerHTML = responseArray[9];
        targetSCHOOL_NAME.innerHTML = responseArray[10];
        targetFACULTYNAME.innerHTML = responseArray[11];
        targetDEPARTMENTNAME.innerHTML = responseArray[12];
        targetZIPCD.innerHTML = responseArray[13];
        targetADDR1.innerHTML = responseArray[14];
        targetADDR2.innerHTML = responseArray[15];
        targetTELNO.innerHTML = responseArray[16];
        targetSCHOOL_GROUP_NAME.innerHTML = responseArray[17];
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            targetSCHOOL_CATEGORY_CD.innerHTML = responseArray[18];
            targetTOKYO_L_CD.innerHTML = responseArray[19];
            targetTOKYO_M_CD.innerHTML = responseArray[20];
        }
    }
}
/************************** Ajax ***********************************/

//学校検索イベント10（確定）
function collegeSelectEvent10() {
    var search10 = document.forms[0].SEARCH10.value;
    document.forms[0].SCHOOL_CD.value = zeroPadding(search10.substr(0, 4), 8, true);
    document.forms[0].FACULTYCD.value = zeroPadding(search10.substr(4, 2), 3, false);
    document.forms[0].DEPARTMENTCD.value = zeroPadding(search10.substr(6, 2), 3, false);

    knjAjax3('subform2_college');
}

function zeroPadding(num, length, retAll0) {
    var retStr = ('0000000000' + num).slice(-length);
    if (!retAll0) {
        if (retStr * 1 == 0) {
            retStr = '';
        }
    }
    return retStr;
}

//学校検索イベント（確定）
function collegeSelectEvent3() {
    if (document.forms[0].SEARCH10) {
        document.forms[0].SEARCH10.value = '';
    }
    knjAjax3('subform2_college');
}
/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax3(cmd) {
    //この区間は送信処理、
    var sendData = '';
    var seq = '';
    var form_datas = document.forms[0];

    sendData = 'cmd=' + cmd;
    sendData += '&SCHREGNO=' + form_datas.SCHREGNO.value;
    sendData += '&SEQ=' + form_datas.SEQ.value;
    sendData += '&SCHOOL_CD=' + form_datas.SCHOOL_CD.value;
    sendData += '&FACULTYCD=' + form_datas.FACULTYCD.value;
    sendData += '&DEPARTMENTCD=' + form_datas.DEPARTMENTCD.value;
    sendData += '&ADVERTISE_DIV=';
    sendData += '&PROGRAM_CD=';
    sendData += '&FORM_CD=';
    sendData += '&L_CD=';
    sendData += '&S_CD=';
    sendData += '&LIMIT_DATE_WINDOW=';
    sendData += '&LIMIT_DATE_MAIL=';
    sendData += '&LIMIT_MAIL_DIV=';
    sendData += '&STAT_DATE1=';
    sendData += '&STAT_DATE3=';
    //東京都集計用
    if (form_datas.useTokyotoShinroTyousasyo.value == '1') {
        sendData += '&clicBtn=1';
        sendData += '&SCHOOL_CATEGORY_CD=' + form_datas.SCHOOL_CATEGORY_CD.value;
        sendData += '&TOKYO_L_CD=' + form_datas.TOKYO_L_CD.value;
        sendData += '&TOKYO_M_CD=' + form_datas.TOKYO_M_CD.value;
    }

    httpObj = createXmlHttp();
    httpObj.onreadystatechange = statusCheck3;
    httpObj.open('GET', 'knje360index.php?' + sendData, true); //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null); //GETメソッドを使う(ブラウザのせいなのかは不明)
}

/* XMLHttpRequest生成 */
function createXmlHttp() {
    if (document.all) {
        return new ActiveXObject('Microsoft.XMLHTTP');
    } else if (document.implementation) {
        return new XMLHttpRequest();
    } else {
        return null;
    }
}

function statusCheck3() {
    //サーバからの応答をチェック
    /******** httpObj.readyState *******/ /********** httpObj.status *********/
    /*  0:初期化されていない           */ /*  200:OK                         */
    /*  1:読込み中                     */ /*  403:アクセス拒否               */
    /*  2:読込み完了                   */ /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */ /***********************************/
    /*  4:準備完了                     */
    /***********************************/

    if (httpObj.readyState == 4 && httpObj.status == 200) {
        //入試カレンダーの使用
        if (document.forms[0].useCollegeExamCalendar.value == '1') {
            var targetADVERTISE_DIV = document.getElementById('ADVERTISE_DIV');
            var targetPROGRAM_CD = document.getElementById('PROGRAM_CD');
            var targetFORM_CD = document.getElementById('FORM_CD');
            var targetL_CD = document.getElementById('L_CD');
            var targetS_CD = document.getElementById('S_CD');
        }
        var targetLIMIT_DATE_WINDOW = document.getElementById('LIMIT_DATE_WINDOW');
        var targetLIMIT_DATE_MAIL = document.getElementById('LIMIT_DATE_MAIL');
        var targetLIMIT_MAIL_DIV = document.getElementById('LIMIT_MAIL_DIV');
        var targetSTAT_DATE1 = document.getElementById('STAT_DATE1');
        var targetSTAT_DATE3 = document.getElementById('STAT_DATE3');
        var targetSCHOOL_NAME = document.getElementById('SCHOOL_NAME');
        var targetFACULTYNAME = document.getElementById('FACULTYNAME');
        var targetDEPARTMENTNAME = document.getElementById('DEPARTMENTNAME');
        var targetZIPCD = document.getElementById('ZIPCD');
        var targetADDR1 = document.getElementById('ADDR1');
        var targetADDR2 = document.getElementById('ADDR2');
        var targetTELNO = document.getElementById('TELNO');
        var targetSCHOOL_GROUP_NAME = document.getElementById('SCHOOL_GROUP_NAME');
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            var targetSCHOOL_CATEGORY_CD = document.getElementById('SCHOOL_CATEGORY_CD');
            var targetTOKYO_L_CD = document.getElementById('TOKYO_L_CD');
            var targetTOKYO_M_CD = document.getElementById('TOKYO_M_CD');
        }

        var response = httpObj.responseText;
        var responseArray = response.split('::');
        //入試カレンダーの使用
        if (document.forms[0].useCollegeExamCalendar.value == '1') {
            targetADVERTISE_DIV.innerHTML = responseArray[0];
            targetPROGRAM_CD.innerHTML = responseArray[1];
            targetFORM_CD.innerHTML = responseArray[2];
            targetL_CD.innerHTML = responseArray[3];
            targetS_CD.innerHTML = responseArray[4];
        }
        targetLIMIT_DATE_WINDOW.innerHTML = responseArray[5];
        targetLIMIT_DATE_MAIL.innerHTML = responseArray[6];
        targetLIMIT_MAIL_DIV.innerHTML = responseArray[7];
        targetSTAT_DATE1.innerHTML = responseArray[8];
        targetSTAT_DATE3.innerHTML = responseArray[9];
        targetSCHOOL_NAME.innerHTML = responseArray[10];
        targetFACULTYNAME.innerHTML = responseArray[11];
        targetDEPARTMENTNAME.innerHTML = responseArray[12];
        targetZIPCD.innerHTML = responseArray[13];
        targetADDR1.innerHTML = responseArray[14];
        targetADDR2.innerHTML = responseArray[15];
        targetTELNO.innerHTML = responseArray[16];
        targetSCHOOL_GROUP_NAME.innerHTML = responseArray[17];
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            targetSCHOOL_CATEGORY_CD.innerHTML = responseArray[18];
            targetTOKYO_L_CD.innerHTML = responseArray[19];
            targetTOKYO_M_CD.innerHTML = responseArray[20];
        }

        //ゼロ埋め
        if (document.forms[0].SCHOOL_CD.value != '') {
            document.forms[0].SCHOOL_CD.value = ('0000000' + document.forms[0].SCHOOL_CD.value).slice(-8);
        }
        if (document.forms[0].FACULTYCD.value != '') {
            document.forms[0].FACULTYCD.value = ('00' + document.forms[0].FACULTYCD.value).slice(-3);
        }
        if (document.forms[0].DEPARTMENTCD.value != '') {
            document.forms[0].DEPARTMENTCD.value = ('00' + document.forms[0].DEPARTMENTCD.value).slice(-3);
        }
    }
}
/************************** Ajax ***********************************/
