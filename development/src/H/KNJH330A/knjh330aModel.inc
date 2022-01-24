<?php

require_once('for_php7.php');

class knjh330aModel extends Model {
    var $field      = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $mockdiv = "";       //primary
    var $mockyear;
    var $copyyear;

    function knjh330aModel()
    {
    }

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //駿台甲府学園か確認
        //DB接続
        $db = Query::dbCheckOut();
        $this->CHECK_SCHOOL = $db->getOne(knjh330aQuery::getCheck_School());
        //DB切断
        Query::dbCheckIn($db);

        $this->field = array("MOCK_DIV"	    =>  VARS::post("MOCK_DIV"),
                             "MOCKCD"		=>  VARS::post("MOCKCD"),
                             "MOCKNAME1"	=>  VARS::post("MOCKNAME1"),
                             "MOCKNAME2"	=>  VARS::post("MOCKNAME2"),
                             "MOCKNAME3"	=>  VARS::post("MOCKNAME3"),
                             "GRADE"	    =>  VARS::post("GRADE"),
                             "COMPANYCD"            =>  VARS::post("COMPANYCD"),
                             "COMPANYMOSI_CD"       =>  VARS::post("COMPANYMOSI_CD"),
                             "TUUCHIHYOU_MOSI_NAME" =>  VARS::post("TUUCHIHYOU_MOSI_NAME"),
                             "SINROSIDOU_MOSI_NAME" =>  VARS::post("SINROSIDOU_MOSI_NAME"),
                             "MOSI_DIV"             =>  VARS::post("MOSI_DIV"),
                             "MOSI_DATE"            =>  str_replace("/","-",VARS::post("MOSI_DATE")),
                             "FILE_NAME"            =>  VARS::post("FILE_NAME")
                             );

        $this->cmd          = VARS::request("cmd");
        $this->copyyear = (VARS::request("COPYYEAR")) ? VARS::request("COPYYEAR") : $this->copyyear;
        $this->mockyear = (VARS::request("MOCKYEAR")) ? VARS::request("MOCKYEAR") : $this->mockyear;
        if (VARS::get("PROGRAMID")) {
            unset($this->copyyear);
            unset($this->mockyear);
        }
        if ($this->cmd == "changeMockyear" || VARS::get("PROGRAMID")){
            unset($this->mockcd);
            unset($this->mockdiv);
            unset($this->grade);
            unset($this->company);
        }
        $temp = VARS::get("MOCKCD");
        if (isset($temp)){
            $this->mockcd = substr(VARS::get("MOCKCD"), 7);
            $this->grade = substr(VARS::get("MOCKCD"), 6,1);
            $this->company = substr(VARS::get("MOCKCD"), 5,1);
            $this->mockdiv = substr(VARS::get("MOCKCD"), 0, 1);
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if ((trim($this->field["MOCK_DIV"]) == "" || trim($this->field["MOCKCD"]) == "") && $this->cmd == "add") {
            $this->setWarning("MSG301","(模試コード)");
            return false;
        }
        if (strlen($this->field["MOCKNAME1"]) == 0) {
            $this->setWarning("MSG301","(模試名称)");
            return false;
        }

        if (($this->mockdiv == "" || $this->mockcd == "" || $this->company == "" || $this->grade == "" ) && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && ($this->mockdiv != $this->field["MOCK_DIV"] || $this->mockcd != $this->field["MOCKCD"] || $this->company != substr($this->field["COMPANYCD"],-1) || $this->grade != substr($this->field["GRADE"],-1) )) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $row = knjh330aQuery::getRow($this->field["MOCK_DIV"].$this->mockyear.substr($this->field["COMPANYCD"],-1).substr($this->field["GRADE"],-1).$this->field["MOCKCD"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(模試コード)");
            return false;
        }
        //文字数チェック
        $checkStrLen["MOCKNAME1"] = array("LEN" => 60, "MESAGE" => "(模試名称)");
        $checkStrLen["MOCKNAME2"] = array("LEN" => 60, "MESAGE" => "(模試略称１)");
        $checkStrLen["MOCKNAME3"] = array("LEN" => 60, "MESAGE" => "(模試略称２)");
        $checkStrLen["TUUCHIHYOU_MOSI_NAME"] = array("LEN" => 60, "MESAGE" => "(通知表試験名)");
        $checkStrLen["SINROSIDOU_MOSI_NAME"] = array("LEN" => 60, "MESAGE" => "(進路指導試験名)");
        $checkStrLen["FILE_NAME"] = array("LEN" => 150, "MESAGE" => "(ファイル名)");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
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
            $this->setMessage("MSG201");
        }    
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->mockcd  = $this->field["MOCKCD"];
            $this->mockdiv = $this->field["MOCK_DIV"];
            $this->company = substr($this->field["COMPANYCD"],-1);
            $this->grade   = substr($this->field["GRADE"],-1);
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->mockdiv == "" || $this->mockcd == "" || $this->company == "" || $this->grade == "" ) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->mockdiv != $this->field["MOCK_DIV"] || $this->mockcd != $this->field["MOCKCD"] || $this->company != substr($this->field["COMPANYCD"],-1) || $this->grade != substr($this->field["GRADE"],-1)) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
         $row = knjh330aQuery::getRow($this->mockdiv.$this->mockyear.$this->company.$this->grade.$this->mockcd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
		knjh330aQuery::getDeleteQuery($this, $this->field);
        $this->mockcd = "";
        $this->mockdiv = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjh330aQuery::getInsertQuery($this, $this->field);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjh330aQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    //データコピー
    function &getCopyModel() {
        if (!$this->copyCheck()) {
            return;
        }
        knjh330aQuery::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
    //コピーチェック
    function copyCheck() {
        //DB接続
        $db = Query::dbCheckOut();

        if (0 < $db->getOne(knjh330aQuery::getCopyCheck($this))) {
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
