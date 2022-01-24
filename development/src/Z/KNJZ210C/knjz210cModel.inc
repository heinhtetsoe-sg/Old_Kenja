<?php

require_once('for_php7.php');

class knjz210cModel extends Model {
    var $field       = array();
    var $cmd;
    var $assessCnt = 5;
    var $testCnt = 1;

    function knjz210cModel() {}
    
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array(
                             "ASSESSCD"         =>  VARS::post("ASSESSCD"),         //評定区分
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "COPY_SUBCLASSCD"  =>  VARS::post("COPY_SUBCLASSCD"),  //コピー科目
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        
        //設定段階値 = 5 固定
        for ($i = 1; $i <= $this->assessCnt; $i++) {
            $this->field["ASSESSMARK".$i] = VARS::post("ASSESSMARK".$i);
            for ($ti = 1; $ti <= $this->testCnt; $ti++) {
                $this->field["ASSESSLOW".$i."_".$ti] = VARS::post("ASSESSLOW".$i."_".$ti);
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->cmd     = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        //コース名、教科、科目のNULLチェック
        if ($this->field["SUBCLASSCD"] == "") {
            $this->setWarning("MSG304", "科目コード");
            return false;
        }
        //テキストエリア入力チェック
        for ($ti = 1; $ti <= $this->testCnt; $ti++) {
            for ($i = 1; $i <= $this->assessCnt; $i++) {
                if (strlen($this->field["ASSESSMARK".$i]) > 6) {
                    $this->setWarning("MSG914","(評定記号は6バイトまでです)");
                    return false;  
                }
                $kai = "第".$ti."回";
                $max = $this->setMax * $ti;
                //下限値の設定
                $low  = $this->field["ASSESSLOW".$i."_".$ti];

                //下限値チェック
                if (!isset($low) || $low == "") {
                   $this->setWarning("MSG301", $kai." 下限");
                   return false;
                }
                
                //数値チェック
                if (!is_numeric($low) && $low != 0) {
                   $this->setWarning("MSG907", $kai." 下限");
                   return false;
                }

                //下限値チェック
                if ($low < 0) {
                    $this->setWarning("MSG913", $kai." 下限");
                    return false;
                } else if($low >= $max) {
                    $this->setWarning("MSG913", $kai." 下限、上限は{$max}を超えてはいけません。:".$low);
                    return false;
                }

                //値大小チェック
                if ($i >= 1) {
                    if ($i > 2) {
                       if ($this->field["ASSESSLOW2_".$ti] < 0) {
                           $this->setWarning("MSG913");
                           return false;
                       }
                    }
                    $assesslow_upper  = $this->field["ASSESSLOW".($i - 1)."_".$ti];
                    if (!($assesslow_upper < $low)) {
                        $this->setWarning("MSG913");
                        return false;
                    }
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
        if ($this->validateInput()) {
            knjz210cQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return true;
    }

    /**
     * 削除処理
    */
    function &getDeleteModel()
    {
        knjz210cQuery::getDeleteQuery($this);
        $this->setMessage("データを削除しました。");
        return true;
    }

    /**
     * コピー処理
    */
    function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        //対象年度、指定年度データチェック
        $cntselected = knjz210cQuery::cntYearOrder($db, $this, CTRL_YEAR);
        $cntfromorder = knjz210cQuery::cntYearOrder($db, $this, CTRL_YEAR - 1);
        Query::dbCheckIn($db);
        if ($cntselected > 0) {
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntfromorder == 0) {
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjz210cQuery::getCopyQuery($this);
            if (!isset($model->warning)) {
                $this->setMessage('MSG201',"（前年度からコピー）");
            }
            return $result;
        }
    }

    /**
     * 科目コピー処理
    */
    function &getCopySubclassModel()
    {
        $db = Query::dbCheckOut();
        //対象年度、指定年度データチェック
        $cntfromorder = knjz210cQuery::cntSubclassOrder($db, $this);
        Query::dbCheckIn($db);
        if ($cntfromorder == 0) {
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjz210cQuery::getCopySubclassQuery($this);
            if (!isset($model->warning)) {
                $this->setMessage('MSG201',"（科目をコピー）");
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
