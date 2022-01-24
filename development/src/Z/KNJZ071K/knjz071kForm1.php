<?php
class knjz071kForm1
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
        $arg["start"]   = $objForm->get_start("knjz071k", "POST", "knjz071kindex.php", "", "knjz071k");
        $db             = Query::dbCheckOut();

        //中分類マスタ設定
        $result    = $db->query(knjz071kQuery::selectMmstQuery());
        $opt_m_mst       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_m_mst[] = array("label" => $row["EXPENSE_M_CD"].":".$row["EXPENSE_M_NAME"],
                                 "value" => $row["EXPENSE_M_CD"]);
        }
        if (!$model->expense_m_cd) $model->expense_m_cd = $opt_m_mst[0]["value"];

        //登録済み小分類一覧取得
        $result      = $db->query(knjz071kQuery::selectSmstQuery($model,"OK"));
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_left[]    = array("label" => $row["EXPENSE_S_CD"]."  ".$row["EXPENSE_S_NAME"],
                                   "value" => $row["EXPENSE_S_CD"]);
            $opt_left_id[] = $row["EXPENSE_S_CD"];
        }
        $opt_right = array();

        //小分類一覧取得
        $result = $db->query(knjz071kQuery::selectSmstQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             $opt_right[] = array("label" => $row["EXPENSE_S_CD"]."  ".$row["EXPENSE_S_NAME"],
                                  "value" => $row["EXPENSE_S_CD"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //中分類コンボボックスを作成する
        $objForm->ae( array("type"        => "select",
                            "name"        => "EXPENSE_M_CD",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "value"       => $model->expense_m_cd,
                            "options"     => $opt_m_mst));

        $arg["TOP"]["EXPENSE_M_CD"] = $objForm->ge("EXPENSE_M_CD");

        //中小分類
        $objForm->ae( array("type"        => "select",
                            "name"        => "EXPENSE_MS",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','EXPENSE_MS','EXPENSE_S',1)\"",
                            "options"     => $opt_left));

        //小分類
        $objForm->ae( array("type"        => "select",
                            "name"        => "EXPENSE_S",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','EXPENSE_MS','EXPENSE_S',1)\"",
                            "options"     => $opt_right));

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','EXPENSE_MS','EXPENSE_S',1);\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','EXPENSE_MS','EXPENSE_S',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','EXPENSE_MS','EXPENSE_S',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','EXPENSE_MS','EXPENSE_S',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("EXPENSE_MS"),
                                   "RIGHT_PART"  => $objForm->ge("EXPENSE_S"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //小分類マスタボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ070K/knjz070kindex.php?mode=KNJZ071K";

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 小分類マスタ ",
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

        $arg["button"] = array("BTN_MASTER" =>$objForm->ge("btn_master"),
                               "BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );

        $arg["info"]    = array("TOP"        => CTRL_YEAR."年度&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp中分類：",
                                "LEFT_LIST"  => "中小分類年度一覧",
                                "RIGHT_LIST" => "中小分類一覧");

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz071kForm1.html", $arg);
    }
}
?>
