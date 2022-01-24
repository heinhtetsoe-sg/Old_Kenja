<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjf165aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform3", "POST", "knjf165aindex.php", "", "subform3");

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //来室日付範囲（テキスト）
        $model->sDate = ($model->sDate) ? str_replace("-", "/", $model->sDate) : str_replace("-", "/", CTRL_DATE);
        $model->eDate = ($model->eDate) ? str_replace("-", "/", $model->eDate) : str_replace("-", "/", CTRL_DATE);
        $extra = "onChange=\"isDate(this); tmp_list('knjf165a', 'on')\"";
        $sDate_Textbox = knjCreateTextBox($objForm, $model->sDate, "SDATE", 12, 12, $extra);
        $eDate_Textbox = knjCreateTextBox($objForm, $model->eDate, "EDATE", 12, 12, $extra);
        //来室日付範囲（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjf165a', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=SDATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['SDATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $sDate_Button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        $extra = "onclick=\"tmp_list('knjf165a', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=EDATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['EDATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $eDate_Button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //来室日付範囲
        $arg["SDATE"] = View::setIframeJs().$sDate_Textbox.$sDate_Button;
        $arg["EDATE"] = View::setIframeJs().$eDate_Textbox.$eDate_Button;
        $db = Query::dbCheckOut();

        //入力項目件数を取得
        $query = knjf165aQuery::getRemark($model);
        $result = $db->query($query);
        $outcnt = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $outcnt++;
        }
        $result->free();

        //データ取得
        if (get_count($model->remarkarry) < 1) {
            //未入力なら、DBから取得。
            $recarry = array();
            $recarry[] = $this->getDataList($db, $model);
        }
        //改めて入力項目を設定
        $query = knjf165aQuery::getRemark($model);
        $result = $db->query($query);
        $idx = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setbuf = array();
            //選択チェックボックス
            $value = "1";
            $extra = "id=\"CHECKED_".$idx."\"";
            $setbuf["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED_".$idx, $value, $extra, "1");
            knjCreateHidden($objForm, "SCHREGNO_".$idx, $row["SCHREGNO"]);

            //TYPE
            $setbuf["TYPE"] = $row["TYPE_NAME"];
            knjCreateHidden($objForm, "TYPE_".$idx, $row["TYPE"]);
            //GRADE - HR_CLASS - ATTENDNO
            $setbuf["GRADE"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
            knjCreateHidden($objForm, "GRADE_".$idx, $row["GRADE"]);
            knjCreateHidden($objForm, "HR_CLASS_".$idx, $row["HR_CLASS"]);
            knjCreateHidden($objForm, "ATTENDNO_".$idx, $row["ATTENDNO"]);

            //NAME
            $setbuf["NAME"] = $row["NAME"];
            knjCreateHidden($objForm, "NAME_".$idx, $row["NAME"]);
            //VISIT_DATE
            list($data1, $data2, $data3) = explode('-', $row["VISIT_DATE"]);
            $val = $data1."年".$data2."月".$data3."日".$row["VISIT_HOUR"]."時".$row["VISIT_MINUTE"]."分";
            $setbuf["VISIT_DATE"] = $val;
            knjCreateHidden($objForm, "VISIT_DATE_".$idx, $row["VISIT_DATE"]);
            knjCreateHidden($objForm, "VISIT_HOUR_".$idx, $row["VISIT_HOUR"]);
            knjCreateHidden($objForm, "VISIT_MINUTE_".$idx, $row["VISIT_MINUTE"]);

            $wkarry = $recarry[0];

            //転帰
            $extra = "id=\"REMARK2_".$idx."\"";
            $opt = array();
            $value = ($model->cmd != "clear" && $model->remark2Arry[$idx]!="") ? $model->remark2Arry[$idx] : $wkarry["REMARK2_".$idx];
            $value_flg = false;
            $query = knjf165aQuery::getNameMst("F225");
            $result2 = $db->query($query);
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row2["LABEL"],
                               'value' => $row2["VALUE"]);
                if ($value == $row2["VALUE"]) $value_flg = true;
            }
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
            $setbuf["REMARK2"] = knjCreateCombo($objForm, "REMARK2_".$idx, $value, $opt, $extra, 1);
            $result2->free();

            //申請日
            $value = ($model->cmd != "clear" && $model->remark3Arry[$idx]!="") ? $model->remark3Arry[$idx] : $wkarry["REMARK3_".$idx];
            $extra = "id=\"REMARK3_".$idx."\"";
            $setbuf["REMARK3"] = knjCreateTextBox($objForm, $value, "REMARK3_".$idx, 12, 12, $extra);
            //支給日
            $value = ($model->cmd != "clear" && $model->remark4Arry[$idx]!="") ? $model->remark4Arry[$idx] : $wkarry["REMARK4_".$idx];
            $extra = "id=\"REMARK4_".$idx."\"";
            $setbuf["REMARK4"] = knjCreateTextBox($objForm, $value, "REMARK4_".$idx, 12, 12, $extra);
            //医療点数
            $value = ($model->cmd != "clear" && $model->remark5Arry[$idx]!="") ? $model->remark5Arry[$idx] : $wkarry["REMARK5_".$idx];
            $extra = "id=\"REMARK5_".$idx."\"";
            $setbuf["REMARK5"] = knjCreateTextBox($objForm, $value, "REMARK5_".$idx, 5, 5, $extra);
            //支給額
            $value = ($model->cmd != "clear" && $model->remark6Arry[$idx]!="") ? $model->remark6Arry[$idx] : $wkarry["REMARK6_".$idx];
            $extra = "id=\"REMARK6_".$idx."\"";
            $setbuf["REMARK6"] = knjCreateTextBox($objForm, $value, "REMARK6_".$idx, 7, 7, $extra);
            //受給日数
            $value = ($model->cmd != "clear" && $model->remark7Arry[$idx]!="") ? $model->remark7Arry[$idx] : $wkarry["REMARK7_".$idx];
            $extra = "id=\"REMARK7_".$idx."\"";
            $setbuf["REMARK7"] = knjCreateTextBox($objForm, $value, "REMARK7_".$idx, 12, 12, $extra);
            //備考
            $value = ($model->cmd != "clear" && $model->remark8Arry[$idx]!="") ? $model->remark8Arry[$idx] : $wkarry["REMARK8_".$idx];
            $extra = "id=\"REMARK8_".$idx."\"";
            $setbuf["REMARK8"] = knjCreateTextBox($objForm, $value, "REMARK8_".$idx, 50, 50, $extra);

            $arg["data"][] = $setbuf;
            $idx++;
        }
        $result->free();
        Query::dbCheckIn($db);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJF165A");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_ROWCNT", $outcnt);
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjf165aForm1.html", $arg);
    }

    function getDataList($db, $model) {
        $retlist = array();
        $query = knjf165aQuery::getRemark($model);
        $result = $db->query($query);
        $cnt = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($rmk = 2; $rmk<=8; $rmk++) {
                $getval = "";
                if ($row["REMARK".$rmk] !== null && $row["REMARK".$rmk] !== "") {
                    $getval = $row["REMARK".$rmk];
                }
                $retlist["REMARK".$rmk."_".$cnt] = $getval;
            }
            $cnt++;
        }
        return $retlist;
    }
}
?>
