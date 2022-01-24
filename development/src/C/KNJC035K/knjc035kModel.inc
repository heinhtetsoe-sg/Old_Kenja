<?php

require_once('for_php7.php');

class knjc035kModel extends Model {
    var $cmd;
    var $field;
    var $appointed_day;
    var $lesson;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjc035kModel() {
        $this->field = array();
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field["CHAIRCD"] = VARS::post("CHAIRCD");

        if ($this->cmd == "subclasscd") {
            $this->field["CHAIRCD"] = "";
        }

        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");

        //講座コード
        $this->field["CHAIRCD"] = VARS::request("CHAIRCD");

        //月コード
        $month = preg_split("/-/", VARS::request("MONTHCD"));
        $this->field["MONTHCD"]  = VARS::request("MONTHCD");
        $this->field["MONTH"]    = $month[0];
        $this->field["SEMESTER"] = $month[1];

        //編集対象データを取得
        $this->field["SCHREGNO"]        = VARS::post("SCHREGNO");       //学籍番号
        $this->field["APPOINTED_DAY"]   = VARS::post("APPOINTED_DAY");  //締め日
        $this->field["LESSON"]          = VARS::post("LESSON");         //授業日数
        $this->field["OFFDAYS"]         = VARS::post("OFFDAYS");        //休学日数
        $this->field["ABROAD"]          = VARS::post("ABROAD");         //留学日数
        $this->field["ABSENT"]          = VARS::post("ABSENT");         //公欠日数
        $this->field["SUSPEND"]         = VARS::post("SUSPEND");        //出停日数
        $this->field["KOUDOME"]         = VARS::post("KOUDOME");        //交止
        $this->field["VIRUS"]           = VARS::post("VIRUS");          //伝染病
        $this->field["MOURNING"]        = VARS::post("MOURNING");       //忌引日数
        $this->field["SICK"]            = VARS::post("SICK");           //病欠
        $this->field["NOTICE"]          = VARS::post("NOTICE");         //事故欠届
        $this->field["NONOTICE"]        = VARS::post("NONOTICE");       //事故欠無
        $this->field["NURSEOFF"]        = VARS::post("NURSEOFF");       //保健室欠課
        $this->field["LATE"]            = VARS::post("LATE");           //遅刻回数
        $this->field["EARLY"]           = VARS::post("EARLY");          //早退回数
        $this->field["LESSON_SET"]      = VARS::post("LESSON_SET");     //授業時数(セット用)

        $this->appointed_day    = VARS::post("APPOINTED_DAY");

        $this->getPropertiesAll();  //プロパティファイルの読込み

        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"]  = VARS::post("HIDDEN_SUBCLASSCD");
            $this->field["CHAIRCD"]     = VARS::post("HIDDEN_CHAIRCD");
            $this->field["MONTHCD"]     = VARS::post("HIDDEN_MONTHCD");
            list($this->field["MONTH"], $this->field["SEMESTER"]) = explode("-",$this->field["MONTHCD"]);
            $this->field["LESSON_SET"]  = VARS::post("HIDDEN_LESSON_SET");
        }
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();
        //更新対象データがない場合
        if($this->field["SCHREGNO"] == NULL || $this->field["SCHREGNO"] == "") {
            $this->setWarning("MSG303");
            return false;
        }

        foreach($this->field["SCHREGNO"] as $key => $schregno) {
            if ($this->dataCheck($key) ||
                (!$this->dataCheck($key) && "" != $this->field["APPOINTED_DAY"])) {
                $errMsg = $this->appDateCheck($db);
                if ("" != $errMsg) {
                    $this->setWarning("MSG901", $errMsg);
                    return false;
                }
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    //入力データ判定
    function dataCheck($key) {
        if ($this->field["LESSON"][$key] != "" ||
            $this->field["OFFDAYS"][$key] != "" ||
            $this->field["ABROAD"][$key] != "" ||
            $this->field["ABSENT"][$key] != "" ||
            $this->field["SUSPEND"][$key] != "" ||
            $this->field["KOUDOME"][$key] != "" ||
            $this->field["VIRUS"][$key] != "" ||
            $this->field["MOURNING"][$key] != "" ||
            $this->field["SICK"][$key] != "" ||
            $this->field["NOTICE"][$key] != "" ||
            $this->field["NONOTICE"][$key] != "" ||
            $this->field["NURSEOFF"][$key] != "" ||
            $this->field["LATE"][$key] != "" ||
            $this->field["EARLY"][$key] != "") {

            return true;
        }
        return false;
    }

    //締め日チェック
    function appDateCheck($db) {
        //締め日の最終日
        $year = CTRL_YEAR;
        if ($this->field["MONTH"] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime( 0, 0, 0, $this->field["MONTH"], 1, $year ));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->field["APPOINTED_DAY"]) {
            return $this->field["MONTH"]."月の最終日は、".$lastday."日です。";
        }
        //締め日がNullの場合
        if ("" == $this->field["APPOINTED_DAY"]) {
            return "締め日が入力されていません。";
        }
        //締め日が0の場合
        if ("" != $this->field["APPOINTED_DAY"] && 0 == $this->field["APPOINTED_DAY"]) {
            return "締め日が0日は存在しません。";
        }
        $semeday = $db->getRow(knjc035kQuery::selectSemesAll($this->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $semeday["S_MONTH"]) == $this->field["MONTH"] &&
            $semeday["S_DAY"] > $this->field["APPOINTED_DAY"]) {
            return $semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。";
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $this->field["MONTH"] &&
            $semeday["E_DAY"] < $this->field["APPOINTED_DAY"]) {
            return $semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。";
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()){
            if($this->update()){
                unset($this->field["LESSON_SET"]);
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //編集対象データを更新
    function &update() {
        $result = knjc035kQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
