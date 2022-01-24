<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knja050hForm1
{
    public function main($model)
    {
        $objForm = new form();

        //DB接続
        $db  = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        //動作条件チェック
        } elseif ($db->getOne(knja050hQuery::checktoStart()) == "0") {
            $arg["check"] = "Show_ErrMsg(1);";
        }

        //初期化
        if ($model->cmd == "schoolKind") {
            unset($model->field["GUARD_ISSUEDATE"]);
            unset($model->field["GUARD_EXPIREDATE"]);
        }

        //対象年度コンボボックス
        $opt_year   = array();
        $opt_year[] = array("label" => (CTRL_YEAR),     "value" => CTRL_YEAR);
        $opt_year[] = array("label" => (CTRL_YEAR + 1), "value" => (CTRL_YEAR + 1));
        $extra = "onChange=\"return btn_submit('schoolKind');\"";
        $model->field["YEAR"] = ($model->field["YEAR"] == "") ? substr(CTRL_DATE, 0, 4): $model->field["YEAR"];
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, $extra, 1);

        // 対象学期コンボ
        $query = knja050hQuery::getSemester($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        // 前回実行日付
        $arg["data"]["NOWDATE"]  = knja050hQuery::getMaxUpdate($db);

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //対象データラジオボタン 1:入学者 2:保護者
        $opt = array(1, 2);
        $model->field["DATADIV"] = ($model->field["DATADIV"] == "") ? "1" : $model->field["DATADIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"DATADIV{$val}\" onClick=\"shorimei_show(this)\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATADIV", $model->field["DATADIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /************/
        /** コンボ **/
        /************/

        //校種
        $query = knja050hQuery::getA023($model);
        $extra = "onChange=\"return btn_submit('schoolKind');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //処理名コンボボックス
        $opt_shori   = array();
        $opt_shori[] = array("label" => "更新","value" => "1");
        $opt_shori[] = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //課程学科コンボ
        $disCourse = ($model->field["DATADIV"] == "2") ? " disabled": "";
        $query = knja050hQuery::getCourseMajor(($model->field["YEAR"]));
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->coursemajor, "COURSEMAJOR", $extra.$disCourse, 1, "");

        //学校マスタ取得
        $query = knja050hQuery::getSchoolMst($model);
        $schData = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //開始日
        $disabled = "";
        $sDate = str_replace("-", "/", $schData["ENTRANCE_DATE"]);
        $value = $model->field["GUARD_ISSUEDATE"] ? $model->field["GUARD_ISSUEDATE"] : $sDate;
        $arg["data"]["GUARD_ISSUEDATE"] = View::popUpCalendarAlp($objForm, "GUARD_ISSUEDATE", $value, $disabled);

        //終了日
        $disabled = "";
        if ($model->field["GUARD_EXPIREDATE"]) {
            $value = $model->field["GUARD_EXPIREDATE"];
        } else {
            $eDate = preg_split("/-/", $schData["GRADUATE_DATE"]);
            $query = knja050hQuery::getA023GradeRange($model->schoolKind);
            $addYear = $db->getOne($query);
            $value = ((int)$eDate[0] + (int)$addYear)."/03/31";
        }
        $arg["data"]["GUARD_EXPIREDATE"] = View::popUpCalendarAlp($objForm, "GUARD_EXPIREDATE", $value, $disabled);

        //ヘッダ有無
        $extra = "checked id=\"HEADERCHECK\"";
        $arg["data"]["HEADERCHECK"] = knjCreateCheckBox($objForm, "HEADERCHECK", "1", $extra);

        //ファイルからの取り込み
        $extra = "";
        $arg["data"]["FILE"] = knjCreateFile($objForm, "FILE", 2048000, $extra);

        /************/
        /** ボタン **/
        /************/
        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["BTN_OK"] = knjcreateBtn($objForm, "btn_ok", "実 行", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_CLEAR"] = knjcreateBtn($objForm, "btn_cancel", "終 了", $extra);

        /***********/
        /** hidden */
        /***********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knja050hindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja050hForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
