<?php

require_once('for_php7.php');

class knjz237vForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz237vindex.php", "", "edit");

        //権限チェック
        if (AUTHORITY < DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //Z010より適用学校情報取得
        $query = knjz237vQuery::getNameMst();
        $getname = $db->getOne($query);

        //校種
        if (get_count($model->sch_kind) > 1) {
            $extra = "onChange=\"btn_submit('list');\"";
            $arg["div"]["SCHOOL_KIND_DIV"] = knjCreateCombo($objForm, "SCHOOL_KIND_DIV", $model->field["SCHOOL_KIND_DIV"], $model->sch_kind, $extra, 1);

        } else {
            $model->field["SCHOOL_KIND_DIV"] = ($model->field["SCHOOL_KIND_DIV"] == "") ? 1 : $model->field["SCHOOL_KIND_DIV"];
            $arg["div"]["SCHOOL_KIND_DIV"] = $model->sch_kind[1]["label"];
            knjCreateHidden($objForm, "SCHOOL_KIND_DIV", $model->sch_kind[1]["value"]);
        }

        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["use_school_detail_gcm_dat"] = 1;
            $extra = "onChange=\"btn_submit('main')\";";
            $query = knjz237vQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, $model->field["COURSE_MAJOR"], "COURSE_MAJOR", $extra, 1);

            $cmCnt = get_count($db->getCol(knjz237vQuery::getCourseMajor($model)));
        }

        //学期コンボ
        $query = knjz237vQuery::getSemester();
        $extra = "onChange=\"btn_submit('list_gakki');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //テストコンボ
        $query = knjz237vQuery::getTestitemMstCountflgNewSdiv($model, $getname, $model->field["SEMESTER"], "combo");
        $extra = "onChange=\"btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTKINDCD"], "TESTKINDCD", $extra, 1, "BLANK");

        /****************/
        /* リスト内表示 */
        /****************/
        $query = knjz237vQuery::getListdata($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($row["DIV"] == "04") {
                $row["MAJORCD"] = $row["GROUP_CD"];
            }
            //リンク作成
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $row["SUBCLASSNAME"] = View::alink("knjz237vindex.php", $row["SUBCLASSNAME"], "target=\"right_frame\"",
                                                array("cmd"             => "edit",
                                                      "SCHOOL_KIND_DIV" => $model->field["SCHOOL_KIND_DIV"],
                                                      "SEMESTER"        => $row["SEMESTER"],
                                                      "TESTKINDCD"      => $row["TESTKINDCD"],
                                                      "SUBCLASSCD"      => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"],
                                                      "DIV"             => $row["DIV"],
                                                      "GRADE"           => $row["GRADE"],
                                                      "COURSECD"        => $row["COURSECD"],
                                                      "MAJORCD"         => $row["MAJORCD"],
                                                      "COURSECODE"      => $row["COURSECODE"],
                                                      "PERFECT"         => $row["PERFECT"],
                                                      "PASS_SCORE"      => $row["PASS_SCORE"]
                                                      ));
            } else {
                $row["SUBCLASSNAME"] = View::alink("knjz237vindex.php", $row["SUBCLASSNAME"], "target=\"right_frame\"",
                                                array("cmd"             => "edit",
                                                      "SEMESTER"        => $row["SEMESTER"],
                                                      "TESTKINDCD"      => $row["TESTKINDCD"],
                                                      "SUBCLASSCD"      => $row["SUBCLASSCD"],
                                                      "DIV"             => $row["DIV"],
                                                      "GRADE"           => $row["GRADE"],
                                                      "COURSECD"        => $row["COURSECD"],
                                                      "MAJORCD"         => $row["MAJORCD"],
                                                      "COURSECODE"      => $row["COURSECODE"],
                                                      "PERFECT"         => $row["PERFECT"],
                                                      "PASS_SCORE"      => $row["PASS_SCORE"]
                                                      ));
            }

            if ($row["GRADE"] == "00") {
                $row["GRADENAME"] = "";
            }
            if ($row["COURSECD"] == "0") {
                $row["COURSECD"]    = "";
                $row["MAJORCD"]     = "";
                $row["COURSECODE"]  = "";
            } else {
                $row["COURSECD_MAJORCD"] = "({$row["COURSECD"]}{$row["MAJORCD"]})";
                $row["COURSECODE"] = "({$row["COURSECODE"]})";
            }

            $arg["data"][] = $row;
        }
        $result->free();

        //コースグループ名表示
        if ($model->Properties["usePerfectCourseGroup"] == "1") {
            $arg["usePerfectCourseGroup"] = "1";
        }

        //合格点表示
        if ($model->Properties["usePerfectPass"] == "1") {
            $arg["usePerfectPass"] = "1";
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        if ($model->cmd == "change" || VARS::post("cmd") == "list") {
            $arg["reload"] = "window.open('knjz237vindex.php?cmd=edit2&SCHOOL_KIND_DIV={$model->field["SCHOOL_KIND_DIV"]}&SEMESTER={$model->field["SEMESTER"]}&TESTKINDCD={$model->field["TESTKINDCD"]}','right_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz237vForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
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
