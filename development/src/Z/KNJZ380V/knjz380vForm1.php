<?php

require_once('for_php7.php');

class knjz380vForm1
{
    function main(&$model)
    {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjz380vindex.php", "", "edit");

    //年度を表示
    $arg["header"] = CTRL_YEAR;

    //コピーボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_copy",
                        "value"       => "前年度からコピー",
                        "extrahtml"   => "style=\"width:130px\" onclick=\"return btn_submit('copy');\"" ) );

    $arg["btn_copy"] = $objForm->ge("btn_copy");

    //出欠集計範囲表示切替
    if ($model->Properties["Semester_Detail_Hyouji"] == "1") {
        $arg["sem_detail"] = 1;
    }
    //見込点入力表示切替
    if ($model->Properties["useMikomiFlg"] == "1") {
        $arg["useMikomiFlg"] = 1;
    }
    //参考点入力表示切替
    if ($model->Properties["useSankouFlg"] == "1") {
        $arg["useSankouFlg"] = 1;
    }
    //備考入力表示切替
    if ($model->Properties["useRemarkFlg"] == "1") {
        $arg["useRemarkFlg"] = 1;
    }

    //リスト内表示
    $db = Query::dbCheckOut();
    $query  = knjz380vQuery::getListdata($model);
    $result = $db->query($query);

    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        //権限チェック
        if($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT){
            break;
        }

         //レコードを連想配列のまま配列$arg[data]に追加していく。
         array_walk($row, "htmlspecialchars_array");
         $row["TESTKIND_SHOW"] = $row["TESTKINDCD"]." ".$row["TESTKINDNAME"];
         //リンク作成
         $row["TESTKIND_SHOW"] = View::alink("knjz380vindex.php", $row["TESTKIND_SHOW"], "target=\"right_frame\"",
                                          array("cmd"  =>"edit",
                                                "SEMESTER"   =>$row["SEMESTER"],
                                                "TESTKINDCD" =>$row["TESTKINDCD"],
                                                "TESTITEMCD" =>$row["TESTITEMCD"],
                                                "SCORE_DIV"  =>$row["SCORE_DIV"],
                                                "UPDATED"    =>$row["UPDATED"]
                                                ));
         $row["TESTITEM_SHOW"] = $row["TESTITEMCD"].'-'.$row["SCORE_DIV"]." ".$row["TESTITEMNAME"];
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
        $arg["reload"] = "window.open('knjz380vindex.php?cmd=edit','right_frame');";
    }
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjz380vForm1.html", $arg);
    }
}    
?>
