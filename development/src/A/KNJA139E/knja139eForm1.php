<?php

require_once('for_php7.php');

class knja139eForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knja139eindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度コピーボタン
        $extra = (AUTHORITY != DEF_UPDATABLE) ? "disabled" : "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //学年コンボ
        $query = knja139eQuery::getGrade($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //種別コンボ
        $opt = array();
        $opt[] = array('label' => '10:道徳',                       'value' => '10');
        $opt[] = array('label' => '21:総合的な学習の時間：学習活動', 'value' => '21');
        $opt[] = array('label' => '22:総合的な学習の時間：観点',     'value' => '22');
        $opt[] = array('label' => '23:総合的な学習の時間：評価',     'value' => '23');

        $model->field["DATA_DIV"] = ($model->field["DATA_DIV"]) ? $model->field["DATA_DIV"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('main')\"";
        $arg["DATA_DIV"] = knjCreateCombo($objForm, "DATA_DIV", $model->field["DATA_DIV"], $opt, $extra, 1);

        //データ数
        if ($model->cmd != "readCnt" && !isset($model->warning)) {
            $query = knja139eQuery::getDataCnt($model);
            $model->field["DATA_CNT"] = $db->getOne($query);
        }
        $extra = "style=\"text-align:right\" onchange=\"dataCntCheck(".$model->field["DATA_CNT"].");\"";
        $arg["DATA_CNT"] = knjCreateTextBox($objForm, $model->field["DATA_CNT"], "DATA_CNT", 2, 2, $extra);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('readCnt');\"";
        $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

        //パターン数
        $model->pattern_cd = array();
        for ($i = 1; $i <= $model->field["DATA_CNT"]; $i++) {
            $model->pattern_cd[] = $i;
        }

        //定型文一覧表示
        for ($i = 0; $i < get_count($model->pattern_cd); $i++) {
            //データ存在チェック
            $query = knja139eQuery::getHtrainremarkTempScoreMst($model, $model->pattern_cd[$i], "cnt");
            $cnt = $db->getOne($query);

            if ($cnt > 0) {
                $query = knja139eQuery::getHtrainremarkTempScoreMst($model, $model->pattern_cd[$i], "list");
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //定型文テキストエリア
                    $value = (!isset($model->warning)) ? $row["REMARK"] : $model->fields[$i]["REMARK"];
                    $extra = "";
                    $row["REMARK"] = KnjCreateTextArea($objForm, "REMARK_".$i, "3", "111", "soft", $extra, $value);
                    //パターンコード（全角で表示）
                    $row["PATTERN_CD_SHOW"] = mb_convert_kana($model->pattern_cd[$i], "R");

                    //下限
                    $valueFromScore = (!isset($model->warning)) ? $row["FROM_SCORE"] : $model->fields[$i]["FROM_SCORE"];
                    $extra = " STYLE=\"text-align:right;\" onblur=\"isNumb(this, ".($model->pattern_cd[$i]).");\"";
                    $row["FROM_SCORE"] = knjCreateTextBox($objForm, $valueFromScore, "FROM_SCORE_".$i, 3, 3, $extra);

                    //上限
                    $valueToScore = (!isset($model->warning)) ? $row["TO_SCORE"] : $model->fields[$i]["TO_SCORE"];
                    if ($model->pattern_cd[$i] == 1) {
                        $extra = " onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
                        $row["TO_SCORE"] = knjCreateTextBox($objForm, $valueToScore, "TO_SCORE_".$i, 3, 3, $extra);
                    } else {
                        $row["TO_SCORE"]  = "<span id=\"TO_SCORE_".$i."\">";
                        $row["TO_SCORE"] .= $valueToScore;
                        $row["TO_SCORE"] .= "</span>";
                        knjCreateHidden($objForm, "TO_SCORE_".$i, $valueToScore);
                    }

                    $arg["data"][] = $row;
                }
            } else {
                $tmp = array();
                //定型文テキストエリア
                $value = (!isset($model->warning)) ? $tmp["REMARK"] : $model->fields[$i]["REMARK"];
                $extra = "";
                $tmp["REMARK"] = KnjCreateTextArea($objForm, "REMARK_".$i, 3, 111, "soft", $extra, $value);
                //パターンコード（全角で表示）
                $tmp["PATTERN_CD_SHOW"] = mb_convert_kana($model->pattern_cd[$i], "R");

                //下限
                $valueFromScore = (!isset($model->warning)) ? $tmp["FROM_SCORE"] : $model->fields[$i]["FROM_SCORE"];
                $extra = " STYLE=\"text-align:right;\" onblur=\"isNumb(this, ".($model->pattern_cd[$i]).");\"";
                $tmp["FROM_SCORE"] = knjCreateTextBox($objForm, $valueFromScore, "FROM_SCORE_".$i, 3, 3, $extra);

                //上限
                $valueToScore = (!isset($model->warning)) ? $tmp["TO_SCORE"] : $model->fields[$i]["TO_SCORE"];
                if ($model->pattern_cd[$i] == 1) {
                    $extra = " onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
                    $tmp["TO_SCORE"] = knjCreateTextBox($objForm, $valueToScore, "TO_SCORE_".$i, 3, 3, $extra);
                } else {
                    $tmp["TO_SCORE"]  = "<span id=\"TO_SCORE_".$i."\">";
                    $tmp["TO_SCORE"] .= $valueToScore;
                    $tmp["TO_SCORE"] .= "</span>";
                    knjCreateHidden($objForm, "TO_SCORE_".$i, $valueToScore);
                }

                $arg["data"][] = $tmp;
            }
        }

        //ボタン作成
        //更新ボタン
        $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja139eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
