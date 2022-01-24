<?php

require_once('for_php7.php');

class knjp705Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp705index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = CTRL_YEAR;

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjp705Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($bifKey !== $row["COLLECT_BANK_CD"]) {
                $cnt = $db->getOne(knjp705Query::getColectBankCnt($row["COLLECT_BANK_CD"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $row["COLLECT_BANK_CD"];

            if ($row["COLLECT_BANK_DIV"] == '1') {
                $row["COLLECT_BANK_DIV_SET"] = '銀行用';
            } else {
                $row["COLLECT_BANK_DIV_SET"] = '郵便局';
            }
            $row["COLLECT_BANK_DIV_SET"] = View::alink("knjp705index.php", $row["COLLECT_BANK_DIV_SET"], "target=\"right_frame\"",
                                                  array("cmd"              => "edit",
                                                        "COLLECT_BANK_CD"  => $row["COLLECT_BANK_CD"],
                                                        "COLLECT_BANK_DIV" => $row["COLLECT_BANK_DIV"] ));
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"]  = "window.open('knjp705index.php?cmd=edit','right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp705Form1.html", $arg);
    }
}
?>
