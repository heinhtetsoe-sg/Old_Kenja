
/**
 * セルオブジェクト
 */
function CellObj() {

    this.cellId = null;
    this.order = null;

    this.lineKey = null;
    this.lineKey2 = null;

    this.unPlacedFlg = '';
    this.dupChairFlg = '';
    this.facCapOverFlg = '';

    /**
     * 講座情報
     */
    this.chairObjList = [];

    this.setCell = function (cell) {

        this.cellId = '#' + $(cell).attr('id');
        this.order = $(cell).attr('order');
        this.lineKey = $(cell).attr('line-key');
        this.lineKey2 = $(cell).attr('line-key2');

        this.unPlacedFlg = $(cell).attr('data-unPlacedFlg');
        this.dupChairFlg = $(cell).attr('data-dupChairFlg');
        this.facCapOverFlg = $(cell).attr('data-facCapOverFlg');

        if ($(this.cellId).attr('data-val')) {
            var chairInfoList = JSON.parse($(this.cellId).attr('data-val'));
            for (let i = 0; i < chairInfoList.length; i++) {
                const chairInfo = chairInfoList[i];

                this.chairObjList.push(chairInfo);
            }
        }
    }

    this.getChairInfoList = function (cell) {

        this.cellId = '#' + $(cell).attr('id');
        var result = [];
        if ($(this.cellId).attr('data-val')) {
            result = JSON.parse($(this.cellId).attr('data-val'));
        }
        return result;
    }

    /**
     * 講座情報の追加判定
     */
    this.checkAddChair = function (chairInfo) {
        for (let i = 0; i < this.chairObjList.length; i++) {
            const chair = this.chairObjList[i];

            // 講座情報が一致する場合は講座の追加不可
            if (chair.classCd == chairInfo.classCd
                && chair.schoolKind == chairInfo.schoolKind
                && chair.curriculumCd == chairInfo.curriculumCd
                && chair.subclassCd == chairInfo.subclassCd
                && chair.chairCd == chairInfo.chairCd) {
                    return false;
            }
        }
        return true;
    }

    /**
     * 講座情報の追加判定(リスト形式)
     */
    this.checkAddChairList = function (chairInfoList) {

        for (let i = 0; i < chairInfoList.length; i++) {
            const chair = chairInfoList[i];
            if (!this.checkAddChair(chair)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 講座情報の追加(単一講座情報)
     */
    this.addChair = function (chairInfo) {
        // 講座情報の一覧に変更がある場合はフラグを初期化
        this.unPlacedFlg = '';
        this.dupChairFlg = '';
        this.facCapOverFlg   = '';
        this.chairObjList.push(chairInfo);
    }

    /**
     * 講座情報の追加(リスト形式)
     */
    this.addChairList = function (chairInfoList) {

        for (let i = 0; i < chairInfoList.length; i++) {
            const chair = chairInfoList[i];
            this.addChair(chair);
        }
    }

    /**
     * 講座情報の全削除
     */
    this.removeChairAll = function () {
        // 講座情報の一覧に変更がある場合はフラグを初期化
        this.unPlacedFlg = '';
        this.dupChairFlg = '';
        this.facCapOverFlg   = '';

        var chairList = [];
        this.chairObjList = chairList;
    }

    /**
     * 講座情報の削除
     */
    this.removeChair = function (chairInfo) {
        // 講座情報の一覧に変更がある場合はフラグを初期化
        this.unPlacedFlg = '';
        this.dupChairFlg = '';
        this.facCapOverFlg = '';

        var chairList = [];

        for (let i = 0; i < this.chairObjList.length; i++) {
            const chair = this.chairObjList[i];

            // 講座情報が一致しない場合は新リストへ追加する
            if (chair.classCd != chairInfo.classCd
                || chair.schoolKind != chairInfo.schoolKind
                || chair.curriculumCd != chairInfo.curriculumCd
                || chair.subclassCd != chairInfo.subclassCd
                || chair.chairCd != chairInfo.chairCd) {

                chairList.push(chair);
            }
        }
        this.chairObjList = chairList;
    }

    /**
     * 講座情報の削除
     */
    this.removeChairList = function (chairInfoList) {

        for (let i = 0; i < chairInfoList.length; i++) {
            const chair = chairInfoList[i];
            this.removeChair(chair);
        }
    }

    /**
     * 講座情報の書込み(HTMLへ反映)
     */
    this.writeCell = function () {

        // 画面表示
        dataText = '';

        // 更新フラグ
        $(this.cellId).attr('data-update', '1');


        // 講座情報初期化
        $(this.cellId).attr('data-val', '');
        // 受講生未配置フラグ
        $(this.cellId).attr('data-unPlacedFlg', '');
        // 受講生重複フラグ
        $(this.cellId).attr('data-dupChairFlg', '');
        // 受講生重複フラグ
        $(this.cellId).attr('data-facCapOverFlg', '');

        // 講座情報
        if (this.chairObjList.length > 0) {
            $(this.cellId).attr('data-val', JSON.stringify(this.chairObjList));
            // 受講生未配置フラグ
            $(this.cellId).attr('data-unPlacedFlg', this.unPlacedFlg);
            // 受講生重複フラグ
            $(this.cellId).attr('data-dupChairFlg', this.dupChairFlg);
            // 受講生重複フラグ
            $(this.cellId).attr('data-facCapOverFlg', this.facCapOverFlg);
        }

        // 画面表示
        if (this.chairObjList.length == 1) {
            dataText += this.chairObjList[0].chairCd + "<br/>";
            dataText += this.chairObjList[0].chairName + "<br/>";
        } else if (this.chairObjList.length > 1) {
            dataText += this.chairObjList.length + "件のデータ<br/>";
        }

        // 受講者未配置判定
        if (this.unPlacedFlg) {
            dataText += '未';
        }
        // 受講者重複判定
        if (this.dupChairFlg) {
            dataText += '重';
        }
        // 施設重複反映
        if (this.facCapOverFlg) {
            dataText += '施';
        }

        // スタイルクラス設定
        this.setStyleClass();
        // HTML書込み
        $(this.cellId).html(dataText);

        return;
    }

    /**
     * スタイルクラスの設定を行う
     */
    this.setStyleClass = function () {
        // 複数講座のCLASS削除
        $(this.cellId).removeClass('hukusuu_box');
        if (this.chairObjList.length > 1) {
            // 複数講座のCLASS追加
            $(this.cellId).addClass('hukusuu_box');
        }
    }

    /**
     * 未配置フラグ設定
     */
    this.setUnPlaced = function (flg) {
        this.unPlacedFlg = flg;
        this.writeCell();
    }

    /**
     * 受講生重複フラグ設定
     */
    this.setDupChair = function (flg) {
        this.dupChairFlg = flg;
        this.writeCell();
    }

    /**
     * 施設講座キャパ超フラグ設定
     */
    this.setFacCapOver = function (flg) {
        this.facCapOverFlg = flg;
        this.writeCell();
    }
}

/**
 * 列の移動・コピー・入替・削除を行う
 * @param {*} order 
 */
function VerticalCellObj(order) {

    this.order = order;
    this.cells = [];

    this.setOrder = function(order) {
        this.order = order;
    }

    this.getCells = function(order) {

        var cells = [];
        // 縦列のセル取得
        var targetBoxs = $('.targetbox[data-val!=""][order=' + order + ']');
        if (targetBoxs.length <= 0) {
            return;
        }
        for (let index = 0; index < targetBoxs.length; index++) {
            const element = targetBoxs[index];

            var cellObj = new CellObj();
            cellObj.setCell($(element));
            cells.push(cellObj);
        }
        return cells;
    }

    // 受講生重複人数
    this.getDupStdCnt = function() {
        return 0;
    }

    // 受講生重複講座
    this.getDupChairList = function() {
        return 0;
    }

    /**
     * 受講生未配置科目
     */
    this.getStdUnPlaced = function() {

        var targetBoxs = $('.targetbox[data-val!=""][order=' + order + ']');
        if (targetBoxs.length <= 0) {
            $('.footer_unplaced[order=' + order + ']').text('');
            return;
        }

        var ajaxParam = {};
        $.ajax({
            url:'knjb3043index.php',
            type:'POST',
            data:{
                  AJAX_PARAM : JSON.stringify(ajaxParam)
                , cmd : 'getStdUnPlacedCnt'
                , YEAR_SEME : $('select[name=YEAR_SEME] option:selected').val()
            },
            async:false
        }).done(function(data, textStatus, jqXHR) {
            var paramList = $.parseJSON(data);

            // 未配置人数の変更
            var stdCnt = 0;

            var targetBoxs = $('.targetbox[data-val!=""][order=' + order + ']');
            for (let index = 0; index < targetBoxs.length; index++) {
                const element = targetBoxs[index];
                var cellObj = new CellObj();
                cellObj.setCell($(element));
                cellObj.setUnPlaced('');

                var courseCd =$(element).attr('line-key');
                if (!paramList[courseCd]) {
                    continue;
                }

                var dataVal = $(element).attr('data-val');
                var chairList = JSON.parse(dataVal);

                for (let i = 0; i < chairList.length; i++) {
                    var chairInfo = chairList[i];

                    var subclassCd = chairInfo["classCd"] + "-" + chairInfo["schoolKind"] + "-" + chairInfo["curriculumCd"] + "-" + chairInfo["subclassCd"];

                    if (paramList[courseCd][subclassCd]) {
                        stdCnt += parseInt(paramList[courseCd][subclassCd]);
                        cellObj.setUnPlaced(true);
                    }
                }
            }
            $('.footer_unplaced[order=' + order + ']').text(stdCnt);
        });

    }

    /**
     * 縦列の移動チェック
     */
    this.isVerticalLineMove = function (lines, srcOrder, targetOrder) {

        var isMove = true;
        var srcCells = this.getCells(srcOrder);

        for (let i = 0; i < srcCells.length; i++) {
            const srcCellObj = srcCells[i];
            // 行での絞込みを追加
            var isChk = lines.filter(function(item, index) {
                if (item['line-key'] == srcCellObj.lineKey && item['line-key2'] == srcCellObj.lineKey2) return true;
            });
            if (isChk.length <= 0) {
                continue;
            }
            var srcIdArray = srcCellObj.cellId.split('_');
            var targetId = '#KOMA_' + srcIdArray[1] + '_' + targetOrder;
            var targetCell = new CellObj();
            targetCell.setCell($(targetId));

            isMove = targetCell.checkAddChairList(srcCellObj.chairObjList);
            if (!isMove) {
                break;
            }
        }
        return isMove;
    }

    /**
     * 縦列のコピーチェック
     */
    this.isVerticalLineCopy = function (lines, srcOrder, targetOrder) {

        var isCopy = true;
        var srcCells = this.getCells(srcOrder);

        for (let i = 0; i < srcCells.length; i++) {
            const srcCellObj = srcCells[i];
            // 行での絞込みを追加
            var isChk = lines.filter(function(item, index) {
                if (item['line-key'] == srcCellObj.lineKey && item['line-key2'] == srcCellObj.lineKey2) return true;
            });
            if (isChk.length <= 0) {
                continue;
            }
            var srcIdArray = srcCellObj.cellId.split('_');
            var targetId = '#KOMA_' + srcIdArray[1] + '_' + targetOrder;


            var targetCell = new CellObj();
            targetCell.setCell($(targetId));

            isCopy = targetCell.checkAddChairList(srcCellObj.chairObjList);
            if (!isCopy) {
                break;
            }
        }
        return isCopy;
    }

    /**
     * 縦列の入替チェック
     */
    this.isVerticalLineSwap = function (lines, srcOrder, targetOrder) {
        // 処理なし
        return true;
    }

    /**
     * 縦列の移動
     */
    this.toVerticalLineMove = function (lines, srcOrders, targetOrder) {

        var orderCells = {};
        for (let i = 0; i < srcOrders.length; i++) {
            const order = srcOrders[i];
            orderCells[order] = this.getCells(order);
        }

        for (let i = 0; i < srcOrders.length; i++) {
            var order = srcOrders[i];
            var tarOrder = targetOrder + i;

            for (let index = 0; index < orderCells[order].length; index++) {
                var srcCell = orderCells[order][index];
                // 行での絞込みを追加
                var isChk = lines.filter(function(item, index) {
                    if (item['line-key'] == srcCell.lineKey && item['line-key2'] == srcCell.lineKey2) return true;
                });
                if (isChk.length <= 0) {
                    continue;
                }

                var srcIdArray = srcCell.cellId.split('_');
                var fromCell = new CellObj();
                fromCell.setCell($(srcCell.cellId));

                var targetId = '#KOMA_' + srcIdArray[1] + '_' + tarOrder;
                var toCell = new CellObj();
                toCell.setCell($(targetId));

                fromCell.removeChairAll();
                fromCell.writeCell();

                toCell.addChairList(srcCell.chairObjList);
                toCell.writeCell();
            }
        }
        return;
    }

    /**
     * 縦列のコピー
     */
    this.toVerticalLineCopy = function (lines, srcOrders, targetOrder) {

        var orderCells = {};
        for (let i = 0; i < srcOrders.length; i++) {
            const order = srcOrders[i];
            orderCells[order] = this.getCells(order);
        }

        for (let i = 0; i < srcOrders.length; i++) {
            var order = srcOrders[i];
            var tarOrder = targetOrder + i;

            for (let index = 0; index < orderCells[order].length; index++) {
                var srcCell = orderCells[order][index];
                // 行での絞込みを追加
                var isChk = lines.filter(function(item, index) {
                    if (item['line-key'] == srcCell.lineKey && item['line-key2'] == srcCell.lineKey2) return true;
                });
                if (isChk.length <= 0) {
                    continue;
                }

                var srcIdArray = srcCell.cellId.split('_');
                var targetId = '#KOMA_' + srcIdArray[1] + '_' + tarOrder;
                var toCell = new CellObj();
                toCell.setCell($(targetId));

                toCell.addChairList(srcCell.chairObjList);
                toCell.writeCell();
            }
        }

        return;
    }

    /**
     * 縦列の入替
     */
    this.toVerticalLineSwap = function (lines, srcOrders, targetOrder) {

        var orderCells = {};
        for (let i = 0; i < srcOrders.length; i++) {
            const order = srcOrders[i];
            orderCells[order] = this.getCells(order);
        }

        // TODO : 移動先のセルに講座が設定されている場合は、移動元が空白でも移動対象にする！
        for (let i = 0; i < srcOrders.length; i++) {
            var order = srcOrders[i];
            var tarOrder = targetOrder + i;

            var targetCells = this.getCells(tarOrder);

            for (let index = 0; index < targetCells.length; index++) {

                const targetCell = targetCells[index];
                var isChk = orderCells[order].filter(function(item, index) {
                    if (item.lineKey == targetCell.lineKey && item.lineKey2 == targetCell.lineKey2) return true;
                });
                if (isChk.length <= 0) {
                    var targetIdArray = targetCell.cellId.split('_');
                    var srcId = '#KOMA_' + targetIdArray[1] + '_' + order;
                    var srcCell = new CellObj();
                    srcCell.setCell($(srcId));
                    orderCells[order].push(srcCell);
                }

            }

        }

        for (let i = 0; i < srcOrders.length; i++) {
            var order = srcOrders[i];
            var tarOrder = targetOrder + i;

            for (let index = 0; index < orderCells[order].length; index++) {
                var srcCell = orderCells[order][index];
                // 行での絞込みを追加
                var isChk = lines.filter(function(item, index) {
                    if (item['line-key'] == srcCell.lineKey && item['line-key2'] == srcCell.lineKey2) return true;
                });
                if (isChk.length <= 0) {
                    continue;
                }

                var srcIdArray = srcCell.cellId.split('_');
                var fromCell = new CellObj();
                fromCell.setCell($(srcCell.cellId));

                var targetId = '#KOMA_' + srcIdArray[1] + '_' + tarOrder;
                var toCell = new CellObj();
                toCell.setCell($(targetId));

                fromCell.removeChairAll();
                fromCell.addChairList(toCell.chairObjList);
                fromCell.writeCell();

                toCell.removeChairAll();
                toCell.addChairList(srcCell.chairObjList);
                toCell.writeCell();
            }
        }
        return;
    }

    /**
     * 縦列の削除
     */
    this.toVerticalLineDelete = function (lines, srcOrders, targetOrder) {

        var orderCells = {};
        for (let i = 0; i < srcOrders.length; i++) {
            const order = srcOrders[i];
            orderCells[order] = this.getCells(order);
        }

        for (let i = 0; i < srcOrders.length; i++) {
            var order = srcOrders[i];
            var tarOrder = targetOrder + i;

            for (let index = 0; index < orderCells[order].length; index++) {
                var srcCell = orderCells[order][index];
                // 行での絞込みを追加
                var isChk = lines.filter(function(item, index) {
                    if (item['line-key'] == srcCell.lineKey && item['line-key2'] == srcCell.lineKey2) return true;
                });
                if (isChk.length <= 0) {
                    continue;
                }

                var fromCell = new CellObj();
                fromCell.setCell($(srcCell.cellId));

                fromCell.removeChairAll();
                fromCell.writeCell();
            }
        }
    }

    return this;
}
