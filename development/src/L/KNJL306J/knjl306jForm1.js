function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function time_check(time){
    switch(time.name){
        case "TIMEUPH":
            var h = toInteger(time.value);
            if (parseInt(h) < 0 || parseInt(h) > 23){
                alert("０から２３の数字を入力してください。");
                time.focus();
            }else{
                time.value = h;
            }
            break;
        case "TIMEUPM":
            var m = toInteger(time.value);
            if (parseInt(m) < 0 || parseInt(m) > 59){
                alert("０から５９の数字を入力してください。");
                time.focus();
            }else{
                time.value = m;
            }
            break;
		default :
            break;
    }
}

//印刷
function newwin(SERVLET_URL){
	if (document.forms[0].TEST_DATE1_FROM.value == ""
        || document.forms[0].TEST_DATE1_TO.value == ""){
		alert("一般出願日付を入力して下さい");
		return;
	}
    
    TEST_DATE_FROM1 = ""+document.forms[0].TEST_DATE_FROM1.value;
    TEST_DATE_TO1   = ""+document.forms[0].TEST_DATE_TO1.value;
    if (TEST_DATE_FROM1 == "" || TEST_DATE_TO1 == "") {
        alert("一般出願範囲を設定して下さい");
		return;
    }
    
    //日付範囲チェック
    input_from = document.forms[0].TEST_DATE1_FROM.value;
    input_from_ary = input_from.split("/");
    input_from_date = new Date(input_from_ary[0], input_from_ary[1]-1, input_from_ary[2]);
    
    input_to   = document.forms[0].TEST_DATE1_TO.value;
    input_to_ary = input_to.split("/");
    input_to_date = new Date(input_to_ary[0], input_to_ary[1]-1, input_to_ary[2]);
    
    chk_from_ary = TEST_DATE_FROM1.split("/");
    chk_from_date = new Date(chk_from_ary[0], chk_from_ary[1]-1, chk_from_ary[2]);
    
    chk_to_ary = TEST_DATE_TO1.split("/");
    chk_to_date = new Date(chk_to_ary[0], chk_to_ary[1]-1, chk_to_ary[2]);
    
    if (input_from_date > input_to_date) {
        alert("一般出願日付の範囲が矛盾しています。入力し直して下さい");
		return;
    }
    
    if ((input_from_date < chk_from_date || input_from_date > chk_to_date)
        || (input_to_date < chk_from_date || input_to_date > chk_to_date)) {
        alert("一般出願日付が範囲外です。入力し直して下さい");
		return;
    }
    
    if (document.forms[0].TEST_DATE2_FROM.value == ""
        || document.forms[0].TEST_DATE2_TO.value == ""){
		alert("帰国生出願日付を入力して下さい");
		return;
	}
    
    TEST_DATE_FROM2 = ""+document.forms[0].TEST_DATE_FROM2.value;
    TEST_DATE_TO2   = ""+document.forms[0].TEST_DATE_TO2.value;
    if (TEST_DATE_FROM2 == "" || TEST_DATE_TO2 == "") {
        alert("帰国生出願範囲を設定して下さい");
		return;
    }
    
    //日付範囲チェック
    input_from = document.forms[0].TEST_DATE2_FROM.value;
    input_from_ary = input_from.split("/");
    input_from_date = new Date(input_from_ary[0], input_from_ary[1]-1, input_from_ary[2]);
    
    input_to   = document.forms[0].TEST_DATE2_TO.value;
    input_to_ary = input_to.split("/");
    input_to_date = new Date(input_to_ary[0], input_to_ary[1]-1, input_to_ary[2]);
    
    chk_from_ary = TEST_DATE_FROM2.split("/");
    chk_from_date = new Date(chk_from_ary[0], chk_from_ary[1]-1, chk_from_ary[2]);
    
    chk_to_ary = TEST_DATE_TO2.split("/");
    chk_to_date = new Date(chk_to_ary[0], chk_to_ary[1]-1, chk_to_ary[2]);
    
    if (input_from_date > input_to_date) {
        alert("帰国生出願日付の範囲が矛盾しています。入力し直して下さい\n");
		return;
    }
    
    if ((input_from_date < chk_from_date || input_from_date > chk_to_date)
        || (input_to_date < chk_from_date || input_to_date > chk_to_date)) {
        alert("帰国生出願日付の範囲外です。入力し直して下さい");
		return;
    }
    
    action = document.forms[0].action;
    target = document.forms[0].target;

	url = location.hostname;
	//document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
