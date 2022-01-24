<?php

require_once('for_php7.php');


class knjf165Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjf165Form1", "POST", "knjf165index.php", "", "knjf165Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //1:クラス,2:個人表示指定
        $opt_data = array(1, 2);
        $model->field["KUBUN"] = ($model->field["KUBUN"] == "") ? "1" : $model->field["KUBUN"];
        $extra = array("id=\"KUBUN1\" onClick=\"btn_submit('knjf165')\"", "id=\"KUBUN2\" onClick=\"btn_submit('knjf165')\"");
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["KUBUN"] == 1) $arg["clsno"] = $model->field["KUBUN"];
        if ($model->field["KUBUN"] == 2) $arg["schno"] = $model->field["KUBUN"];

        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjf165');\"";
        $query = knjf165Query::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //2:個人表示指定用
        if ($model->field["KUBUN"] == 2) {
            //クラスコンボ
            $extra = "onChange=\"return btn_submit('knjf165');\"";
            $query = knjf165Query::getHrClassAuth($model->field["GRADE"]);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        }

        //クラス一覧リスト
        makeClassItiran($objForm, $arg, $db, $model);

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf165Form1.html", $arg); 

    }

}
/**************************************** 以下関数 **************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeClassItiran(&$objForm, &$arg, $db, &$model) {
    //初期化
    $opt_left = array();
    $opt_right = array();
    //1:クラス表示指定用
    if ($model->field["KUBUN"] == 1) {
        $query = knjf165Query::getHrClassAuth($model->field["GRADE"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //クラスの時、BASE_REMARK1が1のカウント取得
            $query = knjf165Query::getBaseCnt($row["VALUE"]);
            $cntRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $mark = "　";
            if ($cntRow["BASE_CNT"] == 0) {
                $mark = "×"; //全員NULL
            } else if ($cntRow["BASE_CNT"] == $cntRow["SCHREG_CNT"]) {
                $mark = "○"; //全員１
            } else if ($cntRow["BASE_CNT"] != $cntRow["SCHREG_CNT"]) {
                $mark = "△"; //一部１
            }
            $row["LABEL"] = $mark."　".$row["LABEL"];
            //全員１の時、左側に表示
            if ($mark == "○") {
                $opt_left[]  = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
        //記号コメント
        $arg["data"]["MARK_COMMENT"] = "○：全員加入済　△：一部加入済　×：全員未加入";
    }
    //2:個人表示指定用
    if ($model->field["KUBUN"] == 2) {
        $query = knjf165Query::getSchno($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ANOTHER_SPORTが1のカウント取得
            $query = knjf165Query::getAnotherCnt($row["SCHREGNO"]);
            $cntRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $mark = "　";
            if ($cntRow["ANOTHER_CNT"] > 0) {
                $mark = "●"; //１
            }
            $row["LABEL"] = $mark."　".$row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"];
            $row["VALUE"] = $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"];
            //BASE_REMARK1を取得
            $query = knjf165Query::getBaseRemark1($row["SCHREGNO"]);
            $baseRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //BASE_REMARK1='1'の時、左側に表示
            if ($baseRow["BASE_REMARK1"] == "1") {
                $opt_left[]  = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
        //記号コメント
        $arg["data"]["MARK_COMMENT"] = "●：前籍校加入済";
    }

    $result->free();

    $chdt = $model->field["KUBUN"];

    //対象クラスリスト
    $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left', $chdt)\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt_right, $extra, 18);

    //出力クラスリスト
    $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right', $chdt)\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $opt_left, $extra, 18);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $chdt);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $chdt);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $chdt);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $chdt);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    //$extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    //$arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "PROGRAMID");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "selectright");
}

?>
