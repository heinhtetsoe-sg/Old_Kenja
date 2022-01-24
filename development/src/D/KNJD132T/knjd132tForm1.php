<?php

require_once('for_php7.php');

class knjd132tForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd132tindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["WindowWidth"]      = $model->windowWidth  - 36;
        $arg["titleWindowWidth"] = $model->windowWidth  - 237;
        $arg["valWindowWidth"]   = $model->windowWidth  - 220;
        $arg["valWindowHeight"]  = $model->windowHeight - 210;
        $arg["tcolWindowHeight"] = $model->windowHeight - 227;
        $resizeFlg = $model->cmd == "cmdStart" ? true : false;

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd132tQuery::getSemesterList($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $semester = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年組コンボ作成
        $query = knjd132tQuery::getGradeHrclass($model, $semester);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        //radio(1:学習・特別活動の記録１, 2:学習・特別活動の記録２)
        $opt = array(1, 2);
        $model->field["DATADIV"] = ($model->field["DATADIV"] == "") ? "1" : $model->field["DATADIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATADIV{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATADIV", $model->field["DATADIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        $arg["setWidth"]  = 0;
        if ($model->field["DATADIV"] == "1") {
            $arg["DATA_DIV1"] = "1";
            $arg["DATA_DIV2"] = "";
            $model->setFieldName = array("学習活動"     => "TOTALSTUDYTIME", 
                                         "観点"         => "VIEWPOINT", 
                                         "活動の様子"   => "SPECIALACTREMARK", 
                                         "学級活動"     => "CLASS_ACT", 
                                         "生徒会活動"   => "SCH_COUNCIL", 
                                         "学校行事"     => "SCHOOL_EVENT", 
                                         "部活動名"     => "CLUB_NAME", 
                                         "部活動の記録" => "CLUB_MEMO");
        } else {
            $arg["DATA_DIV1"] = "";
            $arg["DATA_DIV2"] = "1";
            $model->setFieldName = array("道徳欄" => "OTHER_MORAL", "その他の活動" => "OTHER_ACT", "通信欄" => "COMMUNICATION");
        }

        //テキストの名前を取得する
        $textFieldName = "";
        $textSep = "";

        //width等をセット
        foreach($model->setFieldName as $key => $fieldName) {
            $arg["COMMENT-".$fieldName] = $model->getPro[$fieldName];
            $arg["WITH-".$fieldName]    = $model->getPro[$fieldName] * 15.5;
            $arg["setWidth"]           += $model->getPro[$fieldName] * 15.5 + 10;

            $textFieldName .= $textSep.$fieldName;
            $textSep = ",";
        }
        $arg["WITH-SOUGOU"]    = $arg["WITH-TOTALSTUDYTIME"] + $arg["WITH-VIEWPOINT"] + $arg["WITH-SPECIALACTREMARK"];
        $arg["WITH-TOKUBETSU"] = $arg["WITH-CLASS_ACT"] + $arg["WITH-SCH_COUNCIL"] + $arg["WITH-SCHOOL_EVENT"];

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd132tQuery::selectQuery($model, $semester));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];
            $model->data["ATTENDNO"][$row["SCHREGNO"]] = $row["ATTENDNO"];

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            if ($model->field["DATADIV"] == "1") {
                /**総合的な学習の時間**/
                //学習活動
                $value = (!isset($model->warning)) ? $row["TOTALSTUDYTIME"] : $model->fields["TOTALSTUDYTIME"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["TOTALSTUDYTIME"] * 2;
                $setName  = "TOTALSTUDYTIME-".$row["SCHREGNO"];
                $row["TOTALSTUDYTIME"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);
                //観点
                $value = (!isset($model->warning)) ? $row["VIEWPOINT"] : $model->fields["VIEWPOINT"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["VIEWPOINT"] * 2;
                $setName  = "VIEWPOINT-".$row["SCHREGNO"];
                $row["VIEWPOINT"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);
                //学習活動の様子
                $value = (!isset($model->warning)) ? $row["SPECIALACTREMARK"] : $model->fields["SPECIALACTREMARK"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["SPECIALACTREMARK"] * 2;
                $setName  = "SPECIALACTREMARK-".$row["SCHREGNO"];
                $row["SPECIALACTREMARK"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);

                /**特別活動の記録**/
                //学級活動
                $value = (!isset($model->warning)) ? $row["CLASS_ACT"] : $model->fields["CLASS_ACT"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["CLASS_ACT"] * 2;
                $setName  = "CLASS_ACT-".$row["SCHREGNO"];
                $row["CLASS_ACT"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);
                //生徒会活動
                $value = (!isset($model->warning)) ? $row["SCH_COUNCIL"] : $model->fields["SCH_COUNCIL"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["SCH_COUNCIL"] * 2;
                $setName  = "SCH_COUNCIL-".$row["SCHREGNO"];
                $row["SCH_COUNCIL"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);
                //学校行事
                $value = (!isset($model->warning)) ? $row["SCHOOL_EVENT"] : $model->fields["SCHOOL_EVENT"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["SCHOOL_EVENT"] * 2;
                $setName  = "SCHOOL_EVENT-".$row["SCHREGNO"];
                $row["SCHOOL_EVENT"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);

                //部活動
                $value = (!isset($model->warning)) ? $row["CLUB_NAME"] : $model->fields["CLUB_NAME"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["CLUB_NAME"] * 2;
                $setName  = "CLUB_NAME-".$row["SCHREGNO"];
                $row["CLUB_NAME"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);
                //部活動の記録
                $value = (!isset($model->warning)) ? $row["CLUB_MEMO"] : $model->fields["CLUB_MEMO"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["CLUB_MEMO"] * 2;
                $setName  = "CLUB_MEMO-".$row["SCHREGNO"];
                $row["CLUB_MEMO"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);
            } else {
                //道徳欄
                $value = (!isset($model->warning)) ? $row["OTHER_MORAL"] : $model->fields["OTHER_MORAL"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["OTHER_MORAL"] * 2;
                $setName  = "OTHER_MORAL-".$row["SCHREGNO"];
                $row["OTHER_MORAL"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);
                //その他の活動
                $value = (!isset($model->warning)) ? $row["OTHER_ACT"] : $model->fields["OTHER_ACT"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["OTHER_ACT"] * 2;
                $setName  = "OTHER_ACT-".$row["SCHREGNO"];
                $row["OTHER_ACT"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);
                //通信欄
                $value = (!isset($model->warning)) ? $row["COMMUNICATION"] : $model->fields["COMMUNICATION"][$row["SCHREGNO"]];
                $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
                $twiceNum = $model->getPro["COMMUNICATION"] * 2;
                $setName  = "COMMUNICATION-".$row["SCHREGNO"];
                $row["COMMUNICATION"] = knjCreateTextBox($objForm, $value, $setName, $twiceNum + 1, $twiceNum, $extra);
            }

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //テキストの名前を取得
        knjCreateHidden($objForm, "TEXT_FIELD_NAME", $textFieldName);

        //ボタン作成
        //更新ボタンを作成する
        $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJD132T");
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "SCHOOLKIND", $model->schoolKind);

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd132tForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
