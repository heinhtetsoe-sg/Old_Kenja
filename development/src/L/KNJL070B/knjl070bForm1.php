<?php

require_once('for_php7.php');

class knjl070bForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjl070bindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ--------------------------------------------

        //入試年度
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //入試制度コンボ
        $query = knjl070bQuery::getName($model->year, "L003");
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //入試区分コンボ
        $query = knjl070bQuery::getName($model->year, "L004");
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");

        //区分(1:正規合格 2:単切合格)コンボ
        $optShdiv = array();
        $query = knjl070bQuery::getShdiv($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        $optShdiv = makeCmb($objForm, $arg, $db, $query, $model->shdiv, "SHDIV", $extra, 1, "");

        //判定区分コンボ
        $query = knjl070bQuery::getJudgmentDiv($model, $model->shdiv);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->judgment_div, "JUDGMENT_DIV", $extra, 1, "");

        //データ1-------------------------------------------

        //指定された入試区分の合否詳細区分マスタ情報を配列にセット
        $model->judgmentDivArray = array();
        foreach ($optShdiv as $key => $array) {
            $shdiv = $array["value"];
            $result = $db->query(knjl070bQuery::getJudgmentDiv($model, $shdiv));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $judgmentDiv = $row["VALUE"];
                $model->judgmentDivArray[$shdiv][$judgmentDiv] = $row;
            }
        }

        //データ2-------------------------------------------

        if ($model->cmd == "main" || isset($model->warning)) {
            //post
            $Row["BORDER_DEVIATION"] = $model->field["BORDER_DEVIATION"];
            $Row["SUCCESS_CNT"] = $model->field["SUCCESS_CNT"];
            $Row["SUCCESS_CNT_SPECIAL"] = $model->field["SUCCESS_CNT_SPECIAL"];
            $Row["SUCCESS_CNT_SPECIAL2"] = $model->field["SUCCESS_CNT_SPECIAL2"];
        } else if ($model->cmd == "simShow") {
            //合格者取得(シミュレーション結果表示)
            $query = knjl070bQuery::selectQuerySuccess_cnt($model);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $Row["BORDER_DEVIATION"] = $model->field["BORDER_DEVIATION"];
            $Row["SUCCESS_CNT"] = $passingRow["SUCCESS_CNT"];
            $Row["SUCCESS_CNT_SPECIAL"] = $passingRow["SUCCESS_CNT_SPECIAL"];
            $Row["SUCCESS_CNT_SPECIAL2"] = $passingRow["SUCCESS_CNT_SPECIAL2"];
        } else {
            //合格点マスタ
            $course = $model->judgmentDivArray[$model->shdiv][$model->judgment_div]["PASS_COURSE"];
            $query = knjl070bQuery::selectQueryPassingmark($model, $course, $model->shdiv);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $Row["BORDER_DEVIATION"] = $passingRow["BORDER_DEVIATION"];
            $Row["SUCCESS_CNT"] = $passingRow["SUCCESS_CNT"];
            $Row["SUCCESS_CNT_SPECIAL"] = $passingRow["SUCCESS_CNT_SPECIAL"];
            $Row["SUCCESS_CNT_SPECIAL2"] = $passingRow["SUCCESS_CNT_SPECIAL2"];
        }

        //合格平均点
        $extra = "style=\"text-align:right\" onblur=\"this.value=toFloat(this.value);\"";
        $arg["BORDER_DEVIATION"] = knjCreateTextBox($objForm, $Row["BORDER_DEVIATION"], "BORDER_DEVIATION", 5, 5, $extra);

        //合格数
        //XXX名 内 確約（合格点以上：XXX名、合格点未満：XXX名）
        if (!strlen($Row["SUCCESS_CNT"])) $Row["SUCCESS_CNT"] = 0;
        if (!strlen($Row["SUCCESS_CNT_SPECIAL"])) $Row["SUCCESS_CNT_SPECIAL"] = 0;
        if (!strlen($Row["SUCCESS_CNT_SPECIAL2"])) $Row["SUCCESS_CNT_SPECIAL2"] = 0;
        $arg["SUCCESS_CNT"] = $Row["SUCCESS_CNT"];
        $arg["SUCCESS_CNT_SPECIAL"] = $Row["SUCCESS_CNT_SPECIAL"];
        $arg["SUCCESS_CNT_SPECIAL2"] = $Row["SUCCESS_CNT_SPECIAL2"];
        $arg["SUCCESS_CNT_CANDI"] = $Row["SUCCESS_CNT"] - $Row["SUCCESS_CNT_SPECIAL"] - $Row["SUCCESS_CNT_SPECIAL2"];
        knjCreateHidden($objForm, "SUCCESS_CNT", $Row["SUCCESS_CNT"]);
        knjCreateHidden($objForm, "SUCCESS_CNT_SPECIAL", $Row["SUCCESS_CNT_SPECIAL"]);
        knjCreateHidden($objForm, "SUCCESS_CNT_SPECIAL2", $Row["SUCCESS_CNT_SPECIAL2"]);
        knjCreateHidden($objForm, "SUCCESS_CNT_CANDI", $Row["SUCCESS_CNT"] - $Row["SUCCESS_CNT_SPECIAL"] - $Row["SUCCESS_CNT_SPECIAL2"]);

        //確定結果一覧--------------------------------------

        //フッタの合計
        $sum["SUCCESS_CNT"] = 0;
        $sum["SUCCESS_CNT_SPECIAL"] = 0;
        $sum["SUCCESS_CNT_SPECIAL2"] = 0;
        //合否詳細区分マスタの情報を配列にセット
        foreach ($model->judgmentDivArray as $shdiv => $array) {
            foreach ($array as $judgmentDiv => $judgmentDivArray) {
                $course = $judgmentDivArray["PASS_COURSE"];
                //合格点マスタ
                $query = knjl070bQuery::selectQueryPassingmark($model, $course, $shdiv);
                $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (isset($passingRow)) {
                    //フッタの合計
                    $sum["SUCCESS_CNT"] += $passingRow["SUCCESS_CNT"];
                    $sum["SUCCESS_CNT_SPECIAL"] += $passingRow["SUCCESS_CNT_SPECIAL"];
                    $sum["SUCCESS_CNT_SPECIAL2"] += $passingRow["SUCCESS_CNT_SPECIAL2"];
                    //1行表示
                    $passingRow["SUCCESS_CNT_CANDI"] = $passingRow["SUCCESS_CNT"] - $passingRow["SUCCESS_CNT_SPECIAL"] - $passingRow["SUCCESS_CNT_SPECIAL2"];
                    //判定区分の名称
                    $passingRow["JUDGMENT_DIV"] = $judgmentDivArray["LABEL"];
                    //リンク
                    $passingRow["BORDER_DEVIATION"] = View::alink("knjl070bindex.php",
                                                                  $passingRow["BORDER_DEVIATION"],
                                                                  "",
                                                                  array("cmd"           => "edit",
                                                                        "APPLICANTDIV"  => $model->applicantdiv,
                                                                        "TESTDIV"       => $model->testdiv,
                                                                        "SHDIV"         => $shdiv,
                                                                        "JUDGMENT_DIV"  => $judgmentDiv
                                                    ));
                    //ピンク表示　確定ボタンを押すとそれより以前の判定結果をピンク表示する
                    $passingRow["BGCOLOR_PINK"] = $passingRow["CAPA_CNT"] == 1 ? "white" : "pink";
                    //1行データセット
                    $arg["dataJudgmentDiv"][] = $passingRow;
                }
            }
        }
        //フッタ
        $arg["sum"]["SUCCESS_CNT"] = $sum["SUCCESS_CNT"];
        $arg["sum"]["SUCCESS_CNT_SPECIAL"] = $sum["SUCCESS_CNT_SPECIAL"];
        $arg["sum"]["SUCCESS_CNT_SPECIAL2"] = $sum["SUCCESS_CNT_SPECIAL2"];
        $arg["sum"]["SUCCESS_CNT_CANDI"] = $sum["SUCCESS_CNT"] - $sum["SUCCESS_CNT_SPECIAL"] - $sum["SUCCESS_CNT_SPECIAL2"];

        //欠席者数など
        $query = knjl070bQuery::getKessekiCnt($model);
        $kRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (isset($kRow)) {
            $arg["KESSEKI_CNT"]         = $kRow["KESSEKI_CNT"];
            $arg["ALL_FUGOU_CNT"]       = (0 < $sum["SUCCESS_CNT"]) ? $kRow["ALL_CNT"] - $kRow["KESSEKI_CNT"] - $sum["SUCCESS_CNT"] : 0;
            $arg["FUGOU_CNT"]           = (0 < $sum["SUCCESS_CNT"]) ? $arg["ALL_FUGOU_CNT"] - $kRow["KAKUYAKU_FUGOU_CNT"] : 0;
            $arg["KAKUYAKU_FUGOU_CNT"]  = (0 < $sum["SUCCESS_CNT"]) ? $kRow["KAKUYAKU_FUGOU_CNT"] : 0;
        } else {
            $arg["KESSEKI_CNT"]         = 0;
            $arg["ALL_FUGOU_CNT"]       = 0;
            $arg["FUGOU_CNT"]           = 0;
            $arg["KAKUYAKU_FUGOU_CNT"]  = 0;
        }
        //入試区分
        $query = knjl070bQuery::getName($model->year, "L004", $model->testdiv);
        $testRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (isset($testRow)) {
            $arg["TESTDIV_NAME"] = $testRow["NAME1"];
        } else {
            $arg["TESTDIV_NAME"] = "";
        }

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
        View::toHTML($model, "knjl070bForm1.html", $arg); 
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

    return $opt;
}
?>
