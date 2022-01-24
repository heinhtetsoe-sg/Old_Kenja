function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (!confirm('{rval MSG107}'))
            return false;
        else 
            cmd = 'main';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
        return false;
}

//一括変更操作
function check_all(val) {
    var arr = ['0','1','2','3','9','7'];
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        if (e.type == "radio" && e.value == val) {
            //一括変更
            e.checked = true;
            //背景色変更
            for (var j=0; j < arr.length; j++) {
                var idname = arr[j]+","+e.name;
                var temp = document.getElementById(idname);
                if (arr[j] == val) {
                    temp.bgColor = "#ccffcc";
                } else {
                    temp.bgColor = "#ffffff";
                }
            }
        }
    }
}

function celcolchan(src, objname) {
    
    var radios = new Array(6);
    
    radios[0] = "0," + objname;
    radios[1] = "1," + objname;
    radios[2] = "2," + objname;
    radios[3] = "3," + objname;
    radios[4] = "9," + objname;
    radios[5] = "7," + objname;
    
    temp1 = new Object(); 
    temp2 = new Object(); 
    for (var i = 0; i < 6; i++){
        temp1 = document.getElementById(radios[i]);
        temp2 = document.getElementsByName(objname);
        if (temp1.id == src.id) {
            temp1.bgColor = "#ccffcc";
            temp2[i].checked = true;
        } else {
            temp1.bgColor = "#ffffff";
            temp2[i].checked = false;
        }
    }
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
