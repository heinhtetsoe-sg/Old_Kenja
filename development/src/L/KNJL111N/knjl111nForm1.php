<?php

require_once('for_php7.php');

class knjl111nForm1
{
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl111nindex.php", "", "edit");

        $db  = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->entexamYear;

        //入試制度
        $query = knjl111nQuery::getApplicantDiv($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試区分
        $query = knjl111nQuery::getTestDiv($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "");

        //科目
        $query = knjl111nQuery::getSubclassCd($model);
        $result = $db->query($query);
        $model->subclassCd = array();
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->subclassCd[$Row["VALUE"]] = $Row["LABEL"];
            $arg["TITLE"]["TESTSUBCLASSCD".$Row["VALUE"]] = $Row["LABEL"];
        }
        $model->subclassCd["A"] = "A";

        //専併
        $query = knjl111nQuery::getShDiv($model);
        $result = $db->query($query);
        $model->shDiv = array();
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->shDiv[$Row["VALUE"]]["NAME"] = $Row["LABEL"];
        }

        //コース
        $query = knjl111nQuery::getCourse($model);
        $result = $db->query($query);
        $course = array();
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $course[$Row["VALUE"]] = $Row["LABEL"];
        }
        foreach ($model->shDiv as $key => $val) {
            $model->shDiv[$key]["COURSE"] = $course;
        }

        //専併、コース
        $bifKey = "";
        foreach ($model->shDiv as $shDiv => $shName) {
            $model->shDiv[$shDiv]["COURSE"] = $course;
            $setRow["SH_NAME"] = $model->shDiv[$shDiv]["NAME"];
            $setRow["ROWSPAN"] = get_count($course);

            foreach ($course as $courseCd => $courseName) {
                $setRow["COURSE_NAME"] = $courseName;

                //得点
                $setAll = 0;
                foreach ($model->subclassCd as $subclassCd => $subclassName) {
                    if ($model->cmd == "recalc") {
                        $query = knjl111nQuery::getRecalc($model, $shDiv, $courseCd, $subclassCd);
                        if ($subclassCd != "A") {
                            $setScore = $db->getOne($query);
                            $setScore = $setScore * 10 / 10;
                            if (strpos($setScore, ".") === false) {
                                $setScore = $setScore.".0";
                            }
                            $setAll += $setScore;
                        } else {
                            $setScore = $setAll;
                        }
                        $setScore = $setScore == "0.0" ? "" : $setScore;
                    } else {
                        $query = knjl111nQuery::getAvg($model, $shDiv, $courseCd, $subclassCd);
                        $setScore = $db->getOne($query);
                    }
                    $soeji = $shDiv."_".$courseCd."_".$subclassCd;
                    //textbox
                    $extra = "style=\"text-align:right;\" onblur=\"this.value = toNumber(this.value)\"";
                    $setRow["SCORE".$subclassCd] = knjCreateTextBox($objForm, $setScore, "SCORE".$soeji, 5, 5, $extra);
                }
                $arg["data"][] = $setRow;
                $setRow = array();
            }
        }

        //再計算ボタン
        $extra = "onclick=\"return btn_submit('recalc');\"";
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl111nForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") {
        if ($blank == "ALL") {
            $opt[] = array("label" => "-- 全て --", "value" => "");
        } else {
            $opt[] = array('label' => "", 'value' => "");
        }
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
