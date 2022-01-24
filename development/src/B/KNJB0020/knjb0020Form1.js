function btn_submit(cmd){
    if (document.forms[0].STAFFCD.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    var period = document.forms[0].period.value;
    var obj = new Object();
    var data = "";
    var sep = "";
    var p = period.split(',');
    for (var ii = 1; ii < 8 ; ii++) {
        for (var i = 0; i < p.length; i++) {
            obj = document.getElementById('HIDDEN_'+ii+'-'+p[i]);
            if (obj == null) continue;
            if (!eval(obj.value)) {
                data += sep +ii+'-'+p[i];
                sep = ',';
            }
        }
    }

    document.forms[0].data.value = data;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function switchCell(ID)
{
    if (ID == '') return false;
    if (ID == '99-99') return false;
    src = new Object();
    src2 = new Object();
    obj = new Object();
    src = document.getElementById(ID);
    src2 = document.getElementById('HIDDEN_'+ID);
    if(src == null) return false;

    var sep = ID.split('-');
    if (sep[0]=='99') {
        for (var ii = 1; ii < 8 ; ii++) {
            obj = document.getElementById(ii+'-'+sep[1]);
            obj2 = document.getElementById('HIDDEN_'+ii+'-'+sep[1]);
            if (obj == null) continue;
            obj.bgColor = eval(src.value)?"#ff0099":"#3399ff";
            outputLAYER(obj.id,(eval(src.value)?"稼動<br>不可":"稼動可"));
            obj.value = !eval(src.value);       
            obj2.value = !eval(src.value);
        }
        src.value = !eval(src.value);
        src2.value = !eval(src2.value);
    } else if (sep[1]=='99') {
        var period = document.forms[0].period.value;
        var p = period.split(',');
        for (var i = 0; i < p.length; i++) {
            obj = document.getElementById(sep[0]+'-'+p[i]);
            obj2 = document.getElementById('HIDDEN_'+sep[0]+'-'+p[i]);
            if (obj == null) continue;
            obj.bgColor = eval(src.value)?"#ff0099":"#3399ff";
            outputLAYER(obj.id,(eval(src.value)?"稼動<br>不可":"稼動可"));
            obj.value = !eval(src.value);           
            obj2.value = !eval(src.value);
        }    
        src.value = !eval(src.value);
        src2.value = !eval(src2.value);
    } else {
        src.bgColor = eval(src2.value)?"#ff0099":"#3399ff";
        outputLAYER(src.id,(eval(src2.value)?"稼動<br>不可":"稼動可"));
        src2.value = !eval(src2.value);
    }
}
