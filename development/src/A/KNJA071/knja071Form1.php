<?php

require_once('for_php7.php');

class knja071Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm      = new form();
        $arg["start"] = $objForm->get_start("list", "POST", "knja071index.php", "", "edit");
        $db     = Query::dbCheckOut();

        $ctrl_year = CTRL_YEAR;
        $ctrl_year_add = CTRL_YEAR + 1;
        $select_ctrl = CTRL_SEMESTER + 1;   //選択されている学期を1学期先に
        $semester_ctrl = CTRL_SEMESTER + 1; //選択されている学期を1学期先に

        //年度コンボボックス
        for ($i = 0; $i < $model->control["学期数"]; $i++) {
            $opt[$i] = array("label" => $ctrl_year."年度  ".$model->control["学期名"][$i+1],
                             "value" => $ctrl_year."-".($i+1));
            $setVal = CTRL_YEAR."-".CTRL_SEMESTER == $ctrl_year."-".($i+1) ? $i + 1 : $setVal;
        }
        //年度コンボボックス
        for ($addcnt = 0; $addcnt < $model->control["学期数"]; $addcnt++) {
            $opt[$i] = array("label" => $ctrl_year_add."年度  ".$model->control["学期名"][$addcnt+1],
                             "value" => $ctrl_year_add."-".($addcnt+1));
            $setVal = CTRL_YEAR."-".CTRL_SEMESTER == $ctrl_year_add."-".($addcnt+1) ? $i + 1 : $setVal;
            $i++;
        }

        $model->term = $model->term ? $model->term : $opt[$setVal]["value"];
        $model->term = $model->term ? $model->term : CTRL_YEAR."-".CTRL_SEMESTER;

        if (!isset($model->term)) {
            $semester    = $select_ctrl;
            $model->term = $ctrl_year. "-" .$semester;
            $selected    = $select_ctrl ;
        }

        $objForm->ae(array("type"      => "select",
                           "name"      => "term",
                           "size"      => "1",
                           "extrahtml" => "selectedindex=\"$selected\" onChange=\"btn_submit('list')\"",
                           "value"     => $model->term,
                           "options"   => $opt));
        $arg["term"] = $objForm->ge("term");

        $objForm->ae(array("type"      =>    "button",
                           "name"      =>    "btn_copy",
                           "value"     =>    "左の学期のデータをコピー",
                           "extrahtml" =>    "onClick=\"return btn_submit('copy');\"".$disabled));
        $arg["btn_copy"] = $objForm->ge("btn_copy");

        //チェックボックス
        $objForm->ae(array("type"      =>    "checkbox",
                           "name"      =>    "check",
                           "value"     =>    "1"));
        $arg["btn_check"] = $objForm->ge("check");

        //参照年度コンボボックス
        $opt2 = array();
        $result = $db->query(knja071Query::getSemester($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt2[] = array("label" => $row['LABEL'],
                            "value" => $row['VALUE']);
        }

        $objForm->ae(array("type"      => "select",
                           "name"      => "term2",
                           "size"      => "1",
                           "extrahtml" => "selectedindex=\"$selected\"",
                           "value"     => $model->term2,
                           "options"   => $opt2));
        $arg["term2"] = $objForm->ge("term2");

        //リスト表示
        $result = $db->query(knja071query::selectList($model->term));
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $row["URL"] = View::alink(
                "knja071index.php",
                $row["HR_CLASS"],
                "target=right_frame",
                array("cmd"         => "edit",
                      "GRADE"       => $row["GRADE"],
                      "HR_CLASS"    => $row["HR_CLASS"],
                      "term"        => $model->term)
            );
            $row["COURSE"] = $row["COURSEMAJORNAME"]." ".$row["COURSECODENAME"];
            $row["backcolor"] = ($i%2 == 0) ? "#ffffff" : "#ccffcc";  //#ccffff
            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae(array("type"      => "hidden",
                           "name"      => "cmd"));
        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "list" && VARS::get("ed") != "1") {
            $arg["reload"] = "window.open('knja071index.php?cmd=edit&init=1','right_frame');";
        }

        View::toHTML($model, "knja071Form1.html", $arg);
    }
}
