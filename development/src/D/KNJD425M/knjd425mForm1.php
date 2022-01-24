<?php

require_once('for_php7.php');

class knjd425mForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd425mindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjd425mQuery::getSchoolKind();
        $extra = "onChange=\"return btn_submit('chgSchKind');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1);

        //学期コンボ
        $query = knjd425mQuery::getSemester();
        $extra = "onChange=\"return btn_submit('chgSeme');\"";
        $model->field["SEMESTER"] = ($model->field["SEMESTER"]) ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //科目コンボ
        $query = knjd425mQuery::getSubclass($model);
        $extra = "onChange=\"return btn_submit('chgSub');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1);

        //講座コンボ
        $query = knjd425mQuery::getChairCd($model);
        $extra = "onChange=\"return btn_submit('chgChair');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", $extra, 1);

        //項目名称セット
        $model->itemNameArr = array();
        $query = knjd425mQuery::getItemName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["koumoku"][] = $row;
            $model->itemNameArr[$row["KIND_SEQ"]] = $row["KIND_REMARK"];
        }

        $model->schInfo = array();
        if ($model->isWarning()) {
            $model->schInfo = $model->schField;
        } else {
            //講座ごとの生徒一覧取得
            $model->schInfo = array();
            $query = knjd425mQuery::getChairSchreg($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->schInfo[$row["SCHREGNO"]] = $row;
            }
        }

        foreach ($model->schInfo as $schregno => $val) {
            //テキストセット
            $outcnt = 1;
            $list = array();

            $list["NAME"] = $val["NAME"];
            knjCreateHidden($objForm, "NAME_{$schregno}", $val["NAME"]);

            foreach ($model->itemNameArr as $itemNo => $item_remark) {
                $tmpData = array();
                //textArea
                $setName = "REMARK_{$schregno}_{$itemNo}";
                $extra = "id=\"{$setName}\"";
                $tmpData["REMARK"] = knjCreateTextArea($objForm, $setName, 10, $model->textLimit[$itemNo]["moji"] * 2, "", $extra, $val["REMARK_{$itemNo}"]);
                $tmpData["EXTFMT"] = "<BR><font size=2, color=\"red\">(全角{$model->textLimit[$itemNo]["moji"]}文字X{$model->textLimit[$itemNo]["gyou"]}行まで)</font>";
                knjCreateHidden($objForm, "{$setName}_KETA", ($model->textLimit[$itemNo]["moji"] * 2));
                knjCreateHidden($objForm, "{$setName}_GYO", $model->textLimit[$itemNo]["gyou"]);
                KnjCreateHidden($objForm, "{$setName}_STAT", "statusarea_".$schregno."_".$outcnt);

                $tmpData["WIDTH"] = $itemNo == "3" ? "width=\"383px\"" : "width=\"400px\"";

                $outcnt++;
                $list["remark"][] = $tmpData;
            }
            $arg["list"][] = $list;
        }
        $cnt = get_count($model->itemNameArr) + 2;
        $arg["COLSPAN"] = ($cnt > 0) ? "colspan=\"{$cnt}\"" : "";

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd425mForm1.html", $arg);
    }
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
