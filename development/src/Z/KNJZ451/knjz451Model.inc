<?php

require_once('for_php7.php');

class knjz451Model extends Model {
    var $field      = array();
    var $cmd;
    var $year;
    var $grade;
    var $groupcd;
    var $selectdata;

    function knjz451Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("QUALIFIED_CD" =>  VARS::post("QUALIFIED_CD"),
                             "LIMIT_MONTH"  =>  VARS::post("LIMIT_MONTH"),
                             "SETUP_CNT"    =>  VARS::post("SETUP_CNT")
                             );
        //初期値
        $this->field["GROUP_SELECT"] = ($this->field["GROUP_SELECT"] == "") ? "1" : $this->field["GROUP_SELECT"];

        //プロパティファイル
        $this->getPropertiesAll();

        unset($this->selectdata);

        if (VARS::post("selectdata") != NULL) {
            $this->selectdata = explode(',', VARS::post("selectdata"));
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }

        if ($this->cmd == '') {
            unset($this->year);
            unset($this->qualifiedCd);
        }

        if (VARS::get("SEND_FLG") == "1"){
            $this->qualifiedCd   = VARS::get("QUALIFIED_CD");
            $this->field["QUALIFIED_CD"] = $this->qualifiedCd;
        }
    }

    //入力項目チェック
    function validateInput() {

        $db = Query::dbCheckOut();

        //前年度のデータをコピーの時
        if($this->cmd =="copy"){

            $year = $this->year - 1;
            $query = knjz451Query::cntQualifiedHdat($year);
            $count = $db->getOne($query);
            if ($count == 0) {
                $this->setMessage("MSG203", "更新対象がありませんでした。");
                return false;
            }

            $year = $this->year;
            $query = knjz451Query::cntQualifiedHdat($year);
            $count = $db->getOne($query);
            if($count > "0"){
                $this->setMessage("MSG203", "既にデータが存在します。");
                return false;
            }

        } else {
            $year = $this->year;
            $query = knjz451Query::cntQualifiedHdat($year);
            $count = $db->getOne($query);

            //文字数チェック
            if (strlen($this->field["LIMIT_MONTH"]) > 2) {
                $this->setWarning("MSG915", "(取得期限) 2バイトまでです。");
                return false;
            } elseif (strlen($this->field["SETUP_CNT"]) > 2) {
                $this->setWarning("MSG915", "(取得個数) 2バイトまでです。");
                return false;
            }
        }

        Query::dbCheckIn($db);

        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->insert();
            $this->qualifiedCd = $this->field["QUALIFIED_CD"];
            $this->setMessage("MSG201"); //データを更新しました。
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()) {
            $this->delete();
            unset($this->qualifiedCd);
            unset($this->field);
            unset($this->selectdata);
            $this->setMessage("MSG201");
        }
    }

    /* DELETE */
    function &delete() {
        knjz451Query::getDeleteDatQuery($this);
        knjz451Query::getDeleteMstQuery($this);
    }

    /* INSERT */
    function &insert() {
        knjz451Query::getInsertMstQuery($this);
        foreach ($this->selectdata as $qualifiedCd) {
            knjz451Query::getInsertDatQuery($this, $qualifiedCd);
        }
    }

    /**********/
    /* コピー */
    /**********/
    //コピーボタン押し時の処理
    function &getInsertCopyModel() {
        //入力チェック
        if($this->validateInput()) {
            knjz451Query::getInsertCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
