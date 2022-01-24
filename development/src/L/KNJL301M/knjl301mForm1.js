function btn_submit(cmd) {

    if(cmd == "csv") {
        if (document.forms[0].APPLICANTDIV.value == ""){
            alert("入試制度を指定して下さい");
            return;
        }

        if(document.forms[0].TAISHOU[1].checked == true) {
            var s_examno = document.forms[0].S_EXAMNO.value;
            var e_examno = document.forms[0].E_EXAMNO.value;

            //入力チェック
            if(s_examno == "" || e_examno == ""){
                alert('{rval MSG301}\n( 受験番号 )');
                if(s_examno == ""){
                    document.forms[0].S_EXAMNO.value = "";
                    document.forms[0].S_EXAMNO.focus();
                } else {
                    document.forms[0].E_EXAMNO.value = "";
                    document.forms[0].E_EXAMNO.focus();
                }
                return;
            }
            //大小チェック
            if(parseInt(s_examno, 10) > parseInt(e_examno, 10)){
                alert("受験番号の大小が不正です。");
                document.forms[0].S_EXAMNO.value = "";
                document.forms[0].E_EXAMNO.value = "";
                document.forms[0].S_EXAMNO.focus();
                return;
            }
            //ゼロ埋め
            if(s_examno != ""){
                sno = ("00000" + s_examno).slice(-5);
                document.forms[0].S_EXAMNO.value = sno;
            }
            if(e_examno != ""){
                eno = ("00000" + e_examno).slice(-5);
                document.forms[0].E_EXAMNO.value = eno;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {

    if(document.forms[0].TAISHOU[1].checked == true) {
        var s_examno = document.forms[0].S_EXAMNO.value;
        var e_examno = document.forms[0].E_EXAMNO.value;

        //入力チェック
        if(s_examno == "" || e_examno == ""){
            alert('{rval MSG301}\n( 受験番号 )');
            if(s_examno == ""){
                document.forms[0].S_EXAMNO.value = "";
                document.forms[0].S_EXAMNO.focus();
            } else {
                document.forms[0].E_EXAMNO.value = "";
                document.forms[0].E_EXAMNO.focus();
            }
            return;
        }
        //大小チェック
        if(parseInt(s_examno, 10) > parseInt(e_examno, 10)){
            alert("受験番号の大小が不正です。");
            document.forms[0].S_EXAMNO.value = "";
            document.forms[0].E_EXAMNO.value = "";
            document.forms[0].S_EXAMNO.focus();
            return;
        }
        //ゼロ埋め
        if(s_examno != ""){
            sno = ("00000" + s_examno).slice(-5);
            document.forms[0].S_EXAMNO.value = sno;
        }
        if(e_examno != ""){
            eno = ("00000" + e_examno).slice(-5);
            document.forms[0].E_EXAMNO.value = eno;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    if(document.forms[0].TAISHOU[1].checked == true) {
        if(s_examno != ""){
            document.forms[0].S_EXAMNO.value = parseInt(s_examno, 10);
        }
        if(e_examno != ""){
            document.forms[0].E_EXAMNO.value = parseInt(e_examno, 10);
        }
    }

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//disabled
function OptionUse(obj) {
    if(document.forms[0].TAISHOU[1].checked == true) {
        document.forms[0].S_EXAMNO.disabled = false;
        document.forms[0].E_EXAMNO.disabled = false;
    } else {
        document.forms[0].S_EXAMNO.disabled = true;
        document.forms[0].E_EXAMNO.disabled = true;
    }
}
