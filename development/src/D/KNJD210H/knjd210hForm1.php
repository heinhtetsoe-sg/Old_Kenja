<?php

require_once('for_php7.php');

class knjd210hform1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd210hindex.php", "", "main");

        $db = Query::dbCheckOut();

        $arg["jscript"] = "";
        $arg["Closing"] = "";
        
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        //事前処理チェック
        if (!knjd210hQuery::ChecktoStart($db)) {
            $arg["Closing"] = " closing_window();";
        }

        //処理年度
        $arg["sepa"]["YEAR"] = CTRL_YEAR . "年度";

        //学期コンボボックス
        $opt_seme = array();
        $result = $db->query(knjd210hQuery::getSemester());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["SEMESTER"] == "9") $row["SEMESTERNAME"] = ($row["SEMESTERNAME"]=="学年末") ? "学年" : $row["SEMESTERNAME"] ;
            $opt_seme[] = array("label" => $row["SEMESTERNAME"],
                                "value" => $row["SEMESTER"]);
        }

        if(!isset($model->seme)) $model->seme = $opt_seme[0]["value"];

        $objForm->ae( array("type"      => "select",
                            "name"      => "seme",
                            "size"      => "1",
                            "value"     => $model->seme,
                            "extrahtml" => "",
                            "options"   => $opt_seme) );

        $arg["sepa"]["SEMESTER"] = $objForm->ge("seme");

        //学年コンボボックス
        $opt_grad = array();
        $result = $db->query(knjd210hQuery::getGrade());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_grad[] = array("label" => $row["GRADE_NAME1"],
                                "value" => $row["GRADE"]);
        }

        if(!isset($model->grad)) $model->grad = $opt_grad[0]["value"];

        $objForm->ae( array("type"      => "select",
                            "name"      => "grad",
                            "size"      => "1",
                            "value"     => $model->grad,
                            "extrahtml" => "",
                            "options"   => $opt_grad) );

        $arg["sepa"]["GRADE"] = $objForm->ge("grad");

        //生成済み一覧
        $result = $db->query(knjd210hQuery::selectQuery());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["SEMESTER"] == "9") $row["SEMESTERNAME"] = ($row["SEMESTERNAME"]=="学年末") ? "学年" : $row["SEMESTERNAME"] ;
            $row["GRADE"]   = $row["GRADE_NAME1"];
            $row["UPDATED"] = str_replace("-","/",$row["UPDATED"]);
            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);


        //更新ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");


        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );


        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd210hForm1.html", $arg);
    }       
}       
?>
