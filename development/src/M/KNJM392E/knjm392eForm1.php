<?php

require_once('for_php7.php');
class knjm392eForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm392eindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //スクーリング種別
        $query = knjm392eQuery::selectName("M001");
        $extra = "onChange=\"btn_submit('chg_course');\"";
        makeCmb($objForm, $arg, $db, $query, "COURSE", $model->field["COURSE"], $extra, 1);

        //日付データ
        if ($model->Date == "") $model->Date = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");

        //チェック用hidden
        knjCreateHidden($objForm, "YEAR", $model->Year);
        knjCreateHidden($objForm, "DEFOULTDATE", $model->Date);
        knjCreateHidden($objForm, "DEFOULTSEME", $model->semester);
        knjCreateHidden($objForm, "GAKKISU", $model->control["学期数"]);
        knjCreateHidden($objForm, "SEME1S", $model->control["学期開始日付"]["1"]);
        knjCreateHidden($objForm, "SEME1E", $model->control["学期終了日付"]["1"]);
        knjCreateHidden($objForm, "SEME2S", $model->control["学期開始日付"]["2"]);
        knjCreateHidden($objForm, "SEME2E", $model->control["学期終了日付"]["2"]);
        if ($model->control["学期数"] == 3) {
            knjCreateHidden($objForm, "SEME3S", $model->control["学期開始日付"]["3"]);
            knjCreateHidden($objForm, "SEME3E", $model->control["学期終了日付"]["3"]);
        }

        //講座コンボ
        $opt_chair = array();
        $result = $db->query(knjm392eQuery::getAuth($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
        if (!$model->field["CHAIR"]) {
            $model->field["CHAIR"]        = $opt_chair[0]["value"];
        }
        if (!$opt_chair[0]) {
            $arg["Closing"] = " closing_window('MSG300');";
        }
        $extra = "onChange=\"btn_submit('');\" ";
        $arg["sel"]["CHAIR"] = knjCreateCombo($objForm, "CHAIR", $model->field["CHAIR"], $opt_chair, $extra, 1);

        //校時
        $disabled = ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 3 || $model->field["COURSE"] == 4) ? "" : "disabled";
        $query = knjm392eQuery::selectName("B001");
        $extra = "onChange=\"btn_submit('change')\" ".$disabled;
        makeCmb($objForm, $arg, $db, $query, "PERIOD", $model->field["PERIOD"], $extra, 1);

        //担当者
        $disabled = ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4) ? "" : "disabled";
        $opt_staf = array();
        $result = $db->query(knjm392eQuery::selectStaff($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_staf[] = array("label" => $row["STAFFNAME"],
                                "value" => $row["STAFFCD"]);
        }

        if (!$model->field["STAFF"] && $model->User == 0) {
            $model->field["STAFF"] = $opt_staf[0]["value"];
        } else if (!$model->field["STAFF"] && $model->User == 1) {
            $model->field["STAFF"] = STAFFCD;
        }
        $extra = "$disabled onChange=\"btn_submit('change');\" ";
        $arg["sel"]["STAFF"] = knjCreateCombo($objForm, "STAFF", $model->field["STAFF"], $opt_staf, $extra, 1);

        //単位時間
        if ($model->cmd == 'chg_course') $model->field["CREDIT_TIME"] = "";
        $opt = array();
        $vals = array();
        $model->max_time = $db->getOne(knjm392eQuery::getCreditTime($model));
        if ($model->max_time) {
            for ($i = 1; $i <= $model->max_time; $i++) {
                $opt[] = array("label" => $i.'時間', "value" => $i);
                $vals[] = $i;
            }
        }
        if ($model->Properties["defaultCreditTime"] && in_array($model->Properties["defaultCreditTime"], $vals)) {
            $model->field["CREDIT_TIME"] = $model->Properties["defaultCreditTime"];
        } else {
            $model->field["CREDIT_TIME"] = $model->max_time;
        }
        $extra = "";
        $arg["sel"]["CREDIT_TIME"] = knjCreateCombo($objForm, "CREDIT_TIME", $model->field["CREDIT_TIME"], $opt, $extra, 1);

        // その他の更新確認ダイアログ
        if ($model->field["sonotaConfFlg"]) {
            $model->field["sonotaConfFlg"] = '';
            $arg["confirmSonota"] = " confirmSonota('add');";
        }

        //選択チェック
        $extra = "id=\"CHECK_ALL\" onClick=\"checkAll(this)\"";
        $arg["data"]["CHECK_ALL"] = knjCreateCheckBox($objForm, "CHECK_ALL", "1", $extra);

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/

        //ソート表示文字作成
        $order[1] = "▲";
        $order[2] = "▼";
        $model->getSort = $model->getSort ? $model->getSort : "SRT_SCHREGNO";

        //リストヘッダーソート作成
        $model->sort["SRT_SCHREGNO"] = $model->sort["SRT_SCHREGNO"] ? $model->sort["SRT_SCHREGNO"] : 1;
        $setOrder = $model->getSort == "SRT_SCHREGNO" ? $order[$model->sort["SRT_SCHREGNO"]] : "";
        $SRT_SCHREGNO = "<a href=\"knjm392eindex.php?cmd=sort&sort=SRT_SCHREGNO\" target=\"_self\" STYLE=\"color:white\">学籍番号{$setOrder}</a>";
        $arg["SRT_SCHREGNO"] = $SRT_SCHREGNO;

        //リストヘッダーソート作成
        $model->sort["SRT_HR_NAME"] = $model->sort["SRT_HR_NAME"] ? $model->sort["SRT_HR_NAME"] : 2;
        $setOrder = $model->getSort == "SRT_HR_NAME" ? $order[$model->sort["SRT_HR_NAME"]] : "";
        $SRT_HR_NAME = "<a href=\"knjm392eindex.php?cmd=sort&sort=SRT_HR_NAME\" target=\"_self\" STYLE=\"color:white\">クラス番号{$setOrder}</a>";
        $arg["SRT_HR_NAME"] = $SRT_HR_NAME;

        //抽出データ出力
        $schcnt = 0;
        $model->schregNos = array();
        //開講しない科目、講座情報チェック
        $model->Attendchaircd = "";
        $model->not_chairdat = $db->getOne(knjm392eQuery::getAttendChairDataCheck($model));
        //開講しない科目、講座情報取得
        if ($model->not_chairdat > 0) {
            $query = knjm392eQuery::getAttendChairData($model, "", "");
            $result = $db->query($query);
            $i = 0;
            while ($rowcheck = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($i == 0) {
                    $model->Attendchaircd  = $rowcheck["CHAIRCD"];
                } else {
                    $model->Attendchaircd .= ','.$rowcheck["CHAIRCD"];
                }
                $i++;
            }
            //開講する科目講座をセット
            $model->Attendchaircd .= ','.$model->field["CHAIR"];
        }
        $result = $db->query(knjm392eQuery::getSch($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //学籍番号
            $Row["SCHREGNO"] = $row["SCHREGNO"];
            $model->schregNos[] = $row["SCHREGNO"];
            //hidden
            knjCreateHidden($objForm, "SCHREGNO".$row["SCHREGNO"], $row["SCHREGNO"]);

            //選択チェックボックス
            $extra  = (isset($model->warning) && isset($model->setdata["CHECK"][$row["SCHREGNO"]])) ? " checked" : "";
            $Row["CHECK"] = knjCreateCheckBox($objForm, "CHECK-".$row["SCHREGNO"], "1", $extra, "");

            //クラス番号
            $Row["HR_NAME_ATTENDNO"] = $row["HR_NAME_ATTENDNO"];

            //氏名（漢字）
            $Row["NAME"] = $row["NAME_SHOW"];

            //クラス番号
            $Row["HR_NAME_ATTENDNO"] = $row["HR_NAME_ATTENDNO"];

            //校時
            $Row["PERIOD_NAME"] = $row["PERIOD_NAME"];

            //単位時間
            $Row["CREDIT_TIME_NAME"] = ($row["CREDIT_TIME"]) ? sprintf("%d", $row["CREDIT_TIME"]).'時間' : "";

            //登録日付
            $Row["T_TIME"] = $row["RECEIPT_TIME"];
            //hidden
            knjCreateHidden($objForm, "T_TIME".$row["SCHREGNO"], $row["RECEIPT_TIME"]);

            //備考
            $extra = "style=\"height:35px;\" ";
            $remark = (!isset($model->warning)) ? $row["REMARK"] : $model->setdata["REMARK"][$row["SCHREGNO"]];
            $Row["REMARK"] = knjCreateTextArea($objForm, "REMARK".$row["SCHREGNO"], 2, 20, "soft", $extra, $remark);
            //データ保持。入力値との比較用
            knjCreateHidden($objForm, "PRE_REMARK".$row["SCHREGNO"], $row["REMARK"]);

            //データ更新用
            knjCreateHidden($objForm, "AT_CHAIRCD".$row["SCHREGNO"], $row["AT_CHAIRCD"]);

            $arg["data2"][] = $Row;

            $schcnt++;
        }
        $model->schcntall = $schcnt;
        $result->free();

        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $extra = "onclick=\"return btn_submit('add');\" ";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "登 録", $extra);

        //$extra = "onclick=\"closeWin();\"";
        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        $extra = "onclick=\"return btn_submit('chdel');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "指定行削除", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "sonotaNotChk", $model->field["sonotaNotChk"]);
        knjCreateHidden($objForm, "sonotaConfFlg", $model->field["sonotaConfFlg"]);
        knjCreateHidden($objForm, "SCHREGNOS", implode(',', $model->schregNos));
        //登録中、サブミットする項目無効（一時的）のため用
        knjCreateHidden($objForm, "DIS_COURSE");
        knjCreateHidden($objForm, "DIS_CHAIR");
        knjCreateHidden($objForm, "DIS_PERIOD");
        knjCreateHidden($objForm, "DIS_STAFF");
        knjCreateHidden($objForm, "DIS_DATE");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm392eForm1.html", $arg); 
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

    $arg["sel"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
