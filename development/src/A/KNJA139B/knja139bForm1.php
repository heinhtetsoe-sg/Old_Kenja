<?php

require_once('for_php7.php');

class knja139bForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knja139bindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度コピーボタン
        $extra = (AUTHORITY != DEF_UPDATABLE) ? "disabled" : "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //学年コンボ
        $query = knja139bQuery::getGrade($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //種別コンボ
        $query = knja139bQuery::getA040("cnt");
        $a040cnt = $db->getOne($query);
        if ($a040cnt == 0) {
            $opt = array();
            $opt[] = array('label' => '総合的な学習の時間：活動', 'value' => '01');
            $opt[] = array('label' => '総合的な学習の時間：評価', 'value' => '02');

            $model->field["DATA_DIV"] = ($model->field["DATA_DIV"]) ? $model->field["DATA_DIV"] : $opt[0]["value"];
            $extra = "onChange=\"return btn_submit('main')\"";
            $arg["DATA_DIV"] = knjCreateCombo($objForm, "DATA_DIV", $model->field["DATA_DIV"], $opt, $extra, 1);
        } else {
            $query = knja139bQuery::getA040();
            $extra = "onChange=\"return btn_submit('main')\"";
            makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->field["DATA_DIV"], $extra, 1);
        }

        //データ数
        if ($model->cmd != "readCnt") {
            $query = knja139bQuery::getDataCnt($model);
            $model->dataCnt = $db->getOne($query);
            $model->field["DATA_CNT"] = $model->dataCnt;
        }
        $extra = "style=\"text-align:right\" onchange=\"dataCntCheck(".$model->field["DATA_CNT"].");\"";
        $arg["DATA_CNT"] = knjCreateTextBox($objForm, $model->field["DATA_CNT"], "DATA_CNT", 2, 2, $extra);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('readCnt');\"";
        $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

        //定型文一覧表示
        for ($i=0; $i < $model->field["DATA_CNT"]; $i++) {
            //データ存在チェック
            $query = knja139bQuery::getHtrainremarkTempDat($model, $model->pattern_cd[$i], "cnt");
            $cnt = $db->getOne($query);

            if ($cnt > 0) {
                $query = knja139bQuery::getHtrainremarkTempDat($model, $model->pattern_cd[$i], "list");
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //定型文テキストエリア
                    $value = (!isset($model->warning)) ? $row["REMARK"] : $model->fields[$i]["REMARK"];
                    $extra = "";
                    $row["REMARK"] = KnjCreateTextArea($objForm, "REMARK_".$i, "3", "111", "soft", $extra, $value);
                    //パターンコード（全角で表示）
                    $row["PATTERN_CD_SHOW"] = mb_convert_kana($model->pattern_cd[$i], "R");

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
        View::toHTML($model, "knja139bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
