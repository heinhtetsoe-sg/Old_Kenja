<?php

require_once('for_php7.php');

class knjz380Form1
{
    function main(&$model)
    {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjz380index.php", "", "edit");

    //年度を表示
    $arg["header"] = CTRL_YEAR;

    //コピーボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_copy",
                        "value"       => "前年度からコピー",
                        "extrahtml"   => "style=\"width:130px\" onclick=\"return btn_submit('copy');\"" ) );

    $arg["btn_copy"] = $objForm->ge("btn_copy");

    //リスト内表示
    $db = Query::dbCheckOut();
    $query  = knjz380Query::getListdata($model);
    $result = $db->query($query);

    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        //権限チェック
        if($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT){
            break;
        }

         //レコードを連想配列のまま配列$arg[data]に追加していく。
         array_walk($row, "htmlspecialchars_array");
         //リンク作成
         $row["TESTKIND_SHOW"] = View::alink("knjz380index.php", $row["TESTKIND_SHOW"], "target=\"right_frame\"",
                                          array("cmd"  =>"edit",
                                                "TESTKINDCD" =>$row["TESTKINDCD"],
                                                "TESTITEMCD" =>$row["TESTITEMCD"],
                                                "UPDATED"    =>$row["UPDATED"]
                                                ));
         $row["TESTITEM_SHOW"] = $row["TESTITEMCD"]." ".$row["TESTITEMNAME"];
         $arg["data"][] = $row;
    }
    $result->free();
    Query::dbCheckIn($db);

    //hiddenを作成する
    $objForm->ae( array("type"  => "hidden",
                        "name"  => "cmd"
                        ) );

    //権限
    if($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT){
        $arg["Closing"]  = " closing_window(); " ;
    }
    if ($model->cmd == "change"){
        $arg["reload"] = "window.open('knjz380index.php?cmd=edit','right_frame');";
    }
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjz380Form1.html", $arg);
    }
}    
?>
