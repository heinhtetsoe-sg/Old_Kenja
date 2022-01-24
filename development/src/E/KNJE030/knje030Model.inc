<?php

require_once('for_php7.php');

class knje030Model extends Model {

    // 定数定義
    var $field      = array();
    var $cmd;
    var $knje030cd;        //primary
    var $hope;
    var $isload;
    var $class_select;
    var $grade_range;
    var $subclasscd;
    var $knje030year;
    var $knje030schreg;
    //教育課程用
    var $school_kind;
    var $curriculum_cd;

    //メンバ変数の初期化
    function knje030Model()
    {
    }

    function init()
    {    
        $row = knje030Query::getGradeQuery($this);
        $this->grade_range=$row["GRADE_HVAL"];

        $this->field["SCHREGNO"]            =  VARS::post("SCHREGNO");
        $this->field["YEAR"]                =  VARS::post("YEAR");
        $this->field["CLASSCD"]             =  VARS::post("CLASSCD");
        $this->field["CLASSNAME"]           =  VARS::post("CLASSNAME");
        $this->field["CLASSABBV"]           =  VARS::post("CLASSABBV");
        $this->field["CLASSNAME_ENG"]       =  VARS::post("CLASSNAME_ENG");
        $this->field["CLASSABBV_ENG"]       =  VARS::post("CLASSABBV_ENG");
        $this->field["CURRICULUM_CD"]       =  VARS::post("CURRICULUM_CD");
        $this->field["SUBCLASSCD"]          =  VARS::post("SUBCLASSCD");
        $this->field["SUBCLASSNAME"]        =  VARS::post("SUBCLASSNAME");
        $this->field["SUBCLASSABBV"]        =  VARS::post("SUBCLASSABBV");
        $this->field["SUBCLASSNAME_ENG"]    =  VARS::post("SUBCLASSNAME_ENG");
        $this->field["SUBCLASSABBV_ENG"]    =  VARS::post("SUBCLASSABBV_ENG");
        $this->subclasscd = $this->field["CLASSCD"].$this->field["SUBCLASSCD"];

        for ($i = 0; $i <= $this->grade_range; $i++) {    
            $annual = sprintf("%02d", $i);

            $this->field[$annual]["YEAR"]        = VARS::post("YEAR".$annual);
            $this->field[$annual]["VALUATION"]   = VARS::post("VALUATION".$annual);
            $this->field[$annual]["GET_CREDIT"]  = VARS::post("GET_CREDIT".$annual);
            $this->field[$annual]["COMP_CREDIT"] = VARS::post("COMP_CREDIT".$annual);
            $this->field[$annual]["REMARK"]      = VARS::post("REMARK".$annual);
            $this->field[$annual]["UPDATED"]     = VARS::post("UPDATED".$annual);

        }

        $this->field["temp_cd"] =  VARS::post("temp_cd");       //最初のCD

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //教科コンボより
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::request("CLASSCD") =="") {
                $classcd = substr(VARS::request("SUBCLASSCD"),0,2).'-'.VARS::request("SCHOOL_KIND").'-'.VARS::request("CURRICULUM_CD");
            } else {
                $classcd = VARS::request("CLASSCD");
            }
            if (VARS::get("CURRICULUM_CD") != "") {
                $this->field["CURRICULUM_CD"] = VARS::get("CURRICULUM_CD");
            }
        } else {
            if (VARS::request("CLASSCD") =="") {
                $classcd = substr(VARS::request("SUBCLASSCD"),0,2);
            } else {
                $classcd = VARS::request("CLASSCD");
            }
        }
        $this->class_select  = $classcd;
        $this->cmd           = VARS::request("cmd");
        
        //Form1より
        $this->knje030cd      = VARS::request("SUBCLASSCD");
        $this->knje030schreg  = VARS::request("SCHREGNO");
        $this->knje030year    = VARS::request("YEAR");
        $this->hope           = VARS::request("hope");
                
        if ($this->knje030schreg=="") {
            $this->knje030schreg = knje030Query::First_No(CTRL_YEAR,CTRL_SEMESTER, $this);
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //１レコード取得
        $db = Query::dbCheckOut();
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $this->subclasscd = substr($this->field["CLASSCD"], 0, 2).$this->field["SUBCLASSCD"];
        }
        $query = knje030Query::getRow($this->subclasscd,$this->knje030year,$this->knje030schreg);
        $result = $db->query($query);
        $row=array();
        while($Mid = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row[$Mid["ANNUAL"]]["UPDATED"] = $Mid["UPDATED"];
        }
        Query::dbCheckIn($db);

        $this->isload = "0";
        if ($kind=="delete") {

            //リストより選択していない
            if ($this->field["temp_cd"]==""){
                $this->isload = "1";
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->subclasscd !=$this->field["temp_cd"]){
                $this->isload = "1";
                $this->setWarning("MSG308","キー値が変更されています。");
                return false;
            }

           //更新日付チェック
           if ($kind == "update") {
               for ($i = 0; $i <= $this->grade_range; $i++) {    
                if (!in_array($i,array_keys($row))) {
                    $row[$i]["UPDATED"]     = "";
                }

                   if ($row[$i]["UPDATED"] != $this->field[$i]["UPDATED"]) {
                       $this->setWarning("MSG307");
                       return false;
                   }    
               }
           }

        }

        //科目チェック
        if (trim($this->field["SUBCLASSCD"]) == "") {                    //科目番号入力
            $this->setWarning("MSG901","科目コードを入力してください。");
            return false;
        } elseif (strlen($this->field["SUBCLASSNAME"]) >60){         //科目名バイト数制限
            $this->setWarning("MSG914","科目名は60バイトまでです。");
            return false; 
        } elseif (strlen($this->field["SUBCLASSABBV"]) >9){         //科目略称バイト数制限
            $this->setWarning("MSG914","科目略称は9バイトまでです。");
            return false; 
        } elseif (strlen($this->field["SUBCLASSNAME_ENG"]) >40){         //科目英字バイト数制限
            $this->setWarning("MSG914","科目英字は40バイトまでです。");
            return false; 
        } elseif (strlen($this->field["SUBCLASSABBV_ENG"]) >20){        //科目略称英字バイト数制限
            $this->setWarning("MSG914","科目略称英字は20バイトまでです。");
            return false;
        }
        $checkval = 0;
        $checkyear = "";
        for ($i = 0; $i <= $this->grade_range; $i++) {
            $annual = sprintf("%02d", $i);
            if (($this->field[$annual]["YEAR"] =="") &&
                ($this->field[$annual]["VALUATION"] !="" || $this->field[$annual]["GET_CREDIT"] !="" || $this->field[$annual]["COMP_CREDIT"] !="")) {
                $this->setWarning("MSG301",$annual."年次の年度データが未入力です。");
                return false;
            }
            if (strlen($this->field[$annual]["REMARK"]) > 90 ) {        //備考
                $this->setWarning("MSG914","備考は90バイトまでです。");
                return false;
            }
            if ($this->field[$annual]["VALUATION"] == "" && $this->field[$annual]["GET_CREDIT"] == "" && $this->field[$annual]["COMP_CREDIT"] == "") {
                $checkval++;
            }
            if ($annual == "00" && $this->field[$annual]["YEAR"] != "" && $this->field[$annual]["YEAR"] != "0") {
                $this->setWarning("MSG901","０年次の年度は、０のみ有効です。");
                return false;
            }
            if ($annual != "00" && $this->field[$annual]["YEAR"] != "" && $this->field[$annual]["YEAR"] == "0") {
                $this->setWarning("MSG901","０年度は、０年次のみ有効です。");
                return false;
            }
            if ($checkyear != "" && $this->field[$annual]["YEAR"] != "" && $checkyear > $this->field[$annual]["YEAR"]) {
                $this->setWarning("MSG901","年度データ");
                return false;
            }
            if ($this->field[$annual]["YEAR"] !="") {
                $checkyear = $this->field[$annual]["YEAR"];
            }
        }

        if ($checkval == (int)$this->grade_range + 1) {
            $this->setWarning("MSG304","年度と評価/修得単位/増加単位\\nの何れかを指定してください。");
            return false;
        }

        if ($kind=="update") {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->subclasscd = substr($this->field["CLASSCD"], 0, 2).$this->field["SUBCLASSCD"];
            } else {
                //コードのゼロ埋め
                if (strlen($this->field["SUBCLASSCD"]) < 4) {
                    $this->knje030cd = $this->field["SUBCLASSCD"] = sprintf("%04d",$this->field["SUBCLASSCD"]);
                }
            }
        } 

        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        }
        return;
    }

    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    } 

    function &delete()
    {
        if (!isset($this->knje030cd) || !isset($this->knje030year) || !isset($this->knje030schreg)) {
            $this->setWarning("MSG303","delete");
            return;
        }

        $result = knje030Query::getDeleteQuery($this->field,CTRL_YEAR,$this->grade_range,$this->cmd,$this);

        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }

        return $result;
    }

    // アップデート時の内部メソッド
    function &update()
    {
        $result = knje030Query::getDeleteInsertQuery($this->field,CTRL_YEAR,$this->grade_range,$this->cmd,CTRL_SEMESTER,$this);

        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    // 変数をクリアします
    function clean()
    {
    }
}
?>
