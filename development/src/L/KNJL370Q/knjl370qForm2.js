
//名前からカナへコピー
$(function() {
        $.fn.autoKana('#NAME_SEI', '#NAME_KANA_SEI');
        $.fn.autoKana('#NAME_MEI', '#NAME_KANA_MEI');
});

window.onload = function(){
    if(document.forms[0].cmd.value == 'change'){
        document.forms[0].NAME_SEI.focus();
    } else if(document.forms[0].cmd.value == 'change1'){
        document.forms[0].SCHOOLCD.focus();
    } else if(document.forms[0].cmd.value == 'change2'){
        document.forms[0].GROUPCD.focus();
    }
}

function btn_submit(cmd) {

    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    
    if(cmd == 'update' || cmd == 'add'){
        if(document.forms[0].EXAM_NO.value == ""){
            alert('受験番号は必須項目です。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}


//中学校県コンボ変更時に出身県も連動する
function schoolPrefChange(change)
{
    var schoolPref = change.value;
    document.forms[0].FROM_PREFCD.value = schoolPref;
    document.forms[0].SEARCH_SCHOOL.value = '';

    document.forms[0].cmd.value = 'change1';
    document.forms[0].submit();
    return false;
}

//ENTER押したら次へ
function toNext(nowItem)
{
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    //方向キー
    //var moveEnt = e.keyCode;
    if (e.keyCode != 13) {
        return;
    }
    
    //特殊
    if(nowItem.name == 'NAME_KANA_MEI'){
        document.forms[0].SEX1.focus();
        return;
    } else if(nowItem.name == "SEX"){
        return;
    } else if(nowItem.name == "SEARCH_SCHOOL"){
        return;
    } else if(nowItem.name == "ZIPCD"){
        document.forms[0].ADDR1.focus();
        return;
    } else if(nowItem.name == "TELNO"){
        document.forms[0].EXAMPLACECD.focus();
        return;
    } else if(nowItem.name == "IN_STUDENTNO"){
        document.forms[0].INPUT_DATE.focus();
        return;
    }
    
    
    var id = nowItem.id;
    var nextOut = '';
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var el = document.forms[0].elements[i];
        if(nextOut == '1' && el.name != 'SEX'){
            var nextItem = document.getElementById(el.name);
            break;
        }
        if(el.name == nowItem.name && el.name != 'SEX'){
            nextOut = '1';
        }
    }
    
    nextItem.focus();
    nextItem.select();
    return;
    
}
