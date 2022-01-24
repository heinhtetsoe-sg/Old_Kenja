<?php

require_once('for_php7.php');

require_once('knjp120kQuery2.inc');
class knjp120kForm5 
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp040kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        $query = knjp120kQuery2::getRow($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)){
            $arg["data"]["BANKCD"] = $row["BANKCD"] .":" .htmlspecialchars($row["BANKNAME_KANA"]);
            $arg["data"]["BRANCHCD"] = $row["BRANCHCD"] .":" .htmlspecialchars($row["BRANCHNAME_KANA"]);
            $arg["data"]["ACCOUNTNO"] = $row["ACCOUNTNO"];
            $arg["data"]["ACCOUNTNAME"] = $row["ACCOUNTNAME"];
            $arg["data"]["DEPOSIT_ITEM"] = $row["DEPOSIT_ITEM"] .":" .htmlspecialchars($row["DEPOSIT_ITEM2"]);
            if ($row["RELATIONSHIP"] == "00") $row["RELATIONSHIP2"] = "本人";
            $arg["data"]["RELATIONSHIP"] = $row["RELATIONSHIP"] .":" .htmlspecialchars($row["RELATIONSHIP2"]);
        }
        Query::dbCheckIn($db);

        View::toHTML($model, "knjp120kForm5.html", $arg);
    }
}
?>
