<?php

require_once('for_php7.php');

class knjl432mForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->objYear;

        //校種コンボ
        $extra = "onchange=\"return btn_submit('main')\"";
        $query = knjl432mQuery::getSchoolKind($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_SCHOOL_KIND", $model->examSchoolKind, $extra, 1);

        //試験IDコンボ
        $query = knjl432mQuery::getExamId($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "EXAM_ID", $model->examId, $extra, 1, "");
        $model->wkExamId = ($model->examId != "") ? explode("-", $model->examId) : array();

        //ソート
        $opt = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //上限値・下限値
        $extra = "";
        $arg["TOP"]["LOWER_EXAM_SCORE"] = knjCreateTextBox($objForm, $model->lowerScore, "LOWER_EXAM_SCORE", 3, 3, $extra);
        $arg["TOP"]["UPPER_EXAM_SCORE"] = knjCreateTextBox($objForm, $model->upperScore, "UPPER_EXAM_SCORE", 3, 3, $extra);

        //合否コンボ
        $query = knjl432mQuery::getSettingMst($model, "L013");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "JUDGEMENT", $model->judgement, $extra, 1, "");

        //更新チェックボックス(ヘッダ)
        $extra  = " id=\"CHECKALL\" onClick=\"return check_all(this);\" ";
        // $extra .= ($model->checkAll == "1") ? " checked": "";
        $arg["HEAD"]["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "1", $extra);

        //一覧表示
        $model->keyArray = $model->examnoArray = $model->receptnoArray = $model->advanceCheckArray = array();
        $query = knjl432mQuery::selectQuery($model);
        $result = $db->query($query);
        $count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["EXAMNO"].$row["RECEPTNO"];

            //更新チェックボックス
            $extra = "id=\"CHECK_{$key}\"";
            $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK_{$key}", "1", $extra, "");

            //繰り上げ合格
            $disable  = ($row["JUDGEMENT_CD"] == "1" || $row["JUDGEMENT_CD"] == "2") ? "" : " disabled ";
            $extra = "id=\"ADVANCE_CHECK_{$key}\"";
            $extra .= ($row["ADVANCE_CHECK"] == "1") ? "checked" : "".$disable;
            $row["ADVANCE_CHECK"] = knjCreateCheckBox($objForm, "ADVANCE_CHECK_{$key}", "1", $extra, "");

            //受験者
            knjCreateHidden($objForm, "EXAMNO_{$key}", $row["EXAMNO"]);
            knjCreateHidden($objForm, "RECEPTNO_{$key}", $row["RECEPTNO"]);

            $arg["data"][] = $row;

            $model->keyArray[] = $key;
            $count++;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $count);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl432mindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl432mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $retDiv = "")
{
    $opt = array();
    $retOpt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        $retOpt[$row["VALUE"]] = $row["LABEL"];

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $retOpt;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $count)
{
    $disable  = ($count > 0) ? "" : " disabled";

    //絞り込みボタン
    $extra = "onclick=\"return btn_submit('search');\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "絞り込み", $extra);
    //全員合格ボタン
    $extra = "onclick=\"return btn_submit('allpass');\"".$disable;
    $arg["button"]["btn_allpass"] = knjCreateBtn($objForm, "btn_allpass", "全員合格", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //CSV処理ボタン
    $extra = "onclick=\"return btn_submit('form2');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV処理", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    if (count($model->keyArray) > 0) {
        knjCreateHidden($objForm, "HID_KEY", implode(",", $model->keyArray));
    } else {
        knjCreateHidden($objForm, "HID_KEY", array());
    }
}
