<?php

require_once('for_php7.php');


class knjf020SubForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjf020SubForm2", "POST", "knjf020index.php", "", "knjf020SubForm2");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["SCHREGNO"] = $model->schregno;
        $query = knjf020Query::getSchregRegdDatData($model);
        $regdRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["HR_NAME"] = $regdRow["HR_NAME"];
        $arg["data"]["ATTENDNO"] = $regdRow["ATTENDNO"]."番";

        $query = knjf020Query::getSchregBaseMstData($model);
        $baseRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["NAME"] = $baseRow["NAME"];

        //入力方法ラジオボタン
        $opt_nyuryoku = array(1, 2);
        $model->nyuryoku = ($model->nyuryoku == "") ? "1" : $model->nyuryoku;
        $extra = array("id=\"NYURYOKU1\" onClick=\"myHidden()\"", "id=\"NYURYOKU2\" onClick=\"myHidden()\"");
        $radioArray = knjCreateRadio($objForm, "NYURYOKU", $model->nyuryoku, $extra, $opt_nyuryoku, get_count($opt_nyuryoku));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力データラジオボタン
        $sql = knjf020Query::getNameMst($model, "F550");
        $result = $db->query($sql);
        $hiddenVal = "";
        $hiddenShow = "";
        $sep = "";
        $f550Array = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cd = (int) $row["VALUE"];
            $arg["data"]["SHOW_DIV".$cd] = $row["LABEL2"];

            $f550Array[$row["VALUE"]] = $row["SHOW"];
            $hiddenVal .= $sep.$row["VALUE"];
            $hiddenShow .= $sep.$row["SHOW"];
            $sep = ",";
        }

        $opt_data = array();
        $extra = array();
        foreach ($f550Array as $key => $val) {
            $cd = (int) $key;
            $opt_data[] = $cd;
            $extra[] = "id=\"TYPE_DIV{$cd}\"";
        }
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }
        /***
                $opt_data = array(1, 2, 3, 4, 5, 6, 7);
                $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
                $extra = array("id=\"TYPE_DIV1\"",
                               "id=\"TYPE_DIV2\"",
                               "id=\"TYPE_DIV3\"",
                               "id=\"TYPE_DIV4\"",
                               "id=\"TYPE_DIV5\"",
                               "id=\"TYPE_DIV6\"",
                               "id=\"TYPE_DIV7\"");
                $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
                foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        ***/
        //表示ラジオ
        $opt = array(1, 2);
        $model->disp = ($model->disp == "") ? "1" : $model->disp;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"DISP{$val}\" onClick=\"btn_submit('sisiki')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DISP", $model->disp, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }


        /* 各歯列作成 */
        $query = knjf020Query::getToothData($model);
        $toothInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //永久歯ラベル上段
        makeSiretu($objForm, $arg, $model->adultUpLabelName, $toothInfo, "UP_ADULT", "upAdult", $f550Array);

        //永久歯ラベル下段
        makeSiretu($objForm, $arg, $model->adultLwLabelName, $toothInfo, "LW_ADULT", "lwAdult", $f550Array);

        //乳歯ラベル上段
        makeSiretu($objForm, $arg, $model->babyUpLabelName, $toothInfo, "UP_BABY", "upBaby", $f550Array);

        //乳歯ラベル下段
        makeSiretu($objForm, $arg, $model->babyLwLabelName, $toothInfo, "LW_BABY", "lwBaby", $f550Array);

        //右側の文言表示
        $arg["data"]["RIGHT_SIDE_LABEL"] = ($model->disp == "1") ? '左' : '右';
        //左側の文言表示
        $arg["data"]["LEFT_SIDE_LABEL"]  = ($model->disp == "1") ? '右' : '左';

        makeBtn($objForm, $arg, $model);

        $sql = knjf020Query::getNameMst($model, "F550");
        $result = $db->query($sql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$row["VALUE"]."')\"",
                                 "NAME" => $row["LABEL2"]);
        }
        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
        $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
        foreach ($dataArray as $key => $val) {
            $setData["CLICK_NAME"] = $val["NAME"];
            $setData["CLICK_VAL"] = $val["VAL"];
            $arg["menu"][] = $setData;
        }
        $result->free();

        //hiddenを作成する
        makeHidden($objForm, $hiddenVal, $hiddenShow, $model);

        if ($model->cmd == "sisiki") {
            $arg["topLoad"] = "top.main_frame.right_frame.chgDataSisikiUp('{$model->schregno}')";
        }

        if (get_count($model->warning) == 0 && $model->cmd == "sisiki2") {
            $arg["next"] = "NextStudent(0);";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf020SubForm2.html", $arg);
    }
}

//歯列作成
function makeSiretu(&$objForm, &$arg, $dataArray, $toothInfo, $field, $setarg, $f550Array)
{
    $textAlign = "style=\"text-align:center\"";
    foreach ($dataArray as $key => $val) {
        $checked = $toothInfo[$key] ? "1" : "";
        $toothVal = $toothInfo[$key];
        $toothShow = $checked == "1" ? $f550Array[$toothInfo[$key]] : "";

        $extra = "readonly=\"readonly\" onClick=\"kirikae(this, '".$key."_ID')\" oncontextmenu=\"kirikae2(this, '".$key."_ID')\"; ";
        $setText[$field."_LABEL_NAME"] = $val;
        $setText[$field."_FORM_NAME"] = knjCreateTextBox($objForm, $toothShow, $key, 2, 2, $textAlign.$extra);
        knjCreateHidden($objForm, $key."_FORM_ID", $toothVal);
        $arg[$setarg][] = $setText;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //永久歯正常入力
    $extra = "onclick=\"return dataChange('ADULT');\"";
    $arg["button"]["btn_adultIns"] = knjCreateBtn($objForm, "btn_adultIns", "永久歯正常", $extra);
    //乳歯正常入力
    $extra = "onclick=\"return dataChange('BABY');\"";
    $arg["button"]["btn_babyIns"] = knjCreateBtn($objForm, "btn_babyIns", "乳歯正常", $extra);
    //全データクリア
    $extra = "onclick=\"return dataChange('CLEAR');\"";
    $arg["button"]["btn_dataClear"] = knjCreateBtn($objForm, "btn_dataClear", "データクリア", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('subUpdate');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前の生徒へボタン
    $extra = "style=\"width:130px\" onclick=\"updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
    $arg["button"]["btn_up_pre"] = KnjCreateBtn($objForm, "btn_up_pre", "更新後前の".$model->sch_label."へ", $extra);
    //更新後次の生徒へボタン
    $extra = "style=\"width:130px\" onclick=\"updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
    $arg["button"]["btn_up_next"] = KnjCreateBtn($objForm, "btn_up_next", "更新後次の".$model->sch_label."へ", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('subEnd', '".$model->sisikiClick."');\"");
}

//Hidden作成
function makeHidden(&$objForm, $hiddenVal, $hiddenShow, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SETVAL", $hiddenVal);
    knjCreateHidden($objForm, "SETSHOW", $hiddenShow);
    knjCreateHidden($objForm, "SISIKI_CLICK", $model->sisikiClick);
}
