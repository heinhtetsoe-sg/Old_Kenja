<?php

require_once('for_php7.php');

class knjz070_2Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz070_2index.php", "", "edit");

        $db = Query::dbCheckOut();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";

            //校種コンボ
            $query = knjz070_2Query::getSchkind($model);
            $extra = " onchange=\"return btn_submit('changeCmb');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schkind, $extra, 1, "");

            //教育課程コンボ
            $query = knjz070_2Query::getNamecd("Z018");
            $extra = " onChange=\"return btn_submit('changeCmb');\"";
            makeCmb($objForm, $arg, $db, $query, "LIST_CURRICULUM_CD", $model->list_curriculum_cd, $extra, 1, "");

            //教科コンボ
            $query = knjz070_2Query::getClassCd($model);
            $extra = " onChange=\"return btn_submit('changeCmb');\"";
            makeCmb($objForm, $arg, $db, $query, "LIST_CLASSCD", $model->list_classcd, $extra, 1, "");

            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $query = knjz070_2Query::getSubclassData($model);
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

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);
        $arg["year"] = $model->year_code;
        //特別支援
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $arg["support"] = 1;
        }
        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeCmb") {
            $arg["reload"]  = "parent.right_frame.location.href='knjz070_2index.php?cmd=edit';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz070_2Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($name == "LIST_CURRICULUM_CD") {
        $opt[] = array("label" => "--全て--", "value" => "99");
    } elseif ($name == "LIST_CLASSCD") {
        $opt[] = array("label" => "--全て--", "value" => "99-99-99");
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
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
