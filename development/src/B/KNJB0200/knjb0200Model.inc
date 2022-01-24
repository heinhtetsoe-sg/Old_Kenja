<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb0200Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $select_data = array();
    var $selectleft;
    var $joukenField = array();
    var $accIndex;
    var $panelCnt;
    var $mainSearch;

    /**
     * メンバ変数の初期化
     */
    function knjb0200Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DBNAME"           =>  VARS::post("DBNAME"));          //DB変更に備えて

        $this->setSubclassCd = VARS::post("SET_SUBCLASSCD");
        $this->setGrade = VARS::post("SET_GRADE");

        $this->selectleft = VARS::post("selectleft");
        $this->data_div = VARS::post("DATA_DIV");
        $this->type_div = VARS::post("TYPE_DIV");
        $this->cmd = VARS::request("cmd");

        $this->joukenCnt = 8;

        $this->joukenField = array();
        for ($i = 1; $i <= $this->joukenCnt; $i++) {
            $this->joukenField["JOUKEN{$i}_HIDEEN"] = VARS::post("JOUKEN{$i}_HIDEEN");
            $this->joukenField["UPD{$i}_HIDEEN"] = VARS::post("UPD{$i}_HIDEEN");
        }

        $this->heikouTouroku = array();
        if (is_array($this->subclassArray)) {
            foreach ($this->subclassArray as $subclassCd => $val) {
                $setHeikou = VARS::post("HEIKOU_FLG".$subclassCd);
                $setCredit = VARS::post("CREDITS".$subclassCd);
                $this->heikouTouroku[$subclassCd] = $this->type_div == "1" ? $setHeikou : $setCredit;
            }
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $setData = array();
        for ($i = 1; $i <= $this->joukenCnt; $i++) {
            if ($this->joukenField["JOUKEN{$i}_HIDEEN"]) {
                $setData[$i]["CONDITION"] = $this->joukenField["UPD{$i}_HIDEEN"];
                $setData[$i]["CONDITION_NUM"] = $this->joukenField["JOUKEN{$i}_HIDEEN"];
            }
        }
        //入力チェック
        if ($this->validateInput()){
            //メイン
            $query = knjb0200Query::getDeleteQuery($this);
            $db->query($query);
            knjb0200Query::getUpdateQuery($db, $this, $setData);
            //科目リスト
            $query = knjb0200Query::getDeleteSubQuery($this);
            $db->query($query);
            knjb0200Query::getUpdateSubQuery($db, $this);
        }
        $db->commit(); // トランザクションをコミットする。
        $this->setMessage("MSG201");
        $this->joukenField = array();
        Query::dbCheckIn($db);
    }

    // 編集完了用のモデルを返します
    function &getUpdateHeikouModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //科目リスト
        knjb0200Query::getUpdateHeikouFlgQuery($db, $this);

        $db->commit(); // トランザクションをコミットする。
        $this->setMessage("MSG201");
        $this->joukenField = array();
        Query::dbCheckIn($db);
    }

    // 削除のモデルを返します
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //メイン
        $query = knjb0200Query::getDeleteQuery($this);
        $db->query($query);
        //科目リスト
        $query = knjb0200Query::getDeleteSubQuery($this);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        $this->setMessage("MSG201");
        $this->joukenField = array();
        Query::dbCheckIn($db);
    }

    //次年度コピー用のモデルを返します
    function &getCopyModel()
    {
        $cntselected = knjb0200Query::cntThisOrder($this);
        $cntlastorder = knjb0200Query::cntLastOrder($this);
        if($cntselected > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntlastorder == 0){
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjb0200Query::getCopyQuery($this);
            if ( !isset($model->warning)){
                $this->setWarning('MSG201',"（前年度からコピー）");
            }
            return $result;
        }
    }
}
?>
