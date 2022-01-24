function btn_submit(cmd) {
    if (cmd == 'update') {
        if (document.forms[0].APPDATE.value == "") {
            alert('講座適用開始日を指定して下さい。');
            return;
        }
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }

    top.main_frame.closeit();
    top.main_frame.document.forms[0].cmd.value = cmd;
    top.main_frame.document.forms[0].submit();
    return false;
}

function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
}

//同じ群で講座を複数選択しないようにチェック
function chkSameGroup(obj) {
    //チェックがONのときのみチェック処理をする
    if (obj.checked == true) {
        var no = obj.name.slice(5);

        var grplist_array = document.forms[0].GROUPLIST.value.split(',');
        var chrlist_array = document.forms[0].CHAIRLIST.value.split(',');

        var grpcnt = 0
        for (var i=0; i < document.forms[0].elements.length; i++) {

            if (document.forms[0].elements[i].name.slice(0,5) == "CHAIR") {

                if (document.forms[0].elements[i].checked == true) {

                    for (var j=0; j < chrlist_array.length; j++) {

                        if (document.forms[0].elements[i].value == chrlist_array[j]) {
                            //自身はカウントから対象外
                            if (j == no) continue;
                            //同じ群の講座をカウント
                            if (grplist_array[j] == grplist_array[no]) grpcnt++;
                        }
                    }
                }
            }
        }

        if (grpcnt > 0) {
            alert('対象の群は既に選ばれています。');
            obj.checked = false;
        }
    }
}
