<?php

require_once('for_php7.php');

class knjz050Form1
{
    function main(&$model)
    {
        $flg = "";
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz050index.php", "", "sel");
        $db             = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjz050Query::selectYearQuery());
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if ($model->year == $row["YEAR"]) $flg = true;
        }
        if (!$flg)    $model->year = $opt[0]["value"];

        //年度学科一覧取得
        $result      = $db->query(knjz050Query::selectQuery($model));
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_left[]    = array("label" => $row["COURSECD"].$row["MAJORCD"]."　".$row["COURSENAME"]."　".$row["MAJORNAME"],
                                   "value" => $row["COURSECD"].$row["MAJORCD"]);
            $opt_left_id[] = $row["COURSECD"].$row["MAJORCD"];
        }
        $opt_right = array();

        //学科一覧取得
        $result = $db->query(knjz050Query::selectJobQuery($opt_left_id,$model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_right[]    = array("label" => $row["COURSECD"].$row["MAJORCD"]."　".$row["COURSENAME"]."　".$row["MAJORNAME"],
                                       "value" => $row["COURSECD"].$row["MAJORCD"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "year",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "options"     => $opt ));

        $objForm->ae( array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => "" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));

        $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

        //学科年度
        $objForm->ae( array("type"        => "select",
                            "name"        => "majoryear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','majoryear','majormaster',1)\"",
                            "options"     => $opt_left));

        //学科マスタ
        $objForm->ae( array("type"        => "select",
                            "name"        => "majormaster",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','majoryear','majormaster',1)\"",
                            "options"     => $opt_right));

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','majoryear','majormaster',1);\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','majoryear','majormaster',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','majoryear','majormaster',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','majoryear','majormaster',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("majoryear"),
                                   "RIGHT_PART"  => $objForm->ge("majormaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //学科マスタボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ050_2/knjz050_2index.php?mode=1";

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 学科マスタ ",
                            "extrahtml"   => "onclick=\"document.location.href='$link'\"") );

        //学科別学校区分ボタン
        $link = REQUESTROOT."/Z/KNJZ050_3/knjz050_3index.php?mode=1&SEND_YEAR=$model->year";
        //学科別学校区分ボタン表示のプロパティ
        if ($model->Properties["useGakkaSchoolDiv"] == '1') {
            $arg["useGakkaSchoolDiv"] = 1;
        }
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_shousai",
                            "value"       => " 学科別学校区分 ",
                            "extrahtml"   => "onclick=\"document.location.href='$link'\"") );

        //保存ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_MASTER"     =>$objForm->ge("btn_master"),
                               "BTN_OK"         =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"      =>$objForm->ge("btn_clear"),
                               "BTN_END"        =>$objForm->ge("btn_end"),
                               "BTN_SHOUSAI"    =>$objForm->ge("btn_shousai"));


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );

        $arg["info"]    = array("TOP"        => "対象年度",
                                "LEFT_LIST"  => "学科年度一覧",
                                "RIGHT_LIST" => "学科一覧");

        $arg["TITLE"]   = "マスタメンテナンス - 学科マスタ";
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "sel.html", $arg);
    }
}
?>
