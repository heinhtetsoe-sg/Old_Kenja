<?php

require_once('for_php7.php');

class knjh160Model extends Model {

    var $field      = array();
    var $cmd;
    var $GradeClass = "";
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持
    var $replace_data = array();    //置換で選択された生徒一覧
    var $coursemajorcd = "";        //課程学科コード(置換処理に仕様)
    var $chargeclass_flg = 1;       //ユーザに担当クラスがあるかどうか


    function knjh160Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            $this->schregno = "";
            unset($this->GradeClass);
        }

        //生徒情報
        $this->field = array("SCHREGNO"          =>  VARS::post("SCHREGNO"),        //学籍番号
                             "INOUTCD"           =>  VARS::post("INOUTCD"),         //内外区分コード
                             "NAME"              =>  VARS::post("NAME"),            //氏名
                             "GRCL"              =>  VARS::post("GRADE_CLASS"),     //学年
                             "ANNUAL"            =>  VARS::post("ANNUAL"),          //年次    
                             "NAME_SHOW"         =>  VARS::post("NAME_SHOW"),       //氏名表示用
                             "NAME_KANA"         =>  VARS::post("NAME_KANA"),       //氏名かな
                             "NAME_ENG"          =>  VARS::post("NAME_ENG"),        //英字氏名
                             "BIRTHDAY"          =>  VARS::post("BIRTHDAY"),        //誕生日
                             "SEX"               =>  VARS::post("SEX"),             //性別
                             "BLOODTYPE"         =>  VARS::post("BLOODTYPE"),       //血液型(型)
                             "BLOOD_RH"          =>  VARS::post("BLOOD_RH"),        //血液型(RH型)
                             "FINSCHOOLCD"       =>  VARS::post("FINSCHOOLCD"),     //出身中学校コード
                             "FINISH_DATE"       =>  VARS::post("FINISH_DATE"),     //卒業年月日
                             "PRISCHOOLCD"       =>  VARS::post("PRISCHOOLCD"),     //出身中学校コード
                             "ENT_DATE"          =>  VARS::post("ENT_DATE"),        //入学日付
                             "ENT_DIV"           =>  VARS::post("ENT_DIV"),         //入学区分
                             "ENT_REASON"        =>  VARS::post("ENT_REASON"),      //事由
                             "ENT_SCHOOL"        =>  VARS::post("ENT_SCHOOL"),      //学校
                             "ENT_ADDR"          =>  VARS::post("ENT_ADDR"),        //住所
                             "GRD_DATE"          =>  VARS::post("GRD_DATE"),        //卒業日付
                             "GRD_DIV"           =>  VARS::post("GRD_DIV"),         //卒業区分
                             "GRD_REASON"        =>  VARS::post("GRD_REASON"),      //事由
                             "GRD_SCHOOL"        =>  VARS::post("GRD_SCHOOL"),      //学校
                             "GRD_ADDR"          =>  VARS::post("GRD_ADDR"),        //住所
                             "REMARK1"           =>  VARS::post("REMARK1") ,        //備考1
                             "REMARK2"           =>  VARS::post("REMARK2") ,        //備考2
                             "REMARK3"           =>  VARS::post("REMARK3") ,        //備考3
                             "UPDATED1"          =>  VARS::post("UPDATED1"),        //更新日付
                             "ATTENDNO"          =>  VARS::post("ATTENDNO"),        //出席番号
                             "COURSEMAJORCD"     =>  VARS::post("COURSEMAJORCD"),   //課程学科
                             "COURSECODE"        =>  VARS::post("COURSECODE"),      //コースコード
                             "UPDATED2"          =>  VARS::post("UPDATED2")         //更新日付
                        );

        $this->cmd      = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if(VARS::get("GRADE") && VARS::get("HR_CLASS")) {
            $this->GradeClass = VARS::get("GRADE") ."," .VARS::get("HR_CLASS");
        }
        if(VARS::get("SCHREGNO")!=""){
            $this->schregno = VARS::get("SCHREGNO");
        }
        //置換
        if ($this->cmd == "replace_update") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["base_field"] = $this->replace_data["regd_field"] = array();
            //チェックされた項目をテーブル別に分ける
            $this->replace_data["regd_check"] = array(VARS::post("RCHECK0"),VARS::post("RCHECK1"));
            $this->replace_data["base_check"] = array(VARS::post("RCHECK2"),
                                                      VARS::post("RCHECK3"),
                                                      VARS::post("RCHECK4"),
                                                      VARS::post("RCHECK5"),
                                                      VARS::post("RCHECK6"),
                                                      VARS::post("RCHECK7"),
                                                      VARS::post("RCHECK8"),
                                                      VARS::post("RCHECK9"),
                                                      VARS::post("RCHECK10"),
                                                      VARS::post("RCHECK11"),
                                                      VARS::post("RCHECK12"),
                                                      VARS::post("RCHECK13"),
                                                      VARS::post("RCHECK14"));
            $this->replace_data["check_all"] = VARS::post("RCHECK14");
            $arr = explode(",",VARS::post("COURSEMAJORCD"));
            if (VARS::post("RCHECK0")=="1"){
               $this->replace_data["regd_field"]["COURSECD"] = $arr[0];
               $this->replace_data["regd_field"]["MAJORCD"] = $arr[1];
            }
            if (VARS::post("RCHECK1") =="1") $this->replace_data["regd_field"]["COURSECODE"]  = VARS::post("COURSECODE");
            if (VARS::post("RCHECK2") =="1") $this->replace_data["base_field"]["INOUTCD"]     = VARS::post("INOUTCD");
            if (VARS::post("RCHECK3") =="1") $this->replace_data["base_field"]["BIRTHDAY"]    = str_replace("/","-",VARS::post("BIRTHDAY"));
            if (VARS::post("RCHECK4") =="1") $this->replace_data["base_field"]["ENT_DATE"]    = str_replace("/","-",VARS::post("ENT_DATE"));
            if (VARS::post("RCHECK5") =="1") $this->replace_data["base_field"]["ENT_DIV"]     = VARS::post("ENT_DIV");
            if (VARS::post("RCHECK6") =="1") $this->replace_data["base_field"]["ENT_REASON"]  = VARS::post("ENT_REASON");
            if (VARS::post("RCHECK7") =="1") $this->replace_data["base_field"]["ENT_SCHOOL"]  = VARS::post("ENT_SCHOOL");
            if (VARS::post("RCHECK8") =="1") $this->replace_data["base_field"]["ENT_ADDR"]    = VARS::post("ENT_ADDR");
            if (VARS::post("RCHECK9") =="1") $this->replace_data["base_field"]["GRD_DATE"]    = str_replace("/","-",VARS::post("GRD_DATE"));
            if (VARS::post("RCHECK10")=="1") $this->replace_data["base_field"]["GRD_DIV"]     = VARS::post("GRD_DIV");
            if (VARS::post("RCHECK11")=="1") $this->replace_data["base_field"]["GRD_REASON"]  = VARS::post("GRD_REASON");
            if (VARS::post("RCHECK12")=="1") $this->replace_data["base_field"]["GRD_SCHOOL"]  = VARS::post("GRD_SCHOOL");
            if (VARS::post("RCHECK13")=="1") $this->replace_data["base_field"]["GRD_ADDR"]    = VARS::post("GRD_ADDR");
            if (VARS::post("RCHECK14")=="1") $this->replace_data["base_field"]["FINSCHOOLCD"] = VARS::post("FINSCHOOLCD");
            if (VARS::post("RCHECK15")=="1") $this->replace_data["base_field"]["FINISH_DATE"] = str_replace("/","-",VARS::post("FINISH_DATE"));
        }
        if(VARS::get("init")) {
            $this->schregno="";
            unset($this->field);
        }
        if(VARS::get("ini2")) {
            unset($this->replace_data);
        }
    }
}
?>
