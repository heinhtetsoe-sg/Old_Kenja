<?php

require_once('for_php7.php');

class knjp100kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp100kindex.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();

        //生徒の名前を取得
        $row = $db->getRow(knjp100kQuery::getStudentName($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $row["SCHREGNO"]."　".$row["NAME_SHOW"];

        $result = $db->query(knjp100kQuery::getList($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            //リンク設定
            $row["APPLICATIONCD"] = View::alink("knjp100kindex.php", $row["APPLICATIONCD"], "target=bottom_frame",
                                    array("APPLICATIONCD" => $row["APPLICATIONCD"],
                                          "APPLI_NAME"    => $row["APPLICATIONNAME"],
                                          "cmd"           => "edit"));

            //金額フォーマット
            $row["APPLI_MONEY_DUE"] = (strlen($row["APPLI_MONEY_DUE"])) ? number_format($row["APPLI_MONEY_DUE"]): "";
            $row["APPLI_PAID_MONEY"] = (strlen($row["APPLI_PAID_MONEY"])) ? number_format($row["APPLI_PAID_MONEY"]): "";

            //日付変換
            $row["APPLIED_DATE"] = str_replace("-", "/", $row["APPLIED_DATE"]);
            $row["APPLI_PAID_DATE"] = str_replace("-", "/", $row["APPLI_PAID_DATE"]);

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);   

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
    
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp100kForm1.html", $arg);
    }
}
?>
