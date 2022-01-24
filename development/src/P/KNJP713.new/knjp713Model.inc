<?php

require_once('for_php7.php');

class knjp713Model extends Model {

    var $cmd;
    var $exp_grade;
    var $selectExp_grade;
    var $exp_majorCd;
    var $selectExp_majorCd;
    var $exp_courseCode;
    var $selectExp_courseCode;
    var $exp_grpcd;
    var $selectExp_grpcd;
    var $name;
    var $field = array();
    var $taisyouYear;
    var $taisyouGrade;

    function knjp713Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID") || $this->cmd == "changeYear") {
            unset($this->taisyouYear);
            unset($this->taisyouGrade);
            unset($this->exp_grade);
            unset($this->exp_majorCd);
            unset($this->exp_courseCode);
            unset($this->exp_grpcd);
            unset($this->selectExp_grade);
            unset($this->selectExp_majorCd);
            unset($this->selectExp_courseCode);
            unset($this->selectExp_grpcd);
            unset($this->name);
        }

        //年度
        $this->taisyouYear = (VARS::request("TAISYOU_YEAR")) ? VARS::request("TAISYOU_YEAR") : $this->taisyouYear;
        $this->taisyouYear = ($this->taisyouYear) ? $this->taisyouYear : CTRL_YEAR;
        $this->taisyouGrade = (VARS::request("TAISYOU_GRADE")) ? VARS::request("TAISYOU_GRADE") : $this->taisyouGrade;

        //左画面から
        if (VARS::get("COLLECT_GRP_CD")) {
            $this->exp_grade = VARS::get("GRADE");
            $this->exp_majorCd = VARS::get("MAJORCD");
            $this->exp_courseCode = VARS::get("COURSECODE");
            $this->exp_grpcd = sprintf("%04d",VARS::get("COLLECT_GRP_CD"));
            $this->selectExp_grade= VARS::get("GRADE");
            $this->selectExp_majorCd = VARS::get("MAJORCD");
            $this->selectExp_courseCode = VARS::get("COURSECODE");
            $this->selectExp_grpcd = sprintf("%04d",VARS::get("COLLECT_GRP_CD"));
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //右画面から
        $mid ="" ;
        //コードのゼロ埋め
        if (VARS::post("COLLECT_GRP_CD")) {
            $mid = sprintf("%02d",VARS::post("COLLECT_GRP_CD"));
        }
        if ($this->cmd == "grpChange") {
            $this->exp_grade = VARS::post("GRADE");
            $this->exp_majorCd = VARS::post("MAJORCD");
            $this->exp_courseCode = VARS::post("COURSECODE");
            $this->exp_grpcd = VARS::post("COLLECT_L_CD").$mid;
        }
        $this->field = array("GRADE"                => VARS::post("GRADE"),
                             "MAJORCD"              => VARS::post("MAJORCD"),
                             "COURSECODE"           => VARS::post("COURSECODE"),
                             "COLLECT_GRP_CD"       => VARS::post("COLLECT_L_CD").$mid,
                             "COLLECT_GRP_NAME"     => VARS::post("COLLECT_GRP_NAME"),
                             "COLLECT_M_CD"         => VARS::post("selectdata2"),
                             "COLLECT_S_CD"         => VARS::post("selectdata3"));
        list($setCourserCd, $setMajorCd) = explode("-", $this->field["MAJORCD"]);
        $this->field["SET_COURSECD"] = $setCourserCd;
        $this->field["SET_MAJORCD"] = $setMajorCd;
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
        if(($kind=="update" || $kind=="delete") &&
            $this->selectExp_grade != $this->field["GRADE"] &&
            $this->selectExp_majorCd != $this->field["MAJORCD"] &&
            $this->selectExp_courseCode != $this->field["COURSECODE"] &&
            $this->selectExp_grpcd != $this->field["COLLECT_GRP_CD"]
        ){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //追加時の重複チェック
        $db = Query::dbCheckOut();
        $query = knjp713Query::getRow($this, 2);
        $row = $db->getRow($query);
        if ($kind == "add" && is_array($row)) {
            $this->setWarning("MSG918");
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

            knjp713Query::getDeleteQuery($this, $db);
            $collectMSexist = knjp713Query::getUpdateQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->exp_grade = $this->field["GRADE"];
            $this->exp_majorCd = $this->field["MAJORCD"];
            $this->exp_courseCode = $this->field["COURSECODE"];
            $this->exp_grpcd = sprintf("%04d", $this->field["COLLECT_GRP_CD"]);
            $this->name = $this->field["COLLECT_GRP_NAME"];

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

            knjp713Query::getDeleteQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->exp_grade = "";
            $this->exp_majorCd = "";
            $this->exp_courseCode = "";
            $this->exp_grpcd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        //前年度データ存在チェック
        $exist_data  = knjp713Query::selectYearQuery($this->taisyouYear - 1, $this->taisyouGrade, "COLLECT_GRP_MST");

        if($exist_data == 0){
            $this->setWarning('MSG203', "前年度のデータは存在しません。");
            return false;
        } else{
            knjp713Query::getCopyYearQuery($this);
            $this->setMessage("MSG201");
        }

        return;
    }
}
?>
