<?php

require_once('for_php7.php');

class knjh438Model extends Model
{
    var $field      = array();
    var $field2     = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $grade = "";       //primary
    var $mockyear;
    var $copyyear;

    function knjh438Model()
    {
    }

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("MOCKCD"       =>  VARS::post("MOCKCD"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "FIELD_CNT"    =>  VARS::post("FIELD_CNT")
                             );

        $this->cmd          = VARS::request("cmd");
        $this->copyyear = (VARS::request("COPYYEAR")) ? VARS::request("COPYYEAR") : $this->copyyear;
        $this->mockyear = (VARS::request("MOCKYEAR")) ? VARS::request("MOCKYEAR") : $this->mockyear;
        if (VARS::get("PROGRAMID")) {
            unset($this->copyyear);
            unset($this->mockyear);
        }
        //右側コピー用年度
        $this->kcopyyear = (VARS::request("KCOPYYEAR")) ? VARS::request("KCOPYYEAR") : $this->kcopyyear;
        if (VARS::get("PROGRAMID")) {
            unset($this->kcopyyear);
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

        //駿台甲府学園か確認
        $this->CHECK_SCHOOL = $db->getOne(knjh438Query::getCheck_School());

        if (strlen($this->field["FIELD_CNT"]) && $this->field["FIELD_CNT"] > 0) {
            //hiddenに入れた科目数取得
            $copymockcd = "1".$this->kcopyyear.mb_substr($this->mockcd, -4);
            for ($field_no = 1; $field_no <= $this->field["FIELD_CNT"]; $field_no++) {
                $name = "MOCK_SUBCLASS_CD".$field_no;
                if ($this->cmd == "kcopy" && $this->kcopyyear != "") {
                    //MOCK_CSV_SUBCLASS_REPLACE_DATから取得
                    $subCodeQuery = knjh438Query::getSubCd($this->kcopyyear, $copymockcd, $field_no, "1");
                    $subCnt = $db->getOne($subCodeQuery);
                    if ($subCnt > 0) {
                        $subCodeQuery = knjh438Query::getSubCd($this->kcopyyear, $copymockcd, $field_no);
                        $subCode = $db->getOne($subCodeQuery);
                        $this->field2[$name] = $subCode;
                    } else {
                        $this->field2[$name] = VARS::post($name);
                    }
                } else {
                    $this->field2[$name] = VARS::post($name);
                }
            }
        }
        //DB切断
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if ($this->field["GRADE"] == "" || $this->field["MOCKCD"] == "" || $this->field["FIELD_CNT"] == "") {
            $this->setWarning("MSG301", "(外部模試名称・学年・科目数)");
            return false;
        }
        for ($field_no = 1; $field_no <= $this->field["FIELD_CNT"]; $field_no++) {
            $name = "MOCK_SUBCLASS_CD".$field_no;
            if ($this->field2[$name] == "") {
                $this->setWarning("MSG301", "(模試科目)");
                return false;
            }
        }

        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->mockcd = $this->field["MOCKCD"];
            $this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
    }

    //DELETE
    function &getDeleteModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //キーの更新チェック
        if ($this->grade == "" || $this->mockcd == "") {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }
        if ($this->grade != $this->field["GRADE"] || $this->mockcd != $this->field["MOCKCD"]) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }
        $row = $db->getRow(knjh438Query::getRow($this->mockyear, $this->mockcd, $this->grade), DB_FETCHMODE_ASSOC);

        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        knjh438Query::getDeleteQuery($this);
        $this->mockcd = "";
        $this->grade = "";
        $this->setMessage("MSG201");

        //DB切断
        Query::dbCheckIn($db);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjh438Query::getUpdateQuery($this);
        return $result;
    }

    //模試データ作成
    function &updateMock()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjh438Query::insertQueryMock($this);
            $this->mockcd = $this->field["MOCKCD"];
            $this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
    }

    //データコピー
    function &getCopyModel()
    {
        if (!$this->copyCheck()) {
            return;
        }
        knjh438Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
    //コピーチェック
    function copyCheck()
    {
        //DB接続
        $db = Query::dbCheckOut();

        if (0 < $db->getOne(knjh438Query::getCopyCheck($this))) {
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
