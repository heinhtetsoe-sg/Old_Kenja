<?php

require_once('for_php7.php');

class knjp713Form1
{
    var $DataRow = array(); //表示用一行分データをセット

    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp713index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjp713Query::getYear();
        $extra = "onChange=\" return btn_submit('changeYear')\"";
        makeCmb($objForm, $arg, $db, $query, $model->taisyouYear, "TAISYOU_YEAR", $extra, 1);

        //学年コンボ
        $query = knjp713Query::getGrade($model);
        $extra = "onChange=\" return btn_submit('changeYear')\"";
        makeCmb($objForm, $arg, $db, $query, $model->taisyouGrade, "TAISYOU_GRADE", $extra, 1);

        //コピー用データチェック 登録
        $query = knjp713Query::selectYearQuery($model->taisyouYear, $model->taisyouGrade, "COLLECT_GRP_MST");
        $year_flg  = $db->getOne($query);
        $query = knjp713Query::selectYearQuery($model->taisyouYear, $model->taisyouGrade, "COLLECT_GRP_DAT");
        $year_flg += $db->getOne($query);

        //コピー用マスタチェック(前年度に登録されている項目が今年度に存在するか)
        $mst_flg = "";
        $result = $db->query(knjp713Query::mstMcdCheck($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["COLLECT_LM_CD"] != $row["MST_EXP_MCD"]) {
                $mst_flg = true;
                break;
            }
        }
        $result->free();

        //コピー用マスタチェック(前年度に登録されている細目が今年度に存在するか)
        if ($mst_flg == "") {
            $result = $db->query(knjp713Query::mstScdCheck($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["COLLECT_LMS_CD"] != $row["MST_EXP_SCD"]) {
                    $mst_flg = true;
                    break;
                }
            }
            $result->free();
        }

        //前年度からコピーボタン
        $extra = "onclick=\"btn_submit('copy');\"";
        $arg["copy_btn"] = knjCreateBtn($objForm, "copy_btn", "前年度からコピー", $extra);

        //会計グループリスト取得
        $query = knjp713Query::SelectQuery($model);
        $result = $db->query($query);
        $this->DataRow = array();
        $dataCnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $keyData = $row["GRADE"].$row["MAJORCD"].$row["COURSECODE"].$row["COLLECT_GRP_CD"];
            if ($dataCnt == 0) $cd[] = $keyData;

            //会計グループが同じ間は表示しない
            if (in_array($keyData, $cd)) {
                $this->setDataRow($row);
            } else {
                $this->modifyDataRow();
                $arg["data"][] = $this->DataRow;
                $cd = $this->DataRow = array();
                $cd[] = $keyData;
                $this->setDataRow($row);
            }
            $dataCnt++;
        }
        $result->free();

        $this->modifyDataRow();
        if (isset($this->DataRow["COLLECT_GRP_CD"])) {
            $arg["data"][] = $this->DataRow;
        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year_flg", $year_flg);
        knjCreateHidden($objForm, "mst_flg", $mst_flg);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp713Form1.html", $arg);
    }
    
    //表示用配列にデータ値をセット
    function setDataRow($row)
    {
        $this->DataRow["COLLECT_GRP_CD"][] = View::alink("knjp713index.php", $row["COLLECT_GRP_CD"].":".$row["COLLECT_GRP_NAME"]."({$row["GRADE_NAME1"]} {$row["MAJORNAME"]} {$row["COURSECODENAME"]})", "target=right_frame",
                                             array("GRADE"          => $row["GRADE"],
                                                   "MAJORCD"        => $row["MAJORCD"],
                                                   "COURSECODE"     => $row["COURSECODE"],
                                                   "COLLECT_GRP_CD" => $row["COLLECT_GRP_CD"],
                                                   "cmd"            => "edit",
                                                   "NAME"           => $row["COLLECT_GRP_NAME"]));

        if (strlen($row["COLLECT_M_CD"]))  $this->DataRow["COLLECT_M_CD"][] = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].":".$row["COLLECT_M_NAME"];
        if (strlen($row["COLLECT_S_CD"]))  $this->DataRow["COLLECT_S_CD"][] = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$row["COLLECT_S_CD"].":".$row["COLLECT_S_NAME"];
    }
    
    //表示用配列を表示できるように修正
    function modifyDataRow()
    {
        if (get_count($this->DataRow["COLLECT_GRP_CD"]))  $this->DataRow["COLLECT_GRP_CD"]  = implode("<BR>",array_unique($this->DataRow["COLLECT_GRP_CD"]));
        if (get_count($this->DataRow["COLLECT_M_CD"]))    $this->DataRow["COLLECT_M_CD"]    = implode("<BR>",array_unique($this->DataRow["COLLECT_M_CD"]));
        if (get_count($this->DataRow["COLLECT_S_CD"]))    $this->DataRow["COLLECT_S_CD"]    = implode("<BR>",array_unique($this->DataRow["COLLECT_S_CD"]));
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    if ($name == "TAISYOU_YEAR") {
        $value = ($value) ? $value : CTRL_YEAR;
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
