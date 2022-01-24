<?php

require_once('for_php7.php');

class knja123sForm2 {
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
        if ($ineiFlg["CHAGE_OPI_SEQ"]) {
            if ($opinionsSignature["CHAGE_OPI"] != $model->hash) {
                $arg["jscript"] = "alert('署名時のデータと不一致です。')";
            }
        }
        if ($ineiFlg["LAST_OPI_SEQ"]) {
            if ($opinionsSignature["LAST_OPI"] != $model->hash) {
                $arg["jscript"] = "alert('署名時のデータと不一致です。')";
            }
        }

        //キャンセル事由履歴表示
        $result = $db->query(knja123sQuery::getCancelRemarkList($model)); 
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["CANCEL_DATE"] = str_replace("-", "/", $row["CANCEL_DATE"]);
            $arg["data"][] = $row;
        }
        $result->free();

        //キャンセル事由
        $extra = "style=\"height:50px;\"";
        $arg["CANCEL_REASON"] = knjCreateTextArea($objForm, "CANCEL_REASON", 2, 40, "soft", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg, $ineiFlg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja123sForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $ineiFlg, $model)
{
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"deleteCookie(); closeWin();\"");

    //署名キャンセル
    $extraDis = "";
    if ($model->exe_type == "PRINCIPAL") {
        if (!$ineiFlg["CHAGE_OPI_SEQ"] || !$ineiFlg["LAST_OPI_SEQ"]) {
            $extraDis = "disabled ";
        }
        $extra = "onclick=\"return btn_submit('sslApplet');\"";
        $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "署名キャンセル", $extraDis.$extra);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    //2010.02.09
    knjCreateHidden($objForm, "SIGNATURE", $model->signature);
    knjCreateHidden($objForm, "GOSIGN", $model->gosign);
    knjCreateHidden($objForm, "RNDM", $model->rndm);
}
?>
