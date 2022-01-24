<?php

require_once('for_php7.php');

class knjh213Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh213Form1", "POST", "knjh213index.php", "", "knjh213Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["valWindowHeight"]  = (int)$model->windowHeight - 200;
        $resizeFlg = $model->cmd == "cmdStart" ? true : false;

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjh213Query::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('knjh213')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "");
        }

        //クラスコンボボックスを作成する
        $query = knjh213Query::getHrClass($model);
        $extra = "onChange=\"return btn_submit('knjh213');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, "1", "BLANK");

        //部活動コンボボックスを作成する
        if ($model->Properties["useClubCombo"] == "1") {
            $arg['useClubCombo'] = '1';
            $query = knjh213Query::getClub($model);
            $extra = "onChange=\"return btn_submit('knjh213');\"";
            makeCmb($objForm, $arg, $db, $query, "CLUB", $model->field["CLUB"], $extra, "1", "BLANK");
        }

        //初期化
        if (!isset($model->warning)) {
            $model->data = array();
        }

        //一覧表示
        $arr_sch_list     = array();
        $model->domiCdArr = array();
        $model->setData   = array();
        $dataflg = false;
        $query = knjh213Query::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->setData[$row["SCHREGNO"]]["HR_NAME"] = $row["HR_NAME"];
            $model->setData[$row["SCHREGNO"]]["NAME"] = $row["NAME"];

            list($entY, $entM, $entD) = explode("-", $row["DOMI_ENTDAY"]);
            list($outY, $outM, $outD) = explode("-", $row["DOMI_OUTDAY"]);

            $entM = $entM < "04" ? (int)$entM + 12 : $entM;
            if ($outM) {
                $outM = $outM < "04" ? (int)$outM + 12 : $outM;
            } else {
                $outM = "15";
            }
            for ($mCnt = $entM; $mCnt <= $outM; $mCnt++) {
                $getM = $mCnt < "13" ? $mCnt : (int)$mCnt - 12;
                $setMcnt = sprintf("%02d", $getM);
                $model->setData[$row["SCHREGNO"]][$setMcnt]["DOMI_CD"] = $row["DOMI_CD"];
                $model->setData[$row["SCHREGNO"]][$setMcnt]["FLG"]     = $row["FLG{$setMcnt}"];
            }
        }

        //データセット
        foreach ($model->setData as $schNo => $dataArr) {
            //HIDDENに保持する用
            $arr_sch_list[] = $schNo;

            //生徒チェックボックス
            $setIdName = "CHK_SCHREG_".$schNo;
            $extra = "id=\"{$setIdName}\" class=\"changeColor\" data-name=\"{$setIdName}\"";
            $parts["CHK_SCHREG"] = knjCreateCheckBox($objForm, $setIdName, "1", $extra);
            $parts["CHK_SCHREG_NAME"] = $setIdName;

            //年組
            $parts['HR_NAME'] = $dataArr['HR_NAME'];

            //名前
            $parts['NAME'] = $dataArr['NAME'];

            //各月チェックボックス
            foreach ($model->monthArray as $key => $month) {
                $chkDis   = ($dataArr[$month]) ? '': ' disabled';
                $value = ($model->isWarning()) ? $model->data["FLG".$month][$schNo] : $dataArr[$month]["FLG"];
                $extra  = ($value == "1") ? "checked" : "";
                $setIdName = "FLG{$month}:{$schNo}";
                $extra .= " id=\"{$setIdName}\" class=\"changeColor\" data-name=\"{$setIdName}\"".$chkDis;
                $parts["FLG".$month] = knjCreateCheckBox($objForm, $setIdName, "1", $extra, "");
                $parts["FLG".$month."_NAME"] = $setIdName;
            }

            $dataflg = true;
            $arg["data"][] = $parts;
        }

        //ALLチェックボックス
        $extra = "id=\"CHK_ALL\" onClick=\"check_all(this);\"";
        $arg["CHK_ALL"] = knjCreateCheckBox($objForm, "CHK_ALL", "1", $extra);

        //ボタン作成
        $disable  = ($dataflg) ? "" : " disabled";
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"".$disable;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCH_LIST_JS", implode(",",$arr_sch_list));
        knjCreateHidden($objForm, "SCH_LIST");

        //DB切断
        Query::dbCheckIn($db);

        //Windowサイズ
        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh213Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    if ($query) {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
    }
    if ($name == 'SCHOOL_KIND') {
        $value = ($value != "" && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
