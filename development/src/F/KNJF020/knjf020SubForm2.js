window.onload = init;
function init() {
    //ウィンドウを開いたら呼ばれる関数
    switchDisabled(); //ラジオボタンを表示したり隠したり
}

//データ挿入用オブジェクトを入れる
var setObj;
var innerName;

function btn_submit(cmd, sisikiClick) {
    if (cmd != "subEnd") {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    } else {
        document.forms[0].SISIKI_CLICK.value == "OFF";
        top.main_frame.right_frame.closeit();
        top.main_frame.right_frame.document.forms[0].cmd.value = cmd;
        top.main_frame.right_frame.document.forms[0].SISIKI_CLICK.value = "OFF";
        top.main_frame.right_frame.document.forms[0].submit();
        return false;
    }
}

function dataChange(changeName) {
    if (changeName == "CLEAR") {
        re = new RegExp("_R_ADULT|_L_ADULT|_R_BABY|_L_BABY");
        setVal = "00";
    } else {
        re = new RegExp("_R_" + changeName + "|_L_" + changeName);
        setVal = "01";
    }
    re2 = new RegExp("_FORM_ID");
    re3 = new RegExp("8");

    typeValArray = document.forms[0].SETVAL.value.split(",");
    typeShowArray = document.forms[0].SETSHOW.value.split(",");

    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var obj_updElement = document.forms[0].elements[i];
        if (obj_updElement.name.match(re)) {
            if (changeName != "ADULT" || !obj_updElement.name.match(re3)) {
                if (obj_updElement.name.match(re2)) {
                    obj_updElement.value =
                        typeValArray[setVal - 1] == undefined
                            ? ""
                            : typeValArray[setVal - 1];
                } else {
                    obj_updElement.value =
                        typeShowArray[setVal - 1] == undefined
                            ? ""
                            : typeShowArray[setVal - 1];
                }
            }
        }
    }
}

function kirikae(obj, showName) {
    setValue(obj, showName, document.forms[0].NYURYOKU[1].checked);
}

function kirikae2(obj, showName) {
    if (event.preventDefault) {
        event.preventDefault();
    }
    event.cancelBubble = true;
    event.returnValue = false;
    clickList(obj, showName);
}

function setValue(obj, showName, clearCheck) {
    if (clearCheck) {
        obj.value = "";
        defObj = eval("document.forms[0]." + obj.name + "_FORM_ID");
        defObj.value = "";
    } else {
        innerName = showName;
        typeValArray = document.forms[0].SETVAL.value.split(",");
        typeShowArray = document.forms[0].SETSHOW.value.split(",");

        defObj = eval("document.forms[0]." + obj.name + "_FORM_ID");

        for (var i = 0; i < document.forms[0].TYPE_DIV.length; i++) {
            typeDiv = document.forms[0].TYPE_DIV[i];
            if (typeDiv.checked) {
                obj.value = typeShowArray[typeDiv.value - 1];
                defObj.value = typeValArray[typeDiv.value - 1];
            }
        }
    }
}

function clickList(obj, showName) {
    innerName = showName;

    setObj = obj;
    if (event.preventDefault) {
        myObj = document.getElementById("myID_Menu").style;
    } else {
        myObj = document.forms[0].all["myID_Menu"].style;
    }
    myObj.left = window.event.clientX + document.body.scrollLeft + "px";
    myObj.top = window.event.clientY + document.body.scrollTop + "px";
    myObj.visibility = "visible";
}

function myHidden() {
    document.getElementById("myID_Menu").style.visibility = "hidden";
    switchDisabled();
}

function setClickValue(val) {
    if (val != "999") {
        typeShowArray = document.forms[0].SETSHOW.value.split(",");
        setObj.value = typeShowArray[val - 1];

        defObj = eval("document.forms[0]." + setObj.name + "_FORM_ID");
        typeValArray = document.forms[0].SETVAL.value.split(",");
        defObj.value = typeValArray[val - 1];
    }
    myHidden();
    setObj.focus();
}

function switchDisabled() {
    obj = document.getElementById("NYURYOKU1");
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/TYPE_DIV/)) {
            document.forms[0].elements[i].disabled = !obj.checked;
        }
    }
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order) {
    if (schregno == "") {
        alert("{rval MSG304}");
        return true;
    }
    nextURL = "";
    for (var i = 0; i < top.main_frame.left_frame.document.links.length; i++) {
        var search = top.main_frame.left_frame.document.links[i].search;
        //searchの中身を&で分割し配列にする。
        arr = search.split("&");

        //学籍番号が一致
        if (arr[1] == "SCHREGNO=" + schregno) {
            //昇順
            if (
                order == 0 &&
                i == top.main_frame.left_frame.document.links.length - 1
            ) {
                idx = 0; //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            } else if (order == 0) {
                idx = i + 1; //更新後次の生徒へ
            } else if (order == 1 && i == 0) {
                idx = top.main_frame.left_frame.document.links.length - 1; //更新後前の生徒へ(データが最初の生徒の時)
            } else if (order == 1) {
                idx = i - 1; //更新後前の生徒へ
            }
            nextURL = top.main_frame.left_frame.document.links[idx].href; //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = "subUpdate2";
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd) {
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if (cd == "0") {
            //クッキー削除
            deleteCookie("nextURL");
            top.main_frame.right_frame.document.location.replace(nextURL);
            alert("{rval MSG201}");
        } else if (cd == "1") {
            //クッキー削除
            deleteCookie("nextURL");
        }
    }
}
