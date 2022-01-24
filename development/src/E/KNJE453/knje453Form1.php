<?php

require_once('for_php7.php');

class knje453form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knje453index.php", "", "main");

        //db接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //種別コンボ
        $opt = array();
        $opt[] = array("label" => "01：総合所見１ ", "value" =>"01");
        $opt[] = array("label" => "02：総合所見２ ", "value" =>"02");

        $model->data_div = $model->data_div ? $model->data_div : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('main');\"";
        $arg["DATA_DIV"] = knjCreateCombo($objForm, "DATA_DIV", $model->data_div, $opt, $extra, 1);

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knje453Query::getRemark($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row = $model->field;
        }

        //定型文テキスト
        $height = $model->remark_size["gyou"] * 13.5 + ($model->remark_size["gyou"] -1 ) * 3 + 5;
        $arg["data"]["REMARK"] = KnjCreateTextArea($objForm, "REMARK", $model->remark_size["gyou"], ($model->remark_size["moji"] * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["REMARK"]);
        if ($model->remark_size["gyou"] == 1) {
            $arg["data"]["REMARK_COMMENT"] = "(全角{$model->remark_size["moji"]}文字まで)";
        } else {
            $arg["data"]["REMARK_COMMENT"] = "(全角{$model->remark_size["moji"]}文字X{$model->remark_size["gyou"]}行まで)";
        }
        $arg["data"]["REMARK_HEIGHT"] = $height + 30;

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //前年度データ件数
        $pre_year = CTRL_YEAR - 1;
        $preYear_cnt = $db->getOne(knje453Query::getCopyData($pre_year, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preYear_cnt);
        //今年度データ件数
        $this_year = CTRL_YEAR;
        $thisYear_cnt = $db->getOne(knje453Query::getCopyData($this_year, "cnt"));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisYear_cnt);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje453Form1.html", $arg);
    }
}
?>
