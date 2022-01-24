<?php

require_once('for_php7.php');

class knjz250_2Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz250_2index.php", "", "edit");

        $db = Query::dbCheckOut();
        $result = $db->query(knjz250_2Query::getIssue());

        //事務発行区分
        $show_I = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $show_I[$row["NAMECD2"]] = $row["NAMECD2"]."  ".$row["NAME1"];
        }
        //在学生、卒業生、転出・退学者
        $result = $db->query(knjz250_2Query::getStudent());
        $show_S = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $show_S[$row["NAMECD2"]] = $row["NAMECD2"]."  ".$row["NAME1"];
        }
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $arg["use_certif_kind_mst_school_kind"] = "1";
        }

        $query = knjz250_2Query::getCertifKindMst($model);
        $result = $db->query($query);
        $i=0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][$i]["CERTIF_KINDCD"] = $row["CERTIF_KINDCD"]; 
            $arg["data"][$i]["KINDNAME"]      = $row["KINDNAME"]; 
            $arg["data"][$i]["ISSUE"]         = $show_I[$row["ISSUECD"]]; 
            $arg["data"][$i]["STUDENT"]       = $show_S[$row["STUDENTCD"]]; 
            $arg["data"][$i]["GRADUATE"]      = $show_S[$row["GRADUATECD"]]; 
            $arg["data"][$i]["DROPOUT"]       = $show_S[$row["DROPOUTCD"]];
            if ($row["ELAPSED_YEARS"]) {
                $arg["data"][$i]["ELAPSED_YEARS"] = $row["ELAPSED_YEARS"].'年';
            }
            if ($model->Properties["certif_no_8keta"] == "1") {
                $arg["data"][$i]["CERTIF_DIV"]      = $row["CERTIF_DIV"];
            }
            $arg["data"][$i]["CURRENT_PRICE"]      = $row["CURRENT_PRICE"];
            $arg["data"][$i]["GRADUATED_PRICE"]    = $row["GRADUATED_PRICE"];
            $arg["data"][$i]["ISSUENO_AUTOFLG"]    = ($row["ISSUENO_AUTOFLG"] == 1)?'ON':'';
            if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
                $arg["data"][$i]["CERTIF_SCHOOL_KIND_NAME"] = $row["CERTIF_SCHOOL_KIND_NAME"];
            }
            $i++;
        }
        $result->free();

        if ($model->Properties["certif_no_8keta"] == "1") {
            $arg["certif_no_8keta"] = 1;
        }

        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz250_2Form1.html", $arg); 
    }
}
?>
