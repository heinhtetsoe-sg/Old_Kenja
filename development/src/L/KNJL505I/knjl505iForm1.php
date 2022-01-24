<?php

require_once('for_php7.php');

class knjl505iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl505iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl505iQuery::selectYearQuery($model);
        makeCombo($objForm, $arg, $db, $query, "year", $model->year, $extra, 1, "");

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //テーブルの中身の作成
        $query = knjl505iQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $hash = array("cmd"           => "edit2",
                          "year"          => $row["ENTEXAMYEAR"],
                          "APPLICANTDIV"  => $row["APPLICANTDIV"],
                          "TESTDIV"       => $row["TESTDIV"],
                          "GENERAL_DIV"   => $row["GENERAL_DIV"],
                          "GENERAL_CD"    => $row["GENERAL_CD"]);

            $row["GENERAL_CD"]    = View::alink("knjl505iindex.php", $row["GENERAL_CD"], "target=\"right_frame\"", $hash);
            foreach ($model->subjectList as $key => $value) {
                if ($row["REMARK1"] == $model->subjectList[$key]["value"]) {
                    $row["REMARK1"] = $model->subjectList[$key]["label"];
                }
            }
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjl505iindex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl505iForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["DEFAULT"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
