function baseTdtag(id) {
    return (
        '<td id="' +
        id +
        '" data-text="" data-val="" data-def="" data-test="" data-exec="" data-linking="" data-zyukou="" data-selectfacility="" data-selecttestfacility="" data-count-lesson="" data-dirty=""></td>'
    );
}

//各セルのオブジェクト
//基本的にこのオブジェクトを基準にsrcからtargetへとデータを移動する
function CellObj() {
    //this.src、this.targetのまとめて操作するタグのプロパティはdata～で始めてsrcとtargetで同じプロパティを用意すること。

    //移動元オブジェクト
    this.src = {
        box: null,
        id: null,
        dataDefList: null, //講座の元のコマの一覧。:デフォルト
        dataValList: null, //講座IDの一覧。:区切り
        dataTextList: null, //講座名の一覧。,区切り
        dataTestList: null, //テスト講座かどうか。TESTCDならテスト、0なら通常,区切り
        dataExecList: null, //出席済みかどうか。1なら出席済み。それ以外は出席前。
        dataZyukouList: null, //受講数オーバー
        dataFacilityList: null, //施設情報
        dataTestFacilityList: null, //試験会場情報
        dataCountLessonList: null, //出欠フラグ/授業形態情報
        linking: null, //連結授業用リンキングデータ。講座ID:コマID,コマID.../講座ID:コマID,コマID...の書式
        kamokuIdList: null, //残す対象
    };
    //移動先オブジェクト
    this.target = {
        box: null,
        id: null,
        dataDefList: null,
        dataValList: null,
        dataTextList: null,
        dataTestList: null,
        dataExecList: null,
        dataZyukouList: null,
        dataFacilityList: null,
        dataTestFacilityList: null,
        dataCountLessonList: null,
        linking: null,
        kamokuIdList: null, //移動対象
    };
    this.isCopy = false; //コピー用
    this.setCellObjEventCancel = false; //イベントの返り値の保持用プロパティ
    this.writeCellTargetEventCancel = false; //イベントの返り値の保持用プロパティ

    //初期化。セルの情報を取得
    this.setCellObj = function (srcBox, targetBox) {
        this.setCellObjEventCancel = false;
        $(window).trigger("setCellObjEvent", [this, srcBox, targetBox]);
        if (this.setCellObjEventCancel) {
            return;
        }
        this.src.box = srcBox;
        this.target.box = targetBox;
        this.src.id = srcBox.id;
        this.target.id = targetBox.id;

        var srcDataDef = srcBox.getAttribute("data-def");
        var targetDataDef = targetBox.getAttribute("data-def");
        var srcDataVal = srcBox.getAttribute("data-val");
        var targetDataVal = targetBox.getAttribute("data-val");
        var srcDataText = srcBox.getAttribute("data-text");
        var targetDataText = targetBox.getAttribute("data-text");
        var srcDataTest = srcBox.getAttribute("data-test");
        var targetDataTest = targetBox.getAttribute("data-test");
        var srcDataExec = srcBox.getAttribute("data-exec");
        var targetDataExec = targetBox.getAttribute("data-exec");
        var srcDataZyukou = srcBox.getAttribute("data-zyukou");
        var targetDataZyukou = targetBox.getAttribute("data-zyukou");
        var srcSelectFacility = srcBox.getAttribute("data-selectfacility");
        var targetSelectFacility = targetBox.getAttribute(
            "data-selectfacility"
        );
        var srcSelectTestFacility = srcBox.getAttribute(
            "data-selecttestfacility"
        );
        var targetSelectTestFacility = targetBox.getAttribute(
            "data-selecttestfacility"
        );
        var srcSelectCountLesson = srcBox.getAttribute("data-count-lesson");
        var targetSelectCountLesson = targetBox.getAttribute(
            "data-count-lesson"
        );

        this.src.dataDefList =
            srcDataDef == "" ? new Array() : srcDataDef.split(",");
        this.target.dataDefList =
            targetDataDef == "" ? new Array() : targetDataDef.split(",");
        this.src.dataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");
        this.target.dataValList =
            targetDataVal == "" ? new Array() : targetDataVal.split(":");
        this.src.dataTextList =
            srcDataText == "" ? new Array() : srcDataText.split(",");
        this.target.dataTextList =
            targetDataText == "" ? new Array() : targetDataText.split(",");
        this.src.dataTestList =
            srcDataTest == "" ? new Array() : srcDataTest.split(",");
        this.target.dataTestList =
            targetDataTest == "" ? new Array() : targetDataTest.split(",");
        this.src.dataExecList =
            srcDataExec == "" ? new Array() : srcDataExec.split(",");
        this.target.dataExecList =
            targetDataExec == "" ? new Array() : targetDataExec.split(",");
        this.src.dataZyukouList =
            srcDataZyukou == "" ? new Array() : srcDataZyukou.split(",");
        this.target.dataZyukouList =
            targetDataZyukou == "" ? new Array() : targetDataZyukou.split(",");
        this.src.dataFacilityList =
            srcSelectFacility == ""
                ? new Array()
                : srcSelectFacility.split(",");
        this.target.dataFacilityList =
            targetSelectFacility == ""
                ? new Array()
                : targetSelectFacility.split(",");
        this.src.dataTestFacilityList =
            srcSelectTestFacility == ""
                ? new Array()
                : srcSelectTestFacility.split(",");
        this.target.dataTestFacilityList =
            targetSelectTestFacility == ""
                ? new Array()
                : targetSelectTestFacility.split(",");
        this.src.dataCountLessonList =
            srcSelectCountLesson == ""
                ? new Array()
                : srcSelectCountLesson.split(",");
        this.target.dataCountLessonList =
            targetSelectCountLesson == ""
                ? new Array()
                : targetSelectCountLesson.split(",");

        this.src.linking = srcBox.getAttribute("data-linking");
        this.target.linking = targetBox.getAttribute("data-linking");
    };

    //targetを空で初期化。セルの情報を取得。削除で使用する。
    this.setCellObjEmptyTarget = function (srcBox, cntNum) {
        $(window).trigger("setCellObjEmptyTargetEvent", [this, srcBox, cntNum]);
        this.src.box = srcBox;
        this.target.box = null;
        this.src.id = srcBox.id;
        this.target.id = "";

        var srcDataDef = srcBox.getAttribute("data-def");
        var srcDataVal = srcBox.getAttribute("data-val");
        var srcDataText = srcBox.getAttribute("data-text");
        var srcDataTest = srcBox.getAttribute("data-test");
        var srcDataExec = srcBox.getAttribute("data-exec");
        var srcDataZyukou = srcBox.getAttribute("data-zyukou");
        var srcDataFacility = srcBox.getAttribute("data-selectfacility");
        var srcDataTestFacility = srcBox.getAttribute(
            "data-selecttestfacility"
        );
        var srcDataCountLesson = srcBox.getAttribute("data-count-lesson");

        this.src.dataDefList =
            srcDataDef == "" ? new Array() : srcDataDef.split(",");
        this.target.dataDefList = new Array();
        this.src.dataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");
        this.target.dataValList = new Array();
        this.src.dataTextList =
            srcDataText == "" ? new Array() : srcDataText.split(",");
        this.target.dataTextList = new Array();
        this.src.dataTestList =
            srcDataTest == "" ? new Array() : srcDataTest.split(",");
        this.target.dataTestList = new Array();
        this.src.dataExecList =
            srcDataExec == "" ? new Array() : srcDataExec.split(",");
        this.target.dataExecList = new Array();
        this.src.dataZyukouList =
            srcDataZyukou == "" ? new Array() : srcDataZyukou.split(",");
        this.target.dataZyukouList = new Array();
        this.src.dataFacilityList =
            srcDataFacility == "" ? new Array() : srcDataFacility.split(",");
        this.target.dataFacilityList = new Array();
        this.src.dataTestFacilityList =
            srcDataTestFacility == ""
                ? new Array()
                : srcDataTestFacility.split(",");
        this.target.dataTestFacilityList = new Array();
        this.src.dataCountLessonList =
            srcDataCountLesson == ""
                ? new Array()
                : srcDataCountLesson.split(",");
        this.target.dataCountLessonList = new Array();

        this.src.linking = srcBox.getAttribute("data-linking");
        this.target.linking = "";
    };

    //連結セルデータの範囲外チェック。
    this.checkLinkingCellsRange = function (isAlert) {
        var idList = this.target.id.split("_");
        if (this.target.id.indexOf("KOMA_99") !== -1) {
            return true;
        }
        var target = $("#" + this.target.id)[0];
        //移動先がない場合
        if (!target) {
            if (isAlert) {
                alert("セルが範囲外です。");
            }
            return false;
        }
        return true;
    };

    //移動元の講座と、移動先の講座重複チェック
    this.checkCellObjKouzaOverlap = function (isAlert) {
        //移動元のIDの一覧と移動先のID一覧でIDが重なっていたらエラー
        for (var i = 0; i < this.src.dataValList.length; i++) {
            for (var j = 0; j < this.target.dataValList.length; j++) {
                if (this.src.dataValList[i] == this.target.dataValList[j]) {
                    if (isAlert) {
                        alert("同じ時間に同じ講座は設定できません。");
                    }
                    return false;
                }
            }
        }
        return true;
    };

    //移動元の講座と、移動先の講座重複チェック。ポップアップ用
    this.checkCellObjKouzaOverlapPop = function (isAlert, cntNum) {
        //移動元のIDの一覧と移動先のIDのcntNum番目が重なっていたらエラー
        for (var i = 0; i < this.target.dataValList.length; i++) {
            if (this.target.dataValList[i] == this.src.dataValList[cntNum]) {
                if (isAlert) {
                    alert("同じ時間に同じ講座は設定できません。");
                }
                return false;
            }
        }
        return true;
    };

    //移動元の講座と、移動先の講座重複チェック。連結データ用
    //kamokuIdがない場合は、全件(２件以上のデータ移動)
    this.checkCellObjKouzaOverlapLinking = function (LObj, isAlert, kamokuId) {
        //kamokuIdがあればそれを、なければ移動元ID一覧を対象にする
        if (kamokuId) {
            var checkSrc = new Array(kamokuId);
        } else {
            var LObjDataVal = LObj.src.getAttribute("data-val");
            var checkSrc =
                LObjDataVal == "" ? new Array() : LObjDataVal.split(":");
        }
        for (var i = 0; i < checkSrc.length; i++) {
            flag = false;
            //移動先IDが連結IDの一覧に含まれていたらエラーなし。
            for (var j = 0; j < LObj.linkingObj.length; j++) {
                for (var k = 0; k < LObj.linkingObj[j].list.length; k++) {
                    if (this.target.id == LObj.linkingObj[j].list[k]) {
                        flag = true;
                    }
                }
            }
            if (!flag) {
                //移動先IDの一覧に上記検索対象が含まれていたらエラー
                for (var j = 0; j < this.target.dataValList.length; j++) {
                    if (checkSrc[i] == this.target.dataValList[j]) {
                        if (isAlert) {
                            alert("同じ時間に同じ講座は設定できません。");
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    };

    //移動元の講座と、移動先の講座重複チェック。連結データコピー用
    //kamokuIdがない場合は、全件(２件以上のデータ移動)
    this.checkCellObjKouzaOverlapLinkingCopy = function (
        LObj,
        isAlert,
        kamokuId
    ) {
        //kamokuIdがあればそれを、なければ移動元ID一覧を対象にする
        if (kamokuId) {
            var checkSrc = new Array(kamokuId);
        } else {
            var LObjDataVal = LObj.src.getAttribute("data-val");
            var checkSrc =
                LObjDataVal == "" ? new Array() : LObjDataVal.split(":");
        }
        for (var i = 0; i < checkSrc.length; i++) {
            flag = false;
            //移動先IDが連結IDの一覧に含まれていたらエラー。
            for (var j = 0; j < LObj.linkingObj.length; j++) {
                for (var k = 0; k < LObj.linkingObj[j].list.length; k++) {
                    if (this.target.id == LObj.linkingObj[j].list[k]) {
                        return false;
                    }
                }
            }
            if (!flag) {
                //移動先IDの一覧に上記検索対象が含まれていたらエラー
                for (var j = 0; j < this.target.dataValList.length; j++) {
                    if (checkSrc[i] == this.target.dataValList[j]) {
                        if (isAlert) {
                            alert("同じ時間に同じ講座は設定できません。");
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    };

    //セルからセルへの移動。
    this.calcCellObj_boxToBox = function () {
        //移動元IDを移動先に全移動。
        for (var i = 0; i < this.src.dataValList.length; i++) {
            for (var key in this.src) {
                if (key.indexOf("data") === 0) {
                    this.target[key].push(this.src[key][i]);
                }
            }
        }
        for (var key in this.src) {
            if (key.indexOf("data") === 0) {
                this.src[key] = new Array();
            }
        }
    };

    //ポップアップからセルへの移動。
    this.calcCellObj_popToBox = function (cntNum) {
        //移動元IDの一覧からcntNum番目を移動先に移動。
        //また移動元IDの一覧からcntNum番目を削除。
        var NewArrays = {};
        for (var key in this.src) {
            if (key.indexOf("data") === 0) {
                NewArrays[key] = new Array();
            }
        }

        for (var i = 0; i < this.src.dataValList.length; i++) {
            if (i == cntNum) {
                for (var key in this.src) {
                    if (key.indexOf("data") === 0) {
                        this.target[key].push(this.src[key][i]);
                    }
                }
            } else {
                for (var key in this.src) {
                    if (key.indexOf("data") === 0) {
                        NewArrays[key].push(this.src[key][i]);
                    }
                }
            }
        }
        for (var key in this.src) {
            if (key.indexOf("data") === 0) {
                this.src[key] = NewArrays[key];
            }
        }
    };

    //連結データ用
    //移動対象講座(linking)を、移動先(target)に移動
    this.calcCellObj_boxToBox_kamokuIdList = function (LObj, isPopup) {
        var srcNewArrays = {};
        var targetNewArrays = {};
        for (var key in this.src) {
            if (key.indexOf("data") === 0) {
                srcNewArrays[key] = new Array();
                targetNewArrays[key] = new Array();
            }
        }

        //セル内の移動元講座が、移動対象講座(kamokuIdList)に
        //あれば、移動用配列(targetNewArrays)に追加。
        //なければ、移動しない用配列(srcNewArrays)に追加。
        //kamokuIdListには、linkingにある講座しか入らない
        for (var i = 0; i < this.src.dataValList.length; i++) {
            var flag = false;
            for (var j = 0; j < this.target.kamokuIdList.length; j++) {
                if (this.src.dataValList[i] == this.target.kamokuIdList[j]) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                for (var key in this.src) {
                    if (key.indexOf("data") === 0) {
                        targetNewArrays[key].push(this.src[key][i]);
                    }
                }
            } else {
                for (var key in this.src) {
                    if (key.indexOf("data") === 0) {
                        srcNewArrays[key].push(this.src[key][i]);
                    }
                }
            }
        }

        //セル内の移動先講座が、linkingObjに
        //なければ、移動用配列(targetNewArrays)に追加。
        //元々、先のセルにあった講座もtargetの情報に入れる
        for (var i = 0; i < this.target.dataValList.length; i++) {
            var flag3 = false;
            for (var j = 0; j < LObj.linkingObj.length; j++) {
                for (var k = 0; k < LObj.linkingObj[j].list.length; k++) {
                    //this.target.id : KOMA_1_6_1
                    //this.target.id : KOMA_1_6_1
                    if (
                        this.target.id == LObj.linkingObj[j].list[k] &&
                        LObj.linkingObj[j].id == this.target.dataValList[i]
                    ) {
                        flag3 = true;
                        break;
                    }
                }
            }
            if (!flag3) {
                for (var key in this.src) {
                    if (key.indexOf("data") === 0) {
                        targetNewArrays[key].push(this.target[key][i]);
                    }
                }
            }
        }

        //自分の移動元が連結データの中心となる移動元と一致した場合。摘まんだセルかどうか
        //TODO:isPopupがfalseで来ることがない？
        if (this.src.id == LObj.src.id && !isPopup) {
            var toCellNewArrays = {};
            for (var key in this.src) {
                if (key.indexOf("data") === 0) {
                    toCellNewArrays[key] = new Array(); //単独だけ入る
                }
            }

            //セル内の移動元講座が、linkingIdList(連続のみ入ってる)に
            //なければ、移動用配列(targetNewArrays)に追加。
            //toCellNewArraysにも追加

            //移動元ID一覧から連結データのID一覧と一致しなかったものを移動先に追加。
            //これは連結データとともに単独データが重なっていた場合単独データを移動対象に追加するため
            //toCell～の配列は横移動の場合、単独データが隣のセルの移動対象に含まれてしまう問題があるので、
            //隣のセルから単独データを削除するために使用する。
            //kamokuIdListには、linkingにある講座しか入らない
            for (var i = 0; i < this.src.dataValList.length; i++) {
                var flag4 = false;
                for (var j = 0; j < LObj.linkingIdList.length; j++) {
                    //連続は除外
                    //LObj.linkingIdListには、連続のみ入っている
                    if (LObj.linkingIdList[j] == this.src.dataValList[i]) {
                        flag4 = true;
                        break;
                    }
                }
                if (!flag4) {
                    for (var key in this.src) {
                        if (key.indexOf("data") === 0) {
                            targetNewArrays[key].push(this.src[key][i]);
                            toCellNewArrays[key].push(this.src[key][i]);
                        }
                    }
                }
            }
            for (var key in this.src) {
                if (key.indexOf("data") === 0) {
                    this.target[key] = targetNewArrays[key];
                    this.src[key] = new Array();
                }
            }
            //セルを走査してこのセルが移動対象であるセルオブジェクトを見つける。
            //処理対象のセルのターゲットから単独を削除する。
            for (var i = 0; i < LObj.cells.length; i++) {
                for (var j = 0; j < LObj.cells[i].length; j++) {
                    if (LObj.cells[i][j].target.id == this.src.id) {
                        for (var key in this.src) {
                            if (key.indexOf("data") === 0) {
                                cellTargetNewArrays[key] = new Array();
                            }
                        }
                        //TODO何かおかしい↓ひとつ目のfor文はなくてもいい？？
                        //対象のセルの移動先ID一覧からtoCellの対象ID以外を新移動先ID一覧として生成し、セットする。
                        for (
                            var k = 0;
                            k < LObj.cells[i][j].target.dataValList.length;
                            k++
                        ) {
                            var flag5 = false;
                            for (
                                var l = 0;
                                l < toCellTargetDataValList.length;
                                l++
                            ) {
                                if (
                                    LObj.cells[i][j].target.dataValList[k] ==
                                    toCellTargetDataValList[l]
                                ) {
                                    flag5 = true;
                                }
                            }
                            if (!flag5) {
                                for (var key in this.src) {
                                    if (key.indexOf("data") === 0) {
                                        cellTargetNewArrays[key].push(
                                            LObj.cells[i][j].target[key][k]
                                        );
                                    }
                                }
                            }
                        }
                        for (var key in this.src) {
                            if (key.indexOf("data") === 0) {
                                cellTargetNewArrays[key].push(
                                    LObj.cells[i][j].target[key][k]
                                );
                                LObj.cells[i][j].target[key] =
                                    cellTargetNewArrays[key];
                            }
                        }
                        //TODO何かおかしい↑
                    }
                }
            }
        } else {
            //自分の移動元が連結データの中心となる移動元と一致しなかった場合
            for (var key in this.src) {
                if (key.indexOf("data") === 0) {
                    this.src[key] = srcNewArrays[key];
                    this.target[key] = targetNewArrays[key];
                }
            }
        }
    };

    //セル書き込み
    this.writeCellObj = function () {
        this.writeCellTarget();
        this.writeCellSrc();
    };

    //セル書き込み。移動先用。
    this.writeCellTarget = function () {
        this.writeCellTargetEventCancel = false;
        $(window).trigger("writeCellTargetEvent", [this, this.target]);
        if (this.writeCellTargetEventCancel) {
            return;
        }
        this.writeCellExec(this.target);
    };

    //セル書き込み。移動元用。
    this.writeCellSrc = function () {
        this.writeCellExec(this.src);
    };

    //実際のセル書き込み。
    this.writeCellExec = function (celldata) {
        celldata.box.setAttribute("data-def", celldata.dataDefList.join(","));
        celldata.box.setAttribute("data-val", celldata.dataValList.join(":"));
        celldata.box.setAttribute("data-text", celldata.dataTextList.join(","));
        celldata.box.setAttribute("data-test", celldata.dataTestList.join(","));
        celldata.box.setAttribute("data-exec", celldata.dataExecList.join(","));
        celldata.box.setAttribute(
            "data-zyukou",
            celldata.dataZyukouList.join(",")
        );
        celldata.box.setAttribute(
            "data-selectfacility",
            celldata.dataFacilityList.join(",")
        );
        celldata.box.setAttribute(
            "data-selecttestfacility",
            celldata.dataTestFacilityList.join(",")
        );
        celldata.box.setAttribute(
            "data-count-lesson",
            celldata.dataCountLessonList.join(",")
        );
        celldata.box.setAttribute("data-linking", celldata.linking);
        celldata.box.setAttribute("data-dirty", "1");
        if (celldata.dataValList.length == 0) {
            celldata.box.innerHTML = "";
        } else if (celldata.dataValList.length == 1) {
            celldata.box.innerHTML = celldata.dataTextList[0];
        } else {
            celldata.box.innerHTML = celldata.dataValList.length + "件のデータ";
        }
        this.writeClass(celldata);
    };

    //CSSクラスの追加
    this.writeClass = function (celldata) {
        var execClass = "no_syukketu";
        if (celldata.dataExecList.length == 1) {
            if (celldata.dataExecList[0] == "SYUKKETSU") {
                execClass = "syukketu";
            } else if (celldata.dataExecList[0] == "MI_SYUKKETSU") {
                execClass = "no_syukketu";
            } else if (celldata.dataExecList[0] == "ITIBU_SYUKKETSU") {
                execClass = "itibu_syukketu";
            }
        }

        var testBoxClass = "";
        if (celldata.dataZyukouList.join(",").indexOf("zyukou_box") !== -1) {
            var testBoxClass = "zyukou_box";
        } else {
            for (var i = 0; i < celldata.dataTestList.length; i++) {
                if (celldata.dataTestList[i] != "0") {
                    var testBoxClass = "test_box";
                }
            }
        }
        if (celldata.dataValList.length > 1) {
            var addClass = new Array("hukusuu_box");
        } else {
            var addClass = new Array();
            if (testBoxClass != "") {
                addClass.push(testBoxClass);
            }
            if (execClass != "") {
                addClass.push(execClass);
            }
        }
        celldata.box.className = "";
        celldata.box.classList.add("targetbox");
        for (var i = 0; i < addClass.length; i++) {
            celldata.box.classList.add(addClass[i]);
        }
    };

    //連結データ用
    //リンクデータの演算と、移動用科目リストの生成。
    this.calcLinkingText = function (LObj, kamokuId) {
        var LArray = this.src.linking.split("/");
        var newLingkingList = new Array();
        var newLingkingListSrc = new Array();
        var kamokuIdList = new Array();
        var kamokuIdListSrc = new Array();
        //移動元リンクデータを走査する
        for (var i = 0; i < LArray.length; i++) {
            //移動元リンクデータに連結メインオブジェクトの中心IDが含まれている場合
            //つまりリンクデータ更新対象であるならば、
            if (LArray[i].indexOf(LObj.src.id) != -1) {
                var LArrayParts = LArray[i].split(":");
                LArrayPartsList = LArrayParts[1].split(",");
                //リンクデータの各IDを新リンクデータのIDへと演算してセットする
                for (var j = 0; j < LArrayPartsList.length; j++) {
                    LArrayPartsList[j] = LObj.calcCellId(LArrayPartsList[j]);
                }
                //kamokuIdがリンクデータに含まれていた場合(つまりポップアップでkamokuIdを選択した場合)
                //またはkamokuIdがない場合(通常移動/全件移動)
                //新しいリンクデータ用配列に追加する
                if (kamokuId == null || LArrayParts[0] == kamokuId) {
                    newLingkingList.push(
                        LArrayParts[0] + ":" + LArrayPartsList.join(",")
                    );
                    kamokuIdList.push(LArrayParts[0]);
                } else {
                    //それ以外の場合、移動元リンクデータを更新するための配列に追加する。
                    var LArrayParts = LArray[i].split(":");
                    newLingkingListSrc.push(LArray[i]);
                    kamokuIdListSrc.push(LArrayParts[0]);
                }
            } else {
                //移動元リンクデータに連結メインオブジェクトの中心IDが含まれていない場合、
                //移動元リンクデータを更新するための配列に追加する。
                var LArrayParts = LArray[i].split(":");
                newLingkingListSrc.push(LArray[i]);
                kamokuIdListSrc.push(LArrayParts[0]);
            }
        }
        var linkingText = newLingkingList.join("/");
        //移動先リンクデータを走査して、連結オブジェクトのリンクデータが含まれていない場合に追加する。
        //含まれている場合とは、横移動の場合。
        //linkingデータが一緒の場合、連続授業1、2校時で1校時を摘まんで2校時に入れた場合
        if (this.target.linking != "") {
            var LArray2 = this.target.linking.split("/");
            var newTaregetLinkingList = new Array();
            for (var i = 0; i < LArray2.length; i++) {
                var flag = false;
                for (var j = 0; j < LObj.linkingKeyList.length; j++) {
                    if (LArray2[i] == LObj.linkingKeyList[j]) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    newTaregetLinkingList.push(LArray2[i]);
                }
            }
            this.target.linking = newTaregetLinkingList.join("/");
            if (this.target.linking != "" && linkingText != "") {
                this.target.linking = this.target.linking + "/";
            }
        }
        this.src.linking = newLingkingListSrc.join("/");
        this.src.kamokuIdList = kamokuIdListSrc;
        this.target.linking = this.target.linking + linkingText;
        this.target.kamokuIdList = kamokuIdList;
    };

    //セルからセル移動一括
    this.execCellObj_boxTobox = function (srcBox, targetBox, checkOnly) {
        isAlert = true;
        if (checkOnly) {
            isAlert = false;
        }
        this.setCellObj(srcBox, targetBox);
        if (this.checkLinkingCellsRange(isAlert)) {
            if (this.checkCellObjKouzaOverlap(isAlert)) {
                if (checkOnly) {
                    return 1;
                }
                this.calcCellObj_boxToBox();
                if (this.isCopy) {
                    this.writeCellTarget();
                } else {
                    this.writeCellObj();
                }
            } else {
                return 2;
            }
        } else {
            return 3;
        }
    };

    //ポップアップからセル移動一括
    this.execCellObj_popTobox = function (
        srcBox,
        cntNum,
        targetBox,
        checkOnly
    ) {
        isAlert = true;
        if (checkOnly) {
            isAlert = false;
        }
        this.setCellObj(srcBox, targetBox);
        if (this.checkLinkingCellsRange(isAlert)) {
            if (this.checkCellObjKouzaOverlapPop(isAlert, cntNum)) {
                if (checkOnly) {
                    return 1;
                }
                this.calcCellObj_popToBox(cntNum);
                if (this.isCopy) {
                    this.writeCellTarget();
                } else {
                    this.writeCellObj();
                }
            } else {
                return 2;
            }
        } else {
            return 3;
        }
    };
    //セル削除
    this.deleteCellObj_boxTobox = function (srcBox) {
        this.setCellObjEmptyTarget(srcBox);
        this.calcCellObj_boxToBox();
        this.writeCellSrc();
    };
    //セル削除ポップアップ用
    this.deleteCellObj_popTobox = function (srcBox, cntNum) {
        this.setCellObjEmptyTarget(srcBox, cntNum);
        this.calcCellObj_popToBox(cntNum);
        this.writeCellSrc();
    };
}

//連結データ制御用オブジェクト
function LinkingCellObj() {
    this.src; //移動元中心オブジェクト
    this.target; //移動先中心オブジェクト
    this.cells = {}; //連結データのセルリスト。二次元配列。
    this.linkingObj; //連結データの解析済みオブジェクト。
    this.linkingKeyList; //この連結データで使用される全リンクキーリスト。
    this.linkingIdList; //linkingKeyListと同じだが、キーではなく科目IDが入っている。
    this.isCopy = false; //コピーの場合
    this.isIrekae = false;

    //連結データ移動。セルからセルへ。
    this.execLinkingCellObj = function (srcBox, targetBox, checkOnly) {
        isAlert = true;
        if (checkOnly) {
            isAlert = false;
        }
        this.src = srcBox;
        this.target = targetBox;
        //リンクオブジェクトの生成
        this.setLinkingTextToObj(srcBox.getAttribute("data-linking"));
        //範囲外チェック
        if (this.checkLinkingCellsRange(isAlert)) {
            //セルオブジェクト生成
            this.setLinkingCells();
            //科目の重複チェック
            var func = this.isCopy
                ? this.checkLinkingCellsCopy
                : this.checkLinkingCells;
            if (func.call(this, isAlert)) {
                if (checkOnly) {
                    return 1;
                } else {
                    //セル書き込み
                    this.writeLinkingCells(false);
                }
            } else {
                return 2;
            }
        } else {
            return 3;
        }
    };

    //連結データ移動。ポップアップからセルへ。
    this.execLinkingCellObjPop = function (
        srcBox,
        cntNum,
        targetBox,
        checkOnly
    ) {
        isAlert = true;
        if (checkOnly) {
            isAlert = false;
        }
        this.src = srcBox;
        this.target = targetBox;

        //cntNumの値から科目IDを検索する。
        var attrDataVal = srcBox.getAttribute("data-val");
        var attrDataValList =
            attrDataVal == "" ? new Array() : attrDataVal.split(":");
        var kamokuId = null;
        for (var i = 0; i < attrDataValList.length; i++) {
            if (i == cntNum) {
                kamokuId = attrDataValList[i];
                break;
            }
        }

        //リンクオブジェクトの生成
        this.setLinkingTextToObj(srcBox.getAttribute("data-linking"), kamokuId);
        //範囲外チェック
        if (this.checkLinkingCellsRange(isAlert)) {
            //リンクキーリストの生成（ポップアップ用）
            this.setLinkingKyeListPop(kamokuId);
            //セルオブジェクト生成
            this.setLinkingCells(kamokuId);
            //科目の重複チェック 何れかの関数をfuncに入れる
            var func = this.isCopy
                ? this.checkLinkingCellsCopy
                : this.checkLinkingCells;
            //上記で入れた関数をコール.call()を使うのは
            //関数が代入されているだけで、thisが空？の状態なので、thisを使用する為には.call(this)とする必要がある。
            if (func.call(this, isAlert, kamokuId)) {
                if (checkOnly) {
                    return 1;
                } else {
                    //セル書き込み
                    this.writeLinkingCells(true);
                }
            } else {
                return 2;
            }
        } else {
            return 3;
        }
    };
    //連結データ削除。
    this.deleteLinkingCellObj = function (srcBox) {
        this.src = srcBox;
        //リンクオブジェクトの生成
        this.setLinkingTextToObj(srcBox.getAttribute("data-linking"));
        //セルオブジェクトの生成(移動先オブジェクトは空で)
        this.setLinkingCellsEmptyTarget();
        //セル書き込み
        this.writeLinkingCellsSrc(false);
    };

    //連結データ削除。ポップアップ用。
    this.deleteLinkingCellObjPop = function (srcBox, cntNum) {
        this.src = srcBox;

        //cntNumの値から科目IDを検索する。
        var attrDataVal = srcBox.getAttribute("data-val");
        var attrDataValList =
            attrDataVal == "" ? new Array() : attrDataVal.split(":");
        var kamokuId = null;
        for (var i = 0; i < attrDataValList.length; i++) {
            if (i == cntNum) {
                kamokuId = attrDataValList[i];
                break;
            }
        }

        //リンクオブジェクトの生成
        this.setLinkingTextToObj(srcBox.getAttribute("data-linking"), kamokuId);
        //リンクキーリストの生成
        this.setLinkingKyeListPop(kamokuId);
        //セルオブジェクトの生成(移動先オブジェクトは空で)
        this.setLinkingCellsEmptyTarget(kamokuId);
        //セル書き込み
        this.writeLinkingCellsSrc(true);
    };

    //連結セルデータの範囲外チェック。
    this.checkLinkingCellsRange = function (isAlert) {
        if (!this.target) {
            if (isAlert) {
                alert("セルが範囲外です。");
            }
            return false;
        }
        var idList = this.target.id.split("_");
        var WeekName = idList[1];
        for (var i = 0; i < this.linkingObj.length; i++) {
            for (var j = 0; j < this.linkingObj[i].list.length; j++) {
                var newId = this.calcCellId(this.linkingObj[i].list[j]);
                if (
                    this.isIrekae &&
                    !$("#" + newId)[0] &&
                    newId.indexOf("KOMA_99") !== -1
                ) {
                    var target = $(baseTdtag(newId))[0];
                } else {
                    var target = $("#" + newId)[0];
                }
                //移動先がない場合
                if (!target) {
                    if (isAlert) {
                        alert("セルが範囲外です。");
                    }
                    return false;
                }
                var idList2 = target.id.split("_");
                //移動先の日付が異なる場合
                if (idList2[1] != WeekName) {
                    if (isAlert) {
                        alert("セルが範囲外です。");
                    }
                    return false;
                }
            }
        }
        return true;
    };
    //全セルオブジェクトの重複チェック
    this.checkLinkingCells = function (isAlert, kamokuId) {
        for (var i = 0; i < this.cells.length; i++) {
            for (var j = 0; j < this.cells[i].length; j++) {
                if (
                    !this.cells[i][j].checkCellObjKouzaOverlapLinking(
                        this,
                        false,
                        kamokuId
                    )
                ) {
                    if (isAlert) {
                        alert("同じ時間に同じ科目は設定できません。");
                    }
                    return false;
                }
            }
        }
        return true;
    };
    //全セル
    //オブジェクトの重複チェック(コピー)
    this.checkLinkingCellsCopy = function (isAlert, kamokuId) {
        for (var i = 0; i < this.cells.length; i++) {
            for (var j = 0; j < this.cells[i].length; j++) {
                if (
                    !this.cells[i][j].checkCellObjKouzaOverlapLinkingCopy(
                        this,
                        false,
                        kamokuId
                    )
                ) {
                    if (isAlert) {
                        alert("同じ時間に同じ科目は設定できません。");
                    }
                    return false;
                }
            }
        }
        return true;
    };

    //連結データの解析済みオブジェクトの生成。リンクキーも生成される。
    this.setLinkingTextToObj = function (LinkingText, kamokuId) {
        this.linkingObj = new Array();
        this.linkingKeyList = new Array();
        this.linkingIdList = new Array();
        if (LinkingText == "") {
            return;
        }
        var LArray = LinkingText.split("/");
        var newLingkingObj = new Array();
        var LArrayList = new Array();
        var LArrayIds = new Array();
        for (var i = 0; i < LArray.length; i++) {
            var LArrayParts = LArray[i].split(":");
            if (kamokuId == null || kamokuId == LArrayParts[0]) {
                newLingkingObj.push({
                    id: LArrayParts[0],
                    list: LArrayParts[1].split(","),
                });
            }
            var flag = false;
            //重複してるキーを除外
            for (var j = 0; j < LArrayList.length; j++) {
                if (LArray[i] == LArrayList[j]) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                LArrayList.push(LArray[i]);
                LArrayIds.push(LArrayParts[0]);
            }
        }
        this.linkingObj = newLingkingObj;
        this.linkingKeyList = LArrayList;
        this.linkingIdList = LArrayIds;
    };

    //リンクキーリストの生成。ポップアップ用。
    this.setLinkingKyeListPop = function (kamokuId) {
        var LArrayList = new Array();
        for (var i = 0; i < this.linkingKeyList.length; i++) {
            if (this.linkingKeyList[i].indexOf(kamokuId) != -1) {
                LArrayList.push(this.linkingKeyList[i]);
            }
        }
        this.linkingKeyList = LArrayList;
        this.linkingIdList = new Array(kamokuId);
    };

    //全セルオブジェクトの生成
    this.setLinkingCells = function (kamokuId) {
        this.cells = new Array();
        for (var i = 0; i < this.linkingObj.length; i++) {
            this.cells[i] = new Array();
            for (var j = 0; j < this.linkingObj[i].list.length; j++) {
                var src = $("#" + this.linkingObj[i].list[j])[0];
                if (
                    this.isIrekae &&
                    !$("#" + this.calcCellId(this.linkingObj[i].list[j]))[0]
                ) {
                    var target = $(
                        baseTdtag(this.calcCellId(this.linkingObj[i].list[j]))
                    )[0];
                } else {
                    var target = $(
                        "#" + this.calcCellId(this.linkingObj[i].list[j])
                    )[0];
                }
                var Obj = new CellObj();
                Obj.isCopy = this.isCopy;
                Obj.setCellObj(src, target);
                Obj.calcLinkingText(this, kamokuId);
                this.cells[i].push(Obj);
            }
        }
    };

    //全セルオブジェクトの生成。ターゲットを空に。
    this.setLinkingCellsEmptyTarget = function (kamokuId) {
        this.cells = new Array();
        for (var i = 0; i < this.linkingObj.length; i++) {
            this.cells[i] = new Array();
            for (var j = 0; j < this.linkingObj[i].list.length; j++) {
                var src = $("#" + this.linkingObj[i].list[j])[0];
                var Obj = new CellObj();
                Obj.isCopy = this.isCopy;
                Obj.setCellObjEmptyTarget(src);
                Obj.calcLinkingText(this, kamokuId);
                this.cells[i].push(Obj);
            }
        }
    };

    //移動元セルのIDを基に、移動先セルのIDを演算する。
    //KOMA_日付_(校時+(先校時-元校時))_(行+(先行-元行))
    this.calcCellId = function (calcSrcId) {
        var srcIdArray = this.src.id.split("_");
        if (!this.target) {
            var targetIdArray = new Array("KOMA", "0", "0", "0"); //削除の場合
        } else {
            var targetIdArray = this.target.id.split("_");
        }
        var divIdArray = calcSrcId.split("_");
        var targetDivId = targetIdArray[0] + "_" + targetIdArray[1] + "_";
        targetDivId =
            targetDivId +
            (parseInt(divIdArray[2]) +
                (parseInt(targetIdArray[2]) - parseInt(srcIdArray[2]))) +
            "_";
        targetDivId =
            targetDivId +
            (parseInt(divIdArray[3]) +
                (parseInt(targetIdArray[3]) - parseInt(srcIdArray[3])));
        return targetDivId;
    };

    //連結データセル書き込み
    //移動先のセルを書き込んだ後すぐ移動元を書き込むと横移動の場合異常な表示となる。
    //移動先のセルのみを書き込み、移動先以外のセルで移動元のセルを後から書き込む。
    //また、カレントのセルオブジェクトのみ他のセルを操作しているため、カレントのセルを最初に処理する。
    this.writeLinkingCells = function (isPopup) {
        var writeTarget = new Array();
        for (var i = 0; i < this.cells.length; i++) {
            for (var j = 0; j < this.cells[i].length; j++) {
                if (this.cells[i][j].src.id == this.src.id) {
                    this.cells[i][j].calcCellObj_boxToBox_kamokuIdList(
                        this,
                        isPopup
                    );
                    this.cells[i][j].writeCellTarget();
                    writeTarget.push(this.cells[i][j].target.id);
                }
            }
        }
        //ターゲット側を更新
        for (var i = 0; i < this.cells.length; i++) {
            for (var j = 0; j < this.cells[i].length; j++) {
                if (this.cells[i][j].src.id != this.src.id) {
                    this.cells[i][j].calcCellObj_boxToBox_kamokuIdList(
                        this,
                        isPopup
                    );
                    this.cells[i][j].writeCellTarget();
                    writeTarget.push(this.cells[i][j].target.id);
                    //this.cells[i][j].target.box.innerHTML=this.cells[i][j].target.box.getAttribute('data-linking');
                }
            }
        }
        if (this.isCopy) {
            return;
        }
        //ターゲットにならないセルを更新
        for (var i = 0; i < this.cells.length; i++) {
            for (var j = 0; j < this.cells[i].length; j++) {
                var flag = false;
                for (var k = 0; k < writeTarget.length; k++) {
                    if (this.cells[i][j].src.id == writeTarget[k]) {
                        flag = true;
                    }
                }
                if (!flag) {
                    this.cells[i][j].writeCellSrc();
                    //this.cells[i][j].src.box.innerHTML=this.cells[i][j].src.box.getAttribute('data-linking');
                }
            }
        }
    };

    //連結データセル書き込み。移動元のみ
    this.writeLinkingCellsSrc = function (isPopup) {
        for (var i = 0; i < this.cells.length; i++) {
            for (var j = 0; j < this.cells[i].length; j++) {
                this.cells[i][j].calcCellObj_boxToBox_kamokuIdList(
                    this,
                    isPopup
                );
                this.cells[i][j].writeCellSrc();
            }
        }
    };

    //連結データの対象であるかどうかのチェック。連結データ対象ならTrueが返る。
    this.isLinkingCell = function (srcBox, cntNum) {
        var attrDataVal = srcBox.getAttribute("data-val");
        var attrDataValList =
            attrDataVal == "" ? new Array() : attrDataVal.split(":");
        var kamokuId = null;
        for (var i = 0; i < attrDataValList.length; i++) {
            if (i == cntNum) {
                kamokuId = attrDataValList[i];
                if (
                    srcBox.getAttribute("data-linking").indexOf(kamokuId) != -1
                ) {
                    return true;
                }
            }
        }
        return false;
    };

    //移動または追加後にメンバーの重複があるかどうかをチェックし、重複があれば「重」と表示する
    //移動または追加後に施設の収容講座数を超えていたら「施」と表示する
    this.checkMeiboAndFac = function (
        srcBoxId,
        srcBoxLinking,
        targetBoxId,
        targetBoxLinking,
        maxLine,
        isSyussekiFunc
    ) {
        // 仕様変更：基本時間割の時も受講生の重複チェックを行う
        // if ($('#SCH_DIV1').is(':checked')) {
        //     return;
        // }
        //日付リストの生成
        var dataMax = $("input[name=DATECNT_MAX]").val();
        var newDataList = new Array();
        if ($("#SCH_DIV1").is(":checked")) {
            var newDate = $("input[name=semesterEndDate]").val();
            newDataList.push(newDate);
        } else {
            newDataList = $("input[name=ALL_DATE]").val().split(",");
        }

        //移動元、移動先の対象校時を取得
        var kouziList = new Array();
        var boxList = new Array(
            new Array(srcBoxId, srcBoxLinking),
            new Array(targetBoxId, targetBoxLinking)
        );
        for (var i = 0; i < boxList.length; i++) {
            this.linkingObj = null;
            kouziList[i] = new Array();
            if (boxList[i][1] == "") {
                var idArray = boxList[i][0].split("_");
                kouziList[i].push(idArray[2]);
            } else {
                this.setLinkingTextToObj(boxList[i][1]);
                for (var j = 0; j < this.linkingObj.length; j++) {
                    for (var k = 0; k < this.linkingObj[j].list.length; k++) {
                        var id = this.linkingObj[j].list[k];
                        var idArray = id.split("_");
                        var flag = false;
                        for (var l = 0; l < kouziList[i].length; l++) {
                            if (kouziList[i][l] == idArray[2]) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            kouziList[i].push(idArray[2]);
                        }
                    }
                }
            }
        }

        //移動先と重なる移動元の対象校時を削除
        var newSrcKouziList = new Array();
        for (var i = 0; i < kouziList[0].length; i++) {
            var flag = false;
            for (var j = 0; j < kouziList[1].length; j++) {
                if (kouziList[0][i] == kouziList[1][j]) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                newSrcKouziList.push(kouziList[0][i]);
            }
        }
        kouziList[0] = newSrcKouziList;

        //各校時の講座IDリストを生成
        calcList = new Array();
        for (var i = 0; i < boxList.length; i++) {
            for (var j = 0; j < kouziList[i].length; j++) {
                calcListParts = new Array();
                calcListParts2 = new Array();
                var sep = "";
                var faccd = new Array();
                var usedFacility = {};
                for (var k = 1; k <= maxLine; k++) {
                    var idArray = boxList[i][0].split("_");
                    var id =
                        idArray[0] +
                        "_" +
                        idArray[1] +
                        "_" +
                        kouziList[i][j] +
                        "_" +
                        k;
                    var BoxDataVal = $("#" + id)[0].getAttribute("data-val");
                    var BoxDataValList =
                        BoxDataVal == "" ? new Array() : BoxDataVal.split(":");
                    var BoxDataSelectFacility = $("#" + id)[0].getAttribute(
                        "data-selectfacility"
                    );
                    var BoxDataSelectFacilityList =
                        BoxDataSelectFacility == ""
                            ? new Array()
                            : BoxDataSelectFacility.split(",");
                    var BoxDataTestVal = $("#" + id)[0].getAttribute(
                        "data-test"
                    );
                    var BoxDataTestValList =
                        BoxDataTestVal == ""
                            ? new Array()
                            : BoxDataTestVal.split(",");
                    for (var l = 0; l < BoxDataValList.length; l++) {
                        flag = false;
                        for (var m = 0; m < calcListParts.length; m++) {
                            if (BoxDataValList[l] == calcListParts[m]) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            calcListParts.push(BoxDataValList[l]);
                            if (BoxDataTestValList[l] != "0") {
                                calcListParts2.push(BoxDataValList[l]);
                            }
                        }

                        var renban = idArray[1];
                        var kouzi = kouziList[i][j];
                        if (
                            usedFacility[
                                renban +
                                    "_" +
                                    kouzi +
                                    "_" +
                                    BoxDataValList[l] +
                                    "_" +
                                    BoxDataSelectFacilityList[l]
                            ] == undefined
                        ) {
                            var dataSelectFacilityList = BoxDataSelectFacilityList[
                                l
                            ].split(":");
                            for (
                                m = 0;
                                m < dataSelectFacilityList.length;
                                m++
                            ) {
                                if (
                                    faccd.indexOf(dataSelectFacilityList[m]) ==
                                    -1
                                ) {
                                    faccd.push(dataSelectFacilityList[m]);
                                }
                                if (
                                    usedFacility[
                                        renban +
                                            "_" +
                                            kouzi +
                                            "_" +
                                            dataSelectFacilityList[m]
                                    ]
                                ) {
                                    usedFacility[
                                        renban +
                                            "_" +
                                            kouzi +
                                            "_" +
                                            dataSelectFacilityList[m]
                                    ] += 1;
                                } else {
                                    usedFacility[
                                        renban +
                                            "_" +
                                            kouzi +
                                            "_" +
                                            dataSelectFacilityList[m]
                                    ] = 1;
                                }
                                usedFacility[
                                    renban +
                                        "_" +
                                        kouzi +
                                        "_" +
                                        BoxDataValList[l] +
                                        "_" +
                                        BoxDataSelectFacilityList[l]
                                ] = true;
                            }
                        }
                    }
                }
                calcList.push({
                    targetDay: newDataList[idArray[1]],
                    id: boxList[i][0],
                    kouzi: kouziList[i][j],
                    list: calcListParts,
                    listHeaderNum: calcListParts2,
                    faccd: faccd,
                    usedFacility: usedFacility,
                });
            }
        }

        //AJAX通信。
        $.ajax({
            url: "knjb3042index.php",
            type: "POST",
            data: {
                AJAX_PARAM: JSON.stringify({
                    AJAX_MEIBO_FAC_PARAM: JSON.stringify(calcList),
                }),
                cmd: "getMeiboAndFacParam",
                YEAR_SEME: document.forms[0].YEAR_SEME.value,
            },
        }).done(function (data, textStatus, jqXHR) {
            //返り値を元に重複講座に「重」と表示。
            paramList = $.parseJSON(data);
            // 基本時間割以外は出席情報のチェック
            if (!$("#SCH_DIV1").is(":checked")) {
                for (var i = 0; i < paramList.length; i++) {
                    for (var j = 1; j <= maxLine; j++) {
                        var idArray = paramList[i]["id"].split("_");
                        var id =
                            idArray[0] +
                            "_" +
                            idArray[1] +
                            "_" +
                            paramList[i]["kouzi"] +
                            "_" +
                            j;
                        var BoxDataVal = $("#" + id)[0].getAttribute(
                            "data-val"
                        );
                        var BoxDataValList =
                            BoxDataVal == ""
                                ? new Array()
                                : BoxDataVal.split(":");
                        var BoxDataExec = $("#" + id)[0].getAttribute(
                            "data-exec"
                        );
                        var BoxDataExecList =
                            BoxDataExec == ""
                                ? new Array()
                                : BoxDataExec.split(",");
                        var flag = false;
                        for (var k = 0; k < BoxDataValList.length; k++) {
                            if (paramList[i]["list"][BoxDataValList[k]] == 1) {
                                if (
                                    BoxDataExecList[k] !== "MI_SYUKKETSU" &&
                                    isSyussekiFunc
                                ) {
                                    isSyussekiFunc();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            for (var i = 0; i < paramList.length; i++) {
                var dipSum = 0;
                for (var j = 1; j <= maxLine; j++) {
                    var idArray = paramList[i]["id"].split("_");
                    var id =
                        idArray[0] +
                        "_" +
                        idArray[1] +
                        "_" +
                        paramList[i]["kouzi"] +
                        "_" +
                        j;
                    var BoxDataVal = $("#" + id)[0].getAttribute("data-val");
                    var BoxDataValList =
                        BoxDataVal == "" ? new Array() : BoxDataVal.split(":");
                    var flag = false;
                    for (var k = 0; k < BoxDataValList.length; k++) {
                        if (paramList[i]["list"][BoxDataValList[k]] > 0) {
                            flag = true;
                        }
                        if (paramList[i]["list2"][BoxDataValList[k]] > 0) {
                            dipSum += parseInt(
                                paramList[i]["list2"][BoxDataValList[k]]
                            );
                        }
                    }
                    setInnerHtml($("#" + id)[0], flag ? "重" : "");
                }
                if (
                    $(
                        "#HEADER1_" +
                            paramList[i]["id"].split("_")[1] +
                            "_" +
                            paramList[i]["kouzi"]
                    )[0]
                ) {
                    $(
                        "#HEADER1_" +
                            paramList[i]["id"].split("_")[1] +
                            "_" +
                            paramList[i]["kouzi"]
                    )[0].innerHTML = dipSum;
                }
                if (
                    $(
                        "#HEADER2_" +
                            paramList[i]["id"].split("_")[1] +
                            "_" +
                            paramList[i]["kouzi"]
                    )[0]
                ) {
                    $(
                        "#HEADER2_" +
                            paramList[i]["id"].split("_")[1] +
                            "_" +
                            paramList[i]["kouzi"]
                    )[0].innerHTML =
                        paramList[i]["sougou"] == ""
                            ? 0
                            : paramList[i]["sougou"];
                }
            }
            //施設の収容講座数をチェック
            for (var i = 0; i < paramList.length; i++) {
                for (var j = 1; j <= maxLine; j++) {
                    var idArray = paramList[i]["id"].split("_");
                    var id =
                        idArray[0] +
                        "_" +
                        idArray[1] +
                        "_" +
                        paramList[i]["kouzi"] +
                        "_" +
                        j;
                    var BoxDataSelectFacility = $("#" + id)[0].getAttribute(
                        "data-selectfacility"
                    );
                    var BoxDataSelectFacilityList =
                        BoxDataSelectFacility == ""
                            ? new Array()
                            : BoxDataSelectFacility.split(",");
                    var renban = idArray[1];
                    var kouzi = paramList[i]["kouzi"];
                    var flag = false;
                    label: for (
                        var k = 0;
                        k < BoxDataSelectFacilityList.length;
                        k++
                    ) {
                        dataSelectFacilityList = BoxDataSelectFacilityList[
                            k
                        ].split(":");
                        for (
                            var l = 0;
                            l < dataSelectFacilityList.length;
                            l++
                        ) {
                            if (
                                paramList[i]["list"][
                                    renban +
                                        "_" +
                                        kouzi +
                                        "_" +
                                        dataSelectFacilityList[l]
                                ]
                            ) {
                                flag = true;
                                break label;
                            }
                        }
                    }
                    if (flag) addInnerHtml($("#" + id)[0], "施");
                }
            }
        });
    };

    //移動時にメンバーの出欠をチェックする。出欠があって重複があればtrueを返す
    //日付校時で移動/コピーボタンで使用する関数
    //TODO：kouziCheckList/calcList周りがおかしい
    this.meiboDupliCheck = function (
        startBoxDayIdx,
        startBoxKouzi,
        endBoxKouzi,
        startTargetBoxDayIdx,
        startTargetBoxKouzi,
        maxLine,
        IsTestNonMove,
        isSyussekiFunc,
        isSyussekiFalseFunc
    ) {
        startBoxDayIdx = parseInt(startBoxDayIdx);
        startBoxKouzi = parseInt(startBoxKouzi);
        endBoxKouzi = parseInt(endBoxKouzi);
        startTargetBoxDayIdx = parseInt(startTargetBoxDayIdx);
        startTargetBoxKouzi = parseInt(startTargetBoxKouzi);

        //日付リストの生成
        var newDataList = $("input[name=ALL_DATE]").val().split(",");

        //移動元、移動先の対象校時を取得
        var kouziCheckList = new Array();
        for (var i = startBoxKouzi; i <= endBoxKouzi; i++) {
            this.linkingObj = null;
            kouziCheckList[0] = {};
            for (var j = 1; j <= maxLine; j++) {
                var linkingText = $(
                    "#KOMA_" + startBoxDayIdx + "_" + i + "_" + j
                )[0].getAttribute("data-linking");
                if (linkingText == "") {
                    kouziCheckList[0][i] = true;
                } else {
                    this.setLinkingTextToObj(linkingText);
                    for (var k = 0; k < this.linkingObj.length; k++) {
                        for (
                            var l = 0;
                            l < this.linkingObj[k].list.length;
                            l++
                        ) {
                            var id = this.linkingObj[k].list[l];
                            var idArray = id.split("_");
                            kouziCheckList[0][idArray[2]] = true;
                        }
                    }
                }
            }
        }

        for (
            var i = startTargetBoxKouzi;
            i <= startTargetBoxKouzi + (endBoxKouzi - startBoxKouzi);
            i++
        ) {
            this.linkingObj = null;
            kouziCheckList[1] = {};
            for (var j = 1; j <= maxLine; j++) {
                var linkingText = $(
                    "#KOMA_" + startTargetBoxDayIdx + "_" + i + "_" + j
                )[0].getAttribute("data-linking");
                if (linkingText == "") {
                    kouziCheckList[1][i] = true;
                } else {
                    this.setLinkingTextToObj(linkingText);
                    for (var k = 0; k < this.linkingObj.length; k++) {
                        for (
                            var l = 0;
                            l < this.linkingObj[k].list.length;
                            l++
                        ) {
                            var id = this.linkingObj[k].list[l];
                            var idArray = id.split("_");
                            kouziCheckList[1][idArray[2]] = true;
                        }
                    }
                }
            }
        }

        //用途は不明だが、念のため？
        for (var kouzi in kouziCheckList[0]) {
            kouziCheckList[1][
                startTargetBoxKouzi + (kouzi - startBoxKouzi)
            ] = true;
            kouziCheckList[1][
                startTargetBoxKouzi +
                    (endBoxKouzi - startBoxKouzi) +
                    (kouzi - endBoxKouzi)
            ] = true;
        }

        kouziList = new Array(new Array(), new Array());
        for (var i = 0; i < kouziCheckList.length; i++) {
            for (var kouzi in kouziCheckList[i]) {
                kouziList[i].push(kouzi);
            }
        }

        srcCalcDataList = {};
        for (var i = 0; i < kouziList[0].length; i++) {
            var dayIdx = startBoxDayIdx;
            for (var j = 1; j <= maxLine; j++) {
                var id =
                    "KOMA_" + startBoxDayIdx + "_" + kouziList[0][i] + "_" + j;
                var BoxDataVal = $("#" + id)[0].getAttribute("data-val");
                var BoxDataValList =
                    BoxDataVal == "" ? new Array() : BoxDataVal.split(":");
                var BoxDataTestVal = $("#" + id)[0].getAttribute("data-test");
                var BoxDataTestValList =
                    BoxDataTestVal == ""
                        ? new Array()
                        : BoxDataTestVal.split(":");
                for (var k = 0; k < BoxDataValList.length; k++) {
                    if (IsTestNonMove && BoxDataTestValList[k] != "0") {
                        continue;
                    }
                    if (
                        !srcCalcDataList[
                            parseInt(kouziList[0][i]) +
                                (startTargetBoxKouzi - startBoxKouzi)
                        ]
                    ) {
                        srcCalcDataList[
                            parseInt(kouziList[0][i]) +
                                (startTargetBoxKouzi - startBoxKouzi)
                        ] = new Array();
                    }
                    srcCalcDataList[
                        parseInt(kouziList[0][i]) +
                            (startTargetBoxKouzi - startBoxKouzi)
                    ].push(BoxDataValList[k]);
                }
            }
        }
        //移動後が必要なだけなので、元はクリアする。
        kouziList[0] = new Array();

        //各校時の講座IDリストを生成
        calcList = new Array();
        for (var i = 0; i < 2; i++) {
            for (var j = 0; j < kouziList[i].length; j++) {
                calcListParts = new Array();
                var dayIdx = i == 0 ? startBoxDayIdx : startTargetBoxDayIdx;
                for (var k = 1; k <= maxLine; k++) {
                    var id = "KOMA_" + dayIdx + "_" + kouziList[i][j] + "_" + k;
                    var BoxDataVal = $("#" + id)[0].getAttribute("data-val");
                    var BoxDataValList =
                        BoxDataVal == "" ? new Array() : BoxDataVal.split(":");
                    for (var l = 0; l < BoxDataValList.length; l++) {
                        flag = false;
                        for (var m = 0; m < calcListParts.length; m++) {
                            if (BoxDataValList[l] == calcListParts[m]) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            calcListParts.push(BoxDataValList[l]);
                        }
                    }
                }
                if (srcCalcDataList[kouziList[i][j]]) {
                    for (
                        var k = 0;
                        k < srcCalcDataList[kouziList[i][j]].length;
                        k++
                    ) {
                        flag = false;
                        for (var l = 0; l < calcListParts.length; l++) {
                            if (
                                srcCalcDataList[kouziList[i][j]][k] ==
                                calcListParts[l]
                            ) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            calcListParts.push(
                                srcCalcDataList[kouziList[i][j]][k]
                            );
                        }
                    }
                }
                calcList.push({
                    targetDay: newDataList[dayIdx],
                    id: "KOMA_" + dayIdx + "_" + kouziList[i][j] + "_1",
                    kouzi: kouziList[i][j],
                    list: calcListParts,
                });
            }
        }

        //AJAX通信。
        $.ajax({
            url: "knjb3042index.php",
            type: "POST",
            data: {
                AJAX_PARAM: JSON.stringify({
                    AJAX_MEIBO_PARAM: JSON.stringify(calcList),
                }),
                cmd: "getMeiboParam",
                YEAR_SEME: document.forms[0].YEAR_SEME.value,
            },
        }).done(function (data, textStatus, jqXHR) {
            paramList = $.parseJSON(data);
            for (var i = 0; i < paramList.length; i++) {
                for (var j = 1; j <= maxLine; j++) {
                    var idArray = paramList[i]["id"].split("_");
                    var id =
                        "KOMA_" +
                        idArray[1] +
                        "_" +
                        paramList[i]["kouzi"] +
                        "_" +
                        j;
                    var BoxDataVal = $("#" + id)[0].getAttribute("data-val");
                    var BoxDataValList =
                        BoxDataVal == "" ? new Array() : BoxDataVal.split(":");
                    var BoxDataExec = $("#" + id)[0].getAttribute("data-exec");
                    var BoxDataExecList =
                        BoxDataExec == ""
                            ? new Array()
                            : BoxDataExec.split(",");
                    for (var k = 0; k < BoxDataValList.length; k++) {
                        if (paramList[i]["list"][BoxDataValList[k]] == 1) {
                            if (
                                BoxDataExecList[k] !== "MI_SYUKKETSU" &&
                                isSyussekiFunc
                            ) {
                                isSyussekiFunc();
                                return;
                            }
                        }
                    }
                }
            }
            if (isSyussekiFalseFunc) {
                isSyussekiFalseFunc();
            }
        });
    };

    //TODO:不具合あるかも↑の関数と一緒に見直し
    this.makeKouziLsit = function (srcBox) {
        var boxList = new Array(
            new Array(srcBox.id, srcBox.getAttribute("data-linking"))
        );
        var kouziList = new Array();
        for (var i = 0; i < boxList.length; i++) {
            this.linkingObj = null;
            kouziList[i] = new Array();
            if (boxList[i][1] == "") {
                var idArray = boxList[i][0].split("_");
                kouziList[i].push(idArray[2]);
            } else {
                this.setLinkingTextToObj(boxList[i][1]);
                for (var j = 0; j < this.linkingObj.length; j++) {
                    for (var k = 0; k < this.linkingObj[j].list.length; k++) {
                        var id = this.linkingObj[j].list[k];
                        var idArray = id.split("_");
                        var flag = false;
                        for (var l = 0; l < kouziList[i].length; l++) {
                            if (kouziList[i][l] == idArray[2]) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            kouziList[i].push(idArray[2]);
                        }
                    }
                }
            }
        }
        return kouziList;
    };

    //TODO:不具合あるかも↑の関数と一緒に見直し
    this.makeCalcList = function (srcBox, calcList, kouziList, maxLine) {
        //日付リストの生成
        var newDataList = $("input[name=ALL_DATE]").val().split(",");

        var boxList = new Array(
            new Array(srcBox.id, srcBox.getAttribute("data-linking"))
        );
        //各校時の講座IDリストを生成
        calcList = new Array();
        for (var i = 0; i < boxList.length; i++) {
            for (var j = 0; j < kouziList[i].length; j++) {
                var idx = -1;
                for (var k = 0; k < calcList.length; k++) {
                    if (
                        calcList[k].id == boxList[i][0] &&
                        calcList[k].kouzi == kouziList[i][j]
                    ) {
                        idx = k;
                        break;
                    }
                }
                if (idx == -1) {
                    calcListParts = new Array();
                    calcListParts2 = new Array();
                } else {
                    calcListParts = calcList[idx].list;
                    calcListParts2 = calcList[idx].listHeaderNum;
                }

                var sep = "";
                var faccd = new Array();
                var usedFacility = {};
                for (var k = 1; k <= maxLine; k++) {
                    var idArray = boxList[i][0].split("_");
                    var id =
                        idArray[0] +
                        "_" +
                        idArray[1] +
                        "_" +
                        kouziList[i][j] +
                        "_" +
                        k;
                    var BoxDataVal = $("#" + id)[0].getAttribute("data-val");
                    var BoxDataValList =
                        BoxDataVal == "" ? new Array() : BoxDataVal.split(":");
                    var BoxDataSelectFacility = $("#" + id)[0].getAttribute(
                        "data-selectfacility"
                    );
                    var BoxDataSelectFacilityList =
                        BoxDataSelectFacility == ""
                            ? new Array()
                            : BoxDataSelectFacility.split(",");
                    var BoxDataTestVal = $("#" + id)[0].getAttribute(
                        "data-test"
                    );
                    var BoxDataTestValList =
                        BoxDataTestVal == ""
                            ? new Array()
                            : BoxDataTestVal.split(",");
                    for (var l = 0; l < BoxDataValList.length; l++) {
                        flag = false;
                        for (var m = 0; m < calcListParts.length; m++) {
                            if (BoxDataValList[l] == calcListParts[m]) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            calcListParts.push(BoxDataValList[l]);
                            if (BoxDataTestValList[l] != "0") {
                                calcListParts2.push(BoxDataValList[l]);
                            }
                        }

                        var renban = idArray[1];
                        var kouzi = kouziList[i][j];
                        if (
                            usedFacility[
                                renban +
                                    "_" +
                                    kouzi +
                                    "_" +
                                    BoxDataValList[l] +
                                    "_" +
                                    BoxDataSelectFacilityList[l]
                            ] == undefined
                        ) {
                            var dataSelectFacilityList = BoxDataSelectFacilityList[
                                l
                            ].split(":");
                            for (
                                m = 0;
                                m < dataSelectFacilityList.length;
                                m++
                            ) {
                                if (
                                    faccd.indexOf(dataSelectFacilityList[m]) ==
                                    -1
                                ) {
                                    faccd.push(dataSelectFacilityList[m]);
                                }
                                if (
                                    usedFacility[
                                        renban +
                                            "_" +
                                            kouzi +
                                            "_" +
                                            dataSelectFacilityList[m]
                                    ]
                                ) {
                                    usedFacility[
                                        renban +
                                            "_" +
                                            kouzi +
                                            "_" +
                                            dataSelectFacilityList[m]
                                    ] += 1;
                                } else {
                                    usedFacility[
                                        renban +
                                            "_" +
                                            kouzi +
                                            "_" +
                                            dataSelectFacilityList[m]
                                    ] = 1;
                                }
                                usedFacility[
                                    renban +
                                        "_" +
                                        kouzi +
                                        "_" +
                                        BoxDataValList[l] +
                                        "_" +
                                        BoxDataSelectFacilityList[l]
                                ] = true;
                            }
                        }
                    }
                }
                if (idx == -1) {
                    calcList.push({
                        targetDay: newDataList[idArray[1]],
                        id: boxList[i][0],
                        kouzi: kouziList[i][j],
                        list: calcListParts,
                        listHeaderNum: calcListParts2,
                        faccd: faccd,
                        usedFacility: usedFacility,
                    });
                }
            }
        }

        return calcList;
    };

    //TODO:不具合あるかも↑の関数と一緒に見直し
    this.checkMeiboAndFacUseCalcList = function (calcList, maxLine) {
        // 仕様変更：基本時間割の時も受講生の重複チェックを行う
        // if ($('#SCH_DIV1').is(':checked')) {
        //     return;
        // }

        //AJAX通信。
        $.ajax({
            url: "knjb3042index.php",
            type: "POST",
            data: {
                AJAX_PARAM: JSON.stringify({
                    AJAX_MEIBO_FAC_PARAM: JSON.stringify(calcList),
                }),
                cmd: "getMeiboAndFacParam",
                YEAR_SEME: document.forms[0].YEAR_SEME.value,
            },
        }).done(function (data, textStatus, jqXHR) {
            //返り値を元に重複講座に「重」と表示。
            paramList = $.parseJSON(data);
            for (var i = 0; i < paramList.length; i++) {
                var dipSum = 0;
                for (var j = 1; j <= maxLine; j++) {
                    var idArray = paramList[i]["id"].split("_");
                    var id =
                        idArray[0] +
                        "_" +
                        idArray[1] +
                        "_" +
                        paramList[i]["kouzi"] +
                        "_" +
                        j;
                    var BoxDataVal = $("#" + id)[0].getAttribute("data-val");
                    var BoxDataValList =
                        BoxDataVal == "" ? new Array() : BoxDataVal.split(":");
                    var flag = false;
                    for (var k = 0; k < BoxDataValList.length; k++) {
                        if (paramList[i]["list"][BoxDataValList[k]] > 0) {
                            flag = true;
                        }
                        if (paramList[i]["list2"][BoxDataValList[k]] > 0) {
                            dipSum += parseInt(
                                paramList[i]["list2"][BoxDataValList[k]]
                            );
                        }
                    }

                    setInnerHtml($("#" + id)[0], flag ? "重" : "");
                }
                if (
                    $(
                        "#HEADER1_" +
                            paramList[i]["id"].split("_")[1] +
                            "_" +
                            paramList[i]["kouzi"]
                    )[0]
                ) {
                    $(
                        "#HEADER1_" +
                            paramList[i]["id"].split("_")[1] +
                            "_" +
                            paramList[i]["kouzi"]
                    )[0].innerHTML = dipSum;
                }
                if (
                    $(
                        "#HEADER2_" +
                            paramList[i]["id"].split("_")[1] +
                            "_" +
                            paramList[i]["kouzi"]
                    )[0]
                ) {
                    $(
                        "#HEADER2_" +
                            paramList[i]["id"].split("_")[1] +
                            "_" +
                            paramList[i]["kouzi"]
                    )[0].innerHTML =
                        paramList[i]["sougou"] == ""
                            ? 0
                            : paramList[i]["sougou"];
                }
            }
            //施設の収容講座数をチェック
            for (var i = 0; i < paramList.length; i++) {
                for (var j = 1; j <= maxLine; j++) {
                    var idArray = paramList[i]["id"].split("_");
                    var id =
                        idArray[0] +
                        "_" +
                        idArray[1] +
                        "_" +
                        paramList[i]["kouzi"] +
                        "_" +
                        j;
                    var BoxDataSelectFacility = $("#" + id)[0].getAttribute(
                        "data-selectfacility"
                    );
                    var BoxDataSelectFacilityList =
                        BoxDataSelectFacility == ""
                            ? new Array()
                            : BoxDataSelectFacility.split(",");
                    var renban = idArray[1];
                    var kouzi = paramList[i]["kouzi"];
                    var flag = false;
                    label: for (
                        var k = 0;
                        k < BoxDataSelectFacilityList.length;
                        k++
                    ) {
                        dataSelectFacilityList = BoxDataSelectFacilityList[
                            k
                        ].split(":");
                        for (
                            var l = 0;
                            l < dataSelectFacilityList.length;
                            l++
                        ) {
                            if (
                                paramList[i]["list"][
                                    renban +
                                        "_" +
                                        kouzi +
                                        "_" +
                                        dataSelectFacilityList[l]
                                ]
                            ) {
                                flag = true;
                                break label;
                            }
                        }
                    }
                    if (flag) addInnerHtml($("#" + id)[0], "施");
                }
            }
        });
    };
}
//重複表示
function setInnerHtml(box, addText) {
    var text = box.innerHTML;
    var textList = text.split("<br>");
    if (text.indexOf("件のデータ") === -1) {
        textList[2] = addText;
    } else {
        textList[1] = addText;
    }
    box.innerHTML = textList.join("<br>");
}

//施設表示
function addInnerHtml(box, addText) {
    var text = box.innerHTML;
    var textList = text.split("<br>");
    if (text.indexOf("件のデータ") === -1) {
        idx = 2;
    } else {
        idx = 1;
    }
    if (textList[idx]) {
        textList[idx] += textList[idx].indexOf(addText) == -1 ? addText : "";
    } else {
        textList[idx] = addText;
    }
    box.innerHTML = textList.join("<br>");
}

function MoveBlockObj() {
    this.movedData; //移動済みの講座のリスト
    this.error; //0:初期、1:正常、2:重複エラー、3:範囲エラー
    this.rireki; //移動履歴、ロールバックで使用
    this.isCopy = false; //コピー処理時true
    this.isIrekae = false; //入替処理時true
    this.targetKouzaColAll = {}; //移動先の縦軸全ての講座(移動先の縦軸に元講座があればエラーを返す為)。KOMA_0_3:講座配列

    this.moveOneblockEventCancel = false;
    this.deleteOneObjEventCancel = false;

    //セルの縦全データの別日付(校時)へのコピー。
    this.execMoveBlockFullCopy = function (
        srcBox,
        cntNum,
        targetBox,
        isAlert,
        isTestKousa
    ) {
        this.checkCells(srcBox, cntNum, targetBox);
        if (this.error == 3) {
            if (isAlert) {
                alert("セルが範囲外です。");
            }
            return;
        }
        this.rireki = new Array();
        this.error = 0;
        this.isCopy = true;

        $(window).on("writeCellTargetEvent", this.writeCellTargetCopy);
        var srcIdArray = srcBox.id.split("_");
        var targetIdArray = targetBox.id.split("_");
        var moveDataVal = {};
        if ($("#copyMoveBox_showListToList").is(":checked")) {
            var selectedValList = getMoveCopyBoxTarget();
            var parentObj = this;
            selectedValList.forEach(function (lineCnt) {
                var srcBox = $(
                    "#KOMA_" +
                        srcIdArray[1] +
                        "_" +
                        srcIdArray[2] +
                        "_" +
                        lineCnt
                )[0];
                var targetBox = $(
                    "#KOMA_" +
                        targetIdArray[1] +
                        "_" +
                        targetIdArray[2] +
                        "_" +
                        lineCnt
                )[0];
                if (isTestKousa) {
                    targetBox = $(
                        "#" +
                            this.calcTestKousaTargetId(
                                targetIdArray[1],
                                targetIdArray[2]
                            )
                    )[0];
                }
                var srcDataVal = srcBox.getAttribute("data-val");
                var srcDataValList =
                    srcDataVal == "" ? new Array() : srcDataVal.split(":");
                for (var i = 0; i < srcDataValList.length; i++) {
                    if (!moveDataVal[srcDataValList[i]]) {
                        parentObj.execMoveBlock(
                            srcBox,
                            i + "",
                            targetBox,
                            isAlert
                        );
                        moveDataVal[srcDataValList[i]] = true;
                    }
                }
            });
        } else {
            var startIdx = isTestKousa ? 2 : 1;
            for (
                var lineCnt = startIdx;
                lineCnt <= document.forms[0].MAX_LINE.value;
                lineCnt++
            ) {
                var srcBox = $(
                    "#KOMA_" +
                        srcIdArray[1] +
                        "_" +
                        srcIdArray[2] +
                        "_" +
                        lineCnt
                )[0];
                var targetBox = $(
                    "#KOMA_" +
                        targetIdArray[1] +
                        "_" +
                        targetIdArray[2] +
                        "_" +
                        lineCnt
                )[0];
                if (isTestKousa) {
                    targetBox = $(
                        "#" +
                            this.calcTestKousaTargetId(
                                targetIdArray[1],
                                targetIdArray[2]
                            )
                    )[0];
                }
                var srcDataVal = srcBox.getAttribute("data-val");
                var srcDataValList =
                    srcDataVal == "" ? new Array() : srcDataVal.split(":");
                for (var i = 0; i < srcDataValList.length; i++) {
                    if (!moveDataVal[srcDataValList[i]]) {
                        this.execMoveBlock(srcBox, i + "", targetBox, isAlert);
                        moveDataVal[srcDataValList[i]] = true;
                    }
                }
            }
        }
        $(window).off("writeCellTargetEvent");
    };

    //セルの縦全データの別日付(校時)への移動。
    this.execMoveBlockFull = function (
        srcBoxId,
        cntNum,
        targetBoxId,
        isAlert,
        isTestKousa
    ) {
        var srcIdArray = srcBoxId.split("_");
        var targetIdArray = targetBoxId.split("_");
        var moveDataList = {};
        if ($("#copyMoveBox_showListToList").is(":checked")) {
            var selectedValList = getMoveCopyBoxTarget();
            var parentObj = this;
            selectedValList.forEach(function (i) {
                var newSrcId =
                    "KOMA_" + srcIdArray[1] + "_" + srcIdArray[2] + "_" + i;
                if (parentObj.isIrekae && !$("#" + newSrcId)[0]) {
                    var srcBox = $(baseTdtag(newSrcId))[0];
                } else {
                    var srcBox = $("#" + newSrcId)[0];
                }

                var newTargetId =
                    "KOMA_" +
                    targetIdArray[1] +
                    "_" +
                    targetIdArray[2] +
                    "_" +
                    i;
                if (parentObj.isIrekae && !$("#" + newTargetId)[0]) {
                    var targetBox = $(baseTdtag(newTargetId))[0];
                } else {
                    if (isTestKousa) {
                        newTargetId = this.calcTestKousaTargetId(
                            targetIdArray[1],
                            targetIdArray[2]
                        );
                    }
                    var targetBox = $("#" + newTargetId)[0];
                }
                while (true) {
                    var srcDataVal = srcBox.getAttribute("data-val");
                    var srcDataValList =
                        srcDataVal == "" ? new Array() : srcDataVal.split(":");

                    var flag = false;
                    for (var j = 0; j < srcDataValList.length; j++) {
                        if (!moveDataList[srcDataValList[j]]) {
                            parentObj.execMoveBlock(
                                srcBox,
                                j + "",
                                targetBox,
                                true
                            );
                            flag = true;
                            moveDataList[srcDataValList[j]] = true;
                            break;
                        }
                    }
                    if (!flag) {
                        break;
                    }
                }
            });
        } else {
            var startIdx = isTestKousa ? 2 : 1;
            for (i = startIdx; i <= document.forms[0].MAX_LINE.value; i++) {
                var newSrcId =
                    "KOMA_" + srcIdArray[1] + "_" + srcIdArray[2] + "_" + i;
                if (this.isIrekae && !$("#" + newSrcId)[0]) {
                    var srcBox = $(baseTdtag(newSrcId))[0];
                } else {
                    var srcBox = $("#" + newSrcId)[0];
                }

                var newTargetId =
                    "KOMA_" +
                    targetIdArray[1] +
                    "_" +
                    targetIdArray[2] +
                    "_" +
                    i;
                if (this.isIrekae && !$("#" + newTargetId)[0]) {
                    var targetBox = $(baseTdtag(newTargetId))[0];
                } else {
                    if (isTestKousa) {
                        newTargetId = this.calcTestKousaTargetId(
                            targetIdArray[1],
                            targetIdArray[2]
                        );
                    }
                    var targetBox = $("#" + newTargetId)[0];
                }
                while (true) {
                    var srcDataVal = srcBox.getAttribute("data-val");
                    var srcDataValList =
                        srcDataVal == "" ? new Array() : srcDataVal.split(":");

                    var flag = false;
                    for (var j = 0; j < srcDataValList.length; j++) {
                        if (!moveDataList[srcDataValList[j]]) {
                            this.execMoveBlock(srcBox, j + "", targetBox, true);
                            flag = true;
                            moveDataList[srcDataValList[j]] = true;
                            break;
                        }
                    }
                    if (!flag) {
                        break;
                    }
                }
            }
        }
    };

    this.calcTestKousaTargetId = function (idArray1, idArray2) {
        for (var i = 2; i <= document.forms[0].MAX_LINE.value; i++) {
            if (
                $(
                    "#KOMA_" + idArray1 + "_" + idArray2 + "_" + i
                )[0].getAttribute("data-val") == ""
            ) {
                return "KOMA_" + idArray1 + "_" + idArray2 + "_" + i;
            }
        }
        return false;
    };

    //コピー時の書き込み処理 function(e = event, cellObj = cellObj, celldata = cellObj.src OR cellObj.target)
    //ここでは、celldata = cellObj.targetがくる
    this.writeCellTargetCopy = function (e, cellObj, celldata) {
        celldata.box.setAttribute("data-val", celldata.dataValList.join(":"));
        celldata.box.setAttribute("data-text", celldata.dataTextList.join(","));
        celldata.box.setAttribute("data-test", celldata.dataTestList.join(","));
        var list = new Array();
        var def = new Array();

        var cellIdArray = celldata.box.id.split("_");

        var sdataTime = new Date(
            Date.parse(
                $("input[name=ALL_DATE]").val().split(",")[
                    parseInt(cellIdArray[1])
                ]
            )
        );
        var year = sdataTime.getFullYear();
        var month = sdataTime.getMonth() + 1;
        var day = sdataTime.getDate();
        var defDate = year + "-" + month + "-" + day;

        for (var i = 0; i < celldata.dataExecList.length; i++) {
            list.push("MI_SYUKKETSU");
            //TODO:cellObj.dataDefList+_Addでいいんじゃない？
            def.push(
                defDate +
                    "_" +
                    cellIdArray[2] +
                    "_" +
                    celldata.dataValList[i] +
                    "_" +
                    celldata.dataTestList[i] +
                    "_" +
                    cellIdArray[3] +
                    "_" +
                    celldata.dataFacilityList[i] +
                    "_" +
                    celldata.dataTestFacilityList[i] +
                    "_" +
                    celldata.dataCountLessonList[i] +
                    "_Add"
            );
        }
        celldata.dataExecList = list;
        celldata.dataDefList = def;
        celldata.box.setAttribute("data-exec", celldata.dataExecList.join(","));
        celldata.box.setAttribute("data-def", celldata.dataDefList.join(","));
        celldata.box.setAttribute("data-linking", celldata.linking);
        celldata.box.setAttribute(
            "data-selectfacility",
            celldata.dataFacilityList.join(",")
        );
        celldata.box.setAttribute(
            "data-selecttestfacility",
            celldata.dataTestFacilityList.join(",")
        );
        celldata.box.setAttribute(
            "data-count-lesson",
            celldata.dataCountLessonList.join(",")
        );
        celldata.box.setAttribute("data-dirty", "1");
        if (celldata.dataValList.length == 0) {
            celldata.box.innerHTML = "";
        } else if (celldata.dataValList.length == 1) {
            celldata.box.innerHTML = celldata.dataTextList[0];
        } else {
            celldata.box.innerHTML = celldata.dataValList.length + "件のデータ";
        }
        cellObj.writeClass(celldata);
        cellObj.writeCellTargetEventCancel = true;
    };

    //セルの移動。コピー用
    this.execMoveBlockCopy = function (srcBox, cntNum, targetBox, isAlert) {
        //writeCellTargetEventというイベントの時、this.writeCellTargetCopyを動かすよう定義
        $(window).on("writeCellTargetEvent", this.writeCellTargetCopy);
        this.isCopy = true;
        this.execMoveBlock(srcBox, cntNum, targetBox, isAlert);
        //writeCellTargetEventのクリア
        $(window).off("writeCellTargetEvent");
    };

    //セルの移動。縦列に同じ科目があったら同時に移動する。
    this.execMoveBlock = function (srcBox, cntNum, targetBox, isAlert) {
        this.checkCells(srcBox, cntNum, targetBox);

        if (this.error == 2) {
            if (isAlert) {
                alert("同じ時間に同じ講座は設定できません。");
            }
            return;
        }
        if (this.error == 3) {
            if (isAlert) {
                alert("セルが範囲外です。");
            }
            return;
        }
        this.rireki = new Array();
        this.error = 0;
        var dragIdArray = srcBox.id.split("_");
        var week = dragIdArray[1];
        var period = dragIdArray[2];
        var line = dragIdArray[3];
        var srcDataVal = srcBox.getAttribute("data-val");
        var srcDataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");
        var LObj = new LinkingCellObj();

        LObj.src = srcBox;
        LObj.target = targetBox;
        var srcIdArray = LObj.src.id.split("_");
        var targetIdArray = LObj.target.id.split("_");

        //下移動の場合
        //下のものから順番に動かす。
        if (parseInt(targetIdArray[3]) - parseInt(srcIdArray[3]) > 0) {
            for (
                var lineCnt = document.forms[0].MAX_LINE.value;
                lineCnt >= 1;
                lineCnt--
            ) {
                var id = "KOMA_" + week + "_" + period + "_" + lineCnt;
                var cnt = 0;
                this.movedData = new Array();
                if (this.isIrekae && !$("#" + id)[0]) {
                    var srcBoxCell = $(baseTdtag(id))[0];
                } else {
                    var srcBoxCell = $("#" + id)[0];
                }
                if (this.isIrekae && !$("#" + LObj.calcCellId(id))[0]) {
                    var targetBoxCell = $(baseTdtag(LObj.calcCellId(id)))[0];
                } else {
                    var targetBoxCell = $("#" + LObj.calcCellId(id))[0];
                }
                while (
                    this.moving(
                        srcDataValList,
                        srcBoxCell,
                        targetBoxCell,
                        cntNum
                    )
                ) {
                    if (cnt == 100) {
                        alert("err");
                        break;
                    }
                    cnt++;
                }
                if (this.error == 2) {
                    return;
                }
            }

            //上移動の場合
            //上のものから順番に動かす
        } else {
            for (
                var lineCnt = 1;
                lineCnt <= document.forms[0].MAX_LINE.value;
                lineCnt++
            ) {
                var id = "KOMA_" + week + "_" + period + "_" + lineCnt;
                var cnt = 0;
                this.movedData = new Array();
                if (this.isIrekae && !$("#" + id)[0]) {
                    var srcBoxCell = $(baseTdtag(id))[0];
                } else {
                    var srcBoxCell = $("#" + id)[0];
                }
                if (this.isIrekae && !$("#" + LObj.calcCellId(id))[0]) {
                    var targetBoxCell = $(baseTdtag(LObj.calcCellId(id)))[0];
                } else {
                    var targetBoxCell = $("#" + LObj.calcCellId(id))[0];
                }
                while (
                    this.moving(
                        srcDataValList,
                        srcBoxCell,
                        targetBoxCell,
                        cntNum
                    )
                ) {
                    if (cnt == 100) {
                        alert("err");
                        break;
                    }
                    cnt++;
                }
                if (this.error == 2) {
                    return;
                }
            }
        }
    };

    //セルの移動。縦列に同じ科目があったら同時に移動する。入れ替え用
    this.execMoveBlockIrekae = function (srcBox, cntNum, targetBox, isAlert) {
        this.error = 0;
        var dragIdArray = srcBox.id.split("_");
        var week = dragIdArray[1];
        var period = dragIdArray[2];
        var line = dragIdArray[3];
        var srcDataVal = srcBox.getAttribute("data-val");
        var srcDataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");
        var LObj = new LinkingCellObj();
        LObj.src = srcBox;
        LObj.target = targetBox;
        var srcIdArray = LObj.src.id.split("_");
        var targetIdArray = LObj.target.id.split("_");
        //配列のコピー。
        //rireki2 = this.rirekiだと、参照コピーになっちゃう
        var rireki2 = $.extend([], this.rireki);
        this.rireki = new Array();

        //下移動の場合
        //下のものから順番に動かす。
        if (parseInt(targetIdArray[3]) - parseInt(srcIdArray[3]) > 0) {
            for (
                var lineCnt = document.forms[0].MAX_LINE.value;
                lineCnt >= 1;
                lineCnt--
            ) {
                var id = "KOMA_" + week + "_" + period + "_" + lineCnt;
                this.movedData = new Array();
                for (var i = 0; i < rireki2.length; i++) {
                    if (rireki2[i][0].id == id) {
                        var moveDataVal = rireki2[i][2].getAttribute(
                            "data-val"
                        );
                        var moveDataValList =
                            moveDataVal == ""
                                ? new Array()
                                : moveDataVal.split(":");
                        var cnt = 0;
                        //KOMA_9999_999_999の中身を実際の移動先に入れる
                        while (
                            this.moving(
                                moveDataValList,
                                rireki2[i][2],
                                $("#" + LObj.calcCellId(id))[0],
                                ""
                            )
                        ) {
                            if (cnt == 100) {
                                alert("err");
                                break;
                            }
                            cnt++;
                        }
                        if (this.error == 2 || this.error == 3) {
                            this.rireki = $.extend([], rireki2);
                            return;
                        }
                    }
                }
            }

            //上移動の場合
            //上のものから順番に動かす
        } else {
            for (
                var lineCnt = 1;
                lineCnt <= document.forms[0].MAX_LINE.value;
                lineCnt++
            ) {
                var id = "KOMA_" + week + "_" + period + "_" + lineCnt;
                this.movedData = new Array();
                for (var i = 0; i < rireki2.length; i++) {
                    if (rireki2[i][0].id == id) {
                        var moveDataVal = rireki2[i][2].getAttribute(
                            "data-val"
                        );
                        var moveDataValList =
                            moveDataVal == ""
                                ? new Array()
                                : moveDataVal.split(":");
                        var cnt = 0;
                        while (
                            this.moving(
                                moveDataValList,
                                rireki2[i][2],
                                $("#" + LObj.calcCellId(id))[0],
                                ""
                            )
                        ) {
                            if (cnt == 100) {
                                alert("err");
                                break;
                            }
                            cnt++;
                        }
                        if (this.error == 2 || this.error == 3) {
                            this.rireki = $.extend([], rireki2);
                            return;
                        }
                    }
                }
            }
        }
        for (i = 0; i < rireki2.length; i++) {
            for (j = 0; j < this.rireki.length; j++) {
                if (rireki2[i][2].id == this.rireki[j][0].id) {
                    this.rireki[j][0] = rireki2[i][0];
                }
            }
        }
    };

    //セルの移動。縦列に同じ科目があったら同時に移動する。教師移動
    this.execMoveBlockKyousi = function (srcBox, cntNum, targetBox, isAlert) {
        this.rireki = new Array();
        this.error = 0;
        var dragIdArray = srcBox.id.split("_");
        var week = dragIdArray[1];
        var period = dragIdArray[2];
        var line = dragIdArray[3];
        var srcDataVal = srcBox.getAttribute("data-val");
        var srcDataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");
        var LObj = new LinkingCellObj();
        LObj.src = srcBox;
        LObj.target = targetBox;
        var srcIdArray = LObj.src.id.split("_");
        var targetIdArray = LObj.target.id.split("_");

        //下移動の場合
        //下のものから順番に動かす。
        if (parseInt(targetIdArray[3]) - parseInt(srcIdArray[3]) > 0) {
            for (
                var lineCnt = document.forms[0].MAX_LINE.value;
                lineCnt >= 1;
                lineCnt--
            ) {
                var id = "KOMA_" + week + "_" + period + "_" + lineCnt;
                var cnt = 0;
                this.movedData = new Array();
                var srcBoxCell = $("#" + id)[0];
                if (id == srcBox.id) {
                    //摘まんだセルは斜め移動(行が変わる)
                    var calcId = LObj.calcCellId(id);
                } else {
                    var tempIdArray = LObj.calcCellId(id).split("_");
                    //摘まんでいないセルは横移動(ポイントは、行がlineCntになってる事)
                    var calcId =
                        tempIdArray[0] +
                        "_" +
                        tempIdArray[1] +
                        "_" +
                        tempIdArray[2] +
                        "_" +
                        lineCnt;
                }
                var targetBoxCell = $("#" + calcId)[0];
                while (
                    this.moving(
                        srcDataValList,
                        srcBoxCell,
                        targetBoxCell,
                        cntNum
                    )
                ) {
                    if (cnt == 100) {
                        alert("err");
                        break;
                    }
                    cnt++;
                }
                if (this.error == 2 || this.error == 3) {
                    return;
                }
            }

            //上移動の場合
            //上のものから順番に動かす
        } else {
            for (
                var lineCnt = 1;
                lineCnt <= document.forms[0].MAX_LINE.value;
                lineCnt++
            ) {
                var id = "KOMA_" + week + "_" + period + "_" + lineCnt;
                var cnt = 0;
                this.movedData = new Array();
                var srcBoxCell = $("#" + id)[0];
                if (id == srcBox.id) {
                    var calcId = LObj.calcCellId(id);
                } else {
                    var tempIdArray = LObj.calcCellId(id).split("_");
                    var calcId =
                        tempIdArray[0] +
                        "_" +
                        tempIdArray[1] +
                        "_" +
                        tempIdArray[2] +
                        "_" +
                        lineCnt;
                }
                var targetBoxCell = $("#" + calcId)[0];
                while (
                    this.moving(
                        srcDataValList,
                        srcBoxCell,
                        targetBoxCell,
                        cntNum
                    )
                ) {
                    if (cnt == 100) {
                        alert("err");
                        break;
                    }
                    cnt++;
                }
                if (this.error == 2 || this.error == 3) {
                    return;
                }
            }
        }
    };

    //範囲チェック。重複チェック。
    this.checkCells = function (srcBox, cntNum, targetBox) {
        var dragIdArray = srcBox.id.split("_");
        var week = dragIdArray[1];
        var period = dragIdArray[2];
        var line = dragIdArray[3];
        var srcDataVal = srcBox.getAttribute("data-val");
        var srcDataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");
        var LObj = new LinkingCellObj();
        LObj.src = srcBox;
        LObj.target = targetBox;
        this.error = 0;
        this.targetKouzaColAll = {};

        for (
            var lineCnt = 1;
            lineCnt <= document.forms[0].MAX_LINE.value;
            lineCnt++
        ) {
            var id = "KOMA_" + week + "_" + period + "_" + lineCnt;
            var cnt = 0;
            this.movedData = new Array();
            if (this.isIrekae && !$("#" + id)[0]) {
                var srcBoxCell = $(baseTdtag(id))[0];
            } else {
                var srcBoxCell = $("#" + id)[0];
                //移動元セルが空の場合はチェックしない
                if (!srcBoxCell.getAttribute("data-val")) {
                    continue;
                }
            }
            if (this.isIrekae && !$("#" + LObj.calcCellId(id))[0]) {
                var targetBoxCell = $(baseTdtag(LObj.calcCellId(id)))[0];
            } else {
                var targetBoxCell = $("#" + LObj.calcCellId(id))[0];
            }

            while (
                this.moving(
                    srcDataValList,
                    srcBoxCell,
                    targetBoxCell,
                    cntNum,
                    true
                )
            ) {
                if (cnt == 100) {
                    alert("err");
                    break;
                }
                cnt++;
            }
        }
    };

    //範囲チェック。重複チェック。教師用
    this.checkCellsKyousi = function (srcBox, cntNum, targetBox) {
        var dragIdArray = srcBox.id.split("_");
        var week = dragIdArray[1];
        var period = dragIdArray[2];
        var line = dragIdArray[3];
        var srcDataVal = srcBox.getAttribute("data-val");
        var srcDataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");
        var LObj = new LinkingCellObj();
        LObj.src = srcBox;
        LObj.target = targetBox;
        this.error = 0;
        this.targetKouzaColAll = {};

        for (
            var lineCnt = 1;
            lineCnt <= document.forms[0].MAX_LINE.value;
            lineCnt++
        ) {
            var id = "KOMA_" + week + "_" + period + "_" + lineCnt;
            var cnt = 0;
            this.movedData = new Array();
            var srcBoxCell = $("#" + id)[0];

            if (id == srcBox.id) {
                //摘まんだセルは斜め移動(行が変わる)
                var calcId = LObj.calcCellId(id);
            } else {
                var tempIdArray = LObj.calcCellId(id).split("_");
                //摘まんでいないセルは横移動(ポイントは、行がlineCntになってる事)
                var calcId =
                    tempIdArray[0] +
                    "_" +
                    tempIdArray[1] +
                    "_" +
                    tempIdArray[2] +
                    "_" +
                    lineCnt;
            }
            var targetBoxCell = $("#" + calcId)[0];

            while (
                this.moving(
                    srcDataValList,
                    srcBoxCell,
                    targetBoxCell,
                    cntNum,
                    true
                )
            ) {
                if (cnt == 100) {
                    alert("err");
                    break;
                }
                cnt++;
            }
        }
    };

    //移動。
    //srcのdata-valを取得してる点に注意。
    //基本的にポップアップ用の個別移動を繰り返す形で処理を作成している。
    //移動のたびにsrcのdata-valが減算されるので無限ループにはならない。
    //が、万一のため上記関数で100回ループしたらエラーになるようにしている。
    //srcDataValList：つまんでいるBox
    //srcBox：処理するBox(縦のループ)
    //targetBox：移動先のBox
    this.moving = function (
        srcDataValList,
        srcBox,
        targetBox,
        cntNum,
        isCheck
    ) {
        var moveDataVal = srcBox.getAttribute("data-val");
        var moveDataValList =
            moveDataVal == "" ? new Array() : moveDataVal.split(":");
        //ポップアップの場合、特定の1科目。
        if (cntNum != "" && cntNum != "all") {
            srcDataValList = new Array(srcDataValList[cntNum]);
        }
        for (var i = 0; i < srcDataValList.length; i++) {
            for (var j = 0; j < moveDataValList.length; j++) {
                //移動対象(同一講座がある)であるかどうか。
                if (srcDataValList[i] == moveDataValList[j]) {
                    var flag = false;
                    //一度移動したIDはもう移動しない。
                    //moveOneblockの中で処理済なので２重に処理してしまうのを防ぐ
                    for (var k = 0; k < this.movedData.length; k++) {
                        if (this.movedData[k] == moveDataValList[j]) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        //移動処理
                        var Obj = this.moveOneblock(
                            srcBox,
                            j + "",
                            targetBox,
                            isCheck
                        );
                        if (isCheck) {
                            if (
                                !this.checkKouzaExist(
                                    Obj,
                                    srcDataValList,
                                    isCheck,
                                    srcBox,
                                    j + ""
                                )
                            ) {
                                return false;
                            }
                        }
                        if (!isCheck) {
                            if (this.error == 2) {
                                //重複エラーの場合履歴をもとに元に戻す。
                                this.rollback();
                                return false;
                            } else {
                                this.rireki.push(
                                    new Array(
                                        srcBox,
                                        j + "",
                                        targetBox,
                                        moveDataValList[j]
                                    )
                                );
                            }
                        }
                        this.movedData.push(moveDataValList[j]);
                        return true;
                    }
                }
            }
        }
        return false;
    };

    //移動先の縦軸に同じ講座があったらエラー
    this.checkKouzaExist = function (
        Obj,
        srcDataValList,
        isCheckOnly,
        srcBox,
        cntNum
    ) {
        var idArray = {};
        if (Obj instanceof CellObj) {
            idArray[Obj.target.id] = Obj;
        } else if (Obj instanceof LinkingCellObj) {
            for (var cellCnt1 = 0; cellCnt1 < Obj.cells.length; cellCnt1++) {
                for (
                    var cellCnt2 = 0;
                    cellCnt2 < Obj.cells[cellCnt1].length;
                    cellCnt2++
                ) {
                    idArray[Obj.cells[cellCnt1][cellCnt2].target.id] =
                        Obj.cells[cellCnt1][cellCnt2];
                }
            }
        } else {
            return true;
        }

        var srcDataValListObj = {};
        for (var i = 0; i < srcDataValList.length; i++) {
            srcDataValListObj[srcDataValList[i]] = true;
        }

        for (var idKey in idArray) {
            if (idKey.indexOf("KOMA_99") !== -1) {
                return true;
            }
            var idKeyArray = idKey.split("_");
            var setColAll = "KOMA_" + idKeyArray[1] + "_" + idKeyArray[2];
            if (!this.targetKouzaColAll[setColAll]) {
                var noListIdArray = {};
                var LObj = new LinkingCellObj();
                if (LObj.isLinkingCell(srcBox, cntNum)) {
                    //cntNumの値から科目IDを検索する。
                    var attrDataVal = srcBox.getAttribute("data-val");
                    var attrDataValList =
                        attrDataVal == ""
                            ? new Array()
                            : attrDataVal.split(":");
                    var kamokuId = null;
                    for (var i = 0; i < attrDataValList.length; i++) {
                        if (i == cntNum) {
                            kamokuId = attrDataValList[i];
                            break;
                        }
                    }

                    //リンクオブジェクトの生成。noListIdArrayにlinkingに含まれる自分自身のID(line除く)をセット
                    LObj.setLinkingTextToObj(
                        srcBox.getAttribute("data-linking"),
                        kamokuId
                    );
                    for (i = 0; i < LObj.linkingObj.length; i++) {
                        if (LObj.linkingObj[i].id == kamokuId) {
                            for (
                                j = 0;
                                j < LObj.linkingObj[i].list.length;
                                j++
                            ) {
                                var idListTemp = LObj.linkingObj[i].list[
                                    j
                                ].split("_");
                                noListIdArray[
                                    idListTemp[0] +
                                        "_" +
                                        idListTemp[1] +
                                        "_" +
                                        idListTemp[2]
                                ] = true;
                            }
                        }
                    }
                }

                this.targetKouzaColAll[setColAll] = new Array();

                for (
                    var targetLineCnt = 1;
                    targetLineCnt <= document.forms[0].MAX_LINE.value;
                    targetLineCnt++
                ) {
                    var tCellId = setColAll + "_" + targetLineCnt;
                    var tCellVal = $("#" + tCellId)[0].getAttribute("data-val");
                    var tCellValList =
                        tCellVal == "" ? new Array() : tCellVal.split(":");
                    for (
                        var tCellListCnt = 0;
                        tCellListCnt < tCellValList.length;
                        tCellListCnt++
                    ) {
                        //自分自身はスキップ
                        if (tCellId == idKey) {
                            continue;
                        }
                        //自分自身のlinkingもスキップ
                        if (noListIdArray[setColAll]) {
                            continue;
                        }
                        var setFlg = false;
                        for (
                            var setCnt = 0;
                            setCnt < this.targetKouzaColAll[setColAll].length;
                            setCnt++
                        ) {
                            //移動先にある、移動元の講座は除く
                            if (
                                this.targetKouzaColAll[setColAll][setCnt] ==
                                tCellValList[tCellListCnt]
                            ) {
                                setFlg = true;
                                break;
                            }
                        }
                        if (!setFlg) {
                            this.targetKouzaColAll[setColAll].push(
                                tCellValList[tCellListCnt]
                            );
                        }
                    }
                }
            }
        }

        for (var idKey in idArray) {
            var idKeyArray = idKey.split("_");
            var setColAll = "KOMA_" + idKeyArray[1] + "_" + idKeyArray[2];
            var idDataValList = idArray[idKey].src.dataValList;
            for (var valCnt = 0; valCnt < idDataValList.length; valCnt++) {
                for (
                    var tKouzaCnt = 0;
                    tKouzaCnt <= this.targetKouzaColAll[setColAll].length;
                    tKouzaCnt++
                ) {
                    if (
                        idDataValList[valCnt] ==
                            this.targetKouzaColAll[setColAll][tKouzaCnt] &&
                        srcDataValListObj[idDataValList[valCnt]]
                    ) {
                        this.error = 2;
                        return false;
                    }
                }
            }
        }

        return true;
    };

    //元に戻す
    this.rollback = function () {
        var calcList = new Array();
        var LObj = new LinkingCellObj();
        for (var i = this.rireki.length - 1; i >= 0; i--) {
            var rirekiDataVal = this.rireki[i][2].getAttribute("data-val");
            var rirekiDataValList =
                rirekiDataVal == "" ? new Array() : rirekiDataVal.split(":");
            for (j = 0; j < rirekiDataValList.length; j++) {
                if (rirekiDataValList[j] == this.rireki[i][3]) {
                    if (this.isCopy) {
                        var kouziList = LObj.makeKouziLsit(this.rireki[i][2]);
                        this.deleteOneObj(this.rireki[i][2], j + "");
                        calcList = LObj.makeCalcList(
                            this.rireki[i][2],
                            calcList,
                            kouziList,
                            document.forms[0].MAX_LINE.value
                        );
                    } else {
                        this.moveOneblock(
                            this.rireki[i][2],
                            j + "",
                            this.rireki[i][0],
                            false
                        );
                        var kouziList = LObj.makeKouziLsit(this.rireki[i][2]);
                        calcList = LObj.makeCalcList(
                            this.rireki[i][2],
                            calcList,
                            kouziList,
                            document.forms[0].MAX_LINE.value
                        );
                        var kouziList = LObj.makeKouziLsit(this.rireki[i][0]);
                        calcList = LObj.makeCalcList(
                            this.rireki[i][0],
                            calcList,
                            kouziList,
                            document.forms[0].MAX_LINE.value
                        );
                    }
                    break;
                }
            }
        }
        LObj.checkMeiboAndFacUseCalcList(
            calcList,
            document.forms[0].MAX_LINE.value
        );
    };

    //移動処理
    //ポップアップで講座をつまんだ場合は、cntNumに値が入っている
    //rollback等では、targetの講座番目が入る
    this.moveOneblock = function (srcBox, cntNum, targetBox, isCheck) {
        this.moveOneblockEventCancel = false;
        $(window).trigger("moveOneblockEvent", [
            this,
            srcBox,
            cntNum,
            targetBox,
            isCheck,
        ]);
        if (this.moveOneblockEventCancel) {
            return;
        }
        if (srcBox != targetBox) {
            //ポップアップじゃない場合
            if (cntNum == "" || cntNum == "all") {
                var attrLinking = srcBox.getAttribute("data-linking");
                if (attrLinking == "") {
                    var Obj = new CellObj();
                    Obj.isCopy = this.isCopy;
                    Obj.isIrekae = this.isIrekae;
                    var ret = Obj.execCellObj_boxTobox(
                        srcBox,
                        targetBox,
                        isCheck
                    );
                } else {
                    var Obj = new LinkingCellObj();
                    Obj.isCopy = this.isCopy;
                    Obj.isIrekae = this.isIrekae;
                    var ret = Obj.execLinkingCellObj(
                        srcBox,
                        targetBox,
                        isCheck
                    );
                }
            } else {
                var Obj = new LinkingCellObj();
                if (Obj.isLinkingCell(srcBox, cntNum)) {
                    Obj.isCopy = this.isCopy;
                    Obj.isIrekae = this.isIrekae;
                    var ret = Obj.execLinkingCellObjPop(
                        srcBox,
                        cntNum,
                        targetBox,
                        isCheck
                    );
                } else {
                    Obj = new CellObj();
                    Obj.isCopy = this.isCopy;
                    Obj.isIrekae = this.isIrekae;
                    var ret = Obj.execCellObj_popTobox(
                        srcBox,
                        cntNum,
                        targetBox,
                        isCheck
                    );
                }
            }
            if (this.error < ret) {
                this.error = ret;
            }
        }
        return Obj;
    };

    //削除処理
    this.deleteMoveObj = function (srcBox, cntNum) {
        var dragIdArray = srcBox.id.split("_");
        var week = dragIdArray[1];
        var period = dragIdArray[2];
        var line = dragIdArray[3];
        var srcDataVal = srcBox.getAttribute("data-val");
        var srcDataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");
        //ポップアップの場合、特定の1科目。
        if (cntNum != "" && cntNum != "all") {
            srcDataValList = new Array(srcDataValList[cntNum]);
        }
        var flag = 0;

        for (
            var lineCnt = 1;
            lineCnt <= document.forms[0].MAX_LINE.value;
            lineCnt++
        ) {
            var id = "KOMA_" + week + "_" + period + "_" + lineCnt;

            var cnt = 0;
            var targetBox = $("#" + id)[0];
            while (true) {
                targetDataVal = targetBox.getAttribute("data-val");
                if (targetDataVal == "") {
                    break;
                }
                var targetDataValList =
                    targetDataVal == ""
                        ? new Array()
                        : targetDataVal.split(":");
                var flag = false;
                for (var i = 0; i < srcDataValList.length; i++) {
                    for (var j = 0; j < targetDataValList.length; j++) {
                        if (srcDataValList[i] == targetDataValList[j]) {
                            this.deleteOneObj(targetBox, j + "");
                            //this.deleteOneObjの中で実際にtargetBox.getAttribute('data-val')を書き換えているので再度取得し直す
                            targetDataVal = targetBox.getAttribute("data-val");
                            targetDataValList =
                                targetDataVal == ""
                                    ? new Array()
                                    : targetDataVal.split(":");
                            flag = true;
                            break;
                        }
                    }
                }
                if (!flag) {
                    break;
                }
                cnt++;
                if (cnt == 100) {
                    alert("error");
                    break;
                }
            }
        }
    };

    //削除処理(1ブロック)
    this.deleteOneObj = function (srcBox, cntNum) {
        this.deleteOneObjEventCancel = false;
        $(window).trigger("deleteOneObjEvent", [this, srcBox, cntNum]);
        if (this.deleteOneObjEventCancel) {
            return;
        }
        if (cntNum == "" || cntNum == "all") {
            var attrLinking = srcBox.getAttribute("data-linking");
            if (attrLinking == "") {
                var Obj = new CellObj();
                Obj.deleteCellObj_boxTobox(srcBox);
            } else {
                var Obj = new LinkingCellObj();
                Obj.deleteLinkingCellObj(srcBox);
            }
        } else {
            var Obj = new LinkingCellObj();
            if (Obj.isLinkingCell(srcBox, cntNum)) {
                Obj.deleteLinkingCellObjPop(srcBox, cntNum);
            } else {
                Obj = new CellObj();
                Obj.deleteCellObj_popTobox(srcBox, cntNum);
            }
        }
    };

    //出欠情報があって日付をまたいだらエラー
    //radioNum == 2：コピーは未出欠とするので何もしない
    this.isSyussekiOverDay = function (srcBox, cntNum, targetBox, radioNum) {
        if ($("#SCH_DIV1").is(":checked")) {
            return false;
        }
        var srcBoxIdArray = srcBox.id.split("_");
        var targetBoxIdArray = targetBox.id.split("_");
        if (srcBoxIdArray[1] == targetBoxIdArray[1]) {
            return false;
        }
        if (radioNum == 1 || radioNum == 3) {
            return this.isSyussekiOverDayParts(srcBox, cntNum);
        }
        if (radioNum == 4) {
            var ret = this.isSyussekiOverDayParts(srcBox, cntNum);
            if (!ret) {
                return this.isSyussekiOverDayParts(targetBox, "");
            } else {
                return true;
            }
        }
    };

    //出欠情報があって日付をまたいだらエラー
    //日付が不一致な場合のみここを通る
    this.isSyussekiOverDayParts = function (box, cntNum) {
        var boxIdArray = box.id.split("_");

        var boxDataVal = box.getAttribute("data-val");
        var boxDataValList =
            boxDataVal == "" ? new Array() : boxDataVal.split(",");
        var checkIds = {};
        for (var i = 0; i < boxDataValList.length; i++) {
            if (cntNum == "" || cntNum == "all") {
                checkIds[boxDataValList[i]] = true;
            } else if (i == cntNum) {
                checkIds[boxDataValList[i]] = true;
            }
        }
        for (
            var lineCnt = 1;
            lineCnt <= document.forms[0].MAX_LINE.value;
            lineCnt++
        ) {
            var id =
                "KOMA_" + boxIdArray[1] + "_" + boxIdArray[2] + "_" + lineCnt;
            var boxDataVal = $("#" + id)[0].getAttribute("data-val");
            var boxDataValList =
                boxDataVal == "" ? new Array() : boxDataVal.split(",");
            var boxDataExec = $("#" + id)[0].getAttribute("data-exec");
            var boxDataExecList =
                boxDataExec == "" ? new Array() : boxDataExec.split(",");
            for (var i = 0; i < boxDataValList.length; i++) {
                if (checkIds[boxDataValList[i]]) {
                    if (boxDataExecList[i] !== "MI_SYUKKETSU") {
                        return true;
                    }
                }
            }
        }
        return false;
    };
}

//入れ替え機能を実現させるためのオブジェクト
function IrekaeBlockObj() {
    this.srcMObj;
    this.targetMObj;

    //入替の前処理と後処理
    this.execIrekae = function (srcBox, cntNum, targetBox) {
        $(window).on(
            "setCellObjEvent",
            function (e, cellObj, srcBox, targetBox) {
                if (targetBox.id.indexOf("KOMA_99") !== -1) {
                    var obj = $("#" + targetBox.id)[0];
                    if (!obj) {
                        $("#templary_cell_data").append(targetBox);
                    }
                }
            }
        );
        $("body").append(
            $('<tr id="templary_cell_data" style="display:none"></tr>')
        );
        this.execIrekaeMain(srcBox, cntNum, targetBox);
        $("#templary_cell_data").remove();
        $(window).off("setCellObjEvent");
    };

    //入替メイン処理
    this.execIrekaeMain = function (srcBox, cntNum, targetBox) {
        var checkMObj = new MoveBlockObj();
        var srcIdArray = srcBox.id.split("_");
        var targetIdArray = targetBox.id.split("_");

        checkMObj.checkCells(
            srcBox,
            cntNum,
            $(
                "#" +
                    srcIdArray[0] +
                    "_" +
                    targetIdArray[1] +
                    "_" +
                    targetIdArray[2] +
                    "_" +
                    srcIdArray[3]
            )[0]
        );
        if (checkMObj.error == 2) {
            alert("同じ時間に同じ科目は設定できません。");
            return;
        }
        if (checkMObj.error == 3) {
            alert("セルが範囲外です。");
            return;
        }

        checkMObj.checkCells(
            targetBox,
            "",
            $(
                "#" +
                    targetIdArray[0] +
                    "_" +
                    srcIdArray[1] +
                    "_" +
                    srcIdArray[2] +
                    "_" +
                    targetIdArray[3]
            )[0]
        );
        if (checkMObj.error == 2) {
            alert("同じ時間に同じ科目は設定できません。");
            return;
        }
        if (checkMObj.error == 3) {
            alert("セルが範囲外です。");
            return;
        }

        this.srcMObj = new MoveBlockObj();
        this.srcMObj.isIrekae = true;
        this.srcMObj.execMoveBlock(
            srcBox,
            cntNum,
            $(baseTdtag("KOMA_9999_999_999"))[0],
            true
        );
        if (this.srcMObj.error == 2 || this.srcMObj.error == 3) {
            return;
        }
        this.targetMObj = new MoveBlockObj();
        this.targetMObj.execMoveBlock(
            targetBox,
            "",
            $(
                "#" +
                    targetIdArray[0] +
                    "_" +
                    srcIdArray[1] +
                    "_" +
                    srcIdArray[2] +
                    "_" +
                    targetIdArray[3]
            )[0],
            true
        );
        if (this.targetMObj.error == 2 || this.targetMObj.error == 3) {
            this.srcMObj.rollback();
            return;
        }
        this.srcMObj.execMoveBlockIrekae(
            srcBox,
            "",
            $(
                "#" +
                    srcIdArray[0] +
                    "_" +
                    targetIdArray[1] +
                    "_" +
                    targetIdArray[2] +
                    "_" +
                    srcIdArray[3]
            )[0],
            true
        );
        if (this.srcMObj.error == 2 || this.srcMObj.error == 3) {
            this.rollback();
        }
    };

    //ロールバック
    this.rollback = function () {
        this.targetMObj.rollback();
        this.srcMObj.rollback();
    };

    //職員の移動
    this.execIrekaeKyousi = function (srcBox, cntNum, targetBox, isAlert) {
        srcIdArray = srcBox.id.split("_");
        targetIdArray = targetBox.id.split("_");
        if (
            srcIdArray[1] != targetIdArray[1] ||
            srcIdArray[2] != targetIdArray[2]
        ) {
            var MObj = new MoveBlockObj();

            MObj.checkCellsKyousi(srcBox, cntNum, targetBox);

            if (MObj.error == 2) {
                if (isAlert) {
                    alert("同じ時間に同じ講座は設定できません。");
                }
                return;
            }
            if (MObj.error == 3) {
                if (isAlert) {
                    alert("セルが範囲外です。");
                }
                return;
            }
        }

        var srcDataVal = srcBox.getAttribute("data-val");
        var srcDataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");

        srcIdArray = srcBox.id.split("_");
        targetIdArray = targetBox.id.split("_");
        if (
            srcBox.id ==
            srcIdArray[0] +
                "_" +
                srcIdArray[1] +
                "_" +
                srcIdArray[2] +
                "_" +
                targetIdArray[3]
        ) {
            alert("この処理では横移動することはできません。");
            return;
        }

        var srcMObj = new MoveBlockObj();
        srcMObj.execMoveBlockKyousi(srcBox, cntNum, targetBox, true);
        if (srcMObj.error == 3) {
            srcMObj.rollback();
        }
        return srcMObj;
    };

    //職員のコピー
    this.execIrekaeKyousiCopy = function (srcBox, cntNum, targetBox, isAlert) {
        srcIdArray = srcBox.id.split("_");
        targetIdArray = targetBox.id.split("_");

        var srcDataVal = srcBox.getAttribute("data-val");
        var srcDataValList =
            srcDataVal == "" ? new Array() : srcDataVal.split(":");
        var targetDataVal = targetBox.getAttribute("data-val");
        var targetDataValList =
            targetDataVal == "" ? new Array() : targetDataVal.split(":");

        //縦移動ではない場合
        if (
            srcIdArray[1] != targetIdArray[1] ||
            srcIdArray[2] != targetIdArray[2]
        ) {
            var LObj = new LinkingCellObj();
            LObj.src = srcBox;
            LObj.target = targetBox;
            LObj.setLinkingTextToObj(srcBox.getAttribute("data-linking"));
            var targetDayPeriArray = {}; //移動先の[日付＋校時][講座]
            for (var i = 0; i < LObj.linkingObj.length; i++) {
                for (var j = 0; j < LObj.linkingObj[i].list.length; j++) {
                    var targetId = LObj.calcCellId(LObj.linkingObj[i].list[j]);
                    var targetIdAry = targetId.split("_");
                    if (
                        !targetDayPeriArray[
                            targetIdAry[1] + "_" + targetIdAry[2]
                        ]
                    ) {
                        targetDayPeriArray[
                            targetIdAry[1] + "_" + targetIdAry[2]
                        ] = {};
                    }
                    //移動先の[日付＋校時][講座]を配列にする。
                    targetDayPeriArray[targetIdAry[1] + "_" + targetIdAry[2]][
                        LObj.linkingObj[i].id
                    ] = true;
                }
            }

            for (var i = 0; i < LObj.linkingObj.length; i++) {
                for (var j = 0; j < LObj.linkingObj[i].list.length; j++) {
                    var srcIdAry = LObj.linkingObj[i].list[j].split("_");
                    if (targetDayPeriArray[srcIdAry[1] + "_" + srcIdAry[2]]) {
                        //移動先の[日付＋校時][講座]と移動元の[日付＋校時][講座]が被ったらエラー
                        if (
                            targetDayPeriArray[srcIdAry[1] + "_" + srcIdAry[2]][
                                LObj.linkingObj[i].id
                            ]
                        ) {
                            if (isAlert) {
                                alert("同じ時間に同じ講座は設定できません。");
                            }
                            return;
                        }
                    }
                }
            }

            var MObj = new MoveBlockObj();

            MObj.checkCellsKyousi(srcBox, cntNum, targetBox);
            if (MObj.error == 2) {
                if (isAlert) {
                    alert("同じ時間に同じ講座は設定できません。");
                }
                return;
            }
            if (MObj.error == 3) {
                if (isAlert) {
                    alert("セルが範囲外です。");
                }
                return;
            }
        } else if (srcBox.id != targetBox.id) {
            //縦移動の場合で
            //同じ講座の場所にドロップしたらエラー
            for (var i = 0; i < srcDataValList.length; i++) {
                if (cntNum != "" && cntNum != "all" && cntNum != i) {
                    continue;
                }
                for (var j = 0; j < targetDataValList.length; j++) {
                    if (srcDataValList[i] == targetDataValList[j]) {
                        if (isAlert) {
                            alert("同じ時間に同じ講座は設定できません。");
                        }
                        return;
                    }
                }
            }
        }

        if (
            srcBox.id ==
            srcIdArray[0] +
                "_" +
                srcIdArray[1] +
                "_" +
                srcIdArray[2] +
                "_" +
                targetIdArray[3]
        ) {
            alert("この処理では横移動することはできません。");
            return;
        }

        var srcMObj = new MoveBlockObj();
        //一致している時(縦移動)は、デフォルトの処理が動く
        if (srcIdArray[2] != targetIdArray[2]) {
            //writeCellTargetEventというイベントの時、this.writeCellTargetCopyを動かすよう定義
            //デフォルトとの違いは、未出欠にするのと、最後に_Addを付けている
            $(window).on("writeCellTargetEvent", srcMObj.writeCellTargetCopy);
        }
        srcMObj.isCopy = true;
        srcMObj.execMoveBlockKyousi(srcBox, cntNum, targetBox, true);
        if (srcMObj.error == 3) {
            srcMObj.rollback();
        }
        //writeCellTargetEventのクリア
        $(window).off("writeCellTargetEvent");
        return srcMObj;
    };
}

function BlockMoveBlockObj() {
    //日付校時で移動/コピーの列移動
    this.execMoveBlockFullMove = function (
        srcDateCd,
        targetDateCd,
        srcStartNum,
        srcEndNum,
        targetStarNum,
        testCd,
        IsTestNonMove,
        isTestKousa
    ) {
        srcStartNum = parseInt(srcStartNum);
        srcEndNum = parseInt(srcEndNum);
        targetStarNum = parseInt(targetStarNum);
        $(window).on(
            "setCellObjEvent",
            function (e, cellObj, srcBox, targetBox) {
                if (targetBox.id.indexOf("KOMA_99") !== -1) {
                    var obj = $("#" + targetBox.id)[0];
                    if (!obj) {
                        $("#templary_cell_data").append(targetBox);
                    }
                }

                if (testCd) {
                    var srcDataTest = srcBox.getAttribute("data-test");
                    var srcDataTestList =
                        srcDataTest == ""
                            ? new Array()
                            : srcDataTest.split(",");
                    var testList = new Array();
                    for (var i = 0; i < srcDataTestList.length; i++) {
                        testList.push(testCd);
                    }
                    srcBox.setAttribute("data-test", testList.join(","));
                }
            }
        );
        $(window).on(
            "moveOneblockEvent",
            function (e, MObjE, srcBoxE, cntNumE, targetBoxE, isCheckE) {
                if (IsTestNonMove) {
                    var srcDataTest = srcBoxE.getAttribute("data-test");
                    var srcDataTestList =
                        srcDataTest == ""
                            ? new Array()
                            : srcDataTest.split(",");
                    if (srcDataTestList[cntNumE] != "0") {
                        MObjE.moveOneblockEventCancel = true;
                    }
                }
            }
        );

        $("body").append(
            $('<tr id="templary_cell_data" style="display:none"></tr>')
        );
        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var srcId = "KOMA_" + srcDateCd + "_" + i + "_1";
            var targetId =
                "KOMA_9999_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var MObj = new MoveBlockObj();
            MObj.isIrekae = true;
            MObj.execMoveBlockFull(srcId, "", targetId, false, isTestKousa);
        }
        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var srcId =
                "KOMA_9999_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var targetId =
                "KOMA_" +
                targetDateCd +
                "_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var MObj = new MoveBlockObj();
            MObj.isIrekae = true;
            MObj.execMoveBlockFull(srcId, "", targetId, false, isTestKousa);
        }
        $("#templary_cell_data").remove();
        $(window).off("setCellObjEvent");
        $(window).off("moveOneblockEvent");

        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var srcBoxId = "KOMA_" + srcDateCd + "_" + i + "_1";
            var targetBoxId =
                "KOMA_" +
                targetDateCd +
                "_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var LObj = new LinkingCellObj();
            LObj.checkMeiboAndFac(
                srcBoxId,
                $("#" + srcBoxId)[0].getAttribute("data-linking"),
                targetBoxId,
                $("#" + targetBoxId)[0].getAttribute("data-linking"),
                document.forms[0].MAX_LINE.value
            );
        }
    };

    //日付校時で移動/コピーの列コピー
    this.execMoveBlockFullCopy = function (
        srcDateCd,
        targetDateCd,
        srcStartNum,
        srcEndNum,
        targetStarNum,
        testCd,
        IsTestNonMove,
        isTestKousa
    ) {
        var LinkingList = {};
        $(window).on(
            "setCellObjEvent",
            function (e, cellObj, srcBox, targetBox) {
                if (testCd) {
                    var srcDataTest = srcBox.getAttribute("data-test");
                    var srcDataTestList =
                        srcDataTest == ""
                            ? new Array()
                            : srcDataTest.split(",");
                    var testList = new Array();
                    for (var i = 0; i < srcDataTestList.length; i++) {
                        testList.push(testCd);
                    }
                    srcBox.setAttribute("data-test", testList.join(","));
                }
            }
        );
        $(window).on(
            "moveOneblockEvent",
            function (e, MObjE, srcBoxE, cntNumE, targetBoxE, isCheckE) {
                if (isCheckE) {
                    return;
                }
                if (IsTestNonMove) {
                    var srcDataTest = srcBoxE.getAttribute("data-test");
                    var srcDataTestList =
                        srcDataTest == ""
                            ? new Array()
                            : srcDataTest.split(",");
                    if (srcDataTestList[cntNumE] != "0") {
                        MObjE.moveOneblockEventCancel = true;
                        return;
                    }
                }
                var attrDataVal = srcBoxE.getAttribute("data-val");
                var attrDataValList =
                    attrDataVal == "" ? new Array() : attrDataVal.split(":");
                var linkingText = srcBoxE.getAttribute("data-linking");
                var linkingTextList =
                    linkingText == "" ? new Array() : linkingText.split("/");
                var linkingTextData = "";
                for (var i = 0; i < attrDataValList.length; i++) {
                    if (i == cntNumE) {
                        for (var j = 0; j < linkingTextList.length; j++) {
                            if (
                                linkingTextList[j].indexOf(
                                    attrDataValList[i]
                                ) != -1
                            ) {
                                linkingTextData = linkingTextList[j];
                                break;
                            }
                        }
                    }
                }
                if (linkingTextData == "") {
                    return;
                }
                //処理済みのlinkingの場合は、イベントキャンセル
                //1校時国語[linking:1校時2校時]、2校時国語[linking:1校時2校時]
                //この場合、2回目の処理はキャンセルされる。
                if (LinkingList[linkingTextData]) {
                    MObjE.moveOneblockEventCancel = true;
                } else {
                    LinkingList[linkingTextData] = true;
                }
            }
        );
        srcStartNum = parseInt(srcStartNum);
        srcEndNum = parseInt(srcEndNum);
        targetStarNum = parseInt(targetStarNum);
        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var srcId = "KOMA_" + srcDateCd + "_" + i + "_1";
            var targetId =
                "KOMA_" +
                targetDateCd +
                "_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var srcBox = $("#" + srcId)[0];
            var targetBox = $("#" + targetId)[0];
            var MObj = new MoveBlockObj();
            MObj.execMoveBlockFullCopy(
                srcBox,
                "",
                targetBox,
                false,
                isTestKousa
            );
        }
        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var srcBoxId = "KOMA_" + srcDateCd + "_" + i + "_1";
            var targetBoxId =
                "KOMA_" +
                targetDateCd +
                "_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var LObj = new LinkingCellObj();
            LObj.checkMeiboAndFac(
                srcBoxId,
                $("#" + srcBoxId)[0].getAttribute("data-linking"),
                targetBoxId,
                $("#" + targetBoxId)[0].getAttribute("data-linking"),
                document.forms[0].MAX_LINE.value
            );
        }
        $(window).off("setCellObjEvent");
        $(window).off("moveOneblockEvent");
    };

    //日付校時で移動/コピーの列入替
    this.execMoveBlockFullIrekae = function (
        srcDateCd,
        targetDateCd,
        srcStartNum,
        srcEndNum,
        targetStarNum,
        IsTestNonMove,
        IsTestKousa
    ) {
        srcStartNum = parseInt(srcStartNum);
        srcEndNum = parseInt(srcEndNum);
        targetStarNum = parseInt(targetStarNum);
        $(window).on(
            "setCellObjEvent",
            function (e, cellObj, srcBox, targetBox) {
                if (targetBox.id.indexOf("KOMA_99") !== -1) {
                    var obj = $("#" + targetBox.id)[0];
                    if (!obj) {
                        $("#templary_cell_data").append(targetBox);
                    }
                }
            }
        );
        $(window).on(
            "moveOneblockEvent",
            function (e, MObjE, srcBoxE, cntNumE, targetBoxE, isCheckE) {
                if (IsTestNonMove) {
                    var srcDataTest = srcBoxE.getAttribute("data-test");
                    var srcDataTestList =
                        srcDataTest == ""
                            ? new Array()
                            : srcDataTest.split(",");
                    if (srcDataTestList[cntNumE] != "0") {
                        MObjE.moveOneblockEventCancel = true;
                    }
                }
            }
        );
        $("body").append(
            $('<tr id="templary_cell_data" style="display:none"></tr>')
        );
        //src → temp
        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var srcId = "KOMA_" + srcDateCd + "_" + i + "_1";
            var targetId =
                "KOMA_9999_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var MObj = new MoveBlockObj();
            MObj.isIrekae = true;
            MObj.execMoveBlockFull(srcId, "", targetId, false, IsTestKousa);
        }

        //target → src
        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var srcId =
                "KOMA_" +
                targetDateCd +
                "_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var targetId = "KOMA_" + srcDateCd + "_" + i + "_1";
            var MObj = new MoveBlockObj();
            MObj.isIrekae = true;
            MObj.execMoveBlockFull(srcId, "", targetId, false, IsTestKousa);
        }

        //temp → target
        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var srcId =
                "KOMA_9999_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var targetId =
                "KOMA_" +
                targetDateCd +
                "_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var MObj = new MoveBlockObj();
            MObj.isIrekae = true;
            MObj.execMoveBlockFull(srcId, "", targetId, false, IsTestKousa);
        }
        $("#templary_cell_data").remove();
        $(window).off("setCellObjEvent");
        $(window).off("moveOneblockEvent");

        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var srcBoxId = "KOMA_" + srcDateCd + "_" + i + "_1";
            var targetBoxId =
                "KOMA_" +
                targetDateCd +
                "_" +
                (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                "_1";
            var LObj = new LinkingCellObj();
            LObj.checkMeiboAndFac(
                srcBoxId,
                $("#" + srcBoxId)[0].getAttribute("data-linking"),
                targetBoxId,
                $("#" + targetBoxId)[0].getAttribute("data-linking"),
                document.forms[0].MAX_LINE.value
            );
        }
    };

    //日付校時で移動/コピーの列削除
    this.execMoveBlockFullDelete = function (
        srcDateCd,
        srcStartNum,
        srcEndNum,
        IsTestNonMove,
        IsTestKousa
    ) {
        var idxCounter = 0;
        $(window).on(
            "deleteOneObjEvent",
            function (e, MObjE, srcBoxE, cntNumE) {
                if (IsTestNonMove) {
                    var srcDataTest = srcBoxE.getAttribute("data-test");
                    var srcDataTestList =
                        srcDataTest == ""
                            ? new Array()
                            : srcDataTest.split(",");
                    if (srcDataTestList[cntNumE] != "0") {
                        MObjE.deleteOneObjEventCancel = true;
                        idxCounter++;
                    }
                }
            }
        );
        srcStartNum = parseInt(srcStartNum);
        srcEndNum = parseInt(srcEndNum);

        docForm = document.forms[0];
        var MObj = new MoveBlockObj();
        if ($("#copyMoveBox_showListToList").is(":checked")) {
            var selectedValList = getMoveCopyBoxTarget();
            for (var i = srcStartNum; i <= srcEndNum; i++) {
                selectedValList.forEach(function (lineCnt) {
                    var id = "KOMA_" + srcDateCd + "_" + i + "_" + lineCnt;

                    var targetBox = $("#" + id)[0];
                    targetDataVal = targetBox.getAttribute("data-val");
                    if (targetDataVal == "") {
                        return;
                    }
                    var targetDataValList =
                        targetDataVal == ""
                            ? new Array()
                            : targetDataVal.split(":");
                    idxCounter = 0;
                    for (var j = 0; j < targetDataValList.length; j++) {
                        MObj.deleteOneObj(targetBox, idxCounter);
                    }
                });
                if (document.forms[0].LEFT_MENU.value == "1") {
                    //globalDelKeyListにセットされた講座が削除対象となる
                    //講座全ての教員が削除対象でなければ、削除対象外とする。
                    globalDelKeyListDef = $.extend([], globalDelKeyList);
                    for (
                        var lineCnt = 1;
                        lineCnt <= docForm.MAX_LINE.value;
                        lineCnt++
                    ) {
                        var targetBoxId =
                            "KOMA_" + srcDateCd + "_" + i + "_" + lineCnt;
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
            }
        } else {
            var startIdx = IsTestKousa ? 2 : 1;
            for (var i = srcStartNum; i <= srcEndNum; i++) {
                for (
                    var lineCnt = startIdx;
                    lineCnt <= docForm.MAX_LINE.value;
                    lineCnt++
                ) {
                    var id = "KOMA_" + srcDateCd + "_" + i + "_" + lineCnt;

                    var targetBox = $("#" + id)[0];
                    targetDataVal = targetBox.getAttribute("data-val");
                    if (targetDataVal == "") {
                        continue;
                    }
                    var targetDataValList =
                        targetDataVal == ""
                            ? new Array()
                            : targetDataVal.split(":");
                    idxCounter = 0;
                    for (var j = 0; j < targetDataValList.length; j++) {
                        MObj.deleteOneObj(targetBox, idxCounter);
                    }
                }
            }
        }
        $(window).off("deleteOneObjEvent");
        selectSubclass("getChair", null);
    };

    this.getChairList = function (targetDate, targetPeriod) {
        var list = [];

        for (var i = 1; i <= document.forms[0].MAX_LINE.value; i++) {
            var targetId = "KOMA_" + targetDate + "_" + targetPeriod + "_" + i;
            var targetBox = $("#" + targetId)[0];
            var dataVal = $(targetBox).attr("data-val");
            if (dataVal) {
                var dataValList = dataVal.split(":");
                for (let j = 0; j < dataValList.length; j++) {
                    var value = dataValList[j];
                    if (list.indexOf(value) < 0) {
                        list.push(value);
                    }
                }
            }
        }
        return list;
    };

    //日付校時で移動/コピーの列移動のチェック
    this.execMoveBlockFullMoveCheck = function (
        srcDateCd,
        targetDateCd,
        srcStartNum,
        srcEndNum,
        targetStarNum,
        IsTestNonMove,
        IsTestKousa
    ) {
        srcStartNum = parseInt(srcStartNum);
        srcEndNum = parseInt(srcEndNum);
        targetStarNum = parseInt(targetStarNum);
        $(window).on(
            "setCellObjEvent",
            function (e, cellObj, srcBox, targetBox) {
                //移動元で、移動先なtargetは空にする。

                //移動元が2/3/4校時として
                //移動先が3/4/5校時の時
                //3/4校時は移動元であり移動先なのでtargetは空として処理
                //なぜなら、移動先の3/4は移動元3/4が移動した後の状態だから空っぽ
                //5校時は、移動元にはないので元々の5校時に4校時を追加する形になるので
                //cellObj.targetは空にしない。
                if (srcDateCd == targetDateCd) {
                    for (var i = srcStartNum; i <= srcEndNum; i++) {
                        if (
                            targetBox.id.indexOf(
                                "KOMA_" + srcDateCd + "_" + i + "_"
                            ) != -1
                        ) {
                            cellObj.setCellObjEmptyTarget(srcBox);
                            cellObj.target.box = targetBox;
                            cellObj.target.id = targetBox.id;
                            cellObj.setCellObjEventCancel = true;
                            break;
                        }
                    }
                }
            }
        );

        var movedisabled = false;
        //操作対象行指定
        var selectedValList = [];
        if ($("#copyMoveBox_showListToList").is(":checked")) {
            var selectedValList = getMoveCopyBoxTarget();
            if (selectedValList.length <= 0) {
                movedisabled = true;
                return movedisabled;
            }
        }

        for (var i = srcStartNum; i <= srcEndNum; i++) {
            // ターゲットの列に登録されている講座を全て取得
            var chairDataList = [];
            var period = parseInt(targetStarNum) + (i - parseInt(srcStartNum));
            // 移動の場合、移動元の範囲内にある場合は講座リストの取得はしない
            if (
                srcDateCd != targetDateCd ||
                period < srcStartNum ||
                srcEndNum < period
            ) {
                // 移動元の範囲外にある場合は列の講座リストを取得
                chairDataList = this.getChairList(targetDateCd, period);
            }

            var startIdx = IsTestKousa ? 2 : 1;
            for (var j = startIdx; j <= document.forms[0].MAX_LINE.value; j++) {
                // 操作対象行以外の場合は読み飛ばし
                if (selectedValList.length > 0) {
                    if (selectedValList.indexOf(j.toString()) < 0) {
                        continue;
                    }
                }

                var srcId = "KOMA_" + srcDateCd + "_" + i + "_" + j;
                var targetId =
                    "KOMA_" +
                    targetDateCd +
                    "_" +
                    (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                    "_" +
                    j;
                var srcBox = $("#" + srcId)[0];
                var targetBox = $("#" + targetId)[0];
                if (!srcBox || !targetBox) {
                    movedisabled = true;
                    break;
                }
                if (!movedisabled) {
                    //移動元セルが空の場合はチェックしない
                    var srcDataVal = srcBox.getAttribute("data-val");
                    var srcDataTestVal = srcBox.getAttribute("data-test");
                    if (srcDataVal) {
                        // var MObj = new MoveBlockObj();
                        // MObj.checkCells(srcBox, '', targetBox);
                        // if (MObj.error == 2 || MObj.error == 3) {
                        //     movedisabled = true;
                        //     break;
                        // }

                        // ターゲットの講座の重複チェック
                        var chairCdList = srcDataVal.split(":");
                        var srcDataTestValList = srcDataTestVal.split(",");
                        for (let idx = 0; idx < chairCdList.length; idx++) {
                            if (
                                IsTestNonMove &&
                                srcDataTestValList[idx] != "0"
                            ) {
                                continue;
                            }
                            var chairCd = chairCdList[idx];
                            if (chairDataList.indexOf(chairCd) >= 0) {
                                movedisabled = true;
                                break;
                            }
                        }
                        if (movedisabled) {
                            break;
                        }
                    }
                }
            }
            if (movedisabled) {
                break;
            }
        }
        $(window).off("setCellObjEvent");
        return movedisabled;
    };

    //日付校時で移動/コピーの列コピーのチェック
    this.execMoveBlockFullCopyCheck = function (
        srcDateCd,
        targetDateCd,
        srcStartNum,
        srcEndNum,
        targetStarNum,
        IsTestNonMove,
        IsTestKousa
    ) {
        srcStartNum = parseInt(srcStartNum);
        srcEndNum = parseInt(srcEndNum);
        targetStarNum = parseInt(targetStarNum);
        var copydisabled = false;

        //操作対象行指定
        var selectedValList = [];
        if ($("#copyMoveBox_showListToList").is(":checked")) {
            var selectedValList = getMoveCopyBoxTarget();
            if (selectedValList.length <= 0) {
                copydisabled = true;
                return copydisabled;
            }
        }

        for (var i = srcStartNum; i <= srcEndNum; i++) {
            // ターゲットの列に登録されている講座を全て取得
            var period = parseInt(targetStarNum) + (i - parseInt(srcStartNum));
            var chairDataList = this.getChairList(targetDateCd, period);

            var startIdx = IsTestKousa ? 2 : 1;
            for (var j = startIdx; j <= document.forms[0].MAX_LINE.value; j++) {
                // 操作対象行以外の場合は読み飛ばし
                if (selectedValList.length > 0) {
                    if (selectedValList.indexOf(j.toString()) < 0) {
                        continue;
                    }
                }

                var srcId = "KOMA_" + srcDateCd + "_" + i + "_" + j;
                var targetId =
                    "KOMA_" +
                    targetDateCd +
                    "_" +
                    (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                    "_" +
                    j;
                var srcBox = $("#" + srcId)[0];
                var targetBox = $("#" + targetId)[0];
                if (!srcBox || !targetBox) {
                    copydisabled = true;
                    break;
                }
                if (!copydisabled) {
                    //移動元セルが空の場合はチェックしない
                    var srcDataVal = srcBox.getAttribute("data-val");
                    var srcDataTestVal = srcBox.getAttribute("data-test");
                    if (srcDataVal) {
                        // var MObj = new MoveBlockObj();
                        // MObj.isCopy = true;
                        // MObj.error = 0;
                        // MObj.checkCells(srcBox, '', targetBox);
                        // if (MObj.error == 2 || MObj.error == 3) {
                        //     copydisabled=true;
                        //     break;
                        // }

                        // ターゲットの講座の重複チェック
                        var chairCdList = srcDataVal.split(":");
                        var srcDataTestValList = srcDataTestVal.split(",");
                        for (let idx = 0; idx < chairCdList.length; idx++) {
                            if (
                                IsTestNonMove &&
                                srcDataTestValList[idx] != "0"
                            ) {
                                continue;
                            }
                            var chairCd = chairCdList[idx];
                            if (chairDataList.indexOf(chairCd) >= 0) {
                                copydisabled = true;
                                break;
                            }
                        }
                        if (copydisabled) {
                            break;
                        }
                    }
                }
            }
            if (copydisabled) {
                break;
            }
        }
        return copydisabled;
    };

    //日付校時で移動/コピーの列入替のチェック
    this.execMoveBlockFullIrekaeCheck = function (
        srcDateCd,
        targetDateCd,
        srcStartNum,
        srcEndNum,
        targetStarNum,
        IsTestNonMove,
        IsTestKousa
    ) {
        srcStartNum = parseInt(srcStartNum);
        srcEndNum = parseInt(srcEndNum);
        targetStarNum = parseInt(targetStarNum);

        calcSrcId = "KOMA_" + srcDateCd + "_" + srcStartNum;
        calcTargetId = "KOMA_" + targetDateCd + "_" + targetStarNum;

        var srcMoveCellList = {};
        var targetMoveCellList = {};
        var tempCellList = {};

        //操作対象行指定
        var selectedValList = [];
        if ($("#copyMoveBox_showListToList").is(":checked")) {
            var selectedValList = getMoveCopyBoxTarget();
            if (selectedValList.length <= 0) {
                return [null, true];
            }
        }

        for (var i = srcStartNum; i <= srcEndNum; i++) {
            var startIdx = IsTestKousa ? 2 : 1;
            for (var j = startIdx; j <= document.forms[0].MAX_LINE.value; j++) {
                // 操作対象行以外の場合は読み飛ばし
                if (selectedValList.length > 0) {
                    if (selectedValList.indexOf(j.toString()) < 0) {
                        continue;
                    }
                }

                var srcId = "KOMA_" + srcDateCd + "_" + i + "_" + j;
                var srcDayPeri = srcDateCd + "_" + i;
                srcMoveCellList = this.getMoveCellList(
                    srcId,
                    srcDayPeri,
                    srcMoveCellList,
                    IsTestNonMove
                );
                if (srcMoveCellList === false) {
                    return [null, true];
                }

                var targetId =
                    "KOMA_" +
                    targetDateCd +
                    "_" +
                    (parseInt(targetStarNum) + (i - parseInt(srcStartNum))) +
                    "_" +
                    j;
                var targetDayPeri =
                    targetDateCd +
                    "_" +
                    (parseInt(targetStarNum) + (i - parseInt(srcStartNum)));
                targetMoveCellList = this.getMoveCellList(
                    targetId,
                    targetDayPeri,
                    targetMoveCellList,
                    IsTestNonMove
                );
                if (targetMoveCellList === false) {
                    return [null, true];
                }
            }
        }

        var oriSrcCellList = $.extend({}, srcMoveCellList);
        var oriTargetCellList = $.extend({}, targetMoveCellList);

        targetMoveCellList = this.setTempCellFullList(
            calcSrcId,
            calcTargetId,
            oriSrcCellList,
            targetMoveCellList
        );
        if (targetMoveCellList === false) {
            return [null, true];
        }
        srcMoveCellList = this.setTempCellFullList(
            calcTargetId,
            calcSrcId,
            oriTargetCellList,
            srcMoveCellList
        );
        if (srcMoveCellList === false) {
            return [null, true];
        }

        var margeCellList = $.extend(
            true,
            {},
            srcMoveCellList,
            targetMoveCellList
        );

        tempCellList = this.setTempCellList(tempCellList, margeCellList);

        var cellListAndFlg = new Array();
        cellListAndFlg = this.checkKouzaTyoufuku(
            tempCellList,
            srcMoveCellList,
            calcSrcId,
            calcTargetId
        );
        if (cellListAndFlg[1]) {
            return [null, true];
        }
        cellListAndFlg = this.checkKouzaTyoufuku(
            cellListAndFlg[0],
            targetMoveCellList,
            calcTargetId,
            calcSrcId
        );
        if (cellListAndFlg[1]) {
            return [null, true];
        }

        return [cellListAndFlg[0], false];
    };

    //移動対象にtrue
    this.getMoveCellList = function (
        srcId,
        dayPeri,
        moveCellList,
        IsTestNonMove
    ) {
        var retMoveCellList = moveCellList;
        var lObj = new LinkingCellObj();
        var box = $("#" + srcId)[0];
        if (!box) {
            return false;
        }
        var dataVal = box.getAttribute("data-val");
        var dataTestVal = box.getAttribute("data-test");
        var dataValList = dataVal == "" ? new Array() : dataVal.split(":");
        var dataTestValList =
            dataTestVal == "" ? new Array() : dataTestVal.split(",");
        for (var i = 0; i < dataValList.length; i++) {
            if (IsTestNonMove && dataTestValList[i] != "0") {
                continue;
            }
            if (!retMoveCellList["KOMA_" + dayPeri]) {
                retMoveCellList["KOMA_" + dayPeri] = {};
            }
            retMoveCellList["KOMA_" + dayPeri][dataValList[i]] = true;
        }
        lObj.setLinkingTextToObj(box.getAttribute("data-linking"));

        for (var j = 0; j < lObj.linkingObj.length; j++) {
            var linkingKouza = lObj.linkingObj[j].id;
            for (var k = 0; k < lObj.linkingObj[j].list.length; k++) {
                var linkCellId = lObj.linkingObj[j].list[k];
                var linkIdArray = linkCellId.split("_");
                var setDayPeri =
                    "KOMA_" + linkIdArray[1] + "_" + linkIdArray[2];
                if (!retMoveCellList[setDayPeri]) {
                    retMoveCellList[setDayPeri] = {};
                }
                retMoveCellList[setDayPeri][linkingKouza] = true;
            }
        }
        return retMoveCellList;
    };

    //移動対象外にtrue
    this.setTempCellFullList = function (
        calcSrcId,
        calcTargetId,
        srcCellList,
        targetCellList
    ) {
        for (var dayPeri in srcCellList) {
            var calcDayPeri = this.calcCellDayPeri(
                calcSrcId,
                calcTargetId,
                dayPeri
            );
            if (!$("#" + calcDayPeri + "_1")[0]) {
                return false;
            }
            if (!targetCellList[calcDayPeri]) {
                targetCellList[calcDayPeri] = {};
            }
        }
        return targetCellList;
    };

    //移動対象外にtrue
    this.setTempCellList = function (tempCellList, moveCellList) {
        for (var dayPeri in moveCellList) {
            for (var i = 1; i <= document.forms[0].MAX_LINE.value; i++) {
                var id = dayPeri + "_" + i;
                var attrDataVal = $("#" + id)[0].getAttribute("data-val");
                var attrDataValList =
                    attrDataVal == "" ? new Array() : attrDataVal.split(":");
                for (var j = 0; j < attrDataValList.length; j++) {
                    if (!moveCellList[dayPeri][attrDataValList[j]]) {
                        if (!tempCellList[dayPeri]) {
                            tempCellList[dayPeri] = {};
                        }
                        tempCellList[dayPeri][attrDataValList[j]] = true;
                    }
                }
            }
        }
        return tempCellList;
    };

    //チェック
    this.checkKouzaTyoufuku = function (
        tempCellList,
        moveCellList,
        calcSrcId,
        calcTargetId
    ) {
        var retTempCellList = tempCellList;
        for (var dayPeri in moveCellList) {
            for (var kouza in moveCellList[dayPeri]) {
                var calcDayPeri = this.calcCellDayPeri(
                    calcSrcId,
                    calcTargetId,
                    dayPeri
                );
                if (!$("#" + calcDayPeri + "_1")[0]) {
                    return [tempCellList, true];
                }
                if (!retTempCellList[calcDayPeri]) {
                    retTempCellList[calcDayPeri] = {};
                }
                if (retTempCellList[calcDayPeri][kouza]) {
                    return [tempCellList, true];
                } else {
                    retTempCellList[calcDayPeri][kouza] = true;
                }
            }
        }
        return [retTempCellList, false];
    };

    //移動元セルのIDを基に、移動先セルのIDを演算する。
    //KOMA_日付_(校時+(先校時-元校時))
    this.calcCellDayPeri = function (calcSrcId, calcTargetId, calcMoveId) {
        var srcIdArray = calcSrcId.split("_");
        var targetIdArray = calcTargetId.split("_");

        var divIdArray = calcMoveId.split("_");
        var targetDivId = targetIdArray[0] + "_" + targetIdArray[1] + "_";
        targetDivId =
            targetDivId +
            (parseInt(divIdArray[2]) +
                (parseInt(targetIdArray[2]) - parseInt(srcIdArray[2])));
        return targetDivId;
    };

    //移動元セルのIDを基に、移動先セルのIDを演算する。
    //KOMA_日付_(校時+(先校時-元校時))
    this.meiboDupliCheckIrekae = function (
        cellDayPeriList,
        maxLine,
        IsTestNonMove,
        isSyussekiFunc,
        isSyussekiFalseFunc
    ) {
        //日付リストの生成
        var newDataList = $("input[name=ALL_DATE]").val().split(",");

        var calcList = new Array();
        for (var dayPeri in cellDayPeriList) {
            var idArray = dayPeri.split("_");
            var kouzaList = new Array();
            for (var kouza in cellDayPeriList[dayPeri]) {
                kouzaList.push(kouza);
            }
            if (IsTestNonMove) {
                for (
                    lineCnt = 1;
                    lineCnt <= document.forms[0].MAX_LINE.value;
                    lineCnt++
                ) {
                    var val = $("#" + dayPeri + "_" + lineCnt)[0].getAttribute(
                        "data-val"
                    );
                    var valList = val == "" ? new Array() : val.split(",");
                    var testVal = $(
                        "#" + dayPeri + "_" + lineCnt
                    )[0].getAttribute("data-test");
                    var testValList =
                        testVal == "" ? new Array() : testVal.split(",");
                    for (var i = 0; i < valList.length; i++) {
                        if (
                            testValList[i] != "0" &&
                            kouzaList.indexOf(valList[i]) === -1
                        ) {
                            kouzaList.push(valList[i]);
                        }
                    }
                }
            }
            calcList.push({
                targetDay: newDataList[idArray[1]],
                id: dayPeri + "_1",
                kouzi: idArray[2],
                list: kouzaList,
            });
        }

        //AJAX通信。
        $.ajax({
            url: "knjb3042index.php",
            type: "POST",
            data: {
                AJAX_PARAM: JSON.stringify({
                    AJAX_MEIBO_PARAM: JSON.stringify(calcList),
                }),
                cmd: "getMeiboParam",
                YEAR_SEME: document.forms[0].YEAR_SEME.value,
            },
        }).done(function (data, textStatus, jqXHR) {
            paramList = $.parseJSON(data);
            for (var i = 0; i < paramList.length; i++) {
                for (var j = 1; j <= maxLine; j++) {
                    var idArray = paramList[i]["id"].split("_");
                    var id =
                        "KOMA_" +
                        idArray[1] +
                        "_" +
                        paramList[i]["kouzi"] +
                        "_" +
                        j;
                    var BoxDataVal = $("#" + id)[0].getAttribute("data-val");
                    var BoxDataValList =
                        BoxDataVal == "" ? new Array() : BoxDataVal.split(":");
                    var BoxDataExec = $("#" + id)[0].getAttribute("data-exec");
                    var BoxDataExecList =
                        BoxDataExec == ""
                            ? new Array()
                            : BoxDataExec.split(",");
                    for (var k = 0; k < BoxDataValList.length; k++) {
                        if (paramList[i]["list"][BoxDataValList[k]] == 1) {
                            if (
                                BoxDataExecList[k] !== "MI_SYUKKETSU" &&
                                isSyussekiFunc
                            ) {
                                isSyussekiFunc();
                                return;
                            }
                        }
                    }
                }
            }
            if (isSyussekiFalseFunc) {
                isSyussekiFalseFunc();
            }
        });
    };
}

function getMoveCopyBoxTarget() {
    var selectedValList = [];
    var selectedList = $("#category_copymovebox_selected option");
    selectedList.each(function (index, element) {
        var selectedVal = $(element).val().split(":");
        selectedValList[index] = selectedVal[1];
    });
    return selectedValList;
}
