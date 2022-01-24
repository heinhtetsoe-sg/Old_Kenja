function f0() { return document.forms[0];}
function btn_submit(cmd) {
    if (cmd == "csv" || cmd == "update") {
        if (f0().category_selected.length == 0)
        {
            alert('{rval MSG916}');
            return;
        }
    }

    var seldat = f0().selectdata;
    seldat.value = "";
    var sep = "";
	var i;
    for (i = 0; i < f0().category_name.length; i++) {
        f0().category_name.options[i].selected = 0;
    }

    for (i = 0; i < f0().category_selected.length; i++) {
        f0().category_selected.options[i].selected = 1;
        var val = f0().category_selected.options[i].value;
        var sel = val.split('-');
        if (sel.length > 1) {
            seldat.value += sep + sel[0] + "-" + sel[5];
            sep = ",";
        }
    }

    f0().cmd.value = cmd;
    f0().submit();

    return false;
}
function checkRisyu() {
    if (f0().MIRISYU[0].checked && f0().RISYU[1].checked) {
        alert('未履修科目が出力される状態になっています。');
        f0().RISYU[0].checked = true;
        f0().RISYU[1].checked = false;
    }
}
function newwin(SERVLET_URL) {
    //何年用のフォームを使うのか決める
    if (f0().FORM6.checked) {
        f0().NENYOFORM.value = f0().NENYOFORM_CHECK.value
    } else {
        f0().NENYOFORM.value = f0().NENYOFORM_SYOKITI.value
    }

    if (f0().category_selected.length == 0)
    {
        alert('{rval MSG916}');
        return false;
    }
	var i;
    for (i = 0; i < f0().category_name.length; i++) {
        f0().category_name.options[i].selected = 0;
    }
    ClearList(f0().category_selected);

//    for (i = 0; i < f0().category_selected.length; i++) {
//        f0().category_selected.options[i].selected = 1;
//		var val = f0().category_selected.options[i].value;
//        var sel = val.split('-');
//		//console.log("print " + i + " = " + val + " / " + sel[0] + ", " + sel[5]);
//        if (sel.length > 1) {
//            f0().category_selected.options[i].value = sel[0] + "-" + sel[5];
//        }
//    }

    action = f0().action;
    target = f0().target;

//    url = location.hostname;
//    f0().action = "http://" + url +"/cgi-bin/printenv.pl";
    f0().action = SERVLET_URL +"/KNJE";
    f0().target = "_blank";
    f0().submit();

    f0().action = action;
    f0().target = target;
}

function ClearList(OptionList)
{
    OptionList.length = 0;
}

function move1(side)
{
    var temp1 = [];
    var temp2 = [];
	var i;
    var attribute1;
    var attribute2;
	var opt;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = f0().category_name;
        attribute2 = f0().category_selected;
    } else {
        attribute1 = f0().category_selected;
        attribute2 = f0().category_name;
    }

    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        temp1[temp1.length] = attribute2.options[i];
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            temp1[temp1.length] = attribute1.options[i];
        } else {
            temp2[temp2.length] = attribute1.options[i];
        }
    }

    temp1.sort(function(a, b) { if (a.value < b.value) { return -1;} else if (a.value > b.value) { return 1;} else 0;  });

    //generating new options
    for (i = 0; i < temp1.length; i++) {
		opt = new Option();
 	    opt.value = temp1[i].value;
        opt.text = temp1[i].text;
        attribute2.options[i] = opt;
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < temp2.length; i++) {
		opt = new Option();
 	    opt.value = temp2[i].value;
        opt.text = temp2[i].text;
        attribute1.options[i] = opt;
    }

}
function moves(sides)
{
	var i;
	var atts;

    //assign what select attribute treat as atts 
    if (sides == "left") {
        atts = f0().category_name;
    } else {
        atts = f0().category_selected;
    }

    for (i = 0; i < atts.options.length; i++) {
        atts.options[i].selected = 1;
    }
    move1(sides);
}

