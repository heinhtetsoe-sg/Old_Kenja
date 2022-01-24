<?php

require_once('for_php7.php');

class knje031Model extends Model {

    var $field = array();
    var $cmd;
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持
    var $sec_competence;            //セキュリティーチェック
    var $transfercd;
    var $transfer_sdate;

    function knje031Model()
    {   
        common::GetControlMaster_Fnc($this->control_data); 
        unset($this->GradeClass);
    }
    function init()
    {
        if(VARS::get("PROGRAMID")){
            unset($this->schregno);
            unset($this->transfercd);
            unset($this->transfer_sdate);
        }

        $this->field = array("TRANSFERCD"       =>  VARS::post("TRANSFERCD"),           //移動区分
                             "TRANSFER_SDATE"   =>  VARS::post("TRANSFER_SDATE"),       //異動期間開始日付
                             "TRANSFER_EDATE"   =>  VARS::post("TRANSFER_EDATE"),       //異動期間終了日付
                             "TRANSFERREASON"   =>  VARS::post("TRANSFERREASON"),       //事由
                             "TRANSFERPLACE"    =>  VARS::post("TRANSFERPLACE"),        //異動先名
                             "TRANSFERADDR"     =>  VARS::post("TRANSFERADDR"),         //移動先住所
                             "ABROAD_CLASSDAYS" =>  VARS::post("ABROAD_CLASSDAYS"),     //留学授業日数
                             "ABROAD_CREDITS"   =>  VARS::post("ABROAD_CREDITS"));      //留学習得単位

        $this->cmd = VARS::request("cmd");
                            
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }

        if (VARS::get("TRANSFER_SDATE")) {
            $this->transfer_sdate = str_replace("/","-",VARS::get("TRANSFER_SDATE"));
        }

        if (VARS::get("TRANSFERCD")) {
            $this->transfercd = VARS::get("TRANSFERCD");
        }

        if (VARS::get("SCHREGNO") && VARS::get("TRANSFER_SDATE")) {
            $this->schregno         = VARS::get("SCHREGNO");
            $this->transfer_sdate   = str_replace("/","-",VARS::get("TRANSFER_SDATE"));
        }

        if (VARS::get("SCHREGNO") && $this->cmd == "edit") {
            $this->schregno         = VARS::get("SCHREGNO");
            unset($this->transfer_sdate);
        }
        if (strlen($this->field["TRANSFER_SDATE"]))
            $this->fdts = strtotime(str_replace("/","-",$this->field["TRANSFER_SDATE"]));
        if (strlen($this->field["TRANSFER_EDATE"]))
            $this->fdtf = strtotime(str_replace("/","-",$this->field["TRANSFER_EDATE"]));

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }
    function validateInput($kind){

        $db = Query::dbCheckOut();

        //更新・削除チェック
        if ($kind == "update" || $kind == "delete") {
            //リストより選択していない
            if ($this->transfercd == "") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー値(異動区分)が変更されてるかをチェック
            if ($this->field["TRANSFERCD"] != $this->transfercd) {
                $this->setWarning("MSG901","\\n異動区分は変更できません。");
                return false;
            }
            //キー値(異動開始日付)が変更されてるかをチェック
            if (str_replace("/","-",$this->field["TRANSFER_SDATE"]) != $this->transfer_sdate) {
                $this->setWarning("MSG901","\\n異動開始日付は変更できません。");
                return false;
            }
        }

        //必須項目チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->field["TRANSFER_SDATE"] == "") {
            $this->setWarning("MSG301"); 
            return false;
        }

        //追加・更新チェック
        if ($kind == "insert" || $kind == "update") {
            //異動区分重複チェック
            $query = knje031Query::getTransferList($this);
            $result = $db->query($query);
            while($this->row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

                //異動期間重複チェック
                switch ($this->row["TRANSFERCD"]) {
                    case "1":
                    case "2":
                    case "3":
                        if(($this->field["TRANSFERCD"] == "1" ||
                            $this->field["TRANSFERCD"] == "2" ||
                            $this->field["TRANSFERCD"] == "3"))
                        {
                            if(($this->row["TRANSFER_SDATE"] == $this->transfer_sdate)
                                &&($kind == "update"))   break;

                            if((strtotime($this->row["TRANSFER_SDATE"]) <= $this->fdts&&
                                strtotime($this->row["TRANSFER_EDATE"]) >= $this->fdts)||
                               (strtotime($this->row["TRANSFER_SDATE"]) <= $this->fdtf&&
                                strtotime($this->row["TRANSFER_EDATE"]) >= $this->fdtf)||
                               (strtotime($this->row["TRANSFER_SDATE"]) >= $this->fdts&&
                                strtotime($this->row["TRANSFER_EDATE"]) <= $this->fdtf))
                            {
                                $this->setWarning("MSG901","\\n( 異動期間が正しくありません。)");
                                return false;
                            }
                        }
                        break;
                    case "4":
                        if($this->field["TRANSFERCD"] == "4") {
                            if(($this->row["TRANSFER_SDATE"] == $this->transfer_sdate) && ($kind == "update")) break;
                            if((strtotime($this->row["TRANSFER_SDATE"]) == $this->fdts)) {
                                $this->setWarning("MSG901","\\n( 日付が重複しています。)");
                                return false;
                            }
                        }
                        break;
                }
            }
        }

        //異動期間日付の前後関係
        switch ($this->field["TRANSFERCD"])
        {
            case "1":
            case "2":
            case "3":
                if ($this->field["TRANSFER_EDATE"] == "") {
                    $this->setWarning("MSG901","\\n( 異動期間の終了日付が入力されていません。)");

                    return false;
                }
                if ($this->field["TRANSFER_SDATE"] > $this->field["TRANSFER_EDATE"]) {
                    $this->setWarning("MSG901","\\n( 異動期間の大小が不正です。)");
                    return false;
                }
                break;
            case "4":
                if (($this->field["TRANSFER_SDATE"] > $this->field["TRANSFER_EDATE"]) && $this->field["TRANSFER_EDATE"] != "") {
                    $this->setWarning("MSG901","\\n( 異動期間の大小が不正です。)");
                    return false;
                }
                break;
        }

        //文字チェック
        foreach(array("TRANSFERREASON"  => array(75,"( 事由 )","全角25文字まで"),
                      "TRANSFERPLACE"   => array(60,"( 異動先名称 )","全角20文字まで"),
                      "TRANSFERADDR"    => array(75,"( 異動先住所 )","全角25文字まで")) as $key => $val) {
            if (mbereg("[｡-ﾟ]",$this->field[$key])) {
                $this->setWarning("MSG901",$val[1]."半角カナは入力できません。");
                return false;
            }
            if (strlen($this->field[$key]) > $val[0]) {
                $this->setWarning("MSG901",$val[1],$val[2]);
                return false;
            }
        }

        if ($this->field["ABROAD_CLASSDAYS"] > 365) {
            $this->setWarning("MSG915","\\n留学授業日数は365日まで");
            return false;
        }

        //年度をまたぐとエラー
        $curriculum_year = $db->getOne(knje031Query::getCurriculumYear($this));
        if (strtotime($this->field["TRANSFER_SDATE"]) < strtotime($curriculum_year."/04/01")) {
            $this->setWarning("MSG901","\\n異動期間は".$curriculum_year."/04/01以降に設定して下さい。 ");
            return false;
        }
        if (strtotime($this->field["TRANSFER_SDATE"]) > strtotime((CTRL_YEAR+1)."/03/31")) {
            $this->setWarning("MSG901","\\n異動期間は".(CTRL_YEAR+1)."/03/31以前に設定して下さい。 ");
            return false;
        }
        list($year,$month,$day) = preg_split("/\//",$this->field["TRANSFER_SDATE"]);
        $e_year = ($month < 4) ? $year : (int)$year+1;
        if ($this->field["TRANSFER_EDATE"] != "" && strtotime($this->field["TRANSFER_EDATE"]) > strtotime($e_year."/03/31")) {
            $this->setWarning("MSG901","\\n異動期間は年度を越えて入力することはできません。");
            return false;
        }

        Query::dbCheckIn($db);
        return true;
    }

    //追加
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->transfer_sdate = str_replace("/","-",$this->field["TRANSFER_SDATE"]);
            $this->transfercd = $this->field["TRANSFERCD"];
        }
        return;
    }
    function &insert()
    {
        knje031Query::getInsertQuery($this);

        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新
    function &getUpdateModel()
    {
       //入力チェック
       if ($this->validateInput("update")){
            $this->update();
            $this->setMessage("MSG201");
            $this->transfer_sdate = str_replace("/","-",$this->field["TRANSFER_SDATE"]);
            $this->transfercd = $this->field["TRANSFERCD"];
        }
    }
    function &update()
    {
        knje031Query::getUpdateQuery($this);

        if ( !isset($this->warning)){
           $this->setMessage("MSG201");
       }
        return;
    }

    //削除
    function &getDeleteModel()
    {
        if ($this->validateInput("delete")){
            $this->delete();
            $this->transfercd = "";
        }
        return;
    }
    function &delete()
    {
        if (!isset($this->field["TRANSFERCD"])) {
            $this->setWarning("MSG303");
            return;
        }

        $result = knje031Query::getDeleteQuery($this);

        if ($result == true) {
            if ( !isset($this->warning)){
                $this->setMessage("MSG201");
            }
            $this->field = array();
        } else {
            $this->setWarning("MSG203","\\nこのデータは他のテーブルで使用されているので削除できません。");
        }
        return $result;
    }
}
?>
