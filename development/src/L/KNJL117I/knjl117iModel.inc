<?php
class knjl117iModel extends Model {
    var $field       = array();
    var $cmd;

    function knjl117iModel() {}
    
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field = array("COURSENAME_SET"   =>  VARS::post("COURSENAME_SET"),   //コース名
                             "POINTCD"          =>  VARS::post("POINTCD"),          //評価区分
                             "MAX_POINTLEVEL"   =>  VARS::post("MAX_POINTLEVEL"),   //評定段階数
                             "rangeFrom"        =>  VARS::post("rangeFrom"),        //入力可能最小値
                             "rangeTo"          =>  VARS::post("rangeTo"),          //入力可能最大値
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        
        $this->dataField = array();

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        
        //入試年度
        $this->examyear = CTRL_YEAR + 1;
        //入試制度
        $this->applicantdiv = "2"; //高校（固定）
        //入試区分
        $this->testdiv  = VARS::post("TESTDIV");
        //評価区分
        $this->pointcd  = VARS::post("POINTCD");
        //評価段階数
        $this->maxLevel  = VARS::post("MAX_POINTLEVEL");

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
        for ($i = 1; $i <= $this->maxLevel ; $i++) {
            //下限の必須チェック
            if ($this->dataField[$i]["POINTLOW"] == '') {
                $this->setWarning("MSG301", "(下限)");
                return false;
            }
            if ($i == $this->maxLevel) $this->dataField[$i]["POINTHIGH"] = $this->field["rangeTo"];
        }

        return true;
    }
        
    /**
     * 更新処理
    */
    function &getUpdateModel()
    {
        //入力値取得
        for ($i = $this->maxLevel; $i > 0 ; $i--) {
            $this->dataField[$i] = array("POINTLEVEL"   => $i,
                                         "POINTLOW"     => VARS::post("POINTLOW".$i),
                                         "POINTHIGH"    => ($i != $this->maxLevel) ? VARS::post("HID_POINTHIGH".$i) : "",
                                         "PLUS_POINT"   => (VARS::post("PLUS_POINT".$i) != "") ? VARS::post("PLUS_POINT".$i) : 0,
                                         "MINUS_POINT"  => (VARS::post("MINUS_POINT".$i) != "") ? VARS::post("MINUS_POINT".$i) : 0
                                        );
        }
        //入力チェック
        if ($this->validateInput()){
            knjl117iQuery::getInsertQuery($this);
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
        $cntselected = knjl117iQuery::cntToYearOrder($this);
        $cntfromorder = knjl117iQuery::cntFromYearOrder($this);
        if($cntselected > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntfromorder == 0){
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjl117iQuery::getCopyQuery($this);
            if ( !isset($model->warning)){
                $this->setMessage('MSG201',"（前年度からコピー）");
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
