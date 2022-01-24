<?php

require_once('for_php7.php');

class knjm712Model extends Model {

    var $cmd;
    var $exp_grpcd;
    var $selectExp_grpcd;
    var $name;
    var $field = array();
    var $year;
    var $grade;
    var $grade2;
    
    function knjm712Model()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_grpcd);
            unset($this->selectExp_grpcd);
            unset($this->name);
            unset($this->grade);
            unset($this->grade2);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::post("GRADE")) {
            $this->grade = VARS::post("GRADE");
        }
        if (VARS::get("COLLECT_GRP_CD")) {
            $this->exp_grpcd = sprintf("%04d",VARS::get("COLLECT_GRP_CD"));
            $this->selectExp_grpcd = sprintf("%04d",VARS::get("COLLECT_GRP_CD"));
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        if (VARS::request("GRADE2")) {
            $this->grade2 = VARS::request("GRADE2");
        }
        if ($this->cmd == "grpChange") {
            $this->exp_grpcd = sprintf("%04d",VARS::post("COLLECT_GRP_CD"));
        }
        $this->field = array("COLLECT_GRP_CD"   => VARS::post("COLLECT_GRP_CD"),
                             "COLLECT_GRP_NAME" => VARS::post("COLLECT_GRP_NAME"),
                             "GRADE"            => VARS::post("GRADE2"),
                             "CLASS"            => VARS::post("selectdata"),
                             "COLLECT_M_CD"     => VARS::post("selectdata2"),
                             "COLLECT_S_CD"     => VARS::post("selectdata3"));
        $this->taisyouYear = $this->grade2 != "00" && strlen($this->grade2) > 0 ? CTRL_YEAR : (CTRL_YEAR + 1);
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["COLLECT_GRP_CD"]) == 0){
            $this->setWarning("MSG301","(会計グループコード)");
            return false;
        }
        if(strlen($this->field["COLLECT_GRP_NAME"]) == 0){
            $this->setWarning("MSG301","(会計グループ名称)");
            return false;
        }
        if(strlen($this->field["GRADE"]) == 0){
            $this->setWarning("MSG301","(学年)");
            return false;
        }
        if(($kind=="update" || $kind=="delete") && $this->selectExp_grpcd != $this->field["COLLECT_GRP_CD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //追加時の重複チェック
        $db = Query::dbCheckOut();
        $query = knjm712Query::getRow($this, 2);
        $row = $db->getRow($query);
        if ($kind == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }

        //入金済みデータの件数
        $paid_money  = $db->getOne(knjm712Query::getPaidMoney($this->taisyouYear, "COLLECT_MONEY_PAID_M_DAT", $this->field["COLLECT_GRP_CD"]));
        $paid_money += $db->getOne(knjm712Query::getPaidMoney($this->taisyouYear, "COLLECT_MONEY_PAID_S_DAT", $this->field["COLLECT_GRP_CD"]));
        if ($paid_money > 0) {
            $this->setWarning("入金済みデータが存在します。");
            return false;
        }
        Query::dbCheckIn($db);

        //文字数チェック
        if (strlen($this->field["COLLECT_GRP_NAME"]) > 60){
             $this->setWarning("MSG915","(会計グループ名称は60バイトまでです。)");
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

            knjm712Query::getDeleteQuery($this, $db);
            $collectMSexist = knjm712Query::getUpdateQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->exp_grpcd = sprintf("%04d", $this->field["COLLECT_GRP_CD"]);
            $this->name = $this->field["COLLECT_GRP_NAME"];
            $this->grade = $this->field["GRADE"];

            $addMsg = "";
            foreach ($collectMSexist as $key => $val) {
                if ($val == "0") {
                    $addMsg = "\\n会計細目が設定されていません。";
                    break;
                }
            }
            $this->setMessage("MSG201", $addMsg);
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

            knjm712Query::getDeleteQuery($this, $db);

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
        $exist_data  = knjm712Query::selectYearQuery($this->leftSetYear, "COLLECT_GRP_MST");
        $exist_data += knjm712Query::selectYearQuery($this->leftSetYear, "COLLECT_GRP_HR_DAT");
        $exist_data += knjm712Query::selectYearQuery($this->leftSetYear, "COLLECT_MONEY_DUE_M_DAT");
        $exist_data += knjm712Query::selectYearQuery($this->leftSetYear, "COLLECT_MONEY_DUE_S_DAT");

        if($exist_data == 0){
            $this->setWarning('MSG203', "今年度の新入生データは存在しません。");
            return false;
        } else{
            knjm712Query::getCopyYearQuery($this, "SINNYU");
            $this->setMessage("MSG201");
        }

        //前年度データ存在チェック
        $exist_data  = knjm712Query::selectYearQuery($this->leftSetYear - 1, "COLLECT_GRP_MST");
        $exist_data += knjm712Query::selectYearQuery($this->leftSetYear - 1, "COLLECT_GRP_HR_DAT");
        $exist_data += knjm712Query::selectYearQuery($this->leftSetYear - 1, "COLLECT_MONEY_DUE_M_DAT");
        $exist_data += knjm712Query::selectYearQuery($this->leftSetYear - 1, "COLLECT_MONEY_DUE_S_DAT");

        if($exist_data == 0){
            $this->setWarning('MSG203', "前年度のデータは存在しません。");
            return false;
        } else{
            knjm712Query::getCopyYearQuery($this, "ZAISEKI");
            $this->setMessage("MSG201");
        }

        return;
    }
}
?>
