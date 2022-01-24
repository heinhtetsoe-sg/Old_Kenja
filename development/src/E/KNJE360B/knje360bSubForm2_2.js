function btn_submit(cmd)
{
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    }

    attribute3       = document.forms[0].selectdata;
    attribute3.value = "";
    sep              = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep              = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//受験方式コンボで、NAMESPARE1が立っているもの選択時のみ表示
function changeDispSh(obj)
{
    var shArr = new Array();
    shArr     = document.forms[0].SH_ARR.value.split(',');

    for (var i=0; i < shArr.length; i++) {
        if (obj.value == shArr[i]) {
            document.getElementById("shDisp").style.display = "inline";
            return;
        } else {
            document.getElementById("shDisp").style.display = "none";
        }
    }
    return;
}

function keyChangeEntToTab(obj, nextObj)
{
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

function check_all(obj)
{
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/^RCHECK/)) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function doSubmit()
{
    var rcheckArray = new Array();
    var checkFlag   = false;
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/^RCHECK/) && document.forms[0].elements[i].name != "RCHECK_ALL") {
            rcheckArray.push(document.forms[0].elements[i]);
        }
    }
    for (var k = 0; k < rcheckArray.length; k++) {
        if (rcheckArray[k].checked) {
            checkFlag = true;
            break;
        }
    }
    if (!checkFlag) {
        alert("最低ひとつチェックを入れてください。");
        return false;
    }

    alert('{rval MSG102}');
    attribute3       = document.forms[0].selectdata;
    attribute3.value = "";
    sep              = "";
    if (document.forms[0].left_select.length==0 && document.forms[0].right_select.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep              = ",";
    }

    if (document.forms[0].TOROKU_DATE.value == "") {
        alert('データを入力してください。\n　　（登録日）');
        return true;
    }

    var date   = document.forms[0].TOROKU_DATE.value.split('/');
    var sdate  = document.forms[0].SDATE.value.split('/');
    var edate  = document.forms[0].EDATE.value.split('/');
    sdate_show = document.forms[0].SDATE.value;
    edate_show = document.forms[0].EDATE.value;

    if(   (new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2])))
       || (new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2]))))
    {
        alert('登録日が入力範囲外です。\n（' + sdate_show + '～' + edate_show + '）');
        return true;
    }

    document.forms[0].cmd.value = 'replace_update2';
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function move1(side)
{
    var temp1    = new Array();
    var temp2    = new Array();
    var tempa    = new Array();
    var tempb    = new Array();
    var tempaa   = new Array();
    var current1 = 0;
    var current2 = 0;
    var y        = 0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].right_select;
        attribute2 = document.forms[0].left_select;
    } else {
        attribute1 = document.forms[0].left_select;
        attribute2 = document.forms[0].right_select;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y         = current1++
        temp1[y]  = attribute2.options[i].value;
        tempa[y]  = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y         = current1++
            temp1[y]  = attribute1.options[i].value;
            tempa[y]  = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y         = current2++
            temp2[y]  = attribute1.options[i].value;
            tempb[y]  = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i]       = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text  =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1,attribute1);

    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i]       = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text  =  tempb[i];
        }
    }
}

function moves(sides)
{
    var temp5    = new Array();
    var tempc    = new Array();
    var tempaa   = new Array();
    var current5 = 0;
    var z        = 0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].right_select;
        attribute6 = document.forms[0].left_select;
    } else {
        attribute5 = document.forms[0].left_select;
        attribute6 = document.forms[0].right_select;
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z         = current5++
        temp5[z]  = attribute6.options[i].value;
        tempc[z]  = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value + "," + z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z         = current5++
        temp5[z]  = attribute5.options[i].value;
        tempc[z]  = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value + "," + z;
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i]       = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text  =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);
}

/************************** Ajax ***********************************/

/* XMLHttpRequest生成 */
function createXmlHttp()
{
    if (document.all) {
        return new ActiveXObject("Microsoft.XMLHTTP");
    } else if (document.implementation) {
        return new XMLHttpRequest();
    } else {
        return null;
    }
}

//学校検索イベント（募集区分等）チェンジコンボ
function collegeSelectEvent2()
{
    knjAjax2('replace2_college');
}
/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax2(cmd)//この区間は送信処理、
{
    var sendData   = '';
    var seq        = '';
    var form_datas = document.forms[0];

    sendData  = "cmd="                     + cmd;
    sendData += "&SCHREGNO="               + form_datas.SCHREGNO.value;
    sendData += "&SEQ="                    + form_datas.SEQ.value;
    sendData += "&SCHOOL_CD="              + form_datas.SCHOOL_CD.value;
    sendData += "&FACULTYCD="              + form_datas.FACULTYCD.value;
    sendData += "&DEPARTMENTCD="           + form_datas.DEPARTMENTCD.value;

    //東京都集計用
    if (form_datas.useTokyotoShinroTyousasyo.value == '1') {
        sendData += "&SCHOOL_CATEGORY_CD=" + form_datas.SCHOOL_CATEGORY_CD.value;
        sendData += "&TOKYO_L_CD="         + form_datas.TOKYO_L_CD.value;
        sendData += "&TOKYO_M_CD="         + form_datas.TOKYO_M_CD.value;
    }

    // httpObj = new ActiveXObject("Microsoft.XMLHTTP");
    httpObj = createXmlHttp();
    httpObj.onreadystatechange = statusCheck2;
    httpObj.open("GET","knje360bindex.php?" + sendData,true);  //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null);                                        //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck2()//サーバからの応答をチェック
{
    /******** httpObj.readyState *******/  /********** httpObj.status *********/
    /*  0:初期化されていない           */  /*  200:OK                         */
    /*  1:読込み中                     */  /*  403:アクセス拒否               */
    /*  2:読込み完了                   */  /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */  /***********************************/
    /*  4:準備完了                     */
    /***********************************/

    if ((httpObj.readyState == 4) && (httpObj.status == 200)) {
        var targetSCHOOL_NAME       = document.getElementById("SCHOOL_NAME");
        var targetFACULTYNAME       = document.getElementById("FACULTYNAME");
        var targetDEPARTMENTNAME    = document.getElementById("DEPARTMENTNAME");
        var targetZIPCD             = document.getElementById("ZIPCD");
        var targetADDR1             = document.getElementById("ADDR1");
        var targetADDR2             = document.getElementById("ADDR2");
        var targetTELNO             = document.getElementById("TELNO");
        var targetSCHOOL_GROUP_NAME = document.getElementById("SCHOOL_GROUP_NAME");
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            var targetSCHOOL_CATEGORY_CD = document.getElementById("SCHOOL_CATEGORY_CD");
            var targetTOKYO_L_CD         = document.getElementById("TOKYO_L_CD");
            var targetTOKYO_M_CD         = document.getElementById("TOKYO_M_CD");
        }

        var response      = httpObj.responseText;
        var responseArray = response.split("::");

        targetSCHOOL_NAME.innerHTML            = responseArray[0];
        targetFACULTYNAME.innerHTML            = responseArray[1];
        targetDEPARTMENTNAME.innerHTML         = responseArray[2];
        targetZIPCD.innerHTML                  = responseArray[3];
        targetADDR1.innerHTML                  = responseArray[4];
        targetADDR2.innerHTML                  = responseArray[5];
        targetTELNO.innerHTML                  = responseArray[6];
        targetSCHOOL_GROUP_NAME.innerHTML      = responseArray[7];
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            targetSCHOOL_CATEGORY_CD.innerHTML = responseArray[8];
            targetTOKYO_L_CD.innerHTML         = responseArray[9];
            targetTOKYO_M_CD.innerHTML         = responseArray[10];
        }
    }
}

/************************** Ajax ***********************************/

//学校検索イベント（確定）
function collegeSelectEvent3()
{
    knjAjax3('replace2_college');
}
/************************** Ajax(IE以外のブラウザは考慮していません。) **************************/
function knjAjax3(cmd)//この区間は送信処理、
{
    var sendData   = '';
    var seq        = '';
    var form_datas = document.forms[0];

    sendData  = "cmd="                     + cmd;
    sendData += "&SCHREGNO="               + form_datas.SCHREGNO.value;
    sendData += "&SEQ="                    + form_datas.SEQ.value;
    sendData += "&SCHOOL_CD="              + form_datas.SCHOOL_CD.value;
    sendData += "&FACULTYCD="              + form_datas.FACULTYCD.value;
    sendData += "&DEPARTMENTCD="           + form_datas.DEPARTMENTCD.value;
    //東京都集計用
    if (form_datas.useTokyotoShinroTyousasyo.value == '1') {
        sendData += "&clicBtn=1";
        sendData += "&SCHOOL_CATEGORY_CD=" + form_datas.SCHOOL_CATEGORY_CD.value;
        sendData += "&TOKYO_L_CD="         + form_datas.TOKYO_L_CD.value;
        sendData += "&TOKYO_M_CD="         + form_datas.TOKYO_M_CD.value;
    }

    httpObj = createXmlHttp();
    httpObj.onreadystatechange = statusCheck3;
    httpObj.open("GET","knje360bindex.php?" + sendData,true);  //POSTメソッドで送るとリクエストヘッダがおかしくなるので
    httpObj.send(null);                                        //GETメソッドを使う(ブラウザのせいなのかは不明)
}

function statusCheck3()//サーバからの応答をチェック
{
    /******** httpObj.readyState *******/  /********** httpObj.status *********/
    /*  0:初期化されていない           */  /*  200:OK                         */
    /*  1:読込み中                     */  /*  403:アクセス拒否               */
    /*  2:読込み完了                   */  /*  404:ファイルが存在しない       */
    /*  3:操作可能                     */  /***********************************/
    /*  4:準備完了                     */
    /***********************************/

    if ((httpObj.readyState == 4) && (httpObj.status == 200)) {
        var targetSCHOOL_NAME            = document.getElementById("SCHOOL_NAME");
        var targetFACULTYNAME            = document.getElementById("FACULTYNAME");
        var targetDEPARTMENTNAME         = document.getElementById("DEPARTMENTNAME");
        var targetZIPCD                  = document.getElementById("ZIPCD");
        var targetADDR1                  = document.getElementById("ADDR1");
        var targetADDR2                  = document.getElementById("ADDR2");
        var targetTELNO                  = document.getElementById("TELNO");
        var targetSCHOOL_GROUP_NAME      = document.getElementById("SCHOOL_GROUP_NAME");
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            var targetSCHOOL_CATEGORY_CD = document.getElementById("SCHOOL_CATEGORY_CD");
            var targetTOKYO_L_CD         = document.getElementById("TOKYO_L_CD");
            var targetTOKYO_M_CD         = document.getElementById("TOKYO_M_CD");
        }

        var response = httpObj.responseText;
        var responseArray = response.split("::");

        targetSCHOOL_NAME.innerHTML            = responseArray[0];
        targetFACULTYNAME.innerHTML            = responseArray[1];
        targetDEPARTMENTNAME.innerHTML         = responseArray[2];
        targetZIPCD.innerHTML                  = responseArray[3];
        targetADDR1.innerHTML                  = responseArray[4];
        targetADDR2.innerHTML                  = responseArray[5];
        targetTELNO.innerHTML                  = responseArray[6];
        targetSCHOOL_GROUP_NAME.innerHTML      = responseArray[7];
        //東京都集計用
        if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
            targetSCHOOL_CATEGORY_CD.innerHTML = responseArray[8];
            targetTOKYO_L_CD.innerHTML         = responseArray[9];
            targetTOKYO_M_CD.innerHTML         = responseArray[10];
        }

        //ゼロ埋め
        if (document.forms[0].SCHOOL_CD.value != "") {
            document.forms[0].SCHOOL_CD.value = ("0000000"+document.forms[0].SCHOOL_CD.value).slice(-8);
        }
        if (document.forms[0].FACULTYCD.value != "") {
            document.forms[0].FACULTYCD.value = ("00"+document.forms[0].FACULTYCD.value).slice(-3);
        }
        if (document.forms[0].DEPARTMENTCD.value != "") {
            document.forms[0].DEPARTMENTCD.value = ("00"+document.forms[0].DEPARTMENTCD.value).slice(-3);
        }
    }
}
/************************** Ajax ***********************************/
