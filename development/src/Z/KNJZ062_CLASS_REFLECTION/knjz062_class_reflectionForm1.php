<?php

require_once('for_php7.php');

class knjz062_class_reflectionForm1
{
    function main(&$model)
    {
        $arg["jscript"] = "";
        
        //権限チェック
        if ($model->auth != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz062_class_reflectionindex.php", "", "sel");
        //学校DB
        $db             = Query::dbCheckOut();
        //教育委員会DB
        $db2            = Query::dbCheckOut2();
        
        //教育委員会学校種別コンボ
        $query     = knjz062_class_reflectionQuery::selectSchoolKind();
        $result    = $db2->query($query);
        $opt       = array();
        $opt[]       = array("label" => "", "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $extra = "onChange=\"return btn_submit('');\" ";
        $arg["data"]["BOARD_SCHOOL_KIND"] = knjCreateCombo($objForm, "BOARD_SCHOOL_KIND", $model->field["BOARD_SCHOOL_KIND"], $opt, $extra, 1);

        //教育委員会教科一覧取得
        $opt_left = array();
        $opt_right = array();
        $query  = knjz062_class_reflectionQuery::selectQuery($model);
        $result = $db2->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knjz062_class_reflectionQuery::getClassCnt($model, $row["VALUE"]);
            $cnt = get_count($db->getCol($query));
            //学校DB.CLASS_MSTにあるものは、左リストに表示し、それ以外は、右リストに表示する。
            if (0 < $cnt) {
                $opt_left[]     = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            } else {
                $opt_right[]    = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            }
        }
        $result->free();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //学校教科
        $objForm->ae( array("type"        => "select",
                            "name"        => "classyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ",
                            "options"     => $opt_left)); 

        //教育委員会教科マスタ
        $objForm->ae( array("type"        => "select",
                            "name"        => "classmaster",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
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

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("classyear"),
                                   "RIGHT_PART"  => $objForm->ge("classmaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"));

        //更新ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "BTN_OK", "取 込", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "BTN_CLEAR", "取 消", $extra);
        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ062A/knjz062aindex.php?mode=1";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "戻 る", $extra);
        
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        $arg["info"]    = array("LEFT_LIST"  => "学校）教科一覧",
                                "RIGHT_LIST" => "教育委員会）教科一覧");

        $arg["TITLE"]   = "マスタメンテナンス - 教科マスタ";
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz062_class_reflectionForm1.html", $arg);
    }
}
?>
