<?php

require_once('for_php7.php');

class knje031Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("right_list", "POST", "knje031index.php", "", "edit");

        //登録・更新・削除の際、履歴を再読込する
        if (VARS::get("cmd") == "from_edit"){
            $arg["reload"] = "window.open('knje031index.php?cmd=right_list','right_frame');";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報表示
        $baseMst = $db->getRow(knje031Query::getSchBaseMst($model), DB_FETCHMODE_ASSOC);
        $arg["SCHINFO"] = $baseMst["SCHREGNO"].'　'.$baseMst["NAME_SHOW"];

        //課程入学日付
        $curriculum_year = $db->getOne(knje031Query::getCurriculumYear($model));
        $entdate = ($curriculum_year) ? $curriculum_year.'-04-01' : $baseMst["ENT_DATE"];

        //履歴表示
        $result = $db->query(knje031Query::getTransferList($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //入学日付後の異動開始日にリンクをはる
            if ($row["TRANSFER_SDATE"] >= $entdate) {
                $row["TRANSFERNAME"] = "<a href=\"knje031index.php?cmd=edit&TRANSFERCD=".$row["TRANSFERCD"]."&TRANSFER_SDATE=".$row["TRANSFER_SDATE"]."\" target=\"edit_frame\">".$row["TRANSFERCD"]."：".$row["TRANSFERNAME"] ."</a>";
            } else {
                $row["TRANSFERNAME"] = $row["TRANSFERCD"]."：".$row["TRANSFERNAME"];
            }

            $row["TRANSFER_SDATE"] = ($row["TRANSFER_SDATE"]) ? str_replace("-","/",$row["TRANSFER_SDATE"]) : "";
            $row["TRANSFER_EDATE"] = ($row["TRANSFER_EDATE"]) ? str_replace("-","/",$row["TRANSFER_EDATE"]) : "";

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "from_list"){
            $arg["reload"] = "window.open('knje031index.php?cmd=from_right&SCHREGNO=$model->schregno','edit_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje031Form1.html", $arg);
    }
}
?>
