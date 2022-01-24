<?php

require_once('for_php7.php');

class knja110Model extends Model {
    var $field      = array();
    var $cmd;
    var $GradeClass = "";
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持
    var $replace_data = array();    //置換で選択された生徒一覧
    var $coursemajorcd = "";        //課程学科コード(置換処理に仕様)
    var $chargeclass_flg = 1;       //ユーザに担当クラスがあるかどうか

    function knja110Model()
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
                             "HANDICAP"          =>  VARS::post("HANDICAP"),        //その他
                             "NATIONALITY"       =>  VARS::post("NATIONALITY"),     //国籍
                             "FINSCHOOLCD"       =>  VARS::post("FINSCHOOLCD"),     //出身中学校コード
                             "FINISH_DATE"       =>  VARS::post("FINISH_DATE"),     //卒業年月日
                             "PRISCHOOLCD"       =>  VARS::post("PRISCHOOLCD"),     //出身中学校コード
                             "ENT_DATE"          =>  VARS::post("ENT_DATE"),        //入学日付
                             "ENT_DIV"           =>  VARS::post("ENT_DIV"),         //入学区分
                             "ENT_REASON"        =>  VARS::post("ENT_REASON"),      //事由
                             "ENT_SCHOOL"        =>  VARS::post("ENT_SCHOOL"),      //学校
                             "ENT_ADDR"          =>  VARS::post("ENT_ADDR"),        //住所1
                             "ENT_ADDR2"         =>  VARS::post("ENT_ADDR2"),       //住所2
                             "GRD_DATE"          =>  VARS::post("GRD_DATE"),        //卒業日付
                             "GRD_DIV"           =>  VARS::post("GRD_DIV"),         //卒業区分
                             "GRD_REASON"        =>  VARS::post("GRD_REASON"),      //事由
                             "GRD_SCHOOL"        =>  VARS::post("GRD_SCHOOL"),      //学校
                             "GRD_ADDR"          =>  VARS::post("GRD_ADDR"),        //住所1
                             "GRD_ADDR2"         =>  VARS::post("GRD_ADDR2"),       //住所2
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
            $this->replace_data["regd_check"] = array(VARS::post("RCHECK0"),
                                                      VARS::post("RCHECK1"));

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
                                                      VARS::post("RCHECK14"),
                                                      VARS::post("RCHECK15"),
                                                      VARS::post("RCHECK16"),
                                                      VARS::post("RCHECK17"));
            $this->replace_data["check_all"] = VARS::post("RCHECK16");
            $arr = explode(",",VARS::post("COURSEMAJORCD"));
            if (VARS::post("RCHECK0")=="1"){
               $this->replace_data["regd_field"]["COURSECD"] = $arr[0];
               $this->replace_data["regd_field"]["MAJORCD"] = $arr[1];
            }
            if (VARS::post("RCHECK1") =="1")  $this->replace_data["regd_field"]["COURSECODE"]  = VARS::post("COURSECODE");
            if (VARS::post("RCHECK2") =="1")  $this->replace_data["base_field"]["INOUTCD"]     = VARS::post("INOUTCD");
            if (VARS::post("RCHECK3") =="1")  $this->replace_data["base_field"]["BIRTHDAY"]    = str_replace("/","-",VARS::post("BIRTHDAY"));
            if (VARS::post("RCHECK4") =="1")  $this->replace_data["base_field"]["ENT_DATE"]    = str_replace("/","-",VARS::post("ENT_DATE"));
            if (VARS::post("RCHECK5") =="1")  $this->replace_data["base_field"]["ENT_DIV"]     = VARS::post("ENT_DIV");
            if (VARS::post("RCHECK6") =="1")  $this->replace_data["base_field"]["ENT_REASON"]  = VARS::post("ENT_REASON");
            if (VARS::post("RCHECK7") =="1")  $this->replace_data["base_field"]["ENT_SCHOOL"]  = VARS::post("ENT_SCHOOL");
            if (VARS::post("RCHECK8") =="1")  $this->replace_data["base_field"]["ENT_ADDR"]    = VARS::post("ENT_ADDR");
            if (VARS::post("RCHECK9")=="1")  $this->replace_data["base_field"]["ENT_ADDR2"]   = VARS::post("ENT_ADDR2");
            if (VARS::post("RCHECK10") =="1")  $this->replace_data["base_field"]["GRD_DATE"]    = str_replace("/","-",VARS::post("GRD_DATE"));
            if (VARS::post("RCHECK11")=="1")  $this->replace_data["base_field"]["GRD_DIV"]     = VARS::post("GRD_DIV");
            if (VARS::post("RCHECK12")=="1")  $this->replace_data["base_field"]["GRD_REASON"]  = VARS::post("GRD_REASON");
            if (VARS::post("RCHECK13")=="1")  $this->replace_data["base_field"]["GRD_SCHOOL"]  = VARS::post("GRD_SCHOOL");
            if (VARS::post("RCHECK14")=="1")  $this->replace_data["base_field"]["GRD_ADDR"]    = VARS::post("GRD_ADDR");
            if (VARS::post("RCHECK15")=="1")  $this->replace_data["base_field"]["GRD_ADDR2"]   = VARS::post("GRD_ADDR2");
            if (VARS::post("RCHECK16")=="1")  $this->replace_data["base_field"]["FINSCHOOLCD"] = VARS::post("FINSCHOOLCD");
            if (VARS::post("RCHECK17")=="1")  $this->replace_data["base_field"]["FINISH_DATE"] = str_replace("/","-",VARS::post("FINISH_DATE"));
        }
        if(VARS::get("init")) {
            $this->schregno="";
            unset($this->field);
        }
        if(VARS::get("ini2")) {
            unset($this->replace_data);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->field["GRCL"]==""){ return false;};

        if ($this->schregno == "" &&
           ($this->cmd == "delete" || $this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up" )) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up" ) && $this->field["SCHREGNO"] != $this->schregno) {
            $this->setMessage("MSG300","\\n学籍番号は変更できません！");
            return false;
        }

        if($this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up" ){
            $row = knja110Query::getStudent_data($this, $this->schregno);
            if($row["UPDATED1"] != $this->field["UPDATED1"] || $row["UPDATED2"] != $this->field["UPDATED2"]){
                $this->setWarning("MSG307");
                return false;
            }
        }
        //必須項目チェック
        if($this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up" || $this->cmd == "add")
        {
            $flag = false;
            $msg ="";
            //年組
            if($this->field["GRCL"]         == "" ){
                $flag = true;
                $msg .= "\\n年組";
            }
            //出席番号
            if($this->field["ATTENDNO"]     == "" ){
                $flag = true;
                $msg .= "\\n出席番号";
            }
            //年次
            if($this->field["ANNUAL"]       == "" ){
                $flag = true;
                $msg .= "\\n年次";
            }
            //課程学科コード
            if($this->field["COURSEMAJORCD"]== "" ){
                $flag = true;
                $msg .= "\\n課程学科";
            }
            //コースコード
            if($this->field["COURSECODE"]   == "" ){
                $flag = true;
                $msg .= "\\n";
            }
            //学籍番号
            if($this->field["SCHREGNO"]     == "" ){
                $flag = true;
                $msg .= "\\n学籍番号";
            }
            //内外区分
            if($this->field["INOUTCD"]      == "" ){
                $flag = true;
                $msg .= "\\n内外区分";
            }
            //氏名
            if($this->field["NAME"]         == "" ){
                $flag = true;
                $msg .= "\\n氏名";
            }
            //氏名表示用
            if($this->field["NAME_SHOW"]    == "" ){
                $flag = true;
                $msg .= "\\n氏名表示用";
            }
            //生年月日
            if($this->field["BIRTHDAY"]     == "" ){
                $flag = true;
                $msg .= "\\n生年月日";
            }
            //入学日付
            if($this->field["ENT_DATE"]     == "" ){
                $flag = true;
                $msg .= "\\n入学日付";
            }
            //入学区分
            if($this->field["ENT_DIV"]      == "" ){
                $flag = true;
                $msg .= "\\n入学区分";
            }
            if( $flag == true){
                $this->setWarning("MSG301",$msg);
                return false;
            }
        }

        //文字型チェック
        if (!preg_match("/^[ a-z]*$/i",trim($this->field["NAME_ENG"]))) {
            $this->warning = "半角英字ではありません。( 英字氏名 )";
            return false;
        }

        foreach (array("NAME"           => array("( 氏名 )","[｡-ﾟ]"),
                       "NAME_SHOW"      => array("( 氏名表示用 )","[｡-ﾟ]"),
                       "NAME_KANA"      => array("( 氏名かな )","[｡-ﾟ]")) as $key => $val) {
            if (mbereg($val[1],$this->field[$key])) {
                $this->setWarning("MSG901",$val[0]);
                return false;
            }
        }

        if ($this->field["BLOODTYPE"] != "") {
            if (strtoupper($this->field["BLOODTYPE"])!="A" && strtoupper($this->field["BLOODTYPE"])!="B" &&
                strtoupper($this->field["BLOODTYPE"])!="O" && strtoupper($this->field["BLOODTYPE"])!="AB") {
                $this->setWarning("MSG901","\\n正しい血液型( A, B, O, AB )を入力してください！");
                return false;
            }
        }

        if ($this->field["BLOOD_RH"] != "") {
            if (!preg_match("/[+]|[-]/", $this->field["BLOOD_RH"])) {
                $this->setWarning("MSG901","\\n正しいＲＨ型( +, - )を入力してください！");
                return false;
            }
        }

        if ($this->field["ANNUAL"] == "0") {
            $this->setWarning("MSG916","( 年次 ) 1～99まで");
            return false;
        }

        if ($this->field["BIRTHDAY"] != "" && strtotime($this->field["BIRTHDAY"]) >= strtotime(CTRL_DATE)) {
            $this->setWarning("MSG901","生年月日には学籍処理日より以前の日付を入力してください");
            return false;
        }

        //文字数チェック
        if($this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up"  || $this->cmd == "add"){

            if (strlen($this->field["NAME"]) > 60 ) {
                $this->setWarning("MSG901", "( 氏名 ) 60バイトまで");
                return false;
            } else if (strlen($this->field["NAME_SHOW"]) > 30 ) {
                $this->setWarning("MSG901", "( 表示用氏名 ) 30バイトまで");
                return false;
            } else if (strlen($this->field["NAME_KANA"]) > 120 ) {
                $this->setWarning("MSG901", "( 氏名かな ) 120バイトまで");
                return false;
            } else if (strlen($this->field["NAME_ENG"]) > 40 ) {
                $this->setWarning("MSG901", "( 英字氏名 ) 40バイトまで");
                return false;
            } else if (strlen($this->field["BLOODTYPE"]) > 2 ) {
                $this->setWarning("MSG901", "( 血液型 ) 2バイトまで");
                return false;
            } else if (strlen($this->field["BLOOD_RH"]) > 1 ) {
                $this->setWarning("MSG901", "( 血液 RH型 ) 1バイトまで");
                return false;
            } else if (strlen($this->field["ENT_REASON"]) > 75 ){
                $this->setWarning("MSG901","( 入学:事由 ）75バイトまで");
                return false;
            } else if (strlen($this->field["ENT_SCHOOL"]) > 75 ){
                $this->setWarning("MSG901","( 入学:学校名 ）75バイトまで");
                return false;
            } else if (strlen($this->field["ENT_ADDR"]) > 90 ){
                $this->setWarning("MSG901","( 入学:学校住所1 ）90バイトまで");
                return false;
            } else if (strlen($this->field["ENT_ADDR2"]) > 90 ){
                $this->setWarning("MSG901","( 入学:学校住所2 ）90バイトまで");
                return false;
            } else if (strlen($this->field["GRD_REASON"]) > 75 ){
                $this->setWarning("MSG901","( 卒業:事由 ）75バイトまで");
                return false;
            } else if (strlen($this->field["GRD_SCHOOL"]) > 75 ){
                $this->setWarning("MSG901","( 卒業:学校名 ）75バイトまで");
                return false;
            } else if (strlen($this->field["GRD_ADDR"]) > 90 ){
                $this->setWarning("MSG901","( 卒業:学校住所1 ）90バイトまで");
                return false;
            } else if (strlen($this->field["GRD_ADDR2"]) > 90 ){
                $this->setWarning("MSG901","( 卒業:学校住所2 ）90バイトまで");
                return false;
            }else if (strlen($this->field["REMARK1"]) > 75 ){
               $this->setWarning("MSG901","( 備考１ ) 75バイトまで");
               return false;
            } else if(strlen($this->field["REMARK2"]) > 75 ){
               $this->setWarning("MSG901","( 備考２ ) 75バイトまで");
               return false;
            } else if(strlen($this->field["REMARK3"]) > 75 ){
               $this->setWarning("MSG901","( 備考３ ) 75バイトまで");
               return false;
            }
        }
        return true;
    }

    //データが存在するかチェック
    function Exist_Check($table_flag)
    {
        if($table_flag == "schreg_regd_dat")  return knja110Query::getExist_regd_dat($this);
        if($table_flag == "schreg_base_mst")  return knja110Query::getExist_base_mst($this->field["SCHREGNO"]);
        if($table_flag == "attendno")         return knja110Query::IsExistAttendno($this);
    }

    function &getUpdateModel()
    {
        if ($this->validateInput())
        {
            if ($this->Exist_Check("attendno")) {
                $this->setWarning("MSG302","( 出席番号 )");
                return;
            }
            if($this->update()){
                $this->setMessage("MSG201");
                if ($this->GradeClass != $this->field["GRCL"]) {
                    unset($this->field);
                    unset($this->schregno);
                }
                if ($this->schregno == "") {
                    unset($this->field);
                }
                return;
            }
        }
    }

    function &ReplaceModel()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        //生年月日は過去日付
        if ($this->replace_data["base_field"]["BIRTHDAY"] != "" &&
            strtotime($this->replace_data["base_field"]["BIRTHDAY"]) >= strtotime(CTRL_DATE)) {

            $this->setWarning("生年月日には学籍処理日より以前の日付を入力してください");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1",$this->replace_data["base_check"]) && !in_array("1",$this->replace_data["regd_check"])) {
            return false;
        }
        //入力チェック
        if (strlen($this->replace_data["base_field"]["GRD_REASON"]) > 75 ){
            $this->setWarning("MSG901","( 事由 ）75バイトまで");
            return false;
        }
        //チェックがついている項目があるテーブルのみを更新
        $db = Query::dbCheckOut();
        if (in_array("1",$this->replace_data["base_check"]) && in_array("1",$this->replace_data["regd_check"])) {
            knja110Query::ReplaceUpdate_Regd($db,$this);
            knja110Query::ReplaceUpdate_Base($db,$this);
        } elseif (!in_array("1",$this->replace_data["base_check"]) && in_array("1",$this->replace_data["regd_check"])) {
            knja110Query::ReplaceUpdate_Regd($db,$this);
        } elseif (in_array("1",$this->replace_data["base_check"]) && !in_array("1",$this->replace_data["regd_check"])) {
            knja110Query::ReplaceUpdate_Base($db,$this);
        }
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["regd_check"],(array)$this->replace_data["base_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }

    function &getAddingModel()
    {
        if ($this->validateInput()){
            //データの有無チェック
            if($this->Exist_Check("schreg_regd_dat")){
                $this->setWarning("MSG302");
                return;
             }
            if($this->Exist_Check("schreg_base_mst")){
                $this->setWarning("MSG302");
                return;
            }
            if ($this->Exist_Check("attendno")) {
                $this->setWarning("MSG302","( 出席番号 )");
                return;
            }
            if($this->add()){
                $this->setMessage("MSG201");
                $this->schregno = $this->field["SCHREGNO"];
                if ($this->GradeClass != $this->field["GRCL"]) {
                    unset($this->field);
                    unset($this->schregno);
                }
                return;
            }
        }
    }

    function &getDeleteModel()
    {
        if ($this->field["GRCL"]==""){ return false;};

        if ($this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }

        if($this->field["SCHREGNO"] != $this->schregno) {
            $this->setMessage("MSG300","\\n学籍番号は変更できません！");
            return false;
        }
        $row = knja110Query::getStudent_data($this, $this->schregno);
        if($row["UPDATED1"] != $this->field["UPDATED1"] || $row["UPDATED2"] != $this->field["UPDATED2"]){
            $this->setWarning("MSG307");
            return false;
        }
        //デリート
        if($this->delete()){
            $this->setMessage("MSG201");
            $this->schregno = "";
            return;
        }
    }

    function &delete()
    {
        $result = knja110Query::getDeleteQuery($this);
        return $result;
    }

    function &update()
    {
        $result1 = knja110Query::getUpdateQuery($this,"schreg_regd_dat");   //在籍データ更新
        $result2 = knja110Query::getUpdateQuery($this,"schreg_base_mst");   //基礎マスタ更新

        if($result1 == true && $result2 == true){
            $result = true;
        }else{
            $result = false;
        }

        $this->setMessage("MSG201");
        return true;
    }

    function &add()
    {
        $result1 = knja110Query::getInsertQuery($this,"schreg_regd_dat");
        $result2 = knja110Query::getInsertQuery($this,"schreg_base_mst");

        if($result1 == true && $result2 == true){
            $result = true;
        }else{
            $result = false;
        }
        return $result;
    }

    //各コンボボックス作成
    function CreateCombo($objForm,$db,$namecd,$varname,$value,$fst)
    {
        $result = $db->query(knja110Query::getNameMst_data($namecd));
        $opt = array();

        //性別と卒業区分には先頭に空をセット
        if ($fst=="1") $opt[] = array("label" => "","value" => "");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
              $opt[] = array( "label" => $row["NAMECD2"]."  ".htmlspecialchars($row["NAME1"]),
                              "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => $varname,
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $value,
                            "options"     => $opt));

        return $objForm->ge($varname);
    }
}
?>
