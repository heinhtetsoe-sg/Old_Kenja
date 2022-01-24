var textRange;
function btn_submit(cmd, index) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'update') {
        document.forms[0].UPDATE_INDEX.value = index;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chksetdate() {
    if (document.forms[0].USEKNJD420PDISPUPDDATE.value == "1") {
        if (document.forms[0].UPDDATE.value == "" || (document.forms[0].UPDDATE.value == "9999/99/99" && document.forms[0].SELNEWDATE.value == "")) {
            alert('{rval MSG304}');
            return false;
        }
    }
    return true;
}

function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

function resetRemark(index) {
    if (!confirm('{rval MSG106}')) {
        return false;
    } else {
        switch (parseInt(index, 10)) {
            case 1:
                for (var i = 1; i <= 2; i++) {
                    var key = ('000'+i).slice(-3);
                    key = document.forms[0]['TITLE_'+key].value
                    document.forms[0][key].value = document.forms[0]['INIT_'+key].value;
                }
                break;
            case 2:
                for (var i = 3; i <= 4; i++) {
                    var key = ('000'+i).slice(-3);
                    key = document.forms[0]['TITLE_'+key].value
                    document.forms[0][key].value = document.forms[0]['INIT_'+key].value;
                }
                break;
            case 3:
                for (var i = 1; i <= 4; i++) {
                    for (var j = 5; j <= 8; j++) {
                        var key = ('000'+j).slice(-3);
                        key = document.forms[0]['TITLE_'+key].value
                        document.forms[0][key+i].value = document.forms[0]['INIT_'+key+i].value;
                    }
                }
                break;
            default:
                break;
        }
        
    }
}

function dataSort(sId) {

    var thNo;
    //ソートの基準となる項目を設定
    if (sId === "typeSemester") {
        thNo_1 = 0;   //SORT_SEMESTER
        thNo_2 = 1;   //SORT_ITEM
        thNo_3 = 2;   //SEMESTERNAME
    }
    //昇順、降順を設定
    var descOrAsc = $("input[name=hidden_"+ sId +"]");
    $('tbody#listTbody').html(
        $('tr.listTr').sort(function(a, b) {
            if (descOrAsc.val() === '1') {
                //降順
                var left = b;
                var right = a;
            } else {
                //昇順
                var left = a;
                var right = b;
            }
            //並び替え
            if ($(left).find('td').eq(thNo_1).text() > $(right).find('td').eq(thNo_1).text()) {
                return 1;
            } else if ($(left).find('td').eq(thNo_1).text() < $(right).find('td').eq(thNo_1).text()) {
                return -1;
            }
            //項目は昇順固定
            if ($(a).find('td').eq(thNo_2).text() > $(b).find('td').eq(thNo_2).text()) {
                return 1;
            } else if ($(a).find('td').eq(thNo_2).text() < $(b).find('td').eq(thNo_2).text()) {
                return -1;
            }
        })
    );
    //ヘッダーの表記修正
    var typeSemester = $('tr#list_header').find('th').eq(thNo_3);
    if (descOrAsc.val() === '1') {
        descOrAsc.val('0');
        var mark = '▼'
    } else {
        descOrAsc.val('1');
        var mark = '▲'
    }

    setMark(sId, "typeSemester", typeSemester.find('a'), '学期', mark);
}
function setMark(sId, objname, obj, label, mark) {
    if (sId === objname) {
        obj.text(label + mark);
    } else {
        obj.text(label);
    }
}