<?php

require_once('for_php7.php');

class knjl004aForm1
{
    function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl004aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボボックスを作成する
        $query = knjl004aQuery::selectYearQuery();
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "year", $model->year, $extra, 1, "");

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //受験校種コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl004aQuery::getNameMst($model, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //リスト作成
        $query  = knjl004aQuery::selectQuery($model);
        $result = $db->query($query);
        while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $hash = array("cmd"             => "edit2",
                          "APPLICANTDIV"    => $row["APPLICANTDIV"],
                          "PATTERN_CD"      => $row["PATTERN_CD"],
                          "TOKUTAI_SELECT"  => $row["TOKUTAI_SELECT"]);

            $title = $row["PATTERN_CD"];
            if ($row["PATTERN_CD"] == "001") {
                if ($row["TOKUTAI_SELECT"] == "1") {
                    $row["TOKUTAI_SELECT_NAME"] = "特待生";
                    $title = $row["PATTERN_CD"]."特待";
                } elseif ($row["TOKUTAI_SELECT"] == "2") {
                    $row["TOKUTAI_SELECT_NAME"] = "特待生以外";
                    $title = $row["PATTERN_CD"]."特以";
                }
            }

            $row["PATTERN_CD"] = View::alink("knjl004aindex.php", $title, "target=\"right_frame\"", $hash);
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"] = "parent.right_frame.location.href='knjl004aindex.php?cmd=edit"
                           . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl004aForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    if ($name == "year") {
        $value = ($value != "" && $value_flg) ? $value : (CTRL_YEAR + 1);
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
