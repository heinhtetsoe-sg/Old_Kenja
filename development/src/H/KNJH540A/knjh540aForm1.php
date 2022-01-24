<?php

require_once('for_php7.php');

class knjh540aForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh540aindex.php", "", "edit");

        /* 年度 */
        $arg["YEAR"] = CTRL_YEAR;
        $db = Query::dbCheckOut();

        /* 学期 */
        $query = knjh540aQuery::getSemester();
        $extra = "onChange=\"btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1);

        /* コピー */
        $extra = "style=\"width:130px\" onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        /* 実力テスト区分 */
        $query = knjh540aQuery::getProficiencyDiv();
        $extra = "onChange=\"btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYDIV"], "PROFICIENCYDIV", $extra, 1, "BLANK");

        /* 実力テストコード */
        if ($model->field["PROFICIENCYDIV"]) {
            $query = knjh540aQuery::getProficiencycd($model);
        } else {
            $query = "";
        }
        $extra = "onChange=\"btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYCD"], "PROFICIENCYCD", $extra, 1, "BLANK");

        /* 実力テストコード */ //※コピー元
        if ($model->field["PROFICIENCYDIV"]) {
            $query = knjh540aQuery::getProficiencycd($model);
        } else {
            $query = "";
        }
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYCD_FROMCOPY"], "PROFICIENCYCD_FROMCOPY", $extra, 1, "BLANK");

        $extra = "style=\"width:130px\" onclick=\"return btn_submit('fromcopy');\"";
        $arg["btn_fromcopy"] = knjCreateBtn($objForm, "btn_fromcopy", "左記からコピー", $extra);

        /****************/
        /* リスト内表示 */
        /****************/
        $query  = knjh540aQuery::getListdata($model);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //権限チェック
            if($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT){
                break;
            }

            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            //リンク作成
            $row["SUBCLASS_NAME"] = View::alink("knjh540aindex.php", $row["SUBCLASS_NAME"], "target=\"right_frame\"",
                                              array("cmd"                      => "edit",
                                                    "SEMESTER"                 => $row["SEMESTER"],
                                                    "PROFICIENCYDIV"           => $row["PROFICIENCYDIV"],
                                                    "PROFICIENCYCD"            => $row["PROFICIENCYCD"],
                                                    "PROFICIENCY_SUBCLASS_CD"  => $row["PROFICIENCY_SUBCLASS_CD"],
                                                    "DIV"                      => $row["DIV"],
                                                    "GRADE"                    => $row["GRADE"],
                                                    "COURSECD"                 => $row["COURSECD"],
                                                    "MAJORCD"                  => $row["MAJORCD"],
                                                    "COURSECODE"               => $row["COURSECODE"],
                                                    "PERFECT"                  => $row["PERFECT"],
                                                    "PASS_SCORE"               => $row["PASS_SCORE"],
                                                    "WEIGHTING"                => $row["WEIGHTING"]
                                                    ));

            if ($row["GRADE"] == "00") {
                $row["GRADE"] = "";
            }
            if ($row["COURSECD"] == "0") {
                $row["COURSECD"] = "";
                $row["MAJORCD"] = "";
                $row["COURSECODE"] = "";
            } else {
                $row["COURSECD_MAJORCD"] = "({$row["COURSECD"]}{$row["MAJORCD"]})";
                $row["COURSECODE"] = "({$row["COURSECODE"]})";
            }

            $arg["data"][] = $row;
        }
        $result->free();

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //権限
        if($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT){
            $arg["Closing"]  = " closing_window(); " ;
        }
        if ($model->cmd == "change" || VARS::post("cmd") == "list"){
            $arg["reload"] = "window.open('knjh540aindex.php?cmd=edit2&SEMESTER={$model->semester}&TEST={$model->test}','right_frame');";
        }

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh540aForm1.html", $arg);
    }
}
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($query) {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        if ($name == "SEMESTER") {
            $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }

        $result->free();
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
