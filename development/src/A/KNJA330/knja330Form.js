    function btn_submit(cmd) {
            if (cmd == 'execute') {
                var flag;
                flag = "";
                for (var i = 0; i < document.forms[0].elements.length; i++) {
                    var e = document.forms[0].elements[i];
                    if (e.type == "checkbox" && e.checked && e.name == "ADD_CHECKED[]"){
                        var val = e.value;
                        if (val != ''){
                            flag = "on";
                        }
                    }
                }
                if (flag == ''){
                    alert("チェックボックスが選択されておりません。");
                    return;
                }
                if (confirm('{rval MSG102}')) {
                } else {
                    return;
                }
            }
            if (cmd == 'delete') {
                var flag;
                flag = "";
                for (var i = 0; i < document.forms[0].elements.length; i++) {
                    var e = document.forms[0].elements[i];
                    if (e.type == "checkbox" && e.checked && e.name == "DEL_CHECKED[]"){
                        var val = e.value;
                        if (val != ''){
                            flag = "on";
                        }
                    }
                }
                if (flag == ''){
                    alert("チェックボックスが選択されておりません。");
                    return;
                }
                if (confirm('{rval MSG103}')) {
                } else {
                    return;
                }
            }
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
    }

