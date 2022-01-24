<?php

require_once('for_php7.php');

class knja127pForm1 {

    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        // Add by PP for title and textarea_cursor 2020-01-20 start
        if($model->schregno != "" && $model->name != ""){
            $arg["TITLE"] = "".$model->schregno."".$model->name."の情報画面";
        } else {
            $arg["TITLE"] = "右情報画面";
        }
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJA127pForm1_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJA127pForm1_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJA127pForm1_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for title and textarea_cursor 2020-01-31 end

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja127pindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knja127pQuery::getTrainRemarkData($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        if($model->schregno == ""){
            $disabled = "disabled";
        } else {
            $disabled = "";
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //行動の記録
        // Edit by PP for PC-talker 2020-01-20 start
        $comment = "全角".$model->behaverec_remark_moji."文字X".$model->behaverec_remark_gyou."行まで";
        $extra = "aria-label=\"行動の記録 $comment\"";
        $arg["data"]["BEHAVEREC_REMARK"] = getTextOrArea($objForm, "BEHAVEREC_REMARK", $model->behaverec_remark_moji, $model->behaverec_remark_gyou, $row["BEHAVEREC_REMARK"], $model, $extra);
        $arg["data"]["BEHAVEREC_REMARK_COMMENT"] = "(全角".$model->behaverec_remark_moji."文字X".$model->behaverec_remark_gyou."行まで)";
        // Edit by PP for PC-talker 2020-01-31 end

        //総合所見及び指導上参考となる諸事項
        // Edit by PP for PC-talker 2020-01-20 start
        $comment = "全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで";
        $extra = "aria-label=\"総合所見及び指導上参考となる諸事項 $comment\"";
        $arg["data"]["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->totalremark_moji, $model->totalremark_gyou, $row["TOTALREMARK"], $model, $extra);
        $arg["data"]["TOTALREMARK_COMMENT"] = "(全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで)";
        // Edit by PP for PC-talker 2020-01-31 end
        
        //入学時の障害の状態
        // Edit by PP for PC-talker 2020-01-20 start
        $comment = "全角".$model->disability_moji."文字X".$model->disability_gyou."行まで";
        $extra = "aria-label=\"入学時の障害の状態 $comment\"";
        $gyo = (!in_array($model->z010, array('naraken', "kyoto")) && $model->disability_gyou > 7) ? "7" : $model->disability_gyou;
        $arg["data"]["ENT_DISABILITY_REMARK"] = getTextOrArea($objForm, "ENT_DISABILITY_REMARK", $model->disability_moji, $gyo, $row["ENT_DISABILITY_REMARK"], $model, $extra);
        $arg["data"]["ENT_DISABILITY_REMARK_COMMENT"] = "(全角".$model->disability_moji."文字X".$model->disability_gyou."行まで)";
        // Edit by PP for PC-talker 2020-01-31 end

        //出欠の記録備考
        // Edit by PP for PC-talker 2020-01-20 start
        $comment = "全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで";
        $extra = "aria-label=\"出欠の記録備考 $comment\"";
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $row["ATTENDREC_REMARK"], $model, $extra);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";
        // Edit by PP for PC-talker 2020-01-31 end

        //出欠の記録参照ボタン
        $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);

        //学校種別
        $schoolkind = $db->getOne(knja127pQuery::getSchoolKind($model));

        //道徳
        // Edit by PP for PC-talker 2020-01-20 start
        $comment = "全角".$model->foreignlangact4_moji."文字X".$model->foreignlangact4_gyou."行まで";
        $extra = "aria-label=\"道徳 $comment\"";
        $arg["data"]["FOREIGNLANGACT4"] = getTextOrArea($objForm, "FOREIGNLANGACT4", $model->foreignlangact4_moji, $model->foreignlangact4_gyou, $row["FOREIGNLANGACT4"], $model, $extra);
        $arg["data"]["FOREIGNLANGACT4_COMMENT"] = "(全角".$model->foreignlangact4_moji."文字X".$model->foreignlangact4_gyou."行まで)";
        // Edit by PP for PC-talker 2020-01-31 end

        //定型文選択ボタン
        $extra = "onclick=\"return btn_submit('teikei');\"";
        //$arg["button"]["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);


        if ((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'P') {
            //更新ボタン
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
            //前の生徒へボタン
            // Add by PP for current cursor 2020-01-20 start
            $extra = "id=\"btn_up_pre\" style=\"width:130px\" onclick=\"current_cursor('btn_up_pre'); top.left_frame.nextStudentOnly('pre');\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
            //次の生徒へボタン
            $extra = "id=\"btn_up_next\" style=\"width:130px\" onclick=\"current_cursor('btn_up_next'); top.left_frame.nextStudentOnly('next');\"";
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
            // Add by PP for current cursor 2020-01-31 end
        } else {
            //更新ボタン
            // Add by PP for PC-talker 2020-01-20 start
            $extra = " id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('update');\" aria-label=\"更新\"";
            // Add by PP for PC-talker 2020-01-31 end
            $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //更新後前の生徒へボタン
            // Add by PP for PC-talker 2020-01-20 start
            $current_cursor = "current_cursor";
            $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update', $current_cursor);
            // Add by PP for PC-talker 2020-01-31 end
        }

        //取消ボタン
        // Add by PP for PC-talker 2020-01-20 start
        $extra = " id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('clear');\" aria-label=\"取消\"";
        // Add by PP for PC-talker 2020-01-31 end
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\" aria-label=\"終了\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //障害の状態ボタン
        // Add by PP for PC-Talker 2020-01-20 start
        $extra = "id=\"btn_subform1\" onclick=\"current_cursor('btn_subform1'); return btn_submit('subform1');\"";
        // Add by PP for PC-Talker 2020-01-31 end
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "障害の状態", $extra.$disable);

        //CSV処理
        $fieldSize  = "";
        $fieldSize .= "BEHAVEREC_REMARK=".((int)$model->behaverec_remark_moji * (int)$model->behaverec_remark_gyou * 3).",";
        $fieldSize .= "TOTALREMARK=".((int)$model->totalremark_moji * (int)$model->totalremark_gyou * 3).",";
        $fieldSize .= "ENT_DISABILITY_REMARK=".((int)$model->disability_moji * (int)$model->disability_gyou * 3).",";
        $fieldSize .= "FOREIGNLANGACT4=".((int)$model->foreignlangact4_moji * (int)$model->foreignlangact4_gyou * 3).",";
        $fieldSize .= "ATTENDREC_REMARK=".((int)$model->attendrec_remark_moji * (int)$model->attendrec_remark_gyou * 3);

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A127P/knjx_a127pindex.php?FIELDSIZE=".$fieldSize."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja127pForm1.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
// Edit by PP for PC-talker 2020-01-20 start
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $setextra = "") {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = (int)$gyou % 5;
            $minus = ((int)$gyou / 5) > 1 ? ((int)$gyou / 5) * 6 : 5;
        }
        $height = (int)$gyou * 13.5 + ((int)$gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" $setextra";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" $setextra";
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
}
// Edit by PP for PC-talker 2020-01-31 end
//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled="") {
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." id=\"syukketsukiroku\" onclick=\"current_cursor('syukketsukiroku'); loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
?>
