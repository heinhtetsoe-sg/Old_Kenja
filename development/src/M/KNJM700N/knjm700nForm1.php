<?php

require_once('for_php7.php');
class knjm700nForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm700nForm1", "POST", "knjm700nindex.php", "", "knjm700nForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //日付データ
        if ($model->Date == "") $model->Date = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");

        //チェック用hidden
        knjCreateHidden($objForm, "YEAR", $model->Year);
        knjCreateHidden($objForm, "DEFOULTDATE", $model->Date);
        knjCreateHidden($objForm, "DEFOULTSEME", $model->semester);
        knjCreateHidden($objForm, "GAKKISU", $model->control["学期数"]);
        knjCreateHidden($objForm, "SEME1S", $model->control["学期開始日付"]["1"]);
        knjCreateHidden($objForm, "SEME1E", $model->control["学期終了日付"]["1"]);
        knjCreateHidden($objForm, "SEME2S", $model->control["学期開始日付"]["2"]);
        knjCreateHidden($objForm, "SEME2E", $model->control["学期終了日付"]["2"]);
        if ($model->control["学期数"] == 3) {
            knjCreateHidden($objForm, "SEME3S", $model->control["学期開始日付"]["3"]);
            knjCreateHidden($objForm, "SEME3E", $model->control["学期終了日付"]["3"]);
        }

        //クラス
        $opt_hr_name=array();
        $query = knjm700nQuery::selectHRName($model);
        $result=$db->query($query);
        while($row=$result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_hr_name[]=array("label"=>$row["LABEL"],"value"=>$row["VALUE"]);

            if(!isset($model->field["HR_NAME"])){
                $model->field["HR_NAME"]=$row["VALUE"];
            }
        }

        $extra = "onChange=\"btn_submit('chg_hr_name');\"";
        $arg["sel"]["HR_NAME"]=knjCreateCombo($objForm,"HR_NAME",$model->field["HR_NAME"],$opt_hr_name,$extra,1);

        //単位時間
        $opt_credit_time = array();
        $result = $db->query(knjm700nQuery::selectCreditTime("M010"));

        $extra = "onChange=\"btn_submit('');\"";
        $opt_credit_time[] = array("label" => " ", "value" => "0");
        while ($row=$result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_credit_time[]= array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }

        // その他の更新確認ダイアログ
        if ($model->field["sonotaConfFlg"]) {
            $model->field["sonotaConfFlg"] = '';
            $arg["confirmSonota"] = " confirmSonota('add');";
        }

        //選択チェック
        $extra = "id=\"CHECK_ALL\" onClick=\"checkAll(this)\"";
        $arg["data"]["CHECK_ALL"] = knjCreateCheckBox($objForm, "CHECK_ALL", "1", $extra);

        //抽出データ出力
        $schcnt = 0;
        $model->schregNos = array();

        //新規ループ
        $result = $db->query(knjm700nQuery::selectSch($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //学籍番号
            $Row["SCHREGNO"] = $row["SCHREGNO"];
            $model->schregNos[] = $row["SCHREGNO"];
            //hidden
            knjCreateHidden($objForm, "SCHREGNO".$row["SCHREGNO"], $row["SCHREGNO"]);
            
            //出席番号
            $Row["ATTENDNO"] = $row["ATTENDNO"];
            
            //氏名（漢字）
            $Row["NAME"] = $row["NAME"];

            //単位時間
            $extra="";
            $Row["CREDIT_TIME"] = knjCreateCombo($objForm, "CREDIT_TIME-".$row["SCHREGNO"], $row["CREDIT_TIME"], $opt_credit_time, $extra, 1);

            $arg["data2"][] = $Row;

            $schcnt++;
        }
        
        //新規ループ終了

        $model->schcntall = $schcnt;
        $result->free();

        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $extra = "onclick=\"return btn_submit('add');\" ";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('reset');\" ";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "取 消", $extra);

        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "sonotaNotChk", $model->field["sonotaNotChk"]);
        knjCreateHidden($objForm, "sonotaConfFlg", $model->field["sonotaConfFlg"]);
        knjCreateHidden($objForm, "SCHREGNOS", implode(',', $model->schregNos));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm700nForm1.html", $arg); 
    }
}

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

    $arg["sel"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
