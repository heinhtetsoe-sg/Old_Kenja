<?php

require_once('for_php7.php');
class knjh111Model extends Model {
    var $field        = array();
    var $cmd;
    var $schregno;
    var $regddate;      //登録日付
    var $subclasscd;    //科目コード
    var $condition;     //学修・併修
    var $seq;           //回数
    var $name;          //生徒氏名
//※教育課程対応
    var $classcd;           

    function knjh111Model() {}

    function init() {
        //クッキー(?)の値をクリアする
        if (!VARS::get("EXP_YEAR") && !VARS::get("REGDDATE") && $this->cmd == "edit") {
            unset($this->regddate);
        }

        if(VARS::get("PROGRAMID")){
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("REGDDATE"      =>  str_replace("/","-",VARS::post("REGDDATE")), //取得日付
                             "CLASSCD"       =>  VARS::post("CLASSCD"),                 //※教育課程対応
                             "SUBCLASSCD"    =>  VARS::post("SUBCLASSCD"),              //科目コード(実際は使っていないが、テーブルにフィールドが存在する為変数としては残している)
                             "ANNUAL"        =>  VARS::post("GRADE"),                   //年次
                             "CONDITION_DIV" =>  VARS::post("CONDITION_DIV"),           //学修・併修
                             "CONTENTSTEXT"  =>  VARS::post("CONTENTSTEXT"),            //資格テキスト
                             "SEQ"           =>  VARS::post("SEQ"),                     //回数      
                             "CONTENTS1"     =>  VARS::post("CONTENTS1"),               //資格内容  
                             "CONTENTS2"     =>  VARS::post("CONTENTS2"),               //合格科目名
                             "REMARK"        =>  VARS::post("REMARK"),                  //備考
                             "CREDITS"       =>  VARS::post("CREDITS"),                 //単位数
                             "UPDATED"       =>  VARS::post("UPDATED"));

        if(VARS::get("SCHREGNO")!="") $this->schregno = VARS::get("SCHREGNO");
        if(VARS::get("subclasscd")!="") $this->subclasscd = VARS::get("subclasscd");
        //※教育課程対応
        if(VARS::get("classcd")!="") $this->subclasscd = VARS::get("classcd");
        
        if(VARS::get("condition")!="") $this->condition = substr(VARS::get("condition"),0,1);
        if(VARS::get("seq")!="") $this->seq = VARS::get("seq");
        if(VARS::get("NAME")!="") $this->name = VARS::get("NAME");
        $this->cmd = VARS::request("cmd");
        if(VARS::get("REGDDATE")!="") $this->regddate    = VARS::request("REGDDATE");

        if(VARS::get("GRADE")){
            $this->grade = VARS::get("GRADE");
        }
        $this->field["ANNUAL"] = $this->grade;
        if(VARS::get("HR_CLASS")){
            $this->hr_class = VARS::get("HR_CLASS");
        }
        //年組コンボが変更されたときキー値を初期化する
        if(VARS::get("init")!="") {
            unset($this->regddate);
            unset($this->subclasscd);
            unset($this->condition);
            unset($this->seq);
            unset($this->schregno);
            unset($this->name);
            //※教育課程対応
            unset($this->classcd);
        }
        
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput($kind) {
        if ($this->field["REGDDATE"]=="") {
            if ($kind=="insert") {
                    $this->setWarning("MSG301");
            } else {
                    $this->setWarning("MSG308");
            }
            return false;
        }
        if ($this->schregno=="") {
           $this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
           return false;
        }

        //１レコード取得
        $db = Query::dbCheckOut();
        $query = knjh111Query::getRow($this->field["REGDDATE"], $this->schregno, $this->field["SUBCLASSCD"], $this->field["CONDITION_DIV"], $this->field["SEQ"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($kind=="update" || $kind=="delete") {

            if ($this->seq=="") {
               $this->setWarning("MSG304","(一覧よりデータを指定してください。)");
               return false;
            }
            if ($this->regddate == ""){
                $this->setWarning("MSG308");
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == ""){
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG302");
                return false;
            }
        }

        if ($kind=="update" || $kind=="insert") {
            if (trim($this->field["REGDDATE"]) == "") {
                $this->setWarning("MSG301","(取得日付)");
                return false;
            }

            if (($this->field["CONDITION_DIV"] == "1" && $this->field["CONTENTSTEXT"] == "") ||
                ($this->field["CONDITION_DIV"] == "2" && $this->field["CONTENTS2"] == "")) {
                $this->setWarning("MSG301","(資格内容/その他内容)");
                return false;
            }

            if (strlen($this->field["CONTENTSTEXT"]) >60 ) {
                $this->setWarning("MSG915","(資格内容は60バイトまでです)");
                return false;
            }
            if (strlen($this->field["CONTENTS2"]) >90 ) {
                $this->setWarning("MSG915","(その他内容は90バイトまでです)");
                return false;
            }
            if (strlen($this->field["REMARK"]) >90 ) {
                $this->setWarning("MSG915","(備考は90バイトまでです)");
                return false;
            }
            if (strlen($this->field["CREDITS"]) >2 ) {
                $this->setWarning("MSG915","(単位数は2バイトまでです)");
                return false;
            }
            if (strlen($this->field["CREDITS"]) > 0 && $this->field["CREDITS"] < 1) {
                $this->setWarning("MSG901", "(単位数は1～99までです)");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput("update")){
            $this->seq = $this->update();
        }
    }

    function &getInsertModel() {
        if ($this->validateInput("insert")){
            $seq = $this->insert();
            $this->regddate   = $this->field["REGDDATE"];
            $this->subclasscd = $this->field["SUBCLASSCD"];
            //※教育課程対応
            $this->classcd = $this->field["CLASSCD"];
            
            $this->condition  = $this->field["CONDITION_DIV"];
            $this->seq        = $seq;
        }
    }

    function &getDeleteModel() {
        if ($this->validateInput("delete")){
            $this->delete();
        }
    }

    function &delete() {
        if ($this->regddate=="") {
            $this->setWarning("MSG308");
            return;
        }
        //キー値が変更されているとエラー
        if (str_replace("/","-",$this->regddate) != $this->field["REGDDATE"] || $this->subclasscd != $this->field["SUBCLASSCD"]) {
           $this->setWarning("MSG308","取得日付または科目は変更できません");
           return;
        }
        $result = knjh111Query::getDeleteQuery($this->regddate,$this->schregno,$this->subclasscd,$this->seq);
        $this->setMessage("MSG201");
        $this->field = array();
    }

    function &insert() {
        $this->setContents();
        $seq = knjh111Query::getInsertQuery($this, $this->field,$this->schregno);
        $this->setMessage("MSG201");
        return $seq;
    }

    function &update() {
        $this->setContents();
        $seq = knjh111Query::getUpdateQuery($this, $this->field, $this->schregno, $this->seq);
        $this->setMessage("MSG201");
        return $seq;
    }

    function setContents() {
        if ($this->field["CONDITION_DIV"] == 1){
            $this->field["CONTENTS1"] = knjh111Query::getInsertContName($this->field);
        }
    }
}
?>
