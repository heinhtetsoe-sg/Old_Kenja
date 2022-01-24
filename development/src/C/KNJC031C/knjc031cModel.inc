<?php

require_once('for_php7.php');

class knjc031cModel extends Model {

    var $cmd;
    var $field = array();
    var $reset = array();
    var $appointed_day;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    var $set_detail_field;

    function knjc031cModel()
    {
        $this->field = array();
    }

    function init()
    {
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
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd               = VARS::request("cmd");
        $this->school_kind       = ($this->school_kind != "") ? $this->school_kind : "";
        $this->appointed_day     = VARS::post("SET_APPOINTED_DAY");     //締め日
        $this->field["hr_class"] = VARS::post("HR_CLASS");  //対象学級
        $this->field["month"]    = VARS::post("MONTH");     //対象月

        /* 学年・学級に分割 */
        list($this->field["grade"],$this->field["class"]) = explode("-",$this->field["hr_class"]);

        $this->set_detail_field = VARS::post("SET_DETAIL_FIELD");
        /* 編集対象データを取得 */
        $this->field["SCHREGNO"]    = VARS::post("SCHREGNO");   //学籍番号
        $this->field["LESSON"]      = VARS::post("LESSON");     //授業日数
        $this->field["OFFDAYS"]     = VARS::post("OFFDAYS");    //休学日数
        $this->field["ABROAD"]      = VARS::post("ABROAD");     //留学日数
        $this->field["ABSENT"]      = VARS::post("ABSENT");     //公欠日数
        $this->field["SUSPEND"]     = VARS::post("SUSPEND");    //出停日数
        $this->field["MOURNING"]    = VARS::post("MOURNING");   //忌引日数
        $this->field["SICK"]        = VARS::post("SICK");       //欠席
        $this->field["NOTICE"]      = VARS::post("NOTICE");     //事故欠届
        $this->field["NONOTICE"]    = VARS::post("NONOTICE");   //事故欠無
        $this->field["LATE"]        = VARS::post("LATE");       //遅刻回数
        $this->field["EARLY"]       = VARS::post("EARLY");      //早退回数
        if ($this->Properties["useVirus"] == "true") {
            $this->field["VIRUS"]       = VARS::post("VIRUS");      //出停（伝染病）日数
        }
        if ($this->Properties["useKoudome"] == "true") {
            $this->field["KOUDOME"]     = VARS::post("KOUDOME");    //出停（交止）日数
        }
        $c002Field = preg_split("/,/", $this->set_detail_field);
        $c002Field = is_array($c002Field) ? $c002Field : array();
        foreach ($c002Field as $c002Key => $c002Val) {
            $c002Val = str_replace("DETAIL_", "", $c002Val);
            $c002Val = str_replace("[]", "", $c002Val);
            $this->field["DETAIL_".$c002Val]     = VARS::post("DETAIL_".$c002Val);
        }

        $this->reset = array();
        if (is_array($this->field["SCHREGNO"])) {
            foreach ($this->field["SCHREGNO"] as $key => $schregno) {
                $this->reset[$schregno] = array("LESSON"       => $this->field["LESSON"][$key],
                                                "OFFDAYS"      => $this->field["OFFDAYS"][$key],
                                                "ABROAD"       => $this->field["ABROAD"][$key],
                                                "ABSENT"       => $this->field["ABSENT"][$key],
                                                "SUSPEND"      => $this->field["SUSPEND"][$key],
                                                "MOURNING"     => $this->field["MOURNING"][$key],
                                                "SICK"         => $this->field["SICK"][$key],
                                                "NOTICE"       => $this->field["NOTICE"][$key],
                                                "NONOTICE"     => $this->field["NONOTICE"][$key],
                                                "LATE"         => $this->field["LATE"][$key],
                                                "EARLY"        => $this->field["EARLY"][$key]);
                if ($this->Properties["useVirus"] == "true") {
                    $this->reset[$schregno]["VIRUS"] = $this->field["VIRUS"][$key];
                }
                if ($this->Properties["useKoudome"] == "true") {
                    $this->reset[$schregno]["KOUDOME"] = $this->field["KOUDOME"][$key];
                }
                foreach ($c002Field as $c002Key => $c002Val) {
                    $c002Val = str_replace("DETAIL_", "", $c002Val);
                    $c002Val = str_replace("[]", "", $c002Val);
                    $this->reset[$schregno]["DETAIL_".$c002Val] = $this->field["DETAIL_".$c002Val][$key];
                }
            }
        }
    }

    /* 入力項目チェック */
    function validateInput()
    {
        $db = Query::dbCheckOut();

        if(!isset($this->field["SCHREGNO"])){
            $this->setMessage("MSG304");
            return false;
        }

        $monthAr = preg_split("/-/", $this->field["month"]);

        //締め日の最終日
        $year = CTRL_YEAR;
        if ($monthAr[0] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime( 0, 0, 0, $monthAr[0], 1, $year ));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->appointed_day) {
            $this->setWarning("MSG901",$monthAr[0]."月の最終日は、".$lastday."日です。");
            return false;
        }
        //締め日が0の場合
        if (0 == $this->appointed_day) {
            $this->setWarning("MSG901","0日は存在しません。");
            return false;
        }

        $semeday = $db->getRow(knjc031cQuery::selectSemesAll($monthAr[1]),DB_FETCHMODE_ASSOC);
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $semeday["S_MONTH"]) == $monthAr[0] &&
            $semeday["S_DAY"] > $this->appointed_day) {
            $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。");
            return false;
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $monthAr[0] &&
            $semeday["E_DAY"] < $this->appointed_day) {
            $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
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
        $result = knjc031cQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
