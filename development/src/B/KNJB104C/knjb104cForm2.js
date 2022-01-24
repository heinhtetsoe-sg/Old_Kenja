var selectedId;
function btn_submit(cmd) {
    if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }
    if (cmd == "update") {
        var sep = "";
        document.forms[0].setData.value = "";
        var retu = document.forms[0].retu.value;
        var gyou = document.forms[0].gyou.value;
        for (var i = 1; i <= retu; i++) {
            for (var j = 1; j <= gyou; j++) {
                if (document.getElementById("MAIN_" + j + "_" + i).getAttribute("data-notuse") == "1") {
                    continue;
                }
                schregno = document.getElementById("MAIN_" + j + "_" + i).getAttribute("data-schregno");
                seatNo = document.getElementById("SEAT_NO_" + j + "_" + i).value;
                if (schregno) {
                    if (seatNo == "") {
                        alert("席順が設定されていません。");
                        return;
                    }
                    document.forms[0].setData.value += sep + schregno + ":" + i + ":" + j + ":" + seatNo;
                    sep = ",";
                }
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function boxClick(obj, i, j) {
    var notuseVal = document.forms[0].notuse.value;
    var notuseValList = notuseVal == "" ? [] : notuseVal.split(",");
    if (notuseValList.indexOf(i + "*" + j) == -1) {
        obj.style.backgroundColor = "#CCCCCC";
        notuseValList.push(i + "*" + j);
        document.forms[0].notuse.value = notuseValList.join(",");
    } else {
        obj.style.backgroundColor = "#FFFFFF";
        notuseValList.splice(notuseValList.indexOf(i + "*" + j), 1);
        document.forms[0].notuse.value = notuseValList.join(",");
    }
}
function f_dragstart(event) {
    event.dataTransfer.setData("text", event.target.id);
}

function f_dragover(event) {
    event.preventDefault();
}

/***** ドロップ時の処理 *****/
function f_drop(event) {
    var id = event.dataTransfer.getData("text");
    if (id == event.currentTarget.id) {
        event.preventDefault();
        return;
    }
    if (id.indexOf("LEFT_") != -1) {
        var dragItem = parent.left_frame.document.getElementById(id);
        var schregno = dragItem.getAttribute("data-schregno");
        if (isExsitSchregno(schregno)) {
            return;
        }
        var list = dragItem.innerHTML.match(/<td>(.*)<\/td>\s*<td>(.*)<\/td>/);
        var list2 = event.currentTarget.innerHTML.match(/<input[^>]+>/);
        event.currentTarget.innerHTML = list2[0] + " " + list[1] + "<br> " + list[2];
        event.currentTarget.setAttribute("data-schregno", schregno);
    } else if (id.indexOf("MAIN_") != -1) {
        var srcItem = document.getElementById(id);
        var srcSchregno = srcItem.getAttribute("data-schregno");
        var srcList = srcItem.innerHTML.match(/(<input[^>]*id="([^"]+)"[^>]*>)(.*)/);
        var srcSeatNo = document.getElementById(srcList[2]).value;

        var targetItem = event.currentTarget;
        var targetSchregno = targetItem.getAttribute("data-schregno");
        var targetList = targetItem.innerHTML.match(/(<input[^>]*id="([^"]+)"[^>]*>)(.*)/);
        var targetSeatNo = document.getElementById(targetList[2]).value;

        targetItem.innerHTML = srcList[1].trim() + " " + srcList[3].trim();
        srcItem.innerHTML = targetList[1].trim() + " " + targetList[3].trim();

        document.getElementById(targetList[2]).value = srcSeatNo;
        targetItem.setAttribute("data-schregno", srcSchregno);

        document.getElementById(srcList[2]).value = targetSeatNo;
        srcItem.setAttribute("data-schregno", targetSchregno);

        obj1 = document.getElementById(targetList[2]);
        obj2 = document.getElementById(srcList[2]);

        obj1.id = srcList[2];
        obj2.id = targetList[2];
    }
    event.preventDefault();
    f_click(event);
}

function f_click(event) {
    if (document.getElementById(event.currentTarget.id).getAttribute("data-notuse") == "1") {
        return;
    }
    if (selectedId) {
        document.getElementById(selectedId).style.backgroundColor = "#FFFFFF";
    }
    selectedId = event.currentTarget.id;
    document.getElementById(selectedId).style.backgroundColor = "#FFCCCC";
    document.getElementById(selectedId.replace("MAIN", "SEAT_NO")).focus();
}

function wariate() {
    if (!confirm("自動座席割り当てを実行します。よろしいでしょうか？")) {
        return;
    }
    var tagList = parent.left_frame.document.querySelectorAll("#box3 tr");
    var retu = document.forms[0].retu.value;
    var gyou = document.forms[0].gyou.value;
    var idx = 1;
    var seatNo = 0;
    minyuuyrokuCnt = 0;
    maxCnt = tagList.length - 1;
    for (var i = 1; i <= retu; i++) {
        for (var j = 1; j <= gyou; j++) {
            if (document.getElementById("MAIN_" + j + "_" + i).getAttribute("data-notuse") == "1") {
                continue;
            }
            var schregno = document.getElementById("MAIN_" + j + "_" + i).getAttribute("data-schregno");
            for (var k = 0; k < tagList.length; k++) {
                if (tagList[k].getAttribute("data-schregno") == schregno) {
                    maxCnt--;
                    break;
                }
            }
            if (schregno) {
                if (document.getElementById("SEAT_NO_" + j + "_" + i).value != "") {
                    var temp = document.getElementById("SEAT_NO_" + j + "_" + i).value;
                    if (parseInt(temp) > seatNo) {
                        seatNo = parseInt(temp);
                    }
                }
            } else {
                minyuuyrokuCnt++;
            }
        }
    }
    if (maxCnt > minyuuyrokuCnt) {
        alert("割り当てられる座席が足りません。");
        return;
    }
    seatNo++;
    for (var i = 1; i <= retu; i++) {
        for (var j = 1; j <= gyou; j++) {
            var obj = document.getElementById("MAIN_" + j + "_" + i);
            if (obj.getAttribute("data-notuse") == "1") {
                continue;
            }
            if (obj.getAttribute("data-schregno") == "") {
                while (tagList[idx]) {
                    var schregno = tagList[idx].getAttribute("data-schregno");
                    if (!isExsitSchregno(schregno)) {
                        var list = tagList[idx].innerHTML.match(/<td>(.*)<\/td>\s*<td>(.*)<\/td>/);
                        var list2 = obj.innerHTML.match(/<input[^>]+>/);
                        obj.innerHTML = list2[0] + " " + list[1] + "<br> " + list[2];
                        obj.setAttribute("data-schregno", schregno);
                        document.getElementById("SEAT_NO_" + j + "_" + i).value = seatNo;
                        seatNo++;
                        idx++;
                        break;
                    } else {
                        idx++;
                    }
                }
            }
        }
    }
}

document.onkeydown = function (event) {
    if (event.keyCode == 8) {
        if (typeof event.target.tagName !== "undefined" && event.target.tagName !== false) {
            tagName = event.target.tagName.toLowerCase();
        }
        if (tagName !== "input") {
            return false;
        }
    }
    if (!selectedId) {
        return;
    }
    if (event.keyCode == 46) {
        obj = document.getElementById(selectedId);
        var list2 = obj.innerHTML.match(/<input[^>]+>/);
        obj.innerHTML = list2[0];
        obj.setAttribute("data-schregno", "");
        var idArray = selectedId.split("_");
        document.getElementById("SEAT_NO_" + idArray[1] + "_" + idArray[2]).value = "";
        obj.style.backgroundColor = "#FFFFFF";
        selectedId = null;
    }
};
function isExsitSchregno(schregno) {
    var retu = document.forms[0].retu.value;
    var gyou = document.forms[0].gyou.value;
    for (var i = 1; i <= retu; i++) {
        for (var j = 1; j <= gyou; j++) {
            if (document.getElementById("MAIN_" + j + "_" + i).getAttribute("data-schregno") == schregno) {
                return true;
            }
        }
    }
    return false;
}
