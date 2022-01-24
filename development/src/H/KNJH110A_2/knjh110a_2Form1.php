<?php

require_once('for_php7.php');

class knjh110a_2Form1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjh110a_2index.php", "", "main");

        //権限チェック
        if ($model->auth < DEF_UPDATE_RESTRICT) {
            $arg["close"] = "closing_window();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //生徒名
        $studentRow = array();
        $studentRow  = $db->getRow(knjh110a_2Query::getStudentName($model), DB_FETCHMODE_ASSOC);
        $arg["NAME"] = "　" .$studentRow["LABEL"];

        //科目名
        //教育課程対応
        /*if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
            $query = knjh110a_2Query::getSchoolKind($model);
            $model->schoolkind = $db->getOne($query);
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $query = knjh110a_2Query::getName($model,$model->schoolkind);
        $extra = "onchange=\"return btn_submit('cmb_sub');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->subclassCd, $extra, 1);*/
        
        $opt = array();
        $value_flg = false;
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
            $query = knjh110a_2Query::getSchoolKind($model);
            $model->schoolkind = $db->getOne($query);
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $query = knjh110a_2Query::getName($model,$model->schoolkind);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->subclassCd == $row["VALUE"]) $value_flg = true;
        }
        $model->subclassCd = ($model->subclassCd && $value_flg) ? $model->subclassCd : $model->first_set_Cd;
        $extra = "onchange=\"return btn_submit('cmb_sub');\"";
        $arg["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $model->subclassCd, $opt, $extra, 1);

        //リスト表示
        $model->data=array();
        makeList($objForm, $arg, $db, $model);

        //入力項目
        makeInput($objForm, $arg, $db, $model);

        //クレジットバンキング登録済みの科目リスト
        makeSubclassList($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh110a_2Form1.html", $arg); 
    }
}
/***************************************    これ以下は関数    **************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    //最初のコンボ表示は、KNJA110Aから受け取った科目
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト表示
function makeList(&$objForm, &$arg, $db, &$model) {
    $sDate = (CTRL_YEAR - 1) ."/04/01";
    $query = knjh110a_2Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //取得日
        $row["REGDDATE"]  = str_replace("-", "/", $row["REGDDATE"]);
        //無効はリンクなし
        if ($sDate <= $row["REGDDATE"]) {
            $row["URL"] = View::alink("knjh110a_2index.php", $row["REGDDATE"], "",
                                        array("cmd"         => "link",
                                              "SEQ"         => $row["SEQ"]
                                              ));
        } else {
            $row["URL"] = $row["REGDDATE"];
        }

        $arg["data2"][] = $row;
    }
    $result->free();
}

//入力項目
function makeInput(&$objForm, &$arg, $db, &$model) {
    $bankRow = array();
    if (isset($model->seq) && !isset($model->warning)){
        $query = knjh110a_2Query::getList($model, $model->seq);
        $bankRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
    } else {
        $bankRow =& $model->field;
        $bankRow["SEQ"] = $model->seq;
    }

    $arg["data"]["SEQ"] = $bankRow["SEQ"];
    /************/
    /* カレンダ */
    /************/
    $name = "REGDDATE";
    $arg["data"][$name] = View::popUpCalendar($objForm, $name, str_replace("-", "/", $bankRow[$name]));
    /************/
    /* テキスト */
    /************/
    $name = "MINUTES";
    $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\" ";
    $arg["data"][$name] = knjCreateTextBox($objForm, $bankRow[$name], $name, 4, 4, $extra);
    $name = "REMARK";
    $extra = "style=\"width: 100%\" ";
    $arg["data"][$name] = knjCreateTextBox($objForm, $bankRow[$name], $name, 90, 90, $extra);
}

//クレジットバンキング登録済みの科目リスト
function makeSubclassList(&$objForm, &$arg, $db, &$model) {
    $query = knjh110a_2Query::getSubclassList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["data3"][] = $row;
    }
    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, &$model) {
    //追加ボタン
    $extra  = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra  = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra  = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //終了ボタン
    $extra  = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, &$model) {
    knjCreateHidden($objForm, "cmd");
}
?>
