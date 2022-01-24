<?php

require_once('for_php7.php');

class knjh562aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //試験年度
        $arg["YEAR"] = $model->year;

        //データ種別コンボ作成
        $query = knjh562aQuery::getDataDiv($model);
        $this->makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->proficiencyDiv, "onchange=\"return btn_submit('change_prfcencydiv');\"", 1);

        //テスト名称コンボ作成
        $query = knjh562aQuery::getProName($model);
        $this->makeCmb($objForm, $arg, $db, $query, "PROFICIENCYCD", $model->proficiencyCd, "onchange=\"return btn_submit('change_prfcencycd');\"", 1);

        //登録済み点数リストを降順で取得
        $lowerList = array();
        $extra = " max='{$model->max_score}' min='{$model->min_score}' style='text-align: right;'";
        if ($model->cmd == "change_row") {
            foreach ($model->lowers as $lower) {
                $lowerList[] = $lower;
            }
        } else {
            $result = $db->query(knjh562aQuery::getScoreList($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $lowerList[] = $row["TICK_LOW"];
            }
            $result->free();

            //入力行数を登録済み件数に合わせる
            if (0 < get_count($lowerList)) {
                $model->rownum = get_count($lowerList);
            }
        }
        $this->makeScoreTextboxes($objForm, $arg, $model->max_score, $model->min_score, $lowerList, $model->rownum, $extra);

        //行数コンボボックス
        $list = array();
        for ($rwno = $model->rownum_min; $rwno <= $model->rownum_max; $rwno++) {
            $list[] = array("label" => $rwno, "value" => $rwno);
        }
        $arg["data"]["ROWNUM"] = knjCreateCombo($objForm, "ROWNUM", $model->rownum, $list, "", 1);

        //ボタン作成
        $btnStyle = "style=\"width:60px\"";
        $arg["button"]["btn_changeRow"] = knjCreateBtn($objForm, "btn_changeRow", "行数確定", " onclick=\"btn_submit('change_row');\" ");
        $arg["button"]["btn_update"]    = knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"btn_submit('update');\" ".$btnStyle);
        $arg["button"]["btn_cancel"]    = knjCreateBtn($objForm, "btn_cancel", "取 消", " onclick=\"btn_submit('cancel');\" ".$btnStyle);
        $arg["button"]["btn_end"]       = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"".$btnStyle);

        //非表示項目
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", $model->programID);
        knjCreateHidden($objForm, "SCHOOLCD", $model->urlSchoolCd);
        knjCreateHidden($objForm, "SCHOOLKIND", $model->selectSchoolKind);

        //DB切断
        Query::dbCheckIn($db);

        //HTML出力終了
        $arg["start"]  = $objForm->get_start("main", "POST", "knjh562aindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjh562aForm1.html", $arg);
    }

    //コンボ作成
    private function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
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
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }

    //点数テキストボックスを作成
    private function makeScoreTextboxes(&$objForm, &$arg, $maxScore, $minScore, $lowerList, $rowNum, $extra = "")
    {
        $upperCore = $maxScore; //対象行の最大点数
        for ($stageNo =1; is_array($lowerList) && $stageNo <= $rowNum; $stageNo++) {
            $lowerScore = -1;
            if ($stageNo <= get_count($lowerList)) {
                $lowerScore = (int)$lowerList[$stageNo -1];
                // 前行の最小点数を基に本行の最大点数を算定
                if (1 < $stageNo) {
                    $upperCore = (int)$lowerList[$stageNo -2] -1;
                }
            }

            //指定された範囲を超える場合は無効化
            if ($lowerScore < $minScore || $maxScore < $lowerScore) {
                $lowerScore = -1;
            }
            if ($upperCore < $minScore || $maxScore < $upperCore) {
                $upperCore = -1;
            }

            $scorerow = [];
            $scorerow["LOWER_LIMIT"] = knjCreateTextBox($objForm, $lowerScore < 0 ? "" : $lowerScore, "LOWER_LIMIT[]", 3, 3, $extra." id='TICK_STAGE_{$stageNo}'");
            $scorerow["UPPER_LIMIT"] = 0 < $upperCore ? $upperCore : "";
            $arg["scorelist"][] = $scorerow;

            $upperCore = -1; // 最大点数を初期化（次行以降の最大点数はその直前の最小点数より算定）
        }
    }
}
