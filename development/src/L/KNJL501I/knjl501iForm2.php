<?php

require_once('for_php7.php');

class knjl501iForm2
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl501iindex.php", "", "edit");

        // 各項目値
        $fields = array();

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg") && $model->testdiv) {
            $EntryFeeAndCostRow = $db->getRow(knjl501iQuery::getEntryFeeAndCostRow($model->leftYear, $model->applicantdiv, $model->testdiv), DB_FETCHMODE_ASSOC);
            if (is_array($EntryFeeAndCostRow)) {
                $fields = array_merge($fields, $EntryFeeAndCostRow);
            }
            $TestClasses = $db->getRow(knjl501iQuery::getTestClasses($model->leftYear, $model->applicantdiv, $model->testdiv), DB_FETCHMODE_ASSOC);
            if (is_array($TestClasses)) {
                $fields = array_merge($fields, $TestClasses);
            }
            $EntrySchedule1 = $db->getRow(knjl501iQuery::getQueryEntrySchedule1($model->leftYear, $model->applicantdiv, $model->testdiv), DB_FETCHMODE_ASSOC);
            if (is_array($EntrySchedule1)) {
                $fields = array_merge($fields, $EntrySchedule1);
            }
            $EntrySchedule2 = $db->getRow(knjl501iQuery::getQueryEntrySchedule2($model->leftYear, $model->applicantdiv, $model->testdiv), DB_FETCHMODE_ASSOC);
            if (is_array($EntrySchedule2)) {
                $fields = array_merge($fields, $EntrySchedule2);
            }
            $EntrySchedule3 = $db->getRow(knjl501iQuery::getQueryEntrySchedule3($model->leftYear, $model->applicantdiv, $model->testdiv), DB_FETCHMODE_ASSOC);
            if (is_array($EntrySchedule3)) {
                $fields = array_merge($fields, $EntrySchedule3);
            }
            $EntrySchedule4 = $db->getRow(knjl501iQuery::getQueryEntrySchedule4($model->leftYear, $model->applicantdiv, $model->testdiv), DB_FETCHMODE_ASSOC);
            if (is_array($EntrySchedule4)) {
                $fields = array_merge($fields, $EntrySchedule4);
            }
            $EntrySchedule5 = $db->getRow(knjl501iQuery::getQueryEntrySchedule5($model->leftYear, $model->applicantdiv, $model->testdiv), DB_FETCHMODE_ASSOC);
            if (is_array($EntrySchedule5)) {
                $fields = array_merge($fields, $EntrySchedule5);
            }
        } else {
            $fields =& $model->field;
        }

        $convertToInt = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["TESTDIV"] = knjCreateTextBox($objForm, $fields["TESTDIV"], "TESTDIV", 2, 2, $convertToInt);
        $arg["data"]["TESTDIV_NAME"] = knjCreateTextBox($objForm, $fields["TESTDIV_NAME"], "TESTDIV_NAME", 19, 10, "");
        $arg["data"]["TESTDIV_ABBV"] = knjCreateTextBox($objForm, $fields["TESTDIV_ABBV"], "TESTDIV_ABBV", 6, 3, "");
        $arg["data"]["TEST_DATE"] = View::popUpCalendar2($objForm, "TEST_DATE", str_replace("-", "/", $fields["TEST_DATE"]), "", "", "");
        $arg["data"]["ANNOUNCEMENT_DATE"] = View::popUpCalendar2($objForm, "ANNOUNCEMENT_DATE", str_replace("-", "/", $fields["ANNOUNCEMENT_DATE"]), "", "", "");
        $arg["data"]["ENTRY_FEE"] = knjCreateTextBox($objForm, $fields["ENTRY_FEE"], "ENTRY_FEE", 5, 6, $convertToInt);
        $arg["data"]["ENTRY_FEE_CLOSING_DATE"] = View::popUpCalendar2($objForm, "ENTRY_FEE_CLOSING_DATE", str_replace("-", "/", $fields["ENTRY_FEE_CLOSING_DATE"]), "", "", "");
        $arg["data"]["ENTRY_FEE_CLOSING_TIME"] = knjCreateTextBox($objForm, $fields["ENTRY_FEE_CLOSING_TIME"], "ENTRY_FEE_CLOSING_TIME", 4, 5, "");
        $arg["data"]["EQUIPMENT_COST"] = knjCreateTextBox($objForm, $fields["EQUIPMENT_COST"], "EQUIPMENT_COST", 5, 6, $convertToInt);
        $arg["data"]["EQUIPMENT_COST_CLOSING_DATE"] = View::popUpCalendar2($objForm, "EQUIPMENT_COST_CLOSING_DATE", str_replace("-", "/", $fields["EQUIPMENT_COST_CLOSING_DATE"]), "", "", "");
        $arg["data"]["EQUIPMENT_COST_CLOSING_TIME"] = knjCreateTextBox($objForm, $fields["EQUIPMENT_COST_CLOSING_TIME"], "EQUIPMENT_COST_CLOSING_TIME", 4, 5, "");
        $arg["data"]["PAYMENT_CLOSING_DATE"] = View::popUpCalendar2($objForm, "PAYMENT_CLOSING_DATE", str_replace("-", "/", $fields["PAYMENT_CLOSING_DATE"]), "", "", "");

        //科目チェックボックスおよびそのラベルを追加
        $result = $db->query(knjl501iQuery::getTestClassesListQuery($model));
        $clsNo = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //初期選択判定
            $extraClsCmb = "onclick='void(0)' id='TEST_CLASS_{$clsNo}'";
            if ($fields["TEST_CLASS_{$clsNo}"] != "" && $row["VALUE"] == $fields["TEST_CLASS_{$clsNo}"]) {
                $extraClsCmb = $extraClsCmb . " checked='true'";
            }

            $clsckbAndLbl = [];
            $clsckbAndLbl["TEST_CLASS_CHECKBOX"] = knjCreateCheckBox($objForm, "TEST_CLASS_{$clsNo}", $row["VALUE"], $extraClsCmb);
            $clsckbAndLbl["TEST_CLASS_ID"] = "TEST_CLASS_{$clsNo}";
            $clsckbAndLbl["TEST_CLASS_LABEL"] = $row["LABEL"];
            $arg["classes"][] = $clsckbAndLbl;

            $clsNo++;
        }

        //入学前スケジュールコントロールを追加
        for ($schdlNo =1; $schdlNo <= $model->SCHEDULE_BEFORE_ENTRY_MAX; $schdlNo++) {
            $rowEvntSchdl = [];
            $rowEvntSchdl["ENTRY_SCHDL_EVNT_INDEX"] = "{$schdlNo}";
            $rowEvntSchdl["ENTRY_SCHDL_EVNT_NAME"] = knjCreateTextBox($objForm, $fields["ENTRY_SCHDL_{$schdlNo}_EVNT_NAME"], "ENTRY_SCHDL_{$schdlNo}_EVNT_NAME", 98, 50, "");
            $rowEvntSchdl["ENTRY_SCHDL_EVNT_OPEN_DATE"] = View::popUpCalendar2($objForm, "ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_DATE", str_replace("-", "/", $fields["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_DATE"]), "", "", "");
            $rowEvntSchdl["ENTRY_SCHDL_EVNT_OPEN_TIME"] = knjCreateTextBox($objForm, $fields["ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_TIME"], "ENTRY_SCHDL_{$schdlNo}_EVNT_OPEN_TIME", 4, 5, "");
            $rowEvntSchdl["ENTRY_SCHDL_EVNT_CLOSE_TIME"] = knjCreateTextBox($objForm, $fields["ENTRY_SCHDL_{$schdlNo}_EVNT_CLOSE_TIME"], "ENTRY_SCHDL_{$schdlNo}_EVNT_CLOSE_TIME", 4, 5, "");
            $rowEvntSchdl["ENTRY_SCHDL_EVNT_PLACE"] = knjCreateTextBox($objForm, $fields["ENTRY_SCHDL_{$schdlNo}_EVNT_PLACE"], "ENTRY_SCHDL_{$schdlNo}_EVNT_PLACE", 59, 30, "");
            $rowEvntSchdl["ENTRY_SCHDL_EVNT_MAIN"] = knjCreateTextBox($objForm, $fields["ENTRY_SCHDL_{$schdlNo}_EVNT_MAIN"], "ENTRY_SCHDL_{$schdlNo}_EVNT_MAIN", 59, 30, "");
            $arg["schedules"][] = $rowEvntSchdl;
        }

        /********/
        /*ボタン*/
        /********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
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

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl501iindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl501iForm2.html", $arg);
    }
}
