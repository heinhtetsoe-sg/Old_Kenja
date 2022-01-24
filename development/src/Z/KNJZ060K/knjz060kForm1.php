<?php

require_once('for_php7.php');

class knjz060kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz060kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_pre_copy",
                            "value"     => "前年度からコピー",
                            "extrahtml" => "onclick=\"return btn_submit('copy');\"" ));
        $arg["pre_copy"] = $objForm->ge("btn_pre_copy");

        //リスト表示
        $result = $db->query(knjz060kQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["EXPENSE_M_CD"] = View::alink("knjz060kindex.php", $row["EXPENSE_M_CD"], "target=\"right_frame\"",
                                                 array("cmd"            => "edit",
                                                       "EXPENSE_L_CD"   => $row["EXPENSE_L_CD"],
                                                       "EXPENSE_M_CD"   => $row["EXPENSE_M_CD"] ));

            if ($row["EXPENSE_L_CD"] != "" && $row["NAME1"] != "") {
                $row["EXPENSE_L_CD"] = $row["EXPENSE_L_CD"].":".$row["NAME1"];
            }

            //費目小分類有無が有りなら費目小分類の合計値を表示
            if ($row["EXPENSE_S_EXIST_FLG"] == "1") {
                $row["EXPENSE_S_EXIST_FLG"] = "有";
                $row["EXPENSE_M_MONEY"] = (strlen($row["EXPENSE_S_MONEY"])) ? number_format($row["EXPENSE_S_MONEY"]): "";
            } else {
                $row["EXPENSE_S_EXIST_FLG"] = "無";
                $row["EXPENSE_M_MONEY"] = (strlen($row["EXPENSE_M_MONEY"])) ? number_format($row["EXPENSE_M_MONEY"]): "";
            }

            $row["DUE_DATE"] = str_replace("-", "/", $row["DUE_DATE"]);
            $row["BANK_TRANS_SDATE"] = str_replace("-", "/", $row["BANK_TRANS_SDATE"]);

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"][] = "parent.right_frame.location.href='knjz060kindex.php?cmd=edit"
                             . "&year=".$model->year."';";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz060kForm1.html", $arg);
    }
}
?>
