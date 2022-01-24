<?php

require_once('for_php7.php');

class knja120oForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knja120oindex.php", "", "edit");
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = knja120oQuery::getTrainRow($model->schregno);
            $row2 = knja120oQuery::getTrainHRow($model->schregno);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
            $row2 =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //記入欄
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYACT",
                            "cols"        => 89,
                            "rows"        => 5,
                            "wrap"        => "soft",
                            "value"       => $row2["TOTALSTUDYACT"] ));
        $arg["data"]["TOTALSTUDYACT"] = $objForm->ge("TOTALSTUDYACT");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYVAL",
                            "cols"        => 89,
                            "rows"        => 6,
                            "wrap"        => "soft",
                            "extrahtml"   => "style=\"height:90px;\"",
                            "value"       => $row2["TOTALSTUDYVAL"] ));
        $arg["data"]["TOTALSTUDYVAL"] = $objForm->ge("TOTALSTUDYVAL");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "SPECIALACTREMARK",
                            "cols"        => 23,
                            "rows"        => 6,
                            "wrap"        => "soft",
                            "extrahtml"   => "style=\"height:90px;\"",
                            "value"       => $row["SPECIALACTREMARK"] ));
        $arg["data"]["SPECIALACTREMARK"] = $objForm->ge("SPECIALACTREMARK");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALREMARK",
                            "cols"        => 89,
                            "rows"        => 6,
                            "wrap"        => "soft",
                            "extrahtml"   => "style=\"height:90px;\"",
                            "value"       => $row["TOTALREMARK"] ));
        $arg["data"]["TOTALREMARK"] = $objForm->ge("TOTALREMARK");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "ATTENDREC_REMARK",
                            "cols"        => 41,
                            "rows"        => 3,
                            "wrap"        => "soft",
                            "extrahtml"   => "style=\"float:left;\"",
                            "value"       => $row["ATTENDREC_REMARK"] ));
        $arg["data"]["ATTENDREC_REMARK"] = $objForm->ge("ATTENDREC_REMARK");

        //出欠備考参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "SANSYO",
                            "value"     => "出欠備考参照",
                            "extrahtml" => "onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,420,300);return;\""));
        $arg["SANSYO"] = $objForm->ge("SANSYO");

        //要録の出欠備考参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "TYOSASYO_SANSYO",
                            "value"     => "調査書(進学用)の出欠の記録参照",
                            "extrahtml" => "onclick=\"loadwindow('../../X/KNJXATTEND_ENTREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:230px;\""));
        $arg["TYOSASYO_SANSYO"] = $objForm->ge("TYOSASYO_SANSYO");



        $arg["IFRAME"] = VIEW::setIframeJs();
        //ボタン
        //通知票所見参照ボタンを非表示とする。(近大のみ)
        $cnt = knja120oQuery::getKindaiJudgment();
        if ($cnt > 0) {
        } else {
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_popup",
                                "value"       => "通知表所見参照",
                                "extrahtml"   => "onclick=\"return btn_submit('subform1');\"" ));
            $arg["button"]["btn_popup"] = $objForm->ge("btn_popup");
        }

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
        $fieldSize  = "TOTALSTUDYACT=0,";
        $fieldSize .= "TOTALSTUDYVAL=0,";
        $fieldSize .= "SPECIALACTREMARK=198,";
        $fieldSize .= "TOTALREMARK=792,";
        $fieldSize .= "ATTENDREC_REMARK=120,";
        $fieldSize .= "VIEWREMARK=0,";
        $fieldSize .= "BEHAVEREC_REMARK=0";


        //セキュリティーチェック
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knja120oQuery::getSecurityHigh());
        Query::dbCheckIn($db);
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_check1",
                                "value"     => "※2 データ".$csvSetName,
                                "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX180O/knjx180oindex.php?FIELDSIZE=".$fieldSize."&SEND_PRGID=KNJA120O&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));
            $arg["button"]["btn_check1"] = $objForm->ge("btn_check1");

            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_check2",
                                "value"     => "※1 データ".$csvSetName,
                                "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX181O/knjx181oindex.php?SEND_PRGID=KNJA120O&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));
            $arg["button"]["btn_check2"] = $objForm->ge("btn_check2");
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

        View::toHTML($model, "knja120oForm1.html", $arg);
    }
}
?>
