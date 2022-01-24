$(window).on('load',function(){
    $('html,body').animate({ scrollTop: 0 }, '1');
});

//CSV処理画面呼び出し
function myBtnWopen() {
    //年度、学年、学期を引き渡す。学籍番号までは不要。利用画面仕様から、校種は"P"固定としている。
    var pYear = document.forms[0].PARAMYEAR;
    var pGrade = document.forms[0].PARAMGRADE;
    var pSemester = document.forms[0].SEMESTER;
    var pHrClass = document.forms[0].PARAMHRCLASS;
    var pFieldsize = document.forms[0].FIELDSIZE;
    var pWidth = screen.availWidth;
    var pHeight = screen.availHeight;
    var pRECACTTBL = document.forms[0].HID_RECACTVIDLIST;
    pRECACTTBL = pRECACTTBL.value.replace(/,/g, '-');
    wopen("../../X/KNJX_D139I/knjx_d139iindex.php?FIELDSIZE=" + pFieldsize.value + "&SCHOOL_KIND=P&YEAR=" + pYear.value + "&GRADEHR=" + pGrade.value + "" + pHrClass.value + "&SEMESTER=" + pSemester.value + "&RECACTTBL=" + pRECACTTBL, "SUBWIN2", 0, 0, pWidth, pHeight);
}

//サブミット
function btn_submit(cmd) {

    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'attendRemark'){      //出欠備考参照
        var attBttn = document.forms[0].btn_attendRemark.getBoundingClientRect();
        var setY    = attBttn.top + window.pageYOffset - 290;
        loadwindow('knjd139iindex.php?cmd=attendRemark', 0, setY, 500, 200);
        return true;
    }

    if (cmd == 'update') {
        var idlist = document.forms[0].HID_RECACTVIDLIST.value.split(",");
        var setArr = document.forms[0].HID_RECACTVNAMELIST.value.split(",");
        for (var i = 0; i < setArr.length; i++) {
            if (document.getElementById(setArr[i]) != null) {
                var findflg = false;
                for (ii = 0;ii < idlist.length;ii++) {
                    if (idlist[ii] == document.getElementById(setArr[i]).value) {
                        findflg = true;
                    }
                }
                if (document.getElementById(setArr[i]).value != "" && !findflg) {
                    //この後、onblur処理が動くので、updateは中止するが、メッセージは出さない。
                    return false;
                }
            }
        }

        document.forms[0].btn_update.disabled = "true";
        document.forms[0].btn_up_pre.disabled = "true";
        document.forms[0].btn_up_next.disabled = "true";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function cntrtn(txt) {
    n = txt.match(/\r/g);
    if (n) len = n.length + 1; else len = 1;
    return len;
}

//入力チェック
function chkrecactv(inputtxt) {
    if (inputtxt.value == "") {
        return true;
    }
    var idlist = document.forms[0].HID_RECACTVIDLIST.value.split(",");
    var findflg = false;
    for (ii = 0;ii < idlist.length;ii++) {
        if (idlist[ii] == inputtxt.value) {
            findflg = true;
        }
    }
    if (!findflg) {
        alert('{rval MSG901}');
        inputtxt.focus();
        return false;
    }
    return true;
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_RECACTVNAMELIST.value.split(",");
        var tmp = obj.id;
        var tmpArr = new Array();
        var index = 0;
        for (var i = 0; i < setArr.length; i++) {
            tmpArr[i] = setArr[i];
            if (tmp == setArr[i]) {
                index = i;
            }
        }

        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i > 0; i--) {
                    targetId = tmpArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (tmpArr.length - 1)) {
                index++;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i < (tmpArr.length - 1); i++) {
                    targetId = tmpArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        }
        document.getElementById(targetId).focus();
        document.getElementById(targetId).select();
        return false;
    }
}
