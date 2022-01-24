<?php

require_once('for_php7.php');

class knjd010Form1
{
    function main(&$model)
    {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjd010index.php", "", "edit");

    //年度を表示
    $arg["header"] = CTRL_YEAR;

    //教科名コンボボックス作成------------------------------
    $db     = Query::dbCheckOut();
    $query  = knjd010Query::getClassName($model);
    $result = $db->query($query);
    $opt_Class = array();

    //更新可のとき、教科コンボボックスの最初はALL
    if($model->sec_competence == DEF_UPDATABLE){
        $opt_Class[] = array("label" => "00 ALL",
                             "value" => "00");
    }
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if (!isset($model->classcd)) $model->classcd = $row["CLASSCD"];
        $opt_Class[] = array("label" => $row["CLASSCD"] ."  " .htmlspecialchars($row["CLASSNAME"]),
                             "value" => $row["CLASSCD"]);
    }
    //教科名コンボボックス
    $objForm->ae( array("type"        => "select",
                        "name"        => "CLASSCD",
                        "extrahtml"   => "onChange=\"btn_submit('change')\"",
                        "value"       => $model->classcd,
                        "options"     => $opt_Class
                        ));
    $arg["CLASSCD"] = $objForm->ge("CLASSCD");

    if($model->flg1==0){
            $arg["Closing"]  = " closing_window(1); " ;
    }

    //コピーボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_copy",
                        "value"       => "前年度からコピー",
                        "extrahtml"   => "style=\"width:130px\" onclick=\"return btn_submit('copy');\"" ) );

    $arg["btn_copy"] = $objForm->ge("btn_copy");

    //リスト項目ソート部作成(科目名)
    $S_SORT = "<a href=\"knjd010index.php?cmd=list&SUBCLASS_SORT=".(($model->subclass_sort == "ASC")? "DESC" : "ASC")."\" target=\"left_frame\" STYLE=\"color:white\">科目名".(($model->subclass_sort == "ASC")? "▲" : "▼")."</a>";

    $arg["S_SORT"] = $S_SORT;

    //リスト項目ソート部作成(テスト種別)
    $T_SORT = "<a href=\"knjd010index.php?cmd=list&TESTKIND_SORT=".(($model->testkind_sort == "ASC")? "DESC" : "ASC")."\" target=\"left_frame\" STYLE=\"color:white\">テスト種別".(($model->testkind_sort == "ASC")? "△" : "▽")."</a>";

    $arg["T_SORT"] = $T_SORT;

    //リスト内表示
    $query  = knjd010Query::getListdata($model);
    $db = Query::dbCheckOut();
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
         $row["SUBCLASS_SHOW"] = View::alink("knjd010index.php", $row["SUBCLASS_SHOW"], "target=\"right_frame\"",
                                          array("cmd"  =>"edit",
                                                "SUBCLASSCD" =>$row["SUBCLASSCD"],
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
        $arg["reload"] = "window.open('knjd010index.php?cmd=edit','right_frame');";
    }
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjd010Form1.html", $arg);
    }
}    
?>
