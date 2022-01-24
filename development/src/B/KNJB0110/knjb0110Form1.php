<?php

require_once('for_php7.php');

class knjb0110Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjb0110index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $query = knjb0110Query::getYearList();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //学期コンボ作成
        $query = knjb0110Query::getSemesterList($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //職名コンボ作成
        $query = knjb0110Query::getJobmst($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "JOBCD", $model->field["JOBCD"], $extra, 1, 1);
        
        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        //$colorFlg = false;
        $result = $db->query(knjb0110Query::selectQuery($model));
        //STAFFCDフィールドサイズ変更対応
        if ($model->Properties["useStaffcdFieldSize"] === '10') {
            $setSize = "10";
        } else {
            $setSize = "8";
        }
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //職員コードを配列で取得
            $model->data["STAFFCD"][] = $row["STAFFCD"];

            /*if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }*/
            //更新用の職員コードのテキスト
            $extra = "";
            $row["UP_STAFFCD"] = knjCreateTextBox($objForm, $model->fields["UP_STAFFCD"][$counter], "UP_STAFFCD".$counter, $setSize, $setSize, $extra);
            
            //職員選択
            $extra = "onclick=\"return btn_select_staff('".$counter."', '".$model->field["YEAR"]."', 'select_staff');\"";
            $row["btn_staff"] = knjCreateBtn($objForm, "btn_staff".$counter, "職員選択", $extra);
            
            //職員名セット用
            $row["UP_STAFFNAME"] = "UP_STAFFNAME".$counter;
            
            //$row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            $row["COLOR"] = "#ffffff";

            $counter++;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJB0110");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "STAFFCD_SIZE", $setSize);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb0110Form1.html", $arg);
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
function makeBtn(&$objForm, &$arg) {
    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
