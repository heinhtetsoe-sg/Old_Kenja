<?php

require_once('for_php7.php');

class knjz210jform1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz210jindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //観点情報を取得
        $query = knjz210jQuery::getTitleData();
        $result = $db->query($query);
        $model->getTitle = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->getTitle[] = $row;
        }
        $result->free();

        //チェック用
        $query = knjz210jQuery::getJviewCnt(CTRL_YEAR);
        $cnt = $db->getOne($query);
        knjCreateHidden($objForm, "CHECK_YEAR_CNT", $cnt);

        $query = knjz210jQuery::getJviewCnt((CTRL_YEAR - 1));
        $cnt = $db->getOne($query);
        knjCreateHidden($objForm, "CHECK_LASTYEAR_CNT", $cnt);

        //列タイトル
        foreach ($model->getTitle as $key => $val) {
            $arg["TITLE"][]["TITLENAME"] = $val["NAME1"];
        }

        $arg["setWith"] = 200 + (get_count($model->getTitle) * 60);

        $setScore= 100;
        for ($scoreCnt=1; $scoreCnt < 102; $scoreCnt++) {
            $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;background:darkgray\" readOnly ";
            $setArg["SCORE"] = knjCreateTextBox($objForm, $setScore, "SCORE".$setScore, 3, 3, $extra);
            $setData = "";
            $dataCnt = 1;
            foreach ($model->getTitle as $key => $val) {
                $setDataRow = array();
                $query = knjz210jQuery::getJviewCntMst($model, $setScore);
                $setDataRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $extra = " STYLE=\"text-align:right;\"";
                $setArg["JVIEW".$dataCnt] = knjCreateTextBox($objForm, $setDataRow["JVIEW".$dataCnt], "JVIEW".$dataCnt."_".$setScore, 1, 1, $extra);
                $dataCnt++;
            }
            $setScore--;
            $arg["data"][] = $setArg;
        }

        /**********/
        /* ボタン */
        /**********/
        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\" ";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\" ";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\" ";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\" ";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //終了ボタン
        $extra = "onclick=\"return closeWin();\" ";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(); " ;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210jForm1.html", $arg);
    }
}
?>
