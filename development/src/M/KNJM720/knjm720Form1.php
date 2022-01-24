<?php

require_once('for_php7.php');

class knjm720Form1 {

    var $dataRow = array(); //表示用一行分データをセット

    function main(&$model) {

         $arg["jscript"] = "";

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjm720index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjm720Query::getYear();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //履修パターンリスト表示
        $result = $db->query(knjm720Query::getCompCreditsPatternList($model));
        $this->DataRow = array();
        for ($i=0; $row=$result->fetchRow(DB_FETCHMODE_ASSOC); $i++) {
            if ($i == 0) $cd[] = $row["PATTERN_CD"];

            //群コードが同じ間は表示しない
            if (in_array($row["PATTERN_CD"], $cd)) {
                $this->setDataRow($row);
            } else {
                $this->ModifyDataRow();
                $arg["data"][] = $this->DataRow;

                $cd = $this->DataRow = array();
                $cd[] = $row["PATTERN_CD"];
                $this->setDataRow($row);
            }
        }
        $this->ModifyDataRow();
        $arg["data"][] = $this->DataRow;

        $result->free();

        //コピーボタン
        if (AUTHORITY >= DEF_UPDATE_RESTRICT) {
            $extra = "onclick=\"return btn_submit('copy');\"";
            $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
        }

        //hidden
        $query = knjm720Query::getCompCreditsPatternCnt($model->year);
        $ctrlCnt = $db->getOne($query);
        knjCreateHidden($objForm, "ctrlCnt", $ctrlCnt);

        $query = knjm720Query::getCompCreditsPatternCnt(($model->year - 1));
        $lastCnt = $db->getOne($query);
        knjCreateHidden($objForm, "lastCnt", $lastCnt);

        knjCreateHidden($objForm, "cmd");

        if ($model->cmd == "combo") {
            $arg["jscript"] = "window.open('knjm720index.php?cmd=edit','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjm720Form1.html", $arg); 
    }

    //表示用配列にデータ値をセット
    function SetDataRow($row) {
        $this->DataRow["PATTERN_CD"][]   = View::alink("knjm720index.php", $row["PATTERN_CD"],"target=right_frame",
                                  array("PATTERN_CD"    => $row["PATTERN_CD"],
                                        "cmd"           => "edit",
                                        "PATTERN_NAME"  => $row["PATTERN_NAME"]));
        $this->DataRow["PATTERN_NAME"][] = $row["PATTERN_NAME"];
        $this->DataRow["SUBCLASSCD"][]   = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"];
    }

    //表示用配列を表示できるように修正
    function ModifyDataRow() {
        if (isset($this->DataRow["PATTERN_CD"]))   $this->DataRow["PATTERN_CD"]   = implode("<BR>",array_unique($this->DataRow["PATTERN_CD"]));
        if (isset($this->DataRow["PATTERN_NAME"])) $this->DataRow["PATTERN_NAME"] = implode("<BR>",array_unique($this->DataRow["PATTERN_NAME"]));
        if (isset($this->DataRow["SUBCLASSCD"]))   $this->DataRow["SUBCLASSCD"]   = implode("<BR>",array_unique($this->DataRow["SUBCLASSCD"]));
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

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
