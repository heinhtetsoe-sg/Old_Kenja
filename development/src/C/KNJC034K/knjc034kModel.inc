<?php

require_once('for_php7.php');

class knjc034kModel extends Model {

    var $cmd;
    var $field;

    function knjc034kModel()
    {
        $this->field = array();
    }

    function init()
    {
        $this->cmd               = VARS::request("cmd");

        $this->field["CHAIRCD"]  = VARS::post("CHAIRCD");   //講座
        $this->field["SCHREGNO"] = VARS::post("SCHREGNO");  //対象生徒

        if ($this->cmd == "subclasscd") {
            $this->field["CHAIRCD"] = "";   //講座
            $this->field["SCHREGNO"] = "";  //対象生徒
        }

        if ($this->cmd == "chaircd") {
            $this->field["SCHREGNO"] = "";  //対象生徒
        }

        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");

        //講座コード
        $this->field["CHAIRCD"] = VARS::request("CHAIRCD");

        /* 編集対象データを取得 */
        $this->field["MONTH"]          = VARS::post("MONTH");           //対象月
        $this->field["APPOINTED_DAY"]  = VARS::post("APPOINTED_DAY");   //締め日
        $this->field["LESSON"]         = VARS::post("LESSON");          //授業日数
        $this->field["OFFDAYS"]        = VARS::post("OFFDAYS");         //休学日数
        $this->field["ABROAD"]         = VARS::post("ABROAD");          //留学日数
        $this->field["ABSENT"]         = VARS::post("ABSENT");          //公欠日数
        $this->field["SUSPEND"]        = VARS::post("SUSPEND");         //出停日数
        $this->field["KOUDOME"]        = VARS::post("KOUDOME");         //交止
        $this->field["VIRUS"]          = VARS::post("VIRUS");           //伝染病
        $this->field["MOURNING"]       = VARS::post("MOURNING");        //忌引日数
        $this->field["SICK"]           = VARS::post("SICK");            //病欠
        $this->field["NOTICE"]         = VARS::post("NOTICE");          //事故欠届
        $this->field["NONOTICE"]       = VARS::post("NONOTICE");        //事故欠無
        $this->field["LATE"]           = VARS::post("LATE");            //遅刻回数
        $this->field["EARLY"]          = VARS::post("EARLY");           //早退回数

        $this->schGrade = VARS::post("schGrade");

        $this->getPropertiesAll(); //プロパティファイルの読込み

        $this->c001 = array();
        $this->c001[4] = "SICK";
        $this->c001[5] = "NOTICE";
        $this->c001[6] = "NONOTICE";

        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"]  = VARS::post("HIDDEN_SUBCLASSCD");
            $this->field["CHAIRCD"]     = VARS::post("HIDDEN_CHAIRCD");
            $this->field["SCHREGNO"]    = VARS::post("HIDDEN_SCHREGNO");
        }
    }

    /* 入力項目チェック */
    function validateInput()
    {
        $db = Query::dbCheckOut();

        /* 更新対象データがない場合 */
        if($this->field["MONTH"] == NULL || $this->field["MONTH"] == ""){
            $this->setWarning("MSG303");
            return false;
        }

        foreach($this->field["MONTH"] as $key => $month){
            //$monthAr[0] = 月、$monthAr[1] = 学期
            $monthAr = preg_split("/-/", $month);

            if ($this->dataCheck($key) ||
                (!$this->dataCheck($key) && "" != $this->field["APPOINTED_DAY"][$key])) {
                $errMsg = $this->appDateCheck($db, $key, $monthAr);
                if ("" != $errMsg) {
                    $this->setWarning("MSG901",$errMsg);
                    return false;
                }
            }
        }

        Query::dbCheckIn($db);

        return true;
    }

    /* 入力データ判定 */
    function dataCheck($key)
    {
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
            $this->field["LATE"][$key] != "" ||
            $this->field["EARLY"][$key] != "")
        {
            return true;
        }
        return false;
    }

    /* 締め日チェック */
    function appDateCheck($db, $key, $monthAr)
    {
        //締め日の最終日
        $year = CTRL_YEAR;
        if ($monthAr[0] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime( 0, 0, 0, $monthAr[0], 1, $year ));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->field["APPOINTED_DAY"][$key]) {
            return $monthAr[0]."月の最終日は、".$lastday."日です。";
        }
        //締め日がNullの場合
        if ("" == $this->field["APPOINTED_DAY"][$key]) {
            return "日付が入力されていません。(".$monthAr[0]."月)";
        }
        //締め日が0の場合
        if ("" != $this->field["APPOINTED_DAY"][$key] && 0 == $this->field["APPOINTED_DAY"][$key]) {
            return "0日は存在しません。(".$monthAr[0]."月)";
        }
        $semeday = $db->getRow(knjc034kQuery::selectSemesAll($this, $this->schGrade, $monthAr[1]),DB_FETCHMODE_ASSOC);
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $semeday["S_MONTH"]) == $monthAr[0] &&
            $semeday["S_DAY"] > $this->field["APPOINTED_DAY"][$key]) {
            return $semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。";
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $monthAr[0] &&
            $semeday["E_DAY"] < $this->field["APPOINTED_DAY"][$key]) {
            return $semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。";
        }
        return "";
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update()
    {
        $result = knjc034kQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
