<?php

require_once('for_php7.php');

class knja080Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $arg["Read"] = "start();";
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knja080index.php", "", "sel");
        $db = Query::dbCheckOut();

        //ログイン校種のA023
        $query = knja080Query::getA023SchoolKind($model, SCHOOLKIND);
        $model->schoolKindA023 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //A023全て
        $query = knja080Query::getA023($model);
        $result = $db->query($query);
        $model->A023 = array();
        $model->A023GradeHr = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $setGradeHr = "00-00".$row["ABBV2"];
            $model->A023[$row["NAME1"]] = $row;
            $model->A023[$row["NAME1"]]["GRADEHR"] = $setGradeHr;

            $model->A023GradeHr[$setGradeHr] = $row;
        }
        $result->free();

        //次年度の最小の学期を求める
        if (!isset($model->min_semester)){
            $query = knja080Query::getNextYearMinSemes($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->min_semester = $row["SEMESTER"];
        }
        if (CTRL_SEMESTER == $model->control["学期数"]) { //最終学期のとき
            $arg["NEWTERM"] = ((int)CTRL_YEAR+1) ."年度　".$model->control["学期名"][1];
        } else {
            $arg["NEWTERM"] = CTRL_YEAR ."年度　".$model->control["学期名"][((int)CTRL_SEMESTER + 1)];
        }
        $arg["OLDTERM"] = CTRL_YEAR ."年度　".$model->control["学期名"][CTRL_SEMESTER];
        //ヘッダのクラス取得
        $result = $db->query(knja080Query::getHr_Class($model));
        $opt = array();
        $opt["NEW"] = $opt["OLD"] = array();
        if (CTRL_SEMESTER == $model->control["学期数"] && $model->Properties["KNJA080_ExceptFreshMan"] != "1") { //最終学期のとき
            foreach ($model->A023 as $a023Key => $a023Val) {
                $opt["OLD"][] = array("label" => "新入生（{$a023Val["ABBV1"]}）", "value" => $a023Val["GRADEHR"]);
            }
        }
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[$row["YEAR"]][] = array("label" => $row["HR_NAME"] ."　".$row["NAME"], 
                                         "value" => $row["GRADE"] . "-" . $row["HR_CLASS"] );
        }
        $opt["NEW_OPTION"] = array();
        $opt["OLD_OPTION"] = array();
        if ($model->cmd == "update" || $model->cmd == "clear"){
            //新クラス一覧
            $result = $db->query(knja080Query::GetNewStudent($model));
            $model->schregno = array();
            $i = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row["NAME"] = str_replace(" ","&nbsp;",$row["NAME"]);
                $remaingrade  = ($row["REMAINGRADE_FLG"] == "1") ? " [留] " : "　　　";
                $opt["NEW_OPTION"][] = array("label"  => $row["HR_NAME"].$row["OLD_ATTENDNO"] ."番"."　".$remaingrade."　" .$row["SCHREGNO"]."　" .$row["NAME"],
                                              "value" => $row["SCHREGNO"]
                                              );

                $model->schregno[] = $row["SCHREGNO"];
                $i++;
            }
            $arg["NEWNUM"] = $i;
            //旧クラス一覧

            $result = $db->query(knja080Query::GetOldStudent($model));
            $i = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row["NAME"] = str_replace(" ","&nbsp;",$row["NAME"]);
                $remaingrade  = ($row["REMAINGRADE_FLG"] == "1") ? " [留] " : "　　　";
                $opt["OLD_OPTION"][] = array("label" => $row["HR_NAME"].$row["ATTENDNO"] ."番"."　".$remaingrade."　".$row["SCHREGNO"]."　".$row["NAME"],
                                            "value" => $row["SCHREGNO"]
                                            );
                $i++;
            }
            $arg["OLDNUM"] = $i;
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "NEWCLASS",
                            "size"        => "1",
                            "value"       => $model->newclass,
                            "extrahtml"   => "onchange=\"btn_submit('selectclass')\"",
                            "options"     => $opt["NEW"])); 

        $arg["NEWCLASS"] = $objForm->ge("NEWCLASS");

        $objForm->ae( array("type"        => "select",
                            "name"        => "OLDCLASS",
                            "size"        => "1",
                            "value"       => $model->oldclass,
                            "extrahtml"   => "onchange=\"btn_submit('selectclass')\"",
                            "options"     => $opt["OLD"])); 

        $arg["OLDCLASS"] = $objForm->ge("OLDCLASS");

        //新クラス一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "NEW_CLASS_STU",
                            "size"        => "35",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"moveStudent('right')\"",
                            "options"     => $opt["NEW_OPTION"])); 

        //旧クラス一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "OLD_CLASS_STU",
                            "size"        => "35",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"moveStudent('left')\"",
                            "options"     => $opt["OLD_OPTION"]));

        $arg["BTN_OLD_READ"] = $objForm->ge("btn_old_read");

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return moveStudent('sel_add_all');\"" ) );

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return moveStudent('left');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return moveStudent('right');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return moveStudent('sel_del_all');\"" ) ); 
                                        
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("NEW_CLASS_STU"),
                                   "RIGHT_PART"  => $objForm->ge("OLD_CLASS_STU"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );  

        //保存ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_update"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));  

        //学校区分取得
        $schooldiv = $db->getOne(knja080Query::getSchooldiv($model));

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useKeepGrade", $model->Properties["useKeepGrade"]);
        knjCreateHidden($objForm, "SCHOOLDIV", $schooldiv);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja080Form1.html", $arg); 
    }
}
?>
