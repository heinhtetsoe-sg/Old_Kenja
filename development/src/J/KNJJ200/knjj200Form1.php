<?php

require_once('for_php7.php');

class knjj200Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjj200index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $schinfo = $db->getRow(knjj200Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $schinfo["SCHREGNO"];
        $arg["NAME"]     = $schinfo["NAME"];

        //HR学籍委員会履歴データよりデータを取得
        if($model->schregno) {
            $result = $db->query(knjj200Query::getGrdCommitHistDat($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $arg["data"][] = $row;
            }
        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "clear", "0");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){
            $arg["reload"]  = "window.open('knjj200index.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj200Form1.html", $arg);
    }
}
?>
