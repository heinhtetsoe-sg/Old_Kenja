function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'fubanClear') {
        if (!confirm('新規管理番号を割振り、表示画面のデータをクリアします。'))
            return false;
    }

    if (cmd == 'delete' || cmd == 'sendDel') {
        if (!confirm('{rval MSG101}'))
            return false;
    }

    if (cmd == 'add' || cmd == 'update') {
        if (document.forms[0].RECRUIT_NO.value == "") {
            alert('{rval MSG301}' + '\n管理番号');
            return true;
        }
        if (document.forms[0].NAME.value == "") {
            alert('{rval MSG301}' + '\n氏名');
            return true;
        }
        if (document.forms[0].NAME_KANA.value == "") {
            alert('{rval MSG301}' + '\n氏名かな');
            return true;
        }
        if (document.forms[0].GRADE.value == "") {
            alert('{rval MSG301}' + '\n学年');
            return true;
        }
    }

    //【新規】押下・・・【新規】を無効にする。
    if (cmd == 'fubanClear' || cmd == 'fuban') {
        document.forms[0].btn_fuban.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function add()
{
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].YEAR.length;
    var w = document.forms[0].YEAR_ADD.value

    if (w == "") {
        alert('{rval MSG901}\n数字を入力してください。');
        return false;
    }

    for (var i = 0; i < v; i++) {
        if (w == document.forms[0].YEAR.options[i].value) {
            alert("追加した年度は既に存在しています。");
            return false;
        }
    }
    document.forms[0].YEAR.options[v] = new Option();
    document.forms[0].YEAR.options[v].value = w;
    document.forms[0].YEAR.options[v].text = w;

    for (var i = 0; i < document.forms[0].YEAR.length; i++) {
        temp1[i] = document.forms[0].YEAR.options[i].value;
        tempa[i] = document.forms[0].YEAR.options[i].text;
    }
    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();

    //generating new options
    ClearList(document.forms[0].YEAR,document.forms[0].YEAR);
    if (temp1.length>0) {
        for (var i = 0; i < temp1.length; i++) {
            document.forms[0].YEAR.options[i] = new Option();
            document.forms[0].YEAR.options[i].value = temp1[i];
            document.forms[0].YEAR.options[i].text =  tempa[i];
            if (w == temp1[i]) {
                document.forms[0].YEAR.options[i].selected=true;
            }
        }
    } 
    //temp_clear();
}
function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function credit_total(obj, notChange, defDis) {
    var checkBef = obj.checked;
    if (obj.type == "checkbox" && notChange == "1") {
        obj.checked = checkBef ? false : true;
        alert('修得済みの科目は変更出来ません。');
        return;
    }
    var chktotal = 0;
    var chkval;
    var chktotalGet = 0;
    var chktotalJikougai = 0;
    reZoutan = new RegExp("^ZOUTAN" );
    if (document.forms[0].EXE_NENDO_PATERN.value == "1" && obj.name == "ADDCHK[]") {
        chkdata = obj.value
        chkdata_arr = chkdata.split(":");
        var soeji = chkdata_arr[0] + ':' + chkdata_arr[1] + ':' + chkdata_arr[2] + ':' + chkdata_arr[3];
        var syoukei = document.getElementById('VAL_TOTAL_COMP_' + soeji).value;
        document.getElementById('COMP').innerHTML = parseInt(document.getElementById('COMP').innerHTML) - parseInt(document.getElementById('ID_TOTAL_COMP_' + soeji).innerHTML);
        document.getElementById('GET').innerHTML = parseInt(document.getElementById('GET').innerHTML) - parseInt(document.getElementById('ID_TOTAL_COMP_' + soeji).innerHTML);
        document.getElementById('COMP_GET').innerHTML = parseInt(document.getElementById('COMP_GET').innerHTML) - parseInt(document.getElementById('ID_TOTAL_COMP_' + soeji).innerHTML);
        syoukei = syoukei > 0 ? syoukei : 0;
        var setVal = obj.checked ? chkdata_arr[4] : "";
        syoukei = parseInt(syoukei) + parseInt(setVal == "" ? 0 : setVal);
        document.getElementById('ID_TOTAL_COMP_' + soeji).innerHTML = syoukei;
        document.getElementById('ID_RISYUTYU_CREDIT_' + soeji).innerHTML = setVal;
        var compVal = document.getElementById('COMP').innerHTML;
        var getVal = document.getElementById('GET').innerHTML;
        var compGetVal = parseInt(document.getElementById('COMP_GET').innerHTML) + parseInt(syoukei);
        document.getElementById('COMP').innerHTML = parseInt(compVal) + syoukei;
        document.getElementById('GET').innerHTML = parseInt(getVal) + syoukei;
    } else {
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var taisyouObj = document.forms[0].elements[i];
            if (((taisyouObj.name == "ADDCHK[]" || taisyouObj.name == "KOUNIN[]") && taisyouObj.checked)
                  || taisyouObj.name.match(reZoutan)
               ) {

                chkdata = taisyouObj.value;

                chkdata_arr = chkdata.split(":");
                if (taisyouObj.name.match(reZoutan)) {
                    chkval = taisyouObj.value;
                } else {
                    chkval = chkdata_arr[4];
                }

                if (eval(chkval)) {
                    chktotal += parseInt(chkval);
                    if (taisyouObj.name == "ADDCHK[]") {
                       chktotalGet += parseInt(chkval);
                    } else {
                       chktotalJikougai += parseInt(chkval);
                    }
                }
            }
        }
        var comp = document.getElementById('COMP').innerHTML;
        if (document.forms[0].EXE_NENDO_PATERN.value == "1") {
            var compGetVal = parseInt(document.forms[0].SATEI_TANNI.value) + parseInt(comp) + parseInt(chktotalJikougai);
        } else {
            var compGetVal = parseInt(document.forms[0].SATEI_TANNI.value) + parseInt(comp) + parseInt(chktotalJikougai) + parseInt(chktotalGet);
        }
        document.getElementById('GET').innerHTML = chktotalGet;
        document.getElementById('JIKOUGAI').innerHTML = chktotalJikougai;
    }
    if (parseInt(document.forms[0].GRAD_CREDITS.value) <= compGetVal) {
        document.forms[0].GRD_YOTEI.checked = true;
    } else {
        document.forms[0].GRD_YOTEI.checked = false;
    }
    document.getElementById('COMP_GET').innerHTML = compGetVal;
    useCheckBoxChange(obj, defDis);
    return;
}

function useCheckBoxChange(obj, defDis) {

    re = new RegExp("^ZOUTAN" );
    var zoutanFlg = obj.name.match(re);
    var changeName1 = "ADDCHK[]";
    var changeName2 = "ZOUTAN";
    if (obj.name == "ADDCHK[]") {
        changeName1 = "KOUNIN[]";
        changeName2 = "ZOUTAN";
    } else if (obj.name.match(re)) {
        changeName1 = "ADDCHK[]";
        changeName2 = "KOUNIN[]";
    }

    for (var i=0; i < document.forms[0].elements.length; i++) {
        var taisyouObj = document.forms[0].elements[i];

        if (zoutanFlg == "ZOUTAN" && (taisyouObj.name == changeName1 || taisyouObj.name == changeName2)) {
            chkdata = taisyouObj.value;
            chkdata_arr = chkdata.split(":");
            var soeji = chkdata_arr[0] + ':' + chkdata_arr[1] + ':' + chkdata_arr[2] + ':' + chkdata_arr[3];
            zoutanSoeji = obj.name.replace("ZOUTAN", "");

            setDisabled(taisyouObj, changeName1, obj, soeji == zoutanSoeji, obj.value.length > 0);
            setDisabled(taisyouObj, changeName2, obj, soeji == zoutanSoeji, obj.value.length > 0);
        } else {
            setDisabled(taisyouObj, changeName1, obj, obj.value == taisyouObj.value, obj.checked);

            chkdata_arr = obj.value.split(":");
            var soeji = chkdata_arr[0] + ':' + chkdata_arr[1] + ':' + chkdata_arr[2] + ':' + chkdata_arr[3];
            sendDis = defDis ? true : obj.checked;
            setDisabled(taisyouObj, changeName2 + soeji, obj, true, sendDis);
        }
    }

    return;
}

function setDisabled(taisyouObj, changeName, obj, jouken, setDis) {
    if (taisyouObj.name == changeName && jouken
       ) {
        taisyouObj.disabled = setDis;
    }
}

//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

	//url = location.hostname;
    
	//document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    
    document.forms[0].action = action;
    document.forms[0].target = target;
}

