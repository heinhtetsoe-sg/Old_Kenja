<?php

require_once('for_php7.php');

class knjz352cForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjz352cForm1", "POST", "knjz352cindex.php", "", "knjz352cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjz352cQuery::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('knjz352c')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->school_kind, $extra, 1, "");
        } else {
            $model->school_kind = '00';
            if ($model->Properties["useSchool_KindField"] == "1") {
                $model->school_kind = SCHOOLKIND;
            }
        }

        //前年度データ件数
        $pre_year = CTRL_YEAR - 1;
        $preYear_cnt = $db->getOne(knjz352cQuery::getCopyData($pre_year, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preYear_cnt);
        //今年度データ件数
        $this_year = CTRL_YEAR;
        $thisYear_cnt = $db->getOne(knjz352cQuery::getCopyData($this_year, "cnt"));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisYear_cnt);
        
        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //区分コンボ
        $opt   = array();
        $opt[] = array("label" => "クラス別",   "value" => "1");
        $opt[] = array("label" => "講座別",     "value" => "2");
        if ($model->field["ATTEND_DIV"] == "") $model->field["ATTEND_DIV"] = "1";
        $extra = " onChange=\"return btn_submit('knjz352c');\"";
        $arg["ATTEND_DIV"] = knjCreateCombo($objForm, "ATTEND_DIV", $model->field["ATTEND_DIV"], $opt, $extra, 1);

        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //対象課程学科コンボ
            $extra = "onChange=\"btn_submit('knjz352c')\";";
            $query = knjz352cQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1);
            $arg["use_COURSE_MAJOR"] = 1;
        } else {
            $arg["unuse_COURSE_MAJOR"] = 1;
        }

        //ADMIN_CONTROL_ATTEND_ITEMNAME_DATから項目名取得
        $itemName = array();
        $query = knjz352cQuery::getAdminControlAttendItemnameDat($model, $model->field["ATTEND_DIV"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemName[$row["ATTEND_ITEM"]] = $row["ATTEND_ITEMNAME"];
        }

        //一覧表示
        $colorFlg = false;
        foreach ($model->item_array[$model->field["ATTEND_DIV"]] as $key => $val) {
            //出欠項目
            $row["ATTEND_ITEM"]     = $val["value"];
            $row["DEFAULT_NAME"]    = $val["label"];

            //変更後テキストボックス
            $value = (!isset($model->warning)) ? $itemName[$val["value"]] : $model->fields["ATTEND_ITEMNAME"][$val["value"]];
            $extra = "";
            $row["ATTEND_ITEMNAME"] = knjCreateTextBox($objForm, $value, "ATTEND_ITEMNAME-".$val["value"], 20, 20, $extra);

            //背景色
            $colorFlg = !$colorFlg;
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz352cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    if ($blank == "ALL")   $opt[] = array('label' => "-- 全て --", 'value' => "ALL");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
