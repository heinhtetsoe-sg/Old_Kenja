<?php

require_once('for_php7.php');

class knjz351vFormList
{
    function main(&$model)
    {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjz351vindex.php", "", "edit");

    //年度を表示
    $arg["header"] = CTRL_YEAR;

    //コピーボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_copy",
                        "value"       => "前年度からコピー",
                        "extrahtml"   => "style=\"width:130px\" onclick=\"return btn_submit('copy');\"" ) );

    $arg["btn_copy"] = $objForm->ge("btn_copy");

    //出欠集計範囲表示切替
    if($model->Properties["Semester_Detail_Hyouji"] == "1") {
        $arg["sem_detail"] = 1;
    }

    //リスト内表示
    $db = Query::dbCheckOut();
    $query  = knjz351vQuery::getListdata($model);
    $result = $db->query($query);
    $counter = 0;
    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
         //レコードを連想配列のまま配列$arg[data]に追加していく。
         array_walk($row, "htmlspecialchars_array");
         //リンク作成
         $row["SAKI_LABEL_SHOW"] = View::alink("knjz351vindex.php", $row["SAKI_LABEL"], "target=\"right_frame\"",
                                          array("cmd"  =>"subclasscd",
                                                "SUBCLASSCD"   =>$row["KAMOKU_VALUE"],
                                                "SAKI_TESTCD"  =>$row["SAKI_VALUE"]
                                                ));
            
        if ($bifKey !== $row["KAMOKU_VALUE"].':'.$row["SAKI_VALUE"]) {
            $cnt = $db->getOne(knjz351vQuery::getListdata($model, $row["KAMOKU_VALUE"], $row["SAKI_VALUE"]));
            $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
        }
        $bifKey = $row["KAMOKU_VALUE"].':'.$row["SAKI_VALUE"];
        $counter++;
        $arg["data"][] = $row;
    }
    $result->free();
    Query::dbCheckIn($db);

    //hiddenを作成する
    $objForm->ae( array("type"  => "hidden",
                        "name"  => "cmd"
                        ) );

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjz351vFormList.html", $arg);
    }
}    
?>
