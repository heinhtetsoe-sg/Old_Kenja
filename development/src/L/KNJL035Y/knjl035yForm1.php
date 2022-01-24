<?php

require_once('for_php7.php');

class knjl035yForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl035yindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //テーブルの中身の作成
        $query = knjl035yQuery::selectQuery($model);
        $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $hash = array("cmd"             => "edit2",
                          "APPLICANTDIV"    => $row["APPLICANTDIV"],
                          "JUDGE_KIND"      => $row["JUDGE_KIND"]);

            $row["APPLICANTNAME"]  = $row["APPLICANTDIV"] .":". $row["APPLICANTNAME"] ;
            $row["JUDGENAME"] = ($row["JUDGE_KIND"] == "0") ? '基準' : $row["JUDGENAME"];
            $row["JUDGENAME"] = $row["JUDGE_KIND"] .":" . $row["JUDGENAME"];
            $row["JUDGE"] = View::alink("knjl035yindex.php", $row["JUDGENAME"], "target=\"right_frame\"", $hash);
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl035yForm1.html", $arg);
    }
}
?>
