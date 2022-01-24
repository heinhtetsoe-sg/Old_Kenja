<?php

require_once('for_php7.php');

class knjmp981Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjmp981index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_pre_copy",
                            "value"     => "前年度からコピー",
                            "extrahtml" => "onclick=\"return btn_submit('copy');\"" ));
        $arg["pre_copy"] = $objForm->ge("btn_pre_copy");

        //収入・支出
        $opt = array();
        $opt[] = array('label' => '全て', 'value' => 'all');
        $opt[] = array('label' => '1:収入', 'value' => '1');
        $opt[] = array('label' => '2:支出', 'value' => '2');
        $extra = " onchange=\"return btn_submit('change');\"";
        $arg["SET_LEVY_IN_OUT_DIV"] = knjCreateCombo($objForm, "SET_LEVY_IN_OUT_DIV", $model->levy_in_out_div, $opt, $extra, 1);

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjmp981Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($bifKey !== $row["LEVY_L_CD"]) {
                if ($model->levy_in_out_div === '1' || $model->levy_in_out_div === '2') {
                    $cnt = $db->getOne(knjmp981Query::getLevyLCnt($row["LEVY_L_CD"], $row["LEVY_IN_OUT_DIV"]));
                } else {
                    $cnt = $db->getOne(knjmp981Query::getLevyLCnt($row["LEVY_L_CD"], ""));
                }
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $row["LEVY_L_CD"];

            $row["LEVY_M_CD"] = View::alink("knjmp981index.php", $row["LEVY_M_CD"], "target=\"right_frame\"",
                                                 array("cmd"         => "edit",
                                                       "LEVY_L_CD"   => $row["LEVY_L_CD"],
                                                       "LEVY_M_CD"   => $row["LEVY_M_CD"] ));

            //費目小分類有無が有りなら費目小分類の合計値を表示
            if ($row["LEVY_S_EXIST_FLG"] == "1") {
                $row["LEVY_S_EXIST_FLG"] = "有";
            } else {
                $row["LEVY_S_EXIST_FLG"] = "無";
            }
            if ($row["LEVY_IN_OUT_DIV"] === '1') {
                $row["LEVY_IN_OUT_DIV"] = "収入";
            } else {
                $row["LEVY_IN_OUT_DIV"] = "支出";
            }
            //収入のみ
            //雑収入
            if ($row["ZATU_FLG"] == "1") {
                $row["ZATU_FLG"] = "レ";
            } else {
                $row["ZATU_FLG"] = "";
            }
            //繰越金
            if ($row["KURIKOSI_FLG"] == "1") {
                $row["KURIKOSI_FLG"] = "レ";
            } else {
                $row["KURIKOSI_FLG"] = "";
            }
            //支出のみ
            //予備費
            if ($row["YOBI_FLG"] == "1") {
                $row["YOBI_FLG"] = "レ";
            } else {
                $row["YOBI_FLG"] = "";
            }

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy" || VARS::post("cmd") == "change"){
            $arg["reload"][] = "parent.right_frame.location.href='knjmp981index.php?cmd=edit"
                             . "&year=".$model->year."';";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjmp981Form1.html", $arg);
    }
}
?>
