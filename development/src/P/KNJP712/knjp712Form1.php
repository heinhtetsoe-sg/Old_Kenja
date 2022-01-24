<?php

require_once('for_php7.php');

class knjp712Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjp712index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //ラジオボタン  1:新入生 2:在校生
        $opt = array(1, 2);
        if (!$model->schdiv) $model->schdiv = 2;
        $extra = array("id=\"SCHDIV1\" onclick =\" return btn_submit('change');\"", "id=\"SCHDIV2\" onclick =\" return btn_submit('change');\"");
        $radioArray = knjCreateRadio($objForm, "SCHDIV", $model->schdiv, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //年度表示
        $arg["YEAR"] = $model->year;

        //校種コンボ
        $query = knjp712Query::getSchKind($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolkind, $extra, 1);

        //入金グループコンボ
        $query = knjp712Query::getCollectGrpMst($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "COLLECT_GRP_CD", $model->collect_grp_cd, $extra, 1);

        //学年・課程・学科コンボ
        $query = knjp712Query::getGradeCourseMajor($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_CM", $model->grade_cm, $extra, 1, "blank");

        //コースコードコンボ
        $query = knjp712Query::getCourseCode($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->coursecode, $extra, 1, "blank");

        //年組コンボ
        $query = knjp712Query::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->grade_hr_class, $extra, 1, "blank");

        //クラブコンボ
        $query = knjp712Query::getClub($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "CLUBCD", $model->clubcd, $extra, 1, "blank");
        $arg["showClub"] = ($model->schdiv == "1") ? "" : "1";

        //寮コンボ
        //テーブルの有無チェック
        $query = knjp712Query::checkTableExist("DOMITORY_YDAT");
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0) {
            $query = knjp712Query::getDomitory($model, 'cnt');
            $domiCnt = $db->getOne($query);
            if ($domiCnt > 0) {
                $arg["showDomi"] = ($model->schdiv == "1") ? "" : "1";
                $query = knjp712Query::getDomitory($model);
                $extra = "onchange=\"return btn_submit('change');\"";
                makeCmb($objForm, $arg, $db, $query, "DOMI_CD", $model->domi_cd, $extra, 1, "blank");
            }
        }

        //全チェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //入金済み or キャンセル 伝票番号取得
        $paidSlipArray = $db->getCol(knjp712Query::getPaidSlipNo($model));

        //一覧取得
        $schCount = 0;
        if (strlen($model->grade_cm) || strlen($model->coursecode) || strlen($model->grade_hr_class) || strlen($model->clubcd) || strlen($model->domi_cd)) {
            $query = knjp712Query::getList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //レコードを連想配列のまま配列$arg[data]に追加していく。
                array_walk($row, "htmlspecialchars_array");

                if (in_array($row["SLIP_NO"], $paidSlipArray)) {
                    //入金済み伝票
                    $row["CHECKED"] = "●";
                } else {
                    //削除可能伝票
                    $array = (get_count($model->checked) > 0) ? $model->checked : array();
                    $extra = (in_array($row["SLIP_NO"], $array) && isset($model->warning)) ? "checked" : "";
                    $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["SLIP_NO"], $extra, "1");
                }

                $arg["data"][] = $row;
                $schCount++;
            }
            $result->free();
        }

        //人数セット
        $arg["SCHREG_COUNT"] = $schCount.'人';

        //削除ボタン
        $extra  = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";
        $extra .= " onClick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "change") {
            $arg["reload"] = "window.open('knjp712index.php?cmd=edit&SCHDIV={$model->schdiv}&SCHOOL_KIND={$model->schoolkind}&COLLECT_GRP_CD={$model->collect_grp_cd}&GRADE_CM={$model->grade_cm}&COURSECODE={$model->coursecode}&GRADE_HR_CLASS={$model->grade_hr_class}&CLUBCD={$model->clubcd}&DOMI_CD={$model->domi_cd}','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp712Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
