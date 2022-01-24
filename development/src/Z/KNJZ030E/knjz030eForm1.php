<?php

require_once('for_php7.php');

class knjz030eForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz030eindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $result = $db->query(knjz030eQuery::selectYearQuery());
        $opt    = array();
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) {
            $opt[] = array("label" => CTRL_YEAR+1, "value" => CTRL_YEAR+1);
            unset($model->year);
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["ENTEXAMYEAR"],
                               "value" => $row["ENTEXAMYEAR"]);
                if ($model->year == $row["ENTEXAMYEAR"]) {
                    $flg = true;
                }
            }
        }
        $result->free();

        //初期表示の年度設定
        if (!$flg) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR + 1;
            } elseif ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];
            } elseif ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->year = $db->getOne(knjz030eQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz030eindex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //年度コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["ENTEXAMYEAR"] = knjCreateCombo($objForm, "ENTEXAMYEAR", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjz030eQuery::getNameMst($model, "L003", "default");
        makeCombo($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //会場グループ件数取得
        $cntArray = array();
        $result = $db->query(knjz030eQuery::Listdata($model, "cnt"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cntArray[$row["DESIREDIV"]."-".$row["TESTDIV1"]."-".$row["SUC_DESIREDIV"]] = $row["CNT"];
        }

        //リスト表示
        $befKey = "";
        $tmp = array();
        $query = knjz030eQuery::Listdata($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg["data"]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $key = $row["DESIREDIV"]."-".$row["TESTDIV1"]."-".$row["SUC_DESIREDIV"];

            $hash1 = array("cmd"                => "edit2",
                           "ENTEXAMYEAR"        => $model->year,
                           "APPLICANTDIV"       => $model->applicantdiv,
                           "DESIREDIV"          => $row["DESIREDIV"],
                           "TESTDIV1"           => $row["TESTDIV1"],
                           "SUC_DESIREDIV"      => $row["SUC_DESIREDIV"]);
            $row["SUC_DESIREDIV_NAME"] = View::alink("knjz030eindex.php", $row["SUC_DESIREDIV_NAME"], "target=\"right_frame\"", $hash1) ;

            //重複した会場はまとめる
            if ($befKey !== $key) {
                $row["ROWSPAN"] = ($cntArray[$key] > 0) ? $cntArray[$key] : 1;
            }
            $befKey = $key;

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjz030eindex.php?cmd=edit"
                             . "&ENTEXAMYEAR=".$model->year."&APPLICANTDIV=".$model->applicantdiv."&DESIREDIV=".$model->desirediv."&TESTDIV1=".$model->testdiv1."&SUC_DESIREDIV=".$model->sucDesirediv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz030eForm1.html", $arg);
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
