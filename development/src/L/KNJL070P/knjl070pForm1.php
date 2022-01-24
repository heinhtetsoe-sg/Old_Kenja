<?php

require_once('for_php7.php');

class knjl070pForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjl070pindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ--------------------------------------------

        //入試年度
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //入試制度コンボ
        $query = knjl070pQuery::getName($model->year, "L003");
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //入試区分コンボ
        $query = knjl070pQuery::getName($model->year, ($model->applicantdiv == "2") ? "L004" : "L024");
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");

        //面接点コンボ
        $query = knjl070pQuery::getMensetsudiv();
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->mensetsudiv, "MENSETSUDIV", $extra, 1, "");

        //データ2-------------------------------------------

        if ($model->cmd == "main" || isset($model->warning)) {
            $Row["BORDER_SCORE"]    = $model->field["BORDER_SCORE"];
            $Row["BORDER_DEVIATION"]= $model->field["BORDER_DEVIATION"];
            for ($s = 0; $s <= 2; $s++) {
                $sex = ($s == 0) ? 9 : $s;
                $Row["SHUTUGAN_CNT" .$sex] = $model->field["SHUTUGAN_CNT".$sex];
                $Row["KESSEKI_CNT"  .$sex] = $model->field["KESSEKI_CNT".$sex];
                $Row["GOUKAKU_CNT"  .$sex] = $model->field["GOUKAKU_CNT".$sex];
                $Row["FUGOUKAKU_CNT".$sex] = $model->field["FUGOUKAKU_CNT".$sex];
                $Row["JITUGOUKAKU_RITU".$sex] = $model->field["JITUGOUKAKU_RITU".$sex];
            }
        } else if ($model->cmd == "simShow") {
            //シミュレーション結果表示
            //$query = knjl070pQuery::selectQuerySuccess_cnt($model);
            $query = knjl070pQuery::getCntPassingmark($model);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["BORDER_SCORE"]    = $model->field["BORDER_SCORE"];
            $Row["BORDER_DEVIATION"]= $model->field["BORDER_DEVIATION"];
            for ($s = 0; $s <= 2; $s++) {
                $sex = ($s == 0) ? 9 : $s;
                $Row["SHUTUGAN_CNT" .$sex] = $passingRow["SHUTUGAN_CNT".$sex];
                $Row["KESSEKI_CNT"  .$sex] = $passingRow["KESSEKI_CNT".$sex];
                $Row["GOUKAKU_CNT"  .$sex] = $passingRow["GOUKAKU_CNT".$sex];
                $Row["FUGOUKAKU_CNT".$sex] = $passingRow["FUGOUKAKU_CNT".$sex];
                $Row["JITUGOUKAKU_RITU".$sex] = $passingRow["JITUGOUKAKU_RITU".$sex];
            }
        } else {
            //合格点マスタ
            $result = $db->query(knjl070pQuery::selectQueryPassingmark($model));
            while ($passingRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sex = $passingRow["SEX"];
                if ($sex == "9") {
                    $Row["BORDER_SCORE"]    = $passingRow["BORDER_SCORE"];
                    $Row["BORDER_DEVIATION"]= $passingRow["BORDER_DEVIATION"];
                }
                $Row["SHUTUGAN_CNT" .$sex] = $passingRow["SHUTUGAN_CNT"];
                $Row["KESSEKI_CNT"  .$sex] = $passingRow["KESSEKI_CNT"];
                $Row["GOUKAKU_CNT"  .$sex] = $passingRow["GOUKAKU_CNT"];
                $Row["FUGOUKAKU_CNT".$sex] = $passingRow["FUGOUKAKU_CNT"];
                $Row["JITUGOUKAKU_RITU".$sex] = $passingRow["JITUGOUKAKU_RITU"];
            }//while
            $result->free();
        }

        for ($s = 0; $s <= 2; $s++) {
            $sex = ($s == 0) ? 9 : $s;
            if (!strlen($Row["SHUTUGAN_CNT" .$sex])) $Row["SHUTUGAN_CNT".$sex] = 0;
            if (!strlen($Row["KESSEKI_CNT"  .$sex])) $Row["KESSEKI_CNT".$sex] = 0;
            if (!strlen($Row["GOUKAKU_CNT"  .$sex])) $Row["GOUKAKU_CNT".$sex] = 0;
            if (!strlen($Row["FUGOUKAKU_CNT".$sex])) $Row["FUGOUKAKU_CNT".$sex] = 0;
            if (!strlen($Row["JITUGOUKAKU_RITU".$sex])) $Row["JITUGOUKAKU_RITU".$sex] = 0.0;
        }

        //合格ライン
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BORDER_SCORE"] = knjCreateTextBox($objForm, $Row["BORDER_SCORE"], "BORDER_SCORE", 5, 3, $extra);

        //合計偏差値ライン
        $extra = "style=\"text-align:right\" onblur=\"this.value=toFloat(this.value);\"";
        $arg["data"]["BORDER_DEVIATION"] = knjCreateTextBox($objForm, $Row["BORDER_DEVIATION"], "BORDER_DEVIATION", 5, 4, $extra);

        //出願者数
        //欠席者数
        //合格数
        //不合格数
        //実合格率
        for ($s = 0; $s <= 2; $s++) {
            $sex = ($s == 0) ? 9 : $s;
            $arg["dataCnt"]["SHUTUGAN_CNT".$sex] = $Row["SHUTUGAN_CNT".$sex];
            $arg["dataCnt"]["KESSEKI_CNT".$sex] = $Row["KESSEKI_CNT".$sex];
            $arg["dataCnt"]["GOUKAKU_CNT".$sex] = $Row["GOUKAKU_CNT".$sex];
            $arg["dataCnt"]["FUGOUKAKU_CNT".$sex] = $Row["FUGOUKAKU_CNT".$sex];
            $arg["dataCnt"]["JITUGOUKAKU_RITU".$sex] = $Row["JITUGOUKAKU_RITU".$sex];
            knjCreateHidden($objForm, "SHUTUGAN_CNT".$sex, $Row["SHUTUGAN_CNT".$sex]);
            knjCreateHidden($objForm, "KESSEKI_CNT".$sex, $Row["KESSEKI_CNT".$sex]);
            knjCreateHidden($objForm, "GOUKAKU_CNT".$sex, $Row["GOUKAKU_CNT".$sex]);
            knjCreateHidden($objForm, "FUGOUKAKU_CNT".$sex, $Row["FUGOUKAKU_CNT".$sex]);
            knjCreateHidden($objForm, "JITUGOUKAKU_RITU".$sex, $Row["JITUGOUKAKU_RITU".$sex]);
        }

        //確定結果一覧--------------------------------------

        //ヘッダ

        //合格点マスタ
        $result = $db->query(knjl070pQuery::selectQueryPassingmark($model));
        while ($passingRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //リンク
            $passingRow["MENSETSUDIV_NAME"] = View::alink("knjl070pindex.php", $passingRow["MENSETSUDIV_NAME"], "",
                                        array("cmd"           => "edit",
                                              "APPLICANTDIV"  => $model->applicantdiv,
                                              "TESTDIV"       => $model->testdiv,
                                              "MENSETSUDIV"   => $model->mensetsudiv
                                        ));

            if ($passingRow["SEX"] == "9") $passingRow["SEX_NAME"] = "全受験者";
            if ($passingRow["SEX"] == "1") $passingRow["SEX_NAME"] = "男子";
            if ($passingRow["SEX"] == "2") $passingRow["SEX_NAME"] = "女子";

            if ($passingRow["SEX"] == "9") $passingRow["rowspan"] = "3";

            $arg["dataD"][] = $passingRow;
        }//while
        $result->free();

        //フッタ

        //DB切断
        Query::dbCheckIn($db);

        //シミュレーションボタン
        $extra = "onclick=\"return btn_submit('sim');\"";
        $arg["btn_sim"] = knjCreateBtn($objForm, "btn_sim", "シミュレーション", $extra);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('decision');\"";
        $arg["btn_decision"] = knjCreateBtn($objForm, "btn_decision", "確 定", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "判定資料", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //帳票パラメータ
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->year);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL321P");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl070pForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["NAMESPARE2"] == '1' && $default_flg){
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
?>
