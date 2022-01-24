<?php

require_once('for_php7.php');

class knjl570jForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjl570jindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ--------------------------------------------

        //入試年度
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //学校種別コンボ
        $query = knjl570jQuery::getName($model->year, "L003");
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //入試種別コンボ
        $query = knjl570jQuery::getTestDiv($model->year, $model->applicantdiv);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");

        //データ2-------------------------------------------

        if ($model->cmd == "main" || isset($model->warning)) {
            $Row["BORDER_SCORE"] = $model->field["BORDER_SCORE"];
            $Row["GOUKAKU_CNT"]  = $model->field["GOUKAKU_CNT"];
        } else if ($model->cmd == "simShow") {
            //シミュレーション結果表示
            $query = knjl570jQuery::getCntPassingmark($model);
            $simRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["BORDER_SCORE"] = $model->field["BORDER_SCORE"];
            $Row["GOUKAKU_CNT"]  = $simRow["GOUKAKU_CNT"];
        } else {
            //合格点マスタ
            $result = $db->query(knjl570jQuery::selectQueryPassingmark($model, $model->testdiv));
            while ($passingRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["BORDER_SCORE"] = $passingRow["BORDER_SCORE"];
                $Row["GOUKAKU_CNT"]  = $passingRow["GOUKAKU_CNT"];
            }//while
            $result->free();
        }

        //初期値
        if (!strlen($Row["GOUKAKU_CNT"])) $Row["GOUKAKU_CNT"] = 0;

        //合格点
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BORDER_SCORE"] = knjCreateTextBox($objForm, $Row["BORDER_SCORE"], "BORDER_SCORE", 5, 3, $extra);

        //合格数
        $arg["dataCnt"]["GOUKAKU_CNT"] = $Row["GOUKAKU_CNT"];
        knjCreateHidden($objForm, "GOUKAKU_CNT", $Row["GOUKAKU_CNT"]);

        //確定結果一覧--------------------------------------

        //ヘッダ

        //合格点マスタ
        $result = $db->query(knjl570jQuery::selectQueryPassingmark($model));
        while ($passingRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //リンク
            $passingRow["BORDER_SCORE"] = View::alink("knjl570jindex.php", $passingRow["BORDER_SCORE"], "",
                                        array("cmd"           => "edit",
                                              "APPLICANTDIV"  => $model->applicantdiv,
                                              "TESTDIV"       => $passingRow["TESTDIV"]
                                        ));

            $arg["dataD"][] = $passingRow;

            $arg["dataSum"]["CAPACITY"] += $passingRow["CAPACITY"];
            $arg["dataSum"]["GOUKAKU_CNT"] += $passingRow["GOUKAKU_CNT"];
            $arg["dataSum"]["ZOUGEN_CNT"] += $passingRow["ZOUGEN_CNT"];
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

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl570jForm1.html", $arg); 
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
