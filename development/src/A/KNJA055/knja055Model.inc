<?php

require_once('for_php7.php');


class knja055Model extends Model {

    var $cmd;
    var $Year;
    var $Sdate;
    var $Edate;
    var $schno;

    function knja055Model()
    {
    }

    function init()
    {
        $this->Year = VARS::post("YEAR");
        //DB接続
        $db = Query::dbCheckOut();
        $result = $db->query(knja055Query::getSch_Information(CTRL_YEAR));
        $row    = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $this->foundedyear  = $row["FOUNDE"];                    //創立年度
        $this->term         = (int)CTRL_YEAR - $this->foundedyear - 1;
        //DB切断
        Query::dbCheckIn($db);
        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新処理チェック
    function validateInput()
    {

        //設定処理年度で、学籍基礎マスタに卒業生がいるかをチェック
        $db = Query::dbCheckOut();
        $grd_student = $db->getOne(knja055Query::get_grd_student($this));
        Query::dbCheckIn($db);

        if ($grd_student == '0') {
            $this->setWarning("MSG303","移行する生徒がいません。");
            return false; 
        }

        return true;
    }

    //実行
    function &getUpdateModel()
    {
        //更新処理チェック
        if ($this->validateInput("execute")) {
            $sccnt = 0;
            $db = Query::dbCheckOut();

            $query = knja055Query::get_grd_schno($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($sccnt == 0) {
                    $this->schno  = "(";
                } else {
                    $this->schno .= ",";
                }
                $this->schno .= "'".$row["SCHREGNO"]."'";
                $sccnt++;
            }
            $this->schno .= ")";
            $result->free();
            Query::dbCheckIn($db);
            if (knja055Query::getUpdateQuery($this)) {
                $this->setMessage("MSG201");
            }
        }
    }

}
?>
