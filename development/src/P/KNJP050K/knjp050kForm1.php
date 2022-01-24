<?php

require_once('for_php7.php');

class knjp050kForm1
{
    var $dataRow = array(); //表示用一行分データをセット

    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp050kindex.php", "", "edit");
        
        $arg["YEAR"] = $model->year;

        //コピー用今年度データ存在チェック１
        $money_flg  = knjp050kQuery::selectYearQuery($model->year, "money_paid_m_dat");
        $money_flg += knjp050kQuery::selectYearQuery($model->year, "money_paid_s_dat");

        //コピー用今年度データ存在チェック２
        $year_flg  = knjp050kQuery::selectYearQuery($model->year, "expense_grp_mst");
        $year_flg += knjp050kQuery::selectYearQuery($model->year, "expense_grp_hr_dat");
        $year_flg += knjp050kQuery::selectYearQuery($model->year, "expense_grp_m_dat");
        $year_flg += knjp050kQuery::selectYearQuery($model->year, "expense_grp_s_dat");
        $year_flg += knjp050kQuery::selectYearQuery($model->year, "money_due_m_dat");
        $year_flg += knjp050kQuery::selectYearQuery($model->year, "money_due_s_dat");

        $db = Query::dbCheckOut();

        //コピー用マスタチェック(前年度に登録されているクラスが今年度に存在するか)
        $mst_flg = "";
        $result = $db->query(knjp050kQuery::mstClassCheck($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["HR_CLASS"] != $row["MST_HR_CLASS"]) {
                $mst_flg = true;
                break;
            }
        }
        $result->free();

        //コピー用マスタチェック(前年度に登録されている中分類が今年度に存在するか)
        if ($mst_flg == "") {
            $result = $db->query(knjp050kQuery::mstMcdCheck($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($row["EXPENSE_M_CD"] != $row["MST_EXP_MCD"]) {
                    $mst_flg = true;
                    break;
                }
            }
            $result->free();
        }

        //コピー用マスタチェック(前年度に登録されている小分類が今年度に存在するか)
        if ($mst_flg == "") {
            $result = $db->query(knjp050kQuery::mstScdCheck($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($row["EXPENSE_S_CD"] != $row["MST_EXP_SCD"]) {
                    $mst_flg = true;
                    break;
                }
            }
            $result->free();
        }


        //学年コンボボックス
        $opt_grade = array();
        $opt_grade[] = array("label" => "", "value" => "00");                 //空リスト設定
        
        $result = $db->query(knjp050kQuery::get_Grade());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_grade[] = array("label" => $row["GRADE"], "value" => $row["GRADE"]);
        }
        $opt_grade[] = array("label" => "全学年", "value" => "99");

        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE",
                            "extrahtml"   => "onChange=\"btn_submit('change')\"",
                            "value"       => $model->grade,
                            "options"     => $opt_grade
                            ));
        $arg["GRADE"] = $objForm->ge("GRADE");

        //学年コンボで全学年が選択されていなかったら、前年度からコピーボタンを押し不可
        if ($model->grade != "99") {
            $disabled = "disabled";
        }

        $objForm->ae( array("type"        => "button",
                            "name"        => "copy_btn",
                            "value"       => "前年度からコピー",
                            "extrahtml"   => " $disabled onclick=\"btn_submit('copy');\"" ));
        $arg["copy_btn"] = $objForm->ge("copy_btn");
        

        //費目グループリスト取得
        $result = $db->query(knjp050kQuery::SelectQuery($model));
        $this->DataRow = array();
        for ($i=0; $row=$result->fetchRow(DB_FETCHMODE_ASSOC); $i++)
        {
            array_walk($row, "htmlspecialchars_array");

            if ($i == 0) $cd[] = $row["EXPENSE_GRP_CD"];

            //費目グループが同じ間は表示しない
            if (in_array($row["EXPENSE_GRP_CD"], $cd)) {
            
                $this->setDataRow($row);
                
            } else {
                
                $this->ModifyDataRow();
                $arg["data"][] = $this->DataRow;

                $cd = $this->DataRow = array();
                $cd[] = $row["EXPENSE_GRP_CD"];
                $this->setDataRow($row);
            }
        }
        $this->ModifyDataRow();
        if (isset($this->DataRow["EXPENSE_GRP_CD"])) {
            $arg["data"][] = $this->DataRow;
        }
        
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        
#        if ($model->cmd == "change") {
#            $arg["jscript"] = "window.open('knjp050kindex.php?cmd=edit','right_frame')";
#        }
        
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
        View::toHTML($model, "knjp050kForm1.html", $arg);        
    }
    
    //表示用配列にデータ値をセット
    function SetDataRow($row)
    {
        $this->DataRow["EXPENSE_GRP_CD"][] = View::alink("knjp050kindex.php", $row["EXPENSE_GRP_CD"].":".$row["EXPENSE_GRP_NAME"], "target=right_frame",
                                             array("EXPENSE_GRP_CD" => $row["EXPENSE_GRP_CD"],
                                                   "GRADE2"         => $row["GRADE"],
                                                   "cmd"            => "edit",
                                                   "NAME"           => $row["EXPENSE_GRP_NAME"]));
               
        $this->DataRow["HR_NAMEABBV"][]    = $row["HR_NAMEABBV"];
        if (strlen($row["EXPENSE_M_CD"]))  $this->DataRow["EXPENSE_M_CD"][] = $row["EXPENSE_M_CD"].":".$row["EXPENSE_M_NAME"];
        if (strlen($row["EXPENSE_S_CD"]))  $this->DataRow["EXPENSE_S_CD"][] = $row["EXPENSE_S_CD"].":".$row["EXPENSE_S_NAME"];
    }
    
    //表示用配列を表示できるように修正
    function ModifyDataRow()
    {
        if (get_count($this->DataRow["EXPENSE_GRP_CD"]))  $this->DataRow["EXPENSE_GRP_CD"]  = implode("<BR>",array_unique($this->DataRow["EXPENSE_GRP_CD"]));
        if (get_count($this->DataRow["HR_NAMEABBV"]))     $this->DataRow["HR_NAMEABBV"]     = implode("<BR>",array_unique($this->DataRow["HR_NAMEABBV"]));
        if (get_count($this->DataRow["EXPENSE_M_CD"]))    $this->DataRow["EXPENSE_M_CD"]    = implode("<BR>",array_unique($this->DataRow["EXPENSE_M_CD"]));
        if (get_count($this->DataRow["EXPENSE_S_CD"]))    $this->DataRow["EXPENSE_S_CD"]    = implode("<BR>",array_unique($this->DataRow["EXPENSE_S_CD"]));
    }
}
?>
