function btn_submit(cmd) {
    if (cmd == "chg_grade") {
        parent.right_frame.location.href = document.forms[0].path.value+'&init=1';
        cmd = "list";
        document.forms[0].mode.value = "ungrd";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function search(f,mode){
    for (var i = 0; i < f.elements.length; i++) {
        elementName = '';
        switch(f.elements[i].name){
        case 'GRADE':
            elementName = 'LEFT_GRADE';
        case 'COURSECODE':
        case 'CLUBCD':
        case 'CHAIRCD':
        case 'HR_CLASS':
        case 'NAME':
        case 'NAME_SHOW':
        case 'NAME_KANA':
        case 'NAME_ENG':
        case 'KEYWORD':
        case 'ACTIONDATE':
        case 'GRD_YEAR':
            if (elementName) {
                document.forms[0][elementName].value = f.elements[i].value;
            } else {
                document.forms[0][f.elements[i].name].value = f.elements[i].value;
            }
        }
    }
    document.forms[0].cmd.value = 'search';
    document.forms[0].mode.value = mode;
    document.forms[0].submit();
}

//右フレームに検索画面
function showSearch(mode){
    parent.right_frame.location.href='index.php?cmd=right&mode='+mode;
}

var objLink;
var index = 0;
function Link(obj) {
    objLink = obj;
}

