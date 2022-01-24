<?php

require_once('for_php7.php');

class knjm281mForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjm281mindex.php", "", "sel");
        $db = Query::dbCheckOut();

        //年度設定
        $model->year = CTRL_YEAR;
        $arg["YEAR"] = $model->year;

        //レポート添削登録確認
        $checkdata = $db->getOne(knjm281mQuery::selectSubclassQuery($model, "COUNT"));
        if ($checkdata == 0){
            $arg["jscript"] = "OnStaffError();";
        }

        //科目一覧取得
        $opt_right = array();
        $opt_left = array();

        $query = knjm281mQuery::selectSubclassQuery($model, "");
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度科目
        $objForm->ae( array("type"        => "select",
                            "name"        => "subclassyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_left)); 

        //科目マスタ
        $objForm->ae( array("type"        => "select",
                            "name"        => "subclassmaster",
                            "size"        => "20",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_right));  

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"moves('left');\"" ) );

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"move1('left');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"move1('right');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"moves('right');\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("subclassyear"),
                                   "RIGHT_PART"  => $objForm->ge("subclassmaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["end"] = knjCreateBtn($objForm, "end", "終了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "temp_year", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "useRepStandarddateCourseDat", $model->Properties["useRepStandarddateCourseDat"]);
        knjCreateHidden($objForm, "PRGID", "KNJM281M");

        $arg["info"] = array("LEFT_LIST"  => "出力対象科目一覧",
                             "RIGHT_LIST" => "担当科目一覧");
        $arg["finish"]  = $objForm->get_finish();

        $arg["TITLE"] = "提出レポートチェックリスト";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm281mForm1.html", $arg); 
    }
}
?>
