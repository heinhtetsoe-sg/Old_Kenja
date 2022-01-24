<?php

require_once('for_php7.php');

class knjj144bForm1 {
    function main(&$model) {
        $objForm = new form;
        //タイトル
        $arg["data"]["YEAR"] = CTRL_YEAR."年度　ＣＳＶ出力／取込";
        //DB接続
        $db = Query::dbCheckOut();

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        if($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header);

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********/
        /* FILE */
        /********/
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        /**********/
        /* コンボ */
        /**********/
        //処理名
        $opt_shori   = array();
        $opt_shori[] = array("label" => "更新","value" => "1");
        $opt_shori[] = array("label" => "削除","value" => "2");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);
        
        //学年コンボ
        $extra = "onchange=\"return btn_submit('');\"";
        $query = knjj144bquery::getGrade('cmb');
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //年組コンボ
        $model->field["GRADE"] = $model->field["GRADE"] ? $model->field["GRADE"] : $db->getOne(knjj144bquery::getGrade('value'));
        $extra = "";
        $query = knjj144bquery::getHrClass($model->field["GRADE"]);
        makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra, 1);

        //性別コンボ
        $model->field["SEX"] = $model->field["SEX"] ? $model->field["SEX"] : "ALL";
        $query = knjj144bquery::getGender();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEX"], "SEX", $extra, 1);

        //欠席コンボ
        $opt_attend_cd   = array();
        $opt_attend_cd[] = array("label" => "含む","value" => "1");
        $opt_attend_cd[] = array("label" => "含まない","value" => "2");
        $arg["data"]["ATTEND_CD"] = knjCreateCombo($objForm, "ATTEND_CD", $model->field["ATTEND_CD"], $opt_attend_cd, "", 1);

        /**********/
        /* ボタン */
        /**********/
        //実行
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
        //終了
        $arg["btn_end"]  = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjj144bindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj144bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size) {
    $opt = array();
    $value_flg = false;
    if($name == "HR_CLASS") $opt[] = array('label' => "すべて", 'value' => "ALL");
    if($name == "SEX") $opt[] = array('label' => "男女", 'value' => "ALL");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $arg['data'][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
