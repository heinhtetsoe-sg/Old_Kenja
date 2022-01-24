function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function Page_jumper(link) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG308}");
        return;
    }
    if (!confirm("{rval MSG108}")) {
        return;
    }
    parent.location.href = link;
}

function syokenNyuryoku(obj, target_obj, nameCd2List) {
    if (obj.value != "" && nameCd2List.indexOf(obj.value) >= 0) {
        target_obj.disabled = false;
    } else {
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
        target_obj.disabled = true;
    }
}

function seitoSentakuZumi() {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("リストよりデータを選択してから行ってください。");
        return false;
    } else {
        if (!confirm("{rval MSG108}")) {
            return false;
        }
        return true;
    }
}
function sisikiClick() {
    document.forms[0].btn_sisiki.click();
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
                    obj_updElement.value = typeValArray[setVal - 1] == undefined ? "" : typeValArray[setVal - 1];
                } else {
                    obj_updElement.value = typeShowArray[setVal - 1] == undefined ? "" : typeShowArray[setVal - 1];
                }
            }
        }
    }
    culcToothCnt();
}

function kirikae(obj, showName) {
    setValue(obj, showName, document.forms[0].NYURYOKU[1].checked);
    culcToothCnt();
}

function kirikae2(obj, showName) {
    if (event.preventDefault) {
        event.preventDefault();
    }
    event.cancelBubble = true;
    event.returnValue = false;
    clickList(obj, showName);
    culcToothCnt();
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
    culcToothCnt();
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

//歯数の算出
function culcToothCnt() {
    reA = new RegExp("_R_ADULT|_L_ADULT");
    reB = new RegExp("_R_BABY|_L_BABY");

    babytooth = 0;
    remainbabytooth = 0;
    treatedbabytooth = 0;
    brack_babytooth = 0;
    adulttooth = 0;
    remainadulttooth = 0;
    treatedadulttooth = 0;
    lostadulttooth = 0;
    brack_adulttooth = 0;
    checkadulttooth = 0;
    dentistremark_co = 0;

    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var obj_updElement = document.forms[0].elements[i];

        if (obj_updElement.name.match(reA) && obj_updElement.name.match("_FORM_ID")) {
            if (obj_updElement.value != "" && obj_updElement.value != "04") {
                adulttooth++;
            }
            if (obj_updElement.value == "02") {
                remainadulttooth++;
            }
            if (obj_updElement.value == "03") {
                treatedadulttooth++;
            }
            if (obj_updElement.value == "04") {
                lostadulttooth++;
            }
            if (obj_updElement.value == "06") {
                brack_adulttooth++;
                dentistremark_co++;
            }
        }
        if (obj_updElement.name.match(reB) && obj_updElement.name.match("_FORM_ID")) {
            if (obj_updElement.value != "" && obj_updElement.value != "04") {
                babytooth++;
            }
            if (obj_updElement.value == "02") {
                remainbabytooth++;
            }
            if (obj_updElement.value == "03") {
                treatedbabytooth++;
            }
            if (obj_updElement.value == "05") {
                brack_babytooth++;
            }
        }
    }

    document.forms[0].BABYTOOTH.value = babytooth;
    document.forms[0].REMAINBABYTOOTH.value = remainbabytooth;
    document.forms[0].TREATEDBABYTOOTH.value = treatedbabytooth;
    document.forms[0].BRACK_BABYTOOTH.value = brack_babytooth;
    document.forms[0].ADULTTOOTH.value = adulttooth;
    document.forms[0].REMAINADULTTOOTH.value = remainadulttooth;
    document.forms[0].TREATEDADULTTOOTH.value = treatedadulttooth;
    document.forms[0].LOSTADULTTOOTH.value = lostadulttooth;
    document.forms[0].BRACK_ADULTTOOTH.value = brack_adulttooth;
    document.forms[0].DENTISTREMARK_CO.value = dentistremark_co;
    //熊本のみ
    if (document.forms[0].IS_Z010.value == "kumamoto") {
        if (document.forms[0].BRACK_BABYTOOTH.value > 0) {
            document.forms[0].OTHERDISEASECD.value = "06"; // 06:要注意乳歯をセット
        } else {
            document.forms[0].OTHERDISEASECD.value = "";
        }
    }
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    nextURL = "";
    for (var i = 0; i < parent.left_frame.document.links.length; i++) {
        var search = parent.left_frame.document.links[i].search;
        //searchの中身を&で分割し配列にする。
        arr = search.split("&");

        //学籍番号が一致
        if (arr[1] == "SCHREGNO=" + schregno) {
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length - 1) {
                idx = 0; //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            } else if (order == 0) {
                idx = i + 1; //更新後次の生徒へ
            } else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length - 1; //更新後前の生徒へ(データが最初の生徒の時)
            } else if (order == 1) {
                idx = i - 1; //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href; //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = "update";
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
            document.location.replace(nextURL);
            alert("{rval MSG201}");
        } else if (cd == "1") {
            //クッキー削除
            deleteCookie("nextURL");
        }
    }
}

//チェックボックスのラベル表示（有・無）
function checkAri_Nasi(obj, id) {
    var ari_nasi = document.getElementById(id);
    if (obj.checked) {
        ari_nasi.innerHTML = "有";
    } else {
        ari_nasi.innerHTML = "無";
    }
}

//その他疾病及び異常
function OptionUse(obj, tgtname) {
    var tgt = document.getElementsByName(tgtname);
    if (tgt && tgt[0]) {
        tgt = tgt[0];
        if (obj.value == "99") {
            tgt.disabled = false;
            tgt.style.backgroundColor = "#ffffff";
        } else {
            tgt.disabled = true;
            tgt.style.backgroundColor = "darkgray";
        }
    }
}
var xmlhttp = null;

/* XMLHttpRequest生成 */
function createXmlHttp() {
    if (document.all) {
        return new ActiveXObject("Microsoft.XMLHTTP");
    } else if (document.implementation) {
        return new XMLHttpRequest();
    } else {
        return null;
    }
}

/* POSTによるデータ送信 */
function chgDataSisikiUp(schregno) {
    /* XMLHttpRequestオブジェクト作成 */
    if (xmlhttp == null) {
        xmlhttp = createXmlHttp();
    } else {
        /* 既に作成されている場合、通信をキャンセル */
        xmlhttp.abort();
    }

    /* 入力フォームデータの処理 */
    var postdata = new String();
    postdata = "cmd=send";
    postdata += "&SCHREGNO=" + schregno;
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function () {
        handleHttpEvent(schregno);
    }; //こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjh400_hakoukuuindex.php", true);
    xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);
}

/* レスポンスデータ処理 */
function handleHttpEvent(schregno) {
    if (xmlhttp.readyState == 4) {
        if (xmlhttp.status == 200) {
            var brackBabytooth = document.forms[0].BRACK_BABYTOOTH;
            var brackAdulttooth = document.forms[0].BRACK_ADULTTOOTH;
            var dentistRemarkCo = document.forms[0].DENTISTREMARK_CO;

            var json = xmlhttp.responseText;
            var response;
            //var debug = document.getElementById('debug');
            //debug.innerHTML = json;
            eval("response = " + json); //JSON形式のデータ(オブジェクトとして扱える)

            //戻り値
            if (response.result) {
                brackBabytooth.value = response.BRACK_BABYTOOTH;
                brackAdulttooth.value = response.BRACK_ADULTTOOTH;
                dentistRemarkCo.value = response.DENTISTREMARK_CO;
            } else {
                brackBabytooth.value = "";
                brackAdulttooth.value = "";
                dentistRemarkCo.value = "";
            }
        } else {
            window.alert("通信エラーが発生しました。");
        }
    }
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == "13") {
        var tmpId = obj.id.split("_");
        var no = parseInt(tmpId[1], 10);
        var maxNo = document.forms[0].ENTMOVE_COUNTER.value;

        var targetId = obj.id;
        if (window.event.shiftKey) {
            for (var i = no - 1; i >= 0; i--) {
                flg = true;
                targetId = tmpId[0] + "_" + i;

                if (document.getElementById(targetId).disabled == true) flg = false;
                if (document.getElementById(targetId).type == "text" || document.getElementById(targetId).type == "textarea") {
                    if (document.getElementById(targetId).readOnly == true) flg = false;
                }
                if (flg == true) break;
            }
        } else {
            for (var i = no + 1; i < maxNo; i++) {
                flg = true;
                targetId = tmpId[0] + "_" + i;

                if (document.getElementById(targetId).disabled == true) flg = false;
                if (document.getElementById(targetId).type == "text" || document.getElementById(targetId).type == "textarea") {
                    if (document.getElementById(targetId).readOnly == true) flg = false;
                }
                if (flg == true) break;
            }
        }
        if (targetId != obj.id) {
            document.getElementById(targetId).focus();
        }
    } else if (obj.type == "button" && window.event.keyCode == "32") {
        var schno = document.forms[0].SCHREGNO.value;
        if (obj.name == "btn_adultIns") {
            //永久歯正常
            dataChange("ADULT");
        } else if (obj.name == "btn_update") {
            //更新
            btn_submit("update");
        } else if (obj.name == "btn_up_pre") {
            //更新後前の生徒へ
            updateNextStudent(schno, 1);
        } else if (obj.name == "btn_up_next") {
            //更新後次の生徒へ
            updateNextStudent(schno, 0);
        }
    }

    return false;
}
//歯肉の状態コンボ変更時、（熊本のみ）
function setCheckOn(obj) {
    if (obj.value == "02") {
        document.forms[0].DENTISTREMARK_GO.checked = true;
        document.getElementById("ari_nasi_go").innerHTML = "有";
    } else {
        document.forms[0].DENTISTREMARK_GO.checked = false;
        document.getElementById("ari_nasi_go").innerHTML = "無";
    }
    if (obj.value == "03") {
        document.forms[0].DENTISTREMARK_G.checked = true;
        document.getElementById("ari_nasi_g").innerHTML = "有";
    } else {
        document.forms[0].DENTISTREMARK_G.checked = false;
        document.getElementById("ari_nasi_g").innerHTML = "無";
    }
    return;
}
window.onload = function () {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var tagName = document.forms[0].elements[i].tagName;
        var tagType = document.forms[0].elements[i].type;
        if ((tagName == "INPUT" && tagType != "button") || tagName == "SELECT" || tagName == "TEXTAREA") {
            document.forms[0].elements[i].disabled = true;
            document.forms[0].elements[i].style.backgroundColor = "#FFFFFF";
        }
    }
};
