<?php

require_once('for_php7.php');

class knjz220uform1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz220uindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //段階数取得
        $query = knjz220uQuery::getAssessLevelCnt($model);
        $assesslevelcnt = $cntFlg = $db->getOne($query);

        if ($model->cmd == "level" || ($model->cmd == "main" && $model->level != "")) {
            $assesslevelcnt = $model->level;
        }

        //段階数
        $assesslevelcnt = ($assesslevelcnt != "" && $assesslevelcnt != "0") ? $assesslevelcnt : "5";
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["sepa"]["ASSESSLEVELCNT"] = knjCreateTextBox($objForm, $assesslevelcnt, "ASSESSLEVELCNT", 2, 1, $extra);

        //確定ボタン
        $extra = "onclick=\"return level(".$assesslevelcnt.");\"";
        $arg["button"]["btn_level"] = knjCreateBtn($objForm, "btn_level", "確 定", $extra);

        $rateData = array();
        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knjz220uQuery::selectQuery($model);
            $result = $db->query($query);
        } else {
            if ($model->level == "") {
                $query = knjz220uQuery::selectQuery($model);
                $result = $db->query($query);
            } else {
                $rateData =& $model->field;
                $result = "";
            }
        }

        if($result != "" && ($cntFlg == $assesslevelcnt)){
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rateData["{$row["ASSESSLEVEL"]}"] = $row["ASSESSRATE"];
            }
        }

        for ($i = 0; $i < $assesslevelcnt; $i++) {
            $row["ASSESSLEVEL"] = $assesslevel = $assesslevelcnt - $i;

            //基準値作成
            if ($assesslevel == "1") {
                $row["ASSESSRATE"] = "1";
            } else {
                $rateVal = "";
                if ($result != "" && ($cntFlg == $assesslevelcnt)) {
                    $rateVal = $rateData[$assesslevel];
                } else if ($result == "" && isset($model->warning)) {
                    $rateVal = $rateData["ASSESSRATE".$assesslevel];
                }
                $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
                $row["ASSESSRATE"] = knjCreateTextBox($objForm, $rateVal, "ASSESSRATE".$assesslevel, 2, 2, $extra);
            }
            $arg["data"][] = $row;
        }

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

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz220uForm1.html", $arg);
    }
}
?>
