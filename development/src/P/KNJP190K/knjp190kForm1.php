<?php

require_once('for_php7.php');

class knjp190kform1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp190kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $query = knjp190kQuery::getSchregno_name($model->schregno);
        $Row   = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME_SHOW"] = $Row["NAME_SHOW"];

        //データを取得
        if($model->schregno)
        {
            $result = $db->query(knjp190kQuery::getlist($model->schregno));
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

        if (VARS::get("cmd") == "right_list"){
                $arg["reload"]  = "window.open('knjp190kindex.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp190kForm1.html", $arg);
    }
}
?>
