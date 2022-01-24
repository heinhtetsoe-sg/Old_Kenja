<?php

require_once('for_php7.php');
class knjm431wform1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjm431windex.php", "", "edit");

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $query = knjm431wQuery::getSchregno_name($model->schregno);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"]     = $Row["NAME_SHOW"];

        if ($model->schregno) {
            $query = knjm431wQuery::getScoreData($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["TEST_DATE"]  = str_replace("-", "/", $row["TEST_DATE"]);
                if ($model->Properties["knjm431wUseGakkiHyouka"] == "1" && $model->sendField["SEND_TESTTYPE"] == '990008') {
                    $row["SCORE"] = $row["VALUE"];
                }

                if (strlen($model->Properties["knjm431wPassScore"]) && $row["GOUHI"] == "否") {
                    $row["SCORE"] = "<span style=\"color: red; \">".$row["SCORE"]."</span>";
                }

                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "clear", "0");

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list") {
            $arg["reload"]  = "window.open('knjm431windex.php?cmd=edit&SCHREGNO=".$model->schregno."','edit_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm431wForm1.html", $arg);
    }
}
