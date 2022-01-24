<?php

require_once('for_php7.php');

class knjp913Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjp913index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目項目マスタ情報取得
        $model->levyLMarray = array();
        $query = knjp913Query::getLevyLMdat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->levyLMdat[$row["LM_CD"]] = $row;
        }

        //学期コンボ作成
        $query = knjp913Query::getSemesterList($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ作成
        $query = knjp913Query::getGrade($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, 1);

        //年組コンボ作成
        $query = knjp913Query::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1, 1);

        //初期化
        $model->data = array();
        $counter = 0;

        //処理区分（1:繰越キャンセル、2:キャンセル取消）
        $opt = array();
        $opt[] = array('label' => '繰越キャンセル', 'value' => '1');
        $opt[] = array('label' => 'キャンセル取消', 'value' => '2');
        $extra = "";
        $arg["SYORI_DIV"] = knjCreateCombo($objForm, "SYORI_DIV", $model->field["SYORI_DIV"], $opt, $extra, 1);

        //全てcheckbox
        $extra = "id=\"CHECK_ALL\" onClick=\"chkAll(this);\"";
        $arg["CHECK_ALL"] = knjCreateCheckBox($objForm, "CHECK_ALL", "1", $extra);

        $disableFlg = " disabled";
        //繰越項目セット
        $model->carryArr = array();
        $query = knjp913Query::getCarryOverDat($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->carryArr[$row["LM_CD"]] = $row["LEVY_M_ABBV"];
            $arg["carry"][] = $row;
            $disableFlg = "";
        }

        //繰越データセット
        $model->carrySchArr = array();
        $query = knjp913Query::getCarryOverDatSch($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->carrySchArr[$row["SCHREGNO"]][$row["LM_CD"]] = $row["CARRY_OVER_MONEY"];
        }

        //一覧表示
        $schregNos = "";
        $schSep = "";
        $colorFlg = false;
        $result = $db->query(knjp913Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];
            $model->data["ATTENDNO"][$row["SCHREGNO"]] = $row["ATTENDNO"];

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //更新対象checkbox
            $setName = "CHECK_".$row["SCHREGNO"];
            $extra = "id=\"{$setName}\"";
            $row["CHECK"] = knjCreateCheckBox($objForm, $setName, "1", $extra);

            //繰越金額
            $miniData = array();
            foreach ($model->carryArr as $lmCd => $mName) {
                $value = $model->carrySchArr[$row["SCHREGNO"]][$lmCd];
                $setName  = "CARRY_OVER_MONEY-".$lmCd.'-'.$row["SCHREGNO"];
                //hidden
                knjCreateHidden($objForm, $setName, $value);
                $miniData["CARRY_OVER_MONEY"] = number_format($value);
                $row["carry"][] = $miniData;
            }

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $schregNos .= $schSep.$row["SCHREGNO"];
            $schSep = ",";

            $counter++;
            $arg["data"][] = $row;
        }

        //テキストの名前を取得
        knjCreateHidden($objForm, "TEXT_FIELD_NAME", "CARRY_OVER_MONEY");
        knjCreateHidden($objForm, "SCHREGNOS", $schregNos);

        //ボタン作成
        //更新ボタンを作成する
        $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('update');\"".$disableFlg;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "実 行", $extra.$disabled);
        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp913Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
