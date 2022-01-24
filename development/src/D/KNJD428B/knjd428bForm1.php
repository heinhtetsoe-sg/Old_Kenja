<?php

require_once('for_php7.php');

class knjd428bForm1 {
    function main(&$model) {
        $objForm = new form;
        // Add by HPA for title 2020-01-20 start
        if($model->schregno != "" && $model->name != ""){
            $arg["TITLE"] = "".$model->schregno."". $model->name."の情報画面";
        }else{
            $arg["TITLE"] = "右情報画面";
        }
        // Add by HPA for title 2020-01-31 end
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd428bindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //グループ情報
        $getGroupRow = array();
        $getGroupRow = $db->getRow(knjd428bQuery::getViewGradeKindSchreg($model, "set"), DB_FETCHMODE_ASSOC);
        if ($model->schregno) {
            $model->groupInfo = $getGroupRow;
            $getGroupName = $db->getOne(knjd428bQuery::getGroupcd($model, $getGroupRow));
            if ($getGroupName) {
                $arg["GROUP_NAME"] = '履修科目グループ:'.$getGroupName;
            } else {
                $arg["GROUP_NAME"] = '履修科目グループ未設定';
            }
            $getConditionName = $db->getOne(knjd428bQuery::getConditionName($model, $getGroupRow["CONDITION"]));
            $arg["CONDITION_NAME"] = ($getConditionName) ? '('.$getConditionName.')' : "";
        }

        //入力項目名称
        $model->inputDivName = array("1" => "自立活動（目標）",
                              "2" => "自立活動",
                              "3" => "各教科等",
                              "4" => "総合所見"
                              );
        foreach($model->inputDivName as $key => $val) {
            $arg["INPUT_DIV_NAME{$key}"] = $val;
        }

        //入力項目ラジオ
        $opt = array(1, 2, 3, 4);
        $model->field["INPUT_DIV"] = ($model->field["INPUT_DIV"] == "") ? "1" : $model->field["INPUT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"INPUT_DIV{$val}\" onchange=\"return btn_submit('edit');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "INPUT_DIV", $model->field["INPUT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //3枠,2枠指定用ラジオ1
        $disabledFlg = ($model->field["INPUT_DIV"] != 2) ? true : false;
        if($model->field["INPUT_DIV"] != 2) $model->field["INPUT_DETAIL_DIV1"] = "";
        makeRadio($objForm, $arg, $model, "INPUT_DETAIL_DIV1", $model->field["INPUT_DETAIL_DIV1"], $disabledFlg);
        //3枠,2枠指定用ラジオ2
        $disabledFlg = ($model->field["INPUT_DIV"] != 3) ? true : false;
        if($model->field["INPUT_DIV"] != 3) $model->field["INPUT_DETAIL_DIV2"] = "";
        makeRadio($objForm, $arg, $model, "INPUT_DETAIL_DIV2", $model->field["INPUT_DETAIL_DIV2"], $disabledFlg);

        //学期項目
        if ($model->field["INPUT_DIV"] != 1) {
            $query = knjd428bQuery::getSemester($model);
            $extra  = " onchange=\"return btn_submit('edit');\" ";
            makeCmb($objForm, $arg, $db, $query, $model->selectSemes, "SELECT_SEMES", $extra, 1);
        } else {
            $arg["SELECT_SEMES"] = "年間目標のため、学期の選択は不要です。";
            $model->selectSemes = "9";
        }

        if ($model->field["INPUT_DIV"] == 3) {
            //科目コンボ
            $query = knjd428bQuery::getSubclass($model);
            /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
            $extra = "id = \"SUBCLASSCD\" aria-label = \"教科・科目\" onChange=\"current_cursor('SUBCLASSCD');return btn_submit('edit');\"";
            /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
            makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1);
        }

        //各入力項目に対応する設定をセット
        $subTorikomiFlg = false;
        if ($model->field["INPUT_DIV"] == 1) {
            //自立活動(目標)
            $kindNo         = "01";
            $kindSeqArray   = array("001");
            $divArray       = array("1");
            $mojiGyouArray   = array('45-4');
            $model->colName = "GOALS";
        } else if ($model->field["INPUT_DIV"] == 2) {
            //自立活動(3枠・2枠)
            $kindNo         = "01";
            if ($model->field["INPUT_DETAIL_DIV1"] == 1) {
                $kindSeqArray   = array("003", "004", "005");
                $divArray       = array("1", "2", "3");
                $mojiGyouArray   = array('10-20', '17-20', '17-20');
            } else {
                $kindSeqArray   = array("003", "005");
                $divArray       = array("1", "3");
                $mojiGyouArray   = array('22-16', '22-16');
            }
            $model->colName     = "REMARK";
        } else if ($model->field["INPUT_DIV"] == 3) {
            //各教科等(3枠・2枠)
            $kindNo         = "03";
            if ($model->field["INPUT_DETAIL_DIV2"] == 1) {
                $kindSeqArray   = array("001", "002", "003");
                $seqArray       = array("1", "2", "3");
                $mojiGyouArray   = array('12-25', '12-25', '15-25');
            } else {
                $kindSeqArray   = array("002", "003");
                $seqArray       = array("2", "3");
                $mojiGyouArray   = array('20-19', '20-19');
            }
            $model->colName     = "REMARK";
            $subTorikomiFlg = true;
        } else if ($model->field["INPUT_DIV"] == 4) {
            //総合所見
            $kindNo         = "03";
            $kindSeqArray   = array("004");
            $divArray       = array("1");
            $mojiGyouArray   = array('40-25');
            $model->colName = "TOTALREMARK";
        }
        if ($subTorikomiFlg) {
            $arg["DIV3_FLG"] = "1";
        } else {
            $arg["NOT_DIV3_FLG"] = "1";
        }

        //「所見等データ」と「項目名」のキーの対応　※この2つは取得テーブルが異なるので各テーブルのキーを連想配列で対応させておく
        $model->remarkToItemName = array();
        $model->remarkToItemName = array_combine(($model->field["INPUT_DIV"] != 3) ? $divArray : $seqArray, $kindSeqArray);

        //項目名称セット
        $model->itemNameArr = array();
        $query = knjd428bQuery::getItemName($model, $kindNo, $kindSeqArray);
        $result = $db->query($query);
        $tmpItemNameArr = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["koumoku"][] = $row;
            $model->itemNameArr[$row["KIND_SEQ"]] = $row["KIND_REMARK"];
        }

        // ソート順変更の場合、ソートを入れ替える
        if ($model->cmd == "sort") {
            if ($model->sort == "SUBCLASSCD") {
                $model->sort = "SEMESTER";
            } else {
                $model->sort = "SUBCLASSCD";
            }
        }

        // 学期 ソート順
        /* Edit by HPA for current_cursor start 2020/01/20 */
        if ($model->sort == "SUBCLASSCD") {
            $arg["LABEL"] = "リストを学期によって降順で並べ替えました";
            $arg["SEMESTER_TITLE"] = "<a style=\"color: white;\" href=\"#\" id = \"sortDes\" onclick=\"current_cursor('sortDes');return btn_submit('sort')\">学期 ▼</a>";
        } else {
            $arg["LABEL"] = "リストを学期によって昇順で並べ替えました";
            $arg["SEMESTER_TITLE"] = "<a style=\"color: white;\" href=\"#\" id = \"sortAsc\" onclick=\"current_cursor('sortAsc');return btn_submit('sort')\">学期 ▲</a>";
        }
        /* Edit by HPA for current_cursor end 2020/01/31 */
        knjCreateHidden($objForm, "SORT", $model->sort);

        /************/
        /* 履歴一覧 */
        /************/
        if ($model->field["INPUT_DIV"] != 3) {
            $query = knjd428bQuery::getList($model, $model->colName, $divArray);
        } else {
            $query = knjd428bQuery::getList2($model, $model->colName, $seqArray);
        }
        makeList($arg, $db, $model, $query);

        /************/
        /*  入力欄  */
        /************/
        //警告メッセージを表示しない場合
        $warning = false;
        if ((isset($model->schregno) && !isset($model->warning) && $model->cmd != "set") || !isset($model->schregno)) {
            if ($model->field["INPUT_DIV"] != 3) {
                $query = knjd428bQuery::getHreportSchregDat($model, $model->colName, $divArray);
            } else {
                $query = knjd428bQuery::getHreportSchregSubclassDat($model, $seqArray);
            }
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $remarkKey   = ($model->field["INPUT_DIV"] != 3) ? "DIV" : "SEQ";
                $itemNameKey = $model->remarkToItemName[$row[$remarkKey]];
                $Row["REMARK_{$itemNameKey}"] = $row[$model->colName];
            }
        } else {
            $Row =& $model->field;
            if (isset($model->warning)) $warning = true;
        }

        //テキストセット
        $outcnt = 0;
        $model->mojiGyouData = array_combine($kindSeqArray, $mojiGyouArray);
        foreach($model->itemNameArr as $kindSeq => $itemName) {
            $tmpData = array();

            //textArea
            $setName = "REMARK_{$kindSeq}";
            $mojiGyou = explode("-", $model->mojiGyouData[$kindSeq]);
            $moji = $mojiGyou[0];
            $gyou = $mojiGyou[1];
            /* Edit by HPA for PC-talker 読み start 2020/01/20 */
            $extra = "aria-label = \"$name1 全角{$moji}文字X{$gyou}行まで\" id =\"{$setName}\"";
            /* Edit by HPA for PC-talker 読み end 2020/01/31 */
            $tmpData["REMARK"] = knjCreateTextArea($objForm, $setName, $gyou, $moji * 2, "", $extra, $Row[$setName]);           //メモ　sql側ではtotalremark等にREMARK_1 という別名を使ってここに合わせる
            $tmpData["EXTFMT"] = "<BR><font size=2, color=\"red\">(全角{$moji}文字X{$gyou}行まで)</font>";
            $outcnt++;

            $arg["data2"][] = $tmpData;
        }

        $cnt = get_count($model->itemNameArr) + 1;
        $arg["COLSPAN"] = ($cnt > 0) ? "colspan=\"{$cnt}\"" : "";

        /**********/
        /* ボタン */
        /**********/
        //指導計画取込
        /* Edit by HPA for current_cursor start 2020/01/20 */
        $extra = "id= \"guidance_copy\" onclick=\"current_cursor('guidance_copy');return btn_submit('guidance_copy');\"";
        /* Edit by HPA for current_cursor end 2020/01/31 */
        $arg["button"]["btn_guidance_copy"] = knjCreateBtn($objForm, "btn_update", "指導計画取込", $extra);
        //全科目取込
        /* Edit by HPA for current_cursor start 2020/01/20 */
        $extra = "id= \"all_copy\" onclick=\"current_cursor('all_copy');return btn_submit('all_copy');\"";
        /* Edit by HPA for current_cursor end 2020/01/31 */
        $arg["button"]["btn_all_copy"] = knjCreateBtn($objForm, "btn_update", "全科目取込", $extra);
        //指定科目取込
        /* Edit by HPA for current_cursor start 2020/01/20 */
        $extra = "id = \"sub_copy\" onclick=\"current_cursor('sub_copy');return btn_submit('sub_copy');\"";
        /* Edit by HPA for current_cursor end 2020/01/31 */
        $arg["button"]["btn_sub_copy"] = knjCreateBtn($objForm, "btn_update", "指定科目取込", $extra);

        //更新
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "id = \"update\" aria-label = \"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        /* Edit by HPA for PC-talker 読み start and current_cursor 2020/01/20 */
        $extra = "id = \"delete\" aria-label = \"削除\" onclick=\"current_cursor('delete');return btn_submit('delete');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "id = \"clear\" aria-label = \"取消\" onclick=\"current_cursor('clear');return btn_submit('clear');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = " aria-label = \"終了\" onclick=\"closeWin();\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "SEMESTERCOPY", $model->semestercopy);
        knjCreateHidden($objForm, "SUBCLASSCOPY", $model->subclasscdcopy);
        knjCreateHidden($objForm, "KEY_DATA", implode(',', ($model->field["INPUT_DIV"] != 3) ? $divArray : $seqArray));

        //印刷用
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "knjd428b");
        knjCreateHidden($objForm, "YEAR", $model->exp_year);
        knjCreateHidden($objForm, "useGradeKindCompGroupSemester", $model->Properties["useGradeKindCompGroupSemester"]);
        knjCreateHidden($objForm, "SELECT_GHR");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knjd428bForm1.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, &$model, $query) {
    $div3Flg = $model->field["INPUT_DIV"] == 3 ? true : false;
    $model->semestercopy = "";
    $model->subclasscdcopy = "";

    $setArr = $remarkArr = array();
    $result = $db->query($query);
    $befKey = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $setKey = (!$div3Flg) ? $row["SEMESTER"] : $row["SEMESTER"].'-'.$row["SET_SUBCLASSCD"];

        if ($setKey != $befKey) {
            $remarkArr = array();
        }

        $remarkArr["SEMESTER"]     = $row["SEMESTER"];
        $remarkArr["SEMESTERNAME"]     = $row["SEMESTERNAME"];
        if ($div3Flg) {
            $remarkArr["SET_SUBCLASSCD"]   = $row["SET_SUBCLASSCD"];
            $remarkArr["SET_SUBCLASSNAME"] = $row["SET_SUBCLASSNAME"];
        }
        $remarkKey   = ($model->field["INPUT_DIV"] != 3) ? "DIV" : "SEQ";
        $itemNameKey = $model->remarkToItemName[$row[$remarkKey]];
        $remarkArr["REMARK_{$itemNameKey}"] = $row["REMARK"];

        $setArr[$setKey] = $remarkArr;

        // データ登録済みフラグ設定
        if ($model->selectSemes == $row["SEMESTER"]) {
            $model->semestercopy = "1";
            if ($div3Flg && $model->subclasscd == $row["SET_SUBCLASSCD"]) {
                $model->subclasscdcopy = "1";
            }
        }
        $befKey = $setKey;
    }

    //データセット
    $setData = array();
    foreach ($setArr as $key => $remarkArr) {
        if ($div3Flg) {
            list($semester, $classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $key);
        }
        //初期化
        $setData = array();

        //学期名
        $setData["SEMESTERNAME"] = $remarkArr["SEMESTERNAME"];

        //リンクセット
        /* Edit by HPA for current_cursor start 2020/01/20 */
        if (!$div3Flg) {
            $linkText = $model->inputDivName[$model->field["INPUT_DIV"]];
            $reqParameter         = "&SEMESTER={$remarkArr["SEMESTER"]}&SORT={$model->sort}&INPUT_DIV={$model->field["INPUT_DIV"]}&SELECT_SEMES={$remarkArr["SEMESTER"]}\" onclick =\"current_cursor('SUBCLASSCD');\" ";
            $setData["SET_NAME"] = "<a href=\"knjd428bindex.php?cmd=list_set{$reqParameter}>{$linkText}</a>";
        } else {
            $reqParameter        = "&SEMESTER={$semester}&SUBCLASSCD={$remarkArr["SET_SUBCLASSCD"]}&SORT={$model->sort}&INPUT_DIV={$model->field["INPUT_DIV"]}&SELECT_SEMES={$remarkArr["SEMESTER"]}\" onclick =\"current_cursor('SUBCLASSCD');\" ";
            $setData["SET_NAME"] = "<a href=\"knjd428bindex.php?cmd=list_set{$reqParameter}>{$remarkArr["SET_SUBCLASSNAME"]}</a>";
        }
        /* Edit by HPA for current_cursor end 2020/01/31 */

        //項目セット
        $miniData = array();
        foreach ($model->itemNameArr as $key2 => $val2) {
            $setRmrkKey           = "REMARK_{$key2}";
            $miniData["REMARK"]   = $remarkArr[$setRmrkKey];
            $setData["koumoku"][] = $miniData;
        }

        $arg["list"][] = $setData;
    }
    return;
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
function makeRadio(&$objForm, &$arg, $model, $name, $value, $disabledFlg) {
    $opt = array(1, 2);
    $extra = array();
    $disabled = ($disabledFlg) ? " disabled " : "";
    foreach($opt as $key => $val) {
        array_push($extra, $disabled." id=\"{$name}{$val}\" onchange=\"return btn_submit('edit');\" ");
    }
    if(!$disabledFlg && $value == "") {
        $value = $opt[0];
        $model->field[$name] = $value;
    }
    $radioArray = knjCreateRadio($objForm, "{$name}", $value, $extra, $opt, get_count($opt));
    foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
}
?>
