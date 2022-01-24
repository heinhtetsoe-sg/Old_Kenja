<?php

require_once('for_php7.php');

class knjd420qForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd420qindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        $query = knjd420qQuery::getGuidancePattern($model);
        $result = $db->query($query);

        //作成者取得
        $extra = " id=\"STAFFNAME\" ";
        $model->staffname = ($model->cmd != 'chgSub' && $model->cmd != 'chgChair') ? $model->staffname : "";
        $arg["STAFFNAME"] = knjCreateTextBox($objForm, $model->staffname, "STAFFNAME", 50, 50, $extra);
        knjCreateHidden($objForm, "STAFFNAME_KETA", (25 * 2));
        knjCreateHidden($objForm, "STAFFNAME_GYO", 1);
        KnjCreateHidden($objForm, "STAFFNAME_STAT", "statusarea_STAFFNAME");

        //学期コンボ
        $query = knjd420qQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        $model->semester = ($model->semester) ? $model->semester : CTRL_SEMESTER;
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //科目コンボ
        $query = knjd420qQuery::getSubclass($model);
        $extra = "onChange=\"return btn_submit('chgSub');\"";
        makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1);

        //講座コンボ
        $query = knjd420qQuery::getChairCd($model);
        $extra = "onChange=\"return btn_submit('chgChair');\"";
        makeCmb($objForm, $arg, $db, $query, $model->chaircd, "CHAIRCD", $extra, 1);

        //項目名称セット
        $model->itemNameArr = array();
        $query = knjd420qQuery::getItemName($model);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["ITEM_REMARK"] != '') {
                $arg["koumoku"][] = $row;
                $model->itemNameArr[$row["ITEM_NO"]] = $row["ITEM_REMARK"];
            }
        }

        //講座ごとの生徒一覧取得
        $model->schInfo = array();
        $query = knjd420qQuery::getChairSchreg($model);
        $result = $db->query($query);
        while ($schRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //テキストセット
            if ($schRow["GUIDANCE_PATTERN"] && $model->subclasscd) {
                $outcnt = 1;
                $data2 = array();
                foreach ($model->itemNameArr as $itemNo => $item_remark) {
                    $tmpData = array();

                    //textArea
                    $setName = "REMARK_{$schRow["SCHREGNO"]}_{$itemNo}";
                    $extra = "id=\"{$setName}\"";
                    $moji = 10;
                    $gyou = 25;
                    $tmpData["REMARK"] = knjCreateTextArea($objForm, $setName, "10", $moji * 2, "", $extra, $schRow["REMARK_{$itemNo}"]);
                    $tmpData["EXTFMT"] = "<BR><font size=2, color=\"red\">(全角{$moji}文字X{$gyou}行まで)</font>";
                    knjCreateHidden($objForm, "{$setName}_KETA", ($moji * 2));
                    knjCreateHidden($objForm, "{$setName}_GYO", $gyou);
                    KnjCreateHidden($objForm, "{$setName}_STAT", "statusarea_".$schRow["SCHREGNO"]."_".$outcnt);
                    $outcnt++;
                    $data2["data2"][] = $tmpData;
                }
                $chkIdName = "CHKBOX_{$schRow["SCHREGNO"]}";
                $data2["CHKBOXID"] = $chkIdName;
                $extra = " id=\"{$chkIdName}\" ";
                $data2["CHKBOX"] = knjCreateCheckBox($objForm, "CHKBOX_".$schRow["SCHREGNO"], $schRow["SCHREGNO"], $extra);
                $data2["NAME"]   = $schRow["NAME"];
                $arg["list"][] = $data2;
                $model->schInfo[$schRow["SCHREGNO"]] = $schRow;
            }
        }

        $cnt = get_count($model->itemNameArr) + 2;
        $arg["COLSPAN"] = ($cnt > 0) ? "colspan=\"{$cnt}\"" : "";
        /**********/
        /* ボタン */
        /**********/

        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "SEMESTERCOPY", $model->semestercopy);
        knjCreateHidden($objForm, "SUBCLASSCOPY", $model->subclasscdcopy);

        //印刷用
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "knjd420q");
        knjCreateHidden($objForm, "YEAR", $model->exp_year);
        knjCreateHidden($objForm, "useGradeKindCompGroupSemester", $model->Properties["useGradeKindCompGroupSemester"]);
        knjCreateHidden($objForm, "SELECT_GHR");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd420qForm1.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, &$model) {

    $model->semestercopy = "";
    $model->subclasscdcopy = "";

    $setArr = $remarkArr = array();
    $query = knjd420qQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setKey = $row["SEMESTER"].'-'.$row["SET_SUBCLASSCD"].'-'.$row["UNITCD"];

        $remarkArr["SEMESTERNAME"]     = $row["SEMESTERNAME"];
        $remarkArr[$row["SEQ"]]        = $row["REMARK"];
        $remarkArr["UNITCD"]           = $row["UNITCD"];
        $remarkArr["SET_SUBCLASSCD"]   = $row["SET_SUBCLASSCD"];
        $remarkArr["SET_SUBCLASSNAME"] = $row["SET_SUBCLASSNAME"];
        $setArr[$setKey] = $remarkArr;

        // データ登録済みフラグ設定
        if ($model->semester == $row["SEMESTER"]) {
            $model->semestercopy = "1";
            if ($model->subclasscd == $row["SET_SUBCLASSCD"]) {
                $model->subclasscdcopy = "1";
            }
        }
    }

    //データセット
    $setData = array();
    foreach ($setArr as $key => $remarkArr) {
        list($semester, $classCd, $schoolKind, $curriculumCd, $subclassCd, $unitCd) = preg_split("/-/", $key);

        //初期化
        $setData = array();

        //学期名
        $setData["SEMESTERNAME"] = $remarkArr["SEMESTERNAME"];

        //リンクセット
        $setData["SET_SUBCLASSNAME"] = "<a href=\"knjd420qindex.php?cmd=list_set&SEMESTER={$semester}&SUBCLASSCD={$remarkArr["SET_SUBCLASSCD"]}&UNITCD={$remarkArr["UNITCD"]}&SORT={$model->sort}\">{$remarkArr["SET_SUBCLASSNAME"]}</a>";

        //項目セット
        $miniData = array();
        foreach ($model->itemNameArr as $key2 => $val2) {
            $miniData["REMARK"] = $remarkArr[$key2];
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
?>
