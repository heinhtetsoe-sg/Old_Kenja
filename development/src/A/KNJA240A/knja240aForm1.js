function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL){

    var obj1 = document.forms[0].DATE;
    if (obj1.value == '') {
        alert("日付が不正です。");
        obj1.focus();
        return false;
    }

    var semeDateArr = document.forms[0].SEME_DATE.value.split(':');
    var syoriDate   = document.forms[0].DATE.value.split('/'); //処理日

    var flag1 = 0;
    for (var i=0; i < semeDateArr.length; i++) {
        var sDate = semeDateArr[i].split(',')[0].split('-');
        var eDate = semeDateArr[i].split(',')[1].split('-');

        if(new Date(eval(sDate[0]),eval(sDate[1])-1,eval(sDate[2])) <= new Date(eval(syoriDate[0]),eval(syoriDate[1])-1,eval(syoriDate[2]))) {
            if(new Date(eval(syoriDate[0]),eval(syoriDate[1])-1,eval(syoriDate[2])) <= new Date(eval(eDate[0]),eval(eDate[1])-1,eval(eDate[2]))) {
                flag1 = i + 1; //学期セット
            }
        }
    }

    if (flag1 == "") {
        alert("指定範囲が学期外です。");
        obj1.focus();
        return;
    } else {
        document.forms[0].SEMESTER.value = flag1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
