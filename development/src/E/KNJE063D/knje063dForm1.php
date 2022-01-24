<?php
//ビュー作成用クラス
class knje063dForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje063dindex.php", "", "edit");
        $db = Query::dbCheckOut();

        if (!in_array($model->cmd, array("add_year", "subclass")) && !isset($model->warning) || in_array($model->cmd, array("updEdit", "reset", "change_year", "add_year"))) {
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;
        $schoolKind = $db->getOne(knje063dQuery::getSchoolKind($model));

        //年度コンボボックス
        $opt_year = makeYear($db, $model, $schoolKind);
        $extra = " onChange=\"return btn_submit('change_year');\"  ";
        $arg["year"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt_year, $extra, 1);
        $year = strlen($model->year) ? $model->year : $opt_year[0]["value"];
        //年度追加テキスト
        $extra = " id=\"year_add\" onblur=\"this.value=toInteger(this.value,'year_add');\"";
        $arg["year"]["year_add"] = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);
        //年度追加ボタン
        $extra = "id=\"add_year_btn\" onclick=\"return add('');\"";
        $arg["year"]["btn_year_add"] = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);


        if ($arg["NOT_WARNING"]) {
            foreach ($model->classcds as $classcd) {
                $model->field["SUBCLASS_".$classcd] = "";
            }
            $query = knje063dQuery::selectQuery($model, $year);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclasscd = $row["SUBCLASSCD"];
                $model->field["SUBCLASS_".$row["CLASSCD"]] = $subclasscd;
                $model->field["REMARK_".$subclasscd] = $row["REMARK1"];
            }
            $result->free();
        }

        foreach ($model->classcds as $classcd) {
            $row = array();
            //教科コンボ
            $query = knje063dQuery::getClassMst($classcd);
            $extra = " onChange=\"return btn_submit('class');\"";
            $row["CLASSCD"] = makeCmb($objForm, $db, $query, $classcd, "CLASSCD_{$classcd}", $extra, 1, "");

            //科目コンボ
            $query = knje063dQuery::getSubclassMst($year, $classcd);
            $extra = " class=\"subclass_select\" onChange=\"return btn_submit('subclass');\"";
            $extra .= " id=\"SUBCLASS_{$classcd}\" ";
            $subclasscd = $model->field["SUBCLASS_".$classcd];
            $row["SUBCLASSCD"] = makeCmb($objForm, $db, $query, $subclasscd, "SUBCLASS_{$classcd}", $extra, 1, "BLANK");

            //文言評価
            $extra = " id=\"REMARK_{$classcd}\" ";
            if ($subclasscd == '') {
                $extra .= " disabled ";
            }
            $row["REMARK"] = knjCreateTextArea($objForm, "REMARK_{$classcd}", $model->gyou, $model->moji * 2, "wrap", $extra, $model->field["REMARK_{$subclasscd}"]);

            $arg["row"][] = $row;
        }
        $arg["REMARK_SIZE"] = "(全角で {$model->moji}文字X{$model->gyou}行)";


        /**********/
        /* ボタン */
        /**********/
        makeButton($objForm, $arg, $db, $model);

        /**********/
        /* hidden */
        /**********/
        makeHidden($objForm, $db, $model);

        if ($model->warning && $model->cmd !="reset") {
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd =="reset") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje063dForm1.html", $arg);
    }
}
//年度作成
function makeYear($db, &$model, $schoolKind)
{
    //年度取得
    $query = knje063dQuery::selectQueryYear($model, $schoolKind);
    $result = $db->query($query);
    $make_year = array();
    if (strlen($model->year)) {
        $make_year[] = $model->year;
    }
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $make_year[] = $row["YEAR"];
    }

    //年度追加された値を保持
    $year_arr = array_unique($make_year);
    $make_year = array();
    foreach ($year_arr as $val) {
        $make_year[] = array("label" => $val, "value" => $val);
    }
    rsort($make_year);
    return $make_year;
}
function makeButton(&$objForm, &$arg, $db, &$model)
{
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    ////更新後前の生徒へボタン
    //$arg["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "updEdit", "update");
    //取消しボタンを作成する
    $extra = "onclick=\"return btn_submit('reset')\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}

//コンボ作成
function makeCmb(&$objForm, $db, $query, &$value, $name, $extra, $size, $blank)
{
    $opt = array();
    if ($blank) {
        $opt[] = array('label' => '',
                       'value' => '');
    }
    $result = $db->query($query);
    $resCount = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        $resCount += 1;
    }
    if (
        preg_match("/^SUBCLASS_/", $name)
    ) {
        if ($value && $value_flg) {
        } elseif ($resCount == 1) {
            $value = $opt[1]["value"];
        } else {
            $value = $opt[0]["value"];
        }
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $cmb = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
    return $cmb;
}
function makeHidden(&$objForm, $db, &$model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "nextURL", $model->nextURL);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "mode", $model->mode);
    knjCreateHidden($objForm, "PROGRAMID", PROGRAMID);
    knjCreateHidden($objForm, "LEFT_GRADE", $model->grade);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
}
