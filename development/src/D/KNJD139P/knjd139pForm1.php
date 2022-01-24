<?php

require_once('for_php7.php');

class knjd139pForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd139pindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        
        //学年コード取得と小学生チェック
        $grade_cd = $db->getOne(knjd139pQuery::getGradeCd($model));
        if(isset($model->schregno) && $grade_cd == "") {
            $model->setWarning("MSG300",'小学生を選択して下さい。');
            unset($model->schregno);
            unset($model->name);
        }

        //学期コンボ
        $opt = array();
        $result = $db->query(knjd139pQuery::getSemester($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        $extra = "onChange=\"return btn_submit('edit');\"";
        $arg["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knjd139pQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
            $Row = $db->getRow(knjd139pQuery::getTrainRow($model, "1"), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
            $Row =& $model->Field;
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        /*通年の処理*/
        //委員会
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $Row["REMARK1"], $model);
        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->getPro["REMARK1"]["moji"]."文字X".$model->getPro["REMARK1"]["gyou"]."行まで)";
        
        //委員会参照
        $extra = "onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_committee"] = KnjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);

        //特別活動
        $arg["data"]["REMARK3"] = getTextOrArea($objForm, "REMARK3", $model->getPro["REMARK3"]["moji"], $model->getPro["REMARK3"]["gyou"], $Row["REMARK3"], $model);
        $arg["data"]["REMARK3_COMMENT"] = "(全角".$model->getPro["REMARK3"]["moji"]."文字X".$model->getPro["REMARK3"]["gyou"]."行まで)";

        //明小タイム 
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $Row["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYTIME"]["moji"]."文字X".$model->getPro["TOTALSTUDYTIME"]["gyou"]."行まで)";


        /*学期ごとの処理*/
        //生活（行動の記録ボタン）
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG="."KNJD139P"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."',0,0,600,500);\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "生活（行動の記録）", $extra);

        //係り
        $arg["data"]["REMARK2"] = getTextOrArea($objForm, "REMARK2", $model->getPro["REMARK2"]["moji"], $model->getPro["REMARK2"]["gyou"], $row["REMARK2"], $model);
        $arg["data"]["REMARK2_COMMENT"] = "(全角".$model->getPro["REMARK2"]["moji"]."文字X".$model->getPro["REMARK2"]["gyou"]."行まで)";
        
        //係り参照
        $extra = "onclick=\"return btn_submit('subform2');\"";
        $arg["button"]["btn_official"] = KnjCreateBtn($objForm, "btn_club", "係り参照", $extra);

        //生活や学習について気がついたこと
        //初期値設定(データが登録されていない場合のみ)
        if ($row["COMMUNICATION"] == "" ) {
            //1,2学期のとき
            if ($model->field["SEMESTER"] == '1' || $model->field["SEMESTER"] == '2') {
                $row["COMMUNICATION"] = "個人面談済み" ;
            //3学期のとき
            } else if ($model->field["SEMESTER"] == '3' ) {
                if ($grade_cd == '06') {
                    $row["COMMUNICATION"] = "ご卒業おめでとうございます。" ;
                }
            }
        }
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        $arg["data"]["COMMUNICATION_COMMENT"] = "(全角".$model->getPro["COMMUNICATION"]["moji"]."文字X".$model->getPro["COMMUNICATION"]["gyou"]."行まで)";


        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //CSV処理
        $REMARK1Size = 45;
        $fieldSize2 .= "REMARK1={$REMARK1Size}=委員会（通年）,";
        $REMARK3Size = 510;
        $fieldSize2 .= "REMARK3={$REMARK3Size}=特別活動（通年）,";
        $studySize   = 278;
        $fieldSize2 .= "TOTALSTUDYTIME={$studySize}=明小タイム（通年）,";

        $REMARK2Size = 30;
        $fieldSize  .= "REMARK2={$REMARK2Size}=特別活動の係り,";
        $commuSize   = 460;
        $fieldSize  .= "COMMUNICATION={$commuSize}=生活や学習について気がついたこと,";

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_D139P/knjx_d139pindex.php?FIELDSIZE=".$fieldSize."&FIELDSIZE2=".$fieldSize2."&SCHOOL_KIND=P','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "totalstudytime_gyou", $model->getPro["TOTALSTUDYTIME"]["gyou"]);
        knjCreateHidden($objForm, "totalstudytime_moji", $model->getPro["TOTALSTUDYTIME"]["moji"]);
        knjCreateHidden($objForm, "remark1_gyou", $model->getPro["REMARK1"]["gyou"]);
        knjCreateHidden($objForm, "remark1_moji", $model->getPro["REMARK1"]["moji"]);
        knjCreateHidden($objForm, "remark2_gyou", $model->getPro["REMARK2"]["gyou"]);
        knjCreateHidden($objForm, "remark2_moji", $model->getPro["REMARK2"]["moji"]);
        knjCreateHidden($objForm, "remark3_gyou", $model->getPro["REMARK3"]["gyou"]);
        knjCreateHidden($objForm, "remark3_moji", $model->getPro["REMARK3"]["moji"]);
        knjCreateHidden($objForm, "communication_gyou", $model->getPro["COMMUNICATION"]["gyou"]);
        knjCreateHidden($objForm, "communication_moji", $model->getPro["COMMUNICATION"]["moji"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd139pForm1.html", $arg);
    }
}

function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = ""; 
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = ""; 
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
