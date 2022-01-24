<?php

require_once('for_php7.php');

class knjc032cModel extends Model {

    var $cmd;
    var $ctrl_year;     //処理年度
    var $ctrl_semester; //処理学期
    var $field;
    var $set_detail_field;

    function knjc032cModel()
    {
        $this->field = array();
    }

    function init()
    {
        $this->cmd               = VARS::request("cmd");
        $this->ctrl_year         = CTRL_YEAR;               //処理年度
        $this->ctrl_semester     = CTRL_SEMESTER;           //処理学期
        $this->field["hr_class"] = VARS::post("HR_CLASS");  //対象学級
        if ($this->cmd == "change_class") {
            $this->field["schregno"] = "";  //対象生徒
        } else {
            $this->field["schregno"] = VARS::post("SCHREGNO");  //対象生徒
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->school_kind = knjc032cQuery::getSchoolKind($this);

        /* 学年・学級に分割 */
        list($this->field["grade"],$this->field["class"]) = explode("-",$this->field["hr_class"]);
        $this->set_detail_field = VARS::post("SET_DETAIL_FIELD");
        /* 編集対象データを取得 */
        $this->field["MONTH"]          = VARS::post("MONTH");           //対象月
        $this->field["APPOINTED_DAY"]  = VARS::post("APPOINTED_DAY");   //締め日
        $this->field["LESSON"]         = VARS::post("LESSON");          //授業日数
        $this->field["OFFDAYS"]        = VARS::post("OFFDAYS");         //休学日数
        $this->field["ABROAD"]         = VARS::post("ABROAD");          //留学日数
        $this->field["ABSENT"]         = VARS::post("ABSENT");          //公欠日数
        $this->field["SUSPEND"]        = VARS::post("SUSPEND");         //出停日数
        $this->field["MOURNING"]       = VARS::post("MOURNING");        //忌引日数
        $this->field["SICK"]           = VARS::post("SICK");            //病欠
        $this->field["NOTICE"]         = VARS::post("NOTICE");          //事故欠届
        $this->field["NONOTICE"]       = VARS::post("NONOTICE");        //事故欠無
        $this->field["LATE"]           = VARS::post("LATE");            //遅刻回数
        $this->field["EARLY"]          = VARS::post("EARLY");           //早退回数
        $this->field["VIRUS"]          = VARS::post("VIRUS");           //出停（伝染病）日数
        $this->field["KOUDOME"]        = VARS::post("KOUDOME");         //出停（交止）日数

        $c002Field = preg_split("/,/", $this->set_detail_field);
        $c002Field = is_array($c002Field) ? $c002Field : array();
        foreach ($c002Field as $c002Key => $c002Val) {
            $c002Val = str_replace("DETAIL_", "", $c002Val);
            $c002Val = str_replace("[]", "", $c002Val);
            $this->field["DETAIL_".$c002Val]     = VARS::post("DETAIL_".$c002Val);
        }
    }

    /* 入力項目チェック */
    function validateInput()
    {
        $db = Query::dbCheckOut();

        /* 更新対象データがない場合 */
        if ($this->field["MONTH"] == NULL || $this->field["MONTH"] == "") {
            $this->setWarning("MSG303");
            return false;
        }

        foreach ($this->field["MONTH"] as $key => $month) {
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
            $this->field["MOURNING"][$key] != "" ||
            $this->field["SICK"][$key] != "" ||
            $this->field["NOTICE"][$key] != "" ||
            $this->field["NONOTICE"][$key] != "" ||
            $this->field["LATE"][$key] != "" ||
            $this->field["EARLY"][$key] != "")
        {
            return true;
        }

        if ($this->Properties["useVirus"] == "true" && $this->field["VIRUS"][$key] != "") {
            return true;
        }
        if ($this->Properties["useKoudome"] == "true" && $this->field["KOUDOME"][$key] != "") {
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
        $semeday = $db->getRow(knjc032cQuery::selectSemesAll($monthAr[1]),DB_FETCHMODE_ASSOC);
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
        $result = knjc032cQuery::getUpdateQuery($this);
        return $result;
    }
}
?>