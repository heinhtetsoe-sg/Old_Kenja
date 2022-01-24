<?php

require_once('for_php7.php');

class knjz072_2aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz072_2aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        }

        //教科コンボ
        $query = knjz072_2aQuery::getClasscd($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SELECT_CLASSCD", $model->select_classcd, $extra, 1, $model, "blank");

        //校種コンボ
        $query = knjz072_2aQuery::getSchoolKind($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SELECT_SCHOOL_KIND", $model->select_school_kind, $extra, 1, $model, "blank");

        //教育課程コンボ
        $query = knjz072_2aQuery::getCurriculumCd($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SELECT_CURRICULUM_CD", $model->select_curriculum_cd, $extra, 1, $model, "blank");

        //一覧
        $query = knjz072_2aQuery::getSubclassData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($row["ELECTDIV"]=="1") {
                $row["ELECTDIV"] = "選";
            } elseif ($row["ELECTDIV"]=="0") {
                $row["ELECTDIV"] = "";
            }

            //更新後この行が画面の先頭に来るようにする
            if ($row["SUBCLASSCD"] == $model->subclasscd) {
                $row["SUBCLASSNAME"] = ($row["SUBCLASSNAME"]) ? $row["SUBCLASSNAME"] : "　";
                $row["SUBCLASSNAME"] = "<a name=\"target\">{$row["SUBCLASSNAME"]}</a><script>location.href='#target';</script>";
            }

            $link = array();
            if ($model->Properties["useCurriculumcd"] == '1') {
                $value = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                $link["CLASSCD"]                = $row["CLASSCD"];
                $link["SCHOOL_KIND"]            = $row["SCHOOL_KIND"];
                $link["CURRICULUM_CD"]          = $row["CURRICULUM_CD"];
                $link["SUBCLASSCD"]             = $row["SUBCLASSCD"];
                $link["year_code"]              = $model->year_code;
                $link["SELECT_CLASSCD"]         = $model->select_classcd;
                $link["SELECT_SCHOOL_KIND"]     = $model->select_school_kind;
                $link["SELECT_CURRICULUM_CD"]   = $model->select_curriculum_cd;
                $link["cmd"]                    = "edit";
            } else {
                $value = $row["SUBCLASSCD"];
                $link["SUBCLASSCD"]             = $row["SUBCLASSCD"];
                $link["year_code"]              = $model->year_code;
                $link["SELECT_CLASSCD"]         = $model->select_classcd;
                $link["cmd"]                    = "edit";
            }
            $row["SUBCLASSCD_LINK"] = View::alink("knjz072_2aindex.php", $value, "target=right_frame", $link);

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "change") {
            $arg["reload"]  = "parent.right_frame.location.href='knjz072_2aindex.php?cmd=edit&SELECT_CLASSCD=".$model->select_classcd;
            if ($model->Properties["useCurriculumcd"] == '1') {
                $arg["reload"] .= "&SELECT_SCHOOL_KIND=".$model->select_school_kind."&SELECT_CURRICULUM_CD=".$model->select_curriculum_cd;
            }
            $arg["reload"] .= "';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz072_2aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array('label' => " -- 全て -- ", 'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
