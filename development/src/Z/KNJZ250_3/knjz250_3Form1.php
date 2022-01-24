<?php

require_once('for_php7.php');

class knjz250_3Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz250_3index.php", "", "edit");

        $db = Query::dbCheckOut();
        $query = knjz250_3Query::getAllRow();
        $result = $db->query($query);
        $i = 0;
        $rcnt = 0;
        $rmax = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $setRow = array();
            if ($rmax==$rcnt) {
                $setRow["PRI_DISPCD"] = 1;
                $rmax = $row["R_MAX"];
                $rcnt = 1;
                $setRow["CERTIF_DIV"] = $row["CERTIF_DIV"]; 
                $setRow["CERTIF_DIV_NAME"] = $row["CERTIF_DIV_NAME"]; 
                $setRow["PRI_ROWSPAN"] = $rmax; 
            } else {
                $setRow["PRI_DISPCD"] = 0;
                $rcnt++;
            }
            $setRow["KINDNAME"] = $row["CERTIF_KINDCD"]." ".$row["KINDNAME"];
            $arg["data"][] = $setRow;
        }
        $result->free();


        //$query = "SELECT * FROM CERTIF_KIND_MST ORDER BY CERTIF_KINDCD";
        //$result = $db->query($query);
        //$i=0;
        //while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        //{
        //    //レコードを連想配列のまま配列$arg[data]に追加していく。
        //    array_walk($row, "htmlspecialchars_array");
        //    $arg["data"][$i]["CERTIF_KINDCD"] = $row["CERTIF_KINDCD"]; 
        //    $arg["data"][$i]["KINDNAME"]      = $row["KINDNAME"]; 
        //    $arg["data"][$i]["ISSUE"]         = $show_I[$row["ISSUECD"]]; 
        //    $arg["data"][$i]["STUDENT"]       = $show_S[$row["STUDENTCD"]]; 
        //    $arg["data"][$i]["GRADUATE"]      = $show_S[$row["GRADUATECD"]]; 
        //    $arg["data"][$i]["DROPOUT"]       = $show_S[$row["DROPOUTCD"]];
        //    if ($row["ELAPSED_YEARS"]) {
        //        $arg["data"][$i]["ELAPSED_YEARS"] = $row["ELAPSED_YEARS"].'年';
        //    }
        //    if ($model->Properties["certif_no_8keta"] == "1") {
        //        $arg["data"][$i]["CERTIF_DIV"]      = $row["CERTIF_DIV"];
        //    }
        //    $arg["data"][$i]["CURRENT_PRICE"]      = $row["CURRENT_PRICE"];
        //    $arg["data"][$i]["GRADUATED_PRICE"]    = $row["GRADUATED_PRICE"];
        //    $arg["data"][$i]["ISSUENO_AUTOFLG"]    = ($row["ISSUENO_AUTOFLG"] == 1)?'ON':'';
        //    $i++;
        //}
        //$result->free();
        //
        //if ($model->Properties["certif_no_8keta"] == "1") {
        //    $arg["certif_no_8keta"] = 1;
        //}

        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz250_3Form1.html", $arg); 
    }
}
?>
