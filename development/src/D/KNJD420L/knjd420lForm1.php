<?php

require_once('for_php7.php');
class knjd420lForm1 {
    function main(&$model) {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjd420lindex.php", "", "edit");

        if($model->schregno != "" && $model->name != ""){
            $arg["TITLE"] = "".$model->schregno."". $model->name."の情報画面";
        }else{
            $arg["TITLE"] = "右情報画面";
        }

        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //履修教科・科目グループ取得
        $query = knjd420lQuery::getGradeKindSchregGroupDat($model);
        $model->schregInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //履修教科・科目グループ名設定
        $arg["GROUPNAME"] = '履修科目グループ未設定';
        if ($model->schregInfo["GROUPNAME"]) {
            $arg["GROUPNAME"] = '履修科目グループ:'.$model->schregInfo["GROUPNAME"];
        }
        //状態区分名
        $arg["CONDITION_NAME"] = "";
        if ($model->schregInfo["CONDITION_NAME"]) {
            $arg["CONDITION_NAME"] = '('.$model->schregInfo["CONDITION_NAME"].')';
        }

        //枠数取得
        $query = knjd420lQuery::getHreportConditionDat($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //３枠表示
        if ($row["REMARK1"] == "103") {
            $model->maxRemarkCnt = 3;
        } else if ($row["REMARK1"] == "102") {
            $model->maxRemarkCnt = 2;
        } else {
            $model->maxRemarkCnt = 1;
        }

        //項目名称取得
        $model->itemNameArr = array();
        $query = knjd420lQuery::getGuidanceItemName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->maxRemarkCnt == 3) {
                //3枠パターン
                for ($i=1; $i <= $model->maxRemarkCnt; $i++) {
                    if ($row["ITEM_REMARK".$i] != '') {
                        $row["ITEM_REMARK"] = $row["ITEM_REMARK".$i];
                        $arg["koumoku"][] = $row;
                        $model->itemNameArr[$i] = $row["ITEM_REMARK"];
                    }
                }
            } else if ($model->maxRemarkCnt == 2) {
                //2枠パターン
                if ($row["ITEM_REMARK1"] != '') {
                    $row["ITEM_REMARK"] = $row["ITEM_REMARK1"];
                    $arg["koumoku"][] = $row;
                    $model->itemNameArr[1] = $row["ITEM_REMARK"];
                }
                if ($row["ITEM_REMARK3"] != '') {
                    $row["ITEM_REMARK"] = $row["ITEM_REMARK3"];
                    $arg["koumoku"][] = $row;
                    $model->itemNameArr[3] = $row["ITEM_REMARK"];
                }
            } else {
                //1枠パターン
                if ($row["ITEM_REMARK3"] != '') {
                    $row["ITEM_REMARK"] = $row["ITEM_REMARK3"];
                    $arg["koumoku"][] = $row;
                    $model->itemNameArr[3] = $row["ITEM_REMARK"];
                }
            }
        }
        $result->free();

        //学期コンボ
        $query = knjd420lQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //科目コンボ
        $query = knjd420lQuery::getSubclass($model);
        $extra = "id=\"SUBCLASSCD\" aria-label=\"教科・科目\" onChange=\"current_cursor('SUBCLASSCD');return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1);

        //単元コンボ
        $query = knjd420lQuery::getUnit($model, $model->subclasscd, "");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($row) {
            $extra = "id=\"UNITCD\" aria-label=\"単元\" onChange=\"current_cursor('UNITCD');return btn_submit('edit');\"";
            // $extra = "id=\"UNITCD\" aria-label=\"単元\" onChange=\"current_cursor('UNITCD');";
            makeCmb($objForm, $arg, $db, $query, $model->unitcd, "UNITCD", $extra, 1);
        } else {
            $arg["UNITCD"] = "単元は設定されていません。";
            $model->unitcd = "00";
            knjCreateHidden($objForm, "UNITCD", "00");
        }

        /************/
        /* 履歴一覧 */
        /************/
        makeList($objForm, $arg, $db, $model);

        //項目内容取得
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $result = $db->query(knjd420lQuery::getHreportTokushiSchregSubclassDat($model, $model->subclasscd, $model->unitcd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["REMARK_{$row["SEQ"]}"] = $row["REMARK"];
            }
        } else {
            $Row =& $model->field;
        }

        $outcnt = 0;
        //テキストセット
        foreach ($model->itemNameArr as $key => $value) {
            $textData = array();
            //テキストエリア作成
            $remarkName = "REMARK_{$key}";

            $moji = $model->paternInfo[$model->maxRemarkCnt][$key]["MOJI"];
            $gyou = $model->paternInfo[$model->maxRemarkCnt][$key]["GYOU"];

            $extra = "aria-label=\"$value 全角{$moji}文字X{$gyou}行まで\" id=\"{$remarkName}\" ";
            $textData["REMARK"] = knjCreateTextArea($objForm, $remarkName, $gyou, $moji * 2, "", $extra, $Row[$remarkName]);
            $textData["REMARK_SIZE"] = "<font size=2, color=\"red\">(全角{$moji}文字X{$gyou}行まで)</font>";

            $arg["data2"][] = $textData;
        }

        $cnt = get_count($model->itemNameArr);
        $arg["COLSPAN"] = ($cnt > 0) ? "colspan=\"{$cnt}\"" : "";

        /**********/
        /* ボタン */
        /**********/
        //リスト削除
        $extra = "id = \"listdelete\" aria-label = \"削除\" onclick=\"current_cursor('listdelete');return btn_submit('listdelete');\"";
        $arg["button"]["btn_listdelete"] = knjCreateBtn($objForm, "btn_listdelete", "削 除", $extra);

        //全科目取込
        $extra = "id= \"allcopy\" onclick=\"current_cursor('allcopy');return btn_submit('allcopy');\"";
        $arg["button"]["btn_allcopy"] = knjCreateBtn($objForm, "btn_update", "全科目取込", $extra);
        //指定科目取込
        $extra = "id = \"copy\" onclick=\"current_cursor('copy');return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_update", "指定科目取込", $extra);

        //更新
        $extra = "id = \"update\" aria-label = \"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "id = \"delete\" aria-label = \"削除\" onclick=\"current_cursor('delete');return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消
        $extra = "id = \"clear\" aria-label = \"取消\" onclick=\"current_cursor('clear');return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = " aria-label = \"終了\" onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "DEL_LIST");

        knjCreateHidden($objForm, "SEMESTERCOPY", $model->semestercopy);
        knjCreateHidden($objForm, "SUBCLASSCOPY", $model->subclasscdcopy);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knjd420lForm1.html", $arg);
    }
}

//履歴一覧
function makeList(&$objForm, &$arg, $db, &$model) {

    $model->semestercopy = "";
    $model->subclasscdcopy = "";


    //科目リスト取得
    $query = knjd420lQuery::getSubclass($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $subclassList[$row["VALUE"]] = $row;
    }
    $result->free();

    //単元リスト取得
    $unitList = array();
    $query = knjd420lQuery::getUnit($model, "", "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $unitList[$row["SUBCLASSCD"]][$row["VALUE"]] = $row;
    }
    $result->free();

    $dataArray = array();
    $remarkArr = array();
    $query = knjd420lQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $key = $row["SEMESTER"].'-'.$row["SUBCLASSCD"].'-'.$row["UNITCD"];

        $dataArray[$key]["SEMESTER"]     = $row["SEMESTER"];
        $dataArray[$key]["SEMESTERNAME"] = $row["SEMESTERNAME"];
        $dataArray[$key]["SUBCLASSCD"]   = $row["SUBCLASSCD"];
        $dataArray[$key]["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
        $dataArray[$key]["UNITCD"]       = $row["UNITCD"];

        $dataArray[$key]["REMARK".$row["SEQ"]] = $row["REMARK"];


        // データ登録済みフラグ設定
        if ($model->semester == $row["SEMESTER"]) {
            $model->semestercopy = "1";
            if ($model->subclasscd == $row["SUBCLASSCD"]) {
                $model->subclasscdcopy = "1";
            }
        }
    }
    $result->free();

    //データセット
    foreach ($dataArray as $key => $value) {
        list($semester, $classCd, $schoolKind, $curriculumCd, $subclassCd, $unitCd) = preg_split("/-/", $key);

        //初期化
        $list = array();
        //削除用チェックボックス
        $extra = "";
        $list["DEL_CHECK"] = knjCreateCheckBox($objForm, "DEL_CHECK", $key, $extra, "");

        //単元名
        $unitName = "";
        if ($unitList[$value["SUBCLASSCD"]][$value["UNITCD"]]) {
            $unitName = "(".$unitList[$value["SUBCLASSCD"]][$value["UNITCD"]]["LABEL"].")";
        }

        $isLink = false;
        if ($subclassList[$value["SUBCLASSCD"]]) {
            if ($value["UNITCD"] == "00") {
                if (get_count($unitList[$value["SUBCLASSCD"]]) == 0) {
                    $isLink = true;
                }
            } else {
                if ($unitList[$value["SUBCLASSCD"]][$value["UNITCD"]]) {
                    $isLink = true;
                }
            }
        }

        if ($isLink) {
            //リンクセット
            $list["SUBCLASSNAME"] = View::alink("knjd420lindex.php",
                                                $value["SUBCLASSNAME"].$unitName,
                                                "onclick =\"current_cursor('SUBCLASSCD');\"",
                                                array("cmd"             => "list_set"
                                                    , "SEMESTER"        => $value["SEMESTER"]
                                                    , "SUBCLASSCD"      => $value["SUBCLASSCD"]
                                                    , "UNITCD"          => $value["UNITCD"]
                                            ));

        } else {
            $list["SUBCLASSNAME"] = $value["SUBCLASSNAME"].$unitName;
        }

        //学期名
        $list["SEMESTERNAME"] = $value["SEMESTERNAME"];
        //項目セット
        foreach ($model->itemNameArr as $key2 => $val2) {
            $remarkData = array();
            $remarkData["REMARK"] = $value["REMARK".$key2];
            $list["koumoku"][] = $remarkData;
        }
        $arg["list"][] = $list;
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
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
