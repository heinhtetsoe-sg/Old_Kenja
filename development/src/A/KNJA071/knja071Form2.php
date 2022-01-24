<?php

require_once('for_php7.php');

class knja071Form2
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja071index.php", "", "edit");

        if (!isset($model->warning)) {
            $Row = knja071Query::getRow($model->term, $model->grade, $model->hr_class);
        } else {
            $Row =& $model->fields;
        }

        //学年
        $arg["data"]["GRADE"] = $model->grade;

        //組
        $arg["data"]["HR_CLASS"] = $model->hr_class;

        //組名称
        $arg["data"]["HR_NAME"] = knja071query::getHrName($model->term, $model->grade, $model->hr_class);

        //課程学科
        $objForm->ae(array("type"        => "select",
                           "name"        => "COURSEMAJOR",
                           "size"        => 1,
                           "extrahtml"   => "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                           "value"       => $Row["COURSECD"]."-".$Row["MAJORCD"],
                           "options"     => knja071query::getCourseMajor(substr($model->term, 0, 4)) ));
        $arg["data"]["COURSEMAJOR"] = $objForm->ge("COURSEMAJOR");

        //コース
        $objForm->ae(array("type"        => "select",
                           "name"        => "COURSECODE",
                           "size"        => 1,
                           "extrahtml"   => "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                           "value"       => $Row["COURSECODE"],
                           "options"     => knja071query::getCourseCode(substr($model->term, 0, 4)) ));
        $arg["data"]["COURSECODE"] = $objForm->ge("COURSECODE");


        //更新ボタン
        $objForm->ae(array("type"        => "button",
                           "name"        => "btn_udpate",
                           "value"       => "更 新",
                           "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //クリアボタン
        $objForm->ae(array("type"        => "reset",
                           "name"        => "btn_reset",
                           "value"       => "取 消",
                           "extrahtml"   => "onclick=\"return btn_submit('reset')\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae(array("type"        => "button",
                           "name"        => "btn_back",
                           "value"       => "終 了",
                           "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                           "name"      => "cmd"
                            ));

        $objForm->ae(array("type"      => "hidden",
                           "name"      => "GRADE",
                           "value"     => $Row["GRADE"]
                            ));

        $objForm->ae(array("type"      => "hidden",
                           "name"      => "HR_CLASS",
                           "value"     => $Row["HR_CLASS"]
                            ));

        $objForm->ae(array("type"      => "hidden",
                           "name"      => "UPDATED",
                           "value"     => $Row["UPDATED"]
                            ));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knja071index.php?cmd=list&ed=1','left_frame');";
        }

        View::toHTML($model, "knja071Form2.html", $arg);
    }
}
