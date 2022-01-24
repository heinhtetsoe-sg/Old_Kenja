<?php

require_once('for_php7.php');

class knjp702Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp702index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp702Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjp702Query::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($bifKey !== $row["COLLECT_L_CD"]) {
                $cnt = $db->getOne(knjp702Query::getColectLCnt($model, $row["COLLECT_L_CD"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $row["COLLECT_L_CD"];

            $row["COLLECT_M_CD"] = View::alink("knjp702index.php", $row["COLLECT_M_CD"], "target=\"right_frame\"",
                                                 array("cmd"            => "edit",
                                                       "SCHOOL_KIND"    => $model->schoolKind,
                                                       "COLLECT_L_CD"   => $row["COLLECT_L_CD"],
                                                       "COLLECT_M_CD"   => $row["COLLECT_M_CD"] ));

            $row["COLLECT_M_MONEY"] = (strlen($row["COLLECT_M_MONEY"])) ? number_format($row["COLLECT_M_MONEY"]): "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if (!isset($model->warning) && (VARS::post("cmd") == "copy" || $model->cmd == "change")) {
            $arg["reload"] = "parent.right_frame.location.href='knjp702index.php?cmd=edit"
                           . "&year=".$model->year."&SCHOOL_KIND=".$model->schoolKind."';";

            if ($model->cmd == "change") {
                unset($model->exp_lcd);
                unset($model->exp_mcd);
            }
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp702Form1.html", $arg);
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
