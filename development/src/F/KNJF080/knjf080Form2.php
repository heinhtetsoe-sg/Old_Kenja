<?php

require_once('for_php7.php');


class knjf080Form2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf080index.php", "", "edit");

        $db = Query::dbCheckOut();

        //熊本
        if ($model->isKumamoto) {
            $arg["isKumamoto"] = "1";

            $nameCd2_Jcd = "F514";
            $minGrade = $db->getOne(knjf080Query::getMinGrade($model));
            $setGrade = (CTRL_YEAR > 2022) ? 0: CTRL_YEAR - 2016 + (int)$minGrade;
            if ($setGrade > 0 && $model->grade > sprintf("%02d", $setGrade)) {
                $nameCd2_Jcd = "F510";
            }
            if ($model->getSchKind != "" && $model->getSchKind != "H") {
                $nameCd2_Jcd = "F510";
            }

            //SQL文発行
            $query = knjf080Query::selectQuery($model, $nameCd2_Jcd);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //視力
            $visionMark = $row["R_BAREVISION_MARK"];
            if ($visionMark != "") {
                $visionMark .= " ";
            }
            $visionMark .= $row["R_VISION_MARK"];
            if ($visionMark != "") {
                $visionMark .= " ";
            }
            $visionMark .= $row["L_BAREVISION_MARK"];
            if ($visionMark != "") {
                $visionMark .= " ";
            }
            $visionMark .= $row["L_VISION_MARK"];
            $row["VISION_MARK"] = $visionMark;

            //歯科
            $tooth = $row["REMAINBABYTOOTH"];
            if ($tooth != "") {
                $tooth .= " ";
            }
            $tooth .= $row["REMAINADULTTOOTH"];
            if ($tooth != "") {
                $tooth .= " ";
            }
            $tooth .= $row["JAWS_JOINTCD"];
            if ($tooth != "") {
                $tooth .= " ";
            }
            $tooth .= $row["PLAQUECD"];
            if ($tooth != "") {
                $tooth .= " ";
            }
            $tooth .= $row["GUMCD"];
            if ($tooth != "") {
                $tooth .= " ";
            }
            $tooth .= $row["TOOTH_OTHERDISEASECD"];
            $row["TOOTH"] = $tooth;
        } else {
            //SQL文発行
            $query = knjf080Query::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        $arg["data"] = $row;

        //キーの表示
        $disabled = "disabled";
        if (is_array($row)) {
            //キーの表示
            $disabled = "";
        }
        foreach ($model->item as $field) {
            $objForm->ae(array("type"        => "text",
                               "name"        => $field,
                               "size"        => 40,
                               "maxlength"   => 40,
                               "extrahtml"   => "onblur=\"check(this)\"",
                               "value"       => $row[$field]));

            $arg["data"][$field] = $objForm->ge($field);
        }

        Query::dbCheckIn($db);

        $arg["SCHREGNO"]    = $model->schregno;
        $arg["NAME"]        = $model->name;

        //修正ボタンを作成する
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "$disabled onclick=\"return btn_submit('update');\"" ));

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //クリアボタンを作成する
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "$disabled onclick=\"return btn_submit('reset');\""));

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                           "name"      => "cmd"
                           ));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf080Form2.html", $arg);
    }
}
