function btn_submit(cmd) {
    var result;
    if (cmd == "clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        } 
    } else if (cmd == "copy_subclass") {
        result = confirm('{rval MSG105}');
        if (result == false) {
            return false;
        } 
    } else if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        } 
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    var result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
}

function isNumb(that, level, testCount, mode) {
    var anser;

    that.value = toNumber(that.value);

    if (that.value <= 0 || level - 1 < 1) {
        return;
    } else {
        anser = (1.0 * that.value) - 1;
        document.getElementById('assessHigh' + (level - 1) + "_" + testCount).innerHTML = anser;
    }
    return;
}

function closing_window(){
    alert('{rval MSG300}');
    closeWin();
    return true;
}

function enterNext(e) {
    if (e.keyCode != 13) {
        return;
    }
    var enterChan = document.getElementsByName("enterChan")[0].value.split(",");
    var idx = enterChan.indexOf(e.srcElement.name);
    if (idx == -1) {
        return;
    }
    idx += e.shiftKey ? -1 : 1;
    var next = document.getElementsByName(enterChan[idx])[0];
    if (next) {
        next.focus();
    }
}

window.onload = function() {
    var elems = document.getElementsByTagName("input");
    var e;
    for (var i = 0; i < elems.length; i++) {
        e = elems[i];
        if (e.type == "text" && e.name && e.name.match(/ASSESS.*/)) {
            if (e.addEventListener) {
                e.addEventListener("keydown", enterNext);
            }
        }
    }
};

