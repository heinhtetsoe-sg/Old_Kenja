<?php

require_once('for_php7.php');

class knjm705Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm705index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //チェック用hidden
        knjCreateHidden($objForm, "DEFOULTDATE", $model->field["ATTENDDATE"]);

        //教科コンボ
        $query = knjm705Query::getClassMst($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["CLASSCD"], "CLASSCD", $extra, 1, "BLANK");

        //科目コンボ
        $query = knjm705Query::getSubClassMst($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //講座コンボ
        $query = knjm705Query::getChairDat($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        $blank = $model->field["CLASSCD"] == "94" ? "" : "BLANK";
        makeCmb($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", $extra, 1, $blank);

        //校時コンボ
        if (substr($model->field["CLASSCD"], 0, 2) == '93' && $model->cmd == 'change') $model->field["PERIODF"] = '7';
        if (substr($model->field["CLASSCD"], 0, 2) == '94' && $model->cmd == 'change') $model->field["PERIODF"] = '1';
        $query = knjm705Query::getNameMst("B001");
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PERIODF"], "PERIODF", $extra, 1, "BLANK");

        //単位時間コンボ
        if (substr($model->field["CLASSCD"], 0, 2) == '93' && $model->cmd == 'change') $model->field["CREDIT_TIME"] = '2';
        if (substr($model->field["CLASSCD"], 0, 2) == '94' && $model->cmd == 'change') $model->field["CREDIT_TIME"] = '2';
        $query = knjm705Query::getNameMst("M010");
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["CREDIT_TIME"], "CREDIT_TIME", $extra, 1, "BLANK");

        //日付データ
        $model->field["ATTENDDATE"] = $model->field["ATTENDDATE"] ? $model->field["ATTENDDATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["sel"]["ATTENDDATE"] = View::popUpCalendar($objForm ,"ATTENDDATE" ,str_replace("-", "/", $model->field["ATTENDDATE"]),"reload=true");

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
        $SRT_SCHREGNO = "<a href=\"knjm705index.php?cmd=sort&sort=SRT_SCHREGNO\" target=\"_self\" STYLE=\"color:white\">学籍番号{$setOrder}</a>";
        $arg["SRT_SCHREGNO"] = $SRT_SCHREGNO;

        //リストヘッダーソート作成
        $model->sort["SRT_HR_NAME"] = $model->sort["SRT_HR_NAME"] ? $model->sort["SRT_HR_NAME"] : 2;
        $setOrder = $model->getSort == "SRT_HR_NAME" ? $order[$model->sort["SRT_HR_NAME"]] : "";
        $SRT_HR_NAME = "<a href=\"knjm705index.php?cmd=sort&sort=SRT_HR_NAME\" target=\"_self\" STYLE=\"color:white\">クラス{$setOrder}</a>";
        $arg["SRT_HR_NAME"] = $SRT_HR_NAME;

        //学籍番号リストの初期化
        $model->schregNos = array();

        $query = knjm705Query::getSemesterMst($model);
        $setSem = $db->getOne($query);
        $setSem = $setSem ? $setSem : CTRL_SEMESTER;

        $result = $db->query(knjm705Query::getSch($model, $setSem));
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

            $Row["HR_ATTEND"] = $row["HR_ATTEND"];
            $Row["NAME"] = $row["NAME"];
            $Row["PERIODF"] = $row["PERIODF"];
            $Row["CREDIT_TIME"] = $row["CREDIT_TIME"];

            //備考
            $extra = "style=\"height:35px;\" ";
            $remark = (!isset($model->warning)) ? $row["REMARK"] : $model->setdata["REMARK"][$row["SCHREGNO"]];
            $Row["REMARK"] = knjCreateTextArea($objForm, "REMARK".$row["SCHREGNO"], 2, 20, "soft", $extra, $remark);
            //データ保持。入力値との比較用
            knjCreateHidden($objForm, "PRE_REMARK".$row["SCHREGNO"], $row["REMARK"]);

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
        knjCreateHidden($objForm, "SCHREGNOS", implode(',', $model->schregNos));
        //登録中、サブミットする項目無効（一時的）のため用
        knjCreateHidden($objForm, "DIS_CLASSCD");
        knjCreateHidden($objForm, "DIS_SUBCLASSCD");
        knjCreateHidden($objForm, "DIS_CHAIRCD");
        knjCreateHidden($objForm, "DIS_ATTENDDATE");
        knjCreateHidden($objForm, "DIS_PERIODF");
        knjCreateHidden($objForm, "DIS_CREDIT_TIME");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm705Form1.html", $arg); 
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
            'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = (strlen($value) && $value_flg) ? $value : $opt[0]["value"];
    $arg["sel"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
