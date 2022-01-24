function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].APPLICANTDIV.value == ""){
        alert("入試制度を指定して下さい");
        return;
    }
    if (document.forms[0].TESTDIV.value == ""){
        alert("入試区分を指定して下さい");
        return;
    }
    if (document.forms[0].TESTDIV0.value == ""){
        alert("入試回数を指定して下さい");
        return;
    }

    if (document.forms[0].PRINT_DATE.value == "") {
        alert("通知日付が未入力です。");
        return;
    }
    var arr = ["A", "B", "C", "D", "E", "F", "G", "H"];
    var i, id1, id2, id3;
    for (i = 1; i <= 8; i++) {
        id1 = document.getElementById("OUTPUT" + i); 
        id2 = document.getElementById("OUTPUT" + arr[i - 1] + "3"); 
        id3 = document.getElementById("EXAMNO" + arr[i - 1]); 
        //console.log(id1, id2, id3);
        if (id1 && id1.checked &&
            id2 && id2.checked &&
            id3 && id3.value == "") {
            alert("受験番号を入力して下さい");
            return;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

