<?php

require_once('for_php7.php');


class knje302Model extends Model {
    var $cmd;

    //年度
    var $year;

    // 塾コード
    var $preischoolcd;
    // 教室コード
    var $preischoolClassCd;

    // 塾コード
    var $editPreischoolCd;
    // 訪問日付
    var $editVisitDate;
    // SEQ
    var $editSeq;

    var $field = array();
    
    /**
     * メンバ変数の初期化
     */
    function knje302Model()
    {
    }
    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        $this->preischoolcd = VARS::post("PRISCHOOLCD_ID");
        $this->preischoolClassCd = VARS::post("PRISCHOOL_CLASS_CD_ID");

        $this->editPreischoolCd = VARS::post("EDIT_PRISCHOOLCD");
        $this->editVisitDate = VARS::post("EDIT_VISIT_DATE");
        $this->editSeq = VARS::post("EDIT_SEQ");

        $this->field = array(
            'PRISCHOOLCD' => VARS::post("PRISCHOOLCD_ID"),
            'VISIT_DATE' => VARS::post("VISIT_DATE"),
            'SEQ' => VARS::post("SEQ"),
            'PRISCHOOL_CLASS_CD' => VARS::post("PRISCHOOL_CLASS_CD"),
            'PRISCHOOL_NAME' => VARS::post("PRISCHOOL_NAME"),
            'STAFFCD' => VARS::post("STAFFCD"),
            'PRISCHOOL_STAFF' => VARS::post("PRISCHOOL_STAFF"),
            'COMMENT' => VARS::post("COMMENT"),
            'EXAM_STD_INFO' => VARS::post("EXAM_STD_INFO"),
            'REMARK' => VARS::post("REMARK"),
            'UPDATED' => VARS::post("UPDATED")
        );

    }

    /*
     * 入力項目チェック
     */
    function validateInput($kind)
    {
        if ($kind == 'update' || $kind == 'delete') {
            // １レコード取得
            $row = knje302Query::getOneRow($this);
            // キー値が変更されていないか判定
            if ($row["VISIT_DATE"] !=  str_replace("/","-", $this->field["VISIT_DATE"])) {
                $this->setWarning("MSG308");
                return false;
            }
            // 更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == "") {
                $this->setWarning("MSG303");
                return false;
            }
            // 更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }
        //空文字チェック
        if (strlen($this->field["PRISCHOOLCD"]) == 0) {
            $this->setWarning("MSG301","(塾コード)");
            return false;
        }
        if (strlen($this->field["VISIT_DATE"]) == 0) {
            $this->setWarning("MSG301","(訪問日付)");
            return false;
        }
        //バイト数チェック
        if (strlen($this->field["PRISCHOOLCD"]) > 7) {
            $this->setWarning("MSG915","(塾コードは7バイトまでです。)");
            return false;
        }
        if (strlen($this->field["STAFFCD"]) > 10) {
            $this->setWarning("MSG915","(職員は10バイトまでです。)");
            return false;
        }
        if (strlen($this->field["PRISCHOOL_CLASS_CD"]) > 7) {
            $this->setWarning("MSG915","(教室は7バイトまでです。)");
            return false;
        }
        if (strlen($this->field["PRISCHOOL_STAFF"]) > 60) {
            $this->setWarning("MSG915","(面接者は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["COMMENT"]) > 120) {
            $this->setWarning("MSG915","(コメントは120バイトまでです。)");
            return false;
        }
        if (strlen($this->field["EXAM_STD_INFO"]) > 60) {
            $this->setWarning("MSG915","(受験者情報は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["REMARK"]) > 60) {
            $this->setWarning("MSG915","(備考は60バイトまでです。)");
            return false;
        }

        return true;
    }

    /**
     * 追加処理
     */
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knje302Query::getInsertQuery($this);

            $this->setMessage("MSG201");
        }
        return;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knje302Query::getUpdateQuery($this);

            $this->setMessage('MSG201');
        }
    }

    /**
     * 削除完了用のモデルを返します
     */
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            knje302Query::getDeleteQuery($this);

            $this->editPreischoolCd = '';
            $this->editVisitDate = '';
            $this->editSeq = '';

            $this->field = array(
                'PRISCHOOLCD' => '',
                'VISIT_DATE' => '',
                'SEQ' => '',
                'PRISCHOOL_CLASS_CD' => '',
                'STAFFCD' => '',
                'PRISCHOOL_STAFF' => '',
                'COMMENT' => '',
                'EXAM_STD_INFO' => '',
                'REMARK' => ''
            );

            $this->setMessage('MSG201');
        }
    }

}
?>
