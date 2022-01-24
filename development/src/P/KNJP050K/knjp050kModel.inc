<?php

require_once('for_php7.php');

class knjp050kModel extends Model {

    var $cmd;
    var $exp_grpcd;
    var $name;
    var $field = array();
    var $year;
    var $grade;
    var $grade2;
    var $money_change_flg;
    
    function knjp050kModel()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_grpcd);
            unset($this->name);
            unset($this->grade);
            unset($this->grade2);            
            unset($this->money_change_flg);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        
        if (VARS::post("GRADE")) {
            $this->grade = VARS::post("GRADE");
        }
        if (VARS::get("EXPENSE_GRP_CD")) {
            $this->exp_grpcd = sprintf("%04d",VARS::get("EXPENSE_GRP_CD"));
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        if (VARS::request("GRADE2")) {
            $this->grade2 = VARS::request("GRADE2");
        }

        $this->field = array("EXPENSE_GRP_CD"   => VARS::post("EXPENSE_GRP_CD"),
                             "EXPENSE_GRP_NAME" => VARS::post("EXPENSE_GRP_NAME"),
                             "GRADE"            => VARS::post("GRADE2"),
                             "CLASS"            => VARS::post("selectdata"),
                             "EXPENSE_M_CD"     => VARS::post("selectdata2"),
                             "EXPENSE_S_CD"     => VARS::post("selectdata3"));
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["EXPENSE_GRP_CD"]) == 0){
            $this->setWarning("MSG301","(費目グループコード)");
            return false;
        }
        if(strlen($this->field["EXPENSE_GRP_NAME"]) == 0){
            $this->setWarning("MSG301","(費目グループ名称)");
            return false;
        }
        if(strlen($this->field["GRADE"]) == 0){
            $this->setWarning("MSG301","(学年)");
            return false;
        }
        if(($kind=="update" || $kind=="delete") && $this->exp_grpcd != $this->field["EXPENSE_GRP_CD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        //追加時の重複チェック
        $row = knjp050kQuery::getRow($this,2);
        if ($kind == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }        
        //HRクラスの重複チェック
        $row = knjp050kQuery::RepetCheck($this);
        if ($row == false) {
            $this->setWarning("MSG302","( クラス )");
            return false;
        }        
#        //入金済みデータの存在チェック
#        $row = knjp050kQuery::CheckMoney($this);
#        if ($row == false) {
#            $this->setWarning("MSG203","入金済データが存在します。");
#            return false;
#        }
        //文字数チェック
        if (strlen($this->field["EXPENSE_GRP_NAME"]) > 60){
             $this->setWarning("MSG915","(費目グループ名称は60バイトまでです。)");
             return false;        
        }
        return true;
    }


    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput($this->cmd)){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            
            knjp050kQuery::getDeleteQuery($this,$db);
            knjp050kQuery::getUpdateQuery($this,$db);
            
            $db->commit();
            Query::dbCheckIn($db);

            $this->exp_grpcd = sprintf("%04d",$this->field["EXPENSE_GRP_CD"]);
            $this->name = $this->field["EXPENSE_GRP_NAME"];
            $this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput($this->cmd)){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knjp050kQuery::getDeleteQuery($this,$db);

            $db->commit();
            Query::dbCheckIn($db);
            
            $this->exp_grpcd = "";
            $this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        //前年度データ存在チェック
        $exist_data  = knjp050kQuery::selectYearQuery($this->year -1, "expense_grp_mst");
        $exist_data += knjp050kQuery::selectYearQuery($this->year -1 , "expense_grp_hr_dat");
        $exist_data += knjp050kQuery::selectYearQuery($this->year -1, "expense_grp_m_dat");
        $exist_data += knjp050kQuery::selectYearQuery($this->year -1, "expense_grp_s_dat");
        $exist_data += knjp050kQuery::selectYearQuery($this->year -1, "money_due_m_dat");
        $exist_data += knjp050kQuery::selectYearQuery($this->year -1, "money_due_s_dat");

        if($exist_data == 0){
            $this->setWarning('MSG203', "前年度のデータは存在しません。");
            return false;
        } else{
            knjp050kQuery::getCopyYearQuery($this->year);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
