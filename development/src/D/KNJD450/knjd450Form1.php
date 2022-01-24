<?php

require_once('for_php7.php');

class knjd450Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd450index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        
        //生徒表示
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;
        
        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "edit") {
            unset($model->getDiv);
            unset($model->getWritingDate);
            unset($model->getUpdFlg);
            unset($model->getSubclassName);
            unset($model->setGyou);
        }

        //作成年月日コンボ
        $opt = array();
        $date_seme = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        if ($model->cmd == "set") $model->field["WRITING_DATE"] = $model->getWritingDate;
        $query = knjd450Query::getWritingDate($model);
        $result1 = $db->query($query);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row1["LABEL"],
                           'value' => $row1["VALUE"]);
            if ($model->field["WRITING_DATE"] == $row1["VALUE"]) $value_flg = true;
            $date_seme[$row1["VALUE"]] = $row1["SEMESTER"];
        }
        $model->field["WRITING_DATE"] = ($model->field["WRITING_DATE"] && $value_flg) ? $model->field["WRITING_DATE"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('change');\"";
        $arg["data"]["WRITING_DATE"] = knjCreateCombo($objForm, "WRITING_DATE", $model->field["WRITING_DATE"], $opt, $extra, 1);

        //労働情報取得
        if ($model->cmd == "set" || $model->cmd == "change" || $model->cmd == "clear") {
            if (isset($model->schregno) && !isset($model->warning)){
                if (!$model->field["WRITING_DATE"]) {
                    unset($model->field);
                } else {
                    $Row = $db->getRow(knjd450Query::getSelectData($model, $date_seme[$model->field["WRITING_DATE"]]), DB_FETCHMODE_ASSOC);
                }
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        
        //行数セット
        if ($model->getDiv === '1') {
            $model->setGyou = "24";
        } else if ($model->getDiv == "") {
            $model->setGyou = "　";
        } else {
            $model->setGyou = "17";
        }
        //項目名表示
        $arg["KOUMOKU"] = $model->getSubclassName;
        
        //授業中の様子
        if ($model->setGyou == "24") {
            $extra = "style=\"height:380px;\"";
        } else {
            $extra = "style=\"height:270px;\"";
        }
        $arg["data"]["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", $model->setGyou, 31, "soft", $extra, $Row["REMARK1"]);
        $arg["data"]["REMARK1_SIZE"] = '<font size="1" color="red">(全角15文字'.$model->setGyou.'行まで)</font>';

        //支援目標
        $arg["data"]["REMARK2"] = knjCreateTextArea($objForm, "REMARK2", $model->setGyou, 23, "soft", $extra, $Row["REMARK2"]);
        $arg["data"]["REMARK2_SIZE"] = '<font size="1" color="red">(全角11文字'.$model->setGyou.'行まで)</font>';

        //具体的支援の方略
        $arg["data"]["REMARK3"] = knjCreateTextArea($objForm, "REMARK3", $model->setGyou, 31, "soft", $extra, $Row["REMARK3"]);
        $arg["data"]["REMARK3_SIZE"] = '<font size="1" color="red">(全角15文字'.$model->setGyou.'行まで)</font>';

        //支援の評価
        $arg["data"]["REMARK4"] = knjCreateTextArea($objForm, "REMARK4", $model->setGyou, 17, "soft", $extra, $Row["REMARK4"]);
        $arg["data"]["REMARK4_SIZE"] = '<font size="1" color="red">(全角8文字'.$model->setGyou.'行まで)</font>';

        //作成日
        if ($model->cmd == "set" || $model->cmd == "change") {
            $model->field["WRT_DATE"] = ($model->field["WRITING_DATE"]) ? $model->field["WRITING_DATE"] : CTRL_DATE;
        }
        $arg["data"]["WRT_DATE"] = View::popUpCalendar($objForm, "WRT_DATE", str_replace("-", "/", $model->field["WRT_DATE"]));

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd450Form1.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    //データチェック
    $div2Cnt = $db->getOne(knjd450Query::getCountData($model, "2"));
    $div3Cnt = $db->getOne(knjd450Query::getCountData($model, "3"));
    $div4Cnt = $db->getOne(knjd450Query::getCountData($model, "4"));
    $retCnt = 0;
    $query = knjd450Query::getRecordList($model, $div2Cnt, $div3Cnt, $div4Cnt);
    $result = $db->query($query);
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowlist["REMARK1"] = substr($rowlist["REMARK1"], 0, 30);
        $rowlist["REMARK2"] = substr($rowlist["REMARK2"], 0, 30);
        $rowlist["REMARK3"] = substr($rowlist["REMARK3"], 0, 30);
        $rowlist["REMARK4"] = substr($rowlist["REMARK4"], 0, 30);
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消しボタンを作成する
    $extra = "onclick=\"return btn_submit('clear')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD450");
}

?>
