<?php

require_once('for_php7.php');

class knjz710Model extends Model {
    var $field      = array();
    var $field2     = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $grade = "";       //primary
    var $mockyear;
    var $copyyear;

    function knjz710Model()
    {
    }

    function init()
    {
        /*$this->field = array("MOCKCD"       =>  VARS::post("MOCKCD"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "FIELD_CNT"    =>  VARS::post("FIELD_CNT")
                             );*/

        $this->cmd          = VARS::request("cmd");
        if($this->cmd == "edit"){
            $this->field2 = array();
        }
        $this->copyyear = (VARS::request("COPYYEAR")) ? VARS::request("COPYYEAR") : $this->copyyear;
        $this->mockyear = (VARS::request("MOCKYEAR")) ? VARS::request("MOCKYEAR") : $this->mockyear;
        if (VARS::get("PROGRAMID")) {
            unset($this->copyyear);
            unset($this->mockyear);
        }
        if ($this->cmd == "changeMockyear" || VARS::get("PROGRAMID")) {
            unset($this->mockcd);
            unset($this->grade);
        }
        if (VARS::get("MOCKCD")) {
            $this->mockcd = VARS::get("MOCKCD");
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }

        //DB接続
        $db = Query::dbCheckOut();
        //hiddenに入れた科目数取得
        $this->kamokuCnt = VARS::post("kamokucount");
        $copymockcd = "1".$this->copyyear.mb_substr($this->mockcd, -4);
        for ($count = 1; $count < $this->kamokuCnt; $count++) {
            $name = "MOCK_SUBCLASS_CD".$count;
            if($this->cmd == "copy" && $this->copyyear != ""){
                //MOCK_SCHOOL_COMP_DATから取得
                $subclassname = VARS::post("subclassname".$count);
                $subCodeQuery = knjz710Query::getSubCd($this->copyyear, $copymockcd, $subclassname, "1");
                $subCnt = $db->getOne($subCodeQuery);
                if($subCnt > 0){
                    $subCodeQuery = knjz710Query::getSubCd($this->copyyear, $copymockcd, $subclassname);
                    $subCode = $db->getOne($subCodeQuery);
                    $this->field2[$name] = $subCode;
                }else{
                    $this->field2[$name] = VARS::post($name);
                }
            }else{
                $this->field2[$name] = VARS::post($name);
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput()
    {
        for ($count = 1; $count < $this->kamokuCnt; $count++) {
            $name = "MOCK_SUBCLASS_CD".$count;
            if ($this->field2[$name] == "") {
                $this->setWarning("MSG301","(模試科目)");
                return false;
            }
        }

        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            //$this->mockcd = $this->field["MOCKCD"];
            //$this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
    }

    //DELETE
    function &getDeleteModel() {
        //DB接続
        $db = Query::dbCheckOut();
        
        //キーの更新チェック
        /*if ($this->grade == "" || $this->mockcd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->grade != $this->field["GRADE"] || $this->mockcd != $this->field["MOCKCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }*/
        $row = $db->getRow(knjz710Query::getRow($this->mockyear, $this->mockcd, $this->grade), DB_FETCHMODE_ASSOC);
        
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        knjz710Query::getDeleteQuery($this);
        $this->mockcd = "";
        $this->grade = "";
        $this->setMessage("MSG201");
        
        //DB切断
        Query::dbCheckIn($db);
    }

    //アップデート時の内部メソッド
    function &update() {
        $result = knjz710Query::getUpdateQuery($this);
        return $result;
    }

    //模試データ作成
    function &updateMock() {
        //入力チェック
        if ($this->validateInput()){
            knjz710Query::insertQueryMock($this);
            //$this->mockcd = $this->field["MOCKCD"];
            //$this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
    }

    //データコピー
    function &getCopyModel() {
        if (!$this->copyCheck()) {
            return;
        }
        knjz710Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
    //コピーチェック
    function copyCheck() {
        //DB接続
        $db = Query::dbCheckOut();

        if (0 < $db->getOne(knjz710Query::getCopyCheck($this))) {
            $this->setWarning("対象年度のデータが既に存在しています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
