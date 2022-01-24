<?php

require_once('for_php7.php');

class knjz292Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz292index.php", "", "edit");

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //警告メッセージを表示しない場合
        $model->iinkai = "";
        if (!isset($model->warning) && isset($model->staffcd)) {
            if ($model->cmd != 'change') {
                $query = knjz292Query::getRow($model, $model->staffcd);
                $Row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        
        //職員コード桁数セット
        $setsize = "";
        //STAFFCDフィールドサイズ変更対応
        if ($model->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
        } else {
            $setsize = 8;
        }

        //教育委員会用学校コード取得
        $query = knjz292Query::getEdboardSchoolcd();
        $model->edboard_schoolcd = $db->getOne($query);

        //設定している学校コード取得
        $query = knjz292Query::getSchoolcd();
        $model->schoolcd = $db->getOne($query);
        
        //県側のEDBOARD_SCHOOL_MSTをチェック
        $query = knjz292Query::getCheckSchoolcd($model);
        $chckCnt = $db2->getOne($query);
        
        if ($chckCnt == 0) {
            //学校コード設定エラー
            $arg["jscript"] = "OnSchoolcdError('".$chckCnt."');";
        }

        //職員名取得
        $query = knjz292Query::getStaffName($model->staffcd);
        $model->staffName = $db->getOne($query);
        
        //職員情報表示
        if ($model->staffcd) {
            $arg["data"]["STAFF_SHOW"] = $model->staffcd.':'.$model->staffName;
        }

        //異動区分
        $extra = "onchange=\"return btn_submit('change');\"";
        $query = knjz292Query::getIdoudiv();
        makeCmb($objForm, $arg, $db, $query, "IDOU_DIV", $Row["IDOU_DIV"], $extra, 1);

        //異動日
        $setYear = CTRL_YEAR + 1;
        if ($Row["IDOU_DIV"] === '2' && $Row["IDOU_DATE"] == "") {
            $Row["IDOU_DATE"] = $setYear.'-03-31';
        }
        $Row["IDOU_DATE"] = str_replace("-","/",$Row["IDOU_DATE"]);
        $arg["data"]["IDOU_DATE"] = View::popUpCalendar($objForm, "IDOU_DATE" ,$Row["IDOU_DATE"]);

        //転入日
        if ($Row["IDOU_DIV"] === '2' && $Row["ASSIGNMENT_DATE"] == "") {
            $Row["ASSIGNMENT_DATE"] = $setYear.'-04-01';
        }
        $Row["ASSIGNMENT_DATE"] = str_replace("-","/",$Row["ASSIGNMENT_DATE"]);
        $arg["data"]["ASSIGNMENT_DATE"] = View::popUpCalendar($objForm, "ASSIGNMENT_DATE" ,$Row["ASSIGNMENT_DATE"]);

        //転出先学校(県側のテーブル)
        $query = knjz292Query::getToFinschoolcd();
        makeCmb($objForm, $arg, $db2, $query, "TO_FINSCHOOLCD", $Row["TO_FINSCHOOLCD"], "", 1);
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz292index.php?cmd=list';";
        }
                                
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz292Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    $disable = ($model->auth == DEF_UPDATABLE) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disable);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"return closeWin();\"";
    $name = "終 了";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", $name, $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
