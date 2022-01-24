<?php

require_once('for_php7.php');

class knja120cForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knja120cindex.php", "", "edit");
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = knja120cQuery::getTrainRow($model->schregno, $model->exp_year);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }

        //明治判定
        $meiji = knja120cQuery::getMeijiHantei();
        if ($meiji > 0) {
             $arg["meiji"] = 1;
        } else {
             $arg["not_meiji"] = 1;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //活動内容
            $extra = "";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 8, 45, "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角22文字X8行まで)';

            //評価（明治:Catholic Spirit）
            $extra = "style=\"height:90px;\"";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, 45, "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角22文字X8行まで)';

            //出欠の記録備考
            $extra = "";
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 81, "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角40文字X2行まで)';
        } else {
            //活動内容
            $extra = "";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 5, 23, "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角11文字X4行まで)';

            //評価（明治:Catholic Spirit）
            $extra = "style=\"height:90px;\"";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", 6, 23, "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角11文字X6行まで)';

            //出欠の記録備考
            $extra = "";
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 41, "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角20文字X2行まで)';
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            //活動内容
            $height = (int)$model->totalstudyact_gyou * 13.5 + ((int)$model->totalstudyact_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ((int)$model->totalstudyact_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            //評価（明治:Catholic Spirit）
            $height = (int)$model->totalstudyval_gyou * 13.5 + ((int)$model->totalstudyval_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ((int)$model->totalstudyval_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            //出欠の記録備考
            $height = (int)$model->attendrec_remark_gyou * 13.5 + ((int)$model->attendrec_remark_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ((int)$model->attendrec_remark_moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";
        }

        if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            //特別活動所見
            $extra = "";
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 45, "soft", $extra, $row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角22文字X10行まで)';
        } else {
            //特別活動所見
            $extra = "style=\"height:90px;\"";
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角11文字X6行まで)';
        }
        if ($model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            //特別活動所見
            $height = (int)$model->specialactremark_gyou * 13.5 + ((int)$model->specialactremark_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", $model->specialactremark_gyou, ((int)$model->specialactremark_moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = "(全角{$model->specialactremark_moji}文字{$model->specialactremark_gyou}行まで)";
        }

        if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            //総合所見
            $extra = "";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 8, 133, "soft", $extra, $row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X8行まで)';
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //総合所見
            $extra = "style=\"height:90px;\"";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 7, 133, "soft", $extra, $row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X7行まで)';
        } else {
            //総合所見
            $extra = "style=\"height:90px;\"";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 6, 89, "soft", $extra, $row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角44文字X6行まで)';
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            //総合所見
            $height = (int)$model->totalremark_gyou * 13.5 + ((int)$model->totalremark_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", $model->totalremark_gyou, ((int)$model->totalremark_moji * 2 + 1), "soft", $extra, $row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = "(全角{$model->totalremark_moji}文字{$model->totalremark_gyou}行まで)";
        }


        //出欠備考参照ボタン
        $sdate = $model->exp_year.'-04-01';
        $edate = ((int)$model->exp_year+1).'-03-31';
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            if (!$model->schregno) {
                $extra = "onclick=\"alert('データを指定してください。')\"";
            } else {
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "まとめ出欠備考参照", $extra);
        } else {
            if (!$model->schregno) {
                $extra = "onclick=\"alert('データを指定してください。')\"";
            } else {
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }

        //要録の出欠備考参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "TYOSASYO_SANSYO",
                            "value"     => "調査書(進学用)の出欠の記録参照",
                            "extrahtml" => "onclick=\"loadwindow('../../X/KNJXATTEND_ENTREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:230px;\""));
        $arg["TYOSASYO_SANSYO"] = $objForm->ge("TYOSASYO_SANSYO");

        //成績参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "SEISEKI_SANSYO",
                            "value"     => "成績参照",
                            "extrahtml" => "onclick=\"return btn_submit('subform4');\" style=\"width:70px\"" ) );

        $arg["SEISEKI_SANSYO"] = $objForm->ge("SEISEKI_SANSYO");

        $arg["IFRAME"] = VIEW::setIframeJs();
        //ボタン
        //通知票所見参照ボタンを非表示とする。(近大のみ)
        $cnt = knja120cQuery::getKindaiJudgment($model);
        if ($cnt > 0) {
        } else {
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_popup",
                                "value"       => "通知表所見参照",
                                "extrahtml"   => "onclick=\"return btn_submit('subform1');\"" ));
            $arg["button"]["btn_popup"] = $objForm->ge("btn_popup");
        }

        $objForm->ae( array("type"        => "button",
                            "name"        => "SIKAKU_SANSYO",
                            "value"       => "資格参照",
                            "extrahtml"   => "onclick=\"return btn_submit('subform5');\"" ));
        $arg["button"]["SIKAKU_SANSYO"] = $objForm->ge("SIKAKU_SANSYO");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_club",
                            "value"       => "部活動参照",
                            "extrahtml"   => "onclick=\"return btn_submit('subform2');\"" ));
        $arg["button"]["btn_club"] = $objForm->ge("btn_club");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_committee",
                            "value"       => "委員会参照",
                            "extrahtml"   => "onclick=\"return btn_submit('subform3');\"" ));
        $arg["button"]["btn_committee"] = $objForm->ge("btn_committee");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //CSV処理
        if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $fieldActSize = "TOTALSTUDYACT=528,";
            $fieldValSize = "TOTALSTUDYVAL=528,";
            $gyouActSize = "TOTALSTUDYACT=8,";
            $gyouValSize = "TOTALSTUDYVAL=8,";
        } else {   
            $fieldActSize = "TOTALSTUDYACT=132,";
            $fieldValSize = "TOTALSTUDYVAL=198,";
            $gyouActSize  = "TOTALSTUDYACT=4,";
            $gyouValSize  = "TOTALSTUDYVAL=6,";
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            $fieldActSize = "TOTALSTUDYACT=".($model->totalstudyact_moji * 3 * $model->totalstudyact_gyou) .",";
            $gyouActSize  = "TOTALSTUDYACT=$model->totalstudyact_gyou,";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            $fieldValSize = "TOTALSTUDYVAL=".($model->totalstudyval_moji * 3 * $model->totalstudyval_gyou) .",";
            $gyouValSize  = "TOTALSTUDYVAL=$model->totalstudyval_gyou,";
        }
        $fieldSize = $fieldActSize.$fieldValSize;
        $gyouSize = $gyouActSize.$gyouValSize;
        if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            $fieldSize .= "SPECIALACTREMARK=660,";
            $gyouSize  .= "SPECIALACTREMARK=10,";
        } else {
            $fieldSize .= "SPECIALACTREMARK=198,";
            $gyouSize  .= "SPECIALACTREMARK=6,";
        }

        if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            $fieldSize .= "TOTALREMARK=1584,";
            $gyouSize  .= "TOTALREMARK=8,";
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $fieldSize .= "TOTALREMARK=1386,";
            $gyouSize  .= "TOTALREMARK=7,";
        } else {
            $fieldSize .= "TOTALREMARK=792,";
            $gyouSize  .= "TOTALREMARK=6,";
        }

        if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $fieldSize .= "ATTENDREC_REMARK=240,";
            $gyouSize  .= "ATTENDREC_REMARK=2,";
        } else {
            $fieldSize .= "ATTENDREC_REMARK=120,";
            $gyouSize  .= "ATTENDREC_REMARK=2,";
        }

        $fieldSize .= "VIEWREMARK=0,";
        $gyouSize  .= "VIEWREMARK=0,";
        $fieldSize .= "BEHAVEREC_REMARK=0";
        $gyouSize  .= "BEHAVEREC_REMARK=0,";

        //セキュリティーチェック
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knja120CQuery::getSecurityHigh());
        Query::dbCheckIn($db);
        $csvSetName = "ＣＳＶ";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_check1",
                                "value"     => $csvSetName."出力",
                                "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX180/knjx180index.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&SEND_PRGID=KNJA120C&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));

            $arg["button"]["btn_check1"] = $objForm->ge("btn_check1");
        }

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja120cForm1.html", $arg);
    }
}
?>
