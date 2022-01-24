<?php

require_once('for_php7.php');

class knje030kModel extends Model {

    // 定数定義
    var $field      = array();
    var $cmd;
    var $knje030kcd;        //primary
    var $hope;
    var $isload;
    var $class_select;
    var $grade_range;
    var $subclasscd;
    var $knje030kyear;
    var $knje030kschreg;
    //教育課程用
    var $school_kind;
    var $curriculum_cd;

    //メンバ変数の初期化
    function knje030kModel()
    {
    }

    function init()
    {    
        $row = knje030kQuery::getGradeQuery();
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

        for ($i=1;$i<=$this->grade_range;$i++) {    
            $annual = sprintf("%02d", $i);

            $this->field[$annual]["VALUATION"]   = VARS::post("VALUATION".$annual);
            $this->field[$annual]["GET_CREDIT"]  = VARS::post("GET_CREDIT".$annual);
            $this->field[$annual]["ADD_CREDIT"]  = VARS::post("ADD_CREDIT".$annual);
            $this->field[$annual]["UPDATED"]     = VARS::post("UPDATED".$annual);

        }

        $this->field["temp_cd"] =  VARS::post("temp_cd");       //最初のCD

        //教科コンボより
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
        $this->knje030kcd      = VARS::request("SUBCLASSCD");
        $this->knje030kschreg  = VARS::request("SCHREGNO");
        $this->knje030kyear    = VARS::request("YEAR");
        $this->hope          = VARS::request("hope");

        if ($this->knje030kschreg=="") {
            $this->knje030kschreg = knje030kQuery::First_No(CTRL_YEAR,CTRL_SEMESTER);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
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
        $query = knje030kQuery::getRow($this->subclasscd,$this->knje030kyear,$this->knje030kschreg);
        $result = $db->query($query);
        $row=array();
        while($Mid = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row[$Mid["ANNUAL"]]["UPDATED"] = $Mid["UPDATED"];
        }
        Query::dbCheckIn($db);

        $this->isload = "0";
        if ($kind=="update" || $kind=="delete") {

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
               for ($i=1;$i<=$this->grade_range;$i++) {    
                if(!in_array($i,array_keys($row))){
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

        //追加時データが存在しているかをチェック
        if ($kind=="insert") {

            $cnt = knje030kQuery::isNew($this->field,$this->field["SCHREGNO"],$this->subclasscd,CTRL_YEAR,CTRL_SEMESTER,$this);
            if ($cnt !=0) {
                $this->setWarning("MSG918");
                return false;
            }

            if ($this->field["SCHREGNO"]=="" && $this->knje030kschreg=="") {
                $this->setWarning("MSG303","(対象生徒)");
                return false;
            }

        }

        if ($kind=="update" || $kind=="insert") {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->subclasscd = substr($this->field["CLASSCD"], 0, 2).$this->field["SUBCLASSCD"];
            } else {
                //コードのゼロ埋め
                if (strlen($this->field["SUBCLASSCD"]) < 4) {
                    $this->knje030kcd = $this->field["SUBCLASSCD"] = sprintf("%04d",$this->field["SUBCLASSCD"]);
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

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
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
        if (!isset($this->knje030kcd) || !isset($this->knje030kyear) || !isset($this->knje030kschreg)) {
            $this->setWarning("MSG303","delete");
            return;
        }

        $result = knje030kQuery::getDeleteQuery($this->field,CTRL_YEAR,$this->grade_range,$this->cmd,$this);

        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }

        return $result;
    }

    function &insert()
    {
        $result = knje030kQuery::getInsertQuery($this->field,CTRL_YEAR,CTRL_SEMESTER,$this->grade_range,$this);

        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    // アップデート時の内部メソッド
    function &update()
    {
        $result = knje030kQuery::getDeleteInsertQuery($this->field,CTRL_YEAR,$this->grade_range,$this->cmd,CTRL_SEMESTER,$this->grade_range,$this);

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
