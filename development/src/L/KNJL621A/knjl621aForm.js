function btn_submit(cmd) {
    var isCancel = false;
    if (cmd == 'insert'){
        if (document.forms[0].SEATNO.value.length != 4) {
            alert('{rval MSG903}'+"\n座席番号は半角4文字です。");
            return false;
        }
        if (!confirm('{rval MSG102}')) {
            return false;
        }
        $.ajax({
            url:'knjl621aindex.php',
            type:'POST',
            data:{
                AJAX_APPLICANTDIV:document.forms[0].APPLICANTDIV.value,
                AJAX_TESTDIV:document.forms[0].TESTDIV.value,
                AJAX_SEATNO:document.forms[0].SEATNO.value,
                AJAX_EXAMNO:document.forms[0].EXAMNO.value,
                cmd:'ajaxGetSeatno',
            },
            async:false
        }).done(function(data, textStatus, jqXHR) {
            var someStr = $.parseJSON(data);
            var splt = someStr.split(',');
            var examno = splt[0];
            if (examno) {
                if (!confirm('この座席番号は既に使用されています。\n新しい受験者に入れ替えますか？\n登録済みの受験者は座席未登録になります。')) {
                    isCancel = true;
                }
                document.forms[0].DUPL_SEATNO_FLG.value = true;
            } else {
                var res = splt[1];
                if (res) {
                    if (!confirm('この受験者は既に座席番号が登録済みです。\n新しい座席番号に入れ替えますか？')) {
                        isCancel = true;
                    }
                }
            }
        });
    }
    if (!isCancel) {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
    return false;
}

function deleteSubmit(entexamyear, applicantdiv, testdiv, examno, execTime) {
    if (!confirm('{rval MSG103}')) {
        return false;
    }

    //削除対象のパラメータを設定
    document.forms[0].LIST_ENTEXAMYEAR.value = entexamyear;
    document.forms[0].LIST_APPLICANTDIV.value = applicantdiv;
    document.forms[0].LIST_TESTDIV.value = testdiv;
    document.forms[0].LIST_EXAMNO.value = examno;
    document.forms[0].LIST_EXEC_TIME.value = execTime;

    document.forms[0].cmd.value = 'delete';
    document.forms[0].submit();
    return false;
}

//maxLengthまで入力後、次の入力フォームに移動
function nextfeild(str) {
    if (event.keyCode === 27 || event.keyCode === 37 || event.keyCode === 38 || event.keyCode === 39 || event.keyCode === 40) {
        return false;
    }

    if (str.value.length >= str.maxLength) {
        for (var i = 0, elm = str.form.elements; i < elm.length; i++) {
            if (elm[i] == str) {
                (elm[i + 1] || elm[0]).focus();
                break;
            }
        }
    }
    return (str);
}

//名前を表示
function showName() {
    $.ajax({
        url:'knjl621aindex.php',
        type:'POST',
        data:{
            AJAX_APPLICANTDIV:document.forms[0].APPLICANTDIV.value,
            AJAX_TESTDIV:document.forms[0].TESTDIV.value,
            AJAX_EXAMNO:document.forms[0].EXAMNO.value,
            cmd:'ajaxGetName',
        }
    }).done(function(data, textStatus, jqXHR) {
        var examname = $.parseJSON(data);
        $('#examname').text('');
        if (examname) {
            $('#examname').text(examname);
        }
    });
}
