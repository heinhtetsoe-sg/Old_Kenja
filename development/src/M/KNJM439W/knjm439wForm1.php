<?php
class knjm439wform1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjm439windex.php", "", "edit");

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $query = knjm439wQuery::getSchregno_name($model->schregno);
        $Row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"]     = $Row["NAME_SHOW"];

        if ($model->schregno) {
            $query = knjm439wQuery::getAttendData($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["TEST_DATE"]  = str_replace("-","/",$row["TEST_DATE"]);
                $row["ATTEND"]  = $row["ATTEND"] == "1" ? "出席" : "";
                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "clear", "0");

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list") {
            $arg["reload"]  = "window.open('knjm439windex.php?cmd=edit&SCHREGNO=".$model->schregno."','edit_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm439wForm1.html", $arg);
    }
}
?>
