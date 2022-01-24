<?php

require_once('for_php7.php');

class knjz210kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz210kindex.php", "", "sel");

        $db = Query::dbCheckOut();

        //リスト選択データ取得
        $ChosenData = array();
        if (!isset($model->warning) && isset($model->groupcd)) {
            $ChosenData = $db->getRow(knjz210kQuery::getEditQuery($model->year, $model->groupcd, $model), DB_FETCHMODE_ASSOC);
        } else {
            $ChosenData =& $model->field;
        }
        if (substr($model->cmd, 0, 3) == "chg") {
            $ChosenData["TYPE_GROUP_CD"] = $model->field["TYPE_GROUP_CD"];
            $ChosenData["TYPE_GROUP_NAME"] = $model->field["TYPE_GROUP_NAME"];
        }
        //学年取得
        $opt_grade = array();
        $grade_flg = 0;
        $result = $db->query(knjz210kQuery::getGradeQuery($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_grade[] = array("label" => "第" . $row["GRADE"] . "学年",
                                 "value" => $row["GRADE"]);
            if ($model->grade == $row["GRADE"]) {
                $grade_flg = 1;
            }
        }
        if (!$grade_flg) {
            $model->grade = $opt_grade[0]["value"];
        }
        
        //科目取得
        $opt = array();
        $value_flg = false;
        $query = knjz210kQuery::getSubclassQuery($model->year, $model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('chg_subclass')\"";
        $arg["select"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $model->subclasscd2, $opt, $extra, 1);

        //類型評定コード取得
        $opt_asses = array();
        $opt_asses[] = array("label" => "", "value" => "");
        $result = $db->query(knjz210kQuery::getAssesQuery($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_asses[] = array("label" => $row["TYPE_ASSES_CD"],
                                 "value" => $row["TYPE_ASSES_CD"]);
        }
        $opt_left = $opt_right = array();
        if ($model->cmd != "chg_subclass" && !isset($model->warning)) {
            //類型グループクラスのセルの値取得
            $result = $db->query(knjz210kQuery::getGroupQuery($model->year, $model->groupcd, $model->grade, $model->subclasscd2, $model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if (!is_null($row["HR_NAME"])) {
                    $opt_left[] = array("label" => $row["HR_NAME"],
                                        "value" => $row["HR_CLASS"]);
                }
            }
            $result = $db->query(knjz210kQuery::getClassQuery($model->year, $model->groupcd, $model->grade, $model->subclasscd2, $model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if (!is_null($row["HR_CLASS"])) {
                    $opt_right[] = array("label" => $row["HR_NAME"],
                                         "value" => $row["HR_CLASS"]);
                }
            }
        } else {
            $selectlength = explode(",", $model->field["SELECTLENGTH"]);
            $selecttext   = explode(",", $model->field["SELECTTEXT"]);
            $selectvalue  = explode(",", $model->field["SELECTVALUE"]);
            for($i = 0; $i < $selectlength[0]; $i++)
            {
                $opt_left[] = array("label" => $selecttext[$i],
                                    "value" => $selectvalue[$i]);
            }
            for($i = $selectlength[0]; $i < $selectlength[0] + $selectlength[1]; $i++)
            {
                $opt_right[] = array("label" => $selecttext[$i],
                                     "value" => $selectvalue[$i]);
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        //グループコード
        $objForm->ae( array("type"        => "text",
                            "name"        => "TYPE_GROUP_CD",
                            "size"        => "7",
                            "maxlength"   => "6",
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $ChosenData["TYPE_GROUP_CD"] ));
        $arg["TYPE_GROUP_CD"] = $objForm->ge("TYPE_GROUP_CD");
        //グループ名
        $objForm->ae( array("type"        => "text",
                            "name"        => "TYPE_GROUP_NAME",
                            "size"        => "40",
                            "maxlength"   => "20",
                            "value"       => $ChosenData["TYPE_GROUP_NAME"] ));
        $arg["TYPE_GROUP_NAME"] = $objForm->ge("TYPE_GROUP_NAME");

        //学年コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('chg_grade')\"",
                            "value"       => $model->grade,
                            "options"     => $opt_grade ));
        $arg["select"]["GRADE"] = $objForm->ge("GRADE");

        //科目コンボボックス
        /*$objForm->ae( array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('chg_subclass')\"",
                            "value"       => $model->subclasscd2,
                            "options"     => $opt_subclass ));
        $arg["select"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");*/

        //類型評定コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "TYPE_ASSES_CD",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('chg_asses')\"",
                            "value"       => $model->asses,
                            "options"     => $opt_asses ));
        $arg["select"]["TYPE_ASSES_CD"] = $objForm->ge("TYPE_ASSES_CD");

        //類型グループクラス
        $objForm->ae( array("type"        => "select",
                            "name"        => "grouplist",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right', 'grouplist', 'classlist', 1)\"",
                            "options"     => $opt_left));
        //クラス一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "classlist",
                            "size"        => "20",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left', 'grouplist', 'classlist', 1)\"",
                            "options"     => $opt_right));

        //全追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all', 'grouplist', 'classlist', 1);\"" ) );
        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left', 'grouplist', 'classlist', 1);\"" ) );
        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right', 'grouplist', 'classlist', 1);\"" ) );
        //全削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all', 'grouplist', 'classlist', 1);\"" ) );
        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_delete",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["info"]    = array("TOP"        => "類型グループ名称 : ".$ChosenData["TYPE_GROUP_CD"]."&nbsp;&nbsp;".$ChosenData["TYPE_GROUP_NAME"],
                                "LEFT_LIST"  => "類型グループクラス",
                                "RIGHT_LIST" => "クラス一覧" );
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("grouplist"),
                                   "RIGHT_PART"  => $objForm->ge("classlist"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all") );
        $arg["button"] = array("BTN_ADD"    =>$objForm->ge("btn_add"),
                               "BTN_KEEP"   =>$objForm->ge("btn_keep"),
                               "BTN_DELETE" =>$objForm->ge("btn_delete"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectlength" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selecttext" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectvalue" ) );
        //評価区分
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "record_dat_flg",
                            "value"     => "0" ) );

        $arg["finish"]  = $objForm->get_finish();

        if ($model->record_dat_flg == "1"){
            $arg["show_confirm"] = "Show_Confirm();";
        }

        //更新できたら左のリストを再読込
        if (isset($model->message)) {
            $arg["reload"] = "parent.left_frame.location.href='knjz210kindex.php?cmd=list&SUBCLASSCD=" . $model->subclasscd2 . "';";
        }

        View::toHTML($model, "knjz210kForm2.html", $arg);
    }
}
?>
