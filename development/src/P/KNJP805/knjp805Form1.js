function btn_submit(cmd) {

    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);
    document.forms[0].windowHeight.value = bodyHeight;

    if (cmd == "update" || cmd == "updateTest") {
        //事務担当
        if(document.forms[0].REMINDER_STAFFCD.value == "") {
            alert('事務担当を指定して下さい。');
            return false;
        }

        //印刷チェックが一つ以上入っているか
        var checkFlg = false;
        re = new RegExp("GO_PRINT:");
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            if (obj_updElement.name.match(re) && obj_updElement.checked == true) {
                checkFlg = true;
                //出力文面nullチェック
                var setId = obj_updElement.name.split(':')[1];
                if (document.forms[0]["DOCUMENTCD-"+ setId].value == "") {
                    alert('出力文面を指定してください。');
                    return false;
                }
            }
        }
        if (!checkFlg) {
            alert('最低ひとつチェックを入れてください。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//画面リサイズ
function submit_reSize() {
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.getElementById("tbody").style.height = bodyHeight - 170;
}

function allCheck(obj, targetClass){
    $('.' + targetClass).prop('checked', obj.checked);
}
function setDocument(){
    $('.setDocument select').val(document.forms[0].DEFAULT_DOCUMENT.value);
}
//印刷
function newwin(SERVLET_URL, cmd){
    document.forms[0].SENDCMD.value = cmd;

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
