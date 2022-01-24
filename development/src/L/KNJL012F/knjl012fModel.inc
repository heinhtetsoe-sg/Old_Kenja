<?php

require_once('for_php7.php');

class knjl012fModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $selectdata;
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $fsCd;
    var $examno1Keta;
    var $examno2Keta;//受験番号の左から2桁目「組コード昇順の行番号（例．001→1、003→2、004→3）」

    /**
     * メンバ変数の初期化
     */
    function knjl012fModel() {
    }

    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TESTDIV0"         =>  VARS::post("TESTDIV0"),         //入試回数
                             "GRADE_HRCLASS"    =>  VARS::post("GRADE_HRCLASS"),    //年組
                             "RECEPTDATE"       =>  VARS::post("RECEPTDATE"),       //受付日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
        //出身学校コードを取得
        $rtnRow = knjl012fQuery::getNameMstA023();
        $this->fsCd = strlen($rtnRow["NAMESPARE1"]) ? $rtnRow["NAMESPARE1"] : "7070101";
        $this->examno1Keta = "9";
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }

    //入力項目チェック
    function validateInput() {
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (!($this->field["TESTDIV0"])) {
            $this->setWarning("MSG301", "( 入試回数 )");
            return false;
        }
        if (!($this->field["RECEPTDATE"])) {
            $this->setWarning("MSG301", "( 受付日付 )");
            return false;
        }

        return true;
    }

    //実行処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjl012fQuery::getUpdateQuery($this);
            $this->setMessage("MSG202");
        }
        return;
    }
}
?>
