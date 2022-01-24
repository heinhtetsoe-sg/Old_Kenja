<?php

require_once('for_php7.php');

class knjl770hForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("main", "POST", "knjl770hindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ--------------------------------------------

        //入試年度
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //学校種別コンボ
        $query = knjl770hQuery::getName($model->year, "L003");
        $extra = "onchange=\"return btn_submit('knjl770h')\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //入試区分コンボ
        $query = knjl770hQuery::getTestDiv($model->year, $model->applicantdiv);
        $extra = "onchange=\"return btn_submit('knjl770h')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");

        //性別
        $opt = array(1, 2, 3);
        $model->sexopt = ($model->sexopt == "") ? "1" : $model->sexopt;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SEX_OPT{$val}\" onchange=\"return btn_submit('knjl770h')\" ");
        }
        $radioArray = knjCreateRadio($objForm, "SEX_OPT", $model->sexopt, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }
        

        //データ2-------------------------------------------

        if ($model->cmd == "main" || isset($model->warning)) {
            $Row["BORDER_SCORE"] = $model->field["BORDER_SCORE"];
            $Row["GOUKAKU_CNT"]  = $model->field["GOUKAKU_CNT"];
            $Row["BORDER_SCORE_CANDI"] = $model->field["BORDER_SCORE_CANDI"];
            $Row["SUCCESS_CNT_CANDI"]  = $model->field["SUCCESS_CNT_CANDI"];
        } elseif ($model->cmd == "simShow") {
            //シミュレーション結果表示
            $query = knjl770hQuery::getCntPassingmark($model);
            $simRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["BORDER_SCORE"] = $model->field["BORDER_SCORE"];
            $Row["GOUKAKU_CNT"]  = $simRow["GOUKAKU_CNT"];
            $Row["BORDER_SCORE_CANDI"] = $model->field["BORDER_SCORE_CANDI"];
            $Row["SUCCESS_CNT_CANDI"]  = $simRow["SUCCESS_CNT_CANDI"];
        } else {
            //合格点マスタ
            $result = $db->query(knjl770hQuery::selectQueryPassingmark($model, $model->testdiv, $model->sex));
            while ($passingRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["BORDER_SCORE"] = $passingRow["BORDER_SCORE"];
                $Row["GOUKAKU_CNT"]  = $passingRow["GOUKAKU_CNT"];
                $Row["BORDER_SCORE_CANDI"] = $passingRow["BORDER_SCORE_CANDI"];
                $Row["SUCCESS_CNT_CANDI"]  = $passingRow["SUCCESS_CNT_CANDI"];
            }//while
            $result->free();
        }

        //初期値
        if (!strlen($Row["GOUKAKU_CNT"])) {
            $Row["GOUKAKU_CNT"] = 0;
        }
        if (!strlen($Row["SUCCESS_CNT_CANDI"])) {
            $Row["SUCCESS_CNT_CANDI"] = 0;
        }

        //合格点
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BORDER_SCORE"] = knjCreateTextBox($objForm, $Row["BORDER_SCORE"], "BORDER_SCORE", 5, 3, $extra);

        //合格数
        $arg["dataCnt"]["GOUKAKU_CNT"] = $Row["GOUKAKU_CNT"];
        knjCreateHidden($objForm, "GOUKAKU_CNT", $Row["GOUKAKU_CNT"]);

        //繰上合格点
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BORDER_SCORE_CANDI"] = knjCreateTextBox($objForm, $Row["BORDER_SCORE_CANDI"], "BORDER_SCORE_CANDI", 5, 3, $extra);

        //繰上合格数
        $arg["dataCnt"]["SUCCESS_CNT_CANDI"] = $Row["SUCCESS_CNT_CANDI"];
        knjCreateHidden($objForm, "SUCCESS_CNT_CANDI", $Row["SUCCESS_CNT_CANDI"]);

        //確定結果一覧--------------------------------------

        //ヘッダ

        //合格点マスタ
        $query = knjl770hQuery::selectQueryPassingmark($model);
        $result = $db->query($query);
        while ($passingRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //リンク
            $sexopt = ($passingRow["SEX"] == "9") ? "1" : (($passingRow["SEX"]  == "1") ? "2" : "3");
            $hash = array("cmd"           => "edit",
                          "APPLICANTDIV"  => $model->applicantdiv,
                          "TESTDIV"       => $passingRow["TESTDIV"],
                          "SEX_OPT"       => $sexopt
            );
            $passingRow["BORDER_SCORE"] = View::alink("knjl770hindex.php", $passingRow["BORDER_SCORE"], "", $hash);

            $passingRow["SEX_NAME"] = ($passingRow["SEX"] == "9") ? "共通" : (($passingRow["SEX"] == "1") ? "男子" : "女子");

            $arg["dataD"][] = $passingRow;

            $arg["dataSum"]["GOUKAKU_CNT"] += $passingRow["GOUKAKU_CNT"];
            $arg["dataSum"]["SUCCESS_CNT_CANDI"] += $passingRow["SUCCESS_CNT_CANDI"];
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

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl770hForm1.html", $arg);
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
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        if ($row["NAMESPARE2"] == '1' && $default_flg) {
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
