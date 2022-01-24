<?php

require_once('for_php7.php');

class knjz237aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz237aindex.php", "", "edit");

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
        $query = knjz237aQuery::getNameMst();
        $getname = $db->getone($query);

        //学期コンボ
        $query = knjz237aQuery::getSemester($getname);
        $extra = "onChange=\"btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //テストコンボ
        $query = knjz237aQuery::getTestitemMstCountflgNew($model->field["SEMESTER"]);
        $extra = "onChange=\"btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTKINDCD"], "TESTKINDCD", $extra, 1, "BLANK", $model->field["SEMESTER"]);

        /****************/
        /* リスト内表示 */
        /****************/
        $query  = knjz237aQuery::getListdata($model);
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
                $row["SUBCLASSNAME"] = View::alink("knjz237aindex.php", $row["SUBCLASSNAME"], "target=\"right_frame\"",
                                                array("cmd"             => "edit",
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
                $row["SUBCLASSNAME"] = View::alink("knjz237aindex.php", $row["SUBCLASSNAME"], "target=\"right_frame\"",
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

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if ($model->cmd == "change" || VARS::post("cmd") == "list"){
            $arg["reload"] = "window.open('knjz237aindex.php?cmd=edit2&SEMESTER={$model->semester}&TESTKINDCD={$model->testkindcd}','right_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz237aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank, $semval = "")
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
        if ($name == "TESTKINDCD") {
            //Z010より適用学校情報取得
            $query = knjz237aQuery::getNameMst();
            $getname = $db->getone($query);
            if ($getname === 'meiji' && $semval !== '9') {
                $opt[] = array('label' => '9901 平常点', 'value' => '99-01');
                if ($value == '99-01') $value_flg = true;
            }
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
