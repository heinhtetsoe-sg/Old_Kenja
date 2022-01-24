<?php
class knja124ooForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knja124ooindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //印影登録有無
        $query = knja124ooQuery::getInkanUmu();
        $inkanCnt = $db->getOne($query);
        if ($inkanCnt == 0) {
            $arg["jscript"] = "notStampClose()";
        }

        //年度
        $arg["TOP"]["YEAR"] = $model->left_year;

        //学期
        $arg["TOP"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //年組コンボ
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knja124ooQuery::getHrClass($model);
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->hr_class, $extra, 1, "BLANK");

        //全員チェックボックス
        $extra  = "onclick=\"chkAll(this);\"";
        $arg["TOP"]["CHK_ALL"] = knjCreateCheckBox($objForm, "CHK_ALL", "on", $extra, "");

        //生徒リスト
        $studentArray = array();
        $query = knja124ooQuery::getStudents($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $studentArray[] = $row;
        }

        $count = 0; //署名ボタン使用有無

        $model->hash = array();
        foreach ($studentArray as $key => $studentRow) {
            $isUnmatched = setData($objForm, $arg, $db, $model, $studentRow);
            if (!$isUnmatched) $count++;
        }

        //校長
        if ($model->exe_type == "PRINCIPAL") {
            //APPLET起動
            $arg["useApplet"] = "ON";
            //検証以外は、パラメータをセット
            if ($model->cmd != "sslExe") {
                $schregnoKnm = $hashKnm = $seq = "";
                if (isset($model->chk_data)) {
                    foreach ($model->chk_data as $key => $schregno) {
                        $schregnoKnm .= $seq . $schregno;
                        $hashKnm .= $seq . $model->hash[$schregno];
                        $seq = ",";
                    }
                }
                $arg["APP"]["PASS"]  = $model->cmd == "sslApplet" ? $model->passwd : "";
                $arg["APP"]["STAFF"] = STAFFCD;
                $arg["APP"]["SCHREGNO"] = $schregnoKnm;
                $arg["APP"]["RANDM"] = $hashKnm;
                $arg["APP"]["EXEDIV"] = "ALL";
                $arg["APP"]["APPHOST"] = '"../../..'.APPLET_ROOT.'_OLD/KNJA122S"';
            }
        }

        //認証中文言
        if ($model->cmd == "sslApplet") {
            $arg["marpColor"] = "red";
        } else {
            $arg["marpColor"] = "white";
        }



        //ボタン作成
        makeBtn($objForm, $arg, $count, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja124ooForm1.html", $arg);
    }
}

//署名時のデータと不一致か
function setData(&$objForm, &$arg, $db, $model, $studentRow) {
    $schregno = $studentRow["SCHREGNO"];

    //署名時のデータと不一致か
    $isUnmatched = false;

    //HTRAINREMARK_DAT 取得
    $query = knja124ooQuery::getTrainRow($model, $schregno);
    $tRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

    //学習記録データ
    $query = knja124ooQuery::getStudyRec($model, $schregno);
    $result = $db->query($query);
    $study = "";
    while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
        } else {
            $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
        }
    }

    //出欠記録データ
    $attend = $db->getRow(knja124ooQuery::getAttendRec($model, $schregno), DB_FETCHMODE_ASSOC);

    //表示データのHASHを取得
    $model->hash[$schregno] = $model->makeHash($tRow, $study, $attend);

    //署名データ取出し
    $query = knja124ooQuery::getOpinionsSignature($model, $schregno);
    $opinionsSignature = $db->getRow($query, DB_FETCHMODE_ASSOC);

    //印影の表示
    $query = knja124ooQuery::getIneiFlg($model, $schregno);
    $ineiFlg = $db->getRow($query, DB_FETCHMODE_ASSOC); //印影表示フラグ(SEQ)
    if ($ineiFlg["CHAGE_OPI_SEQ"]) {
        $studentRow["IMAGE1"] = "レ";
        if ($opinionsSignature["CHAGE_OPI"] != $model->hash[$schregno]) {
            $isUnmatched = true;
            $studentRow["MSG"] = "レ"; //署名時のデータと不一致です。
        }
    }
    if ($ineiFlg["LAST_OPI_SEQ"]) {
        $studentRow["IMAGE2"] = "レ";
        if ($opinionsSignature["LAST_OPI"] != $model->hash[$schregno]) {
            $isUnmatched = true;
            $studentRow["MSG"] = "レ"; //署名時のデータと不一致です。
        }
    }

    //署名済み
    if ($model->exe_type == "CHARGE") {
        //担任
        if ($ineiFlg["CHAGE_OPI_SEQ"]) {
            $isUnmatched = true;
        }
    } else if ($model->exe_type == "PRINCIPAL") {
        if ($ineiFlg["CHAGE_OPI_SEQ"]) {
            //校長
            if ($ineiFlg["LAST_OPI_SEQ"]) {
                $isUnmatched = true;
            }
        } else {
            $isUnmatched = true;
        }
    }

    //チェックボックス
    $extraDis = ($isUnmatched) ? " disabled " : "";
    $extra = (isset($model->chk_data) && in_array($schregno, $model->chk_data)) ? " checked " : "";
    $studentRow["CHK_DATA"] = knjCreateCheckBox($objForm, "CHK_DATA", $schregno, $extraDis.$extra, 1);

    $arg["data"][] = $studentRow;

    return $isUnmatched;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $count, $model)
{
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"deleteCookie(); closeWin();\"");

    //署名
    $extraDis = (0 < $count) ? "" : " disabled ";
    if ($model->exe_type == "CHARGE") {
        //担当
        $extra = "onclick=\"return btn_submit('shomei');\"";
        $arg["button"]["btn_shomei"] = knjCreateBtn($objForm, "btn_shomei", "担任署名", $extraDis.$extra);
    } else if ($model->exe_type == "PRINCIPAL") {
        $extra = "onclick=\"return btn_submit('sslApplet');\"";
        $arg["button"]["btn_shomei"] = knjCreateBtn($objForm, "btn_shomei", "校長署名", $extraDis.$extra);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO");
    knjCreateHidden($objForm, "SIGNATURE");
    knjCreateHidden($objForm, "GOSIGN");
    knjCreateHidden($objForm, "RNDM", $model->rndm);
}
?>
