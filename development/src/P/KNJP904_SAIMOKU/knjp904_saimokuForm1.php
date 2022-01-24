<?php

require_once('for_php7.php');

class knjp904_saimokuForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjp904_saimokuForm1", "POST", "knjp904_saimokuindex.php", "", "knjp904_saimokuForm1");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "clear") unset($model->field);

        //校種
        $query = knjp904_saimokuQuery::getSchoolKind($model);
        $arg["SCHOOL_KIND"] = $db->getOne($query);

        //年度
        $arg["YEAR"] = $model->getYear."年度";

        //前年度
        $arg["LAST_YEAR"] = $model->lastYear."年度";

        //預り金科目
        $query = knjp904_saimokuQuery::getLevyLMst($model);
        $arg["LEVY_L_CD"] = $db->getOne($query);

        //預り金項目
        $query = knjp904_saimokuQuery::getLevyMMst($model);
        $arg["LEVY_M_CD"] = $db->getOne($query);

        //チェックボックス(全て)
        $extra = "id=\"CHECK_ALL\" onclick=\"allCheck(this)\"";
        $arg["CHECK_ALL"] = knjCreateCheckBox($objForm, "CHECK_ALL", "1", $extra, "");

        //去年の情報をセット
        $lastDataArr = array();
        $query = knjp904_saimokuQuery::getLastData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $lastDataArr[$row["LEVY_S_CD"]] = $row;

            //hidden
            knjCreateHidden($objForm, "HID_LASTYEAR_BUDET:".$row["LEVY_S_CD"], $row["LASTYEAR_BUDGET"]);
        }

        //リスト
        $model->updateArr = array();
        $query = knjp904_saimokuQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //チェックボックス
            $setName = "CHECK_BOX:".$row["LEVY_S_CD"];
            $extra = "id=\"{$setName}\" class=\"changeColor\" data-name=\"{$setName}\" onclick=\"checkOn(this)\"";
            $row["CHECK_BOX"] = knjCreateCheckBox($objForm, $setName, "1", $extra, "");
            $row["CHECK_BOX_NAME"] = $setName;

            //今年度予算テキストボックス
            $setName = "BUDGET_MONEY:".$row["LEVY_S_CD"];
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $row["BUDGET_MONEY"] = knjCreateTextBox($objForm, $row["BUDGET_MONEY"], $setName, 10, 10, $extra);

            //カンマ区切り
            $row["LASTYEAR_BUDGET"] = number_format($lastDataArr[$row["LEVY_S_CD"]]["LASTYEAR_BUDGET"]);
            $row["LASTYEAR_SCH_PRICE"] = number_format($lastDataArr[$row["LEVY_S_CD"]]["LASTYEAR_SCH_PRICE"]);

            //更新用にセット
            $model->updateArr[] = $row["LEVY_S_CD"];

            $arg["data2"][] = $row;
        }
        $result->free();

        /**************/
        /* ボタン作成 */
        /**************/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻る
        $link  = REQUESTROOT."/P/KNJP904/knjp904index.php?";
        $link .= "cmd=main";
        $link .= "&SEND_PRGID=KNJP904_SAIMOKU";
        $link .= "&SEND_OUTGO_L_CD={$model->getOutgoLcd}";
        $link .= "&SEND_SCHOOL_KIND={$model->getSchoolKind}";
        $extra = "onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp904_saimokuForm1.html", $arg);
    }
}
?>
