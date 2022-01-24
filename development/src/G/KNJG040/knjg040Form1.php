<?php

require_once('for_php7.php');

class knjg040Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY == DEF_REFER_RESTRICT){
            $arg["close_win"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjg040index.php", "", "edit");

        //ヘッダー表示
        //$arg["header"] = array("CTRL_CHAR1" => $model->control_data["年度"]);
        $arg["header"] = array("CTRL_CHAR1" => CTRL_YEAR);

        //リスト内データ取得
        $db = Query::dbCheckOut();
        $query = knjg040Query::ReadQuery($model);

        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //レコードを連想配列のまま配列$arg[data]に追加していく。
                array_walk($row, "htmlspecialchars_array");
                $row["PERM_NAME"] = $model->perm_data[$row["PERM_CD"]]["NAME1"];

                //権限
                $row["PERM_NAME"] = View::alink("knjg040index.php", $row["PERM_NAME"], "target=right_frame",
                                                    array("cmd"            => "edit",
                                                          "applyday"       => $row["APPLYDAY"],
                                                          "APPLYCD"        => $row["APPLYCD"],
                                                          "STAFFCD"        => $row["STAFFCD"],
                                                          "sdate"          => $row["SDATE"],
                                                          "edate"          => $row["EDATE"],
                                                          "PERM_CD"        => $row["PERM_CD"]
                                                          )
                                               );

                //許可区分の変換
                //コンボボックス変更でクリア
                if($model->clear == 0){
                    $arg["data"][] = $row;
                }
        }

        //申請区分コンボボックスの中身を作成------------------------------
        $opt = array();
        $array_count = get_count($model->apply_data);
        if($array_count == 0)
        {
            $opt =array("label" => "　　　　　　　　　　　");
        }else{
            for($i=0;$i<$array_count;$i++)
            {
                $opt[] = array("label" => htmlspecialchars($model->apply_data[$i]["NAMECD2"].":".
                                                           $model->apply_data[$i]["NAME1"]),
                               "value" => $model->apply_data[$i]["NAMECD2"]);
            }
        }

        //申請区分コンボボックスを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "apply_div",
                            "size"      => "1",
                            "value"     => $model->apply_div,
                            "extrahtml" => "onChange=\"return Cleaning(this);\"",
                            "options"   => $opt));
        /*
        //読 込ボタンを作成する
        $objForm->ae( array("type"        => "submit",
                           "name"        => "btn_read",
                           "value"       => "読 込",
                           "extrahtml"   => "onclick=\"return btn_submit('list');\"" ));
        */
        $arg["APPLY"] = array("VAL" => $objForm->ge("apply_div"),
                              "BUTTON" => $objForm->ge("btn_read"));





        //許可区分コンボボックスの中身を作成------------------------------
        $opt = array();
        $array_count = get_count($model->perm_data);

        if($array_count == 0)
        {
            $opt =array("label" => "　　　　　　　　　　　");
        }else{
            for($i=0;$i<$array_count;$i++)
            {
                $opt[] = array("label" => htmlspecialchars($model->perm_data[$i]["NAMECD2"].":".
                                                           $model->perm_data[$i]["NAME1"]),
                               "value" => $model->perm_data[$i]["NAMECD2"]);
            }
        }

        //許可区分コンボボックスを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "perm_div",
                            "size"      => "1",
                            "value"     => $model->perm_div,
                            "extrahtml" => "onChange=\"return ChangeSelection_perm(this);\"",
                            "options"   => $opt));

        $arg["PERM"] = array("VAL" => $objForm->ge("perm_div"),
                              "BUTTON" => $objForm->ge("btn_read"));


        //エディットに渡す
        $arg["year"]["YEAR"] =$model->year;

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        # //hiddenを作成する
        # $objForm->ae( array("type"      => "hidden",
        #                    "name"      => "clear",
        #                    "value"     => "0"
        #                    ) );

#        if($model->sec_competence == DEF_NOAUTH){
#            $arg["close_win"] = "Close_Win()";
#        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg040Form1.html", $arg);
    }
}
?>
