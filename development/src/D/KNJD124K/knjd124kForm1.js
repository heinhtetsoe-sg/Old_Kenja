<!--kanji=漢字-->
<!-- <?php # $RCSfile: knjd124kForm1.js,v $ ?> -->
<!-- <?php # $Revision: 68690 $ ?> -->
<!-- <?php # $Date: 2019-07-12 15:14:14 +0900 (金, 12 7 2019) $ ?> -->

function btn_submit(cmd, electdiv) {

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    } else if (cmd == 'update'){

        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                var str = e.value;
                var nam = e.name;
                //英小文字から大文字へ自動変換
                if (str.match(/a|b|c/)) { 
                    e.value = str.toUpperCase();
                    str = str.toUpperCase();
                }

                //評定
                if (nam.match(/STATUS9./)) {
                    //選択科目
                    if (electdiv != '0' && !str.match(/A|B|C/)) { 
                        alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。\n（評定）');
                        //background_color(e);
                        return;
                    } else if (electdiv == '0' && !str.match(/1|2|3|4|5/)) {
                        alert('{rval MSG901}'+'「1～5」を入力して下さい。\n（評定）');
                        //background_color(e);
                        return;
                    }

                //観点1～5
                } else {
                    if (!str.match(/A|B|C/) && str != '-' && str != '/') { 
                        alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。\n（観点①～⑤）');
                        //background_color(e);
                        return;
                    }
                }
            }
        }

        clickedBtnUdpate(true);
    }
    document.forms[0].btn_update.disabled = true;
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新時、サブミットする項目使用不可
function clickedBtnUdpate(disFlg) {
    var sk_flg = false;
    if (document.forms[0].use_prg_schoolkind.value == "1" && document.forms[0].SCHOOL_KIND.type != "hidden") {
        sk_flg = true;
    }

    if (disFlg) {
        if (sk_flg) document.forms[0].H_SCHOOL_KIND.value = document.forms[0].SCHOOL_KIND.value;
        document.forms[0].H_SEMESTER.value = document.forms[0].SEMESTER.value;
        document.forms[0].H_CLASSCD.value = document.forms[0].CLASSCD.value;
        document.forms[0].H_CHAIRCD.value = document.forms[0].CHAIRCD.value;
    } else {
        if (sk_flg) document.forms[0].SCHOOL_KIND.value = document.forms[0].H_SCHOOL_KIND.value;
        document.forms[0].SEMESTER.value = document.forms[0].H_SEMESTER.value;
        document.forms[0].CLASSCD.value = document.forms[0].H_CLASSCD.value;
        document.forms[0].CHAIRCD.value = document.forms[0].H_CHAIRCD.value;
    }
    if (sk_flg) document.forms[0].SCHOOL_KIND.disabled = disFlg;
    document.forms[0].SEMESTER.disabled = disFlg;
    document.forms[0].CLASSCD.disabled = disFlg;
    document.forms[0].CHAIRCD.disabled = disFlg;
    document.forms[0].btn_reset.disabled = disFlg;
    document.forms[0].btn_end.disabled = disFlg;
    document.forms[0].btn_print.disabled = disFlg;
}

function background_color(obj){
//    obj.style.background='#ffff99';
    obj.style.background='#ffffff';
}
//入力チェック
function calc(obj, electdiv){

    var str = obj.value;
    var nam = obj.name;
    
    //空欄
    if (str == '') { 
        return;
    }

    //英小文字から大文字へ自動変換
    if (str.match(/a|b|c/)) { 
        obj.value = str.toUpperCase();
        str = str.toUpperCase();
    }

    //評定
    if (nam.match(/STATUS9./)) {
        //選択科目
        if (electdiv != '0' && !str.match(/A|B|C/)) { 
            alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。');
            obj.focus();//2006/04/14
            background_color(obj);//2006/04/14
            return;
        } else if (electdiv == '0' && !str.match(/1|2|3|4|5/)) {
            alert('{rval MSG901}'+'「1～5」を入力して下さい。');
            obj.focus();//2006/04/14
            background_color(obj);//2006/04/14
            return;
        }

    //観点1～5
    } else {
        if (!str.match(/A|B|C/) && str != '-' && str != '/') { 
            alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。');
            obj.focus();//2006/04/14
            background_color(obj);//2006/04/14
            return;
        }
    }
}
//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].CHAIRCD.value == '') {
        alert('クラス・講座を指定してください。');
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//観点①～⑤へマウスを乗せた場合、観点名称をチップヘルプで表示
function ViewcdMousein(e, msg_no){
    var msg = "";
    if (msg_no==1) msg = document.forms[0].VIEWCD1.value;
    if (msg_no==2) msg = document.forms[0].VIEWCD2.value;
    if (msg_no==3) msg = document.forms[0].VIEWCD3.value;
    if (msg_no==4) msg = document.forms[0].VIEWCD4.value;
    if (msg_no==5) msg = document.forms[0].VIEWCD5.value;

    x = event.clientX+document.body.scrollLeft;
    y = event.clientY+document.body.scrollTop;
    document.all("lay").innerHTML = msg;
    document.all["lay"].style.position = "absolute";
    document.all["lay"].style.left = x+5;
    document.all["lay"].style.top = y+10;
    document.all["lay"].style.padding = "4px 3px 3px 8px";
    document.all["lay"].style.border = "1px solid";
    document.all["lay"].style.visibility = "visible";
    document.all["lay"].style.background = "#ccffff";
    //document.all["lay"].style.cursor = "hand";
}

function ViewcdMouseout(){
    document.all["lay"].style.visibility = "hidden";
}
