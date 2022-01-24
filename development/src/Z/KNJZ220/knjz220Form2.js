function btn_submit(cmd) {

    if (cmd == 'copy'){
        document.forms[0].Cleaning.value = 'off';
    }

    if (cmd == 'update'){
        if(document.forms[0].SUBCLASSCD.value == ''){
            alert('{rval MSG308}');
            return true;
        }
    }

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }


    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}

function isNumb(ival){
    return toInteger(ival)
}

function cleaning_val(str){
    if(str == 'off')
        document.forms[0].Cleaning.value = 'off';
}

function insertDefVal(cnt){

    var i;
    var mark;
    var lowname;
    var highname;
    var def_mark;
    var def_low;
    var def_high;

    def_mark = document.forms[0].default_val_mark.value;
    def_mark = def_mark.split(",");

    def_low = document.forms[0].default_val_low.value;
    def_low = def_low.split(",");

    def_high = document.forms[0].default_val_high.value;
    def_high = def_high.split(",");

    for(i=0; i<cnt; i++){
        mark  = "ASSESSMARK" + (i + 1) ;
        document.all[mark].value = def_mark[i];

        if(i>0){
            lowname  = "ASSESSLOW" + (i + 1);
            highname = "strID" + i;

            document.all[lowname].value = def_low[(i)];
            document.all[highname].innerHTML = def_high[(i - 1)];
        }
    }
}

