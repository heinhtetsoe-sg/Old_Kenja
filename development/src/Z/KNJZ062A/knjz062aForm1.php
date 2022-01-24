<?php

require_once('for_php7.php');

class knjz062aForm1
{
    function main(&$model)
    {
        $arg["jscript"] = "";
        
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz062aindex.php", "", "sel");
        $db             = Query::dbCheckOut();
        $no_year        = 0;
        
        //年度設定
        $query     = knjz062aQuery::selectYearQuery();
        $result    = $db->query($query);
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if ($row["YEAR"] == $model->year)
                $no_year = 1;
        }
        if ($no_year == 0)
            $model->year = $opt[0]["value"];

        //年度教科一覧取得
        $query       = knjz062aQuery::selectQuery($model);
        $result      = $db->query($query);
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            $opt_left_id[] = $row["VALUE"];
        }
        $opt_right = array();

        //教科一覧取得
        $query  = knjz062aQuery::selectClassQuery($opt_left_id,$model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        //年度コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "year",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "options"     => $opt));

        $objForm->ae( array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => "")); 

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));

        $arg["year"] = array( "VAL"       => $objForm->ge("year"),
                              "BUTTON"    => $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));
        //教科年度
        $objForm->ae( array("type"        => "select",
                            "name"        => "classyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_left)); 

        //教科マスタ
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
                            "extrahtml"   => " onclick=\"moves('left');\"" ) );

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => " onclick=\"move1('left');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => " onclick=\"move1('right');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => " onclick=\"moves('right');\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("classyear"),
                                   "RIGHT_PART"  => $objForm->ge("classmaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //教科マスタボタン
        $link = REQUESTROOT."/Z/KNJZ062_2A/knjz062_2aindex.php?mode=1&SEND_AUTH=".AUTHORITY." ";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_MASTER"] = knjCreateBtn($objForm, "BTN_OK", " 教科マスタ ", $extra);
        //保存ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "BTN_OK", "更新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "BTN_CLEAR", "取消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "終了", $extra);
        //教育委員会教科取込ボタン
        $link = REQUESTROOT."/Z/KNJZ062_CLASS_REFLECTION/knjz062_class_reflectionindex.php?SEND_AUTH=".AUTHORITY." ";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_BOARD"] = knjCreateBtn($objForm, "BTN_BOARD", "教育委員会教科取込", $extra);
        
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "firstData");
        knjCreateHidden($objForm, "rightMoveData");

        $arg["info"]    = array("TOP"        => "対象年度",
                                "LEFT_LIST"  => "教科年度一覧",
                                "RIGHT_LIST" => "教科一覧");

        $arg["TITLE"]   = "マスタメンテナンス - 教科マスタ";
        $arg["finish"]  = $objForm->get_finish();

        $arg["jscript"] = " setFirstData(); ";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz062aForm1.html", $arg);
    }
}
?>
