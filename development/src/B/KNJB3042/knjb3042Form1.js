var weekArray = ["MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"];
var weekWaArray = [
    "月曜日",
    "火曜日",
    "水曜日",
    "木曜日",
    "金曜日",
    "土曜日",
    "日曜日",
];
var weekWaAbbvArray = ["月", "火", "水", "木", "金", "土", "日"];
var weekValArray = ["2", "3", "4", "5", "6", "7", "1"];
var globalDelKeyList = {};
var globalDelKeyListDef = {};
var globalLayoutRightListFull = "";
var globalLayoutRightList = "";

function btn_submit(cmd) {
    //サブミット中、更新ボタン使用不可
    if (document.forms[0].btn_update) {
        document.forms[0].btn_update.disabled = true;
    }
    if (cmd == "update") {
        if (document.forms[0].BSCSEQ) {
            showDialog("ptrnUpdateBox", "基本時間割更新", ptrnUpdateInitFunc);
        } else {
            $("#lockScreen").css({ width: $(document).width() + "px" });
            $("#lockScreen").css({ height: $(document).height() + "px" });
            $("#lockScreen").css("display", "table");

            getContent(cmd);
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    } else {
        if (cmd == "reset") {
            if (!confirm("{rval MSG106}")) {
                return false;
            }
        } else if (cmd == "bscseqDelete") {
            if (
                !confirm(
                    "{rval MSG103}" +
                        "\n注意：このテンプレートの関連データも全て削除されます！"
                )
            ) {
                return false;
            }
        }
        $("#lockScreen").css({ width: $(document).width() + "px" });
        $("#lockScreen").css({ height: $(document).height() + "px" });
        $("#lockScreen").css("display", "table");

        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
}
function btn_submitPtn(cmd) {
    if ($("#ptrnUpdate_TITLE").val() == "") {
        alert("タイトルを入力してください。");
        return;
    }

    $("#lockScreen").css({ width: $(document).width() + "px" });
    $("#lockScreen").css({ height: $(document).height() + "px" });
    $("#lockScreen").css("display", "table");

    getContent("updatePtrn");
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function btn_close() {
    getContent("");
    if (
        document.forms[0].updateAddData.value != "" ||
        document.forms[0].updateDelData.value != "" ||
        document.forms[0].lineStaffInfo.value != ""
    ) {
        if (!confirm("{rval MSG108}")) {
            return false;
        }
    }
    closeWin();
}

function btn_overlapMeibo(URL, param) {
    getContent("");
    if (
        document.forms[0].updateAddData.value != "" ||
        document.forms[0].updateDelData.value != "" ||
        document.forms[0].lineStaffInfo.value != ""
    ) {
        if (!confirm("{rval MSG108}")) {
            return false;
        }
    }
    var selectCell = $("input[name=selectStartTD]").val();
    if (selectCell) {
        param += "&SELECT_CHAIRCD=" + $("#" + selectCell).attr("data-val");
        param += "&SELECT_PERIOD=" + $("#" + selectCell).attr("data-period");
        param += "&SELECT_SYORIBI=" + $("#" + selectCell).attr("data-syoribi");
    }
    wopen(URL + param, "SUBWIN2", 0, 0, screen.availWidth, screen.availHeight);
}

function dispInit() {
    if ($("#SCH_DIV3").is(":checked")) {
        //[考察]
        //通常時間割の選択不可
        $("#TEST_RADIO2").prop("checked", true);
        $("#testCdBox").show();
        $("#TEST_RADIO1").prop("disabled", true);
        //[日付校時で移動/コピー]使用不可
        $("[name=btn_moveCopy]").prop("disabled", true);
    }
}

function showWindow(URL, param, winName) {
    getContent("");
    if (
        document.forms[0].updateAddData.value != "" ||
        document.forms[0].updateDelData.value != "" ||
        document.forms[0].lineStaffInfo.value != ""
    ) {
        if (!confirm("{rval MSG108}")) {
            return false;
        }
    }

    //画面のロック
    //    $('#lockScreenPopUp').css({'width': $(document).width() + "px"});
    //    $('#lockScreenPopUp').css({'height': $(document).height() + "px"});
    //    $('#lockScreenPopUp').show();

    wopen(URL + param, winName, 0, 0, screen.availWidth, screen.availHeight);
}

function dateChange(object) {
    var dateValue = object;
    if (!dateValue) {
        return false;
    }
    if (isDate2(dateValue)) {
        //学期末日付
        var semesterEndDate = new Date();
        var semesterEndDates = document.getElementsByName("semesterEndDate");
        for (var i = 0; i < semesterEndDates.length; i++) {
            semesterEndDate = new Date(semesterEndDates[i].value);
        }

        var startDate = new Date(dateValue);
        //Toの日付へ一週間後の日付を設定
        var endDate = new Date();
        endDate.setTime(startDate.getTime() + 6 * 24 * 3600 * 1000);
        if (endDate > semesterEndDate) {
            endDate = semesterEndDate;
        }

        var mm = endDate.getMonth() + 1;
        if (mm < 10) {
            mm = "0" + mm;
        }
        var dd = endDate.getDate();
        if (dd < 10) {
            dd = "0" + dd;
        }
        var endDateElements = document.getElementsByName("END_DATE");
        for (var i = 0; i < endDateElements.length; i++) {
            endDateElements[i].value =
                endDate.getFullYear() + "/" + mm + "/" + dd;
        }
    }
}

function yomikomiTimeCheck() {
    $("#lockScreen").css({ width: $(document).width() + "px" });
    $("#lockScreen").css({ height: $(document).height() + "px" });
    $("#lockScreen").css("display", "table");

    var sdata = $("input[name=START_DATE]").val();
    var edata = $("input[name=END_DATE]").val();

    sdataTime = Date.parse(sdata);
    edataTime = Date.parse(edata);

    if (sdataTime > edataTime) {
        alert("日付の範囲が不正です");
        $("#lockScreen").hide();
        btn_submit("");
    } else if (sdataTime + 24 * 60 * 60 * 1000 * 13 < edataTime) {
        alert("日付の範囲が2週間を超えています。");
        $("#lockScreen").hide();
        btn_submit("");
    } else {
        btn_submit("edit");
    }
}
function f_dblclick(event) {
    setClickValue("chairInfo");
}

function setClickValue(cmd, kouzaId) {
    if (cmd != "999") {
        //クリックしたTD
        var idArray = document.forms[0].selectStartTD.value.split("_");
        var dateCd = idArray[1];
        var period = idArray[2];
        var line = idArray[3];
        var idName = dateCd + "_" + period + "_" + line;
        var sendDate = document.forms[0]["UPDDATE" + dateCd].value;
        var komaVal = $("#" + "KOMA_" + idName)[0].getAttribute("data-val");
        var komaExec = $("#" + "KOMA_" + idName)[0].getAttribute("data-exec");
        var komaTest = $("#" + "KOMA_" + idName)[0].getAttribute("data-test");
        var komaCountLesson = $("#" + "KOMA_" + idName)[0].getAttribute(
            "data-count-lesson"
        );

        if (komaVal == "") {
            return false;
        }
        if (!kouzaId) {
            kouzaId = "";
        }

        $("#lockScreenPopUp").css({ width: $(document).width() + "px" });
        $("#lockScreenPopUp").css({ height: $(document).height() + "px" });
        $("#lockScreenPopUp").show();

        //コマに設定されている、講座CD：更新用hidden
        loadwindow(
            "knjb3042index.php?cmd=" +
                cmd +
                "&SEND_DATE=" +
                sendDate +
                "&SEND_PERIOD=" +
                period +
                "&SEND_KOUZA=" +
                kouzaId +
                "&SEND_KOMA_VAL=" +
                komaVal +
                "&SEND_KOMA_EXEC=" +
                komaExec +
                "&SEND_KOMA_TEST=" +
                komaTest +
                "&SEND_KOMA_COUNT_LESSON=" +
                komaCountLesson,
            0,
            0,
            700,
            650
        );
        $("#dwindow").css({ "z-index": 800 });
        //ポップアップのDiv(dwindow)の最初のDivの最後のimg(要は×ボタン)のクリック処理を追加
        $("#dwindow > div:first-child > img:last-child").off("click");
        $("#dwindow > div:first-child > img:last-child").on(
            "click",
            function () {
                $("#lockScreenPopUp").hide();
            }
        );
    }
}

/***** ドラッグ開始時の処理 *****/
function f_dragstart(event) {
    //ドラッグするデータのid名をDataTransferオブジェクトにセット
    event.dataTransfer.setData("text", event.target.id);
    document.forms[0].startTD.value = event.target.id;

    //クリックしたTDがあれば、色をリセット
    f_clearSelectTDColor();
    DRAGOVER_CURRENT_ID = null;
}

/***** ドラッグ要素がドロップ要素に重なっている間の処理 *****/
//var DRAGOVER_CURRENT_ID;    //画面ちらつきを抑えるためのキャッシュ
function f_dragover(event, obj) {
    if (!obj) {
        obj = this;
    }
    //dragoverイベントをキャンセルして、ドロップ先の要素がドロップを受け付けるようにする
    event.preventDefault();
    //通過中の色設定

    if (
        obj.id == "TRASH_BOX" ||
        $("#OPERATION_RADIO3").is(":checked") ||
        $("#OPERATION_RADIO4").is(":checked") ||
        $("#OPERATION_RADIO5").is(":checked")
    ) {
        obj.style.backgroundColor = "#F58899";
    } else {
        //    if(DRAGOVER_CURRENT_ID !=obj.id){
        var idArray1 = document.forms[0].startTD.value.split("_");
        var idArray2 = obj.id.split("_");
        //        DRAGOVER_CURRENT_ID = obj.id;
        $(
            "#" +
                idArray2[0] +
                "_" +
                idArray2[1] +
                "_" +
                idArray2[2] +
                "_" +
                idArray1[3]
        ).css({ "background-color": "#F58899" });
        //    }
    }
}

/***** ドラッグ要素がドロップ要素から出る時の処理 *****/

function f_dragleave(event, obj) {
    if (!obj) {
        obj = this;
    }
    event.preventDefault();

    if (obj.id == "TRASH_BOX") {
        obj.style.backgroundColor = "#003366";
    } else if ($("#OPERATION_RADIO3").is(":checked")) {
        obj.style.backgroundColor = "";
    } else if ($("#OPERATION_RADIO4").is(":checked")) {
        obj.style.backgroundColor = "";
    } else if ($("#OPERATION_RADIO5").is(":checked")) {
        obj.style.backgroundColor = "";
    } else {
        var idArray1 = document.forms[0].startTD.value.split("_");
        var idArray2 = obj.id.split("_");
        $(
            "#" +
                idArray2[0] +
                "_" +
                idArray2[1] +
                "_" +
                idArray2[2] +
                "_" +
                idArray1[3]
        ).css({ "background-color": "" });
    }
}

/***** ドラッグの基本イベント *****/
function f_dragevent(event) {
    event.preventDefault();
}

/***** コンテキストメニューイベント *****/
function f_contextmenu() {
    return false;
}

/***** ドロップ時の処理 *****/
function f_drop(event) {
    obj = this;

    //ドラッグされたデータのid名をDataTransferオブジェクトから取得
    var id_name = event.dataTransfer.getData("text");
    var dragIdArray = id_name.split("_");
    var dragWeekName = dragIdArray[1];
    var dragPeriod = dragIdArray[2];
    var dragLine = dragIdArray[3];
    var cntNum = "";
    //ポップアップの時は４番目がある
    if (dragIdArray.length > 4) {
        cntNum = dragIdArray[4];
    }
    //id名からドラッグされた要素を取得
    var drag_elm = $(
        "#" +
            dragIdArray[0] +
            "_" +
            dragIdArray[1] +
            "_" +
            dragIdArray[2] +
            "_" +
            dragIdArray[3]
    )[0];
    if (!drag_elm) {
        event.preventDefault();
        return;
    }
    //ドロップされたTD
    var dropIdArray = this.id.split("_");
    if (
        $("#OPERATION_RADIO3").is(":checked") ||
        $("#OPERATION_RADIO4").is(":checked") ||
        $("#OPERATION_RADIO5").is(":checked")
    ) {
        var targetBox = $("#" + this.id)[0];
    } else {
        var targetBox = $(
            "#KOMA_" + dropIdArray[1] + "_" + dropIdArray[2] + "_" + dragLine
        )[0];
    }
    if (
        $("#OPERATION_RADIO1").is(":checked") &&
        dragIdArray[0] == dropIdArray[0] &&
        dragIdArray[1] == dropIdArray[1] &&
        dragIdArray[2] == dropIdArray[2]
    ) {
        alert("この処理では縦移動することはできません。");
    } else if ($("#SCH_DIV3").is(":checked") && dragLine == "1") {
        //考査の場合は[通常講座]の移動は不可
        alert("この処理では移動することはできません。");
    } else if (drag_elm != targetBox) {
        var srcLinking = drag_elm.getAttribute("data-linking");
        var MObj = new MoveBlockObj();

        var chairList = [];
        if (drag_elm.getAttribute("data-val")) {
            if (cntNum == "" || cntNum == "all") {
                chairList = drag_elm.getAttribute("data-val").split(":");
            } else {
                chairList.push(
                    drag_elm.getAttribute("data-val").split(":")[cntNum]
                );
            }
        }

        var key_event = event || window.event;
        var isShift = key_event.shiftKey;
        if (isShift) {
            MObj.execMoveBlockCopy(drag_elm, cntNum, targetBox, true);
            writeOperationHistory("ADD", chairList, "", targetBox);
        } else {
            var LObj = new LinkingCellObj();
            if ($("#OPERATION_RADIO1").is(":checked")) {
                if (MObj.isSyussekiOverDay(drag_elm, cntNum, targetBox, 1)) {
                    alert(
                        "出席情報が含まれてます。出席情報は日を変えることはできません。"
                    );
                    return;
                }
                MObj.execMoveBlock(drag_elm, cntNum, targetBox, true);
                //操作履歴追加
                if (MObj.error == 0) {
                    writeOperationHistory(
                        "MOVE",
                        chairList,
                        drag_elm,
                        targetBox
                    );
                }
                LObj.checkMeiboAndFac(
                    drag_elm.id,
                    srcLinking,
                    targetBox.id,
                    targetBox.getAttribute("data-linking"),
                    document.forms[0].MAX_LINE.value,
                    function () {
                        alert("出席情報が重複しています。");
                        MObj.rollback();
                        //操作履歴削除
                        deleteLastOperationHistory();
                    }
                );
            } else if ($("#OPERATION_RADIO2").is(":checked")) {
                MObj.execMoveBlockCopy(drag_elm, cntNum, targetBox, true);
                //操作履歴追加
                if (MObj.error == 0) {
                    writeOperationHistory("ADD", chairList, "", targetBox);
                }
                LObj.checkMeiboAndFac(
                    drag_elm.id,
                    srcLinking,
                    targetBox.id,
                    targetBox.getAttribute("data-linking"),
                    document.forms[0].MAX_LINE.value,
                    function () {
                        alert("出席情報が重複しています。");
                        //操作履歴削除
                        deleteLastOperationHistory();
                        MObj.rollback();
                    }
                );
            } else if ($("#OPERATION_RADIO3").is(":checked")) {
                if (MObj.isSyussekiOverDay(drag_elm, cntNum, targetBox, 3)) {
                    alert(
                        "出席情報が含まれてます。出席情報は日を変えることはできません。"
                    );
                    return;
                }
                IObj = new IrekaeBlockObj();
                srcMObjKyousi = IObj.execIrekaeKyousi(
                    drag_elm,
                    cntNum,
                    targetBox,
                    true
                );
                //操作履歴追加
                var fromStaff = "";
                var toStaff = "";
                // dragIdArray
                fromStaff = $(
                    ".redips-mark[data-linecnt=" + dragIdArray[3] + "]"
                ).attr("data-keyname");
                if (fromStaff == "--") fromStaff = "";
                if (fromStaff == "**") fromStaff = "";
                toStaff = $(
                    ".redips-mark[data-linecnt=" + dropIdArray[3] + "]"
                ).attr("data-keyname");
                if (toStaff == "--") toStaff = "";
                if (toStaff == "**") toStaff = "";
                writeOperationHistory(
                    "STAFF",
                    chairList,
                    drag_elm,
                    targetBox,
                    fromStaff,
                    toStaff
                );
                LObj.checkMeiboAndFac(
                    drag_elm.id,
                    srcLinking,
                    targetBox.id,
                    targetBox.getAttribute("data-linking"),
                    document.forms[0].MAX_LINE.value,
                    function () {
                        alert("出席情報が重複しています。");
                        srcMObjKyousi.rollback();
                        //操作履歴削除
                        deleteLastOperationHistory();
                    }
                );
            } else if ($("#OPERATION_RADIO4").is(":checked")) {
                if (MObj.isSyussekiOverDay(drag_elm, cntNum, targetBox, 3)) {
                    alert(
                        "出席情報が含まれてます。出席情報は日を変えることはできません。"
                    );
                    return;
                }
                IObj = new IrekaeBlockObj();
                srcMObjKyousi = IObj.execIrekaeKyousiCopy(
                    drag_elm,
                    cntNum,
                    targetBox,
                    true
                );
                //操作履歴追加
                writeOperationHistory("ADD", chairList, "", targetBox);
                LObj.checkMeiboAndFac(
                    drag_elm.id,
                    srcLinking,
                    targetBox.id,
                    targetBox.getAttribute("data-linking"),
                    document.forms[0].MAX_LINE.value,
                    function () {
                        alert("出席情報が重複しています。");
                        srcMObjKyousi.rollback();
                        //操作履歴削除
                        deleteLastOperationHistory();
                    }
                );
            } else if ($("#OPERATION_RADIO5").is(":checked")) {
                if (MObj.isSyussekiOverDay(drag_elm, cntNum, targetBox, 4)) {
                    alert(
                        "出席情報が含まれてます。出席情報は日を変えることはできません。"
                    );
                    return;
                }
                var targetChairList = [];
                if (targetBox.getAttribute("data-val")) {
                    targetChairList = targetBox
                        .getAttribute("data-val")
                        .split(":");
                }

                if (
                    document.forms[0].KNJB3042_SchTestPattern.value == "1" &&
                    !$("#SCH_DIV3").is(":checked")
                ) {
                    $(window).on(
                        "moveOneblockEvent",
                        function (
                            e,
                            MObjE,
                            srcBoxE,
                            cntNumE,
                            targetBoxE,
                            isCheckE
                        ) {
                            var srcDataTest = srcBoxE.getAttribute("data-test");
                            var srcDataTestList =
                                srcDataTest == ""
                                    ? new Array()
                                    : srcDataTest.split(",");
                            if (srcDataTestList[cntNumE] != "0") {
                                MObjE.moveOneblockEventCancel = true;
                            }
                        }
                    );
                }
                IObj = new IrekaeBlockObj();
                IObj.execIrekae(drag_elm, cntNum, targetBox);
                //操作履歴追加
                //移動元→移動先
                writeOperationHistory("MOVE", chairList, drag_elm, targetBox);
                if (targetChairList.length > 0) {
                    //移動先→移動元
                    writeOperationHistory(
                        "MOVE",
                        targetChairList,
                        targetBox,
                        drag_elm
                    );
                }
                LObj.checkMeiboAndFac(
                    drag_elm.id,
                    srcLinking,
                    targetBox.id,
                    targetBox.getAttribute("data-linking"),
                    document.forms[0].MAX_LINE.value,
                    function () {
                        alert("出席情報が重複しています。");
                        IObj.rollback();
                        //操作履歴削除
                        deleteLastOperationHistory();
                        if (targetChairList.length > 0) {
                            deleteLastOperationHistory();
                        }
                    }
                );
                if (
                    document.forms[0].KNJB3042_SchTestPattern.value == "1" &&
                    !$("#SCH_DIV3").is(":checked")
                ) {
                    $(window).off("moveOneblockEvent");
                }
            }
        }
        selectSubclass("getChair", null);
    }
    dodragleave();
    //エラー回避のため、ドロップ処理の最後にdropイベントをキャンセルしておく
    event.preventDefault();
    //ドロップ後色を戻す
    obj.style.backgroundColor = "";
    $(
        "#KOMA_" + dropIdArray[1] + "_" + dropIdArray[2] + "_" + dragLine
    )[0].style.backgroundColor = "";
}

/***** ゴミ箱ドロップ時の処理 *****/
function f_dropTrash(event, obj) {
    //ドラッグされたデータのid名をDataTransferオブジェクトから取得
    var komaId_name = event.dataTransfer.getData("text");
    //id名からドラッグされた要素を取得
    var drag_elm = $("#" + komaId_name)[0];

    var id_name = event.dataTransfer.getData("text");

    var dragIdArray = id_name.split("_");
    var dragWeekName = dragIdArray[1];
    var dragPeriod = dragIdArray[2];
    var dragLine = dragIdArray[3];
    var cntNum = "";
    if (dragIdArray.length > 4) {
        cntNum = dragIdArray[4];
    }
    var drag_elm = $(
        "#" +
            dragIdArray[0] +
            "_" +
            dragIdArray[1] +
            "_" +
            dragIdArray[2] +
            "_" +
            dragIdArray[3]
    )[0];

    isDeleteDialogShow(
        drag_elm,
        cntNum,
        function () {
            makeDeleteDataList();
            var tdId = document.forms[0].selectStartTD.value;
            document.forms[0].selectStartTD.value = id_name;
            showDialog("deleteSelectBox", "削除", deleteSelectInitFunc);
            document.forms[0].selectStartTD.value = tdId;
        },
        function () {
            var MObj = new MoveBlockObj();
            var LObj = new LinkingCellObj();
            kouziList = LObj.makeKouziLsit(drag_elm);

            var chairList = [];
            if (drag_elm.getAttribute("data-val")) {
                if (cntNum == "" || cntNum == "all") {
                    chairList = drag_elm.getAttribute("data-val").split(":");
                } else {
                    chairList.push(
                        drag_elm.getAttribute("data-val").split(":")[cntNum]
                    );
                }
            }
            makeDeleteDataList(cntNum);
            MObj.deleteMoveObj(drag_elm, cntNum);

            // 操作履歴追加
            writeOperationHistory("DEL", chairList, drag_elm, "");

            var calcList = LObj.makeCalcList(
                drag_elm,
                new Array(),
                kouziList,
                document.forms[0].MAX_LINE.value
            );
            LObj.checkMeiboAndFacUseCalcList(
                calcList,
                document.forms[0].MAX_LINE.value
            );
            dodragleave();
            selectSubclass("getChair", null);
        }
    );
    //エラー回避のため、ドロップ処理の最後にdropイベントをキャンセルしておく
    event.preventDefault();

    //ドロップ後色を戻す
    obj.style.backgroundColor = "#003366";
    //alert(id_name);
}

//削除するデータをグローバル変数にセットするイベント
function makeDeleteDataList(inCntNum) {
    //イベントキャンセル(キャンセルしないと、通る度にイベントが重なる)
    $(window).off("setCellObjEmptyTargetEvent");
    //イベント追加(削除ボタン押下の処理)
    $(window).on(
        "setCellObjEmptyTargetEvent",
        function (e, cellObj, srcBox, cntNum) {
            var srcDataDef = srcBox.getAttribute("data-def");
            var dataDefList =
                srcDataDef == "" ? new Array() : srcDataDef.split(",");
            for (var i = 0; i < dataDefList.length; i++) {
                if (inCntNum != "" && i != cntNum) continue;
                globalDelKeyList[dataDefList[i]] = true;
            }
        }
    );
}

/***** クリックしたTDを保存 *****/
function f_click(event) {
    obj = this;
    //keyEvent
    var key_event = event || window.event;
    //Shift
    var isShift = key_event.shiftKey;

    //前回にクリックしたTDがあれば、色をリセット
    f_clearSelectTDColor();
    //TDに色を付ける
    document.forms[0].selectStartTD.value = obj.id;

    var idArray = obj.id.split("_");
    var lineHed = $(".redips-mark[data-linecnt=" + idArray[3] + "]")[0];
    var lineKey = $(lineHed).attr("data-keyname");
    //クリック時、右側項目の絞り込みを行う
    if ($("select[name=LEFT_MENU]").val() == "1") {
        $("select[name=STAFFCD]").val(lineKey);
    } else if ($("select[name=LEFT_MENU]").val() == "2") {
        // TODO: 年組のコード変換が必要 XXXXX → XX-XXX
        lineKey = lineKey.substring(0, 2) + "-" + lineKey.substring(2);
        $("select[name=GRAND_HR_CLASSCD]").val(lineKey);
    } else if ($("select[name=LEFT_MENU]").val() == "3") {
        $("select[name=SUBCLASSCD]").val(lineKey);
    } else if ($("select[name=LEFT_MENU]").val() == "4") {
        // TODO:講座番号から科目を取得
        var chairList = new Array();
        if (lineKey) {
            chairList.push(lineKey);
            var ajaxParam = { AJAX_KOUZA_PARAM: JSON.stringify(chairList) };
            $.ajax({
                url: "knjb3042index.php",
                type: "POST",
                data: {
                    AJAX_PARAM: JSON.stringify(ajaxParam),
                    cmd: "getFacilityKouzaList",
                    YEAR_SEME: $(
                        "select[name=YEAR_SEME] option:selected"
                    ).val(),
                },
                async: false,
            }).done(function (data, textStatus, jqXHR) {
                var paramList = $.parseJSON(data);
                var lineKey = "";
                for (let index = 0; index < paramList.length; index++) {
                    const element = paramList[index];
                    lineKey = element["Kamoku"];
                }
                $("select[name=SUBCLASSCD]").val(lineKey);
            });
        }
    }

    obj.style.backgroundColor = "#F5F599";
    selectSubclassRemoveIds(obj.id.replace(/TD/g, "KOMA"));

    doclick(event);
    if (!ActiveFlag) {
        dodragleave();
    }
    ActiveFlag = false;
}

/***** 前回クリックしたTDの色をクリア *****/
function f_clearSelectTDColor() {
    var select_elm = $("#" + document.forms[0].selectStartTD.value)[0];
    if (select_elm != null) {
        select_elm.style.backgroundColor = "";
    }
}

/***** Shift+Click時のTDの色 *****/
function f_SelectShiftTDColor() {
    var select_elm = $("#" + document.forms[0].selectStartTD.value)[0];
    if (select_elm != null) {
        select_elm.style.backgroundColor = "";
    }
}

/***** 選択した講座をセット *****/
function setChair() {
    //クリックしたTD
    var idArray = document.forms[0].selectStartTD.value.split("_");
    var dateCd = idArray[1];
    var period = idArray[2];
    var line = idArray[3];
    var idName = dateCd + "_" + period + "_" + line;
    //講座リスト
    var selectList = document.forms[0].CATEGORY_SELECTED;
    //定期考査
    var isTestRadio = $("#TEST_RADIO2").is(":checked");

    //イベントキャンセル
    $(window).off("setCellObjEmptyTargetEvent");

    //講座CD:[1,5,10]この講座の割当られる行番号(職員だったら講座担当の行番号)
    var chairList = JSON.parse($("input[name=chairList]").val());

    var haveSelectData = false;
    for (var i = 0; i < selectList.length; i++) {
        if (selectList.options[i].selected) {
            haveSelectData = true;
            break;
        }
    }
    if (haveSelectData) {
        for (var i = 0; i < selectList.length; i++) {
            if (selectList.options[i].selected == false) {
                continue;
            }
            //講座CD:連続授業:講座ABBV
            var selectArray = selectList.options[i].value.split(":");
            var targetStaffList = chairList[selectArray[0]];
            if (targetStaffList == null) {
                targetStaffList = $("#SCH_DIV3").is(":checked") ? [2] : [1];
            }
            //講座を割振る行。例：[1,5,10]
            for (var j = 0; j < targetStaffList.length; j++) {
                var renzoku = selectArray[1];
                //定期考査の場合は1時間のみ追加
                if (isTestRadio) {
                    renzoku = 1;
                }
                //連続授業
                for (var k = 0; k < renzoku; k++) {
                    var targetBox = $(
                        "#" +
                            "KOMA_" +
                            dateCd +
                            "_" +
                            (parseInt(period) + k) +
                            "_" +
                            targetStaffList[j]
                    )[0];
                    if (!targetBox) {
                        alert("セルが範囲外です");
                        return false;
                    }
                    //移動先のセルに同一講座があるかチェック
                    var attrDataVal = targetBox
                        .getAttribute("data-val")
                        .split(":");
                    for (l = 0; l < attrDataVal.length; l++) {
                        if (attrDataVal[l] == selectArray[0]) {
                            alert("同一講座が設定されています。");
                            return false;
                        }
                    }
                }
            }
        }
        //selectList=CATEGORY_SELECTED
        for (var i = 0; i < selectList.length; i++) {
            if (selectList.options[i].selected == false) {
                continue;
            }
            //講座CD:連続授業:講座ABBV
            var selectArray = selectList.options[i].value.split(":");

            //講座CD:[1,5,10]
            var targetStaffList = chairList[selectArray[0]];
            if (targetStaffList == null) {
                targetStaffList = $("#SCH_DIV3").is(":checked") ? [2] : [1];
            }

            var rereki = new Array();
            //講座を割振る行。例：[1,5,10]
            for (var j = 0; j < targetStaffList.length; j++) {
                //連続授業有無(通常時間割)
                if (
                    selectArray[1] != "" &&
                    selectArray[1] > 1 &&
                    !isTestRadio
                ) {
                    //                    var linkingText = selectArray[0] + ':';
                    //
                    //                    //連続授業でループ。各セルを連結していく。講座CD:KOMA_日付_校時_Line,KOMA_日付_校時_Line
                    //                    for (k = 0; k < selectArray[1]; k++) {
                    //                        linkingText = linkingText + 'KOMA_' + dateCd + "_" + (parseInt(period) + k) + "_" + targetStaffList[j] + ',';
                    //                    }
                    //                    linkingText = linkingText.replace(/,$/g,'');    //最後の,を削除

                    //連続授業は反映ボタン押下時のみ
                    //ドラッグ移動時は1コマ単位での移動
                    var linkingText = "";
                    //連続授業でループ。各セルの内容をセットする。
                    for (k = 0; k < selectArray[1]; k++) {
                        var targetId =
                            "KOMA_" +
                            dateCd +
                            "_" +
                            (parseInt(period) + k) +
                            "_" +
                            targetStaffList[j];
                        rereki.push(
                            insertCellData(
                                $("#" + targetId)[0],
                                selectList.options[i],
                                linkingText
                            )
                        );
                    }
                } else {
                    rereki.push(
                        insertCellData(
                            $(
                                "#" +
                                    "KOMA_" +
                                    dateCd +
                                    "_" +
                                    period +
                                    "_" +
                                    targetStaffList[j]
                            )[0],
                            selectList.options[i],
                            ""
                        )
                    );
                }
            }
            var id = "KOMA_" + dateCd + "_" + period + "_" + targetStaffList[0];

            //操作履歴追加
            var targetChairList = [];
            targetChairList.push(selectArray[0]);
            for (k = 0; k < selectArray[1]; k++) {
                var targetId =
                    "KOMA_" +
                    dateCd +
                    "_" +
                    (parseInt(period) + k) +
                    "_" +
                    targetStaffList[0];
                writeOperationHistory(
                    "ADD",
                    targetChairList,
                    "",
                    $("#" + targetId)[0]
                );
            }

            var LObj = new LinkingCellObj();
            LObj.checkMeiboAndFac(
                id,
                $("#" + id)[0].getAttribute("data-linking"),
                id,
                $("#" + id)[0].getAttribute("data-linking"),
                document.forms[0].MAX_LINE.value,
                function () {
                    alert("出席情報が重複しています。");
                    for (var i = rereki.length - 1; i >= 0; i--) {
                        for (var key in rereki[i]) {
                            if (key != "id" && key != "innerHTML") {
                                $("#" + rereki[i]["id"])[0].setAttribute(
                                    key,
                                    rereki[i][key]
                                );
                            }
                        }
                        $("#" + rereki[i]["id"])[0].innerHTML =
                            rereki[i]["innerHTML"];
                    }
                    selectSubclassRemoveIds("KOMA_" + idName);
                    //操作履歴削除
                    deleteLastOperationHistory();
                }
            );
        }
        selectSubclassRemoveIds("KOMA_" + idName);
    }

    return true;
}

//反映ボタンの処理
function insertCellData(targetBox, selectOption, linkingText) {
    var attrDataDef = targetBox.getAttribute("data-def");
    //data-val : 1234567:1234568(講座)
    var attrDataVal = targetBox.getAttribute("data-val");
    var textArray = selectOption.text.split(":");
    //data-text : 1234567<BR>講座名1,1234568<BR>講座名2
    var attrDataText = targetBox.getAttribute("data-text");
    //data-test : 0101,0102(テストCD)
    var attrDataTest = targetBox.getAttribute("data-test");
    var selectArray = selectOption.value.split(":");
    var setVal = selectArray[0];
    var attrDataExec = targetBox.getAttribute("data-exec");
    var attrDataZyukou = targetBox.getAttribute("data-zyukou");
    var attrDataFacility = targetBox.getAttribute("data-selectfacility");
    var attrDataTestFacility = targetBox.getAttribute(
        "data-selecttestfacility"
    );
    var attrDataCountLesson = targetBox.getAttribute("data-count-lesson");
    var attrDirty = targetBox.getAttribute("data-dirty");

    var setDisp = selectArray[0] + "<br>" + selectArray[2];

    var targetBoxIdArray = targetBox.id.split("_");

    var ret = {};
    ret["id"] = targetBox.id;
    ret["innerHTML"] = targetBox.innerHTML;
    ret["data-def"] = attrDataDef;
    ret["data-val"] = attrDataVal;
    ret["data-text"] = attrDataText;
    ret["data-test"] = attrDataTest;
    ret["data-exec"] = attrDataExec;
    ret["data-zyukou"] = attrDataZyukou;
    ret["data-selectfacility"] = attrDataFacility;
    ret["data-selecttestfacility"] = attrDataTestFacility;
    ret["data-count-lesson"] = attrDataCountLesson;
    ret["data-dirty"] = attrDirty;

    if ($("#SCH_DIV1").is(":checked")) {
        var newDate = targetBoxIdArray[1];
    } else {
        var sdataTime = new Date(Date.parse($("input[name=START_DATE]").val()));
        sdataTime.setDate(sdataTime.getDate() + parseInt(targetBoxIdArray[1]));
        var year = sdataTime.getFullYear();
        var month = sdataTime.getMonth() + 1;
        var day = sdataTime.getDate();
        var newDate = year + "-" + month + "-" + day;
    }
    var execKey = "MI_SYUKKETSU";

    //選択したセルに講座があるか
    if (attrDataVal.length > 0) {
        setVal = attrDataVal + ":" + selectArray[0];
        var kensuArray = setVal.split(":");
        attrDataText = attrDataText + "," + setDisp;
        attrDataTestTemp = $("#TEST_RADIO2").is(":checked")
            ? $("#TESTCD").val()
            : "0";
        attrDataTest = attrDataTest + "," + attrDataTestTemp;
        attrDataExec = attrDataExec + "," + execKey;
        attrDataZyukou = attrDataZyukou + ",0";
        attrDataFacility = attrDataFacility + ",0";
        attrDataTestFacility = attrDataTestFacility + ",0";
        attrDataCountLesson = attrDataCountLesson + ",00-000/1-00";
        attrDataDef =
            attrDataDef +
            "," +
            newDate +
            "_" +
            targetBoxIdArray[2] +
            "_" +
            selectArray[0] +
            "_" +
            attrDataTestTemp +
            "_" +
            targetBoxIdArray[3] +
            "_0" +
            "_00-000/1-00" +
            "_Add";
        setDisp = kensuArray.length + "件のデータ";
    } else {
        attrDataText = setDisp;
        attrDataTest = $("#TEST_RADIO2").is(":checked")
            ? $("#TESTCD").val()
            : "0";
        attrDataExec = execKey;
        attrDataZyukou = "0";
        attrDataFacility = "0";
        attrDataTestFacility = "0";
        attrDataCountLesson = "00-000/1-00";
        attrDataDef =
            newDate +
            "_" +
            targetBoxIdArray[2] +
            "_" +
            selectArray[0] +
            "_" +
            attrDataTest +
            "_" +
            targetBoxIdArray[3] +
            "_0" +
            "_00-000/1-00" +
            "_Add";
    }
    targetBox.innerHTML = setDisp;
    targetBox.setAttribute("data-def", attrDataDef);
    targetBox.setAttribute("data-text", attrDataText);
    targetBox.setAttribute("data-test", attrDataTest);
    targetBox.setAttribute("data-val", setVal);
    targetBox.setAttribute("data-exec", attrDataExec);
    targetBox.setAttribute("data-zyukou", attrDataZyukou);
    targetBox.setAttribute("data-selectfacility", attrDataFacility);
    targetBox.setAttribute("data-selecttestfacility", attrDataTestFacility);
    targetBox.setAttribute("data-count-lesson", attrDataCountLesson);
    targetBox.setAttribute("data-dirty", "1");
    // 講座:KOMA_日付_校時_行
    // ７校時に1234567(連続2)、８校時に1234568(連続2)
    //data-linking : ７校時 1234567:KOMA_1_7_1,KOMA_1_8_1
    //data-linking : ８校時 1234567:KOMA_1_7_1,KOMA_1_8_1/1234568:KOMA_1_8_1,KOMA_1_9_1
    //data-linking : ９校時 1234568:KOMA_1_8_1,KOMA_1_9_1
    var attrLinking = targetBox.getAttribute("data-linking");
    if (attrLinking != "" && linkingText != "") {
        attrLinking = attrLinking + "/";
    }
    targetBox.setAttribute("data-linking", attrLinking + linkingText);
    var Obj = new CellObj();
    Obj.setCellObjEmptyTarget(targetBox);
    Obj.writeClass(Obj.src);

    return ret;
}
//更新ボタンの処理
//設定したデータの取得
function getContent(cmd) {
    var sepAdd = "";
    var sepDel = "";
    var sepStf = "";
    docForm = document.forms[0];
    docForm.updateAddData.value = "";
    docForm.updateDelData.value = "";
    docForm.lineStaffInfo.value = "";
    var keyList = {};
    if (docForm.MAX_LINE) {
        for (lineCnt = 1; lineCnt <= docForm.MAX_LINE.value; lineCnt++) {
            for (dateCnt = 0; dateCnt < docForm.DATECNT_MAX.value; dateCnt++) {
                for (
                    periCnt = 1;
                    periCnt <= docForm.MAX_PERIOD.value;
                    periCnt++
                ) {
                    var id = "KOMA_" + dateCnt + "_" + periCnt + "_" + lineCnt;
                    var targetBox = $("#" + id)[0];
                    //data-val : 1234567:1234568(講座)
                    var dataVal = targetBox.getAttribute("data-val");
                    var dataDef = targetBox.getAttribute("data-def");
                    if (dataVal == "" && dataDef == "") {
                        continue;
                    }
                    var dataValList =
                        dataVal == "" ? new Array() : dataVal.split(":");
                    var dataDefList =
                        dataDef == "" ? new Array() : dataDef.split(",");
                    for (var i = 0; i < dataDefList.length; i++) {
                        if ($("#SCH_DIV1").is(":checked")) {
                            dateDispVal = dateCnt;
                        } else {
                            var dateDispVal =
                                docForm["DATEDISP" + dateCnt].value;
                            dateDispVal = dateDispVal.substring(0, 10);
                            dateDispVal = dateDispVal.replace(/\//g, "-");
                        }
                        //data-test : 0101,0102(テストCD)
                        var dataTest = targetBox.getAttribute("data-test");
                        var dataTestList =
                            dataTest == "" ? new Array() : dataTest.split(",");

                        var keyStr =
                            dateDispVal + "_" + periCnt + "_" + dataValList[i];
                        if (dataTestList[i] != null) {
                            keyStr += "_" + dataTestList[i];
                        } else {
                            keyStr += "_0";
                        }
                        keyStr += "_" + lineCnt;

                        var dataFac = targetBox.getAttribute(
                            "data-selectfacility"
                        );
                        var dataFacList =
                            dataFac == "" ? new Array() : dataFac.split(",");
                        keyStr += "_" + dataFacList[i];

                        var dataTestFac = targetBox.getAttribute(
                            "data-selecttestfacility"
                        );
                        var dataTestFacList =
                            dataTestFac == ""
                                ? new Array()
                                : dataTestFac.split(",");
                        keyStr += "_" + dataTestFacList[i];

                        var dataCountLesson = targetBox.getAttribute(
                            "data-count-lesson"
                        );
                        var dataCountLessonList =
                            dataCountLesson == ""
                                ? new Array()
                                : dataCountLesson.split(",");
                        keyStr += "_" + dataCountLessonList[i];

                        if (!keyList[dataValList[i]]) {
                            keyList[dataValList[i]] = {
                                flag: false,
                                list: new Array(),
                            };
                        }
                        keyList[dataValList[i]].list.push(keyStr);
                        if (cmd != "updatePtrn" && dataDefList[i] == keyStr) {
                            continue;
                        }
                        keyList[dataValList[i]].flag = true;

                        keyStr = dataDefList[i] + "," + keyStr;
                        if (docForm.updateAddData.value.indexOf(keyStr) == -1) {
                            docForm.updateAddData.value += sepAdd + keyStr;
                            sepAdd = "|";
                        }
                    }
                }
            }
        }
    }
    for (var key in globalDelKeyList) {
        if (key.indexOf("_Add") != -1) {
            continue;
        }
        var keyStr = key + ",";
        if (docForm.updateDelData.value.indexOf(keyStr) == -1) {
            docForm.updateDelData.value += sepDel + keyStr;
            sepDel = "|";
        }
    }
    for (var key in globalDelKeyListDef) {
        var list = key.split("_");
        if (keyList[list[2]]) {
            keyList[list[2]].flag = true;
        }
    }
    for (var chairCd in keyList) {
        if (keyList[chairCd].flag) {
            for (var i = 0; i < keyList[chairCd].list.length; i++) {
                keyStr = keyList[chairCd].list[i];
                if (docForm.lineStaffInfo.value.indexOf(keyStr) == -1) {
                    docForm.lineStaffInfo.value += sepStf + keyStr;
                    sepStf = "|";
                }
            }
        }
    }
    if (cmd == "kakunin") {
        alert("追加\n" + docForm.updateAddData.value);
        alert("削除\n" + docForm.updateDelData.value);
        alert("教員\n" + docForm.lineStaffInfo.value);
    }
    return;
}

//科目を選択したとき
//TODO:処理の最後にこの関数をコールしていない箇所がないか調査
function selectSubclass(cmd, removeIds, selectChaircd) {
    $("#divChair")[0].innerHTML =
        '<select name="CATEGORY_SELECTED" size="20"></select>'; //空のコンボを表示

    var sendData = "";
    var form_datas = document.forms[0];
    form_datas.cmd.value = cmd;

    var sendData = {
        YEAR_SEME: form_datas.YEAR_SEME.value,
        GRAND_HR_CLASSCD: form_datas.GRAND_HR_CLASSCD.value,
        SUBCLASSCD: form_datas.SUBCLASSCD.value,
        GUNCD: form_datas.GUNCD.value,
        STAFFCD: form_datas.STAFFCD.value,
        COUSECD: form_datas.COUSECD.value,
        REMOVE_IDS: JSON.stringify(removeIds),
        cmd: form_datas.cmd.value,
    };
    //post(呼び出すurl, 引数, 処理後に走る関数)
    $.post("knjb3042index.php?", sendData, function (data) {
        $("#divChair")[0].innerHTML = data;
        if (form_datas.GUNCD.value != "") {
            //群コンボが選択されていれば、講座リストを選択状態にする。
            $("select[name=CATEGORY_SELECTED] option").prop("selected", true);
        } else {
            //選択していた講座をselectedに再設定
            if (selectChaircd) {
                for (var i = 0; i < selectChaircd.length; i++) {
                    if (
                        $(
                            'select[name=CATEGORY_SELECTED] option[value="' +
                                selectChaircd[i] +
                                '"]'
                        ).length
                    ) {
                        $(
                            'select[name=CATEGORY_SELECTED] option[value="' +
                                selectChaircd[i] +
                                '"]'
                        ).prop("selected", true);
                    }
                }
            }
        }
    });
}
//ポップアップを非表示
function dodragleave(e) {
    var obj = $("#box")[0];
    obj.classList.add("non_active_box");
    obj.classList.remove("active_box");
}
var CTarget;
var ActiveFlag;
function doclick(e) {
    //console.log($('#'+e.currentTarget.id)[0].getAttribute('data-test'));
    var obj = $("#box")[0];
    //自分自身でポップアップが出ていれば非表示
    if (CTarget == e.currentTarget && obj.className == "active_box") {
        obj.classList.add("non_active_box");
        obj.classList.remove("active_box");
    } else {
        //ポップアップが出てなければ表示
        var text = e.target.getAttribute("data-text");
        var list = text.split(",");
        if (list.length > 1) {
            var dataTest = $("#" + e.currentTarget.id)[0].getAttribute(
                "data-test"
            );
            var dataTestList =
                dataTest == "" ? new Array() : dataTest.split(",");
            var inHtml = "";
            var ZenkenStyle =
                ' style="background-color:#CCFFCC" draggable="true"';
            for (var i = 0; i < list.length; i++) {
                var SchTestPatternHtml = ' draggable="true"';
                if (
                    document.forms[0].KNJB3042_SchTestPattern.value == "1" &&
                    !$("#SCH_DIV3").is(":checked") &&
                    dataTestList[i] != "0"
                ) {
                    SchTestPatternHtml = ' style="background-color:#CCCCCC"';
                    ZenkenStyle = ' style="background-color:#CCCCCC"';
                }
                inHtml +=
                    '<div id="' +
                    e.currentTarget.id +
                    "_" +
                    i +
                    '" class="inner_box" ' +
                    SchTestPatternHtml +
                    ">" +
                    list[i] +
                    "</div>";
            }
            inHtml +=
                '<div id="' +
                e.currentTarget.id +
                '_all" class="inner_box"' +
                ZenkenStyle +
                ">全件移動</div>";
            obj.innerHTML = inHtml;
            var elements = $(".inner_box");
            for (var i = 0; i < elements.length; i++) {
                elements[i].ondragstart = f_dragstart;
            }
            obj.style.top = e.pageY + 10;
            obj.style.left = e.pageX + 10;
            //activeにしないと座標がとれない
            obj.classList.add("active_box");
            obj.classList.remove("non_active_box");
            //座標の再計算、一番下の時にセルの上方向、右なら左方向といった感じ
            pos = setposition(e, obj, $("#tbody")[0]);
            obj.style.top = pos.y;
            obj.style.left = pos.x;
            ActiveFlag = true;
        }
    }
    CTarget = e.currentTarget;
}
function setposition(e, box1, box2) {
    pos1 = getElementPosition(box1);
    pos2 = getElementPosition(box2);
    if (pos1.x + box1.clientWidth > pos2.x + box2.clientWidth) {
        x = e.pageX - 20 - box1.clientWidth;
    } else {
        x = e.pageX + 10;
    }
    if (pos1.y + box1.clientHeight > pos2.y + box2.clientHeight) {
        y = e.pageY - 20 - box1.clientHeight;
    } else {
        y = e.pageY + 10;
    }
    return { x: x, y: y };
}
function getElementPosition(elm) {
    var fn = arguments.callee, // 再帰用
        rslt = { x: 0, y: 0 },
        p;
    if (elm) {
        rslt = {
            x: parseInt(elm.offsetLeft),
            y: parseInt(elm.offsetTop),
        };
        if (elm.offsetParent) {
            p = fn(elm.offsetParent);
            rslt.x += p.x;
            rslt.y += p.y;
        }
    }
    return rslt;
}

function calc(obj) {
    //スペース削除
    var str_num = obj.value;
    obj.value = str_num.replace(/ |　/g, "");
    var str = obj.value;
    var nam = obj.name;

    //数字チェック
    obj.value = toInteger(obj.value);
    btn_submit("edit");
}

//権限チェック
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}
//スクロール
function scrollRC() {
    $("#trow")[0].scrollLeft = $("#tbody")[0].scrollLeft;
    $("#tcol")[0].scrollTop = $("#tbody")[0].scrollTop;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//ダイアログ表示
//ダイアログ内のIDのプレフィックスにdef_は使わないこと。
function showDialog(contentsId, title, callback) {
    var srcDialogBox = $("#def_" + contentsId)[0];
    if (!srcDialogBox) {
        var srcDialogBoxHTML = $("#" + contentsId)[0].outerHTML;
        //ダイアログ内のid="××" → id="def_××"に変更。要はコピー作成
        srcDialogBoxNameHTML = srcDialogBoxHTML.replace(
            /(<[^>]*name=")([^"]+)("[^>]*>)/g,
            "$1def_$2$3"
        );
        $("#" + contentsId)[0].outerHTML = srcDialogBoxNameHTML.replace(
            /(<[^>]*id=")([^"]+)("[^>]*>)/g,
            "$1def_$2$3"
        );
    } else {
        var srcDialogBoxHTML = $("#def_" + contentsId)[0].outerHTML.replace(
            /(<[^>]*name=")def_([^"]+)("[^>]*>)/g,
            "$1$2$3"
        );
        var srcDialogBoxHTML = srcDialogBoxHTML.replace(
            /(<[^>]*id=")def_([^"]+)("[^>]*>)/g,
            "$1$2$3"
        );
    }

    //dialogBox.dialogBoxContentsの中身を書き換えている
    $("#dialogBoxContents")[0].innerHTML = srcDialogBoxHTML;
    $("#dialogBoxTitle")[0].innerHTML = title;
    $("#" + contentsId).show();
    $("#dialogBox").show();

    // ブラウザの横幅を取得(全体の幅からダイアログの幅を引いて半分。要は中央ぞろえ)
    var browserWidth = $(window).width();
    var boxW = $("#dialogBoxTable").width();
    var plusPxW = (browserWidth - boxW) / 2;
    var browserHeight = $(window).height();
    var boxH = $("#dialogBoxTable").height();
    var plusPxH = (browserHeight - boxH) / 2;
    if (plusPxH < 0) {
        plusPxH = 0;
    }

    //dialogBox見えないDivを全画面に展開（表示したダイアログ以外触れないようにする）
    $("#dialogBox").css({ left: 0 });
    $("#dialogBox").css({ top: 0 });
    $("#dialogBox").css({ width: $(document).width() + "px" });
    $("#dialogBox").css({ height: $(document).height() + "px" });

    //ダイアログ
    $("#dialogBoxTable").css({ position: "absolute" });
    $("#dialogBoxTable").css({ left: plusPxW + "px" });
    $("#dialogBoxTable").css({ top: plusPxH + "px" });

    //タイトルの部分をドラックして移動できるようにしている。
    $("#dialogTitleBar")
        .mousedown(function (e) {
            e.preventDefault();
            //dialogBoxTableの中にdataを使って変数を作成(最初の座標保持)
            $("#dialogBoxTable")
                .data(
                    "clickPointX",
                    e.pageX - $("#dialogTitleBar").offset().left
                )
                .data(
                    "clickPointY",
                    e.pageY - $("#dialogTitleBar").offset().top
                );
            //dialogBoxTableのmousedown時の座標と現在の座標を使用してダイアログの位置を動かす
            //激しく動かしてダイアログからマウスが外れても動くようにdocument.mousemoveにしてる
            $(document).mousemove(function (e) {
                e.preventDefault();
                $("#dialogBoxTable").css({
                    top:
                        e.pageY -
                        $("#dialogBoxTable").data("clickPointY") +
                        "px",
                    left:
                        e.pageX -
                        $("#dialogBoxTable").data("clickPointX") +
                        "px",
                });
            });
        })
        .mouseup(function (e) {
            e.preventDefault();
            $(document).unbind("mousemove");
        });
    if (callback) {
        callback();
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//操作履歴

function changeCheckHistory() {
    if (!$("#opeHistory").prop("checked")) {
        deleteAllOperationHistory();
    }
}

/**
 * 操作履歴の追加
 *
 * @param {*} chairList 講座リスト
 * @param {*} opeMode 操作モード("ADD":追加, "DEL":削除, "MOVE":移動, "STAFF":担当者変更, "FAC:施設変更", "TESTFAC:試験会場変更", "TEST:時間割種別")
 * @param {*} srcBox 移動元セル
 * @param {*} targetBox 移動先セル
 * @param {*} fromParam 変更前パラメータ(FAC:施設, )
 * @param {*} toParam 変更後パラメータ(FAC:施設, )
 */
function writeOperationHistory(
    opeMode,
    chairList,
    srcBox,
    targetBox,
    fromParam,
    toParam
) {
    // 操作履歴保存時のみ追加
    if ($("#opeHistory").prop("checked")) {
        var opeHist = $("input[name=operationHistory]").val();
        var historyList = [];
        if (opeHist) {
            historyList = $.parseJSON(opeHist);
        }

        var history = {};

        history["OPEMODE"] = opeMode;
        // 移動元セル
        if (srcBox) {
            var srcId = srcBox.getAttribute("id");
            var srcIdArray = srcId.split("_");
            history["FROM_ID"] = srcId;
            history["FROM_EXECUTEDATE"] = $(
                "input[name=UPDDATE" + srcIdArray[1] + "]"
            ).val();
            history["FROM_PERIODCD"] = srcIdArray[2];
            history["FROM_CHAIRCD"] = chairList;
            // 職員
            if (opeMode == "STAFF") {
                history["FROM_STAFF"] = fromParam;
            }
            // 施設
            if (opeMode == "FAC") {
                history["FROM_FAC"] = fromParam;
            }
            // 試験会場
            if (opeMode == "TESTFAC") {
                history["FROM_TESTFAC"] = fromParam;
            }
            // 時間割種別
            if (opeMode == "TEST") {
                history["FROM_TEST"] = fromParam;
            }
            // 集計フラグ/授業形態
            if (opeMode == "COUNTLESSON") {
                history["FROM_COUNTLESSON"] = fromParam;
            }
        }
        // 移動先セル
        if (targetBox) {
            var targetId = targetBox.getAttribute("id");
            var targetIdArray = targetId.split("_");
            history["TO_ID"] = targetId;
            history["TO_EXECUTEDATE"] = $(
                "input[name=UPDDATE" + targetIdArray[1] + "]"
            ).val();
            history["TO_PERIODCD"] = targetIdArray[2];
            history["TO_CHAIRCD"] = chairList;
            // 職員
            if (opeMode == "STAFF") {
                history["TO_STAFF"] = toParam;
            }
            // 施設変更
            if (opeMode == "FAC") {
                history["TO_FAC"] = toParam;
            }
            // 試験会場変更
            if (opeMode == "TESTFAC") {
                history["TO_TESTFAC"] = toParam;
            }
            // 時間割種別
            if (opeMode == "TEST") {
                history["TO_TEST"] = toParam;
            }
            // 集計フラグ/授業形態
            if (opeMode == "COUNTLESSON") {
                history["TO_COUNTLESSON"] = toParam;
            }
        }
        historyList.push(history);
        opeHist = JSON.stringify(historyList);
        $("input[name=operationHistory]").val(opeHist);
    }
}

/**
 * 操作履歴を全削除
 */
function deleteAllOperationHistory() {
    $("input[name=operationHistory]").val("");
}

/**
 * 最後に追加した操作履歴の削除
 */
function deleteLastOperationHistory() {
    var opeHist = $("input[name=operationHistory]").val();
    if (opeHist) {
        var historyList = $.parseJSON(opeHist);
        historyList = historyList.slice(0, historyList.length - 1);
        opeHist = JSON.stringify(historyList);
        $("input[name=operationHistory]").val(opeHist);
    }
}

// 変更通知履歴のダイアログ初期化
function operationHistoryBoxInitFunc() {
    var ajaxParam = {};

    ajaxParam["CANCEL_SHOW"] = $("input[name=opelogCancelShowFlg]").prop(
        "checked"
    )
        ? "1"
        : "0";

    // TODO : AJAXで通知履歴を取得する。()
    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            AJAX_PARAM: JSON.stringify(ajaxParam),
            cmd: "selectOperationHistory",
            YEAR_SEME: $("input[name=YEAR_SEME]").val(),
            START_DATE: $("input[name=START_DATE]").val(),
            END_DATE: $("input[name=END_DATE]").val(),
        },
        async: true,
    }).done(function (data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);

        var opeLogTable = $("#operationHistoryBoxTable");
        // 通知履歴行を全削除
        $(opeLogTable).empty();
        // 通知履歴行を追加
        for (let index = 0; index < paramList.length; index++) {
            const param = paramList[index];

            var opeLog = $("<tr>")
                .attr("name", "operationHistoryLine")
                .attr("bgcolor", "#FFFFFF");

            var td1 = $("<td>").attr("width", "20").attr("align", "center");
            $(td1).append(param["CHK"]);
            $(opeLog).append(td1);

            var td2 = $("<td>")
                .attr("width", "90")
                .attr("align", "center")
                .attr("name", "registDate");
            $(td2).append(param["REGIST_DATE"]);
            $(opeLog).append(td2);

            var td3 = $("<td>").attr("width", "90").attr("align", "center");
            $(td3).append(param["CANCEL_FLG"]);
            $(opeLog).append(td3);

            var td4 = $("<td>").attr("width", "150").attr("align", "center");
            $(td4).append(param["STAFFNAME"]);
            $(opeLog).append(td4);

            var td5 = $("<td>").attr("width", "*");
            $(td5).append(param["NOTICE_MESSAGE"]);
            $(opeLog).append(td5);

            $(opeLogTable).append(opeLog);
        }
    });
}

function opeLogChange(obj) {
    // 変更通知の内容を変更した場合、更新フラグをチェックする
    var parantTr = $(obj).parents("tr[name=operationHistoryLine]");
    var chk = $(parantTr).find("input[name=opelogChk]")[0];
    $(chk).prop("checked", true);
}

// 変更通知の更新
function updateOperationHistory() {
    if (!confirm("{rval MSG102}")) {
        return false;
    }

    var ajaxParam = [];
    // チェックのついている変更通知を取得し、更新内容を作成
    var opeHistoryLine = $("tr[name=operationHistoryLine]");
    for (let i = 0; i < opeHistoryLine.length; i++) {
        const line = opeHistoryLine[i];
        var chk = $(line).find("input[name=opelogChk]")[0];
        if ($(chk).prop("checked")) {
            var param = {};

            var registDate = $(line).find("td[name=registDate]")[0];
            param["REGISTDATE"] = $(registDate).text();
            param["SEQ"] = $(chk).val();
            var cancel = $(line).find("input[name=opelogCancel]")[0];
            param["CANCEL"] = $(cancel).prop("checked") ? "1" : "0";
            var message = $(line).find("input[name=opelogMessage]")[0];
            param["MESSAGE"] = $(message).val();

            ajaxParam.push(param);
        }
    }

    if (ajaxParam.length > 0) {
        $.ajax({
            url: "knjb3042index.php",
            type: "POST",
            data: {
                AJAX_PARAM: JSON.stringify(ajaxParam),
                cmd: "updateOperationHistory",
                YEAR_SEME: document.forms[0].YEAR_SEME.value,
            },
        }).done(function (data, textStatus, jqXHR) {
            if (data == "OK") {
                var msg = "{rval MSG201}";
                alert(msg);
            }
            // 更新データを再読み込み
            operationHistoryBoxInitFunc();
        });
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//日付、校時ダイアログ初期化
function hidukekouziInitFunc() {
    var kouzaName = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .text();
    $("#hidukekouzi_targetName")[0].innerHTML = "講座=" + kouzaName;

    var kouzaId = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .val();
    var idArray = document.forms[0].selectStartTD.value.split("_");
    var dateCd = idArray[1];
    var period = idArray[2];
    var line = idArray[3];

    if ($("#SCH_DIV1").is(":checked")) {
        var dataMax = $("input[name=DATECNT_MAX]").val();
        var newDataList = new Array();
        $("#hidukekouzi_hiduke")[0].innerHTML = "";
        for (var i = 0; i < dataMax; i++) {
            var week = weekWaAbbvArray[i];
            newDataList.push(week);
            var option = $("<option>")
                .val(i)
                .text(week)
                .prop("selected", i == dateCd);
            $("#hidukekouzi_hiduke").append(option);
        }
    } else {
        var weekList = ["日", "月", "火", "水", "木", "金", "土"];
        var dataMax = $("input[name=DATECNT_MAX]").val();
        var sdataTime = new Date(Date.parse($("input[name=START_DATE]").val()));
        var newDataList = new Array();
        $("#hidukekouzi_hiduke")[0].innerHTML = "";
        for (var i = 0; i < dataMax; i++) {
            var year = sdataTime.getFullYear();
            var month = sdataTime.getMonth() + 1;
            var day = sdataTime.getDate();
            var week = weekList[sdataTime.getDay()];
            var newDate = year + "/" + month + "/" + day + "(" + week + ")";
            newDataList.push(newDate);
            var option = $("<option>")
                .val(i)
                .text(newDate)
                .prop("selected", i == dateCd);
            $("#hidukekouzi_hiduke").append(option);
            sdataTime.setDate(sdataTime.getDate() + 1);
        }
    }

    $("#hidukekouzi_kouzi")[0].innerHTML = "";
    var elements = $("#tyousei3 th");
    var kouziList = new Array();
    for (var i = 0; i < elements.length; i++) {
        if (elements[i].id.indexOf("0_") === 0) {
            var option = $("<option>")
                .val(i + 1)
                .text(elements[i].innerText)
                .prop("selected", i + 1 == period);
            $("#hidukekouzi_kouzi").append(option);
            kouziList[i] = elements[i].innerText;
        }
    }

    var srcId = "KOMA_" + dateCd + "_" + period + "_" + line;
    var srcBox = $("#" + srcId)[0];
    var srcLinking = srcBox.getAttribute("data-linking");
    var srcDataVal = srcBox.getAttribute("data-val");
    var srcDataValList = srcDataVal == "" ? new Array() : srcDataVal.split(":");
    var cntNum = null;
    for (var i = 0; i < srcDataValList.length; i++) {
        if (srcDataValList[i] == kouzaId) {
            cntNum = i;
        }
    }

    var periodLen = kouziList.length;

    for (var i = 0; i < newDataList.length; i++) {
        //移動不可の日付校時を入れる
        var noList = new Array();
        for (var j = 1; j <= periodLen; j++) {
            if ("KOMA_" + i + "_" + j + "_" + line == srcId) {
                noList.push("[" + kouziList[j - 1] + "]");
            } else {
                var targetBox = $("#KOMA_" + i + "_" + j + "_" + line)[0];
                var MObj = new MoveBlockObj();
                //選択セルの講座が、移動先に移動可能か
                MObj.checkCells(srcBox, cntNum + "", targetBox);
                //1が移動可能、2が講座重複、3が範囲エラー
                if (MObj.error != 1) {
                    noList.push("[" + kouziList[j - 1] + "]");
                }
            }
        }
        if (noList.length != 0) {
            var trData = $(
                "<tr><td>" +
                    newDataList[i] +
                    '</td><td class="hidukekouzi_gun"></td><td class="hidukekouzi_kamoku"></td><td>' +
                    kouzaName +
                    "</td><td>" +
                    noList.join(",") +
                    "</td></tr>"
            );
            $("#hidukekouzi_noTargetList").append(trData);
        }
    }
    $("#hidukekouzi_move").prop("disabled", true);
    $("#hidukekouzi_copy").prop("disabled", true);

    $.getJSON(
        "knjb3042index.php?YEAR_SEME=" +
            document.forms[0].YEAR_SEME.value +
            "&AJAX_PARAM=" +
            JSON.stringify({ AJAX_CHAIRCD: kouzaId }) +
            "&cmd=getChairData",
        null,
        function (data) {
            var kamokuData =
                data["kamoku"]["SUBCLASSCD"] +
                ":" +
                data["kamoku"]["SUBCLASSNAME"];
            var gunData =
                data["gun"]["GROUPCD"] + ":" + data["gun"]["CHAIRNAME"];
            $("#hidukekouzi_targetName").text(
                "群=" +
                    gunData +
                    "," +
                    "科目=" +
                    kamokuData +
                    "," +
                    $("#hidukekouzi_targetName").text()
            );
            $(".hidukekouzi_kamoku").text(kamokuData);
            $(".hidukekouzi_gun").text(gunData);
        }
    );
}

//日付、校時コンボ変更時処理
function hidukekouzi_change() {
    var hiduke = $("#hidukekouzi_hiduke option:selected").text();
    var kouzi = "[" + $("#hidukekouzi_kouzi option:selected").text() + "]";

    var elements = $("#hidukekouzi_noTargetList tr");
    var flag = false;
    for (var i = 0; i < elements.length; i++) {
        if (
            elements[i].innerHTML.indexOf(hiduke) !== -1 &&
            elements[i].innerHTML.indexOf(kouzi) !== -1
        ) {
            flag = true;
            break;
        }
    }
    if (flag) {
        $("#hidukekouzi_move").prop("disabled", true);
    } else {
        $("#hidukekouzi_move").prop("disabled", false);
    }

    //paramArray = 移動前のセル、ソースBoxセル内の講座順序番、移動先のセル
    var paramArray = hidukekouzi_getParam();
    var srcBox = paramArray[0];
    var cntNum = paramArray[1];
    var targetBox = paramArray[2];
    var MObj = new MoveBlockObj();
    //MObj.isCopyを立てるとコピー機能に変わる
    MObj.isCopy = true;
    //選択セルが、移動先にコピー可能か
    MObj.checkCells(srcBox, cntNum, targetBox);
    if (MObj.error != 1) {
        $("#hidukekouzi_copy").prop("disabled", true);
    } else {
        $("#hidukekouzi_copy").prop("disabled", false);
    }
}

//ソースBox、ターゲットBox、ソースBoxセル内の講座順序番号取得
function hidukekouzi_getParam() {
    var hiduke = $("#hidukekouzi_hiduke option:selected").val();
    var kouzi = $("#hidukekouzi_kouzi option:selected").val();

    //親ダイアログ(cframe)の対象講座コンボ
    var kouzaId = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .val();
    var idArray = document.forms[0].selectStartTD.value.split("_");
    var dateCd = idArray[1];
    var period = idArray[2];
    var line = idArray[3];

    //選択したセル
    var srcId = "KOMA_" + dateCd + "_" + period + "_" + line;
    //$()で指定した場合は、配列が返る。ID指定なので一つしかないから[0]
    var srcBox = $("#" + srcId)[0];

    var srcDataVal = srcBox.getAttribute("data-val");
    //講座:講座:講座
    var srcDataValList = srcDataVal == "" ? new Array() : srcDataVal.split(":");
    var cntNum = null;
    for (var i = 0; i < srcDataValList.length; i++) {
        if (srcDataValList[i] == kouzaId) {
            cntNum = i;
        }
    }
    var targetBox = $("#KOMA_" + hiduke + "_" + kouzi + "_" + line)[0];
    return new Array(srcBox, cntNum + "", targetBox);
}

//日付、校時移動処理
function hidukekouzi_moveButton() {
    var paramArray = hidukekouzi_getParam();
    var srcBox = paramArray[0];
    var cntNum = paramArray[1];
    var targetBox = paramArray[2];

    var chairList = [];
    if (srcBox.getAttribute("data-val")) {
        if (cntNum == "" || cntNum == "all") {
            chairList = srcBox.getAttribute("data-val").split(":");
        } else {
            chairList.push(srcBox.getAttribute("data-val").split(":")[cntNum]);
        }
    }

    var MObj = new MoveBlockObj();
    MObj.execMoveBlock(srcBox, cntNum, targetBox, false);

    // 操作履歴追加
    writeOperationHistory("MOVE", chairList, srcBox, targetBox);

    //移動先を選択状態にする。
    f_clearSelectTDColor();
    //TDに色を付ける
    document.forms[0].selectStartTD.value = targetBox.id;
    targetBox.style.backgroundColor = "#F5F599";
    selectSubclassRemoveIds(targetBox.id);

    location.href = "#" + targetBox.id;

    $("#dialogBox").hide();

    //cframeを閉じる
    closeit();
    //移動先セルの状態でcframeを開き直す
    setClickValue("chairInfo");
}

//日付、校時コピー処理
function hidukekouzi_copyButton() {
    var paramArray = hidukekouzi_getParam();
    var srcBox = paramArray[0];
    var cntNum = paramArray[1];
    var targetBox = paramArray[2];

    var chairList = [];
    if (srcBox.getAttribute("data-val")) {
        if (cntNum == "" || cntNum == "all") {
            chairList = srcBox.getAttribute("data-val").split(":");
        } else {
            chairList.push(srcBox.getAttribute("data-val").split(":")[cntNum]);
        }
    }

    var MObj = new MoveBlockObj();
    MObj.execMoveBlockCopy(srcBox, cntNum, targetBox, false);

    // 操作履歴追加
    writeOperationHistory("ADD", chairList, "", targetBox);

    $("#dialogBox").hide();
}

//右の講座コンボからカレントの講座を抜く前処理
function selectSubclassRemoveIds(srcBoxId) {
    var idArray = srcBoxId.split("_");

    //設定講座一覧の選択講座を取得
    var selectChaircd = new Array();
    if (document.forms[0].GUNCD.value == "") {
        var selectList = document.forms[0].CATEGORY_SELECTED;
        for (var i = 0; i < selectList.length; i++) {
            if (selectList.options[i].selected == false) {
                continue;
            }
            selectChaircd.push(selectList.options[i].value);
        }
    }

    //1列の全ての講座が入る(重複は除外)
    var removeIds = new Array();
    for (i = 1; i <= document.forms[0].MAX_LINE.value; i++) {
        var id = idArray[0] + "_" + idArray[1] + "_" + idArray[2] + "_" + i;
        var boxDataVal = $("#" + id)[0].getAttribute("data-val");
        //講座:講座
        var boxDataValList =
            boxDataVal == "" ? new Array() : boxDataVal.split(":");
        for (var j = 0; j < boxDataValList.length; j++) {
            var flag = false;
            for (var k = 0; k < removeIds.length; k++) {
                if (removeIds[k] == boxDataValList[j]) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                removeIds.push(boxDataValList[j]);
            }
        }
    }
    selectSubclass("getChair", removeIds, selectChaircd);
}

//デバッグ用アクション
function testAction() {
    //showDialog('dumyBox','ダミー',function(){/*alert('A');*/});
    //showDialog('deleteSelectBox','削除',deleteSelectInitFunc);
}

//キーダウン処理。現在Deleteのみ対応
function documentKyeDown(event) {
    if (!$("#dialogBox").is(":hidden")) {
        return true;
    }

    if (
        event.keyCode == 46 &&
        document.forms[0].selectStartTD.value != "" &&
        document.forms[0].AUTHORITY.value == "4"
    ) {
        if (
            $(
                "#" +
                    document.forms[0].selectStartTD.value.replace(/TD/, "KOMA")
            )[0].getAttribute("data-val") != ""
        ) {
            makeDeleteDataList();
            showDialog("deleteSelectBox", "削除", deleteSelectInitFunc);
        }
    }
}

//削除ダイアログ初期化
function deleteSelectInitFunc() {
    dodragleave();
    var idArray = document.forms[0].selectStartTD.value.split("_");
    var cntNum = null;
    //複数件(2件とか)ある場合のクリック時のリストで選択した番号
    if (idArray.length > 4) {
        cntNum = idArray[4];
    }

    var weekList = ["日", "月", "火", "水", "木", "金", "土"];
    var dataMax = $("input[name=DATECNT_MAX]").val();
    var sdataTime = new Date(Date.parse($("input[name=START_DATE]").val()));
    sdataTime.setDate(sdataTime.getDate() + parseInt(idArray[1]));
    var year = sdataTime.getFullYear();
    var month = sdataTime.getMonth() + 1;
    var day = sdataTime.getDate();
    var titleDate = year + "/" + month + "/" + day;
    if ($("input[name=SCH_DIV]:checked").val() == "1") {
        var weeks = $("input[name=ALL_DATE]").val().split(",");
        var week = weeks[idArray[1]] - 1;
        titleDate = weekList[week];
    }

    var elements = $("#tyousei3 th");
    var kouziList = new Array();
    for (var i = 0; i < elements.length; i++) {
        if (i + 1 == idArray[2]) {
            var titleKouzi = elements[i].innerText;
            break;
        }
    }

    $("#deleteSelect_targetName")[0].innerText = titleDate + " " + titleKouzi;

    var srcBoxId = "KOMA_" + idArray[1] + "_" + idArray[2] + "_" + idArray[3];
    var srcBox = $("#" + srcBoxId)[0];
    var boxDataVal = srcBox.getAttribute("data-val");
    var boxDataValList = boxDataVal == "" ? new Array() : boxDataVal.split(":");
    var boxDataText = srcBox.getAttribute("data-text");
    var boxDataTextList =
        boxDataText == "" ? new Array() : boxDataText.split(",");
    var boxDataTest = srcBox.getAttribute("data-test");
    var boxDataTestList =
        boxDataTest == "" ? new Array() : boxDataTest.split(",");
    var boxDataTestKouzaList = {};
    var ajaxBoxDataValList = new Array();
    for (var i = 0; i < boxDataValList.length; i++) {
        var flag = false;
        if (cntNum != null && cntNum != "" && cntNum != "all") {
            if (i == cntNum) {
                flag = true;
            }
        } else {
            flag = true;
        }
        if (flag) {
            if (
                document.forms[0].KNJB3042_SchTestPattern.value == "1" &&
                !$("#SCH_DIV3").is(":checked") &&
                boxDataTestList[i] != "0"
            ) {
                boxDataTestKouzaList[boxDataValList[i]] = true;
                var trData = $(
                    "<tr><td>" +
                        boxDataTextList[i].replace(/<br>|<BR>/g, ":") +
                        '</td><td id="deleteSelect_gun_' +
                        boxDataValList[i] +
                        '"></td><td></td></tr>'
                );
            } else {
                var trData = $(
                    "<tr><td>" +
                        boxDataTextList[i].replace(/<br>|<BR>/g, ":") +
                        '</td><td id="deleteSelect_gun_' +
                        boxDataValList[i] +
                        '"></td><td><input type="checkbox" name="deleteSelectCheckBox" value="' +
                        boxDataValList[i] +
                        ":" +
                        srcBoxId +
                        '" onclick="delAllChecked(this, \'DEL_STF_' +
                        boxDataValList[i] +
                        '\')" checked="checked"></td></tr>'
                );
            }
            $("#deleteSelectTable").append(trData);
            ajaxBoxDataValList.push(boxDataValList[i]);
        }
    }
    var dispListId = {};
    var dispListText = {};
    var staffList = {};
    for (lineCnt = 1; lineCnt <= document.forms[0].MAX_LINE.value; lineCnt++) {
        var targetBoxId =
            "KOMA_" + idArray[1] + "_" + idArray[2] + "_" + lineCnt;
        var targetBox = $("#" + targetBoxId)[0];
        var targetBoxDataVal = targetBox.getAttribute("data-val");
        var targetBoxDataValList =
            targetBoxDataVal == "" ? new Array() : targetBoxDataVal.split(":");
        var targetBoxDataText = targetBox.getAttribute("data-text");
        var targetBoxDataTextList =
            targetBoxDataText == ""
                ? new Array()
                : targetBoxDataText.split(",");
        for (var i = 0; i < targetBoxDataValList.length; i++) {
            // var setStaff = $('#REC td')[lineCnt - 1].innerHTML.replace(/<br>|<BR>/g, ':');
            var setStaff = $($(".redips-mark")[lineCnt - 1])
                .html()
                .replace(/<br>|<BR>/g, ":");
            if (!staffList[targetBoxDataValList[i]]) {
                staffList[targetBoxDataValList[i]] = new Array();
            }
            staffList[targetBoxDataValList[i]].push(
                setStaff + ":" + targetBoxId
            );
            var flag = false;
            for (var j = 0; j < ajaxBoxDataValList.length; j++) {
                if (targetBoxDataValList[i] == ajaxBoxDataValList[j]) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                dispListId[targetBoxDataValList[i]] = targetBoxId;
                dispListText[targetBoxDataValList[i]] =
                    targetBoxDataTextList[i];
                ajaxBoxDataValList.push(targetBoxDataValList[i]);
            }
        }
    }

    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            AJAX_PARAM: JSON.stringify({
                AJAX_GUN_PARAM: JSON.stringify(ajaxBoxDataValList),
            }),
            cmd: "getGunCode",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        var gunCodeList = {};
        var paramList = $.parseJSON(data);
        for (var i = 0; i < paramList.length; i++) {
            var box = $("#deleteSelect_gun_" + paramList[i]["Kouza"])[0];
            if (box) {
                box.innerText =
                    paramList[i]["GunCode"] + ":" + paramList[i]["GunName"];
                gunCodeList[paramList[i]["GunCode"]] = true;
            } else {
                if (
                    paramList[i]["GunCode"] != "0000" &&
                    gunCodeList[paramList[i]["GunCode"]]
                ) {
                    if (boxDataTestKouzaList[paramList[i]["Kouza"]]) {
                        var trData = $(
                            "<tr><td>" +
                                dispListText[paramList[i]["Kouza"]].replace(
                                    /<br>|<BR>/g,
                                    ":"
                                ) +
                                '</td><td id="deleteSelect_gun_' +
                                paramList[i]["Kouza"] +
                                '">' +
                                paramList[i]["GunCode"] +
                                ":" +
                                paramList[i]["GunName"] +
                                "</td><td></td></tr>"
                        );
                    } else {
                        var trData = $(
                            "<tr><td>" +
                                dispListText[paramList[i]["Kouza"]].replace(
                                    /<br>|<BR>/g,
                                    ":"
                                ) +
                                '</td><td id="deleteSelect_gun_' +
                                paramList[i]["Kouza"] +
                                '">' +
                                paramList[i]["GunCode"] +
                                ":" +
                                paramList[i]["GunName"] +
                                '</td><td><input type="checkbox" name="deleteSelectCheckBox" value="' +
                                paramList[i]["Kouza"] +
                                ":" +
                                dispListId[paramList[i]["Kouza"]] +
                                '" onclick="delAllChecked(this, \'DEL_STF_' +
                                paramList[i]["Kouza"] +
                                '\')" checked="checked"></td></tr>'
                        );
                    }
                    $("#deleteSelectTable").append(trData);
                }
            }
        }
        var tags = $("#deleteSelectTable tr").sort(function (a, b) {
            var reg1 = a.innerHTML.match(
                /<t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.*)<\/t[dh]><t[dh][^>]*>(.*)<\/t[dh]>/
            );
            var kouzaParts1 = reg1[1].split(":");
            var gunParts1 = reg1[2].split(":");

            var reg2 = b.innerHTML.match(
                /<t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.*)<\/t[dh]><t[dh][^>]*>(.*)<\/t[dh]>/
            );
            var kouzaParts2 = reg2[1].split(":");
            var gunParts2 = reg2[2].split(":");

            if (reg1[2] == "群") {
                return -1;
            } else if (reg2[2] == "群") {
                return 1;
            } else {
                if (gunParts1[0] > gunParts2[0]) {
                    return 1;
                } else if (gunParts1[0] == gunParts2[0]) {
                    if (kouzaParts1[0] > kouzaParts2[0]) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    return -1;
                }
            }
        });

        tdData = new Array();
        tags.each(function () {
            tdData.push(this.outerHTML);
        });
        $("#deleteSelectTable tr").remove();
        for (var i = 0; i < tdData.length; i++) {
            $("#deleteSelectTable").append(tdData[i]);
            if (document.forms[0].LEFT_MENU.value == "1") {
                var result = tdData[i].match(/deleteSelect_gun_(\d+)/);
                if (result) {
                    for (
                        var stfCnt = 0;
                        stfCnt < staffList[result[1]].length;
                        stfCnt++
                    ) {
                        var stfInfo = staffList[result[1]][stfCnt].split(":");
                        if (boxDataTestKouzaList[result[1]]) {
                            var trData = $(
                                '<tr><td colspan="2">　　　　┗　' +
                                    stfInfo[0] +
                                    ":" +
                                    stfInfo[1] +
                                    "</td><td></td></tr>"
                            );
                        } else {
                            var trData = $(
                                '<tr><td colspan="2">　　　　┗　' +
                                    stfInfo[0] +
                                    ":" +
                                    stfInfo[1] +
                                    '</td><td><input type="checkbox" class="DEL_STF_' +
                                    result[1] +
                                    '" name="deleteSelectStfCheckBox" value="' +
                                    result[1] +
                                    ":" +
                                    stfInfo[2] +
                                    '" checked="checked"></td></tr>'
                            );
                        }
                        $("#deleteSelectTable").append(trData);
                    }
                }
            }
        }
    });
}

function delAllChecked(obj, targetClassName) {
    $("." + targetClassName).prop("checked", obj.checked);
}

//削除ダイアログ削除処理
function deleteSelect_deleteButton() {
    //出欠の確認
    var flag = false;
    var noCheckedObj = {};
    var attendZumiObj = {};
    if (document.forms[0].LEFT_MENU.value == "1") {
        var befKouza = "";
        var checked = $("input[name=deleteSelectStfCheckBox]").each(
            function () {
                var checkedVal = $(this).val();
                var checkedValParts = checkedVal.split(":");
                if (checkedValParts.length < 2) {
                    alert("error");
                    return;
                }
                if (!noCheckedObj[checkedValParts[0]]) {
                    noCheckedObj[checkedValParts[0]] = false;
                }
                if (!$(this).prop("checked")) {
                    noCheckedObj[checkedValParts[0]] = true;
                }

                if (!attendZumiObj[checkedValParts[0]]) {
                    attendZumiObj[checkedValParts[0]] = false;
                }
                var box = $("#" + checkedValParts[1])[0];
                var boxDataVal = box.getAttribute("data-val");
                var boxDataValList =
                    boxDataVal == "" ? new Array() : boxDataVal.split(":");
                var boxDataExec = box.getAttribute("data-exec");
                var boxDataExecList =
                    boxDataExec == "" ? new Array() : boxDataExec.split(",");
                for (var i = 0; i < boxDataValList.length; i++) {
                    if ($("input[name=SCH_DIV]:checked").val() != "1") {
                        if (
                            boxDataValList[i] == checkedValParts[0] &&
                            boxDataExecList[i] != "MI_SYUKKETSU"
                        ) {
                            attendZumiObj[checkedValParts[0]] = true;
                        }
                    }
                }
            }
        );
        for (var attendKey in attendZumiObj) {
            if (attendZumiObj[attendKey] && !noCheckedObj[attendKey]) {
                flag = true;
            }
        }
    } else {
        var checked = $("input[name=deleteSelectCheckBox]:checked").each(
            function () {
                var checkedVal = $(this).val();
                var checkedValParts = checkedVal.split(":");
                if (checkedValParts.length < 2) {
                    alert("error");
                    return;
                }

                var box = $("#" + checkedValParts[1])[0];
                var boxDataVal = box.getAttribute("data-val");
                var boxDataValList =
                    boxDataVal == "" ? new Array() : boxDataVal.split(":");
                var boxDataExec = box.getAttribute("data-exec");
                var boxDataExecList =
                    boxDataExec == "" ? new Array() : boxDataExec.split(",");
                for (var i = 0; i < boxDataValList.length; i++) {
                    if (
                        boxDataValList[i] == checkedValParts[0] &&
                        boxDataExecList[i] != "MI_SYUKKETSU"
                    ) {
                        flag = true;
                    }
                }
            }
        );
    }
    if (flag && !$("#SCH_DIV1").is(":checked")) {
        if (
            !confirm(
                "削除対象に出欠済が含まれています。\n　出欠情報も削除されますが、宜しいでしょうか？"
            )
        ) {
            return;
        }
    }

    var calcList = new Array();
    var LObj = new LinkingCellObj();
    //削除処理
    var checked = $("input[name=deleteSelectCheckBox]:checked").each(
        function () {
            var checkedVal = $(this).val();
            var checkedValParts = checkedVal.split(":");
            if (checkedValParts.length < 2) {
                alert("error");
                return;
            }

            if (document.forms[0].LEFT_MENU.value == "1") {
                var boxArray = new Array();
                $(".DEL_STF_" + checkedValParts[0] + ":checked").each(function (
                    index,
                    element
                ) {
                    var elmVal = element.value.split(":");
                    var box = $("#" + elmVal[1])[0];
                    var boxDataVal = box.getAttribute("data-val");
                    var boxDataValList =
                        boxDataVal == "" ? new Array() : boxDataVal.split(":");
                    for (var i = 0; i < boxDataValList.length; i++) {
                        if (boxDataValList[i] == checkedValParts[0]) {
                            var MObj = new MoveBlockObj();
                            var kouziList = LObj.makeKouziLsit(box);
                            MObj.deleteOneObj(box, i + "");
                            calcList = LObj.makeCalcList(
                                box,
                                calcList,
                                kouziList,
                                document.forms[0].MAX_LINE.value
                            );
                        }
                    }
                    boxArray.push(box);
                });
                //globalDelKeyListにセットされた講座が削除対象となる
                //講座全ての教員が削除対象でなければ、削除対象外とする。
                globalDelKeyListDef = $.extend([], globalDelKeyList);
                for (var boxCnt = 0; boxCnt < boxArray.length; boxCnt++) {
                    var idArray = boxArray[boxCnt].id.split("_");
                    for (
                        lineCnt = 1;
                        lineCnt <= document.forms[0].MAX_LINE.value;
                        lineCnt++
                    ) {
                        var targetBoxId =
                            "KOMA_" +
                            idArray[1] +
                            "_" +
                            idArray[2] +
                            "_" +
                            lineCnt;
                        var targetBox = $("#" + targetBoxId)[0];
                        var targetBoxDataVal = targetBox.getAttribute(
                            "data-val"
                        );
                        var targetBoxDataValList =
                            targetBoxDataVal == ""
                                ? new Array()
                                : targetBoxDataVal.split(":");
                        for (
                            targetCnt = 0;
                            targetCnt < targetBoxDataValList.length;
                            targetCnt++
                        ) {
                            for (var key in globalDelKeyList) {
                                if (
                                    key.indexOf(
                                        targetBoxDataValList[targetCnt]
                                    ) !== -1
                                ) {
                                    delete globalDelKeyList[key];
                                }
                            }
                        }
                    }
                }
            } else {
                var box = $("#" + checkedValParts[1])[0];
                var boxDataVal = box.getAttribute("data-val");
                var boxDataValList =
                    boxDataVal == "" ? new Array() : boxDataVal.split(":");
                for (var i = 0; i < boxDataValList.length; i++) {
                    if (boxDataValList[i] == checkedValParts[0]) {
                        var MObj = new MoveBlockObj();
                        var kouziList = LObj.makeKouziLsit(box);
                        MObj.deleteMoveObj(box, i + "");
                        calcList = LObj.makeCalcList(
                            box,
                            calcList,
                            kouziList,
                            document.forms[0].MAX_LINE.value
                        );
                    }
                }
            }
            // 操作履歴追加
            var targetChairList = [];
            targetChairList.push(checkedValParts[0]);
            writeOperationHistory(
                "DEL",
                targetChairList,
                $("#" + checkedValParts[1])[0],
                ""
            );
        }
    );
    $("#dialogBox").hide();
    LObj.checkMeiboAndFacUseCalcList(
        calcList,
        document.forms[0].MAX_LINE.value
    );
    selectSubclass("getChair", null);
}

//削除ダイアログを出すかどうか
function isDeleteDialogShow(srcBox, cntNum, callback, falseCallback) {
    if (document.forms[0].LEFT_MENU.value == "1") {
        callback();
        return;
    }
    var idArray = srcBox.id.split("_");
    var boxDataVal = srcBox.getAttribute("data-val");
    var boxDataValList = boxDataVal == "" ? new Array() : boxDataVal.split(":");
    var ajaxBoxDataValList = new Array();
    var srcBoxDataValList = new Array();
    for (var i = 0; i < boxDataValList.length; i++) {
        if (cntNum != null && cntNum != "" && cntNum != "all") {
            if (i == cntNum) {
                ajaxBoxDataValList.push(boxDataValList[i]);
                srcBoxDataValList.push(boxDataValList[i]);
            }
        } else {
            ajaxBoxDataValList.push(boxDataValList[i]);
            srcBoxDataValList.push(boxDataValList[i]);
        }
    }
    if (ajaxBoxDataValList.length > 1) {
        callback();
        return;
    }
    for (lineCnt = 1; lineCnt <= document.forms[0].MAX_LINE.value; lineCnt++) {
        var targetBoxId =
            "KOMA_" + idArray[1] + "_" + idArray[2] + "_" + lineCnt;
        var targetBox = $("#" + targetBoxId)[0];
        var targetBoxDataVal = targetBox.getAttribute("data-val");
        var targetBoxDataValList =
            targetBoxDataVal == "" ? new Array() : targetBoxDataVal.split(":");
        for (var i = 0; i < targetBoxDataValList.length; i++) {
            var flag = false;
            for (var j = 0; j < ajaxBoxDataValList.length; j++) {
                if (targetBoxDataValList[i] == ajaxBoxDataValList[j]) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                ajaxBoxDataValList.push(targetBoxDataValList[i]);
            }
        }
    }

    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            AJAX_PARAM: JSON.stringify({
                AJAX_GUN_PARAM: JSON.stringify(ajaxBoxDataValList),
            }),
            cmd: "getGunCode",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        var gunCodeList = {};
        var paramList = $.parseJSON(data);
        for (var i = 0; i < paramList.length; i++) {
            if (
                gunCodeList[paramList[i]["GunCode"]] == null &&
                srcBoxDataValList[0] == paramList[i]["Kouza"]
            ) {
                gunCodeList[paramList[i]["GunCode"]] = true;
            } else {
                if (
                    paramList[i]["GunCode"] != "0000" &&
                    gunCodeList[paramList[i]["GunCode"]]
                ) {
                    callback();
                    return;
                }
            }
        }
        falseCallback();
    });
}

//日付校時で移動コピーダイアログ初期化
function copyMoveBoxInitFunc() {
    var id = document.forms[0].selectStartTD.value;
    if (id == null || id == "") {
        var idArray = new Array("0", "1", "1");
    } else {
        var idArray = document.forms[0].selectStartTD.value.split("_");
    }
    var dateCd = idArray[1];
    var period = idArray[2];
    var line = idArray[3];

    if ($("#SCH_DIV1").is(":checked")) {
        var dataMax = $("input[name=DATECNT_MAX]").val();
        var newDataList = new Array();
        $("#copyMoveBox_fromDay")[0].innerHTML = "";
        $("#copyMoveBox_toDay")[0].innerHTML = "";
        for (var i = 0; i < dataMax; i++) {
            var week = weekWaAbbvArray[i];
            var newDate = "(" + week + ")";
            newDataList.push(newDate);

            var $input = $(
                '<input type="radio" onclick="copyMoveBox_change()" id="copyMoveBox_fromDayRadio' +
                    i +
                    '" name="copyMoveBox_fromDayRadio" />'
            ).val(i);
            $("#copyMoveBox_fromDay").append($input);
            $("#copyMoveBox_fromDay").append(
                '<label for="copyMoveBox_fromDayRadio' +
                    i +
                    '">' +
                    newDate +
                    "</label><br>"
            );
            var $input = $(
                '<input type="radio"  onclick="copyMoveBox_change()" id="copyMoveBox_toDayRadio' +
                    i +
                    '" name="copyMoveBox_toDayRadio" />'
            ).val(i);
            $("#copyMoveBox_toDay").append($input);
            $("#copyMoveBox_toDay").append(
                '<label for="copyMoveBox_toDayRadio' +
                    i +
                    '">' +
                    newDate +
                    "</label><br>"
            );
        }
    } else {
        var weekList = ["日", "月", "火", "水", "木", "金", "土"];
        var dataMax = $("input[name=DATECNT_MAX]").val();
        var sdataTime = new Date(Date.parse($("input[name=START_DATE]").val()));
        var newDataList = new Array();
        $("#copyMoveBox_fromDay")[0].innerHTML = "";
        $("#copyMoveBox_toDay")[0].innerHTML = "";
        for (var i = 0; i < dataMax; i++) {
            var year = sdataTime.getFullYear();
            var month = sdataTime.getMonth() + 1;
            var day = sdataTime.getDate();
            var week = weekList[sdataTime.getDay()];
            var newDate = day + "(" + week + ")";
            newDataList.push(newDate);

            var $input = $(
                '<input type="radio" onclick="copyMoveBox_change()" id="copyMoveBox_fromDayRadio' +
                    i +
                    '" name="copyMoveBox_fromDayRadio" />'
            ).val(i);
            $("#copyMoveBox_fromDay").append($input);
            $("#copyMoveBox_fromDay").append(
                '<label for="copyMoveBox_fromDayRadio' +
                    i +
                    '">' +
                    newDate +
                    "</label><br>"
            );
            var $input = $(
                '<input type="radio"  onclick="copyMoveBox_change()" id="copyMoveBox_toDayRadio' +
                    i +
                    '" name="copyMoveBox_toDayRadio" />'
            ).val(i);
            $("#copyMoveBox_toDay").append($input);
            $("#copyMoveBox_toDay").append(
                '<label for="copyMoveBox_toDayRadio' +
                    i +
                    '">' +
                    newDate +
                    "</label><br>"
            );
            sdataTime.setDate(sdataTime.getDate() + 1);
        }
    }

    $("#copyMoveBox_fromKouzi")[0].innerHTML = "";
    $("#copyMoveBox_toKouzi")[0].innerHTML = "";
    $("#copyMoveBox_fromKouzi").append(
        $(
            '<select id="copyMoveBox_fromKouziSelect" name="copyMoveBox_fromKouziSelect" onchange="copyMoveBox_change()" multiple="multiple"></select>'
        )
    );
    $("#copyMoveBox_toKouzi").append(
        $(
            '<select id="copyMoveBox_toKouziSelect" name="copyMoveBox_toKouziSelect" onchange="copyMoveBox_change()"></select>'
        )
    );
    var elements = $("#tyousei3 th");
    var kouziList = new Array();
    var len = 0;
    for (var i = 0; i < elements.length; i++) {
        if (elements[i].id.indexOf("0_") === 0) {
            var option = $("<option>")
                .val(i + 1)
                .text(elements[i].innerText)
                .prop("selected", i + 1 == period);
            $("#copyMoveBox_fromKouziSelect").append(option);
            var option = $("<option>")
                .val(i + 1)
                .text(elements[i].innerText)
                .prop("selected", i + 1 == period);
            $("#copyMoveBox_toKouziSelect").append(option);
            kouziList[i] = elements[i].innerText;
            len++;
        }
    }
    $("#copyMoveBox_fromKouziSelect").prop("size", len);
    $("#copyMoveBox_toKouziSelect").prop("size", len);
    $("[name=copyMoveBox_fromDayRadio]").val([0]);
    $("[name=copyMoveBox_toDayRadio]").val([0]);
    copyMoveBox_change();

    //リストTOリストの中身を用意
    // var dispList = $('#REC tr td');
    var dispList = $(".redips-mark");
    dispList.each(function (index, element) {
        if ($(element).is(":hidden")) {
            return true;
        }
        var elemetArray = element.innerHTML.split("<br>");
        //ソートを考慮して'data-keyname'を先頭に設定
        var elemetKey =
            element.getAttribute("data-keyname") +
            ":" +
            element.getAttribute("data-linecnt");

        var option = $("<option>")
            .val(elemetKey)
            .text(elemetArray[0] + "：" + elemetArray[1]);
        $("#category_copymovebox_name").append(option);
    });

    if (
        document.forms[0].KNJB3042_SchTestPattern.value == "1" &&
        !$("#SCH_DIV3").is(":checked")
    ) {
        $("#copyMoveBox_moveType_label").hide();
    }
}

//日付校時で移動コピーの移動元コンボの範囲選択の開始と最後の校時を返す
function copyMoveBox_fromKouziSelect_check() {
    var Obj = $("#copyMoveBox_fromKouziSelect")[0];
    var flag = false;
    var idx = 0;
    var startIdx = 0;
    var endIdx = 0;
    for (var i = 0; i < Obj.options.length; i++) {
        if (Obj.options[i].selected) {
            if (!flag) {
                flag = true;
                idx = i;
                startIdx = i;
                endIdx = i;
            } else if (flag) {
                if (idx + 1 != i) {
                    //飛び石
                    return false;
                } else {
                    idx = i;
                    endIdx = i;
                }
            }
        }
    }
    if (flag) {
        return new Array(
            Obj.options[startIdx].value,
            Obj.options[endIdx].value
        );
    } else {
        return false;
    }
}

//日付校時で移動コピー、日付校時変更時処理
function copyMoveBox_change() {
    var srcDateCd = $("[name=copyMoveBox_fromDayRadio]:checked").val();
    var targetDateCd = $("[name=copyMoveBox_toDayRadio]:checked").val();
    var srcPeriod = $("#copyMoveBox_fromKouziSelect").val();
    var targetPeriod = $("#copyMoveBox_toKouziSelect").val();

    var movedisabled = false;
    var copydisabled = false;
    var irekaedisabled = false;
    var deletedisabled = false;
    var errorMsg = "";
    var ret = copyMoveBox_fromKouziSelect_check();
    if (!srcDateCd) {
        movedisabled = true;
        copydisabled = true;
        irekaedisabled = true;
        deletedisabled = true;
    }
    if (!targetDateCd) {
        movedisabled = true;
        copydisabled = true;
        irekaedisabled = true;
    }
    if (srcDateCd == targetDateCd && srcPeriod[0] == targetPeriod) {
        movedisabled = true;
        copydisabled = true;
        irekaedisabled = true;
        errorMsg = "当日です。";
    }
    if (ret === false) {
        movedisabled = true;
        copydisabled = true;
        irekaedisabled = true;
        deletedisabled = true;
        if (errorMsg == "") {
            errorMsg = "校時を飛び石選択または選択されていません。";
        }
    }
    if (srcDateCd && targetDateCd) {
        var IsTestNonMove =
            document.forms[0].KNJB3042_SchTestPattern.value == "1" &&
            !$("#SCH_DIV3").is(":checked");
        if (!movedisabled) {
            var Obj = new BlockMoveBlockObj();
            movedisabled = Obj.execMoveBlockFullMoveCheck(
                srcDateCd,
                targetDateCd,
                ret[0],
                ret[1],
                targetPeriod,
                IsTestNonMove
            );
        }

        if (!copydisabled) {
            var Obj = new BlockMoveBlockObj();
            copydisabled = Obj.execMoveBlockFullCopyCheck(
                srcDateCd,
                targetDateCd,
                ret[0],
                ret[1],
                targetPeriod,
                IsTestNonMove
            );
        }

        if (!irekaedisabled) {
            var Obj = new BlockMoveBlockObj();
            var retIrekaeData = Obj.execMoveBlockFullIrekaeCheck(
                srcDateCd,
                targetDateCd,
                ret[0],
                ret[1],
                targetPeriod,
                IsTestNonMove
            );
            irekaedisabled = retIrekaeData[1];
        }

        if (movedisabled || copydisabled || irekaedisabled) {
            if (errorMsg == "") {
                errorMsg = "範囲エラーまたは重複エラーです。";
            }
        }
    }

    if ($("#SCH_DIV1").is(":checked")) {
        $("#copyMoveBox_moveButton").prop("disabled", movedisabled);
        $("#copyMoveBox_irekaeButton").prop("disabled", irekaedisabled);
    } else {
        if (!movedisabled) {
            var LObj = new LinkingCellObj();
            LObj.meiboDupliCheck(
                srcDateCd,
                ret[0],
                ret[1],
                targetDateCd,
                targetPeriod,
                document.forms[0].MAX_LINE.value,
                IsTestNonMove,
                function () {
                    $("#copyMoveBox_moveButton").prop("disabled", true);
                    if (errorMsg == "") {
                        errorMsg = "出席情報があり、名簿が重複しています。";
                        $("#copyMoveBox_errorMsg")[0].innerHTML = errorMsg;
                    }
                },
                function () {
                    $("#copyMoveBox_moveButton").prop("disabled", false);
                    $("#copyMoveBox_errorMsg")[0].innerHTML = errorMsg;
                }
            );
        } else {
            $("#copyMoveBox_moveButton").prop("disabled", movedisabled);
            $("#copyMoveBox_errorMsg")[0].innerHTML = errorMsg;
        }

        if (!irekaedisabled) {
            var bmbObj = new BlockMoveBlockObj();
            bmbObj.meiboDupliCheckIrekae(
                retIrekaeData[0],
                document.forms[0].MAX_LINE.value,
                IsTestNonMove,
                function () {
                    $("#copyMoveBox_irekaeButton").prop("disabled", true);
                    if (errorMsg == "") {
                        errorMsg = "出席情報があり、名簿が重複しています。";
                        $("#copyMoveBox_errorMsg")[0].innerHTML = errorMsg;
                    }
                },
                function () {
                    $("#copyMoveBox_irekaeButton").prop("disabled", false);
                    $("#copyMoveBox_errorMsg")[0].innerHTML = errorMsg;
                }
            );
        } else {
            $("#copyMoveBox_irekaeButton").prop("disabled", irekaedisabled);
            $("#copyMoveBox_errorMsg")[0].innerHTML = errorMsg;
        }
    }
    $("#copyMoveBox_copyButton").prop("disabled", copydisabled);
    $("#copyMoveBox_deleteButton").prop("disabled", deletedisabled);
}

//日付校時で移動コピー、移動処理
function copyMoveBox_move() {
    copyMoveBox_exec(false);
}

//日付校時で移動コピー、コピー処理
function copyMoveBox_copy() {
    copyMoveBox_exec(true);
}

//日付校時で移動コピー、移動コピー処理
function copyMoveBox_exec(isCopy) {
    var srcDateCd = $("[name=copyMoveBox_fromDayRadio]:checked").val();
    var targetDateCd = $("[name=copyMoveBox_toDayRadio]:checked").val();
    var srcPeriod = $("#copyMoveBox_fromKouziSelect").val();
    var targetPeriod = $("#copyMoveBox_toKouziSelect").val();
    var isTest = $("#copyMoveBox_moveType").is(":checked");
    var IsTestNonMove =
        document.forms[0].KNJB3042_SchTestPattern.value == "1" &&
        !$("#SCH_DIV3").is(":checked");

    var testCd = "";
    if (isTest) {
        testCd = $("#MOVECOPY_TESTCD").val();
    }

    var ret = copyMoveBox_fromKouziSelect_check();
    if (ret !== false) {
        if (srcDateCd && targetDateCd) {
            var Obj = new BlockMoveBlockObj();
            if (isCopy) {
                // 操作履歴追加
                writeOperationHistoryBlockFull(
                    "ADD",
                    srcDateCd,
                    targetDateCd,
                    ret[0],
                    ret[1],
                    targetPeriod
                );

                Obj.execMoveBlockFullCopy(
                    srcDateCd,
                    targetDateCd,
                    ret[0],
                    ret[1],
                    targetPeriod,
                    testCd,
                    IsTestNonMove
                );
            } else {
                // 操作履歴追加
                writeOperationHistoryBlockFull(
                    "MOVE",
                    srcDateCd,
                    targetDateCd,
                    ret[0],
                    ret[1],
                    targetPeriod
                );
                Obj.execMoveBlockFullMove(
                    srcDateCd,
                    targetDateCd,
                    ret[0],
                    ret[1],
                    targetPeriod,
                    testCd,
                    IsTestNonMove
                );
            }
            //変更後にボタンの活性/非活性を設定
            copyMoveBox_change();
        }
    }
}

function writeOperationHistoryBlockFull(
    opeMode,
    srcDateCd,
    targetDateCd,
    srcStartNum,
    srcEndNum,
    targetStarNum
) {
    var lineList = [];
    if ($("#copyMoveBox_showListToList").is(":checked")) {
        var selectedList = $("#category_copymovebox_selected option");
        selectedList.each(function (index, element) {
            var selectedVal = $(element).val().split(":");
            lineList[index] = selectedVal[1];
        });
    } else {
        for (i = 1; i <= document.forms[0].MAX_LINE.value; i++) {
            lineList.push(i);
        }
    }

    for (var i = srcStartNum; i <= srcEndNum; i++) {
        var opeLog = {};
        for (let lineCnt = 0; lineCnt < lineList.length; lineCnt++) {
            const line = lineList[lineCnt];

            var srcBoxId = "KOMA_" + srcDateCd + "_" + i + "_" + line;
            var targetBoxId = "";
            if (targetDateCd) {
                targetBoxId =
                    "KOMA_" +
                    targetDateCd +
                    "_" +
                    (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                    "_" +
                    line;
            }

            var srcBox = $("#" + srcBoxId)[0];
            var targetBox = $("#" + targetBoxId)[0];
            var chairList = [];
            if (srcBox.getAttribute("data-val")) {
                chairList = srcBox.getAttribute("data-val").split(":");
            }
            for (let index = 0; index < chairList.length; index++) {
                const chairCd = chairList[index];
                if (opeLog[srcDateCd + "_" + i + chairCd]) {
                    continue;
                }
                opeLog[srcDateCd + "_" + i + chairCd] = "1";

                // 操作履歴追加
                if (opeMode == "ADD") {
                    writeOperationHistory("ADD", [chairCd], "", targetBox);
                } else if (opeMode == "MOVE") {
                    writeOperationHistory("MOVE", [chairCd], srcBox, targetBox);
                } else if (opeMode == "DEL") {
                    writeOperationHistory("DEL", [chairCd], srcBox, "");
                } else if (opeMode == "SWAP") {
                    writeOperationHistory("MOVE", [chairCd], srcBox, targetBox);
                }
            }

            if (opeMode == "SWAP") {
                var targetChairList = [];
                if (targetBox.getAttribute("data-val")) {
                    targetChairList = targetBox
                        .getAttribute("data-val")
                        .split(":");
                }
                for (
                    let targetIndex = 0;
                    targetIndex < targetChairList.length;
                    targetIndex++
                ) {
                    const targetChairCd = targetChairList[targetIndex];
                    if (
                        opeLog[
                            targetDateCd +
                                "_" +
                                (parseInt(targetStarNum) +
                                    (i - parseInt(srcStartNum))) +
                                "_" +
                                targetChairCd
                        ]
                    ) {
                        continue;
                    }
                    opeLog[
                        targetDateCd +
                            "_" +
                            (parseInt(targetStarNum) +
                                (i - parseInt(srcStartNum))) +
                            "_" +
                            targetChairCd
                    ] = "1";
                    writeOperationHistory(
                        "MOVE",
                        [targetChairCd],
                        targetBox,
                        srcBox
                    );
                }
            }
        }
    }
}

//日付校時で移動コピー、入替処理
function copyMoveBox_irekae() {
    var srcDateCd = $("[name=copyMoveBox_fromDayRadio]:checked").val();
    var targetDateCd = $("[name=copyMoveBox_toDayRadio]:checked").val();
    var srcPeriod = $("#copyMoveBox_fromKouziSelect").val();
    var targetPeriod = $("#copyMoveBox_toKouziSelect").val();
    var IsTestNonMove =
        document.forms[0].KNJB3042_SchTestPattern.value == "1" &&
        !$("#SCH_DIV3").is(":checked");

    var ret = copyMoveBox_fromKouziSelect_check();
    if (ret !== false) {
        if (srcDateCd && targetDateCd) {
            // 操作履歴追加
            writeOperationHistoryBlockFull(
                "SWAP",
                srcDateCd,
                targetDateCd,
                ret[0],
                ret[1],
                targetPeriod
            );
            var Obj = new BlockMoveBlockObj();
            Obj.execMoveBlockFullIrekae(
                srcDateCd,
                targetDateCd,
                ret[0],
                ret[1],
                targetPeriod,
                IsTestNonMove
            );
        }
    }
}

//日付校時で移動コピー、削除処理
function copyMoveBox_delete() {
    var srcDateCd = $("[name=copyMoveBox_fromDayRadio]:checked").val();
    var srcPeriod = $("#copyMoveBox_fromKouziSelect").val();
    var IsTestNonMove =
        document.forms[0].KNJB3042_SchTestPattern.value == "1" &&
        !$("#SCH_DIV3").is(":checked");
    var ret = copyMoveBox_fromKouziSelect_check();
    if (ret !== false) {
        if (srcDateCd) {
            //MoveBlockObjの削除処理のイベントをセット
            makeDeleteDataList("");
            // 操作履歴追加
            writeOperationHistoryBlockFull(
                "DEL",
                srcDateCd,
                "",
                ret[0],
                ret[1],
                ""
            );

            var Obj = new BlockMoveBlockObj();
            Obj.execMoveBlockFullDelete(
                srcDateCd,
                ret[0],
                ret[1],
                IsTestNonMove
            );
        }
    }
}

//施設初期化
function facilityInitFunc() {
    //0:KOMA 1:日付番号(開始日付の番号は0、1、2・・・) 2:校時 3:行番号
    var idArray = document.forms[0].selectStartTD.value.split("_");
    var cntNum = null;

    var kouzaName = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .text();
    var kouzaId = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .val();

    //日付/校時の列内にある全講座
    var ajaxBoxDataValList = new Array();

    kouzaNameParts = kouzaName.split(":");
    var setKouzaName1 = kouzaNameParts[1].replace("\r\n", "").replace("\n", "");
    var trData = $(
        '<tr data-facilityKouza="' +
            kouzaId +
            '"><td id="facility_targetList_gun_' +
            kouzaId +
            '"></td><td id="facility_targetList_kamoku_' +
            kouzaId +
            '"></td><td>' +
            kouzaId +
            ":" +
            setKouzaName1 +
            "</td></tr>"
    );
    $("#facility_targetList").append(trData);
    ajaxBoxDataValList.push(kouzaId);

    //日付、校時の全講座をajaxBoxDataValListに追加
    var dispListId = {};
    var dispListText = {};
    var updFac = {};
    for (lineCnt = 1; lineCnt <= document.forms[0].MAX_LINE.value; lineCnt++) {
        var targetBoxId =
            "KOMA_" + idArray[1] + "_" + idArray[2] + "_" + lineCnt;
        var targetBox = $("#" + targetBoxId)[0];
        var targetBoxDataVal = targetBox.getAttribute("data-val");
        var targetBoxDataValList =
            targetBoxDataVal == "" ? new Array() : targetBoxDataVal.split(":");
        var targetBoxDataText = targetBox.getAttribute("data-text");
        var targetBoxDataTextList =
            targetBoxDataText == ""
                ? new Array()
                : targetBoxDataText.split(",");
        var targetBoxDataFac = targetBox.getAttribute("data-selectfacility");
        var targetBoxDataFacList =
            targetBoxDataFac == "" ? new Array() : targetBoxDataFac.split(",");
        //一コマの講座ループ
        //TODO:高速化予定(全ては必要ない、群でなければその講座のみ、群であれば同一群のみ)
        for (var i = 0; i < targetBoxDataValList.length; i++) {
            var flag = false;
            for (var j = 0; j < ajaxBoxDataValList.length; j++) {
                if (targetBoxDataValList[i] == ajaxBoxDataValList[j]) {
                    updFac[targetBoxDataValList[i]] = targetBoxDataFacList[i];
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                dispListId[targetBoxDataValList[i]] = targetBoxId;
                dispListText[targetBoxDataValList[i]] =
                    targetBoxDataTextList[i];
                ajaxBoxDataValList.push(targetBoxDataValList[i]);
                updFac[targetBoxDataValList[i]] = targetBoxDataFacList[i];
            }
        }
    }

    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            AJAX_PARAM: JSON.stringify({
                AJAX_KOUZA_PARAM: JSON.stringify(ajaxBoxDataValList),
            }),
            cmd: "getFacilityKouzaList",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        var gunCodeList = {};
        var paramList = $.parseJSON(data);
        for (var i = 0; i < paramList.length; i++) {
            var boxGun = $(
                "#facility_targetList_gun_" + paramList[i]["Kouza"]
            )[0];
            var boxKamoku = $(
                "#facility_targetList_kamoku_" + paramList[i]["Kouza"]
            )[0];
            if (boxGun && boxKamoku) {
                boxGun.innerText =
                    paramList[i]["GunCode"] + ":" + paramList[i]["GunName"];
                boxKamoku.innerText =
                    paramList[i]["Kamoku"] + ":" + paramList[i]["KamokuName"];
                gunCodeList[paramList[i]["GunCode"]] = true;
            } else {
                if (
                    paramList[i]["GunCode"] != "0000" &&
                    gunCodeList[paramList[i]["GunCode"]]
                ) {
                    var trData = $(
                        '<tr data-facilityKouza="' +
                            paramList[i]["Kouza"] +
                            '"><td>' +
                            paramList[i]["GunCode"] +
                            ":" +
                            (null == paramList[i]["GunName"]
                                ? ""
                                : paramList[i]["GunName"]) +
                            "</td><td>" +
                            paramList[i]["Kamoku"] +
                            ":" +
                            paramList[i]["KamokuName"] +
                            "</td><td>" +
                            paramList[i]["Kouza"] +
                            ":" +
                            paramList[i]["KouzaName"] +
                            "</td></tr>"
                    );
                    $("#facility_targetList").append(trData);
                }
            }
        }
        var tags = $("#facility_targetList tr").sort(function (a, b) {
            var reg1 = a.innerHTML.match(
                /<t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.+)<\/t[dh]>/
            );
            var gunParts1 = reg1[1].split(":");
            var kouzaParts1 = reg1[3].split(":");

            var reg2 = b.innerHTML.match(
                /<t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.+)<\/t[dh]>/
            );
            var gunParts2 = reg2[1].split(":");
            var kouzaParts2 = reg2[3].split(":");

            if (reg1[1] == "群") {
                return -1;
            } else if (reg2[1] == "群") {
                return 1;
            } else {
                if (gunParts1[0] > gunParts2[0]) {
                    return 1;
                } else if (gunParts1[0] == gunParts2[0]) {
                    if (kouzaParts1[0] > kouzaParts2[0]) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    return -1;
                }
            }
        });

        tdData = new Array();
        tags.each(function () {
            tdData.push(this.outerHTML);
        });
        $("#facility_targetList tr").remove();
        for (i = 0; i < tdData.length; i++) {
            $("#facility_targetList").append(tdData[i]);
        }
        $("#facility_targetList tr").off("click");
        $("#facility_targetList tr").on("click", function (e) {
            var facilityKouza = $(this)[0].getAttribute("data-facilityKouza");

            var ajaxParam = {};
            ajaxParam["AJAX_KOUZA_SELECT"] = facilityKouza;

            var sdataTime = new Date(
                Date.parse($("input[name=START_DATE]").val())
            );
            sdataTime.setDate(sdataTime.getDate() + parseInt(idArray[1]));
            var year = sdataTime.getFullYear();
            var month = sdataTime.getMonth() + 1;
            var day = sdataTime.getDate();
            var newDate = year + "-" + month + "-" + day;

            ajaxParam["AJAX_DATE"] = newDate;
            ajaxParam["AJAX_PERIOD"] = idArray[2];
            //講座のTRを全て白
            $("#facility_targetList tr").css("background-color", "");
            if (facilityKouza != null) {
                var self = this;
                $(self).css("background-color", "pink");
                $("#facilityBox").prop(
                    "data-selectfacility-kouza",
                    facilityKouza
                );
                if (!$(self).prop("data-selected")) {
                    $(self).prop("data-selected", true);

                    $.ajax({
                        url: "knjb3042index.php",
                        type: "POST",
                        data: {
                            cmd: "getFacilitySelect",
                            YEAR_SEME: document.forms[0].YEAR_SEME.value,
                            AJAX_PARAM: JSON.stringify(ajaxParam),
                        },
                    }).done(function (data, textStatus, jqXHR) {
                        var facList = $.parseJSON(data);
                        $("#facility_selected option").remove();
                        $("#facility_collect option").remove();

                        var setSelectFacility = new Array();
                        var srcFac = updFac[facilityKouza];
                        var srcFacList =
                            srcFac == "0" ? new Array() : srcFac.split(":");
                        var srcFacObj = {};
                        for (var i = 0; i < srcFacList.length; i++) {
                            srcFacObj[srcFacList[i]] = true;
                        }

                        for (var faci = 0; faci < facList.length; faci++) {
                            var option = $("<option>")
                                .val(facList[faci]["Faccd"])
                                .text(facList[faci]["FacilityName"]);
                            if (srcFacObj[facList[faci]["Faccd"]]) {
                                $("#facility_selected").append(option);
                                setSelectFacility.push(facList[faci]["Faccd"]);
                            } else {
                                $("#facility_collect").append(option);
                            }
                        }
                        $(self).prop(
                            "data-selectfacility",
                            setSelectFacility.join(":")
                        );
                    });
                } else {
                    $.ajax({
                        url: "knjb3042index.php",
                        type: "POST",
                        data: {
                            cmd: "getFacilitySelect",
                            YEAR_SEME: document.forms[0].YEAR_SEME.value,
                            AJAX_PARAM: JSON.stringify(ajaxParam),
                        },
                    }).done(function (data, textStatus, jqXHR) {
                        var selectFacility = $(self).prop(
                            "data-selectfacility"
                        );
                        var selectFacilityList =
                            selectFacility == null || selectFacility == ""
                                ? new Array()
                                : selectFacility.split(":");
                        var selectFacilityObj = {};
                        for (var i = 0; i < selectFacilityList.length; i++) {
                            selectFacilityObj[selectFacilityList[i]] = true;
                        }

                        var facList = $.parseJSON(data);
                        $("#facility_selected option").remove();
                        $("#facility_collect option").remove();
                        for (var faci = 0; faci < facList.length; faci++) {
                            var option = $("<option>")
                                .val(facList[faci]["Faccd"])
                                .text(facList[faci]["FacilityName"]);
                            if (selectFacilityObj[facList[faci]["Faccd"]]) {
                                $("#facility_selected").append(option);
                            } else {
                                $("#facility_collect").append(option);
                            }
                        }
                    });
                }
            }
        });
    });

    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            cmd: "getFacility",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        $("#facility_collect")[0].innerHTML = data;
    });
}
function facility_insAll_button() {
    $("#facility_selected option").each(function () {
        $("#facility_collect").append(this.outerHTML);
    });

    $("#facility_selected option").remove();
    $("#facility_collect option").each(function () {
        $("#facility_selected").append(this.outerHTML);
    });
    $("#facility_collect option").remove();
    facilityListSort("facility_collect", "facility_selected");
}
function facility_ins_button() {
    $("#facility_collect option:selected").each(function () {
        $("#facility_selected").append(this.outerHTML);
    });
    facilityListSort("facility_collect", "facility_selected");
}
function facility_del_button() {
    $("#facility_selected option:selected").each(function () {
        $("#facility_collect").append(this.outerHTML);
    });
    facilityListSort("facility_selected", "facility_collect");
}
function facility_delAll_button() {
    $("#facility_collect option").each(function () {
        $("#facility_selected").append(this.outerHTML);
    });

    $("#facility_collect option").remove();
    $("#facility_selected option").each(function () {
        $("#facility_collect").append(this.outerHTML);
    });
    $("#facility_selected option").remove();
    facilityListSort("facility_selected", "facility_collect");
}
function saveFacility() {
    var optSelect = new Array();
    $("#facility_selected option").each(function (index, element) {
        optSelect.push(element.value);
    });
    var kouza = $("#facilityBox").prop("data-selectfacility-kouza");
    $("#facility_targetList tr[data-facilitykouza=" + kouza + "]").prop(
        "data-selectfacility",
        optSelect.join(":")
    );
}

//施設OKボタン
function facility_ok_button() {
    var setElementObj = {};
    $("#facility_targetList tr").each(function (index, element) {
        var elmKouza = element.getAttribute("data-facilitykouza");
        if (elmKouza) {
            var setFac = $(element).prop("data-selectfacility");
            setElementObj[elmKouza] =
                setFac == null || setFac == undefined || setFac == ""
                    ? "0"
                    : setFac;
        }
    });

    var opeLog = {};
    //0:KOMA 1:日付番号(開始日付の番号は0、1、2・・・) 2:校時 3:行番号
    var idArray = document.forms[0].selectStartTD.value.split("_");

    for (lineCnt = 1; lineCnt <= document.forms[0].MAX_LINE.value; lineCnt++) {
        var targetBoxId =
            "KOMA_" + idArray[1] + "_" + idArray[2] + "_" + lineCnt;
        var targetBox = $("#" + targetBoxId)[0];
        var targetBoxDataVal = targetBox.getAttribute("data-val");
        var targetBoxDataValList =
            targetBoxDataVal == "" ? new Array() : targetBoxDataVal.split(":");
        var targetBoxDataFac = targetBox.getAttribute("data-selectfacility");
        var targetBoxDataFacList =
            targetBoxDataFac == "" ? new Array() : targetBoxDataFac.split(",");

        for (var i = 0; i < targetBoxDataValList.length; i++) {
            if (setElementObj[targetBoxDataValList[i]]) {
                targetBoxDataFacList[i] =
                    setElementObj[targetBoxDataValList[i]];
            }
        }
        targetBox.setAttribute(
            "data-selectfacility",
            targetBoxDataFacList.join(",")
        );
        targetBox.setAttribute("data-dirty", "1");

        // 施設が変更されている場合
        if (targetBoxDataFac != targetBoxDataFacList.join(",")) {
            var facList = [];
            if (targetBoxDataFac) {
                facList = targetBoxDataFac.split(",");
            }
            for (var i = 0; i < targetBoxDataValList.length; i++) {
                if (setElementObj[targetBoxDataValList[i]]) {
                    // 操作履歴へ登録した日付-校時-講座は再登録しない
                    if (
                        opeLog[
                            idArray[1] +
                                "_" +
                                idArray[2] +
                                "-" +
                                targetBoxDataValList[i]
                        ]
                    ) {
                        continue;
                    }
                    opeLog[
                        idArray[1] +
                            "_" +
                            idArray[2] +
                            "-" +
                            targetBoxDataValList[i]
                    ] = "1";

                    var chairList = [];
                    chairList.push(targetBoxDataValList[i]);
                    var fromFac = facList[i];
                    var toFac = setElementObj[targetBoxDataValList[i]];
                    if (fromFac == "0") fromFac = "";
                    if (toFac == "0") toFac = "";
                    //操作履歴追加
                    writeOperationHistory(
                        "FAC",
                        chairList,
                        targetBox,
                        targetBox,
                        fromFac,
                        toFac
                    );
                }
            }
        }
    }

    var srcBox = $(
        "#KOMA_" + idArray[1] + "_" + idArray[2] + "_" + idArray[3]
    )[0];
    var LObj = new LinkingCellObj();
    kouziList = LObj.makeKouziLsit(srcBox);
    var calcList = LObj.makeCalcList(
        srcBox,
        new Array(),
        kouziList,
        document.forms[0].MAX_LINE.value
    );
    LObj.checkMeiboAndFacUseCalcList(
        calcList,
        document.forms[0].MAX_LINE.value
    );

    $("#dialogBox").hide();

    var kouzaId = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .val();
    //cframeを閉じる
    closeit();
    //移動先セルの状態でcframeを開き直す
    setClickValue("chairInfo", kouzaId);
}

function facilityListSort(srcName, targetName) {
    $("#" + srcName + " option:selected").remove();
    var tags = $("#" + targetName + " option").sort(function (a, b) {
        var aVal = a.value;
        var bVal = b.value;
        if (aVal > bVal) {
            return 1;
        } else {
            return -1;
        }
    });

    optionData = new Array();
    tags.each(function () {
        optionData.push(this.outerHTML);
    });
    $("#" + targetName + " option").remove();
    for (i = 0; i < optionData.length; i++) {
        $("#" + targetName).append(optionData[i]);
    }
}
//試験会場初期化
function testFacilityInitFunc() {
    //0:KOMA 1:日付番号(開始日付の番号は0、1、2・・・) 2:校時 3:行番号
    var idArray = document.forms[0].selectStartTD.value.split("_");
    var cntNum = null;

    var kouzaName = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .text();
    var kouzaId = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .val();

    //日付/校時の列内にある全講座
    var ajaxBoxDataValList = new Array();

    kouzaNameParts = kouzaName.split(":");
    var setKouzaName1 = kouzaNameParts[1].replace("\r\n", "").replace("\n", "");
    var trData = $(
        '<tr data-test-facilityKouza="' +
            kouzaId +
            '"><td id="test_facility_targetList_gun_' +
            kouzaId +
            '"></td><td id="test_facility_targetList_kamoku_' +
            kouzaId +
            '"></td><td>' +
            kouzaId +
            ":" +
            setKouzaName1 +
            "</td></tr>"
    );
    $("#test_facility_targetList").append(trData);
    ajaxBoxDataValList.push(kouzaId);

    //日付、校時の全講座をajaxBoxDataValListに追加
    var dispListId = {};
    var dispListText = {};
    var updFac = {};
    for (lineCnt = 1; lineCnt <= document.forms[0].MAX_LINE.value; lineCnt++) {
        var targetBoxId =
            "KOMA_" + idArray[1] + "_" + idArray[2] + "_" + lineCnt;
        var targetBox = $("#" + targetBoxId)[0];
        var targetBoxDataVal = targetBox.getAttribute("data-val");
        var targetBoxDataValList =
            targetBoxDataVal == "" ? new Array() : targetBoxDataVal.split(":");
        var targetBoxDataText = targetBox.getAttribute("data-text");
        var targetBoxDataTextList =
            targetBoxDataText == ""
                ? new Array()
                : targetBoxDataText.split(",");
        var targetBoxDataFac = targetBox.getAttribute(
            "data-selecttestfacility"
        );
        var targetBoxDataFacList =
            targetBoxDataFac == "" ? new Array() : targetBoxDataFac.split(",");
        //一コマの講座ループ
        //TODO:高速化予定(全ては必要ない、群でなければその講座のみ、群であれば同一群のみ)
        for (var i = 0; i < targetBoxDataValList.length; i++) {
            var flag = false;
            for (var j = 0; j < ajaxBoxDataValList.length; j++) {
                if (targetBoxDataValList[i] == ajaxBoxDataValList[j]) {
                    updFac[targetBoxDataValList[i]] = targetBoxDataFacList[i];
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                dispListId[targetBoxDataValList[i]] = targetBoxId;
                dispListText[targetBoxDataValList[i]] =
                    targetBoxDataTextList[i];
                ajaxBoxDataValList.push(targetBoxDataValList[i]);
                updFac[targetBoxDataValList[i]] = targetBoxDataFacList[i];
            }
        }
    }

    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            AJAX_PARAM: JSON.stringify({
                AJAX_KOUZA_PARAM: JSON.stringify(ajaxBoxDataValList),
            }),
            cmd: "getTestFacilityKouzaList",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        var gunCodeList = {};
        var paramList = $.parseJSON(data);
        for (var i = 0; i < paramList.length; i++) {
            var boxGun = $(
                "#test_facility_targetList_gun_" + paramList[i]["Kouza"]
            )[0];
            var boxKamoku = $(
                "#test_facility_targetList_kamoku_" + paramList[i]["Kouza"]
            )[0];
            if (boxGun && boxKamoku) {
                boxGun.innerText =
                    paramList[i]["GunCode"] + ":" + paramList[i]["GunName"];
                boxKamoku.innerText =
                    paramList[i]["Kamoku"] + ":" + paramList[i]["KamokuName"];
                gunCodeList[paramList[i]["GunCode"]] = true;
            } else {
                if (
                    paramList[i]["GunCode"] != "0000" &&
                    gunCodeList[paramList[i]["GunCode"]]
                ) {
                    var trData = $(
                        '<tr data-test-facilityKouza="' +
                            paramList[i]["Kouza"] +
                            '"><td>' +
                            paramList[i]["GunCode"] +
                            ":" +
                            (null == paramList[i]["GunName"]
                                ? ""
                                : paramList[i]["GunName"]) +
                            "</td><td>" +
                            paramList[i]["Kamoku"] +
                            ":" +
                            paramList[i]["KamokuName"] +
                            "</td><td>" +
                            paramList[i]["Kouza"] +
                            ":" +
                            paramList[i]["KouzaName"] +
                            "</td></tr>"
                    );
                    $("#test_facility_targetList").append(trData);
                }
            }
        }
        var tags = $("#test_facility_targetList tr").sort(function (a, b) {
            var reg1 = a.innerHTML.match(
                /<t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.+)<\/t[dh]>/
            );
            var gunParts1 = reg1[1].split(":");
            var kouzaParts1 = reg1[3].split(":");

            var reg2 = b.innerHTML.match(
                /<t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.+)<\/t[dh]><t[dh][^>]*>(.+)<\/t[dh]>/
            );
            var gunParts2 = reg2[1].split(":");
            var kouzaParts2 = reg2[3].split(":");

            if (reg1[1] == "群") {
                return -1;
            } else if (reg2[1] == "群") {
                return 1;
            } else {
                if (gunParts1[0] > gunParts2[0]) {
                    return 1;
                } else if (gunParts1[0] == gunParts2[0]) {
                    if (kouzaParts1[0] > kouzaParts2[0]) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    return -1;
                }
            }
        });

        tdData = new Array();
        tags.each(function () {
            tdData.push(this.outerHTML);
        });
        $("#test_facility_targetList tr").remove();
        for (i = 0; i < tdData.length; i++) {
            $("#test_facility_targetList").append(tdData[i]);
        }
        $("#test_facility_targetList tr").off("click");
        $("#test_facility_targetList tr").on("click", function (e) {
            var testFacilityKouza = $(this)[0].getAttribute(
                "data-test-facilityKouza"
            );

            var ajaxParam = {};
            ajaxParam["AJAX_KOUZA_SELECT"] = testFacilityKouza;

            var sdataTime = new Date(
                Date.parse($("input[name=START_DATE]").val())
            );
            sdataTime.setDate(sdataTime.getDate() + parseInt(idArray[1]));
            var year = sdataTime.getFullYear();
            var month = sdataTime.getMonth() + 1;
            var day = sdataTime.getDate();
            var newDate = year + "-" + month + "-" + day;

            ajaxParam["AJAX_DATE"] = newDate;
            ajaxParam["AJAX_PERIOD"] = idArray[2];
            //講座のTRを全て白
            $("#test_facility_targetList tr").css("background-color", "");
            if (testFacilityKouza != null) {
                var self = this;
                $(self).css("background-color", "pink");
                $("#testFacilityBox").prop(
                    "data-selecttestfacility-kouza",
                    testFacilityKouza
                );
                if (!$(self).prop("data-selected")) {
                    $(self).prop("data-selected", true);

                    $.ajax({
                        url: "knjb3042index.php",
                        type: "POST",
                        data: {
                            cmd: "getTestFacilitySelect",
                            YEAR_SEME: document.forms[0].YEAR_SEME.value,
                            AJAX_PARAM: JSON.stringify(ajaxParam),
                        },
                    }).done(function (data, textStatus, jqXHR) {
                        var facList = $.parseJSON(data);
                        $("#test_facility_selected option").remove();
                        $("#test_facility_collect option").remove();

                        var setSelectTestFacility = new Array();
                        var srcFac = updFac[testFacilityKouza];
                        var srcFacList =
                            srcFac == "0" ? new Array() : srcFac.split(":");
                        var srcFacObj = {};
                        for (var i = 0; i < srcFacList.length; i++) {
                            srcFacObj[srcFacList[i]] = true;
                        }

                        for (var faci = 0; faci < facList.length; faci++) {
                            var option = $("<option>")
                                .val(facList[faci]["Faccd"])
                                .text(facList[faci]["FacilityName"]);
                            if (srcFacObj[facList[faci]["Faccd"]]) {
                                $("#test_facility_selected").append(option);
                                setSelectTestFacility.push(
                                    facList[faci]["Faccd"]
                                );
                            } else {
                                $("#test_facility_collect").append(option);
                            }
                        }
                        $(self).prop(
                            "data-selecttestfacility",
                            setSelectTestFacility.join(":")
                        );
                    });
                } else {
                    $.ajax({
                        url: "knjb3042index.php",
                        type: "POST",
                        data: {
                            cmd: "getTestFacilitySelect",
                            YEAR_SEME: document.forms[0].YEAR_SEME.value,
                            AJAX_PARAM: JSON.stringify(ajaxParam),
                        },
                    }).done(function (data, textStatus, jqXHR) {
                        var selectTestFacility = $(self).prop(
                            "data-selecttestfacility"
                        );
                        var selectTestFacilityList =
                            selectTestFacility == null ||
                            selectTestFacility == ""
                                ? new Array()
                                : selectTestFacility.split(":");
                        var selectTestFacilityObj = {};
                        for (
                            var i = 0;
                            i < selectTestFacilityList.length;
                            i++
                        ) {
                            selectTestFacilityObj[
                                selectTestFacilityList[i]
                            ] = true;
                        }

                        var facList = $.parseJSON(data);
                        $("#test_facility_selected option").remove();
                        $("#test_facility_collect option").remove();
                        for (var faci = 0; faci < facList.length; faci++) {
                            var option = $("<option>")
                                .val(facList[faci]["Faccd"])
                                .text(facList[faci]["FacilityName"]);
                            if (selectTestFacilityObj[facList[faci]["Faccd"]]) {
                                $("#test_facility_selected").append(option);
                            } else {
                                $("#test_facility_collect").append(option);
                            }
                        }
                    });
                }
            }
        });
    });

    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            cmd: "getTestFacility",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        $("#test_facility_collect")[0].innerHTML = data;
    });
}
function test_facility_insAll_button() {
    $("#test_facility_selected option").each(function () {
        $("#test_facility_collect").append(this.outerHTML);
    });

    $("#test_facility_selected option").remove();
    $("#test_facility_collect option").each(function () {
        $("#test_facility_selected").append(this.outerHTML);
    });
    $("#test_facility_collect option").remove();
    testFacilityListSort("test_facility_collect", "test_facility_selected");
}
function test_facility_ins_button() {
    $("#test_facility_collect option:selected").each(function () {
        $("#test_facility_selected").append(this.outerHTML);
    });
    testFacilityListSort("test_facility_collect", "test_facility_selected");
}
function test_facility_del_button() {
    $("#test_facility_selected option:selected").each(function () {
        $("#test_facility_collect").append(this.outerHTML);
    });
    testFacilityListSort("test_facility_selected", "test_facility_collect");
}
function test_facility_delAll_button() {
    $("#test_facility_collect option").each(function () {
        $("#test_facility_selected").append(this.outerHTML);
    });

    $("#test_facility_collect option").remove();
    $("#test_facility_selected option").each(function () {
        $("#test_facility_collect").append(this.outerHTML);
    });
    $("#test_facility_selected option").remove();
    testFacilityListSort("test_facility_selected", "test_facility_collect");
}
function saveTestFacility() {
    var optSelect = new Array();
    $("#test_facility_selected option").each(function (index, element) {
        optSelect.push(element.value);
    });
    var kouza = $("#testFacilityBox").prop("data-selecttestfacility-kouza");
    $(
        "#test_facility_targetList tr[data-test-facilitykouza=" + kouza + "]"
    ).prop("data-selecttestfacility", optSelect.join(":"));
}

//試験会場OKボタン
function test_facility_ok_button() {
    var setElementObj = {};
    $("#test_facility_targetList tr").each(function (index, element) {
        var elmKouza = element.getAttribute("data-test-facilitykouza");
        if (elmKouza) {
            var setFac = $(element).prop("data-selecttestfacility");
            setElementObj[elmKouza] =
                setFac == null || setFac == undefined || setFac == ""
                    ? "0"
                    : setFac;
        }
    });

    var opeLog = {};
    //0:KOMA 1:日付番号(開始日付の番号は0、1、2・・・) 2:校時 3:行番号
    var idArray = document.forms[0].selectStartTD.value.split("_");

    for (lineCnt = 1; lineCnt <= document.forms[0].MAX_LINE.value; lineCnt++) {
        var targetBoxId =
            "KOMA_" + idArray[1] + "_" + idArray[2] + "_" + lineCnt;
        var targetBox = $("#" + targetBoxId)[0];
        var targetBoxDataVal = targetBox.getAttribute("data-val");
        var targetBoxDataValList =
            targetBoxDataVal == "" ? new Array() : targetBoxDataVal.split(":");
        var targetBoxDataTestFac = targetBox.getAttribute(
            "data-selecttestfacility"
        );
        var targetBoxDataTestFacList =
            targetBoxDataTestFac == ""
                ? new Array()
                : targetBoxDataTestFac.split(",");

        for (var i = 0; i < targetBoxDataValList.length; i++) {
            if (setElementObj[targetBoxDataValList[i]]) {
                targetBoxDataTestFacList[i] =
                    setElementObj[targetBoxDataValList[i]];
            }
        }
        targetBox.setAttribute(
            "data-selecttestfacility",
            targetBoxDataTestFacList.join(",")
        );
        targetBox.setAttribute("data-dirty", "1");
        // 試験会場が変更されている場合
        if (targetBoxDataTestFac != targetBoxDataTestFacList.join(",")) {
            var testFacList = [];
            if (targetBoxDataTestFac) {
                testFacList = targetBoxDataTestFac.split(",");
            }
            for (var i = 0; i < targetBoxDataValList.length; i++) {
                if (setElementObj[targetBoxDataValList[i]]) {
                    // 操作履歴へ登録した日付-校時-講座は再登録しない
                    if (
                        opeLog[
                            idArray[1] +
                                "_" +
                                idArray[2] +
                                "-" +
                                targetBoxDataValList[i]
                        ]
                    ) {
                        continue;
                    }
                    opeLog[
                        idArray[1] +
                            "_" +
                            idArray[2] +
                            "-" +
                            targetBoxDataValList[i]
                    ] = "1";

                    var chairList = [];
                    chairList.push(targetBoxDataValList[i]);
                    var fromTestFac = testFacList[i];
                    var toTestFac = setElementObj[targetBoxDataValList[i]];
                    if (fromTestFac == "0") fromTestFac = "";
                    if (toTestFac == "0") toTestFac = "";
                    //操作履歴追加
                    writeOperationHistory(
                        "TESTFAC",
                        chairList,
                        targetBox,
                        targetBox,
                        fromTestFac,
                        toTestFac
                    );
                }
            }
        }
    }

    var srcBox = $(
        "#KOMA_" + idArray[1] + "_" + idArray[2] + "_" + idArray[3]
    )[0];
    var LObj = new LinkingCellObj();
    kouziList = LObj.makeKouziLsit(srcBox);
    var calcList = LObj.makeCalcList(
        srcBox,
        new Array(),
        kouziList,
        document.forms[0].MAX_LINE.value
    );
    LObj.checkMeiboAndFacUseCalcList(
        calcList,
        document.forms[0].MAX_LINE.value
    );

    $("#dialogBox").hide();

    var kouzaId = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .val();
    //cframeを閉じる
    closeit();
    //移動先セルの状態でcframeを開き直す
    setClickValue("chairInfo", kouzaId);
}

function testFacilityListSort(srcName, targetName) {
    $("#" + srcName + " option:selected").remove();
    var tags = $("#" + targetName + " option").sort(function (a, b) {
        var aVal = a.value;
        var bVal = b.value;
        if (aVal > bVal) {
            return 1;
        } else {
            return -1;
        }
    });

    optionData = new Array();
    tags.each(function () {
        optionData.push(this.outerHTML);
    });
    $("#" + targetName + " option").remove();
    for (i = 0; i < optionData.length; i++) {
        $("#" + targetName).append(optionData[i]);
    }
}

//集計フラグ/授業形態初期化
function countLessonInitFunc(gradeHr, countFlg) {
    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            cmd: "getLessonModeList",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        var startBox = $("#" + document.forms[0].selectStartTD.value)[0];
        var startBoxDataVal = startBox.getAttribute("data-val");
        var startBoxDataValList =
            startBoxDataVal == "" ? new Array() : startBoxDataVal.split(":");
        var kouzaId = $("#cframe")
            .contents()
            .find("select[name=CI_CHAIRCD] option:selected")
            .val();
        var dataCnt = 0;
        for (var boxCnt = 0; boxCnt < startBoxDataValList.length; boxCnt++) {
            if (startBoxDataValList[boxCnt] == kouzaId) {
                dataCnt = boxCnt;
                break;
            }
        }

        var startBoxDataCountLesson = startBox.getAttribute(
            "data-count-lesson"
        );
        var startBoxDataCountLessonList =
            startBoxDataCountLesson == ""
                ? new Array()
                : startBoxDataCountLesson.split(",");

        var gradeHrArray = gradeHr.split(",");
        var dataCountLessonArray = startBoxDataCountLessonList[dataCnt].split(
            ":"
        );
        for (var cntI = 0; cntI < gradeHrArray.length; cntI++) {
            var gradeHrVal = gradeHrArray[cntI].split(":");
            var gHr = gradeHrVal[0];
            var gHrName = gradeHrVal[1];

            var countLessonValList = dataCountLessonArray[cntI].split("/");
            var countLessonValListParts = countLessonValList[1].split("-");
            var lessonMode = countLessonValListParts[1];

            var setData = data.replace(
                /DLOG_LESSONMODE/g,
                "DLOG_LESSONMODE_" + gHr
            );
            setData = setData.replace(/selected/g, "");
            var lessonObj = $(setData);
            lessonObj
                .find("option[value=" + lessonMode + "]")
                .attr("selected", true);

            var setHtml =
                '<tr data-gradehr="' + gHr + '"><td>' + gHrName + "</td>";
            var setChecked = countFlg == "1" ? ' checked="checked" ' : "";
            setHtml +=
                '<td><input type="checkbox" name="DLOG_COUNTFLG_' +
                gHr +
                '" value="1" ' +
                setChecked +
                "></td>";
            if ($("#SCH_DIV2").is(":checked")) {
                setHtml += "<td>" + lessonObj[0].outerHTML + "</td></tr>";
            }
            $("#countLesson_targetList").append(setHtml);
        }
    });
}

//集計フラグ/授業形態OKボタン
function countLesson_ok_button() {
    var kouzaId = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .val();

    var setCountLesson = "";
    var sep = "";
    $("#countLesson_targetList tr").each(function (index, element) {
        var gradeHr = element.getAttribute("data-gradehr");
        if (gradeHr != null && gradeHr != undefined && gradeHr != "") {
            var setCountFlg = $("input[name=DLOG_COUNTFLG_" + gradeHr + "]").is(
                ":checked"
            )
                ? "1"
                : "0";
            if ($("#SCH_DIV1").is(":checked")) {
                var setLessonMode = "00";
            } else {
                var setLessonMode =
                    $("#DLOG_LESSONMODE_" + gradeHr).val() == ""
                        ? "00"
                        : $("#DLOG_LESSONMODE_" + gradeHr).val();
            }
            setCountLesson +=
                sep + gradeHr + "/" + setCountFlg + "-" + setLessonMode;
            sep = ":";
        }
    });

    var opeLog = {};
    //0:KOMA 1:日付番号(開始日付の番号は0、1、2・・・) 2:校時 3:行番号
    var idArray = document.forms[0].selectStartTD.value.split("_");
    for (
        var lineCnt = 1;
        lineCnt <= document.forms[0].MAX_LINE.value;
        lineCnt++
    ) {
        var targetBoxId =
            "KOMA_" + idArray[1] + "_" + idArray[2] + "_" + lineCnt;
        var targetBox = $("#" + targetBoxId)[0];
        var targetBoxDataVal = targetBox.getAttribute("data-val");
        var targetBoxDataValList =
            targetBoxDataVal == "" ? new Array() : targetBoxDataVal.split(":");
        var targetBoxDataCountLesson = targetBox.getAttribute(
            "data-count-lesson"
        );
        var targetBoxDataCountLessonList =
            targetBoxDataCountLesson == ""
                ? new Array()
                : targetBoxDataCountLesson.split(",");
        for (var cntI = 0; cntI < targetBoxDataValList.length; cntI++) {
            if (targetBoxDataValList[cntI] == kouzaId) {
                targetBoxDataCountLessonList[cntI] = setCountLesson;
            }
        }
        targetBox.setAttribute(
            "data-count-lesson",
            targetBoxDataCountLessonList.join(",")
        );
        targetBox.setAttribute("data-dirty", "1");

        // 集計フラグ/授業形態が変更されている場合
        if (
            targetBoxDataCountLesson != targetBoxDataCountLessonList.join(",")
        ) {
            var countLessonList = [];
            if (targetBoxDataCountLesson) {
                countLessonList = targetBoxDataCountLesson.split(",");
            }
            for (var i = 0; i < targetBoxDataValList.length; i++) {
                if (kouzaId == targetBoxDataValList[i]) {
                    // 操作履歴へ登録した日付-校時-講座は再登録しない
                    if (opeLog[idArray[1] + "_" + idArray[2] + "-" + kouzaId]) {
                        continue;
                    }
                    opeLog[idArray[1] + "_" + idArray[2] + "-" + kouzaId] = "1";

                    var chairList = [];
                    chairList.push(targetBoxDataValList[i]);
                    var fromCountLesson = countLessonList[i];
                    var toCountLesson = targetBoxDataCountLessonList[i];
                    if (fromCountLesson == "0") fromCountLesson = "";
                    if (toCountLesson == "0") toCountLesson = "";
                    //操作履歴追加
                    writeOperationHistory(
                        "COUNTLESSON",
                        chairList,
                        targetBox,
                        targetBox,
                        fromCountLesson,
                        toCountLesson
                    );
                }
            }
        }
    }
    $("#dialogBox").hide();

    //cframeを閉じる
    closeit();
    //移動先セルの状態でcframeを開き直す
    setClickValue("chairInfo", kouzaId);
}

//レイアウト変更初期化
function layoutStaffChairBoxInitFunc() {
    //メイン画面の年組、科目、群のコンボをコピー
    var parentSchoolKind = "";
    if ($("select[name=schoolKind_STAFF]")[0]) {
        parentSchoolKind = $("select[name=schoolKind_STAFF]")[0].outerHTML;
        parentSchoolKind = parentSchoolKind.replace(
            /onchange="[^"]+"/,
            'onchange="staffCmbChage()"'
        );
        parentSchoolKind = parentSchoolKind.replace(/selected=""/, "");
        parentSchoolKind =
            "校種：" + parentSchoolKind.replace(/name="([^"]+)"/, 'name="$1"');
    }
    var parentGradeHr = "";
    if ($("select[name=GRAND_HR_CLASSCD]")[0]) {
        parentGradeHr = $("select[name=GRAND_HR_CLASSCD]")[0].outerHTML;
        parentGradeHr = parentGradeHr.replace(
            /onchange="[^"]+"/,
            'onchange="staffCmbChage()"'
        );
        parentGradeHr = parentGradeHr.replace(/selected=""/, "");
        parentGradeHr =
            "年組：" +
            parentGradeHr.replace(/name="([^"]+)"/, 'name="$1_STAFF"');
    }
    var parentSubclass = "";
    if ($("select[name=SUBCLASSCD]")[0]) {
        parentSubclass = $("select[name=SUBCLASSCD]")[0].outerHTML;
        parentSubclass = parentSubclass.replace(
            /onchange="[^"]+"/,
            'onchange="staffCmbChage()"'
        );
        parentSubclass = parentSubclass.replace(/selected=""/, "");
        parentSubclass =
            "科目：" +
            parentSubclass.replace(/name="([^"]+)"/, 'name="$1_STAFF"');
    }
    var parentGun = "";
    if ($("select[name=GUNCD]")[0]) {
        parentGun = $("select[name=GUNCD]")[0].outerHTML;
        parentGun = parentGun.replace(
            /onchange="[^"]+"/,
            'onchange="staffCmbChage()"'
        );
        parentGun = parentGun.replace(/selected=""/, "");
        parentGun =
            "群：" + parentGun.replace(/name="([^"]+)"/, 'name="$1_STAFF"');
    }

    $("#layoutStaffChairCmbId")[0].innerHTML =
        parentSchoolKind +
        "　" +
        parentGradeHr +
        "　" +
        parentSubclass +
        "　" +
        parentGun;

    if (document.forms[0].LEFT_MENU.value == "1") {
        $("#btn_dispStaffChairLoad")[0].innerHTML = "表示中職員読込";
    } else if (document.forms[0].LEFT_MENU.value == "4") {
        $("#btn_dispStaffChairLoad")[0].innerHTML = "表示中講座読込";
    }

    dispLoad("StaffChair");
}

//レイアウト変更初期化
function layoutHrSubclassBoxInitFunc() {
    if (document.forms[0].LEFT_MENU.value == "2") {
        $("#btn_dispHrSuclassLoad")[0].innerHTML = "表示中クラス読込";
        $("#layoutHrSubclassCmbId")[0].innerHTML = "";
    } else if (document.forms[0].LEFT_MENU.value == "3") {
        $("#btn_dispHrSuclassLoad")[0].innerHTML = "表示中科目読込";
    }

    dispLoad("HrSubclass");
}

//レイアウト職員の条件コンボ変更
function staffCmbChage() {
    //検索クリア
    if (document.forms[0].STAFF_CHAIR_SEARCH) {
        document.forms[0].STAFF_CHAIR_SEARCH.value = "";
    }

    var selectedStaffChairList = $("#category_staffchair_selected option");
    selectedStaffChairList.remove();

    var ajaxParam = {};
    ajaxParam["LAYOUT_LIST_NAME"] = "category_staffchair_name";
    $.get(
        "knjb3042index.php?YEAR_SEME=" +
            document.forms[0].YEAR_SEME.value +
            "&LEFT_MENU=" +
            document.forms[0].LEFT_MENU.value +
            "&AJAX_PARAM=" +
            JSON.stringify(ajaxParam) +
            "&cmd=getLayoutStaffChair",
        null,
        function (data) {
            //右リスト作成
            $("#layoutStaffChairRight")[0].innerHTML = data;

            //JSON形式のパラメーター追加
            var ajaxParam = {};
            ajaxParam["LAYOUT_LIST_NAME"] = "category_staffchair_selected";
            if ($("select[name=schoolKind_STAFF]")[0]) {
                ajaxParam["SCHOOL_KIND_STAFF"] =
                    document.forms[0].schoolKind_STAFF.value;
            }
            ajaxParam["GRAND_HR_CLASSCD_STAFF"] =
                document.forms[0].GRAND_HR_CLASSCD_STAFF.value;
            ajaxParam["SUBCLASSCD_STAFF"] =
                document.forms[0].SUBCLASSCD_STAFF.value;
            ajaxParam["GUNCD_STAFF"] = document.forms[0].GUNCD_STAFF.value;
            $.get(
                "knjb3042index.php?YEAR_SEME=" +
                    document.forms[0].YEAR_SEME.value +
                    "&LEFT_MENU=" +
                    document.forms[0].LEFT_MENU.value +
                    "&AJAX_PARAM=" +
                    JSON.stringify(ajaxParam) +
                    "&cmd=getLayoutStaffChair",
                null,
                function (data) {
                    //左リスト
                    $("#layoutStaffChairLeft")[0].innerHTML = data;

                    //左のリストにある先生は右リストから除く
                    rightListDropLeftList("StaffChair");
                }
            );
        }
    );
}
function hrSubclassCmbChage() {
    //検索クリア
    if (document.forms[0].STAFF_CHAIR_SEARCH) {
        document.forms[0].STAFF_CHAIR_SEARCH.value = "";
    }

    var selectedStaffChairList = $("#category_hrsubclass_selected option");
    selectedStaffChairList.remove();

    var ajaxParam = {};
    ajaxParam["LAYOUT_LIST_NAME"] = "category_hrsubclass_name";
    $.get(
        "knjb3042index.php?YEAR_SEME=" +
            document.forms[0].YEAR_SEME.value +
            "&LEFT_MENU=" +
            document.forms[0].LEFT_MENU.value +
            "&AJAX_PARAM=" +
            JSON.stringify(ajaxParam) +
            "&cmd=getLayoutHrSubclass",
        null,
        function (data) {
            //右リスト作成
            $("#layoutHrSubclassRight")[0].innerHTML = data;

            //JSON形式のパラメーター追加
            var ajaxParam = {};
            ajaxParam["LAYOUT_LIST_NAME"] = "category_hrsubclass_selected";
            ajaxParam["SCHOOL_KIND_HRSUBCLASS"] =
                document.forms[0].schoolKind_HrSubClass.value;
            $.get(
                "knjb3042index.php?YEAR_SEME=" +
                    document.forms[0].YEAR_SEME.value +
                    "&LEFT_MENU=" +
                    document.forms[0].LEFT_MENU.value +
                    "&AJAX_PARAM=" +
                    JSON.stringify(ajaxParam) +
                    "&cmd=getLayoutHrSubclass",
                null,
                function (data) {
                    //左リスト
                    $("#layoutHrSubclassLeft")[0].innerHTML = data;

                    //左のリストにある先生は右リストから除く
                    rightListDropLeftList("HrSubclass");
                }
            );
        }
    );
}

function dispLoad(upperName) {
    if (upperName == "StaffChair") {
        $("select[name=schoolKind_STAFF]").val("");
        $("select[name=GRAND_HR_CLASSCD_STAFF]").val("");
        $("select[name=SUBCLASSCD_STAFF]").val("");
        $("select[name=GUNCD_STAFF]").val("");
    }
    if (document.forms[0].LEFT_MENU.value == "3") {
        $("select[name=schoolKind_HrSubClass]").val("");
    }

    //検索クリア
    if (document.forms[0].STAFF_CHAIR_SEARCH) {
        document.forms[0].STAFF_CHAIR_SEARCH.value = "";
    }

    var lowerName = upperName.toLowerCase();
    //左リスト
    var selectedList = $("#category_" + lowerName + "_selected option");
    selectedList.remove();

    var ajaxParam = {};
    ajaxParam["LAYOUT_LIST_NAME"] = "category_" + lowerName + "_name";
    $.get(
        "knjb3042index.php?YEAR_SEME=" +
            document.forms[0].YEAR_SEME.value +
            "&LEFT_MENU=" +
            document.forms[0].LEFT_MENU.value +
            "&AJAX_PARAM=" +
            JSON.stringify(ajaxParam) +
            "&cmd=getLayout" +
            upperName,
        null,
        function (data) {
            //右リスト作成
            $("#layout" + upperName + "Right")[0].innerHTML = data;

            //右リスト退避
            globalLayoutRightListFull = $("#category_" + lowerName + "_name")[0]
                .innerHTML;

            //親画面の縦軸
            var dispList = $(".redips-mark");
            dispList.each(function (index, element) {
                //非表示は飛ばす
                if ($(element).is(":hidden")) {
                    return true;
                }
                var elemetArray = element.innerHTML.split("<br>");
                var elemetKey = element.getAttribute("data-keyname");
                if (elemetKey) {
                    var option = $("<option>")
                        .val(elemetKey)
                        .text(elemetArray[0] + "：" + elemetArray[1]);
                    $("#category_" + lowerName + "_selected").append(option);
                }
            });
            //左のリストにある先生は右リストから除く
            rightListDropLeftList(upperName);
        }
    );
}

//ダブルクリックから時間割種別
function testInitFunc() {
    $("#DLOG_TESTCD option:first-child").prop("selected", true);
    var kouzaId = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .val();
    var srcBox = $("#" + document.forms[0].selectStartTD.value)[0];
    var srcDataVal = srcBox.getAttribute("data-val");
    var srcDataValList = srcDataVal == "" ? new Array() : srcDataVal.split(":");
    var srcDataTest = srcBox.getAttribute("data-test");
    var srcDataTestList =
        srcDataTest == "" ? new Array() : srcDataTest.split(",");
    var isZero = true;
    for (var dataCnt = 0; dataCnt < srcDataValList.length; dataCnt++) {
        if (kouzaId == srcDataValList[dataCnt]) {
            isZero = srcDataTestList[dataCnt] == "0";
            if (!isZero) {
                $("#DLOG_TESTCD").val(srcDataTestList[dataCnt]);
            }
        }
    }
    if (isZero) {
        $("#DLOG_TESTCD").prop("disabled", true);
        $("#TEST_SELECT_RADIO1").prop("checked", true);
        $("#TEST_SELECT_RADIO2").prop("checked", false);
    } else {
        $("#DLOG_TESTCD").prop("disabled", false);
        $("#TEST_SELECT_RADIO1").prop("checked", false);
        $("#TEST_SELECT_RADIO2").prop("checked", true);
    }
}

function testSelectChange() {
    if ($("#TEST_SELECT_RADIO1").is(":checked")) {
        $("#DLOG_TESTCD").prop("disabled", true);
    } else {
        $("#DLOG_TESTCD").prop("disabled", false);
    }
}

//時間割種別ＯＫ処理
function dialogTestOkButton() {
    var kouzaId = $("#cframe")
        .contents()
        .find("select[name=CI_CHAIRCD] option:selected")
        .val();

    //イベントキャンセル
    $(window).off("setCellObjEmptyTargetEvent");

    var opeLog = {};
    var idArray = document.forms[0].selectStartTD.value.split("_");
    for (lineCnt = 1; lineCnt <= document.forms[0].MAX_LINE.value; lineCnt++) {
        var targetBoxId =
            "KOMA_" + idArray[1] + "_" + idArray[2] + "_" + lineCnt;
        var targetBox = $("#" + targetBoxId)[0];
        var targetBoxDataVal = targetBox.getAttribute("data-val");
        var targetBoxDataValList =
            targetBoxDataVal == "" ? new Array() : targetBoxDataVal.split(":");
        var targetTest = targetBox.getAttribute("data-test");
        var targetTestList =
            targetTest == "" ? new Array() : targetTest.split(",");
        for (
            var dataCnt = 0;
            dataCnt < targetBoxDataValList.length;
            dataCnt++
        ) {
            if (kouzaId == targetBoxDataValList[dataCnt]) {
                targetTestList[dataCnt] = $("#TEST_SELECT_RADIO1").is(
                    ":checked"
                )
                    ? "0"
                    : $("#DLOG_TESTCD").val();
            }
        }
        targetBox.setAttribute("data-test", targetTestList.join(","));

        var Obj = new CellObj();
        Obj.setCellObjEmptyTarget(targetBox);
        Obj.writeClass(Obj.src);

        // 時間割種別が変更されている場合
        if (targetTest != targetTestList.join(",")) {
            var testList = [];
            if (targetTest) {
                testList = targetTest.split(",");
            }
            for (var i = 0; i < targetBoxDataValList.length; i++) {
                // 操作履歴へ登録した日付-校時-講座は再登録しない
                if (kouzaId == targetBoxDataValList[i]) {
                    if (opeLog[idArray[1] + "_" + idArray[2] + "-" + kouzaId]) {
                        continue;
                    }
                    opeLog[idArray[1] + "_" + idArray[2] + "-" + kouzaId] = "1";

                    var chairList = [];
                    chairList.push(targetBoxDataValList[i]);
                    var fromTest = testList[i];
                    var toTest = targetTestList[i];
                    if (fromTest == "0") fromTest = "";
                    if (toTest == "0") toTest = "";
                    //操作履歴追加
                    writeOperationHistory(
                        "TEST",
                        chairList,
                        targetBox,
                        targetBox,
                        fromTest,
                        toTest
                    );
                }
            }
        }
    }

    $("#dialogBox").hide();

    //cframeを閉じる
    closeit();
    //移動先セルの状態でcframeを開き直す
    setClickValue("chairInfo", kouzaId);
}

//右リストから左リストを除く
function rightListDropLeftList(upperName) {
    var lowerName = upperName.toLowerCase();
    var newSelectedList = $("#category_" + lowerName + "_selected option");

    var newNameList = $("#category_" + lowerName + "_name option");

    //左リスト
    newSelectedList.each(function (leftIndex, leftElement) {
        //右リスト
        newNameList.each(function (rightIndex, rightElement) {
            //左リストの職員は除く
            if (leftElement.value == rightElement.value) {
                $(
                    "#category_" +
                        lowerName +
                        "_name option[value=" +
                        rightElement.value +
                        "]"
                ).remove();
                return true;
            }
        });
    });
    globalLayoutRightList = $("#category_" + lowerName + "_name")[0].innerHTML;
}

//反映押下で職員の表示/非表示切替
function dispChange(upperName) {
    var lowerName = upperName.toLowerCase();
    var newSelectedList = $("#category_" + lowerName + "_selected option");

    //親画面の縦軸
    var dispList = $(".redips-mark");

    var parentIdxArray = {};
    dispList.each(function (index, element) {
        var elemetArray = element.innerHTML.split("<br>");

        $(element).parent().hide();

        //左リスト
        newSelectedList.each(function (leftIndex, leftElement) {
            var leftElmArray = $(leftElement).text().split("：");
            if (elemetArray[0] == leftElmArray[0]) {
                $(element).parent().show();
                parentIdxArray[index] = true;
                return true;
            }
        });
    });

    //親画面の縦軸
    var dispList = $("#REC2 tr");
    var setSep = "";
    document.forms[0].visibleLine.value = "";
    dispList.each(function (index, element) {
        $(element).hide();

        if (parentIdxArray[index]) {
            $(element).show();
            document.forms[0].visibleLine.value += setSep + index;
            setSep = ",";
        }
    });
    $("#dialogBox").hide();
}

//表示/非表示初期化
function dispLineIni() {
    var visibleList = document.forms[0].visibleLine.value.split(",");
    var visibleObj = {};
    for (var i = 0; i < visibleList.length; i++) {
        visibleObj[visibleList[i]] = true;
    }

    //親画面の縦軸
    var dispLineList = $("#REC tr");

    dispLineList.each(function (lineIndex, lineElement) {
        $(lineElement).hide();

        if (visibleObj[lineIndex]) {
            $(lineElement).show();
        }
    });

    //親画面の縦軸
    var dispLineList = $("#REC2 tr");
    dispLineList.each(function (lineIndex, lineElement) {
        $(lineElement).hide();

        if (visibleObj[lineIndex]) {
            $(lineElement).show();
        }
    });
}

//職員名の検索
function searchStaffChairName(obj) {
    $("#category_staffchair_name")[0].innerHTML = globalLayoutRightList;
    var rightList = $("#category_staffchair_name option");
    rightList.each(function (rightIndex, rightElement) {
        if ($(rightElement).text().indexOf(obj.value) === -1) {
            $(rightElement).remove();
        }
    });
}

//基本時間割更新初期化
function ptrnUpdateInitFunc() {
    var bscSeqText = $("#BSCSEQ option:selected").text();
    if ($("#BSCSEQ").val() != "0") {
        var bscSeqTextParts = bscSeqText.split(" ");
        $("#ptrnUpdate_TITLE").val(bscSeqTextParts[3]);
        $("#PTRN_UPDATE_RADIO1").prop("disabled", false);
        $("#PTRN_UPDATE_RADIO1").prop("checked", true);
        var idx = bscSeqTextParts[0];
    } else {
        $("#PTRN_UPDATE_RADIO1").prop("disabled", true);
        $("#PTRN_UPDATE_RADIO2").prop("checked", true);
        var idx = "0";
    }
    $("label[for=PTRN_UPDATE_RADIO1]")[0].innerHTML =
        "現在のSEQ(" + idx + ")で上書き保存";
}

// 背景色ダイアログ初期化処理
function backColorInitFunc() {
    // 選択色リスト
    var colorList = [
        { BACKCOLOR: "", FORECOLOR: "" },
        { BACKCOLOR: "#0000CC", FORECOLOR: "#FFFFFF" },
        { BACKCOLOR: "#009933", FORECOLOR: "#FFFFFF" },
        { BACKCOLOR: "#0099FF", FORECOLOR: "#FFFFFF" },
        { BACKCOLOR: "#3399FF", FORECOLOR: "#000000" },
        { BACKCOLOR: "#993333", FORECOLOR: "#FFFFFF" },
        { BACKCOLOR: "#999933", FORECOLOR: "#000000" },
        { BACKCOLOR: "#99CC66", FORECOLOR: "#000000" },
        { BACKCOLOR: "#CC6699", FORECOLOR: "#000000" },
        { BACKCOLOR: "#CCFF00", FORECOLOR: "#000000" },
    ];

    // 背景色のコンボ作成
    for (let index = 0; index < colorList.length; index++) {
        const element = colorList[index];

        var text = element["BACKCOLOR"];
        if (!text) {
            text = "初期値";
        }
        var option = $("<option>").val(element["BACKCOLOR"]).text(text);
        $(option).attr("forecolor", element["FORECOLOR"]);
        $(option).css({
            "background-color": element["BACKCOLOR"],
            color: element["FORECOLOR"],
        });

        $("select[name=backColorCapaOverColor]").append($(option).clone());
        $("select[name=backColorClassColor]").append($(option).clone());
        $("select[name=backColorSameMeiboColor]").append($(option).clone());
        $("select[name=backColorStdChairColor]").append($(option).clone());
    }

    // 時間割画面の初期値を取得
    $("select[name=backColorCapaOverColor]").val(
        $("#colorBoxCapaOver").attr("bkcolor-value")
    );
    $("select[name=backColorClassColor]").val(
        $("#colorBoxSelectClass").attr("bkcolor-value")
    );
    $("select[name=backColorSameMeiboColor]").val(
        $("#colorBoxSameMeibo").attr("bkcolor-value")
    );
    $("select[name=backColorStdChairColor]").val(
        $("#colorBoxStdChair").attr("bkcolor-value")
    );
    backColorSelectChange($("select[name=backColorClassColor]")[0]);
    backColorSelectChange($("select[name=backColorCapaOverColor]")[0]);
    backColorSelectChange($("select[name=backColorSameMeiboColor]")[0]);
    backColorSelectChange($("select[name=backColorStdChairColor]")[0]);

    /////////////////////////////////////////////////////////////////
    // 講座人数オーバーの初期設定
    //   処理なし

    /////////////////////////////////////////////////////////////////
    // 指定科目・講座の初期設定
    // 科目の一覧取得
    // 右側の科目一覧からコピーする
    subclassOptions = $("select[name=SUBCLASSCD] option");
    for (let index = 0; index < subclassOptions.length; index++) {
        const option = subclassOptions[index];
        $("select[name=backColorClassSelect]").append($(option).clone());
    }

    // 時間割画面の初期値を取得
    var classSelect = $("#colorBoxSelectClassText").text();
    var selectTextArry;
    if (classSelect) {
        selectTextArry = classSelect.split("・");
    }
    var classTextArry;
    if (selectTextArry) {
        classTextArry = selectTextArry[0].split(":");
    }
    if (classTextArry) {
        var classCd = classTextArry[0].substring(1);
        $("select[name=backColorClassSelect]").val(classCd);
        backColorSelectSubclass();
    }

    /////////////////////////////////////////////////////////////////
    // 同一名簿の初期設定
    // 時間割に設定されている講座の一覧を設定する
    var chairOptionList = [];
    var chairPutList = [];
    var targetBoxs = $('.targetbox[data-text!=""]');
    for (let index = 0; index < targetBoxs.length; index++) {
        const element = targetBoxs[index];
        var dataText = $(element).attr("data-text");
        var chairList = dataText.split(",");

        for (let i = 0; i < chairList.length; i++) {
            var text = chairList[i].toLowerCase();
            var chairText = text.split("<br>");

            if (!chairPutList[chairText[0]]) {
                chairPutList[chairText[0]] = chairText[1];
                chairOptionList.push({
                    KEY: chairText[0],
                    LABEL: chairText[1],
                });
            }
        }
    }
    // ソートする
    chairOptionList.sort(function (a, b) {
        if (a["KEY"] == b["KEY"]) {
            return 0;
        }
        if (a["KEY"] == b["KEY"]) {
            return 1;
        }
        if (a["KEY"] > b["KEY"]) {
            return 1;
        }
        return -1;
    });
    for (let index = 0; index < chairOptionList.length; index++) {
        const element = chairOptionList[index];
        var option = $("<option>")
            .val(element["KEY"])
            .text(element["KEY"] + " " + element["LABEL"]);
        $("select[name=backColorSubSameMeiboSelect]").append($(option));
    }
    // 時間割画面の初期値を取得
    var meiboSelect = $("#colorBoxSameMeiboText").text();
    var subclassTextArry;
    if (meiboSelect) {
        subclassTextArry = meiboSelect.split(" ");
    }
    if (subclassTextArry) {
        var subclassCd = subclassTextArry[0].substring(1);
        $("select[name=backColorSubSameMeiboSelect]").val(subclassCd);
    }

    /////////////////////////////////////////////////////////////////
    // 指定生徒の初期設定
    // 年組の一覧取得
    // 右側の年組一覧からコピーする
    hrclassOptions = $("select[name=GRAND_HR_CLASSCD] option");
    for (let index = 0; index < hrclassOptions.length; index++) {
        var option = hrclassOptions[index];
        var optClone = $(option).clone();
        $(optClone).text($(option).val() + " " + $(option).text());
        $("select[name=backColorHrClassSelect]").append($(optClone));
    }
    // 時間割画面の初期値を取得
    var hrclassSelect = $("#colorBoxStdChairText").text();
    var selectTextArry;
    if (hrclassSelect) {
        selectTextArry = hrclassSelect.split("・");
    }
    var hrclassTextArry;
    if (selectTextArry) {
        hrclassTextArry = selectTextArry[0].split(" ");
    }
    if (hrclassTextArry) {
        var hrclassCd = hrclassTextArry[0].substring(1);
        $("select[name=backColorHrClassSelect]").val(hrclassCd);
        backColorSelectHrclass();
    }
}
// 背景色コンボの選択変更時、自身の背景色を変更
function backColorSelectChange(obj) {
    var option = $("select[name=" + $(obj).prop("name") + "] option:selected");
    $(obj).css({
        "background-color": $(obj).val(),
        color: $(option).attr("forecolor"),
    });
}

// 背景色変更-科目変更時
function backColorSelectSubclass() {
    var selectClassCd = $("select[name=backColorClassSelect]").val();
    // 講座一覧は初期化
    $("select[name=backColorSubClassSelect] option").remove();
    if (selectClassCd) {
        var ajaxParam = [];
        $.ajax({
            url: "knjb3042index.php",
            type: "POST",
            data: {
                AJAX_PARAM: JSON.stringify(ajaxParam),
                cmd: "getBackColorChair",
                YEAR_SEME: document.forms[0].YEAR_SEME.value,
                GRAND_HR_CLASSCD: "",
                SUBCLASSCD: selectClassCd,
                GUNCD: "",
                STAFFCD: "",
                COUSECD: "",
                REMOVE_IDS: "",
                STAFFCD: "",
            },
        }).done(function (data, textStatus, jqXHR) {
            var paramList = $.parseJSON(data);
            var option = $("<option>").val("").text("");
            $("select[name=backColorSubClassSelect]").append(option);
            // 科目のリスト初期化
            for (var i = 0; i < paramList.length; i++) {
                var option = $("<option>")
                    .val(paramList[i]["value"])
                    .text(paramList[i]["label"]);
                $("select[name=backColorSubClassSelect]").append(option);
            }

            // 時間割画面の初期値を取得
            var classSelect = $("#colorBoxSelectClassText").text();
            var selectTextArry;
            if (classSelect) {
                selectTextArry = classSelect.split("・");
            }
            var classTextArry;
            if (selectTextArry && selectTextArry[1]) {
                classTextArry = selectTextArry[1].split(":");
            }
            if (classTextArry) {
                var subclassCd = classTextArry[0];
                $("select[name=backColorSubClassSelect]").val(subclassCd);
            }
        });
    }
}

// 背景色変更-年組変更時
function backColorSelectHrclass() {
    var selectHrClassCd = $("select[name=backColorHrClassSelect]").val();

    // 受講生一覧は初期化
    $("select[name=backColorStdSelect] option").remove();
    if (selectHrClassCd) {
        var hrClass = selectHrClassCd.split("-");
        var ajaxParam = {};
        ajaxParam["GRADE"] = hrClass[0];
        ajaxParam["HR_CLASS"] = hrClass[1];

        $.ajax({
            url: "knjb3042index.php",
            type: "POST",
            data: {
                AJAX_PARAM: JSON.stringify(ajaxParam),
                cmd: "getBackColorHrClassStdMeibo",
                YEAR_SEME: document.forms[0].YEAR_SEME.value,
                AJAX_PARAM: JSON.stringify(ajaxParam),
            },
        }).done(function (data, textStatus, jqXHR) {
            var paramList = $.parseJSON(data);
            var option = $("<option>").val("").text("");
            $("select[name=backColorStdSelect]").append(option);
            // 科目のリスト初期化
            for (var i = 0; i < paramList.length; i++) {
                var option = $("<option>").val(paramList[i]["SCHREGNO"]);
                $(option).text(
                    paramList[i]["SCHREGNO"] + " " + paramList[i]["NAME"]
                );
                $("select[name=backColorStdSelect]").append(option);
            }

            // 時間割画面の初期値を取得
            var hrClassSelect = $("#colorBoxStdChairText").text();
            var selectTextArry;
            if (hrClassSelect) {
                selectTextArry = hrClassSelect.split("・");
            }
            var stdTextArry;
            if (selectTextArry && selectTextArry[1]) {
                stdTextArry = selectTextArry[1].split(" ");
            }
            if (stdTextArry) {
                var stdCd = stdTextArry[0];
                $("select[name=backColorStdSelect]").val(stdCd);
            }
        });
    }
}

var interval;

// 背景色初期化処理
function executeBackColorInit() {
    $("#backColorProcessed").css("display", "table");

    interval = setInterval(function () {
        clearInterval(interval);
        // 背景色を変更する講座の一覧
        var changeChairList = [];
        // 時間割に設定されている講座の一覧を設定する
        var chairPutList = [];
        var targetBoxs = $('.targetbox[data-text!=""]');
        for (let index = 0; index < targetBoxs.length; index++) {
            const element = targetBoxs[index];
            var dataText = $(element).attr("data-text");
            var chairList = dataText.split(",");

            for (let i = 0; i < chairList.length; i++) {
                var text = chairList[i].toLowerCase();
                var chairText = text.split("<br>");

                if (!chairPutList[chairText[0]]) {
                    chairPutList[chairText[0]] = chairText[1];
                    changeChairList.push(chairText[0]);
                }
            }
        }
        executeBackColorChange(changeChairList);
        // 時間割画面の背景色情報を初期化する
        $("#colorBoxCapaOver").text("初期値");
        $("#colorBoxCapaOver").attr("bkcolor-value", "");
        $("#colorBoxCapaOver").css({ "background-color": "", color: "" });
        $("#colorBoxSelectClassText").text("");
        $("#colorBoxSelectClass").text("初期値");
        $("#colorBoxSelectClass").attr("bkcolor-value", "");
        $("#colorBoxSelectClass").css({ "background-color": "", color: "" });
        $("#colorBoxSameMeiboText").text("");
        $("#colorBoxSameMeibo").text("初期値");
        $("#colorBoxSameMeibo").attr("bkcolor-value", "");
        $("#colorBoxSameMeibo").css({ "background-color": "", color: "" });
        $("#colorBoxStdChairText").text("");
        $("#colorBoxStdChair").text("初期値");
        $("#colorBoxStdChair").attr("bkcolor-value", "");
        $("#colorBoxStdChair").css({ "background-color": "", color: "" });

        $("#backColorProcessed").hide();
    }, 10);
}

// 講座人数オーバー背景色変更
function executeBackColorCapaOver() {
    $("#backColorProcessed").css("display", "table");

    // 背景色を変更する講座の一覧
    var changeChairList = [];

    var ajaxParam = {};
    ajaxParam["UPDDATE"] = [];
    if ($("#SCH_DIV1").is(":checked")) {
        ajaxParam["UPDDATE"].push($("input[name=semesterEndDate]").val());
    } else {
        for (
            let index = 0;
            index < $("input[name=DATECNT_MAX]").val();
            index++
        ) {
            ajaxParam["UPDDATE"].push(
                $("input[name=UPDDATE" + index + "]").val()
            );
        }
    }
    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            AJAX_PARAM: JSON.stringify(ajaxParam),
            cmd: "getBackColorChairCapaOver",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);
        // 科目のリスト初期化
        for (var key in paramList) {
            var chairList = paramList[key];
            if (!changeChairList[key]) {
                changeChairList[key] = [];
            }
            for (var i = 0; i < chairList.length; i++) {
                changeChairList[key].push(chairList[i]);
            }
        }

        var colorOption = $(
            "select[name=backColorCapaOverColor] option:selected"
        );
        var backColor = $(colorOption).val();
        var foreColor = $(colorOption).attr("forecolor");
        executeBackColorChange(
            changeChairList,
            "backColorCapaOver",
            backColor,
            foreColor
        );

        // 時間割画面の背景色情報を変更する(講座人数オーバー)
        $("#colorBoxCapaOver").text($(colorOption).text());
        $("#colorBoxCapaOver").attr("bkcolor-value", backColor);
        $("#colorBoxCapaOver").css({
            "background-color": backColor,
            color: foreColor,
        });

        $("#backColorProcessed").hide();
    });
}

// 指定科目・講座 背景色変更
function executeBackColorClass() {
    $("#backColorProcessed").css("display", "table");

    interval = setInterval(function () {
        clearInterval(interval);
        // 背景色を変更する講座の一覧
        var changeChairList = [];

        var selected = $("select[name=backColorSubClassSelect]").val();
        // 講座が選択されている場合、選択されている講座のみ対象
        if (selected) {
            selected = ":selected";
        }
        var options = $(
            "select[name=backColorSubClassSelect] option" + selected
        );
        for (let index = 0; index < options.length; index++) {
            var element = options[index];
            var value = $(element).val();
            if (value) {
                var chairCd = value.split(":");
                changeChairList.push(chairCd[0]);
            }
        }

        var colorOption = $("select[name=backColorClassColor] option:selected");
        var backColor = $(colorOption).val();
        var foreColor = $(colorOption).attr("foreColor");
        executeBackColorChange(
            changeChairList,
            "backColorClass",
            backColor,
            foreColor
        );

        // 時間割画面の背景色情報を変更する(指定科目・講座)
        if ($("select[name=backColorClassSelect] option:selected").val()) {
            var text = $(
                "select[name=backColorClassSelect] option:selected"
            ).text();
            if (
                $("select[name=backColorSubClassSelect] option:selected").val()
            ) {
                text +=
                    "・" +
                    $(
                        "select[name=backColorSubClassSelect] option:selected"
                    ).text();
            }
            $("#colorBoxSelectClassText").text("（" + text + "）");
        }
        $("#colorBoxSelectClass").text($(colorOption).text());
        $("#colorBoxSelectClass").attr("bkcolor-value", backColor);
        $("#colorBoxSelectClass").css({
            "background-color": backColor,
            color: foreColor,
        });

        $("#backColorProcessed").hide();
    }, 10);
}
// 同一名簿 背景色変更
function executeBackColorSameMeibo() {
    $("#lockScreen").css({ width: $(document).width() + "px" });
    $("#lockScreen").css({ height: $(document).height() + "px" });
    $("#lockScreen").css("display", "table");
    $("#backColorProcessed").css("display", "table");

    // 背景色を変更する講座の一覧
    var changeChairList = {};

    var ajaxParam = {};
    ajaxParam["CHAIRCD"] = $("select[name=backColorSubSameMeiboSelect]").val();
    ajaxParam["UPDDATE"] = [];
    if ($("#SCH_DIV1").is(":checked")) {
        ajaxParam["UPDDATE"].push($("input[name=semesterEndDate]").val());
    } else {
        for (
            let index = 0;
            index < $("input[name=DATECNT_MAX]").val();
            index++
        ) {
            ajaxParam["UPDDATE"].push(
                $("input[name=UPDDATE" + index + "]").val()
            );
        }
    }
    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            AJAX_PARAM: JSON.stringify(ajaxParam),
            cmd: "getBackColorChairSameMeibo",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);
        // 科目のリスト初期化
        for (var key in paramList) {
            var chairList = paramList[key];
            if (!changeChairList[key]) {
                changeChairList[key] = [];
            }
            for (var i = 0; i < chairList.length; i++) {
                changeChairList[key].push(chairList[i]);
            }
        }

        var colorOption = $(
            "select[name=backColorSameMeiboColor] option:selected"
        );
        var backColor = $(colorOption).val();
        var foreColor = $(colorOption).attr("foreColor");
        executeBackColorChange(
            changeChairList,
            "backColorSameMeibo",
            backColor,
            foreColor
        );

        // 時間割画面の背景色情報を変更する(同一名簿)
        if (
            $("select[name=backColorSubSameMeiboSelect] option:selected").val()
        ) {
            var text = $(
                "select[name=backColorSubSameMeiboSelect] option:selected"
            ).text();
            $("#colorBoxSameMeiboText").text("（" + text + "）");
        }
        $("#colorBoxSameMeibo").text($(colorOption).text());
        $("#colorBoxSameMeibo").attr("bkcolor-value", backColor);
        $("#colorBoxSameMeibo").css({
            "background-color": backColor,
            color: foreColor,
        });

        $("#lockScreen").hide();
        $("#backColorProcessed").hide();
    });
}
// 生徒の受講講座 背景色変更
function executeBackColorStdChair() {
    var selectSchregNo = $("select[name=backColorStdSelect]").val();

    if (!selectSchregNo) {
        alert("生徒が指定されていません。生徒を選択してください。");
        return;
    }

    $("#lockScreen").css({ width: $(document).width() + "px" });
    $("#lockScreen").css({ height: $(document).height() + "px" });
    $("#lockScreen").css("display", "table");
    $("#backColorProcessed").css("display", "table");

    // 背景色を変更する講座の一覧
    var changeChairList = {};

    var ajaxParam = {};
    ajaxParam["SCHREGNO"] = $("select[name=backColorStdSelect]").val();
    ajaxParam["UPDDATE"] = [];
    if ($("#SCH_DIV1").is(":checked")) {
        ajaxParam["UPDDATE"].push($("input[name=semesterEndDate]").val());
    } else {
        for (
            let index = 0;
            index < $("input[name=DATECNT_MAX]").val();
            index++
        ) {
            ajaxParam["UPDDATE"].push(
                $("input[name=UPDDATE" + index + "]").val()
            );
        }
    }
    $.ajax({
        url: "knjb3042index.php",
        type: "POST",
        data: {
            AJAX_PARAM: JSON.stringify(ajaxParam),
            cmd: "getBackColorStdChair",
            YEAR_SEME: document.forms[0].YEAR_SEME.value,
        },
    }).done(function (data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);
        // 科目のリスト初期化
        for (var key in paramList) {
            var chairList = paramList[key];
            if (!changeChairList[key]) {
                changeChairList[key] = [];
            }
            for (var i = 0; i < chairList.length; i++) {
                changeChairList[key].push(chairList[i]);
            }
        }

        var colorOption = $(
            "select[name=backColorStdChairColor] option:selected"
        );
        var backColor = $(colorOption).val();
        var foreColor = $(colorOption).attr("foreColor");
        executeBackColorChange(
            changeChairList,
            "backColorStdChair",
            backColor,
            foreColor
        );

        // 時間割画面の背景色情報を変更する(指定生徒の受講講座)
        if ($("select[name=backColorHrClassSelect] option:selected").val()) {
            var text = $(
                "select[name=backColorHrClassSelect] option:selected"
            ).text();
            if ($("select[name=backColorStdSelect] option:selected").val()) {
                text +=
                    "・" +
                    $("select[name=backColorStdSelect] option:selected").text();
            }
            $("#colorBoxStdChairText").text("（" + text + "）");
        }
        $("#colorBoxStdChair").text($(colorOption).text());
        $("#colorBoxStdChair").attr("bkcolor-value", backColor);
        $("#colorBoxStdChair").css({
            "background-color": backColor,
            color: foreColor,
        });

        $("#lockScreen").hide();
        $("#backColorProcessed").hide();
    });
}

// 時間割の背景色変更
function executeBackColorChange(
    changeChairList,
    className,
    backColor,
    foreColor
) {
    if (className) {
        $("#backColorStyle_" + className + "").remove();
        if (backColor) {
            var style = '<style id="backColorStyle_' + className + '">';
            style += "  td." + className + " { ";
            style += "  background-color : " + backColor + ";";
            style += "  color : " + foreColor + ";";
            style += "} ";
            style += "</style>";
            $(document.forms[0]).append(style);
            // $(document.body).append(style);
        }
    } else {
        $("#backColorStyle_backColorCapaOver").remove();
        $("#backColorStyle_backColorClass").remove();
        $("#backColorStyle_backColorSameMeibo").remove();
        $("#backColorStyle_backColorStdChair").remove();
    }

    // 時間割に設定されている講座の一覧を設定する
    var targetBoxs = $('.targetbox[data-text!=""]');
    for (let index = 0; index < targetBoxs.length; index++) {
        const element = targetBoxs[index];
        var dataText = $(element).attr("data-text");
        var chairList = dataText.split(",");

        for (let i = 0; i < chairList.length; i++) {
            var text = chairList[i].toLowerCase();
            var chairText = text.split("<br>");
            var chairCd = chairText[0];
            // 講座番号が見つかった場合は背景色を変更する
            if (className == "" || className == "backColorClass") {
                if (changeChairList.indexOf(chairCd) >= 0) {
                    // $(element).css({'background-color' : backColor, 'color' : foreColor });
                    // 背景色を最後に追加する
                    if (className) {
                        $(element).removeClass(className);
                        $(element).addClass(className);
                        break;
                    } else {
                        // 背景色のクラスを削除
                        $(element).removeClass("backColorCapaOver");
                        $(element).removeClass("backColorClass");
                        $(element).removeClass("backColorSameMeibo");
                        $(element).removeClass("backColorStdChair");
                    }
                } else {
                    $(element).removeClass(className);
                }
            } else {
                // IDから日付を取得
                var idArray = $(element).attr("id").split("_");
                var dateKey;
                if ($("#SCH_DIV1").is(":checked")) {
                    dateKey = $("input[name=semesterEndDate]").val();
                } else {
                    dateKey = $("input[name=UPDDATE" + idArray[1] + "]").val();
                }
                if (
                    changeChairList[dateKey] &&
                    changeChairList[dateKey].indexOf(chairCd) >= 0
                ) {
                    // $(element).css({'background-color' : backColor, 'color' : foreColor });
                    // 背景色を最後に追加する
                    $(element).removeClass(className);
                    $(element).addClass(className);
                    break;
                } else {
                    $(element).removeClass(className);
                }
            }
        }
    }
}

function helpInitFunc() {
    var tabIndex = $("input[name=btn_help]")[0].getAttribute("data-tabIndex");
    if (!tabIndex) {
        tabIndex = 1;
    }
    helpBoxTab($("#helpBoxTable th:nth-child(" + tabIndex + ")"), tabIndex);
}
function helpBoxTab(obj, idx) {
    $("#helpBoxTable th").css("background-color", "");
    $(obj).css("background-color", "#FFCCCC");
    $("#helpBoxTable td div").hide();
    $("#helpBoxContents" + idx).show();
    $("input[name=btn_help]")[0].setAttribute("data-tabIndex", idx);
}
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function layoutMove(side, upperName) {
    var lowerName = upperName.toLowerCase();
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "rightall") {
        attribute1 = document.forms[0]["category_" + lowerName + "_name"];
        attribute2 = document.forms[0]["category_" + lowerName + "_selected"];
    } else {
        attribute1 = document.forms[0]["category_" + lowerName + "_selected"];
        attribute2 = document.forms[0]["category_" + lowerName + "_name"];
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        temp1[current2] = attribute2.options[i].value;
        tempa[current2] = attribute2.options[i].text;
        tempaa[current2] = String(attribute2.options[i].value) + "," + current2;
        current2++;
    }
    if (side == "rightall" || side == "leftall") {
        for (var i = 0; i < attribute1.length; i++) {
            if (attribute1.options[i].style.display != "none") {
                attribute1.options[i].selected = 1;
            }
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            temp1[current2] = attribute1.options[i].value;
            tempa[current2] = attribute1.options[i].text;
            tempaa[current2] =
                String(attribute1.options[i].value) + "," + current2;
            current2++;
        } else {
            temp2[current1] = attribute1.options[i].value;
            tempb[current1] = attribute1.options[i].text;
            current1++;
        }
    }

    ClearList(attribute2, attribute2);

    tempaa.sort();

    //generating new options
    for (var i = 0; i < current2; i++) {
        var val = tempaa[i];
        var tmp = val.split(",");
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text = tempa[tmp[1]];
    }
    attribute2.length = current2;

    //generating new options
    ClearList(attribute1, attribute1);
    if (current1 > 0) {
        for (var i = 0; i < current1; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }
    attribute1.length = current1;

    var rightFullObj = $(globalLayoutRightListFull);
    globalLayoutRightList = "";
    rightFullObj.each(function (rIndex, rElement) {
        var isExistFlg = false;
        $("#category_" + lowerName + "_selected option").each(function (
            lIndex,
            lElement
        ) {
            if (rElement.value == lElement.value) {
                isExistFlg = true;
                return false;
            }
        });
        if (!isExistFlg) {
            globalLayoutRightList += $(rElement)[0].outerHTML;
        }
    });

    if (document.forms[0].STAFF_CHAIR_SEARCH) {
        searchStaffChairName(document.forms[0].STAFF_CHAIR_SEARCH);
    }
}
