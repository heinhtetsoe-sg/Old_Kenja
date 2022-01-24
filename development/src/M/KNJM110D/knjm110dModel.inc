<?php

require_once('for_php7.php');

class knjm110dModel extends Model {
    var $field      = array();  //右画面データ
    var $cmd;                   //コマンド
    var $Year;                  //選択年度
    var $Select_Semester;       //選択学期
    var $semester;              //データ更新時に使用
    var $Chair;                 //講座コード
    var $Periodcd;              //校時コード
    var $Exedate;               //実施日付
    var $Schooling_seq;         //回数
    var $Remark;                //備考
    var $Div;                   //選択項目
    var $control = array();     //コントロールマスタデータを保持

    function knjm110dModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->Year);
            unset($this->Div);
            unset($this->Chair);
            unset($this->Exedate);
            unset($this->Periodcd);
            unset($this->Schooling_seq);
        }

        if (VARS::get("CHAIRCD") != "")         $this->Chair            = VARS::get("CHAIRCD");
        if (VARS::get("PERIODCD") != "")        $this->Periodcd         = VARS::get("PERIODCD");
        if (VARS::get("EXEDATE") != "")         $this->Exedate          = VARS::get("EXEDATE");
        if (VARS::get("SCHOOLING_SEQ") != "")   $this->Schooling_seq    = VARS::get("SCHOOLING_SEQ");
        if (VARS::post("YEAR"))                 $this->Year             = VARS::post("YEAR");
        if (VARS::post("SELECT_SEMESTER"))      $this->Select_Semester  = VARS::post("SELECT_SEMESTER");
        if (VARS::post("DIV"))                  $this->Div              = VARS::post("DIV");
        if (VARS::post("CHAIRCD"))              $this->Chair            = VARS::post("CHAIRCD");
        if (VARS::post("EXEDATE"))              $this->Exedate          = VARS::post("EXEDATE");

        $this->field = array ("EXEDATE2"        =>  VARS::post("EXEDATE2"),
                              "KOUJI2"          =>  VARS::post("KOUJI2"),
                              "CHAIRCD2"        =>  VARS::post("CHAIRCD2"),
                              "SCHOOLING_SEQ"   =>  VARS::post("SCHOOLING_SEQ"),
                              "REMARK"          =>  VARS::post("REMARK")
                             );

        if (VARS::get("PERIODCD") != "")    $this->field["KOUJI2"]      = VARS::get("PERIODCD");
        if (VARS::get("EXEDATE") != "")     $this->field["EXEDATE2"]    = VARS::get("EXEDATE");
        if (VARS::get("CHAIRCD") != "")     $this->field["CHAIRCD2"]    = VARS::get("CHAIRCD");

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == 'init'){
            unset($this->Chair);
            unset($this->Exedate);
            unset($this->Periodcd);
            unset($this->Schooling_seq);
        }
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();
        //必須入力チェック
        if ($this->field["EXEDATE2"] == '' || $this->field["KOUJI2"] == '' || ($this->Div == '1' && $this->Chair == '') || ($this->Div == '2' && $this->field["CHAIRCD2"] == '')) {
            if (VARS::request("cmd") != 'delete') {
                $this->setWarning("MSG301");
                return false;
            }
        }

        //桁数チェック
        if (strlen($this->field["REMARK"]) > 60) {
            $this->setWarning("MSG915",strlen($this->field["REMARK"])."バイト(備考は60バイトまでです)");
            return false;
        }

        //日付範囲チェック&&学期取得
        $this->semester = 0;
        $exedate = str_replace("/","-",$this->field["EXEDATE2"]);
        $result = $db->query(knjm110dQuery::getSemesterMst($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SDATE"] <= $exedate && $row["EDATE"] >= $exedate && $row["SEMESTER"] == $this->Select_Semester) $this->semester = $this->Select_Semester;
        }
        if ($this->semester == 0 && VARS::request("cmd") != 'delete') {
            $this->setWarning("日付が学期範囲外です。");
            return false;
        }

        //左から取得したデータかをチェック
        if (VARS::request("cmd") != 'add') {
            if ((str_replace("/","-",$this->field["EXEDATE2"]) != str_replace("/","-",$this->Exedate)) || ($this->field["KOUJI2"] != $this->Periodcd) || ($this->field["CHAIRCD2"] != $this->Chair)) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //データの存在チェック
        if (VARS::request("cmd") != 'add') {
            $query = knjm110dQuery::CheckData2($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!is_array($row)) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //重複チェック
        if (VARS::request("cmd") == 'add') {
            $data_cnt = $db->getOne(knjm110dQuery::CheckData($this));
            if ($data_cnt > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            if (VARS::request("cmd") == 'add') {
                $this->insert();
            } else if(VARS::request("cmd") == 'update') {
                $this->update();
            } else {
                $this->delete();
            }
            $this->setMessage("MSG201");
        }
    }

    //INSERTの内部メソッド
    function &insert() {
        knjm110dQuery::getInsertQuery($this);
    }

    //UPDATEの内部メソッド
    function &update() {
        knjm110dQuery::getUpdateQuery($this);
    }

    //DELETEの内部メソッド
    function &delete() {
        knjm110dQuery::getDeleteQuery($this);
        unset($this->field);
        if ($this->Div == '1') {
            $this->field["CHAIRCD2"] = $this->Chair;
            unset($this->Exedate);
        }
        if ($this->Div == '2') {
            $this->field["EXEDATE2"] = $this->Exedate;
            unset($this->Chair);
        }
        unset($this->Periodcd);
        unset($this->Schooling_seq);
    }
}
?>
