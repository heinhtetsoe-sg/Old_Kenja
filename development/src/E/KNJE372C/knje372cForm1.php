<?php

require_once('for_php7.php');

class knje372cForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje372cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["TOP"]["CTRL_YEAR"] = $model->year;

        //学級コンボ
        $extra = "onchange=\"btn_submit('changeCmb');\" tabindex=\"-1\" ";
        $query = knje372cQuery::getGradeHrClass($model);
        $arg["GRADE_HR_CLASS"] = makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        //一覧
        $model->schregnoList = array();
        $query = knje372cQuery::getList($model);
        $result = $db->query($query);
        $schRow = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!array_key_exists($row["SCHREGNO"], $schRow)) {
                $schRow[$row["SCHREGNO"]] = array("ATTENDNO"               => $row["ATTENDNO"],
                                                  "PROFICIENCY1_AVG"       => $row["PROFICIENCY1_AVG"],
                                                  "PROFICIENCY2_AVG"       => $row["PROFICIENCY2_AVG"],
                                                  "NAME"                   => $row["NAME"],
                                                  "ACTIVITY_CD"            => $row["ACTIVITY_CD"],
                                                  "ACTIVITY_CONTENT"       => $row["ACTIVITY_CONTENT"],
                                                  "DECLINE_FLG"            => $row["DECLINE_FLG"],
                                                  );
            }
            if ($row["HOPE_ORDER"]) {
                $schRow[$row["SCHREGNO"]]["HOPEORDER".$row["HOPE_ORDER"]] = $row["DEPARTMENT_CD"];
            }
        }
        $result->free();

        //推薦枠マスタのレコード数取得
        $cntQuery = knje372cQuery::getRecLimitCnt();
        $limitCnt = $db->getOne($cntQuery) * 2;
        $model->limitCnt = $limitCnt <= 42 ? $limitCnt : 42;

        for($i = 1; $i <= 42; $i++) {
            
            $arg["HOPE_NO".$i] = ($model->limitCnt >= $i) ? sprintf("%02d", $i) : "　";
        }

        //校友会活動CDのSQL
        $queryE071 = knje372cQuery::getNameMstE071();

        $rowCnt = 1; //※生徒毎にテキストボックス行が2行あるので、その単位で行数をカウントする
        $colCnt = 1;
        $model->schregnoList = array();
        $model->schregnoRowList = array();
        foreach($schRow as $schregno => $schData) {
            $model->schregnoRowList[$schregno][] = $rowCnt;     //1段目の行番号を取得
            $model->schregnoRowList[$schregno][] = $rowCnt + 1; //2段目の行番号を取得

            for ($i = 1; $i <= 42; $i++) {
                $extra = "onblur=\"checkVal(this);\" onpaste=\"showPaste(this);\" ";
                if ($i <= $model->limitCnt) {
                    $hopeOrder  = sprintf("%02d", $i);
                    $value = (isset($model->warning)) ? $model->schField[$schregno]["HOPE_ORDER"][$hopeOrder] : $schData["HOPEORDER".$hopeOrder];
                    $schData["DISP_HOPEORDER".$hopeOrder] = knjCreateTextBox($objForm, $value, "HOPEORDER".$colCnt."-".$rowCnt, 2, 2, $extra);
                }
                $colCnt++;
                if ($i == 21) {
                    $rowCnt++;
                    $colCnt = 1;
                }
            }

            //校友会活動CDコンボ
            $extra = " style=\"width:60px;\" tabindex=\"-1\" ";
            $value = (isset($model->warning)) ? $model->schField[$schregno]["ACTIVITY_CD"] : $schData["ACTIVITY_CD"];
            $schData["ACTIVITY_CD"] = makeCmb($objForm, $arg, $db, $queryE071, $value, "ACTIVITY_CD_".$schregno, $extra, 1);

            //校友会活動内容
            $value = (isset($model->warning)) ? $model->schField[$schregno]["ACTIVITY_CONTENT"] : $schData["ACTIVITY_CONTENT"];
            $schData["ACTIVITY_CONTENT"] = getTextOrArea($objForm, "ACTIVITY_CONTENT_".$schregno, $model->moji, $model->gyou, $value, $model);
            $schData["ACTIVITY_CONTENT_COMMENT"] = setInputChkHidden($objForm, "ACTIVITY_CONTENT_".$schregno, $model->moji, $model->gyou, $arg);

            //辞退
            $value = (isset($model->warning)) ? $model->schField[$schregno]["DECLINE_FLG"] : $schData["DECLINE_FLG"];
            $extra = " tabindex=\"-1\" ";
            $extra .= ($value == "1") ? " checked " : "";
            $schData["DECLINE_FLG"] = knjCreateCheckBox($objForm, "DECLINE_FLG_".$schregno, 1, $extra);

            $rowCnt++;
            $colCnt = 1;
            $model->schregnoList[] = $schregno;
            $model->errDispStr[$schregno] = $schData["ATTENDNO"];
            $arg["data"][] = $schData;
        }

        //入力値チェック用
        $departmentCdArray = knje372cQuery::getRecLimitDepartmentCd($db);
        knjCreateHidden($objForm, "validateValues", implode(",", $departmentCdArray));

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML5($model, "knje372cForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\" ";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印刷/プレビュー", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);

    //終了ボタン
    $extra = " onclick=\"return closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //CSV画面遷移ボタン
        $extra = "id= \"btn_csv\" onClick=\" wopen('".REQUESTROOT."/X/KNJX_E372C/knjx_e372cindex.php?SEND_PRGID=KNJE372C&SEND_AUTH={$model->auth}&SEND_GRADE_HR_CLASS={$model->field["GRADE_HR_CLASS"]}&LIMITCNT={$model->limitCnt}','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV処理", $extra);
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $res = $db->query($query);
    while ($row = $res->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "limitCnt", $model->limitCnt);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    list($grade, $hrclass) = explode("-", $model->field["GRADE_HR_CLASS"]);
    knjCreateHidden($objForm, "GRADE", $grade);
    knjCreateHidden($objForm, "HR_CLASS", $hrclass);

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE372C");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

}

function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" id=\"".$name."\" tabindex=\"-1\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\" tabindex=\"-1\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg) {
    //KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    //KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    //KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);

    return getTextAreaComment($keta, $gyo);
}

function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}



?>
