<?php

require_once('for_php7.php');

class knja120msForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knja120msindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //HTRAINREMARK_P_DAT 取得
        $query = knja120msQuery::getTrainRow($model);
        $tRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //学習記録データ
        $query = knja120msQuery::getStudyRec($model);
        $result = $db->query($query);
        $study = "";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	            $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
	            $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	        }
        } else {
	        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	            $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
	            $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	        }
        }

        //ホームルーム出席履歴データ
        $attend = $db->getOne(knja120msQuery::getHrAttend($model));

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
        $query = knja120msQuery::getOpinionsSignature($model);
        $opinionsSignature = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //印影登録有無
        $query = knja120msQuery::getInkanUmu();
        $inkanCnt = $db->getOne($query);
        if ($inkanCnt == 0) {
            $arg["jscript"] = "notStampClose()";
        }

        //印影の表示
        $query = knja120msQuery::getIneiFlg($model);
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

        //EXTRA
        $extra = "readonly=\"readonly\"";

        //活動内容
        $arg["data"]["TOTALSTUDYACT"] = knjCreateTextBox($objForm, $tRow["TOTALSTUDYACT"], "TOTALSTUDYACT", 88, 88, $extra);

        //評価
        $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextBox($objForm, $tRow["TOTALSTUDYVAL"], "TOTALSTUDYVAL", 88, 88, $extra);

        //特別活動所見
        $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $tRow["SPECIALACTREMARK"]);

        //総合所見
        $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 6, 45, "soft", $extra, $tRow["TOTALREMARK"]);

        //評価
        $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextBox($objForm, $tRow["ATTENDREC_REMARK"], "ATTENDREC_REMARK", 80, 80, "");

        //ボタン作成
        makeBtn($objForm, $arg, $ineiFlg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja120msForm1.html", $arg);
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

    //署名
    $extraDis = "";
    if ($model->exe_type == "CHARGE") {
        //担当
        if ($ineiFlg["CHAGE_OPI_SEQ"]) {
            $extraDis = " disabled ";
        }
        $extra = "onclick=\"return btn_submit('shomei');\"";
        $arg["button"]["btn_shomei"] = knjCreateBtn($objForm, "btn_check1", "担任署名", $extraDis.$extra);
    } else if ($model->exe_type == "PRINCIPAL") {
        if ($ineiFlg["CHAGE_OPI_SEQ"]) {
            //校長
            if ($ineiFlg["LAST_OPI_SEQ"]) {
                $extraDis = "disabled ";
            }
        } else {
            $extraDis = "disabled ";
        }
        $extra = "onclick=\"return btn_submit('sslApplet');\"";
        $arg["button"]["btn_shomei"] = knjCreateBtn($objForm, "btn_check1", "校長署名", $extraDis.$extra);

        $extra = "onclick=\"return btn_submit('sasimodosi');\"";
        $arg["button"]["btn_sasimodosi"] = knjCreateBtn($objForm, "btn_sasimodosi", "差戻し", $extraDis.$extra);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SIGNATURE", $model->signature);
    knjCreateHidden($objForm, "GOSIGN", $model->gosign);
    knjCreateHidden($objForm, "RNDM", $model->rndm);
}
?>
