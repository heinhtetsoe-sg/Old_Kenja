<?php

require_once('for_php7.php');

class knjz210aModel extends Model {
    var $field       = array();
    var $cmd;

    function knjz210aModel() {}
    
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field = array("COURSENAME_SET"   =>  VARS::post("COURSENAME_SET"),   //コース名
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        
        //設定段階値 = 5 固定
        $cnt = "5";
        for($i = 1; $i <= $cnt; $i++) {
            $this->field["ASSESSMARK".$i] =  VARS::post("ASSESSMARK".$i);
            $this->field["ASSESSLOW".$i] =  VARS::post("ASSESSLOW".$i);
        }
        $this->cmd     = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        //コース名、教科、科目のNULLチェック
        if ($this->field["COURSENAME_SET"] == "" || $this->field["CLASSCD"] == "" || $this->field["SUBCLASSCD"] == "") {
            $this->setWarning("MSG304");
            return false;
        }
        //テキストエリア入力チェック
        $cnt = "5";
        for ($i = 1; $i <= $cnt; $i++) {
            if (strlen($this->field["ASSESSMARK".$i]) >6 ) {
                $this->setWarning("MSG914","(評定記号は6バイトまでです)");
                return false;  
            }
            //下限値の設定
            $ASSESSLOW  = "ASSESSLOW".($i);

            if(!isset($this->field[$ASSESSLOW])){
               $this->setWarning("MSG301","下限");
               return false;
            }
            
            //下限値NULLチェック
            if($this->field[$ASSESSLOW] == ""){
               $this->setWarning("MSG301","下限");
               return false;
            }

            //数値チェック
            if (!is_numeric($this->field[$ASSESSLOW]) && !$this->field[$ASSESSLOW] == 0) {
               $this->setWarning("MSG907","下限");
               return false;
            }

            //下限値の0 or 1 or 100 かチェック
            if ($this->field[$ASSESSLOW] < 0) {
               $this->setWarning("MSG913");
               return false;
            } else if($this->field[$ASSESSLOW] >= 100) {
               $this->setWarning("MSG913","下限、上限は100を超えてはいけません。");
               return false;
            }

            //値大小チェック
            if ($i >= 1) {
                if ($i > 2) {
                   if($this->field["ASSESSLOW2"] < 1){
                       $this->setWarning("MSG913");
                       return false;
                   }
                }
                $ASSESSLOW_UPPER  = "ASSESSLOW".($i - 1);
                if (!($this->field[$ASSESSLOW_UPPER] < $this->field[$ASSESSLOW])) {
                    $this->setWarning("MSG913");
                    return false;
                }
            }
        }
        return true;
    }
        
    /**
     * 更新処理
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjz210aQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return true;
    }

    /**
     * コピー処理
    */
    function &getCopyModel()
    {
        //対象年度、指定年度データチェック
        $cntselected = knjz210aQuery::cntToYearOrder($this);
        $cntfromorder = knjz210aQuery::cntFromYearOrder($this);
        if($cntselected > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntfromorder == 0){
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjz210aQuery::getCopyQuery($this);
            if ( !isset($model->warning)){
                $this->setWarning('MSG201',"（前年度からコピー）");
            }
            return $result;
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
