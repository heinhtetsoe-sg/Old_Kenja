<?php

require_once('for_php7.php');


class knjz220gForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz220gForm1", "POST", "knjz220gindex.php", "", "knjz220gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        if(!isset($model->field["YEAR"])) $model->field["YEAR"] = CTRL_YEAR;
        $extra = " onblur=\"this.value=toInteger(this.value);　return btn_submit('knjz220g');\"";
        $arg["data"]["YEAR"] = knjCreateTextBox($objForm, $model->field["YEAR"], "YEAR", 6, 4, $extra);

        //学年コンボ作成
        $query = knjz220gQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('knjz220g')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");
        $model->schoolKind = $db->getOne(knjz220gQuery::getSchoolKind($model));

        //テストコンボ作成
        if(!$model->field["TESTCD"]){
            $model->field["TESTCD"] = $db->getOne(knjz220gQuery::getTestone($model, '9', $model->schoolKind));
        }
        $query = knjz220gQuery::getTest($model, $model->schoolKind);
        $extra = "onchange=\"return btn_submit('knjz220g')\"";
        $opt = makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1, "");

        //科目コンボ
        $query = knjz220gQuery::getSubclass($model);
        $extra = "onchange=\"return btn_submit('knjz220g')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "ALL");

        //リスト表示
        $data_flg = false;
        $i = 1;
        $result = $db->query(knjz220gQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            knjCreateHidden($objForm, "SUBCLASSCNT", $i);
            $data_flg = true;
            knjCreateHidden($objForm, "SUBCLASS".$i, $row["SUBCLASS"]);
            $extra = "";
            $row["ASSESS_TBL_DIV"] = knjCreateTextBox($objForm, $row["ASSESS_TBL_DIV"], "ASSESS_TBL_DIV".$i, 6, 4, $extra);
            $row["CALC_UNIT_DIV"] = knjCreateTextBox($objForm, $row["CALC_UNIT_DIV"], "CALC_UNIT_DIV".$i, 6, 4, $extra);
            $arg["list"][] = $row;
            $i++;
        }

        $result->free();

        //前年度データ件数
        $pre_year = $model->field["YEAR"] - 1;
        $preYear_cnt = $db->getOne(knjz220gQuery::getCopyData($model, $pre_year, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preYear_cnt);

        //今年度データ件数
        $this_year = $model->field["YEAR"];
        $thisYear_cnt = $db->getOne(knjz220gQuery::getCopyData($model, $this_year, "cnt"));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisYear_cnt);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz220gForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $all)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    if($all == "ALL"){
        $opt[] = array('label' => "全て",
                       'value' => 'ALL');
    }
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($value && $value_flg) {
    } else {
        $value = $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $opt;
}

function makeBtn(&$objForm, &$arg) {
    //コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJZ220G");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
}

?>
