<?php

require_once('for_php7.php');

class knje020oForm1
{
    function main(&$model)
    {
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje020oindex.php", "", "edit");

        //名前表示
        $arg["name"] = "出席番号　".$model->attendno."　氏名　".$model->name;

        $db = Query::dbCheckOut();

        //調査所見データ取得
        if($model->warning ==""){
            $query = knje020oQuery::getReportRemark_dat($model);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        }else{
            $row =& $model->field;
        }

        Query::dbCheckIn($db);

        //就職用特別活動記録
        $objForm->ae( array("type"      =>  "textarea",
                            "name"      =>  "jobhunt_rec",
                            "rows"      =>  "8",
                            "cols"      =>  "40",
                            "extrahtml" =>  "",
                            "wrap"      => "soft",
                            "value"     =>  $row["JOBHUNT_REC"]));

        $arg["data"]["JOBHUNT_REC"] = $objForm->ge("jobhunt_rec");

        //就職用欠席理由
        $objForm->ae( array("type"      =>  "textarea",
                            "name"      =>  "jobhunt_absence",
                            "rows"      =>  "4",
                            "cols"      =>  "20",
                            "extrahtml" =>  "",
                            "wrap"      => "soft",
                            "value"     =>  $row["JOBHUNT_ABSENCE"]));

        $arg["data"]["JOBHUNT_ABSENCE"] = $objForm->ge("jobhunt_absence");

        //出欠備考参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "SANSYO",
                            "value"     => "出欠備考参照",
                            "extrahtml" => "onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,420,300);return;\""));
        $arg["SANSYO"] = $objForm->ge("SANSYO");

        //要録の出欠備考参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "YOROKU_SANSYO",
                            "value"     => "要録の出欠の記録備考参照",
                            "extrahtml" => "onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:210px;\""));
        $arg["YOROKU_SANSYO"] = $objForm->ge("YOROKU_SANSYO");

        //就職用身体状況備考
        $objForm->ae( array("type"      =>  "textarea",
                            "name"      =>  "jobhunt_healthremark",
                            "rows"      =>  "3",
                            "cols"      =>  "28",
                            "extrahtml" =>  "",
                            "wrap"      => "soft",
                            "value"     =>  $row["JOBHUNT_HEALTHREMARK"]));

        $arg["data"]["JOBHUNT_HEALTHREMARK"] = $objForm->ge("jobhunt_healthremark");

        //異常なしチェックボックス
        $objForm->ae( array("type"      =>  "checkbox",
                            "name"      =>  "CHECK",
                            "value"     =>  "1",
                            "extrahtml" =>  "onclick=\"return CheckHealthRemark();\""));

        $arg["data"]["CHECK"] = $objForm->ge("CHECK")."異常なし";

        //就職用推薦事由
        $objForm->ae( array("type"      =>  "textarea",
                            "name"      =>  "jobhunt_recommend",
                            "rows"      =>  "10",
                            "cols"      =>  "76",
                            "extrahtml" =>  "",
                            "wrap"      => "soft",
                            "value"     =>  $row["JOBHUNT_RECOMMEND"]));

        $arg["data"]["JOBHUNT_RECOMMEND"] = $objForm->ge("jobhunt_recommend");

        //健康診断詳細データ取得

        $medexam_row = knje020oQuery::getMedexam_det_dat($model);

        $arg["data"]["HEIGHT"] = $medexam_row["HEIGHT"]. " cm";
        $arg["data"]["WEIGHT"] = $medexam_row["WEIGHT"]. " kg";
        if ($medexam_row["R_VISION_MARK"]) {
            $arg["data"]["R_VISION_MARK"] = $medexam_row["R_BAREVISION_MARK"]."　( ".$medexam_row["R_VISION_MARK"]." )";
        } else {
            $arg["data"]["R_VISION_MARK"] = $medexam_row["R_BAREVISION_MARK"]."　(　　)";
        }
        if ($medexam_row["L_VISION_MARK"]) {
            $arg["data"]["L_VISION_MARK"] = $medexam_row["L_BAREVISION_MARK"]."　( ".$medexam_row["L_VISION_MARK"]." )";
        } else {
            $arg["data"]["L_VISION_MARK"] = $medexam_row["L_BAREVISION_MARK"]."　(　　)";
        }
        $arg["data"]["R_EAR_NAME"] = $medexam_row["R_EAR_NAME"];
        $arg["data"]["L_EAR_NAME"] = $medexam_row["L_EAR_NAME"];

        if (preg_match("/([0-9]{4})-([0-9]{2})-([0-9]{2})/",$medexam_row["DATE"],$date_std)) {
            $arg["data"]["DATE"] = $date_std[1]."年".$date_std[2]."月".$date_std[3]."日";
        }

        $arg["data"]["GRADE1"] = "　".$attendrec_show[1];
        $arg["data"]["GRADE2"] = "　".$attendrec_show[2];
        $arg["data"]["GRADE3"] = "　".$attendrec_show[3];

        //出欠の記録修正(KNJXATTEND出欠記録修正画面)
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_attend",
                            "value"     => "出欠の記録修正",
                            "extrahtml" => "onclick=\"return btn_submit('modify');\"" ) );

        $arg["button"]["btn_attend"] = $objForm->ge("btn_attend");

        //成績参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_popup",
                            "value"     => "成績参照",
                            "extrahtml" => "onclick=\"return btn_submit('subform1');\" style=\"width:70px\"" ) );

        $arg["button"]["reference"] = $objForm->ge("btn_popup");
        $arg["IFRAME"] = VIEW::setIframeJs();

        //更新ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_update",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return btn_submit('update');\""));

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]   = View::updateNext($model, $objForm, 'btn_update');

        //取消ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('reset');\""));

        $arg["button"]["btn_reset"]	= $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"return closeWin();\""));

        $arg["button"]["btn_end"]	=	$objForm->ge("btn_end");


        //セキュリティーチェック
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knje020oQuery::getSecurityHigh());
        Query::dbCheckIn($db);
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_csv1",
                                "value"     => "データ".$csvSetName,
                                "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX192O/knjx192oindex.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE020O&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));
            $arg["button"]["btn_csv1"] = $objForm->ge("btn_csv1");
        }


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "updated",
                            "value"     => $row["UPDATED"]));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "mode",
                            "value"     => $model->mode
                            ));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRD_YEAR",
                            "value"     => $model->grd_year
                            ));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRD_SEMESTER",
                            "value"     => $model->grd_semester
                            ));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PROGRAMID",
                            "value"     => PROGRAMID
                            ));

        if (get_count($model->warning)== 0 && $model->cmd !="reset"){
            $arg["next"] = "NextStudent(0);";
        } else if ($model->cmd =="reset"){
            $arg["next"] = "NextStudent(1);";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje020oForm1.html", $arg);
    }
}
?>
