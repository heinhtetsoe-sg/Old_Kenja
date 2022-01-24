<?php

require_once('for_php7.php');

class knjj200aForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjj200aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $schinfo = $db->getRow(knjj200aQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $schinfo["SCHREGNO"];
        $arg["NAME"]     = $schinfo["NAME"];

        //HR学籍委員会履歴データよりデータを取得
        if($model->schregno) {
            $result = $db->query(knjj200aQuery::getGrdCommitHistDat($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //ログイン年度のみリンクあり
                $executive = $row["EXECUTIVECD"]." ".$row["EXECUTIVENAME"];
                if ($row["YEAR"] == CTRL_YEAR) {
                    $row["EXECUTIVE"] = "<a href=\"knjj200aindex.php?cmd=edit&SCHREGNO=".$row["SCHREGNO"]."&DIV=".$row["DIV"]."&EXECUTIVECD=".$row["EXECUTIVECD"]."\" target=\"edit_frame\">".$executive."</a>";
                } else {
                    $row["EXECUTIVE"] = $executive;
                }

                //保護者氏名
                $div = $row["GUARD_NAME_DIV"];
                if (strlen($div)) {
                    $guardName = $row["GUARD_NAME{$div}"];
                    $guardName = ($guardName) ? $guardName : "";
                    $row["GUARD_NAME"] = "保護者{$div}（{$guardName}）";
                } else {
                    $row["GUARD_NAME"] = "";
                }

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
            $arg["reload"]  = "window.open('knjj200aindex.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj200aForm1.html", $arg);
    }
}
?>
