<?php

require_once('for_php7.php');

class knjxSearch
{
    function main(&$model){
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("search", "POST", "index.php", "right_frame");

        $db = Query::dbCheckOut();
        //年度と学期
        $Row = $db->getRow(knjxexp5Query::getYearSemester($model->exp_year), DB_FETCHMODE_ASSOC);
        $arg["EXP_YEAR"] = "年度：" .$Row["YEAR"] ."　学期：" .$Row["SEMESTERNAME"];

        //在籍生徒
        if ($model->mode == "ungrd"){
            //学年コンボボックス
            $opt = array();
            $opt[] = array("label"  => '',
                            "value" => '');

            $result = $db->query(knjxexp5Query::GetGrade($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label"  => $row["LABEL"],
                               "value"  => $row["VALUE"]);
            }

            $arg["GRADE"] = $objForm->ge("GRADE2");
            $extra = " Onchange=\"btn_submit('right');\" ";
            $arg["GRADE"] = knjCreateCombo($objForm, "GRADE2", $model->searchGrade, $opt, $extra, "1");

            //組コンボボックス
            $opt = array();
            $opt[] = array("label"  => '',
                            "value" => '');

            $result = $db->query(knjxexp5Query::GetHr_Class($model, $model->searchGrade));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label"  => $row["HR_NAME"],
                               "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
            }
            $extra = "";
            $arg["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS2", $model->searchGradeHr, $opt, $extra, "1");

        //卒業生徒
        } else if ($model->mode == "grd") {
            //卒業年度
            $opt = array();
            $opt[] = array("label"  => '',
                            "value" => '');
            $result = $db->query(knjxexp5Query::GetGrdYear());
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  => $row["GRD_YEAR"]."年",
                                "value" => $row["GRD_YEAR"]);

            }
            $objForm->ae( array("type"        => "select",
                                "name"        => "GRD_YEAR",
                                "size"        => "1",
                                "extrahtml"   => "onChange=\"return btn_submit('right')\"",
                                "value"       => $model->search["GRD_YEAR"],
                                "options"     => $opt ));

            $arg["GRD_YEAR"] = $objForm->ge("GRD_YEAR");
            //卒業時組
            $query = knjxexp5Query::GetGrdHrClass($model);
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->search["HR_CLASS"], "", 1, "BLANK");
        }
        //コースコンボボックス
        $opt = array();
        $opt[] = array("label"  => '',
                        "value" => '');
        $result = $db->query(knjxexp5Query::GetCourseCode());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  => $row["COURSECODE"]."　".$row["COURSECODENAME"],
                            "value" => $row["COURSECODE"]);

        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSECODE",
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["COURSECODE"] = $objForm->ge("COURSECODE");

        Query::dbCheckIn($db);

        //学籍番号
        $arg["SRCH_SCHREGNO"] = knjCreateTextBox($objForm, "", "SRCH_SCHREGNO", 8, 8, "");

        //漢字姓
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"  => "",
                            "name"        => "NAME"));

        $arg["NAME"] = $objForm->ge("NAME");

        //漢字姓
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"  => "",
                            "name"        => "NAME_SHOW"));

        $arg["NAME_SHOW"] = $objForm->ge("NAME_SHOW");

        //かな姓
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "name"        => "NAME_KANA"));
        
        $arg["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //英字氏名
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"  => "onblur=\"this.value=toAlpha(this.value)\"",
                            "name"        => "NAME_ENG"));

        $arg["NAME_ENG"] = $objForm->ge("NAME_ENG");

        //性別
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            "options"     => array(array("label" => "", "value" => ''),
                                                   array("label" => "男性", "value" => '1'),
                                                   array("label" => "女性", "value" => '2'))
                             ));

        $arg["SEX"] = $objForm->ge("SEX");

        if($model->programid == "KNJF150"){
            //日付
            $arg["DATE"] = View::popUpCalendar($objForm, "DATE", $model->search["DATE"]);
        }

        //実行ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search",
                            "value"       => "検索",
                            "extrahtml"   => "onclick=\"return search_submit('".$model->mode."');\"" ));
        $arg["btn_search"] = $objForm->ge("btn_search");

        //閉じるボタン
        if ($model->diagtype != "") {
            $link = REQUESTROOT."/F/KNJF150D/knjf150dindex.php";
            $extra = "onclick=\"parent.location.href='$link';\"";
            $name = "戻る";
        } else {
            $extra = "onclick=\"return btn_back();\"";
            $name = "終了";
        }
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_end",
                            "value"       => $name,
                            "extrahtml"   => $extra ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxSearch.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>