<?php

require_once('for_php7.php');

class knjm392wForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm392windex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //スクーリング種別
        $query = knjm392wQuery::selectName("M001");
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
        $result = $db->query(knjm392wQuery::getAuth($model));
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

        $objForm->ae( array("type"    => "select",
                            "name"    => "CHAIR",
                            "size"    => "1",
                            "value"   => $model->field["CHAIR"],
                            "extrahtml" => "onChange=\"btn_submit('');\" ",
                            "options" => $opt_chair));

        $arg["sel"]["CHAIR"] = $objForm->ge("CHAIR");

        $result->free();

        //校時
        $disabled = ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 3 || $model->field["COURSE"] == 4) ? "" : "disabled";
        $query = knjm392wQuery::selectName("B001");
        $extra = "$disabled onChange=\"btn_submit('');\" ";
        makeCmb($objForm, $arg, $db, $query, "PERIOD", $model->field["PERIOD"], $extra, 1);

        //担当者
        $disabled = ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4) ? "" : "disabled";
        $opt_staf = array();
        $result = $db->query(knjm392wQuery::selectStaff($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_staf[] = array("label" => $row["STAFFNAME"],
                                "value" => $row["STAFFCD"]);
        }

        if (!$model->field["STAFF"] && $model->User == 0) {
            $model->field["STAFF"] = $opt_staf[0]["value"];
        } else if (!$model->field["STAFF"] && $model->User == 1) {
            $model->field["STAFF"] = STAFFCD;
        }
        $objForm->ae( array("type"    => "select",
                            "name"    => "STAFF",
                            "size"    => "1",
                            "value"   => $model->field["STAFF"],
                            "extrahtml" => "$disabled onChange=\"btn_submit('change');\" ",
                            "options" => $opt_staf));

        $arg["sel"]["STAFF"] = $objForm->ge("STAFF");

        $result->free();

        //単位時間
        if ($model->cmd == 'chg_course') $model->field["CREDIT_TIME"] = "";
        $opt = array();
        $max_time = $db->getOne(knjm392wQuery::getCreditTime($model));
        if ($max_time && $model->field["COURSE"] != "1" && $model->field["COURSE"] != 4) {
            for ($i = 1; $i <= $max_time; $i++) {
                $opt[] = array("label" => $i.'時間', "value" => $i);
            }
        }
        $extra = ($model->field["COURSE"] == "1" || $model->field["COURSE"] == 4) ? "disabled style=\"width:70px;background-color:darkgray;\"" : "";
        $arg["sel"]["CREDIT_TIME"] = knjCreateCombo($objForm, "CREDIT_TIME", $model->field["CREDIT_TIME"], $opt, $extra, 1);

        // その他の更新確認ダイアログ
        if ($model->field["sonotaConfFlg"]) {
            $model->field["sonotaConfFlg"] = '';
            $arg["confirmSonota"] = " confirmSonota('add');";
        }

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/
        //抽出データ出力
        $schcnt = 0;
        //開講しない科目、講座情報チェック
        $model->Attendchaircd = "";
        $model->not_chairdat = $db->getOne(knjm392wQuery::getAttendChairDataCheck($model));
        //開講しない科目、講座情報取得
        if ($model->not_chairdat > 0) {
            $query = knjm392wQuery::getAttendChairData($model, "", "");
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
        $result = $db->query(knjm392wQuery::getSch($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //学籍番号
            $Row["SCHREGNO"] = $row["SCHREGNO"];
            //hidden
            knjCreateHidden($objForm, "SCHREGNO".$schcnt, $row["SCHREGNO"]);

            //氏名（漢字）
            $Row["NAME"] = $row["NAME_SHOW"];

            //登録日付
            $Row["T_TIME"] = $row["RECEIPT_TIME"];
            //hidden
            knjCreateHidden($objForm, "T_TIME".$schcnt, $row["RECEIPT_TIME"]);

            //出席者
            $extra  = (!isset($model->warning) && strlen($row["ATTEND"]) || isset($model->warning) && isset($model->setdata["ATTEND"][$schcnt])) ? "checked" : "";
            $Row["ATTEND"] = knjCreateCheckBox($objForm, "ATTEND".$schcnt, "1", $extra, "");
            //データ保持。入力値との比較用
            knjCreateHidden($objForm, "PRE_ATTEND".$schcnt, $row["ATTEND"]);

            //備考
            $extra = "style=\"height:35px;\" ";
            $remark = (!isset($model->warning)) ? $row["REMARK"] : $model->setdata["REMARK"][$schcnt];
            $Row["REMARK"] = knjCreateTextArea($objForm, "REMARK".$schcnt, 2, 20, "soft", $extra, $remark);
            //データ保持。入力値との比較用
            knjCreateHidden($objForm, "PRE_REMARK".$schcnt, $row["REMARK"]);

            //データ更新用
            knjCreateHidden($objForm, "AT_CHAIRCD".$schcnt, $row["AT_CHAIRCD"]);

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
        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";	//2013/01/15 キーイベントタイムアウト処理復活
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        $extra = "onclick=\"return btn_submit('chdel');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "指定行削除", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "sonotaNotChk", $model->field["sonotaNotChk"]);
        knjCreateHidden($objForm, "sonotaConfFlg", $model->field["sonotaConfFlg"]);
        //登録中、サブミットする項目無効（一時的）のため用
        knjCreateHidden($objForm, "DIS_COURSE");
        knjCreateHidden($objForm, "DIS_CHAIR");
        knjCreateHidden($objForm, "DIS_PERIOD");
        knjCreateHidden($objForm, "DIS_STAFF");
        knjCreateHidden($objForm, "DIS_DATE");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm392wForm1.html", $arg); 
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
