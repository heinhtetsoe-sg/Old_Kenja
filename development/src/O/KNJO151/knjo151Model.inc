<?php

class knjo151Model extends Model {

    var $cmd;
    var $schregno;
    var $search_div;
    var $payment_date;
    var $payment_money;
    var $delchk;
    var $year;
    var $subwin;

    //更新で使用する
    var $up_year;
    var $up_schregno;
    var $up_class;
    var $up_subclass;
    var $up_curriculum_cd;
    var $up_rep_class;
    var $up_rep_subclass;
    var $up_rep_curriculum_cd;
    var $up_rep_schoolcd;

    var $select_year;
    var $select_schregno;
    var $select_class;
    var $select_school_kind;
    var $select_subclass;
    var $select_subclass2;
    var $select_curriculum_cd;
    var $select_rep_class;
    var $select_rep_school_kind;
    var $select_rep_subclass;
    var $select_rep_curriculum_cd;
    var $select_rep_former_schoolcd;
    
    var $checked;
    var $mode;

    function knjo151Model() {
    }
 
    function init()
    {
        $this->auth = $this->sendAuth ? $this->sendAuth : AUTHORITY;
        $this->cmd = VARS::request("cmd");

        $this->delchk = VARS::request("DELCHK");

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID")) {
            $this->subwin = VARS::request("SUBWIN");
        }

        if (VARS::post("MODE")) {
            $this->mode = VARS::post("MODE");
        }

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }

        if (VARS::get("SEARCH_DIV")) {
            $this->search_div = VARS::get("SEARCH_DIV");
        }
        
        
        
        
        if(VARS::request("DATAROW")){
            $this->dataRow = VARS::request("DATAROW");
        }
        
        
        
        if ($this->cmd == "") {
            unset($this->schregno);
            
            unset($this->dataRow);
            
            unset($this->left_field);

        }else if(VARS::get("SCHREGNO")){
            unset($this->left_field);
        }
        //フィールド
        $this->field = array("REP_CLASSCD"           => VARS::post("REP_CLASSCD"),                 //教科コード
                             "REP_CURRICULUM_CD"     => VARS::post("REP_CURRICULUM_CD"),           //教育課程年度
                             "REP_SUBCLASSCD"        => VARS::post("REP_SUBCLASSCD"),              //科目コード
                             "SUBCLASSNAME"          => VARS::post("SUBCLASSNAME"),            //科目名称
                             "SUBCLASSABBV"          => VARS::post("SUBCLASSABBV"),            //科目略称
                             "GET_CREDIT"            => VARS::post("GET_CREDIT"),              //修得単位
                             "COMP_CREDIT"           => VARS::post("COMP_CREDIT"),             //履修単位
                             "VALUATION"             => VARS::post("VALUATION"),               //評定
                             "REP_GET_CREDIT"        => VARS::post("REP_GET_CREDIT"),          //修得単位
                             "REP_COMP_CREDIT"       => VARS::post("REP_COMP_CREDIT"),         //履修単位
                             "REP_VALUATION"         => VARS::post("REP_VALUATION"),           //評定
                             "FORMER_REG_SCHOOLCD"   => VARS::post("FORMER_REG_SCHOOLCD"),     //前籍校コード
                             "REMARK"                => VARS::post("REMARK"),                  //備考
                             "MODE"                  => VARS::post("MODE"),                    //入力方法
                             "GET_DIV"               => VARS::post("GET_DIV"),                 //修得区分
                             "GET_DATE"              => VARS::post("GET_DATE"),                //修得日付
                             "UPDATED"               => VARS::post("UPDATED")                  //更新日付
                            );

        $this->field2 = array("APPLICANTNO"           => VARS::post("APPLICANTNO"),             //志願者番号
                              "SCHREGNO"              => VARS::post("SCHREGNO"),                //学籍番号
                              "EDUCATION_REC_PUT_FLG" => VARS::post("EDUCATION_REC_PUT_FLG"),   //指導要録入力完
                              "UPDATED"               => VARS::post("UPDATED")                  //更新日付
                        );

    }
    

    
    //仮更新処理
    //H_APP_Y_COMMON_SUBJ_DATまたはH_APP_Y_EXPERT_SUBJ_DATの更新のみ
    function &getPreUpdateModel()
    {

        $db = Query::dbCheckOut();

        $update = array();
        $update["REGISTERCD"] = STAFFCD;
        $update["UPDATED"] = "sysdate()";
        
        $update["CLASSCD"] = $this->field["REP_CLASSCD"] != "" ? substr($this->field["REP_CLASSCD"], 0, 2) : " ";
        $update["CURRICULUM_CD"] = $this->field["REP_CURRICULUM_CD"] != "" ? $this->field["REP_CURRICULUM_CD"] : " ";
        $update["SUBCLASSCD"] = $this->field["REP_SUBCLASSCD"] != "" ? substr($this->field["REP_SUBCLASSCD"], -6) : " ";
        
        $dataRow = explode("-", $this->dataRow);
        if($dataRow[0] == "1"){
            $upTable = "H_APP_Y_COMMON_SUBJ_DAT";
        }else{
            $upTable = "H_APP_Y_EXPERT_SUBJ_DAT";
        }
        $where = " WHERE SCHREGNO = '".$this->schregno."' AND DATA_ROW = '".$dataRow[1]."' ";

        common::_update($upTable, "", $where, $update, $db, "1");
        
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
        return;
    }
    
    
    
    //更新処理  SCHREG_STUDYREC_DATを作成
    function &getUpdateTopModel()
    {
        $db = Query::dbCheckOut();
        
        //SCHREG_STUDYREC_DATにSCHOOLCD=1のデータがあるかどうかカウント
        $query = knjo151Query::getStudyRecCnt($this->schregno);
        $cnt = $db->getOne($query);
        if($cnt > 0){
            //DELETE
            $query = knjo151Query::delStudyRec($this->schregno);
            $db->query($query);
        }
        
        //SCHREG_STUDYREC_DAT作成
        $query = knjo151Query::getHappData($this->schregno);
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            
            if($row["SUBCLASSCD"] != ""){   //置き替えがされているデータのみ作成
                $insert = array();
                $insert["REGISTERCD"] = STAFFCD;
                $insert["UPDATED"] = "sysdate()";
                
                $insert["SCHREGNO"] = $this->schregno;
                
                $insert["SCHOOLCD"] = "1";
                $insert["YEAR"] = "0000";
                $insert["ANNUAL"] = "00";
                
                $insert["CLASSCD"] = $row["CLASSCD"];
                $insert["SCHOOL_KIND"] = "H";
                $insert["CURRICULUM_CD"] = $row["CURRICULUM_CD"];
                $insert["SUBCLASSCD"] = $row["SUBCLASSCD"];
                
                $insert["GET_CREDIT"] = $row["SUBCLASS_TANNI"] != "" ? $row["SUBCLASS_TANNI"] : $row["SCHOOL_SUBCLASS_TANNI"];
                $insert["COMP_CREDIT"] = "";
                
                common::_update(SCHREG_STUDYREC_DAT, "", "", $insert, $db, "2");
            }
            
        }
        
        Query::dbCheckIn($db);
        
        $this->setMessage("更新しました。");
        
        return;
    }

    
}
?>
