function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        } 
    }

    if (cmd=="update") {
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text') {
                var nam = e.name;
                if (nam.match(/RATE./)) {
                    if (e.value == '') {
                        alert('{rval MSG301}' + '\n(評定率)');
                        return false;
                    }
                    else if (!isNaN(e.value) && (e.value > 100 || e.value < 0)) {
                        alert('{rval MSG901}' + '\n0～100まで入力可能です(評定率)');
                        return false;
                    }
                }
                if (nam.match(/ASSESSLEVEL5./)) {
                    if (e.value == '') {
                        alert('{rval MSG301}' + '\n(評定5段階)');
                        return false;
                    }
                    else if (!isNaN(e.value) && (e.value > 5 || e.value < 1)) {
                        alert('{rval MSG901}' + '\n1～5まで入力可能です(評定5段階)');
                        return false;
                    }
                }
            }
        }
        var sum_rate = document.all('rate').innerHTML;
        if (sum_rate != 100) {
            alert('{rval MSG901}' + '\n評定率合計を 100% にして下さい。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function ShowConfirm(){
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
}

function isNumb(that,level){

    if (!isNaN(that.value) && (that.value > 100 || that.value < 0)) {
        alert('{rval MSG901}' + '\n0～100まで入力可能です(評定率)');
        return false;
    }

        var sum_rate = 0;
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                var nam = e.name;
                if (nam.match(/RATE./)) {
                    if (!isNaN(e.value) && (e.value > 100 || e.value < 0)) {
                        alert('{rval MSG901}' + '\n0～100まで入力可能です(評定率)');
                        return false;
                    }
                    sum_rate = parseInt(sum_rate) + parseInt(e.value);
                }
            }
        }
        document.all('rate').innerHTML = sum_rate;

    return;
}

function isNumb2(that,level){

    if (that.value != '' && !isNaN(that.value) && (that.value > 5 || that.value < 1)) {
        alert('{rval MSG901}' + '\n1～5まで入力可能です(評定5段階)');
        return false;
    }
    return;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
