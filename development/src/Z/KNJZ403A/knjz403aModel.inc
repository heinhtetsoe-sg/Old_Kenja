<?php

require_once('for_php7.php');


class knjz403aModel extends Model {
    var $field      = array();
    var $cmd;
    var $grade;
    var $lCd;
    var $mCd;
    var $schoolKind;

    function knjz403aModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            $this->grade = "";
            $this->lCd = "";
            $this->mCd = "";
            $this->schoolKind = "";
        }
    
        $this->field = array("GRADE"      =>  VARS::post("GRADE"),
                             "L_CD"       =>  VARS::post("L_CD"),
                             "L_NAME"     =>  VARS::post("L_NAME"),
                             "UPDATED"    =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        $this->mCnt = VARS::post("mCnt");
        $this->mMstArray = array();
        for ($i = 1; $i <= $this->mCnt; $i++) {
            $postMcd = VARS::post("M_CD".$i);
            $postMname = VARS::post("M_NAME".$i);
            if ($postMcd != "") {
                $postMcd = sprintf("%02d", $postMcd);
            }
            $this->mMstArray[$i]["M_CD"] = $postMcd;
            $this->mMstArray[$i]["M_NAME"] = $postMname;
        }

        //リンク用から取得
        if (VARS::get("SCHOOL_KIND")!="") {
            $this->schoolKind = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("GRADE")!="") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("L_CD")!="") {
            $this->lCd = VARS::get("L_CD");
        }
        if (VARS::get("M_CD")!="") {
            $this->mCd = VARS::get("M_CD");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["L_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(観点コード)");
            return false;
        }

        if ($this->lCd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }

        list($schoolKind, $grade) = preg_split("/-/", $this->field["GRADE"]);
        if ($this->cmd == "update" && ($this->lCd != $this->field["L_CD"] && $this->grade != $grade && $this->schoolKind != $schoolKind)) {
            $this->setWarning("MSG308");
            return;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //１レコード取得
        $query = knjz403aQuery::getChkSelectOne($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(観点コード)");
            return false;
        }

        //文字数チェック    
        if (strlen($this->field["L_NAME"]) > 30 ) {
            $this->setWarning("MSG914", "(観点名称) 30バイトまで");
            return false;
        } 

        //文字数チェック    
        if (strlen($this->field["L_NAME"]) <= 0 ) {
            $this->setWarning("MSG301", "(観点名称)");
            return false;
        }

        $mDataFlg = false;
        $befKeyArray = array();
        foreach ($this->mMstArray as $soeji => $mArray) {
            $mKey = $mArray["M_CD"];
            $mVal = $mArray["M_NAME"];

            if ($mKey == "" && $mVal == "") {
                continue;
            } else {
                $mDataFlg = true;
            }
            if ($mKey != "" && $mVal == "" || $mKey == "" && $mVal != "") {
                $this->setWarning("MSG301", "(めあてコード/名称を入力して下さい。)");
                return false;
            }

            if (array_key_exists($mKey, $befKeyArray)) {
                $this->setWarning("(めあてコードが重複しています。)");
                return false;
            }

            //文字数チェック    
            if (strlen($mVal) > 120) {
                $this->setWarning("MSG914", "(めあて名称) 120バイトまで");
                return false;
            } 
            $befKeyArray[$mKey] = $mKey;
        }
        if (!$mDataFlg) {
            $this->setWarning("MSG301", "(めあてデータを一件以上入力して下さい。)");
            return false;
        }

        return true;
    }

    //次年度コピー用のモデルを返します
    function &getCopyModel()
    {
        $cntselected = knjz403aQuery::cntThisOrder();
        $cntlastorder = knjz403aQuery::cntLastOrder();
        if($cntselected > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntlastorder == 0){
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjz403aQuery::getCopyQuery();
            if ( !isset($this->warning)){
                $this->setWarning('MSG201',"（前年度からコピー）");
            }
            return $result;
        }
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            list($schoolKind, $grade) = preg_split("/-/", $this->field["GRADE"]);
            $this->grade = $grade;
            $this->lCd = $this->field["L_CD"];
            $this->schoolKind = $schoolKind;
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->lCd == "") {
            $this->setWarning("MSG308");
            return false;
        }

        list($schoolKind, $grade) = preg_split("/-/", $this->field["GRADE"]);
        if ($this->lCd != $this->field["L_CD"] || $this->grade != $grade || $this->schoolKind != $schoolKind) {
            $this->setWarning("MSG308");
            return;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //１レコード取得
        $query = knjz403aQuery::getChkSelectOne($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }

        knjz403aQuery::getDeleteQuery($this->field, $this);
        $this->grade = "";
        $this->lCd = "";
        $this->schoolKind = "";
        $this->setMessage("MSG201");
    }

    function &copy()
    {
        $result = knjz403aQuery::getCopyQuery($this->field);
    }

    function &insert()
    {
        $result = knjz403aQuery::getInsertQuery($this->field, $this);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz403aQuery::getUpdateQuery($this->field, $this);
    }

    function clean()
    {
    }
}
?>
