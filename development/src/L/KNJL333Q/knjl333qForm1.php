<?php

require_once('for_php7.php');

class knjl333qForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl333qForm1", "POST", "knjl333qindex.php", "", "knjl333qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl333q');\"";
        $query = knjl333qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl333q');\"";
        if (SCHOOLKIND == "J") {
            $query = knjl333qQuery::getNameMst($model->ObjYear, "L024");
        } else {
            $query = knjl333qQuery::getNameMstL004($model->ObjYear);
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //radio（1.作業用リスト、2.書類発送用）
        $opt = array(1, 2);
        $model->field["TAISYO"] = ($model->field["TAISYO"] == "" || $model->field["TESTDIV"] == "2") ? "1" : $model->field["TAISYO"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"TAISYO{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "TAISYO", $model->field["TAISYO"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //radio（1.中学校順、2.合否結果順）
        $opt = array(1, 2);
        $model->field["ORDER"] = ($model->field["ORDER"] == "") ? "1" : $model->field["ORDER"];
        $extra = array();
        $disabled1 = ($model->field["TAISYO"] == "2") ? "" : " disabled";
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"ORDER{$val}\"".$disabled1);
        }
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->field["ORDER"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        $disabled2 = ($model->field["TAISYO"] == "1") ? "" : " disabled";

        if ($model->field["TESTDIV"] == "2") {
            $arg["suisen"] = 1;
            //合格
            $extra = "id=\"GOKAKU\"".$disabled2;
            $arg["data"]["GOKAKU"] = knjCreateCheckBox($objForm, "GOKAKU", "1", $extra);
        } elseif ($model->field["TESTDIV"] == "3") {
            $arg["ippan"] = 1;
            //県内合格
            $extra = "id=\"NAIGOKAKU\"".$disabled2;
            $arg["data"]["NAIGOKAKU"] = knjCreateCheckBox($objForm, "NAIGOKAKU", "1", $extra);
            //県外合格
            $extra = "id=\"GAIGOKAKU\"".$disabled2;
            $arg["data"]["GAIGOKAKU"] = knjCreateCheckBox($objForm, "GAIGOKAKU", "1", $extra);
            //スカラー希望者
            $extra = "id=\"SKKIBO\"".$disabled2;
            $arg["data"]["SKKIBO"] = knjCreateCheckBox($objForm, "SKKIBO", "1", $extra);
        }
        //全員
        $extra = "id=\"ALL\"".$disabled2;
        $arg["data"]["ALL"] = knjCreateCheckBox($objForm, "ALL", "1", $extra);
        //不合格
        $extra = "id=\"FUGOKAKU\"".$disabled2;
        $arg["data"]["FUGOKAKU"] = knjCreateCheckBox($objForm, "FUGOKAKU", "1", $extra);
        //欠席
        $extra = "id=\"KESSEKI\"".$disabled2;
        $arg["data"]["KESSEKI"] = knjCreateCheckBox($objForm, "KESSEKI", "1", $extra);
        //スカラー採用
        $extra = "id=\"SKSAIYO\"".$disabled2;
        $arg["data"]["SKSAIYO"] = knjCreateCheckBox($objForm, "SKSAIYO", "1", $extra);
        //スカラー不採用
        $extra = "id=\"SKFUSAIYO\"".$disabled2;
        $arg["data"]["SKFUSAIYO"] = knjCreateCheckBox($objForm, "SKFUSAIYO", "1", $extra);
        //入寮希望者
        $extra = "id=\"NYURYO\"".$disabled2;
        $arg["data"]["NYURYO"] = knjCreateCheckBox($objForm, "NYURYO", "1", $extra);
        //中学校宛通知を送る中学校一覧
        $extra = "id=\"CHUGAKUATE\"".$disabled2;
        $arg["data"]["CHUGAKUATE"] = knjCreateCheckBox($objForm, "CHUGAKUATE", "1", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        if (SCHOOLKIND != "J") {
            //CSVボタン
            $extra = "onclick=\"return btn_submit('csv');\"";
            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL333Q");
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl333qForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
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

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
