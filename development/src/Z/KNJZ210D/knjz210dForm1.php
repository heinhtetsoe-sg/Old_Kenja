<?php

require_once('for_php7.php');

class knjz210dForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz210dindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学年コンボ作成
        $query = knjz210dQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, 1);

        $setNameCd = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setSchoolKind = $db->getOne(knjz210dQuery::getSchoolKind($model->field["GRADE"]));
            $setNameCd = "Z".$setSchoolKind."09";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }

        //学期取得
        $result = $db->query(knjz210dQuery::getNameMst($setNameCd));
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学期を配列で取得
            $model->gakki["SEMESTER"][] = $Row["LABEL"];
            $model->semester["VALUE"][] = $Row["VALUE"];
            $arg["gakki"][] = $Row;
        }
        $result->free();

        //学期数の取得
        $model->semester_count = $db->getOne(knjz210dQuery::getSemestercount($setNameCd));

        //教科コンボ作成
        $query = knjz210dQuery::getClassMst("", $model->field["GRADE"], $model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");

        //科目コンボ作成
        $query = knjz210dQuery::getSubclassMst($model->field["CLASSCD"], $model->field["GRADE"], $model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $result = $db->query(knjz210dQuery::selectQuery($model, $model->semester_count));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //観点コードを配列で取得
            $model->data["VIEWCD"][] = $row["VIEWCD"];
            
            //初期データチェック
            if ($counter == 0) {
                $datacheck = $db->getOne(knjz210dQuery::setdatacount($model));
            }
            $model->fields["VIEWFLG1"][$counter] = $row["VIEWFLG1"];
            $model->fields["VIEWFLG2"][$counter] = $row["VIEWFLG2"];
            $model->fields["VIEWFLG3"][$counter] = $row["VIEWFLG3"];
            if($datacheck == 0) {
                $model->fields["VIEWFLG1"][$counter] = "1";
                $model->fields["VIEWFLG2"][$counter] = "1";
                $model->fields["VIEWFLG3"][$counter] = "1";
                knjz210dQuery::delete($model, $db);
                knjz210dQuery::insJviewstatInputSeqDat($model, $db, '1');
                knjz210dQuery::insJviewstatInputSeqDat($model, $db, '2');
                //3学期がある場合のみ
                if ($model->semester_count == 3) {
                    knjz210dQuery::insJviewstatInputSeqDat($model, $db, '3');
                }
            }
            
            //1学期または前期の観点ごとのチェックボックス
            if ($model->fields["VIEWFLG1"][$counter] == "1") {
                $extra = "checked='checked' ";
            } else {
                $extra = "";
            }
            $row["VIEWCHECK1"] = knjCreateCheckBox($objForm, "VIEWFLG1-".$counter, "1", $extra);
            
            //2学期または後期の観点ごとのチェックボックス
            if ($model->fields["VIEWFLG2"][$counter] == "1") {
                $extra = "checked='checked' ";
            } else {
                $extra = "";
            }
            $row["VIEWCHECK2"] = knjCreateCheckBox($objForm, "VIEWFLG2-".$counter, "1", $extra);
            
            //3学期の観点ごとのチェックボックス(存在するときのみ)
            if ($model->semester_count == 3) {
                $arg["3gakki"] = "1";
                if ($model->fields["VIEWFLG3"][$counter] == "1") {
                    $extra = "checked='checked' ";
                } else {
                    $extra = "";
                }
                $row["VIEWCHECK3"] = knjCreateCheckBox($objForm, "VIEWFLG3-".$counter, "1", $extra);
            }

            //学年観点評価設定済みフラグ
            if ($row["ASSESSHIGH"]) {
                $row["HYOUKACHECK"] = 'レ';
            }

            $counter++;            
            
            $arg["data"][] = $row;

        }
        $result->free();

        //ASSESSHIGHTがNULLであるものを取得
        $null_assesshight = $db->getOne(knjz210dQuery::AssessHighcount($model));
        
        //ボタン作成
        makeBtn($objForm, $arg, $null_assesshight, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJZ210D");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210dForm1.html", $arg);
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

//ボタン作成
function makeBtn(&$objForm, &$arg, &$null_assesshight, &$model) {
    
    if ($null_assesshight != 0) {
        $nullcheck_disabled = "disabled";
    }

    //設定ボタンを作成する
    $link  = REQUESTROOT."/Z/KNJZ210E/knjz210eindex.php?&SEND_PRGRID=KNJZ210D&SEND_AUTH={$model->auth}&SEND_DIV=1&SEND_CLASSCD={$model->field["CLASSCD"]}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_GRADE={$model->field["GRADE"]}";
    if ($model->Properties["use_prg_schoolkind"] == "1") {
        $link .= "&SEND_selectSchoolKind=".$model->selectSchoolKind;
    }
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["btn_settei"] = knjCreateBtn($objForm, "btn_settei", "学年観点別評価設定", $extra);
    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$nullcheck_disabled);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra.$nullcheck_disabled);
}
?>
