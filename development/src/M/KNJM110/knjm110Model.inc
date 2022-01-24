<?php

require_once('for_php7.php');

class knjm110Model extends Model {

    var $field      = array();  //右画面データ
    var $cmd;                   //コマンド
    var $Year;                  //選択年度
    var $upmindate;             //出欠制御日付退避
    var $semester;              //データ更新時に使用
    var $Chair;                 //講座コード
    var $Periodcd;              //校時コード
    var $Exedate;               //実施日付
    var $Schooling_seq;         //回数
    var $Remark;                //備考

    var $control_data = array();    //コントロールマスタデータを保持

    function knjm110Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
        unset($this->Chair);
        unset($this->Remark);
        unset($this->Exedate);
        unset($this->Schooling_seq);
        unset($this->Periodcd);
    }
    function init()
    {
        if (VARS::get("CHAIRCD") != "")       $this->Chair         = VARS::get("CHAIRCD");
        if (VARS::get("REMARK") != "")        $this->Remark        = VARS::get("REMARK");
        if (VARS::get("PERIODCD") != "")      $this->Periodcd      = VARS::get("PERIODCD");
        if (VARS::get("EXEDATE") != "")       $this->Exedate       = VARS::get("EXEDATE");
        if (VARS::get("SCHOOLING_SEQ") != "") $this->Schooling_seq = VARS::get("SCHOOLING_SEQ");
        if (VARS::post("GrYEAR")) {
            $this->Year = VARS::post("GrYEAR");
        }
        $this->field = array ("EXEDATE2"    =>  VARS::post("EXEDATE2"),
                              "KOUJI2"      =>  VARS::post("KOUJI2"),
                              "KAISUU2"     =>  VARS::post("KAISUU2"),
                              "REMARK"      =>  VARS::post("REMARK")
                             );

        if (VARS::post("Chair") != "") {
            $this->Chair = VARS::post("Chair");
        }
        $this->cmd       = VARS::request("cmd");
        if ($this->cmd == 'init'){
            unset($this->Chair);
            unset($this->Remark);
            unset($this->Exedate);
            unset($this->Schooling_seq);
            unset($this->Periodcd);
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->field["EXEDATE2"] == '' || $this->field["KOUJI2"] == '' || $this->field["KAISUU2"] == '' || $this->Chair == '') {
            if (VARS::request("cmd") != 'delete'){
                $this->setWarning("MSG301");
                return false;
            }
        }
        if (strlen($this->field["REMARK"]) > 60) {
            $this->setWarning("MSG915",strlen($this->field["REMARK"])."(備考は60バイトまでです)");
            return false;
        }
        $this->semester = 0;
        if ($this->upmindate > str_replace("/","-",$this->field["EXEDATE2"]) && VARS::request("cmd") != 'delete'){
            $this->setWarning("日付が出欠制御日付(".str_replace("-","/",$this->upmindate).")未満です。");
            return false;
        }
        if ($this->control_data["学期数"] == 3){
            if ($this->control_data["学期開始日付"]["1"] <= $this->field["EXEDATE2"] && $this->control_data["学期終了日付"]["1"] >= $this->field["EXEDATE2"]) $this->semester = 1;
            if ($this->control_data["学期開始日付"]["2"] <= $this->field["EXEDATE2"] && $this->control_data["学期終了日付"]["2"] >= $this->field["EXEDATE2"]) $this->semester = 2;
            if ($this->control_data["学期開始日付"]["3"] <= $this->field["EXEDATE2"] && $this->control_data["学期終了日付"]["3"] >= $this->field["EXEDATE2"]) $this->semester = 3;
        }else {
            if ($this->control_data["学期開始日付"]["1"] <= $this->field["EXEDATE2"] && $this->control_data["学期終了日付"]["1"] >= $this->field["EXEDATE2"]) $this->semester = 1;
            if ($this->control_data["学期開始日付"]["2"] <= $this->field["EXEDATE2"] && $this->control_data["学期終了日付"]["2"] >= $this->field["EXEDATE2"]) $this->semester = 2;
        }
        if ($this->semester == 0 && VARS::request("cmd") != 'delete'){
            $this->setWarning("日付が学期範囲外です。");
            return false;
        }
        //チェック用データ取得
        $db = Query::dbCheckOut();
        if (VARS::request("cmd") != 'add') {
            $query = knjm110Query::CheckData2($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!is_array($row)){
                $this->setWarning("MSG308");
                return false;
            }
        }
        if (VARS::request("cmd") != 'delete') {
            $query = knjm110Query::CheckData($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        if (VARS::request("cmd") == 'add' || VARS::request("cmd") == 'update') {
            if (VARS::request("cmd") == 'update'){
                $checkcnt = 0;
                if ($this->Periodcd == VARS::post("KOUJI2")) $checkcnt++ ;
                if (str_replace("/","-",$this->Exedate) == str_replace("/","-",VARS::post("EXEDATE2"))) $checkcnt++ ;
                if ($this->Schooling_seq == VARS::post("KAISUU2")) $checkcnt++ ;
            }
            if ($checkcnt < 3){
                if (is_array($row)){
                    $this->setWarning("MSG302");
                    return false;
                }
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if (VARS::request("cmd") == 'add'){
                $this->insert();
            }else if(VARS::request("cmd") == 'update'){
                $this->update();
            }else {
                $this->delete();
            }
            $this->setMessage("MSG201");
        }
    }

    /**
     * インサート時の内部メソッド
     */
    function &insert()
    {
        knjm110Query::getInsertQuery($this);
    }
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        knjm110Query::getUpdateQuery($this);
    }
    /**
     * デリート時の内部メソッド
     */
    function &delete()
    {
        knjm110Query::getDeleteQuery($this);
    }

}
?>
