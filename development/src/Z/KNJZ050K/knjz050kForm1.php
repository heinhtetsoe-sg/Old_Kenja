<?php

require_once('for_php7.php');

class knjz050kForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz050kindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        //銀行コンボ---NO001
        $result      = $db->query(knjz050kQuery::selectBankQuery());
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["BANKCD"]."　".$row["BANKNAME"],
                           "value" => $row["BANKCD"]);
        }
        $opt[] = array("label" => "　", "value" => "all");//全銀行表示
        if ($model->bankcmb == "") $model->bankcmb = $opt[0]["value"];
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "BANKCMB",
                            "size"        => "1",
                            "value"       => $model->bankcmb,
                            "extrahtml"   => "onChange=\"btn_submit('change')\"",
                            "options"     => $opt));
        $arg["BANKCMB"] = $objForm->ge("BANKCMB");

        //一覧データ
        //$result = $db->query(knjz050kQuery::selectQuery());
        $result = $db->query(knjz050kQuery::selectQuery($model->bankcmb));//NO001
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             array_walk($row, "htmlspecialchars_array");
             
             $arg["data"][] = $row; 
        }
        $result->free();
        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //銀行が変わった時---NO001
        if (!isset($model->warning) && $model->cmd == "change") {
            $arg["reload"]  = "parent.right_frame.location.href='knjz050kindex.php?cmd=edit';";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz050kForm1.html", $arg);
    }
} 
?>
