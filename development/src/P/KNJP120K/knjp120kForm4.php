<?php

require_once('for_php7.php');

require_once('knjp120kQuery2.inc');
class knjp120kform4
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp190kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //データを取得
        if($model->schregno)
        {
            $result = $db->query(knjp120kQuery2::getlist2($model->schregno));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row["GRANTCDNAME"] = $row["GRANTCD"]."：".$row["GRANTNAME"];

                //日付表示
                $row["GRANTSDATE"]  = str_replace("-","/",$row["GRANTSDATE"]);
                $row["GRANTEDATE"]  = str_replace("-","/",$row["GRANTEDATE"]);

                //金額表示
                $row["GRANT_MONEY"] = (strlen($row["GRANT_MONEY"])) ? number_format($row["GRANT_MONEY"]): "";

                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"));

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp120kForm4.html", $arg);
    }
}
?>
