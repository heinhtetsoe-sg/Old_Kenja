<?php

require_once('for_php7.php');

class knja110_3bModel extends Model {
    var $field      = array();
    var $cmd;
    var $mode;
    var $viewcd;
    var $TRANSFER_SDATE = "";
    var $TRANSFER_EDATE;
    var $SCHREGNO;
    var $TRANSFERCD = "";
    var $row;
    var $fdts;
    var $fdtf;
    var $control_data;
    var $kyoto;

    function knja110_3bModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        $this->field = array("TRANSFERCD"       =>  VARS::post("TRANSFERCD"),           //移動区分
                             "TRANSFER_SDATE"   =>  VARS::post("TRANSFER_SDATE"),       //異動期間開始日付
                             "TRANSFER_EDATE"   =>  VARS::post("TRANSFER_EDATE"),       //異動期間終了日付
                             "TRANSFERREASON"   =>  VARS::post("TRANSFERREASON"),       //事由
                             "TRANSFERPLACE"    =>  VARS::post("TRANSFERPLACE"),        //異動先名
                             "TRANSFERADDR"     =>  VARS::post("TRANSFERADDR"),         //移動先住所
                             "REMARK1"          =>  VARS::post("REMARK1"),              //備考
                             "ABROAD_CLASSDAYS" =>  VARS::post("ABROAD_CLASSDAYS"),     //留学授業日数
                             "ABROAD_CREDITS"   =>  VARS::post("ABROAD_CREDITS"));      //留学習得単位

        $this->cmd = VARS::request("cmd");

        if(VARS::get("SCHREGNO") != ""){
            $this->SCHREGNO = VARS::get("SCHREGNO");
            $this->TRANSFERCD = "";
        }

        if(VARS::get("TRANSFER_SDATE")){
            $this->TRANSFER_SDATE   = str_replace("/","-",VARS::get("TRANSFER_SDATE"));
        }

        if (VARS::get("TRANSFERCD") !=""){
            $this->TRANSFERCD       = VARS::get("TRANSFERCD");
        }

        if(VARS::get("SCHREGNO")   != "" && VARS::get("ISSUE_DATE") != ""){
            $this->SCHREGNO         = VARS::get("SCHREGNO");
            $this->TRANSFER_SDATE   = str_replace("/","-",VARS::get("TRANSFER_SDATE"));
        }

        if(VARS::get("SCHREGNO") != "" && $this->cmd =="edit"){
            $this->SCHREGNO       = VARS::get("SCHREGNO");
            unset($this->TRANSFER_SDATE);
        }
        if (strlen($this->field["TRANSFER_SDATE"]))
            $this->fdts = strtotime(str_replace("/","-",$this->field["TRANSFER_SDATE"]));
        if (strlen($this->field["TRANSFER_EDATE"]))
            $this->fdtf = strtotime(str_replace("/","-",$this->field["TRANSFER_EDATE"]));

        if (VARS::get("PROGRAMID")) {
            $this->kyoto = "";
        }

        //京都判定用
        $db = Query::dbCheckOut();
        $this->kyoto = $db->getOne(knja110_3bQuery::getKindaiJudgment());
        Query::dbCheckIn($db);

    }

    function validateInput($kind){
        //１レコード取得
        $db = Query::dbCheckOut();

        //入学日付取得
        $entdate = $db->getOne(knja110_3bQuery::getEntDate($this->SCHREGNO));
        $entdate = str_replace("-", "/", $entdate);
        if ($this->field["TRANSFER_SDATE"] < $entdate) {
            $this->setWarning("MSG916", "\\n入学日前の日付は指定出来ません。\\n入学日：".$entdate);
            return false;
        }

        //更新・削除チェック
        if ($kind=="update" || $kind=="delete" )
        {
            //リストより選択していない
            if ($this->TRANSFERCD==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値(異動区分)が変更されてるかをチェック
            if ($this->field["TRANSFERCD"] !=$this->TRANSFERCD){
                $this->setWarning("MSG901","\\n異動区分は変更できません。削除してから再度追加して下さい。");
                return false;
            }
        }
        //必須項目チェック
        if ($this->field["TRANSFER_SDATE"] == "") {
            $this->setWarning("MSG301"); 
            return false;
        }

       //追加・更新チェック
       if ($kind=="insert"|| $kind=="update")
       {
           //異動区分重複チェック
           $query = knja110_3bQuery::getList($this);
           $result = $db->query($query);

           while($this->row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

               //異動期間重複チェック
               switch ($this->row["TRANSFERCD"])
               {
                   case "1":
                   case "2":
                   case "3":
                       if(($this->field["TRANSFERCD"] == "1"||
                           $this->field["TRANSFERCD"] == "2"||
                           $this->field["TRANSFERCD"] == "3"))
                       {
                           if(($this->row["TRANSFER_SDATE"]== $this->TRANSFER_SDATE)
                               &&($kind =="update"))    break;

                           if((strtotime($this->row["TRANSFER_SDATE"])<=$this->fdts&&
                               strtotime($this->row["TRANSFER_EDATE"])>=$this->fdts)||
                               (strtotime($this->row["TRANSFER_SDATE"])<=$this->fdtf&&
                               strtotime($this->row["TRANSFER_EDATE"])>=$this->fdtf)||
                               (strtotime($this->row["TRANSFER_SDATE"])>=$this->fdts&&
                               strtotime($this->row["TRANSFER_EDATE"])<=$this->fdtf))
                           {

                            $this->setWarning("MSG901","\\n( 異動期間が正しくありません。)");
                            return false;
                           }
                       }
                       break;
                   case "4":
                       if($this->field["TRANSFERCD"] == "4")
                       {
                          if(($this->row["TRANSFER_SDATE"] == $this->TRANSFER_SDATE) && ($kind =="update")) break;
                           if((strtotime($this->row["TRANSFER_SDATE"]) == $this->fdts))
                           {
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
//                if (($this->field["TRANSFER_SDATE"] >= $this->field["TRANSFER_EDATE"]) || $this->field["TRANSFER_SDATE"] == "")
                if (($this->field["TRANSFER_SDATE"] > $this->field["TRANSFER_EDATE"]) || $this->field["TRANSFER_SDATE"] == "")	//2006.02.07 alp m-yama
                {
                   $this->setWarning("MSG901","\\n( 異動期間が正しくありません。)");
                   return false;
                }
                break;
            case "4":
                if ($this->field["TRANSFER_SDATE"] == ""){
                   $this->setWarning("MSG901","\\n( 異動期間が正しくありません。)");
                   return false;
                }
                break;
        }
        if ($this->kyoto > 0 && $this->field["TRANSFERCD"] == "1") {
            $chk_list = array("TRANSFERREASON"  => array(75, "( 国名 )",    "全角25文字まで！"),
                              "TRANSFERPLACE"   => array(60, "( 学校名 )",  "全角20文字まで！"),
                              "TRANSFERADDR"    => array(75, "( 学年 )",    "全角25文字まで！"),
                              "REMARK1"         => array(90, "( 備考 )",    "全角30文字まで！"));
        } else {
            $chk_list = array("TRANSFERREASON"  => array(75, "( 事由 )",        "全角25文字まで！"),
                              "TRANSFERPLACE"   => array(60, "( 異動先名称 )",  "全角20文字まで！"),
                              "TRANSFERADDR"    => array(75, "( 異動先住所 )",  "全角25文字まで！"));
        }

        foreach ($chk_list as $key => $val) {
            if (mbereg("[｡-ﾟ]",$this->field[$key])) {
                $this->setWarning("MSG901",$val[1]."半角カナは入力できません！");
                return false;
            }            
            if (strlen($this->field[$key]) > $val[0]) {
                $this->setWarning("MSG901",$val[1].$val[2]); 
                return false;
            }
        }
       
        if ($this->field["ABROAD_CLASSDAYS"] > 365) {
            $this->setWarning("MSG915","\\n留学授業日数は365日まで！");
            return false;
        }
        
        //年度をまたぐとエラー
        if ($this->field["TRANSFER_EDATE"]!="" && strtotime($this->field["TRANSFER_EDATE"]) > strtotime((CTRL_YEAR+1)."/03/31")) {
            $this->setWarning("MSG901","\\n異動期間は年度を越えて入力することはできません！");
            return false;
        }
        
        if ($this->field["TRANSFER_SDATE"]!="" && strtotime($this->field["TRANSFER_SDATE"]) < strtotime((CTRL_YEAR)."/04/01")) {
            $this->setWarning("MSG901","\\n異動期間は年度を越えて入力することはできません！");
            return false;
        }
        
        Query::dbCheckIn($db);
        return true;
    }

    function &getUpdateModel()
    {
       //入力チェック
       if ($this->validateInput("update")){
           $this->update();
            $this->setMessage("MSG201");
            $this->TRANSFER_SDATE = str_replace("/","-",$this->field["TRANSFER_SDATE"]);
            $this->TRANSFERCD = $this->field["TRANSFERCD"];
        }
    }

    function &getInsertModel()
    {
        if ($this->SCHREGNO=="") {
            $this->setWarning("MSG304");
            return false;
        }

        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->TRANSFER_SDATE = str_replace("/","-",$this->field["TRANSFER_SDATE"]);
            $this->TRANSFERCD = $this->field["TRANSFERCD"];
        }
        return;
    }

    function &getDeleteModel()
    {
        if ($this->validateInput("delete")){
            $this->delete();
            $this->TRANSFERCD = "";
        }
        return;
    }

    function &delete()
    {
        if (!isset($this->field["TRANSFERCD"])) {
            $this->setWarning("MSG303");
            return;
        }

        $result = knja110_3bQuery::getDeleteQuery($this);

        if ($result==true) {
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
            }
            $this->field = array();
        } else {
            $this->setWarning("MSG203","\\nこのデータは他のテーブルで使用されているので削除できません！");
        }
        return $result;
    }

    function &insert()
    {
        $result = knja110_3bQuery::getInsertQuery($this);

        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    function &update()
    {
        $result = knja110_3bQuery::getUpdateQuery($this);

        if ( !isset($model->warning)){
           $this->setMessage("MSG201");
       }
        return $result;
    }

    function clean()
    {
    }
}
?>