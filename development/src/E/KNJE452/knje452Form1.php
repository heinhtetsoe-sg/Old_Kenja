<?php

require_once('for_php7.php');

class knje452form1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje452index.php", "", "main");

        //db接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //設問区分
        $opt = array();
        $opt[] = array("label" => "02：学習面 ", "value" =>"02");
        $opt[] = array("label" => "03：生活・行動面 ", "value" =>"03");
        $opt[] = array("label" => "04：社会性・対人関係 ", "value" =>"04");

        $model->assessDiv = $model->assessDiv ? $model->assessDiv : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('change');\"";
        $arg["sepa"]["ASSESS_DIV"] = knjCreateCombo($objForm, "ASSESS_DIV", $model->assessDiv, $opt, $extra, 1);

        $setQ = array();
        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $query = knje452Query::getQuestion($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setQ[$row["ASSESS_CD"]] = $row["QUESTION"]; 
            }
        } else {
            for ($i = 1; $i <= $model->assessCnt[$model->assessDiv]; $i++) {
                $setQ[$i] = $model->field["QUESTION".$i];
            }
        }

        for ($i = 1; $i <= $model->assessCnt[$model->assessDiv]; $i++) {
            $setRow["ASSESS_CD"] = $i;
            //textbox
            $extra = "";
            $setRow["QUESTION"] = knjCreateTextBox($objForm, $setQ[$i], "QUESTION".$i, 60, 60, $extra);

            $arg["data"][] = $setRow;
        }

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
        $preYear_cnt = $db->getOne(knje452Query::getCopyData($pre_year, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preYear_cnt);
        //今年度データ件数
        $this_year = CTRL_YEAR;
        $thisYear_cnt = $db->getOne(knje452Query::getCopyData($this_year, "cnt"));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisYear_cnt);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje452Form1.html", $arg);
    }
}
?>
