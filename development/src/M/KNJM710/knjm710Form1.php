<?php

require_once('for_php7.php');

class knjm710Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjm710index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjm710Query::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($bifKey !== $row["COLLECT_L_CD"]) {
                $cnt = $db->getOne(knjm710Query::getColectLCnt($row["COLLECT_L_CD"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $row["COLLECT_L_CD"];

            $row["COLLECT_M_CD"] = View::alink("knjm710index.php", $row["COLLECT_M_CD"], "target=\"right_frame\"",
                                                 array("cmd"            => "edit",
                                                       "COLLECT_L_CD"   => $row["COLLECT_L_CD"],
                                                       "COLLECT_M_CD"   => $row["COLLECT_M_CD"] ));

            //費目小分類有無が有りなら費目小分類の合計値を表示
            if ($row["COLLECT_S_EXIST_FLG"] == "1") {
                $row["COLLECT_S_EXIST_FLG"] = "有";
                //$row["COLLECT_M_MONEY"] = (strlen($row["COLLECT_S_MONEY"])) ? number_format($row["COLLECT_S_MONEY"]): "";
            } else {
                $row["COLLECT_S_EXIST_FLG"] = "無";
                //$row["COLLECT_M_MONEY"] = (strlen($row["COLLECT_M_MONEY"])) ? number_format($row["COLLECT_M_MONEY"]): "";
            }
            
            if ($row["PAY_DIV"] === '1') {
                $row["PAY_DATE"] = '自動振替日：'.str_replace("-", "/", $row["PAY_DATE"]);
            } else if ($row["PAY_DIV"] === '2'){
                $row["PAY_DATE"] = '　納入期限：'.str_replace("-", "/", $row["PAY_DATE"]);
            }
            $row["COLLECT_M_MONEY"] = (strlen($row["COLLECT_M_MONEY"])) ? number_format($row["COLLECT_M_MONEY"]): "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjm710index.php?cmd=edit"
                             . "&year=".$model->year."';";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm710Form1.html", $arg);
    }
}
?>
