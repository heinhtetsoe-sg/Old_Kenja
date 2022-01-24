<?php

require_once('for_php7.php');

class knjmp712Form1
{
    var $dataRow = array(); //表示用一行分データをセット

    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjmp712index.php", "", "edit");

        $arg["YEAR"] = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //学年コンボボックス
        $opt = array();
        $opt[] = array("label" => "新入生", "value" => "00");
        $value_flg = false;
        $query = knjmp712Query::get_Grade();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->grade == $row["VALUE"]) $value_flg = true;
        }
        $opt[] = array("label" => "全学年", "value" => "99");
        $model->grade = $model->grade ? $model->grade : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('change')\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $opt, $extra, 1);

        $model->leftSetYear = $model->grade != "00" ? CTRL_YEAR : (CTRL_YEAR + 1);
        Query::dbCheckIn($db);

        //コピー用データチェック 入金
        $money_flg  = knjmp712Query::selectYearQuery($model->leftSetYear, "COLLECT_MONEY_PAID_M_DAT");
        $money_flg += knjmp712Query::selectYearQuery($model->leftSetYear, "COLLECT_MONEY_PAID_S_DAT");

        //コピー用データチェック 入金
        $money_flg += knjmp712Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_MONEY_PAID_M_DAT");
        $money_flg += knjmp712Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_MONEY_PAID_S_DAT");

        //コピー用データチェック 登録
        $year_flg  = knjmp712Query::selectYearQuery($model->leftSetYear, "COLLECT_GRP_MST");
        $year_flg += knjmp712Query::selectYearQuery($model->leftSetYear, "COLLECT_GRP_HR_DAT");
        $year_flg += knjmp712Query::selectYearQuery($model->leftSetYear, "COLLECT_GRP_DAT");
        $year_flg += knjmp712Query::selectYearQuery($model->leftSetYear, "COLLECT_MONEY_DUE_M_DAT");
        $year_flg += knjmp712Query::selectYearQuery($model->leftSetYear, "COLLECT_MONEY_DUE_S_DAT");

        //コピー用データチェック 登録
        $year_flg += knjmp712Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_GRP_MST");
        $year_flg += knjmp712Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_GRP_HR_DAT");
        $year_flg += knjmp712Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_GRP_DAT");
        $year_flg += knjmp712Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_MONEY_DUE_M_DAT");
        $year_flg += knjmp712Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_MONEY_DUE_S_DAT");

        $db = Query::dbCheckOut();

        //コピー用マスタチェック(前年度に登録されているクラスが今年度に存在するか)
        $mst_flg = "";
        $result = $db->query(knjmp712Query::mstClassCheck($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["HR_CLASS"] != $row["MST_HR_CLASS"]) {
                $mst_flg = true;
                break;
            }
        }
        $result->free();

        //コピー用マスタチェック(前年度に登録されている項目が今年度に存在するか)
        if (!$mst_flg) {
            $result = $db->query(knjmp712Query::mstMcdCheck($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["COLLECT_LM_CD"] != $row["MST_EXP_MCD"]) {
                    $mst_flg = true;
                    break;
                }
            }
            $result->free();
        }

        //コピー用マスタチェック(前年度に登録されている細目が今年度に存在するか)
        if ($mst_flg == "") {
            $result = $db->query(knjmp712Query::mstScdCheck($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["COLLECT_LMS_CD"] != $row["MST_EXP_SCD"]) {
                    $mst_flg = true;
                    break;
                }
            }
            $result->free();
        }

        //学年コンボで全学年が選択されていなかったら、前年度からコピーボタンを押し不可
        $disabled = $model->grade != "99" ? "disabled" : "";
        $extra = " $disabled onclick=\"btn_submit('copy');\"";
        $arg["copy_btn"] = knjCreateBtn($objForm, "copy_btn", "前年度からコピー", $extra);

        //会計グループリスト取得
        $query = knjmp712Query::SelectQuery($model);
        $result = $db->query($query);
        $this->DataRow = array();
        $dataCnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            if ($dataCnt == 0) $cd[] = $row["COLLECT_GRP_CD"];

            //会計グループが同じ間は表示しない
            if (in_array($row["COLLECT_GRP_CD"], $cd)) {
                $this->setDataRow($row);
            } else {
                $this->ModifyDataRow();
                $arg["data"][] = $this->DataRow;
                $cd = $this->DataRow = array();
                $cd[] = $row["COLLECT_GRP_CD"];
                $this->setDataRow($row);
            }
            $dataCnt++;
        }
        $this->ModifyDataRow();
        if (isset($this->DataRow["COLLECT_GRP_CD"])) {
            $arg["data"][] = $this->DataRow;
        }

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $result->free();
        Query::dbCheckIn($db);
        
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_flg",
                            "value"     => $year_flg) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "money_flg",
                            "value"     => $money_flg) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "mst_flg",
                            "value"     => $mst_flg) );
        
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjmp712Form1.html", $arg);        
    }
    
    //表示用配列にデータ値をセット
    function SetDataRow($row)
    {
        $this->DataRow["COLLECT_GRP_CD"][] = View::alink("knjmp712index.php", $row["COLLECT_GRP_CD"].":".$row["COLLECT_GRP_NAME"], "target=right_frame",
                                             array("COLLECT_GRP_CD" => $row["COLLECT_GRP_CD"],
                                                   "GRADE2"         => $row["GRADE"],
                                                   "cmd"            => "edit",
                                                   "NAME"           => $row["COLLECT_GRP_NAME"]));

        $this->DataRow["HR_NAME"][]    = $row["HR_NAME"];
        if (strlen($row["COLLECT_M_CD"]))  $this->DataRow["COLLECT_M_CD"][] = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].":".$row["COLLECT_M_NAME"];
        if (strlen($row["COLLECT_S_CD"]))  $this->DataRow["COLLECT_S_CD"][] = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$row["COLLECT_S_CD"].":".$row["COLLECT_S_NAME"];
    }
    
    //表示用配列を表示できるように修正
    function ModifyDataRow()
    {
        if (get_count($this->DataRow["COLLECT_GRP_CD"]))  $this->DataRow["COLLECT_GRP_CD"]  = implode("<BR>",array_unique($this->DataRow["COLLECT_GRP_CD"]));
        if (get_count($this->DataRow["HR_NAME"]))     $this->DataRow["HR_NAME"]     = implode("<BR>",array_unique($this->DataRow["HR_NAME"]));
        if (get_count($this->DataRow["COLLECT_M_CD"]))    $this->DataRow["COLLECT_M_CD"]    = implode("<BR>",array_unique($this->DataRow["COLLECT_M_CD"]));
        if (get_count($this->DataRow["COLLECT_S_CD"]))    $this->DataRow["COLLECT_S_CD"]    = implode("<BR>",array_unique($this->DataRow["COLLECT_S_CD"]));
    }
}
?>
