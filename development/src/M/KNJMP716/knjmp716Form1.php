<?php

require_once('for_php7.php');

class knjmp716Form1
{
    var $dataRow = array(); //表示用一行分データをセット

    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjmp716index.php", "", "edit");

        $arg["YEAR"] = CTRL_YEAR;

        $db = Query::dbCheckOut();

        $model->leftSetYear = CTRL_YEAR;
        Query::dbCheckIn($db);

        //コピー用データチェック 入金
        $money_flg  = knjmp716Query::selectYearQuery($model->leftSetYear, "COLLECT_MONEY_PAID_M_DAT");
        $money_flg += knjmp716Query::selectYearQuery($model->leftSetYear, "COLLECT_MONEY_PAID_S_DAT");

        //コピー用データチェック 入金
        $money_flg += knjmp716Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_MONEY_PAID_M_DAT");
        $money_flg += knjmp716Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_MONEY_PAID_S_DAT");

        //コピー用データチェック 登録
        $year_flg  = knjmp716Query::selectYearQuery($model->leftSetYear, "COLLECT_GRP_MST");
        $year_flg += knjmp716Query::selectYearQuery($model->leftSetYear, "COLLECT_GRP_HR_DAT");
        $year_flg += knjmp716Query::selectYearQuery($model->leftSetYear, "COLLECT_GRP_DAT");
        $year_flg += knjmp716Query::selectYearQuery($model->leftSetYear, "COLLECT_MONEY_DUE_M_DAT");
        $year_flg += knjmp716Query::selectYearQuery($model->leftSetYear, "COLLECT_MONEY_DUE_S_DAT");

        //コピー用データチェック 登録
        $year_flg += knjmp716Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_GRP_MST");
        $year_flg += knjmp716Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_GRP_HR_DAT");
        $year_flg += knjmp716Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_GRP_DAT");
        $year_flg += knjmp716Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_MONEY_DUE_M_DAT");
        $year_flg += knjmp716Query::selectYearQuery($model->leftSetYear + 1, "COLLECT_MONEY_DUE_S_DAT");

        $db = Query::dbCheckOut();

        //コピー用マスタチェック(前年度に登録されているクラスが今年度に存在するか)
        $mst_flg = "";
        $result = $db->query(knjmp716Query::mstClassCheck($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["HR_CLASS"] != $row["MST_HR_CLASS"]) {
                $mst_flg = true;
                break;
            }
        }
        $result->free();

        //コピー用マスタチェック(前年度に登録されている項目が今年度に存在するか)
        if (!$mst_flg) {
            $result = $db->query(knjmp716Query::mstMcdCheck($model));
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
            $result = $db->query(knjmp716Query::mstScdCheck($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["COLLECT_LMS_CD"] != $row["MST_EXP_SCD"]) {
                    $mst_flg = true;
                    break;
                }
            }
            $result->free();
        }

        //会計グループリスト取得
        $query = knjmp716Query::SelectQuery($model);
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

        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year_flg", $year_flg);
        knjCreateHidden($objForm, "money_flg", $money_flg);
        knjCreateHidden($objForm, "mst_flg", $mst_flg);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjmp716Form1.html", $arg);        
    }

    //表示用配列にデータ値をセット
    function SetDataRow($row)
    {
        $this->DataRow["COLLECT_GRP_CD"][] = View::alink("knjmp716index.php", $row["COLLECT_GRP_CD"].":".$row["COLLECT_GRP_NAME"], "target=right_frame",
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
