<?php

require_once('for_php7.php');

class knja123sForm1 {
    function main(&$model) {

        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knja123sindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //HTRAINREMARK_DAT 取得
        $query = knja123sQuery::getTrainRow($model);
        $tRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //学習記録データ
        $query = knja123sQuery::getStudyRec($model);
        $result = $db->query($query);
        $study = "";
        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                          $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
            } else {
                $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                          $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
            }
        }

        //出欠記録データ
        $attend = $db->getRow(knja123sQuery::getAttendRec($model), DB_FETCHMODE_ASSOC);

        //表示データのHASHを取得
        $model->hash = $model->makeHash($tRow, $study, $attend);

        //校長
        if ($model->exe_type == "PRINCIPAL") {
            //APPLET起動
            $arg["useApplet"] = "ON";
            //検証以外は、パラメータをセット
            if ($model->cmd != "sslExe") {
                $arg["APP"]["PASS"]  = $model->cmd == "sslApplet" ? $model->passwd : "";
                $arg["APP"]["STAFF"] = STAFFCD;
                $arg["APP"]["RANDM"] = $model->hash;
                $arg["APP"]["EXEDIV"] = "VIEWS";
                $arg["APP"]["APPHOST"] = '"../../..'.APPLET_ROOT.'/KNJA122S"';
            }
        }
        //認証中文言
        if ($model->cmd == "sslApplet") {
            $arg["marpColor"] = "red";
        } else {
            $arg["marpColor"] = "white";
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //署名データ取出し
        $query = knja123sQuery::getOpinionsSignature($model);
        $opinionsSignature = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //印影登録有無
        $query = knja123sQuery::getInkanUmu();
        $inkanCnt = $db->getOne($query);
        if ($inkanCnt == 0) {
            $arg["jscript"] = "notStampClose()";
        }

        //印影の表示
        $query = knja123sQuery::getIneiFlg($model);
        $ineiFlg = $db->getRow($query, DB_FETCHMODE_ASSOC); //印影表示フラグ(SEQ)
        $ineiUrl = REQUESTROOT."/image/stamp/";
        if ($ineiFlg["CHAGE_OPI_SEQ"]) {
            $arg["IMAGE1"] = "<image src=\"".$ineiUrl.$ineiFlg["CHAGE_STAMP"].".bmp\" width=\"60\">";
            if ($opinionsSignature["CHAGE_OPI"] != $model->hash) {
                $arg["jscript"] = "alert('署名時のデータと不一致です。')";
            }
        }
        if ($ineiFlg["LAST_OPI_SEQ"]) {
            $arg["IMAGE2"] = "<image src=\"".$ineiUrl.$ineiFlg["LAST_STAMP"].".bmp\" width=\"60\">";
            if ($opinionsSignature["LAST_OPI"] != $model->hash) {
                $arg["jscript"] = "alert('署名時のデータと不一致です。')";
            }
        }

        if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //活動内容
            $extra = "readonly=\"readonly\" style=\"height:123px;\"";
            $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 8, 45, "soft", $extra, $tRow["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角22文字X8行まで)';

            //評価
            $extra = "readonly=\"readonly\" style=\"height:123px;\"";
            $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, 45, "soft", $extra, $tRow["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角22文字X8行まで)';

            //出欠の記録備考
            $extra = "readonly=\"readonly\" style=\"height:35px;\"";
            $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 81, "soft", $extra, $tRow["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角40文字X2行まで)';
        } else {
            //活動内容
            $extra = "readonly=\"readonly\" style=\"height:63px;\"";
            $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 5, 23, "soft", $extra, $tRow["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角11文字X4行まで)';

            //評価
            $extra = "readonly=\"readonly\" style=\"height:90px;\"";
            $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 6, 23, "soft", $extra, $tRow["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角11文字X6行まで)';

            //出欠の記録備考
            $extra = "readonly=\"readonly\" style=\"height:35px;\"";
            $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 41, "soft", $extra, $tRow["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角20文字X2行まで)';
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            //活動内容
            $height = (int)$model->totalstudyact_gyou * 13.5 + ((int)$model->totalstudyact_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ((int)$model->totalstudyact_moji * 2 + 1), "soft", $extra, $tRow["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            //評価
            $height = (int)$model->totalstudyval_gyou * 13.5 + ((int)$model->totalstudyval_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ((int)$model->totalstudyval_moji * 2 + 1), "soft", $extra, $tRow["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        }

        if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            //特別活動所見
            $extra = "readonly=\"readonly\" style=\"height:145px;\"";
            $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 45, "soft", $extra, $tRow["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角22文字X10行まで)';
        } else {
            //特別活動所見
            $extra = "readonly=\"readonly\" style=\"height:90px;\"";
            $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $tRow["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角11文字X6行まで)';
        }

        if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            //総合所見
            $extra = "readonly=\"readonly\" style=\"height:123px;\"";
            $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 8, 133, "soft", $extra, $tRow["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X8行まで)';
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //総合所見
            $extra = "readonly=\"readonly\" style=\"height:105px;\"";
            $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 7, 133, "soft", $extra, $tRow["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X7行まで)';
        } else {
            //総合所見
            $extra = "readonly=\"readonly\" style=\"height:90px;\"";
            $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 6, 89, "soft", $extra, $tRow["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角44文字X6行まで)';
        }

        //ボタン作成
        makeBtn($objForm, $arg, $ineiFlg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja123sForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $ineiFlg, $model)
{
    //前の生徒へボタン
    $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('pre');\"";
    $arg["button"]["btn_up_pre"]   = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);

    //次の生徒へボタン
    $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('next');\"";
    $arg["button"]["btn_up_next"]  = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);

    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"deleteCookie(); closeWin();\"");

    //署名キャンセル
    $extraDis = "";
    if ($model->exe_type == "PRINCIPAL") {
        if (!$ineiFlg["CHAGE_OPI_SEQ"] || !$ineiFlg["LAST_OPI_SEQ"]) {
            $extraDis = "disabled ";
        }
        $extra = "onclick=\"loadwindow('knja123sindex.php?cmd=form2', 0, 0, 460, 400)\"";
        $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "署名キャンセル", $extraDis.$extra);
    }

}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CANCEL_REASON", $model->cancel_reason);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    //2010.02.09
    knjCreateHidden($objForm, "SIGNATURE", $model->signature);
    knjCreateHidden($objForm, "GOSIGN", $model->gosign);
    knjCreateHidden($objForm, "RNDM", $model->rndm);
}
?>
