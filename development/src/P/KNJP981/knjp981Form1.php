<?php

require_once('for_php7.php');

class knjp981Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp981index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp981Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //収入・支出
        $opt = array();
        $opt[] = array('label' => '全て', 'value' => 'all');
        $opt[] = array('label' => '1:収入', 'value' => '1');
        $opt[] = array('label' => '2:支出', 'value' => '2');
        $extra = " onchange=\"return btn_submit('change');\"";
        $arg["SET_LEVY_IN_OUT_DIV"] = knjCreateCombo($objForm, "SET_LEVY_IN_OUT_DIV", $model->levy_in_out_div, $opt, $extra, 1);

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjp981Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($bifKey !== $row["LEVY_L_CD"]) {
                if ($model->levy_in_out_div === '1' || $model->levy_in_out_div === '2') {
                    $cnt = $db->getOne(knjp981Query::getLevyLCnt($model, $row["LEVY_L_CD"], $row["LEVY_IN_OUT_DIV"]));
                } else {
                    $cnt = $db->getOne(knjp981Query::getLevyLCnt($model, $row["LEVY_L_CD"], ""));
                }
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $row["LEVY_L_CD"];

            $row["LEVY_M_CD"] = View::alink("knjp981index.php", $row["LEVY_M_CD"], "target=\"right_frame\"",
                                                 array("cmd"            => "edit",
                                                       "SCHOOL_KIND"    => $model->schoolKind,
                                                       "LEVY_L_CD"      => $row["LEVY_L_CD"],
                                                       "LEVY_M_CD"      => $row["LEVY_M_CD"] ));

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
        knjCreateHidden($objForm, "cmd");

        if (!isset($model->warning) && $model->cmd == "change") {
            $arg["reload"] = "parent.right_frame.location.href='knjp981index.php?cmd=edit"
                           . "&year=".$model->year."&SCHOOL_KIND=".$model->schoolKind."';";

            if ($model->cmd == "change") {
                unset($model->exp_lcd);
                unset($model->exp_mcd);
            }
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp981Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
