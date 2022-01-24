<?php

require_once('for_php7.php');

class knja128jForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;
        // Add by HPA for title and textarea_cursor 2020-01-20 start
        if($model->schregno != "" && $model->name != ""){
            $arg["TITLE"] = "".$model->schregno."". $model->name."の情報画面";
        }else{
            $arg["TITLE"] = "右情報画面";
        }
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJA128JForm1_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJA128JForm1_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJA128JForm1_CurrentCursor\");</script>";
            $model->message915 = "";
        }
        // Add by HPA for title and textarea_cursor 2020-01-31 end

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja128jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knja128jQuery::getTrainRow($model, ""), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
            $model->field["REMARK1_BG_COLOR_FLG"] = "";
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
        
        //道徳
        if ($model->field["REMARK1_BG_COLOR_FLG"]) {
            /* Edit by HPA for PC-talker 読み start 2020/01/20 */
            $extra = " background-color:#FFCCFF aria-label = \"道徳 全角".$model->remark1_moji."文字X".$model->remark1_gyou."行まで\"";
        } else {
            $extra = "aria-label = \"道徳 の 全角".$model->remark1_moji."文字X".$model->remark1_gyou."行まで \"";
        }
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->remark1_moji, $model->remark1_gyou, $row["REMARK1"], $model, $extra);
            /* Edit by HPA for PC-talker 読み end 2020/01/31 */

        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->remark1_moji."文字X".$model->remark1_gyou."行まで)";

        //学習活動
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "aria-label = \"学習活動 全角".$model->totalstudyact_moji."文字X".$model->totalstudyact_gyou."行まで\"";
        $arg["data"]["TOTALSTUDYACT"] = getTextOrArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_moji, $model->totalstudyact_gyou, $row["TOTALSTUDYACT"], $model,$extra);
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["TOTALSTUDYACT_COMMENT"] = "(全角".$model->totalstudyact_moji."文字X".$model->totalstudyact_gyou."行まで)";

        //観点
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "aria-label = \"観点 全角".$model->viewremark_moji."文字X".$model->viewremark_gyou."行まで\"";
        $arg["data"]["VIEWREMARK"] = getTextOrArea($objForm, "VIEWREMARK", $model->viewremark_moji, $model->viewremark_gyou, $row["VIEWREMARK"], $model,$extra);
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["VIEWREMARK_COMMENT"] = "(全角".$model->viewremark_moji."文字X".$model->viewremark_gyou."行まで)";

        //評価
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "aria-label = \"評価 全角".$model->totalstudyval_moji."文字X".$model->totalstudyval_gyou."行まで\"";
        $arg["data"]["TOTALSTUDYVAL"] = getTextOrArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_moji, $model->totalstudyval_gyou, $row["TOTALSTUDYVAL"], $model,$extra);
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["TOTALSTUDYVAL_COMMENT"] = "(全角".$model->totalstudyval_moji."文字X".$model->totalstudyval_gyou."行まで)";

        //行動の記録・特別活動の記録ボタン
        /* Edit by HPA for current_cursor start 2020/01/20 */
        $extra = " id= \"btn_form2\" onclick=\"current_cursor('btn_form2');return btn_submit('form2');\"";
        /* Edit by HPA for current_cursor end 2020/01/31 */
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "行動の記録・特別活動の記録", $extra);

        //自立活動の記録
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "aria-label = \"自立活動の記録 全角".$model->indep_remark_moji."文字X".$model->indep_remark_gyou."行まで\"";
        $arg["data"]["INDEPENDENT_REMARK"] = getTextOrArea($objForm, "INDEPENDENT_REMARK", $model->indep_remark_moji, $model->indep_remark_gyou, $row["INDEPENDENT_REMARK"], $model,$extra);
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["INDEPENDENT_REMARK_COMMENT"] = "(全角".$model->indep_remark_moji."文字X".$model->indep_remark_gyou."行まで)";

        //入学時の障害の状態
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "aria-label = \"入学時の障害の状態 全角".$model->disability_moji."文字X".$model->disability_gyou."行まで\"";
        $arg["data"]["ENT_DISABILITY_REMARK"] = getTextOrArea($objForm, "ENT_DISABILITY_REMARK", $model->disability_moji, $model->disability_gyou, $row["ENT_DISABILITY_REMARK"], $model,$extra);
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["ENT_DISABILITY_REMARK_COMMENT"] = "(全角".$model->disability_moji."文字X".$model->disability_gyou."行まで)";

        //総合所見及び指導上参考となる諸事項
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "aria-label = \"総合所見及び指導上参考となる諸事項 全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで\"";
        $arg["data"]["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->totalremark_moji, $model->totalremark_gyou, $row["TOTALREMARK"], $model,$extra);
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["TOTALREMARK_COMMENT"] = "(全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで)";

        //出欠の記録備考
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "aria-label = \"出欠の記録備考 全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで\"";
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $row["ATTENDREC_REMARK"], $model,$extra);
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //出欠の記録参照ボタン
        $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);

        //学校種別
        $schoolkind = $db->getOne(knja128jQuery::getSchoolKind($model));

        if ((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'J') {
            //更新ボタン
            /* Edit by HPA for PC-talker 読み start 2020/01/20 */
            $extra = "disabled aria-label = \"更新\"";
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //前の生徒へボタン
            $extra = " id=\"btn_up_pre\" style=\"width:130px\" onclick=\"current_cursor('btn_up_pre');top.left_frame.nextStudentOnly('pre');\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
            //次の生徒へボタン
            $extra = " id=\"btn_up_next\" style=\"width:130px\" onclick=\"current_cursor('btn_up_next');top.left_frame.nextStudentOnly('next');\"";
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
            /* Edit by HPA for PC-talker 読み start 2020/01/31 */
        } else {
            //更新ボタン
            /* Edit by HPA for PC-talker 読み start 2020/01/20 */
            $extra = "id =\"update\" onclick=\"current_cursor('update');return btn_submit('update');\" aria-label = \"更新\"";
            /* Edit by HPA for PC-talker 読み end 2020/01/31 */
            $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //更新後前の生徒へボタン
            // Add by HPA for PC-talker 2020-01-20 start
            $current_cursor = "current_cursor";
            $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update', $current_cursor);
            // Add by HPA for PC-talker 2020-01-31 end
        }

        //取消ボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "id =\"clear\" onclick=\"current_cursor('clear');return btn_submit('clear');\" aria-label = \"取消\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "onclick=\"closeWin();\" aria-label = \"終了\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //既入力内容参照（道徳）
        $extra = " id=\"shokenlist5\" onclick=\"current_cursor('shokenlist5');return btn_submit('shokenlist5');\" aria-label = \"道徳の既入力内容参照\"";
        $arg["button"]["shokenlist5"] = knjCreateBtn($objForm, "shokenlist5", "既入力内容の参照", $extra);
        //既入力内容参照（総合的な学習時間）
        $extra = " id=\"shokenlist1\" onclick=\"current_cursor('shokenlist1');return btn_submit('shokenlist1');\" aria-label = \"総合的な学習時間の既入力内容参照\"";
        $arg["button"]["shokenlist1"] = knjCreateBtn($objForm, "shokenlist1", "既入力内容の参照", $extra);
        //既入力内容参照（総合所見）
        $extra = " id=\"shokenlist2\" onclick=\"current_cursor('shokenlist2');return btn_submit('shokenlist2');\" aria-label = \"総合所見の既入力内容参照\"";
        $arg["button"]["shokenlist2"] = knjCreateBtn($objForm, "shokenlist2", "既入力内容の参照", $extra);
        //既入力内容参照（出欠の記録備考）
        $extra = " id=\"shokenlist3\" onclick=\"current_cursor('shokenlist3');return btn_submit('shokenlist3');\" aria-label = \"出欠の記録備考の既入力内容参照\"";
        $arg["button"]["shokenlist3"] = knjCreateBtn($objForm, "shokenlist3", "既入力内容の参照", $extra);
        //既入力内容参照（自立活動の記録）
        $extra = " id=\"shokenlist4\" onclick=\"current_cursor('shokenlist4');return btn_submit('shokenlist4');\" aria-label = \"自立活動の記録の既入力内容参照\"";
        $arg["button"]["shokenlist4"] = knjCreateBtn($objForm, "shokenlist4", "既入力内容の参照", $extra);

        //CSV処理
        $fieldSize  = "";
        $fieldSize .= "TOTALSTUDYACT=".((int)$model->totalstudyact_moji * (int)$model->totalstudyact_gyou * 3).",";
        $fieldSize .= "VIEWREMARK=".((int)$model->viewremark_moji * (int)$model->viewremark_gyou * 3).",";
        $fieldSize .= "TOTALSTUDYVAL=".((int)$model->totalstudyval_moji * (int)$model->totalstudyval_gyou * 3).",";
        $fieldSize .= "INDEPENDENT_REMARK=".((int)$model->indep_remark_moji * (int)$model->indep_remark_gyou * 3).",";
        $fieldSize .= "ENT_DISABILITY_REMARK=".((int)$model->disability_moji * (int)$model->disability_gyou * 3).",";
        $fieldSize .= "TOTALREMARK=".((int)$model->totalremark_moji * (int)$model->totalremark_gyou * 3).",";
        $fieldSize .= "ATTENDREC_REMARK=".((int)$model->attendrec_remark_moji * (int)$model->attendrec_remark_gyou * 3).",";
        $fieldSize .= "SPECIALACTREMARK=".((int)$model->specialactremark_moji * (int)$model->specialactremark_gyou * 3);

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A128J/knjx_a128jindex.php?FIELDSIZE=".$fieldSize."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
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
        View::toHTML5($model, "knja128jForm1.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
/* Edit by HPA for PC-talker 読み start 2020/01/20 */
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $setExtra = "") {
/* Edit by HPA for PC-talker 読み end 2020/01/31 */
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
        $extra = " id=\"".$name."\" style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ((int)$moji * 2), true);\" $setExtra";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
        knjCreateHidden($objForm, $name."_KETA", (int)$moji * 2);
        knjCreateHidden($objForm, $name."_GYO", $gyou);
        KnjCreateHidden($objForm, $name."_STAT", "statusarea".$name);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
}
//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled="") {
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled."id=\"syukketsukiroku\" onclick=\"current_cursor('syukketsukiroku');loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
?>
