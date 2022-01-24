<?php

require_once('for_php7.php');

class knjz204Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz204index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->patternCd) && !isset($model->warning) && $model->cmd != "kakutei") || $model->cmd == "reset") {
            $Row = $db->getRow(knjz204Query::getRow1($model->patternCd),DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //パターンコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PATTERNCD"] = knjCreateTextBox($objForm, $Row["PATTERNCD"], "PATTERNCD", 3, 2, $extra);

        //パターン名称
        $arg["data"]["PATTERNCDNAME"] = knjCreateTextBox($objForm, $Row["PATTERNCDNAME"], "PATTERNCDNAME", 20, 30, "");

        //基準日
        $Row["BASEDATE"] = str_replace("-","/",$Row["BASEDATE"]);
        $arg["data"]["BASEDATE"] = View::popUpCalendar($objForm, "BASEDATE", $Row["BASEDATE"]);

        //段階値
        $query = knjz204Query::getRowCount($model->patternCd);
        $model->assesslevelCnt = $db->getOne($query);
        $model->field["ASSESSLEVELCNT"] = ($model->field["ASSESSLEVELCNT"]) ? $model->field["ASSESSLEVELCNT"] : $model->assesslevelCnt;
        if ($model->cmd == "reset") {
            $model->field["ASSESSLEVELCNT"] = $model->assesslevelCnt;
        }
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ASSESSLEVELCNT"] = knjCreateTextBox($objForm, $model->field["ASSESSLEVELCNT"], "ASSESSLEVELCNT", 3, 2, $extra);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('kakutei');\"";
        $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

        $model->data = array();
        if ($model->cmd == "kakutei" || isset($model->warning)) {
            $counter = 0;
            for ($dataCnt = 1; $dataCnt <= $model->field["ASSESSLEVELCNT"]; $dataCnt++) {
                //段階値表示
                $row["ASSESSLEVEL"] = $dataCnt;

                //記号テキストボックス
                $extra = "style=\"text-align: center\"";
                $row["ASSESSMARK"] = knjCreateTextBox($objForm, $model->fields["ASSESSMARK"][$counter], "ASSESSMARK-".$counter, 4, 2, $extra);

                //率テキストボックス
                $extra = "style=\"text-align: center\" onblur=\"this.value=toInteger(this.value)\"";
                $row["RATE"] = knjCreateTextBox($objForm, $model->fields["RATE"][$counter], "RATE-".$counter, 4, 2, $extra);

                $counter++;
                $model->assesslevelCnt = $dataCnt;
                $arg["data2"][] = $row;
            }
        } else {
            if ($model->field["ASSESSLEVELCNT"]) {
                $counter = 0;
                $aslvCnt = 1;
                //一覧表示
                $result = $db->query(knjz204Query::getRow2($model->patternCd));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //段階値表示
                    $row["ASSESSLEVEL"] = $aslvCnt;

                    //記号テキストボックス
                    $extra = "style=\"text-align: center\"";
                    $value = (!isset($model->warning)) ? $row["ASSESSMARK"] : $model->fields["ASSESSMARK"][$counter];
                    $row["ASSESSMARK"] = knjCreateTextBox($objForm, $value, "ASSESSMARK-".$counter, 4, 2, $extra);

                    //率テキストボックス
                    $extra = "style=\"text-align: center\" onblur=\"this.value=toInteger(this.value)\"";
                    $value = (!isset($model->warning)) ? $row["RATE"] : $model->fields["RATE"][$counter];
                    $value = floor($value);
                    $row["RATE"] = knjCreateTextBox($objForm, $value, "RATE-".$counter, 4, 2, $extra);

                    $counter++;
                    $model->assesslevelCnt = $aslvCnt;
                    $aslvCnt++;
                    $arg["data2"][] = $row;
                }
                $result->free();
            }
        }

        /********/
        /*ボタン*/
        /********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SDATE", CTRL_YEAR."/04/01");
        knjCreateHidden($objForm, "EDATE", (CTRL_YEAR+1)."/03/31");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz204index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz204Form2.html", $arg); 
    }
}
?>
