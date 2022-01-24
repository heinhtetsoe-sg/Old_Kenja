<?php

require_once('for_php7.php');

class knjz050wModel extends Model {
    var $field      = array();
    var $cmd;
    var $sportCd = "";           //primary
    var $thisIsGet = false;

    function knjz050wModel() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->sportCd);
            unset($this->changeFlg);
        }

        $temp = VARS::request("YEAR");
        if (isset($temp)) {
            $this->year = VARS::request("YEAR");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                 //入試年度
                             "SPORT_CD"         =>  VARS::post("SPORT_CD"),      //競技コード
                             "SPORT_NAME"       =>  VARS::post("SPORT_NAME")     //競技名称
                             );

        $this->cmd          = VARS::request("cmd");

        if (VARS::get("changeFlg")) {
            $this->changeFlg = VARS::get("changeFlg");
        }

        $temp = VARS::get("SPORT_CD");
        if (isset($temp)) {
            $this->sportCd = VARS::get("SPORT_CD");
            $this->thisIsGet = true;
        } else {
            $this->thisIsGet = false;
        }
        if ($this->cmd == "firstEdit") {
            unset($this->year);
            unset($this->sportCd);
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (strlen($this->field["SPORT_CD"]) == 0) {
            $this->setWarning("MSG301","(競技コード)");
            return false;
        }

        if (($this->field["ENTEXAMYEAR"] == "" || $this->sportCd == "") && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->sportCd != $this->field["SPORT_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //コードのゼロ埋め
        $this->field["SPORT_CD"] = sprintf("%05d",$this->field["SPORT_CD"]);
        //１レコード取得
        $db = Query::dbCheckOut(); //DB接続
        $row = knjz050wQuery::getRow($db, $this->field["ENTEXAMYEAR"], $this->field["SPORT_CD"]);
        Query::dbCheckIn($db);     //DB切断
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(競技コード)");
            return false;
        }
        //文字数チェック
        $checkStrLen["SPORT_CD"]   = array("LEN" => 5,  "MESAGE" => "(競技コード)");
        $checkStrLen["SPORT_NAME"] = array("LEN" => 60, "MESAGE" => "(競技名称)");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }    
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->year    = $this->field["ENTEXAMYEAR"];
            $this->sportCd = $this->field["SPORT_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->sportCd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->sportCd != $this->field["SPORT_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $db = Query::dbCheckOut(); //DB接続
        $row = knjz050wQuery::getRow($db, $this->field["ENTEXAMYEAR"], $this->sportCd);
        Query::dbCheckIn($db);     //DB切断
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        knjz050wQuery::getDeleteQuery($this->field, $this);

        $db = Query::dbCheckOut(); //DB接続
        $dataCheck = $db->getOne(knjz050wQuery::getCheckCourseMstSql($this->year));
        Query::dbCheckIn($db);     //DB切断
        if ($dataCheck == 0) {
            $this->year = "";
        }
        $this->sportCd = "";
        $this->setMessage("MSG201");

        $this->field = array();
    }

    //インサート
    function &insert() {
        $result = knjz050wQuery::getInsertQuery($this->field, $this);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjz050wQuery::getUpdateQuery($this->field, $this);
        return $result;
    }

    //次年度のデータを作成
    function &getCopyModel() {
        $db = Query::dbCheckOut();

        $this->nextYear = $this->year + 1;
        $message = '';
        $flg = true;
        if (!checkThisYear($db, $this->year)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkNextYear($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjz050wQuery::getCopyQuery($this->year, $this->nextYear);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }
        Query::dbCheckIn($db);
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
function checkThisYear($db, $year) {
    $query = knjz050wQuery::getCheckCourseMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear) {
    $query = knjz050wQuery::getCheckCourseMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
?>
