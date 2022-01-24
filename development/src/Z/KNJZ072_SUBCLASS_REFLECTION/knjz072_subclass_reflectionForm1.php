<?php

require_once('for_php7.php');

class knjz072_subclass_reflectionForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz072_subclass_reflectionindex.php", "", "sel");
        //学校DB
        $db  = Query::dbCheckOut();
        //教育委員会DB
        $db2 = Query::dbCheckOut2();

        //学校種別コンボ
        $opt       = array();
        $opt[]     = array("label" => "", "value" => "");
        $query     = knjz072_subclass_reflectionQuery::selectSchoolKind($model);
        $result    = $db2->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $extra = "onChange=\"return btn_submit('');\" ";
        $arg["data"]["BOARD_SCHOOL_KIND"] = knjCreateCombo($objForm, "BOARD_SCHOOL_KIND", $model->field["BOARD_SCHOOL_KIND"], $opt, $extra, 1);

        //教育課程コンボ
        $opt       = array();
        $opt[]     = array("label" => "", "value" => "");
        $query     = knjz072_subclass_reflectionQuery::selectCurriculum($model);
        $result    = $db2->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $extra = "onChange=\"return btn_submit('');\" ";
        $arg["data"]["BOARD_CURRICULUM_CD"] = knjCreateCombo($objForm, "BOARD_CURRICULUM_CD", $model->field["BOARD_CURRICULUM_CD"], $opt, $extra, 1);

        //教育委員会DB.SUBCLASS_MST一覧
        $opt_left = array();
        $opt_right = array();
        if ($model->field["BOARD_SCHOOL_KIND"] !== '' || $model->field["BOARD_CURRICULUM_CD"] !== '') {
            $query = knjz072_subclass_reflectionQuery::selectSubclassQuery($model);
            $result = $db2->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $query = knjz072_subclass_reflectionQuery::getSubclassCnt($model, $row["VALUE"]);
                $cnt = get_count($db->getCol($query));
                //学校DB.SUBCLASS_MSTにあるものは、左リストに表示し、それ以外は、右リストに表示する。
                if (0 < $cnt) {
                    $opt_left[]     = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
                } else {
                    $opt_right[]    = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
                }
            }
            $result->free();
        }

        //学校科目
        $objForm->ae( array("type"        => "select",
                            "name"        => "subclassyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple style=\"width:300px\" width=\"300px\"",
                            "options"     => $opt_left)); 
        //教育委員会科目
        $objForm->ae( array("type"        => "select",
                            "name"        => "subclassmaster",
                            "size"        => "20",
                            "value"       => "right",
                            "extrahtml"   => "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_right));  
        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );
        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("subclassyear"),
                                   "RIGHT_PART"  => $objForm->ge("subclassmaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"));

        //更新ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "BTN_OK", "取 込", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "BTN_CLEAR", "取 消", $extra);
        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ072A/knjz072aindex.php?year_code=".$model->year_code;
        $extra = "onclick=\"parent.location.href='$link'\"";
        //$extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        $arg["info"]    = array("LEFT_LIST"  => "学校）科目一覧",
                                "RIGHT_LIST" => "教育委員会）科目一覧");

        $arg["TITLE"]   = "マスタメンテナンス - 科目マスタ";

        //学校DB切断
        //教育委員会DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz072_subclass_reflectionForm1.html", $arg); 
    }
}
?>
